package visiontek.djicontroller.forms.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.LatLng;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;

import java.util.ArrayList;
import java.util.List;

import dji.common.error.DJIError;
import dji.common.useraccount.UserAccountState;
import dji.common.util.CommonCallbacks;
import dji.sdk.mission.timeline.TimelineEvent;
import dji.sdk.useraccount.UserAccountManager;
import visiontek.djicontroller.R;
import visiontek.djicontroller.dataManager.TaskManager;
import visiontek.djicontroller.models.SrtmData;
import visiontek.djicontroller.models.TaskViewModel;
import visiontek.djicontroller.orm.FlyAreaPoint;
import visiontek.djicontroller.orm.FlyTask;
import visiontek.djicontroller.orm.HeightAreaPoint;
import visiontek.djicontroller.util.AmapTool;
import visiontek.djicontroller.util.Common;
import visiontek.djicontroller.util.FlightControllerTool;

//任务控制Tab
public class TaskControlFragment extends MapFragment {
    private TaskViewModel taskViewModel;
    private AMap aMap;
    private AmapTool maptool;
    TaskManager taskManager=null;
    private FlightControllerTool flightControllerTool;
    private Spinner switch_aircraftmap;
    private ImageButton djilogin;
    private ImageButton locationbtn;
    private ImageButton startpausebtn;
    private SeekBar taskprogress;
    private TextView compeletePointSize;
    private TextView totalPointsCount;
    private TextView flightHeight;
    private TextView lineSpace;
    private TextView pointSpace;
    private TextView totalFlyLinesLength;
    private TextView flightHeightInArea;
    public static final String FLAG_CONNECTION_CHANGE = "dji_sdk_connection_change";
    protected BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(flightControllerTool!=null){
                if(intent.getAction().equals(FLAG_CONNECTION_CHANGE)){
                    flightControllerTool.initFlightController();
                }
            }
        }
    };
    @Override
    void onMapInit(AMap map){
        aMap=map;
        maptool=new AmapTool(aMap,getContext());
        flightControllerTool=new FlightControllerTool(maptool, getContext(), new FlightControllerTool.OnTaskStatusChanged() {
            @Override
            public void onPaused() {
                Common.ShowQMUITipToast(getContext(),"任务停止", QMUITipDialog.Builder.ICON_TYPE_INFO,1500);
                refreshUIStatus(0);
            }
            @Override
            public void onStarted() {
                Common.ShowQMUITipToast(getContext(),"任务启动", QMUITipDialog.Builder.ICON_TYPE_INFO,1500);
                refreshUIStatus(1);
            }
            @Override
            public void onError(TimelineEvent event, @Nullable DJIError djiError) {
                Common.ShowQMUITipToast(getContext(),djiError.toString(), QMUITipDialog.Builder.ICON_TYPE_FAIL,3000);
            }
            @Override
            public void onPointReached(int index,int total){
                refreshUIStatus(taskViewModel.taskstatus);
            }
        });
        taskManager=new TaskManager();
        aMap.setMapType(AMap.MAP_TYPE_NORMAL);//普通地图 卫星地图MAP_TYPE_SATELLITE
        aMapLocation();
        IntentFilter filter = new IntentFilter();
        filter.addAction(FLAG_CONNECTION_CHANGE);
        getActivity().registerReceiver(mReceiver, filter);
    }
    @Override
    public void onDestroy() {
        getActivity().unregisterReceiver(mReceiver);
        super.onDestroy();
    }
    @Override
    void setOnTaskLoad(String id){
        if(taskViewModel!=null&&taskViewModel.taskstatus==1){//防止在飞行的时候强制改变对象
            return;
        }
        else{
            taskViewModel= taskManager.getTask(id);
            if(taskViewModel!=null){
                maptool.ClearDrawArea();
                List<FlyAreaPoint> list=taskManager.getFlyAreaPoints(taskViewModel.id);
                maptool.LoadTask2UI(taskViewModel,list);
                flightControllerTool.setTask(taskViewModel);//绑定到飞行控制
                totalFlyLinesLength.setText((int)maptool.GetTotalFlyLinesLength()+"km");
                flightHeight.setText((int)taskViewModel.FlyHeight+"m");
                lineSpace.setText((int)taskViewModel.lineSpace+"m");
                pointSpace.setText((int)taskViewModel.pointSpace+"m");
                List<HeightAreaPoint> heightAreaPointList=taskManager.getHeightAreaPoints(taskViewModel.id);
                if(heightAreaPointList!=null&&heightAreaPointList.size()>0){
                    List<LatLng> res=new ArrayList<>();
                    for(int i=0;i<heightAreaPointList.size();i++){
                        res.add(new LatLng(heightAreaPointList.get(i).lat,heightAreaPointList.get(i).lon));
                    }
                    maptool.LoadDrawArea(res,heightAreaPointList.get(0).color);
                    flightHeightInArea.setText(heightAreaPointList.get(0).height+"m");
                }
                else{
                    flightHeightInArea.setText("N");
                }
                refreshUIStatus(taskViewModel.taskstatus);//更新UI
            }
        }
        maptool.enableEditArea(false);//任务执行时禁止编辑飞行区域
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.task_control_widgets,null);
        View taskinfo=layout.findViewById(R.id.taskinfo);
        flightHeight=taskinfo.findViewById(R.id.flightHeight);
        lineSpace=taskinfo.findViewById(R.id.lineSpace);
        pointSpace=taskinfo.findViewById(R.id.pointSpace);
        totalFlyLinesLength=taskinfo.findViewById(R.id.totalFlyLinesLength);
        flightHeightInArea=taskinfo.findViewById(R.id.flightHeightInArea);
        switch_aircraftmap=layout.findViewById(R.id.switch_aircraftmap);
        switch_aircraftmap.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                flightControllerTool.setAircraftMappingStyle(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        compeletePointSize=layout.findViewById(R.id.compeletePointSize);
        totalPointsCount=layout.findViewById(R.id.totalPointsCount);
        djilogin=layout.findViewById(R.id.djilogin);
        djilogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginClick();
            }
        });
        locationbtn=layout.findViewById(R.id.locationbtn);
        locationbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                aMapLocation();
            }
        });
        startpausebtn=layout.findViewById(R.id.startpausebtn);

        startpausebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                controlClick();
            }
        });
        taskprogress=layout.findViewById(R.id.taskprogress);
        taskprogress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(taskViewModel!=null){
                    taskViewModel.currentStart=i;
                    taskViewModel.compeletePointSize=i==0?0:i+1;
                    flightControllerTool.setTask(taskViewModel);
                    compeletePointSize.setText(taskViewModel.compeletePointSize+"");
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        return layout;

    }
    private void aMapLocation(){//地图定位
        if(maptool!=null){
            maptool.StartLocation(new AmapTool.LocationListener() {
                @Override
                public void onLocationSuccess(LatLng var1) {
                    aMap.moveCamera(CameraUpdateFactory.newLatLng(var1));
                    aMap.moveCamera(CameraUpdateFactory.zoomTo(15));
                    Common.ShowQMUITipToast(getContext(),"定位完成", QMUITipDialog.Builder.ICON_TYPE_SUCCESS,500);
                }
                @Override
                public void onLocationFail(String msg) {
                    Common.ShowQMUITipToast(getContext(),msg, QMUITipDialog.Builder.ICON_TYPE_FAIL,500);
                }
            });
        }
    }

    private void LoginClick(){
        UserAccountState state=  UserAccountManager.getInstance().getUserAccountState();
        if(state.value()==0){
            loginAccount();
        }
        else{
            new QMUIDialog.MessageDialogBuilder(getActivity())
                    .setTitle("退出登录")
                    .setMessage("确定要退出大疆账号吗？")
                    .addAction("取消", new QMUIDialogAction.ActionListener() {
                        @Override
                        public void onClick(QMUIDialog dialog, int index) {
                            dialog.dismiss();
                        }
                    })
                    .addAction("确定", new QMUIDialogAction.ActionListener() {
                        @Override
                        public void onClick(QMUIDialog dialog, int index) {
                            dialog.dismiss();
                            logoutAccount();
                        }
                    })
                    .create().show();
        }
    }
    private void loginAccount(){
        UserAccountManager.getInstance().logIntoDJIUserAccount(getContext(),
                new CommonCallbacks.CompletionCallbackWith<UserAccountState>() {
                    @Override
                    public void onSuccess(final UserAccountState userAccountState) {
                        Common.ShowQMUITipToast(getContext(),"登录成功", QMUITipDialog.Builder.ICON_TYPE_SUCCESS,500);
                    }
                    @Override
                    public void onFailure(DJIError error) {
                        if(error!=null)
                        Common.ShowQMUITipToast(getContext(),error.toString(), QMUITipDialog.Builder.ICON_TYPE_FAIL,1000);
                    }
                });
    }
    private void logoutAccount(){
        UserAccountManager.getInstance().logoutOfDJIUserAccount(new CommonCallbacks.CompletionCallback(){
            @Override
            public void onResult(DJIError djiError) {
                Common.ShowQMUITipToast(getContext(),"登录退出", QMUITipDialog.Builder.ICON_TYPE_INFO,500);
            }
        });
    }
    private void controlClick(){
        //flightControllerTool.StartTask(taskViewModel);
        if(!flightControllerTool.isFlightConnected()){
            Common.ShowQMUITipToast(getContext(),"设备未连接", QMUITipDialog.Builder.ICON_TYPE_FAIL,1000);
            return;
        }
        UserAccountState state=  UserAccountManager.getInstance().getUserAccountState();
        if(state.value()==0){
            loginAccount();
        }
        else if(taskViewModel==null){
            Common.ShowQMUITipToast(getContext(),"请先加载一个任务航线", QMUITipDialog.Builder.ICON_TYPE_FAIL,500);
            return;
        }
        if(taskViewModel.taskstatus==1){//正在运行
            flightControllerTool.pauseTask();
        }
        else{//未开始
            new QMUIDialog.MessageDialogBuilder(getActivity())
                    .setTitle("任务执行")
                    .setMessage("当前开始位置"+taskprogress.getProgress()+",确定开始?")
                    .addAction("取消", new QMUIDialogAction.ActionListener() {
                        @Override
                        public void onClick(QMUIDialog dialog, int index) {
                            dialog.dismiss();
                        }
                    })
                    .addAction("确定", new QMUIDialogAction.ActionListener() {
                        @Override
                        public void onClick(QMUIDialog dialog, int index) {
                            dialog.dismiss();
                            flightControllerTool.StartTask(taskViewModel);
                        }
                    })
                    .create().show();
        }
    }
    private void refreshUIStatus(final int taskstatus){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setCount();
                switch (taskstatus){
                    case 0:{//停止
                        startpausebtn.setImageDrawable(getResources().getDrawable(R.drawable.start,null));
                        taskprogress.setEnabled(true);
                        break;
                    }
                    case 1:{//运行中
                        startpausebtn.setImageDrawable(getResources().getDrawable(R.drawable.pause,null));
                        taskprogress.setEnabled(false);
                        break;
                    }
                    default:startpausebtn.setImageDrawable(getResources().getDrawable(R.drawable.start,null));break;
                }
            }
        });
    }
    private void setCount(){
        taskViewModel=flightControllerTool.getCurrentTask();//重新获取航点进度
        totalPointsCount.setText(flightControllerTool.getTotal()+"");
        if(taskViewModel!=null){
            int total=flightControllerTool.getTotal()==0?0:flightControllerTool.getTotal()-1;
            taskprogress.setMax(total);
            int progress=taskViewModel.compeletePointSize==0?0:taskViewModel.compeletePointSize-1;
            taskprogress.setProgress(progress);
            compeletePointSize.setText(progress+"");
        }
        else{
            compeletePointSize.setText("0");
        }
    }
}
