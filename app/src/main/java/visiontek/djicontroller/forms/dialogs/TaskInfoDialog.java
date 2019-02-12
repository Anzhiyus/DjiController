package visiontek.djicontroller.forms.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import visiontek.djicontroller.R;
import visiontek.djicontroller.dataManager.CameraManager;
import visiontek.djicontroller.dataManager.TaskManager;
import visiontek.djicontroller.models.TaskViewModel;
import visiontek.djicontroller.orm.FlyCamera;
import visiontek.djicontroller.orm.FlyTask;
import visiontek.djicontroller.util.Common;

import static android.content.Context.MODE_PRIVATE;

public class TaskInfoDialog extends DialogFragment {
    private Context mContext;
    private LinearLayout dialog_task;
    private CameraManager cameraManager;


    private Button btnConfirm;//确认按钮
    private ImageButton btn_cancel;//取消按钮
    private TaskManager taskManager;
    public FlyTask flyTaskEntity;
    private List cameralist=new ArrayList<String>();

    private EditText et_taskname;//飞行任务名称
    private EditText et_areaASL;//基准面海拔高度
    private EditText et_homeASL;//起始点海拔高度
    private EditText et_GSD;//分辨率
    private EditText gohomeHeight;
    private SeekBar sb_speed;//飞行速度
    private SeekBar sb_adjacentverlapping;//航向重叠度Seekbar
    private SeekBar sb_parallellapping;//旁向重叠度Seekbar
    private SeekBar sb_pitchangle;//云台俯仰角Seekbar

    private TextView tv_speed;//飞行速度
    private TextView tv_adjacentverlapping;//航向重叠度
    private TextView tv_parallellapping;//庞向重叠度
    private TextView tv_pitchangle;//云台俯仰角


    private Spinner spinner_camera;//相机Spinner
    private Spinner spinner_hover;//悬停拍照0关闭1打开
    private Spinner spinner_camera_orientation;//相机方向0°或90°
    private Spinner spinner_finishaction;//任务完成动作

