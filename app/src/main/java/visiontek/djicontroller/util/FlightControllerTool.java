package visiontek.djicontroller.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.google.android.gms.common.util.MapUtils;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import dji.common.battery.BatteryState;
import dji.common.camera.SettingsDefinitions;
import dji.common.error.DJIError;
import dji.common.flightcontroller.ConnectionFailSafeBehavior;
import dji.common.flightcontroller.ControlMode;
import dji.common.flightcontroller.FlightControllerState;
import dji.common.gimbal.Attitude;
import dji.common.gimbal.Rotation;
import dji.common.mission.waypoint.Waypoint;
import dji.common.mission.waypoint.WaypointAction;
import dji.common.mission.waypoint.WaypointActionType;
import dji.common.mission.waypoint.WaypointMission;
import dji.common.mission.waypoint.WaypointMissionDownloadEvent;
import dji.common.mission.waypoint.WaypointMissionExecutionEvent;
import dji.common.mission.waypoint.WaypointMissionFinishedAction;
import dji.common.mission.waypoint.WaypointMissionFlightPathMode;
import dji.common.mission.waypoint.WaypointMissionGotoWaypointMode;
import dji.common.mission.waypoint.WaypointMissionHeadingMode;
import dji.common.mission.waypoint.WaypointMissionUploadEvent;
import dji.common.model.LocationCoordinate2D;
import dji.common.remotecontroller.AircraftMappingStyle;
import dji.common.util.CommonCallbacks;
import dji.sdk.airlink.AirLink;
import dji.sdk.base.BaseComponent;
import dji.sdk.base.BaseProduct;
import dji.sdk.battery.Battery;
import dji.sdk.camera.Camera;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.flightcontroller.Simulator;
import dji.sdk.mission.MissionControl;
import dji.sdk.mission.timeline.TimelineElement;
import dji.sdk.mission.timeline.TimelineEvent;
import dji.sdk.mission.timeline.TimelineMission;
import dji.sdk.mission.timeline.actions.GimbalAttitudeAction;
import dji.sdk.mission.timeline.actions.GoToAction;
import dji.sdk.mission.waypoint.WaypointMissionOperator;
import dji.sdk.mission.waypoint.WaypointMissionOperatorListener;
import dji.sdk.products.Aircraft;
import dji.sdk.remotecontroller.RemoteController;
import visiontek.djicontroller.DJIApplication;
import visiontek.djicontroller.R;
import visiontek.djicontroller.dataManager.TaskManager;
import visiontek.djicontroller.models.SrtmData;
import visiontek.djicontroller.models.TaskViewModel;
import visiontek.djicontroller.orm.FlyAreaPoint;
import visiontek.djicontroller.orm.FlyCamera;
import visiontek.djicontroller.orm.HeightAreaPoint;

public class FlightControllerTool {
    FlightController mFlightController;//飞行控制对象
    RemoteController remoteController;//遥控器对象
    AirLink airLink;//无线信号链接
    AmapTool amapTool;//扩展高德地图工具
    LocationCoordinate2D homeLocation;//飞机起飞时大疆获取的坐标
    LatLng amapHomeLocation;//高德地图定位的起始位置
    LocationCoordinate2D flightposition;//飞行器的实时位置
    CollectLog clog = CollectLog.getInstance();//日志监控
    TaskManager taskManager;//数据持久化控制器

    TaskViewModel currentTask;//当前正在执行的任务
    List<HeightAreaPoint> heightArea;//变高区域
    List<LatLng> allPointsInTask;//全部的航点
    List<LatLng> currentPage;//当前要执行的航点
    ResHelper resHelper;//资源映射帮助
    MissionControl missionControl;//任务控制对象
    Handler mUIHandler = new Handler(Looper.getMainLooper());
    private ProgressDialog mLoadingDialog;
    Context _context;
    public FlightControllerTool(AmapTool maptool, Context context,OnTaskStatusChanged event){
        amapTool=maptool;
        _context=context;
        taskManager=new TaskManager();
        resHelper=new ResHelper(context);
        onTaskStatusChanged=event;
        mLoadingDialog = new ProgressDialog(context);
        mLoadingDialog.setCanceledOnTouchOutside(true);
        mLoadingDialog.setCancelable(true);
        mLoadingDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

    }

    public TaskViewModel getCurrentTask(){
        return currentTask;
    }
    //获取相机
    public Camera getFlightCamera(){
        BaseProduct product = DJIApplication.getProductInstance();
        if (product != null && product.isConnected()) {
            if (product instanceof Aircraft) {
                return ((Aircraft) product).getCamera();
            }
        }
        return null;
    }
    //设置控制器控制方式
    public void setAircraftMappingStyle(int type){//0日本手，1美国手,2中国手
         if(remoteController!=null){
             switch (type){
                 case 0:remoteController.setAircraftMappingStyle(AircraftMappingStyle.STYLE_1,null);break;
                 case 1:remoteController.setAircraftMappingStyle(AircraftMappingStyle.STYLE_2,null);break;
                 case 2:remoteController.setAircraftMappingStyle(AircraftMappingStyle.STYLE_3,null);break;
                 default:break;
             }
         }
    }
    private void GetHomeLocation(){
        if(mFlightController!=null){
            mFlightController.getHomeLocation(new CommonCallbacks.CompletionCallbackWith<LocationCoordinate2D>() {
                @Override
                public void onSuccess(LocationCoordinate2D locationCoordinate2D) {
                    homeLocation = locationCoordinate2D;
                }
                @Override
                public void onFailure(DJIError djiError) {

                }
            });
        }
    }
    //初始化
    public void initFlightController() {
            final BaseProduct product = DJIApplication.getProductInstance();
            amapTool.HomePointDraggable(true);
            if (product != null && product.isConnected()) {
                amapTool.HomePointDraggable(false);//此时禁止修改降落位置
                if (product instanceof Aircraft) {
                    mFlightController = ((Aircraft) product).getFlightController();
                    remoteController=((Aircraft) product).getRemoteController();
                    setAircraftMappingStyle(0);//默认重置为日本手
                    Battery battery=product.getBattery();
                    if(battery!=null){
                        battery.setStateCallback(new BatteryState.Callback() {
                            @Override
                            public void onUpdate(BatteryState djiBatteryState) {
                                int val= djiBatteryState.getChargeRemainingInPercent();
                                if(val<=30){//电池低于30直接返航
                                    pauseTask();
                                }
                            }
                        });
                    }
                }
            }
            if (mFlightController != null) {
                mFlightController.setControlMode(ControlMode.SMART,null);//智能控制模式。飞机可以在智能模式下稳定其高度和姿态。
                mFlightController.setConnectionFailSafeBehavior(ConnectionFailSafeBehavior.UNKNOWN,null);//连接失败不GOHOME继续跑
                mFlightController.setSmartReturnToHomeEnabled(true,null);//电量只够回家,必须回家
                mFlightController.setMaxFlightHeight(500, null);//设置最大可飞行高度
                mFlightController.setMaxFlightRadiusLimitationEnabled(false,null);//关闭半径限制
                mFlightController.setLowBatteryWarningThreshold(35,null);//35%警告
                mFlightController.setSeriousLowBatteryWarningThreshold(25,null);//20严重掉电警告
                //mFlightController.getFlightAssistant().setRTHObstacleAvoidanceEnabled(false,null);//开启避障4.6不支持
                GetHomeLocation();
                mFlightController.setStateCallback(
                        new FlightControllerState.Callback() {
                            @Override
                            public void onUpdate(FlightControllerState djiFlightControllerCurrentState) {
                                double droneLocationLat = djiFlightControllerCurrentState.getAircraftLocation().getLatitude();
                                double droneLocationLng = djiFlightControllerCurrentState.getAircraftLocation().getLongitude();
                                LocationCoordinate2D position = new LocationCoordinate2D(droneLocationLat, droneLocationLng);//获取飞机的实时位置
                                if(checkLocation(position.getLatitude(),position.getLongitude())){
                                    flightposition=position;
                                    if(!djiFlightControllerCurrentState.isHomeLocationSet()){
                                        //如果起始点未设置则默认当前的位置为起始点(理论上和高德地图定位设置的起飞点相同实际有偏差)
                                        SetHomeLocation(position);
                                    }
                                    updateDroneLocation(position);//更新位置
                                    /*if(mFlightController.isConnected()&&mFlightController.getState().isGoingHome()&&currentTask.taskstatus==1){
                                        pauseTask();
                                    }*/
                                    String val= refreshCurrentIndex(position);
                                    if(val!=null){
                                        int index=Integer.parseInt(val);
                                        SetTaskProgress(index);
                                    }
                                }
                            }
                 });
                mFlightController.setComponentListener(new BaseComponent.ComponentListener() {
                    @Override
                    public void onConnectivityChange(boolean b) {
                        if(currentTask!=null){
                            if(b){
                                clog.saveLogInfo("信号恢复",currentTask.id);
                            }
                            else{
                                clog.saveLogInfo("信号丢失",currentTask.id);
                            }
                        }
                    }
                });
            }
    }

