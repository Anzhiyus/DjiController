package visiontek.djicontroller.forms.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import visiontek.djicontroller.R;
import visiontek.djicontroller.dataManager.CameraManager;

import visiontek.djicontroller.dataManager.TaskManager;
import visiontek.djicontroller.models.TaskViewModel;
import visiontek.djicontroller.orm.FlyCamera;
import visiontek.djicontroller.orm.FlyTask;
import visiontek.djicontroller.util.Common;


public class CalculateHightDialog extends DialogFragment{
    private AlertDialog mDialog;
    private LinearLayout dialog_calculate;
    private Context mContext;
    private TaskViewModel taskViewModel;
    private CameraManager mCameraManager;

    private List<FlyCamera> allCameras=new ArrayList<>();
    //输入参数
    private Spinner spinner_camera;
    private Spinner spinner_cameraOri;
    private EditText et_adjacentverlapping;//航向重叠度
    private EditText et_parallellapping;//庞向重叠度
    private EditText et_gsd;//地面分辨率
    private EditText et_averageHeight;
    private EditText et_lowHeight;
    private EditText et_hightHeight;

    //输出参数
    private TextView tv_absoluteHeight;
    private TextView tv_adjacentSeparateverlapping;
    private TextView tv_lineSpace;
    private TextView tv_low_gsd;
    private TextView tv_high_gsd;
    private TextView tv_highAdjacentverlapping;
    private TextView tv_highParallellapping;

