package visiontek.djicontroller.forms.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.model.LatLng;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;
import com.qmuiteam.qmui.widget.popup.QMUIListPopup;
import com.qmuiteam.qmui.widget.popup.QMUIPopup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import visiontek.djicontroller.R;
import visiontek.djicontroller.dataManager.TaskManager;

import visiontek.djicontroller.forms.Adapter.TaskListViewAdapter_new;
import visiontek.djicontroller.forms.dialogs.CalculateHightDialog;
import visiontek.djicontroller.forms.dialogs.KMLLoadDialog;
import visiontek.djicontroller.forms.dialogs.TaskInfoDialog;
import visiontek.djicontroller.forms.userControl.ColorPickerView;
import visiontek.djicontroller.forms.userControl.PullRefreshTaskList;
import visiontek.djicontroller.models.TaskViewModel;
import visiontek.djicontroller.models.kml.Color;
import visiontek.djicontroller.orm.FlyAreaPoint;
import visiontek.djicontroller.orm.HeightAreaPoint;
import visiontek.djicontroller.util.AmapTool;
import visiontek.djicontroller.util.Common;

//航线规划Tab
public class TaskEditFragment extends MapFragment{
    public static final String ON_EDITAREA_ENABLED= "ON_EDITAREA_ENABLED";