    private String refreshCurrentIndex(LocationCoordinate2D position){
        //任务必须正在运行
        if(currentTask!=null&&currentTask.taskstatus==1&&allPointsInTask!=null&&position!=null
                &&amapHomeLocation!=null&&homeLocation!=null){
            int size=allPointsInTask.size();
            for(int i=0;i<size;i++){//先确定在当前已经上传的航线上
                LatLng point=allPointsInTask.get(i);
                LatLng flightposition=FlightLocation2MapLocation(position);
                float dis=AMapUtils.calculateLineDistance(flightposition,point);
                int angle=currentTask.cameradirection==0?90:0;
                int yaw=currentTask.airwayangle+angle;
                if(yaw>180){
                    yaw=yaw-180;
                }
                float ayaw= (float)mFlightController.getState().getAttitude().yaw;
                if(Math.abs(Math.abs(yaw)-Math.abs(ayaw))<5&&dis<5){//在5米以内范围而且飞行器角度和计算值偏差低于5度认为到达此点
                    return String.valueOf(i);
                }
            }
        }
        return null;//找不到返回null标记
    }
    private void SetTaskProgress(int index){
        if(allPointsInTask!=null){
            currentTask.compeletePointSize=index+1;
            if(currentTask.currentStart!=index){//此判定大幅减少数据库操作
                currentTask.currentStart=index;
                clog.saveLogInfo("到达点"+index,currentTask.id);
                SetStartPoint(index);
                onTaskStatusChanged.onPointReached(currentTask.compeletePointSize,allPointsInTask.size());
                taskManager.SaveTask(currentTask);
            }
        }
    }
    public Boolean isFlightConnected(){
        return mFlightController!=null;
    }
    private Marker flightMarker;//飞行器在地图上的标注
    //飞机位置产生变化时更新飞机位置
    private void updateDroneLocation(final LocationCoordinate2D flightLocation){
        amapHomeLocation=amapTool.get_homePoint();
        if(amapHomeLocation!=null&&flightLocation!=null&&homeLocation!=null){
            final LatLng pos = FlightLocation2MapLocation(flightLocation);
            mUIHandler.post(new Runnable() {
                @Override
                public void run() {
                    if(checkLocation(pos.latitude,pos.longitude)){
                        if(flightMarker!=null){
                            flightMarker.setPosition(pos);
                        }
                        else{
                            flightMarker=amapTool.AddPoint(R.drawable.aircraft,pos,false,null,null,0.5f,0.5f);
                        }
                        float yaw= (float)mFlightController.getState().getAttitude().yaw;
                        flightMarker.setRotateAngle(-yaw);//设置飞行器方向
                    }
                }
            });
        }
    }
    //设置飞机的起飞和降落地点
    private void SetHomeLocation(final LocationCoordinate2D position){
        if(position!=null){
            if(checkLocation(position.getLatitude(),position.getLongitude())){
                homeLocation=position;
                mFlightController.setHomeLocation(homeLocation, new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                        if(djiError!=null){
                            Common.ShowQMUITipToast(_context,"设置起飞点失败", QMUITipDialog.Builder.ICON_TYPE_FAIL,500);
                        }
                    }
                });
            }
        }
    }
    //检查经纬度是否在合理范围内
    private boolean checkLocation(double latitude,double longitude){
        return (latitude > -90 && latitude < 90 && longitude > -180 && longitude < 180) && (latitude != 0f
                && longitude != 0f);
    }
    //把飞机所在的经纬度转换到地图上的经纬度（有距离偏差）
    private LatLng FlightLocation2MapLocation(LocationCoordinate2D locationCoordinate2D){//隔着一段距离
        double distanceLat=amapHomeLocation.latitude- homeLocation.getLatitude();
        double distanceLon=amapHomeLocation.longitude-homeLocation.getLongitude();
        LatLng newLocation=new LatLng(locationCoordinate2D.getLatitude()+distanceLat,locationCoordinate2D.getLongitude()+distanceLon);
        return newLocation;
    }
    //把地图上的经纬度转换为飞机接受的经纬度（有距离偏差）
    private LocationCoordinate2D MapLocation2FlightLocation(LatLng location){
        double distanceLat=amapHomeLocation.latitude- homeLocation.getLatitude();
        double distanceLon=amapHomeLocation.longitude-homeLocation.getLongitude();
        LocationCoordinate2D newLocation=new LocationCoordinate2D(location.latitude-distanceLat,location.longitude-distanceLon);
        return newLocation;
    }
    //开始任务执行
    private Boolean startTimeline() {
        if(currentTask!=null&&MissionControl.getInstance()!=null){
            if (MissionControl.getInstance().scheduledCount() > 0) {
                MissionControl.getInstance().startTimeline();
                currentTask.taskstatus=1;
                taskManager.SaveTask(currentTask);
                onTaskStatusChanged.onStarted();
                return true;
            }
        }
        return false;
    }
    //点击暂停按钮触发
    public void pauseTask() {
        if(currentTask!=null&&allPointsInTask!=null&&allPointsInTask.size()>0){
           currentTask.taskstatus=0;
           taskManager.SaveTask(currentTask);
           stopShootPhoto();
           MissionControl.getInstance().stopTimeline();
           onTaskStatusChanged.onPaused();
           clog.saveLogInfo("任务暂停",currentTask.id);
            goHome();
        }
    }
    //初始化航点
    public Boolean initTimeline(MissionControl.Listener listener){
        List<TimelineElement> timelineElements=null;//时间线节点对象
        clog.saveLogInfo("已完成航点数量"+currentTask.compeletePointSize,currentTask.id);
        amapHomeLocation=amapTool.get_homePoint();
        Boolean hover=currentTask.hover==1;
        allPointsInTask=GetAllPointInTask(hover,currentTask.pointSpace);//生成全部航点
        List<LatLng> points=GetPagedPoints(currentTask.compeletePointSize,99);//获取要执行的页默认按99分页后期根据限制会产生变化
        int size=points.size();
        int angle=currentTask.cameradirection==0?90:0;
        int yaw=currentTask.airwayangle+angle;//设置偏航角度范围必须是+-180
        if(yaw>180){
            yaw=yaw-180;
        }
        if(currentTask.isThridCamera==0){//大疆自带相机
            timelineElements=getTimeLineWithCameraSettings(points,currentTask.FlyHeight,currentTask.pitch,currentTask.speed,currentTask.pointSpace,hover,yaw);
        }
        else{//第三方相机,不控制相机参数直接飞行
            timelineElements=getTimeLineWithoutCameraSettings(points,currentTask.FlyHeight,currentTask.speed,currentTask.yaw);
        }
        mFlightController.setGoHomeHeightInMeters(currentTask.GoHomeHeight,null);//设置返航高度
        clog.saveLogInfo("当前点数:"+points.size()+"|全部点数:"+allPointsInTask.size(),currentTask.id);
        clog.saveLogInfo("当前航线长度:"+getWayPointsTotalLength(points)+"|总长度:"+getWayPointsTotalLength(allPointsInTask),currentTask.id);
        if(timelineElements.size()>0){
            SetMissionControl(timelineElements,listener);
            currentTask.currentEnd=currentTask.currentStart+size-1;
            taskManager.SaveTask(currentTask);
            return true;
        }
        else{
            return false;
        }
    }
    public void initTimeline(final MissionControl.Listener listener,final onSrtmMissionCompelete srtmInitListener){
        clog.saveLogInfo("已完成航点数量"+currentTask.compeletePointSize,currentTask.id);
        amapHomeLocation=amapTool.get_homePoint();
        Boolean hover=currentTask.hover==1;
        allPointsInTask=GetAllPointInTask(hover,currentTask.pointSpace);//生成全部航点
        List<LatLng> points=GetPagedPoints(currentTask.compeletePointSize,99);//获取要执行的页默认按99分页后期根据限制会产生变化
        final int size=points.size();
        LoadSrtmData(points, new onSrtmWaypointInit() {
            @Override
            public void onCompelete(WaypointMission mission) {
                List<TimelineElement> timelineElements=new ArrayList<>();
                TimelineElement element = TimelineMission.elementFromWaypointMission(mission);
                DJIError error=mission.checkParameters();
                if(error!=null){
                    Common.ShowQMUITipToast(_context,error.toString(), QMUITipDialog.Builder.ICON_TYPE_FAIL,500);
                    clog.saveLogInfo("检查路点已经存在错误:"+error,currentTask.id);
                }
                else{
                    timelineElements.add(element);
                    mFlightController.setGoHomeHeightInMeters(currentTask.GoHomeHeight,null);//设置返航高度
                    if(timelineElements.size()>0){
                        SetMissionControl(timelineElements,listener);
                        currentTask.currentEnd=currentTask.currentStart+size-1;
                        taskManager.SaveTask(currentTask);
                        srtmInitListener.onCompelete();
                    }
                }
            }
        });
    }
    private void SetStartPoint(int i){
        if(allPointsInTask!=null&&allPointsInTask.size()>0){
            if(i>=allPointsInTask.size()){
                i=allPointsInTask.size()-1;
            }
            amapTool.setStartPoint(allPointsInTask.get(i));
            currentTask.compeletePointSize=i==0?0:i+1;
            onTaskStatusChanged.onPointReached(currentTask.compeletePointSize,allPointsInTask.size());
        }
    }
    private void SetEndPoint(int i){
        if(allPointsInTask!=null&&allPointsInTask.size()>0) {
            if(i>=allPointsInTask.size()||i==0){
                i=allPointsInTask.size()-1;
            }
            amapTool.setEndPoint(allPointsInTask.get(i));
        }
    }
    private  void updatePregress(final int progress,final int max){
        mUIHandler.post(new Runnable() {
            public void run() {
                if (mLoadingDialog != null) {
                    mLoadingDialog.setMessage("正在上传...");
                    mLoadingDialog.setMax(max);
                    mLoadingDialog.setProgress(progress);
                }
            }
        });
    }
    private void showProgressDialog() {
        mUIHandler.post(new Runnable() {
            public void run() {
                if (mLoadingDialog != null) {
                    mLoadingDialog.show();
                }
            }
        });
    }
    private void hideProgressDialog() {
        mUIHandler.post(new Runnable() {
            public void run() {
                if (null != mLoadingDialog && mLoadingDialog.isShowing()) {
                    mLoadingDialog.dismiss();
                }
            }
        });
    }
    private WaypointMissionOperatorListener waypointMissionOperatorListener=new WaypointMissionOperatorListener() {
        @Override
        public void onDownloadUpdate(@NonNull WaypointMissionDownloadEvent waypointMissionDownloadEvent) {
        }
        @Override
        public void onUploadUpdate(@NonNull WaypointMissionUploadEvent waypointMissionUploadEvent) {
            DJIError djiError=waypointMissionUploadEvent.getError();
            if(djiError!=null){//仍可上传成功
            }
            if(waypointMissionUploadEvent!=null&&waypointMissionUploadEvent.getProgress()!=null)
            {

                int total= waypointMissionUploadEvent.getProgress().totalWaypointCount;
                int current=waypointMissionUploadEvent.getProgress().uploadedWaypointIndex+1;
                if(current==1){
                    showProgressDialog();
                }
                updatePregress(current,total);
                if(current==total){
                    hideProgressDialog();
                }
            }
        }
        @Override
        public void onExecutionUpdate(@NonNull WaypointMissionExecutionEvent waypointMissionExecutionEvent) {
            DJIError djiError=waypointMissionExecutionEvent.getError();
            if(djiError!=null){
                clog.saveLogInfo("路点执行时出错:"+djiError,currentTask.id);
            }
        }
        @Override
        public void onExecutionStart() {
        }
        @Override
        public void onExecutionFinish(@Nullable DJIError djiError) {
            if(djiError!=null){
                clog.saveLogInfo("执行完成时出错:"+djiError,currentTask.id);
            }
        }
    };
    //设置任务控制对象
    private void SetMissionControl(List<TimelineElement> elements,MissionControl.Listener listener){
        missionControl = MissionControl.getInstance();
        WaypointMissionOperator operator =missionControl.getWaypointMissionOperator();
        if (missionControl.scheduledCount() > 0) {
            missionControl.unscheduleEverything();
            missionControl.removeAllListeners();
            operator.removeListener(waypointMissionOperatorListener);
        }
        operator.addListener(waypointMissionOperatorListener);
        missionControl.scheduleElements(elements);
        missionControl.addListener(listener);
    }
    //使用第三方挂载相机直接跳过相机相关
    private List<TimelineElement> getTimeLineWithoutCameraSettings(List<LatLng> points, float altitude,float speed,int yaw){
        List<TimelineElement> elements = new ArrayList<>();
        WaypointMission.Builder waypointMissionBuilder = new WaypointMission.Builder().autoFlightSpeed(speed)
                .maxFlightSpeed(15f)
                .setExitMissionOnRCSignalLostEnabled(false)//如果信号丢失继续执行
                .finishedAction(WaypointMissionFinishedAction.GO_HOME)//安全起见一定要返航
                .flightPathMode(
                        WaypointMissionFlightPathMode.NORMAL)
                .gotoFirstWaypointMode(
                        WaypointMissionGotoWaypointMode.SAFELY)
                .headingMode(
                        WaypointMissionHeadingMode.USING_INITIAL_DIRECTION);
        List<Waypoint> waypoints =new ArrayList<>();
        int size=points.size();
        for(int i=0;i<size;i++){
            LatLng taskPoint=points.get(i);
            LocationCoordinate2D  point=MapLocation2FlightLocation(taskPoint);//传入飞机之前要转换
            Waypoint waypoint=new Waypoint(point.getLatitude(),point.getLongitude(),altitude);
            waypoint.addAction(new WaypointAction(WaypointActionType.STAY,1000));//必须增加这个无效动作否则一条航线结束时会导致触发器提前触发
            if(i==0){//在一条航线的起点设置y轴
                WaypointAction aircraftYawAction=new WaypointAction(WaypointActionType.ROTATE_AIRCRAFT,yaw);
                waypoint.addAction(aircraftYawAction);
            }
            waypoints.add(waypoint);
        }
        waypoints=SetHeightAreaPoints(waypoints,points);
        waypointMissionBuilder.waypointList(waypoints).waypointCount(waypoints.size());
        WaypointMission mission=waypointMissionBuilder.build();
        TimelineElement element= TimelineMission.elementFromWaypointMission(mission);
        DJIError error=mission.checkParameters();
        if(error!=null){
            Common.ShowQMUITipToast(_context,error.toString(), QMUITipDialog.Builder.ICON_TYPE_FAIL,1000);
            clog.saveLogInfo("检查路点已经存在错误:"+error,currentTask.id);
        }
        //丢失信号时触发器会失效没用
        //List<Trigger> tgs= GenTriggers(mission.getWaypointCount());
        //element.setTriggers(tgs);
        elements.add(element);
        return  elements;
    }

    //使用大疆默认相机
    private List<TimelineElement> getTimeLineWithCameraSettings(List<LatLng> points, float altitude,
                             float pitch,float speed,float space, Boolean hover,int yaw){
        List<TimelineElement> elements = new ArrayList<>();
        FlyCamera camera=currentTask.cameraInfo;
        //Step1:设置相机参数
        Camera djiCamera= DJIApplication.getProductInstance().getCamera();
        if(djiCamera!=null) {//每次都要重置相机
            djiCamera.restoreFactorySettings(null);//重置相机
            djiCamera.setMode(SettingsDefinitions.CameraMode.SHOOT_PHOTO, null);
            //设置变焦镜头的焦距。它仅支持X5，X5R和X5S相机，镜头为Olympus M.Zuiko ED 14-42mm f / 3.5-5.6 EZ，Z3相机和Z30相机。
            djiCamera.setOpticalZoomFocalLength((int) camera.focallength, null);
            djiCamera.setISO(resHelper.getIsoValues(camera.iso), null);
            djiCamera.setAperture(resHelper.getApertureValues(camera.aperture), null);
            djiCamera.setExposureCompensation(resHelper.getExposureValues(camera.exposurecompensation), null);
            djiCamera.setShutterSpeed(resHelper.getShutterValues(camera.shutterspeed), null);
            djiCamera.setPhotoFileFormat(resHelper.getImageTypes(camera.imgtype), null);
            //Step 2: 设置云台P轴俯仰角度
            Attitude attitude = new Attitude(-pitch, Rotation.NO_ROTATION, Rotation.NO_ROTATION);
            GimbalAttitudeAction gimbalAction = new GimbalAttitudeAction(attitude);
            gimbalAction.setCompletionTime(2);
            elements.add(gimbalAction);
            if(currentTask.compeletePointSize==0){//首次执行记录日志
                clog.saveLogInfo("设置相机为拍照模式",currentTask.id);
                clog.saveLogInfo("设置相机拍焦距:"+camera.focallength,currentTask.id);
                clog.saveLogInfo("设置相机ISO:"+resHelper.getIsoValues(camera.iso),currentTask.id);
                clog.saveLogInfo("设置相机AV:"+resHelper.getApertureValues(camera.aperture),currentTask.id);
                clog.saveLogInfo("设置相机EV:"+resHelper.getExposureValues(camera.exposurecompensation),currentTask.id);
                clog.saveLogInfo("设置相机快门:"+resHelper.getShutterValues(camera.shutterspeed),currentTask.id);
                clog.saveLogInfo("设置图片格式:"+resHelper.getImageTypes(camera.imgtype),currentTask.id);
                clog.saveLogInfo("设置P轴:"+pitch,currentTask.id);
                clog.saveLogInfo("悬停"+currentTask.hover,currentTask.id);
                clog.saveLogInfo("是否默认相机"+currentTask.isThridCamera,currentTask.id);
                clog.saveLogInfo("飞行高度"+currentTask.FlyHeight,currentTask.id);
                clog.saveLogInfo("俯仰角度"+currentTask.pitch,currentTask.id);
                clog.saveLogInfo("飞行速度"+currentTask.speed,currentTask.id);
                clog.saveLogInfo("拍照间距"+currentTask.pointSpace,currentTask.id);
                clog.saveLogInfo("y轴角度"+yaw,currentTask.id);
            }
        }
        //Step 4:执行航线
        WaypointMission mission=initWaypointMission(points,speed,altitude,space,hover,yaw);
        TimelineElement element = TimelineMission.elementFromWaypointMission(mission);
        DJIError error=mission.checkParameters();
        if(error!=null){
            Common.ShowQMUITipToast(_context,error.toString(), QMUITipDialog.Builder.ICON_TYPE_FAIL,500);
            clog.saveLogInfo("检查路点已经存在错误:"+error,currentTask.id);
        }
        //丢失信号时触发器会无效
        //List<Trigger> tgs= GenTriggers(mission.getWaypointCount());
        //element.setTriggers(tgs);
        elements.add(element);
        return  elements;
    }
    private WaypointMission initWaypointMission(List<LatLng> points,float speed,float altitude,
                                                float space,Boolean hover,int yaw) {
        WaypointMission.Builder waypointMissionBuilder = new WaypointMission.Builder().autoFlightSpeed(speed)
                .maxFlightSpeed(15f)
                .setExitMissionOnRCSignalLostEnabled(false)
                .finishedAction(WaypointMissionFinishedAction.GO_HOME)//安全起见一定要返航
                .flightPathMode(
                        WaypointMissionFlightPathMode.NORMAL)
                .gotoFirstWaypointMode(
                        WaypointMissionGotoWaypointMode.SAFELY)
                .headingMode(
                        WaypointMissionHeadingMode.USING_INITIAL_DIRECTION);
        List<Waypoint> waypoints =new ArrayList<>();
        for(int i=0;i<points.size();i++){
            LatLng taskPoint=points.get(i);
            LocationCoordinate2D  point=MapLocation2FlightLocation(taskPoint);//传入飞机之前要转换
            Waypoint waypoint=new Waypoint(point.getLatitude(),point.getLongitude(),altitude);
            waypoint.addAction(new WaypointAction(WaypointActionType.STAY,1000));//必须有此动作防止触发器提前触发
            if(i==0){//到达第一个点时调整飞行器飞行角度
                WaypointAction aircraftYawAction=new WaypointAction(WaypointActionType.ROTATE_AIRCRAFT,yaw);
                waypoint.addAction(aircraftYawAction);
            }
            if(!hover){//不悬停直接等距离拍摄
                waypoint.shootPhotoDistanceInterval=space;
            }
            else{
                waypoint.addAction(new WaypointAction(WaypointActionType.START_TAKE_PHOTO,1));//悬停直接拍1张照片
            }
            waypoints.add(waypoint);
        }
        //if()//勾选贴地飞行则重新全部处理
        waypoints=SetHeightAreaPoints(waypoints,points);
        waypointMissionBuilder.waypointList(waypoints).waypointCount(waypoints.size());
        return waypointMissionBuilder.build();
    }
    //生成一条航线上拆分的航点结尾处不够长度不补以真实点结束（不悬停）
    private List<LatLng> GetWarypointInLine(LatLng start ,LatLng end){
        float limit=999;//以999分段保证一条线上至少2个点以上且不超过2Km
        List<LatLng> res=new ArrayList<>();
        res.add(start);
        float distance= AMapUtils.calculateLineDistance(start,end);
        int steps = (int)(distance / limit);
        for(int i=0;i<steps;i++){
            double lat=(end.latitude-start.latitude)*limit*(i+1)/(distance)+start.latitude;
            double lon=(end.longitude-start.longitude)*limit*(i+1)/(distance)+start.longitude;
            LatLng point=new LatLng(lat,lon);
            if(i==steps-1){//最后一个点
                if(AMapUtils.calculateLineDistance(point,end)>5f){//最后一个点距离结尾点超过5米要保留结尾
                    res.add(point);
                    res.add(end);
                }
                else{//5米 内把此点当作结尾,忽略真实结尾防止过近出错
                    res.add(point);
                }
            }
            else{
                res.add(point);
            }
        }
        return  res;
    }
    //生成一条航线上拆分的航点结尾处不够长度补全（悬停）
    private List<LatLng> GetWarypointInLine(LatLng start ,LatLng end,float space){
        List<LatLng> res=new ArrayList<>();
        res.add(start);
        float distance= AMapUtils.calculateLineDistance(start,end);
        int steps = (int)(distance / space)+1;
        for(int i=0;i<steps;i++){
            double lat=(end.latitude-start.latitude)*space*(i+1)/(distance)+start.latitude;
            double lon=(end.longitude-start.longitude)*space*(i+1)/(distance)+start.longitude;
            LatLng point=new LatLng(lat,lon);
            res.add(point);
        }
        return  res;
    }
    //获取全部航线上生成的航点
    private List<LatLng> GetAllPointInTask(Boolean hover,float space){
        List<LatLng> points=amapTool.getPointsFlyLines();
        List<LatLng> allPoints=new ArrayList<>();
        if(points==null){
            return new ArrayList<>();
        }
        else{
            for(int i=0;i<points.size();i++){
                if((i+1)%2==0){//是一条航线的终点
                    LatLng start=points.get(i-1);
                    LatLng end=points.get(i);
                    if(hover){//悬停距离不会超过2000
                        List<LatLng> pointsinLine=GetWarypointInLine(start,end,space);//一条航线上的全部航点
                        allPoints.addAll(pointsinLine);
                    }
                    else{
                        float distance=AMapUtils.calculateLineDistance(start,end);
                        if(distance>=2000){//航线距离超过2KM 此处要加点拆分突破2000
                            List<LatLng> pointsinLine=GetWarypointInLine(start,end);
                            allPoints.addAll(pointsinLine);
                        }
                        else{//没超出范围直接加
                            allPoints.add(start);
                            allPoints.add(end);
                        }
                    }
                }
            }
            return   allPoints;
        }
    }
    //获取集合中航线的总长度
    private float getWayPointsTotalLength(List<LatLng> list){
        float total=0f;
        if(list!=null){
            for (int i=0;i<list.size();i++) {
                if(i>0){
                    LatLng point1=list.get(i-1);
                    LatLng point2=list.get(i);
                    total=total+AMapUtils.calculateLineDistance(point1,point2);
                }
            }
        }
        return total;
    }
    private float getWayPointsTotalLengthW(List<Waypoint> list){
        float total=0f;
        if(list!=null){
            for (int i=0;i<list.size();i++) {
                if(i>0){
                    LatLng point1=FlightLocation2MapLocation(list.get(i-1).coordinate);
                    LatLng point2=FlightLocation2MapLocation(list.get(i).coordinate);
                    total=total+AMapUtils.calculateLineDistance(point1,point2);
                }
            }
        }
        return total;
    }
    //检查任务点中2KM的奇葩限制 ERROR：WAYPOINT_DISTANCE_TOO_LONG
    //航点距离太长。两个相邻航点之间的有效距离小于2km且大于0.5m。
    //此外，任务的第一个和最后一个航点还必须具有小于2千米且大于0.5米的间隔。
    //如果任何连续航路点的分离或第一个和最后一个航路点的分离大于2km，则会引发该错误。
    private List<LatLng> Check2KMLimt(List<LatLng> source){
        LatLng end=source.get(source.size()-1);
        for(int i=0;i<source.size();i++){
            LatLng point=source.get(i);
            float distance=AMapUtils.calculateLineDistance(point,end);
            if(distance>=2000){//超过2KM 从此处截断只能抛弃该终点留到下一页执行
                List<LatLng>  res= source.subList(0,source.indexOf(end));//不包括end
                return Check2KMLimt(res);
            }
        }
        return source;
    }
    //获取当前执行页
    //航点之间所有距离的总和限制,最大30KM。ERROR：WAYPOINT_TRACE_TOO_LONG
    //总数量不能超过99个点数量限制
    // 1.幻影2视觉/视觉+：最大点数限制为16.
    // 2.Inspire1/Phantom3系列：最大点数限制为99。
    //总航点任务距离太长限制。最大总距离为40公里。ERROR：WAYPOINT_TOTAL_TRACE_TOO_LONG
    //总距离包括以下总和：
    //1.从当前飞机位置到第一航点的距离;2.航点之间所有距离的总和;3.从最后一个航路点到本地点的距离。
    private List<LatLng> GetPagedPoints(int compeleteCount, int pageSize){//pageSize默认从99开始距离超出时开始-2递减
        List<LatLng> res=new ArrayList<>();
        int index=compeleteCount==0?0:compeleteCount-1;
        int left=allPointsInTask.size()-compeleteCount;
        if(left-pageSize==1||left<pageSize){//倒数第二页留给最后一页只有一个航点,最后一页会出问题
            pageSize=left;//剩余不足一页或除去一页剩下1个航点直接一次传完
        }
        for (int i=index;i<=index+pageSize;i++){
            if(i<allPointsInTask.size()){
                LatLng item= allPointsInTask.get(i);
                res.add(item);
            }
        }
        float totalLength=getWayPointsTotalLength(res);
        if(res.size()>0&&pageSize>0){
            float flight2start=AMapUtils.calculateLineDistance(flightMarker.getPosition(),res.get(0));
            float home2end=AMapUtils.calculateLineDistance(amapHomeLocation,res.get(pageSize-1));
            if(totalLength>=25000||totalLength+flight2start+home2end>=35000){//超出限制了要减点直至不超出，剩余的下次执行
                return GetPagedPoints(compeleteCount,pageSize-2);//-2不-1 是防止上面剩余1个点时产生死循环
            }
            else{
                currentPage=Check2KMLimt(res);//最后一次处理2KM 防止飞机不飞
                return  currentPage;
            }
        }
        else return res;
    }
    //停止相机拍照状态
    private void stopShootPhoto(){
        Camera camera=getFlightCamera();
        if(camera!=null){
            camera.stopShootPhoto(null);//停止拍照
        }
    }
    //返回
    private void goHome(){
        if(mFlightController!=null){
            mFlightController.startGoHome(new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
                    if(djiError!=null){
                        clog.saveLogInfo("返航失败"+djiError,currentTask.id);
                    }
                    else{
                        clog.saveLogInfo("开始返航",currentTask.id);
                    }
                }
            });
        }
    }
    //原地降落 不安全!!
    private void land(){
        if(mFlightController!=null){
            mFlightController.startLanding(new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
                    if(djiError!=null)
                        clog.saveLogInfo("降落失败"+djiError,currentTask.id);
                    else
                        clog.saveLogInfo("自动降落",currentTask.id);
                }
            });
        }
    }
    private Boolean CheckTask(){
        if(flightMarker==null){
            Common.ShowQMUITipToast(_context,"飞行器未连接", QMUITipDialog.Builder.ICON_TYPE_INFO,1000);
            return false;
        }
        if(mFlightController==null){
            Common.ShowQMUITipToast(_context,"设备未连接", QMUITipDialog.Builder.ICON_TYPE_INFO,1000);
            return false;
        }
        amapHomeLocation=amapTool.get_homePoint();
        if(amapHomeLocation==null){
            Common.ShowQMUITipToast(_context,"请先定位当前位置", QMUITipDialog.Builder.ICON_TYPE_INFO,1000);
            return false;
        }
        if(currentTask.FlyHeight>500||currentTask.FlyHeight<=1){
            Common.ShowQMUITipToast(_context,"当前飞行高度不合理[1-500]", QMUITipDialog.Builder.ICON_TYPE_INFO,1000);
            return false;
        }
        if(heightArea!=null&&heightArea.size()>0){
            int height=heightArea.get(0).height;
            if(height<=1||height>500){
                Common.ShowQMUITipToast(_context,"变高高度不合理[1-500]", QMUITipDialog.Builder.ICON_TYPE_INFO,1000);
                return false;
            }
        }
        if(!mFlightController.getState().isFlying()) {
            mFlightController.startTakeoff(null);
        }
        if(flightposition!=null){//当前飞机位置
            clog.saveLogInfo("起飞点飞行器经纬度："+homeLocation.getLatitude()+"#"+homeLocation.getLongitude(),currentTask.id);
            clog.saveLogInfo("起飞点地图经纬度："+amapHomeLocation.latitude+"#"+amapHomeLocation.longitude,currentTask.id);
            LatLng flightPosition=FlightLocation2MapLocation(flightposition);
            float area2flight= AMapUtils.calculateLineDistance(flightPosition,amapTool.getStartPointLocation());
            if(area2flight>2000){
                Common.ShowQMUITipToast(_context,"飞行器距离目标位置太远", QMUITipDialog.Builder.ICON_TYPE_FAIL,500);
                return false;
            }
        }
        return true;
    }
    //开始任务
    public Boolean StartTask(TaskViewModel task){
        currentTask=task;
        if(currentTask!=null){
            if(CheckTask()){
                taskManager.SaveTask(task);
                Boolean res= initTimeline(missionListener);
                if(res){
                    clog.saveLogInfo("初始化航点成功",currentTask.id);
                    return startTimeline();
                }
                else{
                    clog.saveLogInfo("初始化航点失败",currentTask.id);
                }
                /*if(task.isSrtm==1){
                    initTimeline(missionListener, new onSrtmMissionCompelete() {
                        @Override
                        public void onCompelete() {
                            startTimeline();//读取完成后开始任务
                        }
                    });
                }
                else{

                }*/
            }
            return false;
        }
        else{
            Common.ShowQMUITipToast(_context,"未找到该任务,请重试", QMUITipDialog.Builder.ICON_TYPE_FAIL,500);
            return false;
        }
    }
    private OnTaskStatusChanged onTaskStatusChanged;
    //提供对外监视接口以便更新UI
    public interface OnTaskStatusChanged{
        void onPaused();
        void onStarted();
        void onPointReached(int current,int total);
        void onError(TimelineEvent event,@Nullable DJIError djiError);
    }
    MissionControl.Listener missionListener=new MissionControl.Listener() {
        @Override
        public void onEvent(@Nullable TimelineElement timelineElement, TimelineEvent timelineEvent, @Nullable DJIError djiError) {
        if(djiError!=null){
                onTaskStatusChanged.onError(timelineEvent,djiError);
            }
        }
    };

    private Marker tempEndMarker;
    //向当前航线中插入变高区域边界并执行DJI限制设置
    private List<Waypoint> SetHeightAreaPoints(List<Waypoint> waypoints,List<LatLng> points){
        List<Waypoint> result=new ArrayList<>();
        if(heightArea!=null&&heightArea.size()>0&&points!=null&&waypoints!=null&&waypoints.size()>0){
            int height= heightArea.get(0).height;//变高区域
            float altitude=waypoints.get(0).altitude;//原区域高度
            int count=0;
            for(int i=0;i<points.size();i++){
                Waypoint item=waypoints.get(i);
                if(pointInHeightArea(heightArea,points.get(i))){//点在变高区域内的航点首先改变其高度
                    item.altitude=height;
                    //amapTool.AddPoint(R.drawable.pointshow,currentPage.get(i),false,null,null,0.5f,0.5f);DEBUG显示
                }
                if(i==points.size()-1){//临时终点
                    if(tempEndMarker!=null){
                        tempEndMarker.setPosition(points.get(i));
                    }
                    else{
                        tempEndMarker=amapTool.AddPoint(R.drawable.pointshow,points.get(i),false,null,null,0.5f,0.5f);
                    }
                }
                if(i>0){//每两个航点为线段与变高区域求交点插入新点
                    LatLng start=points.get(i-1);//线段起点
                    LatLng end=points.get(i);//线段终点
                    List<LatLng> crossPoints=getCrossPointInHeightArea(start,end);//线段与变高边界求交
                    count=count+crossPoints.size();//每产生一个交点表示一次变高动作
                    float distance=0;
                    if(crossPoints.size()>1){//2个交点的情况
                        LatLng in,out;
                        float dis0=AMapUtils.calculateLineDistance(crossPoints.get(0),end);
                        float dis1=AMapUtils.calculateLineDistance(crossPoints.get(1),end);
                        if(dis0>dis1){//1是出0是进
                            distance=dis1;
                            in=crossPoints.get(0);out=crossPoints.get(1);
                        }
                        else{//1是进0是出
                            distance=dis0;//取最小
                            in=crossPoints.get(1);out=crossPoints.get(0);
                        }
                        //进入点的处理
                        LocationCoordinate2D pntIn=MapLocation2FlightLocation(in);
                        Waypoint inHeight=new Waypoint(pntIn.getLatitude(),pntIn.getLongitude(),height);//新高度的航点
                        Waypoint waypoint0=new Waypoint(pntIn.getLatitude(),pntIn.getLongitude(),altitude);//旧高度的航点
                        inHeight.addAction(new WaypointAction(WaypointActionType.STAY,1000));//必须增加这个无效动作否则一条航线结束时会导致触发器提前触发
                        waypoint0.addAction(new WaypointAction(WaypointActionType.STAY,1000));
                        result.add(waypoint0);//加在交点前面实现变高
                        result.add(inHeight);//加交点

                        //走出点的处理
                        LocationCoordinate2D pntOut=MapLocation2FlightLocation(out);
                        Waypoint outHeight=new Waypoint(pntOut.getLatitude(),pntOut.getLongitude(),height);//新高度的航点
                        Waypoint waypoint1=new Waypoint(pntOut.getLatitude(),pntOut.getLongitude(),altitude);//旧高度的航点
                        outHeight.addAction(new WaypointAction(WaypointActionType.STAY,1000));//必须增加这个无效动作否则一条航线结束时会导致触发器提前触发
                        waypoint1.addAction(new WaypointAction(WaypointActionType.STAY,1000));
                        result.add(outHeight);
                        result.add(waypoint1);//加在交点后面实现高度恢复
                    }
                    else if(crossPoints.size()==1){//1个交点的情况
                        distance=AMapUtils.calculateLineDistance(crossPoints.get(0),end);
                        Boolean flag=true;
                        if(!pointInHeightArea(heightArea, end)){
                            flag=false;
                        }
                        LocationCoordinate2D pnt=MapLocation2FlightLocation(crossPoints.get(0));
                        Waypoint heightWpnt=new Waypoint(pnt.getLatitude(),pnt.getLongitude(),height);//新高度的航点
                        Waypoint waypoint=new Waypoint(pnt.getLatitude(),pnt.getLongitude(),altitude);//旧高度的航点
                        heightWpnt.addAction(new WaypointAction(WaypointActionType.STAY,1000));//必须增加这个无效动作否则一条航线结束时会导致触发器提前触发
                        waypoint.addAction(new WaypointAction(WaypointActionType.STAY,1000));
                        if(flag){
                            result.add(waypoint);//加在交点前面实现变高
                            result.add(heightWpnt);//加交点
                        }
                        else{
                            result.add(heightWpnt);
                            result.add(waypoint);//加在交点后面实现高度恢复
                        }
                    }
                    if(distance>=5||crossPoints.size()==0){//低于5米直接抛弃如果没有交点也要保留
                        result.add(item);//加入当前点到航线上
                    }
                }
                else{
                    result.add(item);
                }
            }
            //此时因为增加了航点造成航程增加极有可能超出限制这里要再次处理防止超出(症状为反复重复上传航点直至超时)
            float distance= (Math.abs(altitude-height))*count;//高度差
            float total= distance+getWayPointsTotalLengthW(result);
            while (total>=25000||result.size()>99){//25KM和99限制
                result=result.subList(0,result.size()-2);
                total= distance+getWayPointsTotalLengthW(result);
            }

            return result;
        }
        else{
            return waypoints;//不存在变高区域直接返回旧航点
        }
    }


    private LatLng getScaleOut(LatLng p1,LatLng p2,double scale){
        double x1=p1.latitude;
        double y1=p1.longitude;
        double x2=p2.latitude;
        double y2=p2.longitude;
        double percent=scale/ AMapUtils.calculateLineDistance(p1,p2);
        double x=x1-(x1-x2)*percent;
        double y=y1-(y1-y2)*percent;
        return new LatLng(x,y);
    }
    //获取与变高区域边界的交点一条航线可能与区域边界有多个交点
    List<LatLng> getCrossPointInHeightArea(LatLng start,LatLng end){
        List<LatLng> result=new ArrayList<>();
        if(heightArea!=null&&heightArea.size()>0){
            for(int i=0;i<heightArea.size();i++){
                if(i>0){
                    LatLng startL=new LatLng(heightArea.get(i-1).lat,heightArea.get(i-1).lon);
                    LatLng endL=new LatLng(heightArea.get(i).lat,heightArea.get(i).lon);
                    LatLng res=GetCrossPoint(start,end,startL,endL);
                    if(res!=null){
                        result.add(res);
                    }
                }
            }
            //起点和终点的线段不能丢掉
            LatLng startL=new LatLng(heightArea.get(heightArea.size()-1).lat,heightArea.get(heightArea.size()-1).lon);
            LatLng endL=new LatLng(heightArea.get(0).lat,heightArea.get(0).lon);
            LatLng res=GetCrossPoint(start,end,startL,endL);
            if(res!=null){
                result.add(res);
            }
        }
        return result;
    }

     //排斥实验
     Boolean IsRectCross(LatLng p1,LatLng p2,LatLng q1, LatLng q2)
    {
        Boolean ret = Math.min(p1.latitude,p2.latitude) <= Math.max(q1.latitude,q2.latitude)    &&
                Math.min(q1.latitude,q2.latitude) <= Math.max(p1.latitude,p2.latitude) &&
                Math.min(p1.longitude,p2.longitude) <= Math.max(q1.longitude,q2.longitude) &&
                Math.min(q1.longitude,q2.longitude) <= Math.max(p1.longitude,p2.longitude);
             return ret;
    }
    //跨立判断
    Boolean IsLineSegmentCross(LatLng P1,LatLng P2,LatLng Q1,LatLng Q2)
    {
        if(((Q1.latitude-P1.latitude)*(Q1.longitude-Q2.longitude)-(Q1.longitude-P1.longitude)*( Q1.latitude-Q2.latitude))
                        * ((Q1.latitude-P2.latitude)*(Q1.longitude-Q2.longitude)-(Q1.longitude-P2.longitude)*(Q1.latitude-Q2.latitude)) < 0 &&
                        ((P1.latitude-Q1.latitude)*(P1.longitude-P2.longitude)-(P1.longitude-Q1.longitude)*(P1.latitude-P2.latitude))
                                * ((P1.latitude-Q2.latitude)*(P1.longitude-P2.longitude)-(P1.longitude-Q2.longitude)*( P1.latitude-P2.latitude)) < 0
                )
            return true;
        else
            return false;
    }
    //获取线段的交点
    LatLng GetCrossPoint(LatLng p1,LatLng p2,LatLng q1,LatLng q2)
    {
        double x,y;
             if(IsRectCross(p1,p2,q1,q2))
                 {
                    if (IsLineSegmentCross(p1,p2,q1,q2))
                         {
                             //求交点
                            double tmpLeft,tmpRight;
                            tmpLeft = (q2.latitude - q1.latitude) * (p1.longitude - p2.longitude) - (p2.latitude - p1.latitude) * (q1.longitude - q2.longitude);
                            tmpRight =(p1.longitude - q1.longitude) * (p2.latitude - p1.latitude) * (q2.latitude - q1.latitude)
                                    + q1.latitude * (q2.longitude - q1.longitude) * (p2.latitude - p1.latitude)
                                    - p1.latitude * (p2.longitude - p1.longitude) * (q2.latitude - q1.latitude);
                             x = (double)tmpRight/(double)tmpLeft;
                             tmpLeft = (p1.latitude - p2.latitude) * (q2.longitude - q1.longitude) - (p2.longitude - p1.longitude) * (q1.latitude - q2.latitude);
                             tmpRight = p2.longitude * (p1.latitude - p2.latitude) * (q2.longitude - q1.longitude)
                                     + (q2.latitude- p2.latitude) * (q2.longitude - q1.longitude) * (p1.longitude - p2.longitude)
                                     - q2.longitude * (q1.latitude - q2.latitude) * (p2.longitude - p1.longitude);
                             y = (double)tmpRight/(double)tmpLeft;
                             return new LatLng(x,y);
                         }
                }
          return null;
    }
    //判定点在多边形内
    private Boolean pointInHeightArea(List<HeightAreaPoint> area,LatLng pt){//lat-x lon-y
        List<LatLng> list=new ArrayList<>();
        for(int i=0;i<area.size();i++){
            HeightAreaPoint apt=area.get(i);
            list.add(new LatLng(apt.lat,apt.lon));
        }
        return  isPolygonContainsPoint(list,pt)||isPointInPolygonBoundary(list,pt);
    }
    public static boolean isPolygonContainsPoint(List<LatLng> mPoints, LatLng point) {
        int nCross = 0;
        for (int i = 0; i < mPoints.size(); i++) {
            LatLng p1 = mPoints.get(i);
            LatLng p2 = mPoints.get((i + 1) % mPoints.size());
            // 取多边形任意一个边,做点point的水平延长线,求解与当前边的交点个数
            // p1p2是水平线段,要么没有交点,要么有无限个交点
            if (p1.longitude == p2.longitude)
                continue;
            // point 在p1p2 底部 --> 无交点
            if (point.longitude < Math.min(p1.longitude, p2.longitude))
                continue;
            // point 在p1p2 顶部 --> 无交点
            if (point.longitude >= Math.max(p1.longitude, p2.longitude))
                continue;
            // 求解 point点水平线与当前p1p2边的交点的 X 坐标
            double x = (point.longitude - p1.longitude) * (p2.latitude - p1.latitude) / (p2.longitude - p1.longitude) + p1.latitude;
            if (x > point.latitude) // 当x=point.x时,说明point在p1p2线段上
                nCross++; // 只统计单边交点
        }
        // 单边交点为偶数，点在多边形之外 ---
        return (nCross % 2 == 1);
    }
    public static boolean isPointInPolygonBoundary(List<LatLng> mPoints, LatLng point) {
        for (int i = 0; i < mPoints.size(); i++) {
            LatLng p1 = mPoints.get(i);
            LatLng p2 = mPoints.get((i + 1) % mPoints.size());
            // 取多边形任意一个边,做点point的水平延长线,求解与当前边的交点个数
            // point 在p1p2 底部 --> 无交点
            if (point.longitude < Math.min(p1.longitude, p2.longitude))
                continue;
            // point 在p1p2 顶部 --> 无交点
            if (point.longitude > Math.max(p1.longitude, p2.longitude))
                continue;

            // p1p2是水平线段,要么没有交点,要么有无限个交点
            if (p1.longitude == p2.longitude) {
                double minX = Math.min(p1.latitude, p2.latitude);
                double maxX = Math.max(p1.latitude, p2.latitude);
                // point在水平线段p1p2上,直接return true
                if ((point.longitude == p1.longitude) && (point.latitude >= minX && point.latitude <= maxX)) {
                    return true;
                }
            } else { // 求解交点
                double x = (point.longitude - p1.longitude) * (p2.latitude - p1.latitude) / (p2.longitude - p1.longitude) + p1.latitude;
                if (x == point.latitude) // 当x=point.x时,说明point在p1p2线段上
                    return true;
            }
        }
        return false;
    }

    //设置当前要执行的任务对象
    public void setTask(TaskViewModel task){
        currentTask=task;
        GetHomeLocation();
        if(currentTask!=null){
            heightArea=taskManager.getHeightAreaPoints(task.id);
            allPointsInTask=GetAllPointInTask(currentTask.hover==1,currentTask.pointSpace);//生成全部航点

            SetStartPoint(currentTask.currentStart);
            SetEndPoint(allPointsInTask.size());
        }
    }
    //获取全部的点数量
    public int getTotal(){
        if(allPointsInTask!=null){
           return allPointsInTask.size();
        }
        return 0;
    }
    private String readToString(String fileName) {
        String encoding = "UTF-8";
        File file = new File(fileName);
        Long filelength = file.length();
        byte[] filecontent = new byte[filelength.intValue()];
        try {
            FileInputStream in = new FileInputStream(file);
            in.read(filecontent);
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            return new String(filecontent, encoding);
        } catch (UnsupportedEncodingException e) {
            System.err.println("The OS does not support " + encoding);
            e.printStackTrace();
            return null;
        }
    }

    private interface onSrtmWaypointInit{
        void onCompelete(WaypointMission mission);
    }
    private interface onSrtmMissionCompelete{
        void onCompelete();
    }
    private void LoadSrtmData(List<LatLng> points,final onSrtmWaypointInit onSrtmCompelete){
        if(currentTask!=null&&currentTask.srtmDataFile!=null&&currentTask.srtmDataFile.length()>0){//存在直接读取
            String pathFolder= Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "visiontekLogFiles";
            String pathFile=pathFolder+currentTask.id+"_srtm.txt";
            File file = new File(pathFile);
            if(file.exists()){
                String res=readToString(pathFile);
                try {
                    List<SrtmData> srtmData = JSON.parseArray(res,SrtmData.class);
                    int angle=currentTask.cameradirection==0?90:0;
                    int yaw=currentTask.airwayangle+angle;//设置偏航角度范围必须是+-180
                    if(yaw>180){
                        yaw=yaw-180;
                    }
                    WaypointMission mission=initSrtmWarypoint(srtmData,currentTask.speed,currentTask.pointSpace,yaw);
                    onSrtmCompelete.onCompelete(mission);
                }
                catch (Exception e){
                    Log.i("srtm","序列化失败FlightControllerTool.LoadSrtmData()");
                }
            }
        }
        else{
            SrtmElevationTool srtmElevationTool=new SrtmElevationTool(currentTask.id, new SrtmElevationTool.onSrtmDataDownload() {
                @Override
                public void onCompelete(List<SrtmData> _srtmData) {
                    hideProgressDialog();
                    int angle=currentTask.cameradirection==0?90:0;
                    int yaw=currentTask.airwayangle+angle;//设置偏航角度范围必须是+-180
                    if(yaw>180){
                        yaw=yaw-180;
                    }
                    WaypointMission mission=initSrtmWarypoint(_srtmData,currentTask.speed,currentTask.pointSpace,yaw);
                    onSrtmCompelete.onCompelete(mission);
                }
                @Override
                public void onProgress(int total, int current) {
                    mLoadingDialog.setMessage("正在下载高程数据...");
                    mLoadingDialog.setMax(total);
                    mLoadingDialog.setProgress(current);
                    if(current==1){
                        showProgressDialog();
                    }
                }
            });
            srtmElevationTool.StartDownloadSrtmData(points);//不存在通过网络下载
        }
    }
    private WaypointMission initSrtmWarypoint(List<SrtmData> srtmData,float speed,
                                              float space,int yaw){

        WaypointMission.Builder waypointMissionBuilder = new WaypointMission.Builder().autoFlightSpeed(speed)
                .maxFlightSpeed(15f)
                .setExitMissionOnRCSignalLostEnabled(false)
                .finishedAction(WaypointMissionFinishedAction.GO_HOME)//安全起见一定要返航
                .flightPathMode(
                        WaypointMissionFlightPathMode.NORMAL)
                .gotoFirstWaypointMode(
                        WaypointMissionGotoWaypointMode.SAFELY)
                .headingMode(
                        WaypointMissionHeadingMode.USING_INITIAL_DIRECTION);
        List<Waypoint> waypoints =new ArrayList<>();
        for(int i=0;i<srtmData.size();i++){
            SrtmData srtm=srtmData.get(i);
            LatLng taskPoint=new LatLng(srtm.lat,srtm.lon);
            LocationCoordinate2D  point=MapLocation2FlightLocation(taskPoint);//传入飞机之前要转换
            //飞行高度=参数计算高度-（起飞点海拔-真实DEM海拔)
            float altitude=currentTask.BasePlaneHeight-(currentTask.homeASL-(float)srtm.height);
            Waypoint waypoint=new Waypoint(point.getLatitude(),point.getLongitude(),altitude);
            waypoint.addAction(new WaypointAction(WaypointActionType.STAY,1000));//必须有此动作防止触发器提前触发
            if(i==0){//到达第一个点时调整飞行器飞行角度
                WaypointAction aircraftYawAction=new WaypointAction(WaypointActionType.ROTATE_AIRCRAFT,yaw);
                waypoint.addAction(aircraftYawAction);
            }
            waypoint.shootPhotoDistanceInterval=space;
            waypoints.add(waypoint);
        }
        waypointMissionBuilder.waypointList(waypoints).waypointCount(waypoints.size());
        return waypointMissionBuilder.build();
    }
}