    private int averageHeight;
    private int lowHeight;
    private int highHeight;
    private int highFlyHeight;
    private int lowFlyHeight;
    private int absoluteHeight;
    private int flyheight;
    private float low_gsd;
    private float high_gsd;
    private float adjacentSeparateverlap;
    private float highparallellap;
    private float highadjacentverlap;
    private Button btn_calculate;
    private ImageButton btn_close;
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        dialog_calculate= (LinearLayout) inflater.inflate(R.layout.dialog_calculate_height,null);
        mContext=getContext();
        mCameraManager=new CameraManager();
        InitUI();
        getCameraSpinner();
        return dialog_calculate;
    }
    @Override
    public void show(FragmentManager manager, String tag) {
        try {
            //在每个add事务前增加一个remove事务，防止连续的add
            manager.beginTransaction().remove(this).commit();
            super.show(manager, tag);
        } catch (Exception e) {
            //同一实例使用不同的tag会异常,这里捕获一下
            e.printStackTrace();
        }
    }
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
   /* @Override
    public void onStart() {//设置宽度
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width= QMUIDisplayHelper.getScreenWidth(getContext());
            dialog.getWindow().setLayout((int) (width * 0.85), ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }*/
    //获取相机列表
    private void getCameraSpinner() {
//        FlyCamera camera= cameraList.get(index);//new FlyCamera("id",28,"Mavic2",100,11,1,8,5472,3648,30,"JPEG");//= cameraList.get(index);
        //allCameras.add(camera);
        allCameras=mCameraManager.getCameraList();
        List<String> cameraList=new ArrayList();
        for (int i = 0; i < allCameras.size(); i++) {
            cameraList.add(allCameras.get(i).getName());
        }
        ArrayAdapter<String> mAdapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_item, cameraList);
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_camera.setAdapter(mAdapter);
        }
    private  void InitUI(){
        et_adjacentverlapping= (EditText) dialog_calculate.findViewById(R.id.et_adjacentverlapping);
        et_parallellapping= (EditText) dialog_calculate.findViewById(R.id.et_parallellapping);
        et_gsd= (EditText) dialog_calculate.findViewById(R.id.et_gsd);
        et_averageHeight= (EditText) dialog_calculate.findViewById(R.id.et_averageHeight);
        et_lowHeight= (EditText) dialog_calculate.findViewById(R.id.et_lowHeight);
        et_hightHeight= (EditText) dialog_calculate.findViewById(R.id.et_hightHeight);
        btn_close=dialog_calculate.findViewById(R.id.close);
        tv_absoluteHeight= (TextView) dialog_calculate.findViewById(R.id.tv_absoluteHeight);
        tv_adjacentSeparateverlapping= (TextView) dialog_calculate.findViewById(R.id.tv_adjacentSeparateverlapping);
        tv_lineSpace= (TextView) dialog_calculate.findViewById(R.id.tv_lineSpace);
        tv_low_gsd= (TextView) dialog_calculate.findViewById(R.id.tv_low_gsd);
        tv_high_gsd= (TextView) dialog_calculate.findViewById(R.id.tv_high_gsd);
        tv_highAdjacentverlapping= (TextView) dialog_calculate.findViewById(R.id.tv_highAdjacentverlapping);
        tv_highParallellapping= (TextView) dialog_calculate.findViewById(R.id.tv_highParallellapping);
        spinner_camera = (Spinner) dialog_calculate.findViewById(R.id.spinner_camera_list);
        spinner_cameraOri = (Spinner) dialog_calculate.findViewById(R.id.spinner_camera_ori);
        btn_calculate= (Button) dialog_calculate.findViewById(R.id.btn_calculate);
        btn_calculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TaskViewModel task= getInput();
                if (checkInput()==true){
                    calculateRefrence();
                    showCalculateResult();
                }
            }
        });
        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }
    public void hide(){
        mDialog.hide();
    }
    private void calculateRefrence(){
        TaskViewModel task=getInput();
        getHeight();
        getAdjacentSeparateverlap();
        getGSD();
        getHighparallellap(task.cameradirection);
    }

    private void getGSD(){
        TaskViewModel task=getInput();
        low_gsd=lowFlyHeight*task.cameraInfo.opticalFormat/task.cameraInfo.focallength/1000;
        high_gsd=highFlyHeight*task.cameraInfo.opticalFormat/task.cameraInfo.focallength/1000;
    }

    private float getHighoverlap(float space ,float border){
        TaskViewModel task=getInput();
        float l=highFlyHeight*border*task.cameraInfo.opticalFormat/task.cameraInfo.focallength/1000;
        return  100*(l-space)/l;
    }

    //计算高处旁向重叠度
    //计算高处航向重叠度
    private void getHighparallellap(int cameraOri){
        TaskViewModel task=getInput();
        switch (cameraOri){
            case 0:
                highparallellap=getHighoverlap(task.lineSpace,task.cameraInfo.imagewidth);
                highadjacentverlap=getHighoverlap(task.pointSpace,task.cameraInfo.imageheight);
                break;
            case 1:
                highparallellap=getHighoverlap(task.lineSpace,task.cameraInfo.imageheight);
                highadjacentverlap=getHighoverlap(task.pointSpace,task.cameraInfo.imagewidth);
                break;
        }
    }

    /*获取隔片重叠度*/
    private void getAdjacentSeparateverlap(){
        TaskViewModel task=getInput();
        adjacentSeparateverlap=0;
        if (task.adjacentverlapping>0.5){
            adjacentSeparateverlap=(2*task.adjacentverlapping-1)*100;
        }
    }

    private void getHeight(){
        TaskViewModel task=getInput();
        float f=task.cameraInfo.focallength;//单位毫米
        float pixSize=task.cameraInfo.opticalFormat;//单位微米
        flyheight=(int) (1000*f*task.gsd/pixSize);
        absoluteHeight=flyheight+averageHeight;
        highFlyHeight=absoluteHeight-highHeight;
        lowFlyHeight=absoluteHeight-lowHeight;
    }

    private TaskViewModel getInput(){
        if(allCameras.size()>0){
            int index=spinner_camera.getSelectedItemPosition();
            FlyCamera camera= allCameras.get(index);
            FlyTask task=new FlyTask();
            task.cameradirection=spinner_cameraOri.getSelectedItemPosition();
            task.adjacentverlapping=String2Float(et_adjacentverlapping.getText().toString())/100;
            task.parallellapping=String2Float(et_parallellapping.getText().toString())/100;
            task.gsd=String2Float(et_gsd.getText().toString());
            averageHeight=String2Int(et_averageHeight.getText().toString());
            lowHeight=String2Int(et_lowHeight.getText().toString());
            highHeight=String2Int(et_hightHeight.getText().toString());
            task.areaASL=String2Int(et_averageHeight.getText().toString());
            return new TaskViewModel(task,camera);
        }
        else {
            return null;
        }
    }
    private int String2Int(String val){
        try {
            val=val.replace(" ","");
            return Integer.parseInt(val);
        } catch (Exception e) {
            return 0;
        }
    }
    private float String2Float(String val){
        try {
            val=val.replace(" ","");
            return Float.parseFloat(val);
        } catch (Exception e) {
            return 0;
        }
    }
    private boolean checkInput(){
        boolean res=true;
        TaskViewModel task=getInput();
        if(task==null){
            return false;
        }
        if(task.adjacentverlapping>=0.99||task.adjacentverlapping<=0||task.parallellapping>=0.99||task.parallellapping<=0){
            Common.ShowQMUITipToast(getContext(),"重叠度输入不正确", QMUITipDialog.Builder.ICON_TYPE_INFO,500);
            res=false;
        }
        else if(task.gsd<=0){
            Common.ShowQMUITipToast(getContext(),"分辨率输入不正确", QMUITipDialog.Builder.ICON_TYPE_INFO,500);
            res=false;
        }
        else if(averageHeight<-153||averageHeight>8800){
            Common.ShowQMUITipToast(getContext(),"平均高程不在合理区间", QMUITipDialog.Builder.ICON_TYPE_INFO,500);
            res=false;
        }
        else if(lowHeight<-153||lowHeight>8800){
            Common.ShowQMUITipToast(getContext(),"低处高程不在合理区间", QMUITipDialog.Builder.ICON_TYPE_INFO,500);
            res=false;
        }
        else if(highHeight<-153||highHeight>8800){
            Common.ShowQMUITipToast(getContext(),"高处高程不在合理区间", QMUITipDialog.Builder.ICON_TYPE_INFO,500);
            res=false;
        }
        return res;
    }

    private void showCalculateResult(){
        TaskViewModel task=getInput();
        tv_absoluteHeight.setText(String.valueOf(absoluteHeight));
        tv_adjacentSeparateverlapping.setText(String.valueOf(Math.round(adjacentSeparateverlap)));
        tv_lineSpace.setText(DecimalFormat(task.lineSpace));
        tv_high_gsd.setText(DecimalFormat((high_gsd)));
        tv_low_gsd.setText(DecimalFormat((low_gsd)));
        tv_highAdjacentverlapping.setText(String.valueOf(Math.round(highadjacentverlap)));
        tv_highParallellapping.setText(String.valueOf(Math.round(highparallellap)));
    }

    public String DecimalFormat(float number){
        String str;
        DecimalFormat decimalFormat=new DecimalFormat();
        decimalFormat.applyPattern("0.00");
        str=decimalFormat.format(number);
        return str;
    }


}