    private List<FlyCamera> cameraList=new ArrayList<>();
    private Switch cameraSwitchbtn;
    private Button btn_mngCamera;
    CameraListDialog dlgcamera;
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        dialog_task= (LinearLayout) inflater.inflate(R.layout.dialog_task,null);
        mContext=getContext();
        taskManager=new TaskManager();
        cameraManager=new CameraManager();
        dlgcamera=new CameraListDialog();
        initUI();
        getCameraSpinner();
        setData();
        return dialog_task;
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
    //获取相机列表
    private void getCameraSpinner(){
        cameraList=cameraManager.getCameraList();
        if(cameraList!=null){
            for (int i=0;i<cameraList.size();i++){
                cameralist.add(cameraList.get(i).getName());
            }
            ArrayAdapter<String> mAdapter=new ArrayAdapter<String>(mContext,android.R.layout.simple_spinner_item,cameralist);
            mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner_camera.setAdapter(mAdapter);
            }
        }
    //初始化界面
    private void initUI() {
        gohomeHeight=dialog_task.findViewById(R.id.gohomeHeight);
        et_taskname = (EditText) dialog_task.findViewById(R.id.et_taskname);
        et_areaASL = (EditText) dialog_task.findViewById(R.id.et_basicheight);
        et_homeASL = (EditText) dialog_task.findViewById(R.id.et_startingheight);
        et_GSD = (EditText) dialog_task.findViewById(R.id.et_gsd);
        btn_mngCamera=dialog_task.findViewById(R.id.btn_mngCamera);
        btn_mngCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dlgcamera.show(getFragmentManager(),"cameras");
            }
        });
        sb_speed = (SeekBar) dialog_task.findViewById(R.id.sb_speed);
        sb_adjacentverlapping = (SeekBar) dialog_task.findViewById(R.id.sb_adjacentverlap);
        sb_parallellapping= (SeekBar) dialog_task.findViewById(R.id.sb_parallellap);
        sb_pitchangle = (SeekBar) dialog_task.findViewById(R.id.sb_pitchangle);

        tv_speed = (TextView) dialog_task.findViewById(R.id.tv_speed);
        tv_adjacentverlapping = (TextView) dialog_task.findViewById(R.id.tv_adjacentverlap);
        tv_parallellapping = (TextView) dialog_task.findViewById(R.id.tv_parallellap);
        tv_pitchangle = (TextView) dialog_task.findViewById(R.id.tv_pitchangle);
        spinner_camera = (Spinner) dialog_task.findViewById(R.id.spinner_camera);
        spinner_camera_orientation = (Spinner) dialog_task.findViewById(R.id.spinner_camera_orientation);
        spinner_finishaction = (Spinner) dialog_task.findViewById(R.id.spinner_finishaction);
        spinner_hover = (Spinner) dialog_task.findViewById(R.id.spinner_photoswitch);
        cameraSwitchbtn=(Switch) dialog_task.findViewById(R.id.cameraSwitch);
        sb_speed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tv_speed.setText(String.valueOf(progress));

            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                tv_speed.setText(String.valueOf(sb_speed.getProgress()));
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                tv_speed.setText(String.valueOf(sb_speed.getProgress()));
            }
        });
        sb_adjacentverlapping.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tv_adjacentverlapping.setText(String.valueOf(progress));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                tv_adjacentverlapping.setText(String.valueOf(sb_adjacentverlapping.getProgress()));
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                tv_adjacentverlapping.setText(String.valueOf(sb_adjacentverlapping.getProgress()));
            }
        });

        sb_parallellapping.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tv_parallellapping.setText(String.valueOf(progress));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                tv_parallellapping.setText(String.valueOf(sb_parallellapping.getProgress()));
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                tv_parallellapping.setText(String.valueOf(sb_parallellapping.getProgress()));
            }
        });

        sb_pitchangle.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tv_pitchangle.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                tv_pitchangle.setText(String.valueOf(sb_pitchangle.getProgress()));
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                tv_pitchangle.setText(String.valueOf(sb_pitchangle.getProgress()) );
            }
        });
        cameraSwitchbtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (flyTaskEntity!=null){
                    if(isChecked){
                        flyTaskEntity.isThridCamera=1;
                    }
                    else {
                        flyTaskEntity.isThridCamera=0;
                    }
                }
            }
        });
        btnConfirm = (Button) dialog_task.findViewById(R.id.btn_confirm);
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (flyTaskEntity==null){
                    flyTaskEntity=new FlyTask();
                }
                flyTaskEntity.taskname=et_taskname.getText().toString();
                int index=spinner_camera.getSelectedItemPosition();
                if(index>=0){
                    FlyCamera camera= cameraList.get(index);
                    flyTaskEntity.cameraid=camera.id;
                    flyTaskEntity.cameradirection=spinner_camera_orientation.getSelectedItemPosition();
                    flyTaskEntity.hover=spinner_hover.getSelectedItemPosition();
                    flyTaskEntity.areaASL=String2Int(et_areaASL.getText().toString());
                    flyTaskEntity.homeASL=String2Int(et_homeASL.getText().toString());
                    flyTaskEntity.gsd=String2Float(et_GSD.getText().toString());
                    flyTaskEntity.speed=String2Float(tv_speed.getText().toString());
                    flyTaskEntity.adjacentverlapping=String2Float(tv_adjacentverlapping.getText().toString())/100;
                    flyTaskEntity.parallellapping=String2Float(tv_parallellapping.getText().toString())/100;
                    flyTaskEntity.pitch=String2Int(tv_pitchangle.getText().toString());
                    flyTaskEntity.createtime = new Date();
                    flyTaskEntity.finishaction=spinner_finishaction.getSelectedItemPosition();
                    flyTaskEntity.isThridCamera=cameraSwitchbtn.isChecked()?1:0;
                    flyTaskEntity.GoHomeHeight=String2Int(gohomeHeight.getText().toString());
                    if(checkTask(flyTaskEntity)){
                        TaskViewModel taskViewModel=new TaskViewModel(flyTaskEntity,camera);
                        if(taskViewModel.GoHomeHeight==0){
                            taskViewModel.GoHomeHeight=(int)taskViewModel.FlyHeight;
                        }
                        taskManager.SaveTask(taskViewModel);
                        dismiss();
                        Common.ShowQMUITipToast(getContext(),"保存成功", QMUITipDialog.Builder.ICON_TYPE_SUCCESS,1000);
                    }
                }
                else{
                    Common.ShowQMUITipToast(getContext(),"请选择相机", QMUITipDialog.Builder.ICON_TYPE_INFO,500);
                }
            }
        });

        btn_cancel = (ImageButton) dialog_task.findViewById(R.id.btn_cancel);
        //取消关闭窗口
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClearTaskUI();
                TaskInfoDialog.this.dismiss();
            }
        });

    }
    private void setData(){
        Bundle args=getArguments();
        if(args!=null){
            String taskid=args.getString("taskid");
            FlyTask task= taskManager.getTask(taskid);
            setTask(task);
        }
        else{
            ClearTaskUI();
        }
    }
    private Boolean checkTask(FlyTask task){
        Boolean res=true;
        if(task==null){
            res =false;
            return res;
        }
        else if(cameraList==null||cameraList.size()==0){
            Common.ShowQMUITipToast(getContext(),"请添加相机", QMUITipDialog.Builder.ICON_TYPE_INFO,500);
            res=false;
        }
        else if(task.taskname==null||task.taskname.isEmpty()){
            Common.ShowQMUITipToast(getContext(),"请填写任务名称", QMUITipDialog.Builder.ICON_TYPE_INFO,500);
            res =false;
        }
        else if(task.cameraid==null||task.cameraid.isEmpty()){
            Common.ShowQMUITipToast(getContext(),"请选择相机", QMUITipDialog.Builder.ICON_TYPE_INFO,500);
            res=false;
        }
        else if(task.adjacentverlapping<0.1||task.adjacentverlapping>0.9){
            Common.ShowQMUITipToast(getContext(),"航向重叠度不在合理区间", QMUITipDialog.Builder.ICON_TYPE_INFO,500);
            res=false;
        }
        else if(task.parallellapping<0.1||task.parallellapping>0.9){
            Common.ShowQMUITipToast(getContext(),"旁向重叠度不在合理区间", QMUITipDialog.Builder.ICON_TYPE_INFO,500);
            res=false;
        }
        else if(task.gsd<=0){
            Common.ShowQMUITipToast(getContext(),"地面分辨率不合理", QMUITipDialog.Builder.ICON_TYPE_INFO,500);
            res=false;
        }
        return res;
    }

    private void setTask(FlyTask TASK) {
        flyTaskEntity=TASK;
        if (TASK != null) {
            et_taskname.setText(TASK.taskname);
            for (int i = 0; i < cameraList.size(); i++)
                if (TASK.cameraid.equals(cameraList.get(i).id)) {
                    spinner_camera.setSelection(i);
                }
            sb_pitchangle.setProgress(TASK.pitch);
            gohomeHeight.setText(String.valueOf(flyTaskEntity.GoHomeHeight));
            sb_adjacentverlapping.setProgress((int)(TASK.adjacentverlapping*100));
            sb_speed.setProgress((int)TASK.speed);
            sb_parallellapping.setProgress((int)(TASK.parallellapping*100));
            spinner_camera_orientation.setSelection(TASK.cameradirection);
            spinner_hover.setSelection(TASK.hover);
            et_areaASL.setText(String.valueOf(TASK.areaASL));
            et_homeASL.setText(String.valueOf(TASK.homeASL));
            et_GSD.setText(String.valueOf(TASK.gsd));
            tv_speed.setText(String.valueOf(TASK.speed));
            tv_adjacentverlapping.setText(String.valueOf(TASK.adjacentverlapping*100).substring(0,2));
            tv_parallellapping.setText(String.valueOf(TASK.parallellapping*100).substring(0,2));
            tv_pitchangle.setText((String.valueOf(TASK.pitch)));
            spinner_finishaction.setSelection(TASK.finishaction);
            cameraSwitchbtn.setChecked(TASK.isThridCamera==1);
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

    private int String2Int(String val){
        try {
            val=val.replace(" ","");
            return Integer.parseInt(val);
        } catch (Exception e) {
            return 0;
        }
    }
    public void ClearTaskUI(){
        et_taskname.setText("");
        spinner_camera.setSelection(0);
        spinner_camera_orientation.setSelection(0);
        spinner_hover.setSelection(0);
        et_areaASL.setText("");
        et_homeASL.setText("");
        et_GSD.setText("");
        flyTaskEntity=null;
    }
    public FlyTask getTask() {

        return flyTaskEntity ;
    }

}