    private AMap aMap;
    private AmapTool maptool;
    TaskManager taskManager=null;
    Bundle _savedInstanceState;
    ImageButton taskBtn;
    ImageButton locationbtn;
    ImageButton linemeasure;
    ImageButton savetaskbtn;
    Boolean lineMeasureFlag=false;
    Boolean areaMeasureFlag=false;
    ImageButton areameasure;
    ImageButton countpreviewbtn;
    ImageButton kmlbtn;
    ImageButton drawareabtn;
    SeekBar linerotateset;
    Switch switchsatellitebtn;
    KMLLoadDialog kmlfileDialog;
    CalculateHightDialog calculateDialog;
    PullRefreshTaskList tasklist;
    ColorPickerView colorPickerView;
    TextView customHeight;
    @Override
    void setOnTaskLoad(String id){
        TaskViewModel taskViewModel=taskManager.getTask(id);
        currentTask=taskViewModel;
        List<FlyAreaPoint> area=taskManager.getFlyAreaPoints(id);
        maptool.LoadTask2UI(taskViewModel,area);
        maptool.ClearDrawArea();
        colorPickerView.setProgressVal(250);
        customHeight.setText("250m");
        customHeight.setTextColor(android.graphics.Color.GREEN);
        List<HeightAreaPoint> areaHeightPoints=taskManager.getHeightAreaPoints(id);
        if(areaHeightPoints!=null&&areaHeightPoints.size()>0){
            List<LatLng> points=new ArrayList<>();
            for (int i=0;i<areaHeightPoints.size();i++){
                points.add(new LatLng(areaHeightPoints.get(i).lat,areaHeightPoints.get(i).lon));
            }
            maptool.LoadDrawArea(points,areaHeightPoints.get(0).color);
            customHeight.setTextColor(areaHeightPoints.get(0).color);
            colorPickerView.setProgressVal(areaHeightPoints.get(0).height);
            customHeight.setText(areaHeightPoints.get(0).height+"m");
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.task_edit_widgets,null);//加载视图
        taskBtn=layout.findViewById(R.id.taskset);
        locationbtn=layout.findViewById(R.id.locationbtn);
        linemeasure=layout.findViewById(R.id.linemeasurebtn);
        areameasure=layout.findViewById(R.id.areameasurebtn);
        switchsatellitebtn=layout.findViewById(R.id.switch_satellite);
        savetaskbtn=layout.findViewById(R.id.savetaskbtn);
        //savetaskbtn.setVisibility(View.GONE);//暂时隐藏保存按钮当区域改变时，自动保存
        linerotateset=layout.findViewById(R.id.linerotateset);
        countpreviewbtn=layout.findViewById(R.id.countpreviewbtn);
        kmlbtn=layout.findViewById(R.id.kmlbtn);
        tasklist=layout.findViewById(R.id.tasklist);
        colorPickerView=layout.findViewById(R.id.colorPicker);
        customHeight=layout.findViewById(R.id.customHeight);
        drawareabtn=layout.findViewById(R.id.drawareabtn);
        initClick();
        setControlSize();//设置列表大小自适应屏幕
        initListPopupIfNeed();
        return layout;
    }
    @Override
    void onMapInit(AMap map){
        aMap=map;
        maptool=new AmapTool(aMap,getContext());
        maptool.setAreaChangedListener(new AmapTool.AreaChangedListener() {
            @Override
            public void onChange() {
                savetaskbtn.callOnClick();//当区域改变时自动保存
            }
        });
        taskManager=new TaskManager();
        aMap.setMapType(AMap.MAP_TYPE_NORMAL);//普通地图 卫星地图MAP_TYPE_SATELLITE
        aMapLocation();
    }
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        _savedInstanceState=savedInstanceState;
        super.onActivityCreated(savedInstanceState);
        initDialog();
    }

    private void initDialog(){
        kmlfileDialog=new KMLLoadDialog();
        calculateDialog=new CalculateHightDialog();
    }
    private TaskViewModel currentTask;
    private void initClick(){
        linerotateset.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(currentTask!=null){
                    maptool.LoadFlyLines(i,maptool.GetFlyLinesSpace());
                    currentTask.airwayangle=i;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        savetaskbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(currentTask!=null){
                    taskManager.SaveTaskAndArea(currentTask,maptool.getPointsFromArea());
                    List<LatLng> area=maptool.getDrawArea();
                    if(area!=null&&area.size()>0){
                        List<HeightAreaPoint> heightarea=new ArrayList<>();
                        for (int i=0;i<area.size();i++){
                            HeightAreaPoint point=new HeightAreaPoint(UUID.randomUUID().toString(),
                                    currentTask.id,area.get(i).latitude,area.get(i).longitude,colorPickerView.getColor(),colorPickerView.getProgressVal());
                            heightarea.add(point);
                        }
                        if(heightarea.size()>0){
                            int height=heightarea.get(0).height;
                            float flyheight=currentTask.FlyHeight;
                            float distance=Math.abs(height-flyheight);
                            if(distance>1){//变高低于1米无法执行航点
                                taskManager.SaveHeightArea(currentTask.id,heightarea);
                            }
                        }
                        //maptool.CloseTool(AmapTool.TOOL_DRAWAREA);
                    }
                    //Common.ShowQMUITipToast(getContext(),"保存成功", QMUITipDialog.Builder.ICON_TYPE_SUCCESS,1000);
                    Intent intent = new Intent(ON_TASK_LOAD);
                    intent.putExtra("id",currentTask.id);
                    getContext().sendBroadcast(intent);

                }
            }
        });
        kmlbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                kmlfileDialog.BindMap(aMap);
                kmlfileDialog.show(getFragmentManager(),"kml");
            }
        });
        countpreviewbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calculateDialog.show(getFragmentManager(),"caculate");
            }
        });
        tasklist.SetListViewEventLisener(new TaskListViewAdapter_new.onClickEvent() {
            @Override
            public void onLoadClick(String taskid) {
                Intent intent = new Intent(ON_TASK_LOAD);
                intent.putExtra("id",taskid);
                getContext().sendBroadcast(intent);
            }
            @Override
            public void onRemoveClick(String taskid) {
                final String id=taskid;
                new QMUIDialog.MessageDialogBuilder(getActivity())
                        .setTitle("删除任务")
                        .setMessage("确定要删除吗？")
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
                                taskManager.RemoveTask(id);
                                Common.ShowQMUITipToast(getContext(),"删除成功", QMUITipDialog.Builder.ICON_TYPE_SUCCESS,1000);
                                tasklist.RefreshData();
                                if(currentTask!=null){
                                    if(currentTask.id.equals(id)){
                                        maptool.ClearArea();//清空区域
                                        maptool.ClearDrawArea();//清空变高区域
                                        currentTask=null;
                                    }
                                }
                            }
                        })
                        .create().show();
            }

            @Override
            public void onItemClick(String taskid) {
                Bundle args=new Bundle();
                args.putString("taskid",taskid);
                TaskInfoDialog editdlg=new TaskInfoDialog();
                editdlg.setArguments(args);
                editdlg.show(getFragmentManager(),"task");
            }
        });
        colorPickerView.setOnColorPickerChangeListener(new ColorPickerView.OnColorPickerChangeListener() {
            @Override
            public void onColorChanged(ColorPickerView picker, int color) {
                customHeight.setText(picker.getProgressVal()+"m");
                customHeight.setTextColor(color);
                maptool.SetDrawAreaFillColor(color);
            }

            @Override
            public void onStartTrackingTouch(ColorPickerView picker) {

            }

            @Override
            public void onStopTrackingTouch(ColorPickerView picker) {

            }
        });
        taskBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tasklist.Toggle();
            }
        });
        locationbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                aMapLocation();
            }
        });
        linemeasure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!lineMeasureFlag) {
                    maptool.OpenTool(AmapTool.TOOL_LINE);
                    lineMeasureFlag=true;
                }
                else{
                    maptool.CloseTool(AmapTool.TOOL_LINE);
                    lineMeasureFlag=false;
                }
            }
        });
        areameasure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!areaMeasureFlag) {
                    maptool.OpenTool(AmapTool.TOOL_AREA);
                    areaMeasureFlag=true;
                }
                else{
                    maptool.CloseTool(AmapTool.TOOL_AREA);
                    areaMeasureFlag=false;
                }
            }
        });
        switchsatellitebtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    maptool.EnableGoogleTileOverlay();
                }
                else
                    maptool.DisableGoogleTileOverlay();
            }
        });
        drawareabtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListPopup.setAnimStyle(QMUIPopup.ANIM_GROW_FROM_CENTER);
                mListPopup.setPreferredDirection(QMUIPopup.DIRECTION_TOP);
                mListPopup.show(drawareabtn);
            }
        });
    }
    private void setControlSize(){
        int height= QMUIDisplayHelper.getScreenHeight(getContext());
        int width=QMUIDisplayHelper.getScreenWidth(getContext());
        ViewGroup.LayoutParams params= tasklist.getLayoutParams();
        params.width=(int)(width*0.3);
        params.height=(int)(height*0.75);
        tasklist.setLayoutParams(params);
        tasklist.Toggle();
        ViewGroup.LayoutParams paramscolor= colorPickerView.getLayoutParams();
        paramscolor.height=(int)(height*0.65);
        colorPickerView.setLayoutParams(paramscolor);
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

    private QMUIListPopup mListPopup;
    private List<String> GetItems(){
        List<String> data = new ArrayList<>();
        data.add("绘制变高区域");
        data.add("清除变高区域");
        return data;
    }
    private void initListPopupIfNeed() {
        if (mListPopup == null) {
            final ArrayAdapter adapter = new ArrayAdapter<>(getActivity(), R.layout.simple_list_item, GetItems());
            mListPopup = new QMUIListPopup(getContext(), QMUIPopup.DIRECTION_NONE, adapter);
            mListPopup.create(QMUIDisplayHelper.dp2px(getContext(), 130), QMUIDisplayHelper.dp2px(getContext(), 200), new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    if(i==0){//编辑
                        maptool.OpenTool(AmapTool.TOOL_DRAWAREA);
                        mListPopup.dismiss();
                    }
                    else{
                        maptool.ClearDrawArea();
                        if(currentTask!=null&&currentTask.id!=null){
                            taskManager.clearTaskHeightArea(currentTask.id);
                        }
                    }
                }
            });
            mListPopup.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    //关闭时触发
                }
            });
        }
    }
}
