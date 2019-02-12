package visiontek.djicontroller.forms.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;

import dji.sdk.base.BaseProduct;
import dji.sdk.camera.Camera;
import dji.sdk.sdkmanager.DJISDKManager;
import visiontek.djicontroller.DJIApplication;
import visiontek.djicontroller.R;
import visiontek.djicontroller.dataManager.CameraManager;
import visiontek.djicontroller.orm.FlyCamera;
import visiontek.djicontroller.util.Common;

import static dji.sdk.camera.Camera.DisplayNamePhantom34KCamera;
import static dji.sdk.camera.Camera.DisplayNamePhantom3AdvancedCamera;
import static dji.sdk.camera.Camera.DisplayNamePhantom3ProfessionalCamera;
import static dji.sdk.camera.Camera.DisplayNamePhantom3StandardCamera;
import static dji.sdk.camera.Camera.DisplayNamePhantom4Camera;
import static dji.sdk.camera.Camera.DisplaynamePhantom4AdvancedCamera;
import static dji.sdk.camera.Camera.DisplaynamePhantom4PV2Camera;
import static dji.sdk.camera.Camera.DisplaynamePhantom4ProCamera;

public class CameraInfoDialog extends DialogFragment {

    private LinearLayout dialog_camera;
    public Context mContext;
    private String productName;
    private ImageButton close_cameraEdit;
    private Button save_Camera;

    private CameraManager datamanager;
    private FlyCamera cameraEntity;

    private EditText camera_name;
    private Spinner image_type;
    private Spinner iso_value;
    private Spinner aperture_value;
    private Spinner exposure_value;
    private Spinner shutter_value;
    private Spinner img_Size;

    private EditText opticalFormat;
    private Spinner camera_focal;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        dialog_camera= (LinearLayout) inflater.inflate(R.layout.dialog_camera,null);
        mContext=getContext();
        datamanager=new CameraManager();
        initUI();
        setData();
        return dialog_camera;
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
    //初始化界面
    private void  initUI(){
        close_cameraEdit=(ImageButton) dialog_camera.findViewById(R.id.close_cameraEdit);
        camera_name=(EditText)dialog_camera.findViewById(R.id.camera_name);
        image_type=(Spinner)dialog_camera.findViewById(R.id.image_type);
        iso_value=(Spinner)dialog_camera.findViewById(R.id.iso_value);
        aperture_value=(Spinner) dialog_camera.findViewById(R.id.aperture_value);
        exposure_value=(Spinner)dialog_camera.findViewById(R.id.exposure_value);
        shutter_value=(Spinner) dialog_camera.findViewById(R.id.shutter_value);
        img_Size=(Spinner)dialog_camera.findViewById(R.id.img_size);
        camera_focal=(Spinner)dialog_camera.findViewById(R.id.focal);
        opticalFormat=(EditText)dialog_camera.findViewById(R.id.opticalFormat);
        close_cameraEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        save_Camera=(Button)dialog_camera.findViewById(R.id.save_Camera);
        save_Camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(cameraEntity==null){
                    cameraEntity=new FlyCamera();
                }
                cameraEntity.imagewidth= getImgSize(img_Size.getSelectedItem().toString())[0];
                cameraEntity.imageheight=getImgSize(img_Size.getSelectedItem().toString())[1];
                cameraEntity.opticalFormat=String2Float(opticalFormat.getText().toString());
                cameraEntity.name=camera_name.getText().toString();
                cameraEntity.imgtype=image_type.getSelectedItem().toString();
                cameraEntity.iso=iso_value.getSelectedItemPosition();
                cameraEntity.aperture=aperture_value.getSelectedItemPosition();
                cameraEntity.exposurecompensation=exposure_value.getSelectedItemPosition();
                cameraEntity.shutterspeed=shutter_value.getSelectedItemPosition();
                cameraEntity.focallength=  String2Int( camera_focal.getSelectedItem().toString());
                datamanager.SaveCamera(cameraEntity);
                dismiss();
                Common.ShowQMUITipToast(getContext(),"保存成功", QMUITipDialog.Builder.ICON_TYPE_SUCCESS,1000);
            }
        });
    }
    private void setData(){
        Bundle args=getArguments();
        if(args!=null){
            String id= getArguments().getString("cameraid");
            setCamera(id);
        }
        else{
            ClearUI();
            setDefaultCameraUI();
        }
    }
    private void setCamera(String cameraid){
        cameraEntity=datamanager.GetCamera(cameraid);
        if(cameraEntity!=null){
            camera_name.setText(cameraEntity.name);
            image_type.setSelection(GetImgIndex(cameraEntity.imgtype));
            iso_value.setSelection(cameraEntity.iso);
            aperture_value.setSelection(cameraEntity.aperture);
            exposure_value.setSelection(cameraEntity.exposurecompensation);
            shutter_value.setSelection(cameraEntity.shutterspeed);
            img_Size.setSelection(getSizeIndex(cameraEntity.imagewidth,cameraEntity.imageheight));
            opticalFormat.setText(String.valueOf(cameraEntity.opticalFormat));
            camera_focal.setSelection(getFocalLength(cameraEntity.focallength));
        }
    }
    private int getSizeIndex(int width,int height){
       String[] arr= mContext.getResources().getStringArray(R.array.camera_size);
       for(int i=0;i<arr.length;i++){
           if((width+"*"+height).equals(arr[i])){
               return i;
           }
       }
       return 0;
    }

    private int getFocalIndex(String focal){
        String[] arr= mContext.getResources().getStringArray(R.array.camera_focal);
        for(int i=0;i<arr.length;i++){
            if(focal.equals(arr[i])){
                return i;
            }
        }
        return 0;
    }

    private int getFocalLength(float focal){
        String[] arr= mContext.getResources().getStringArray(R.array.camera_focal);
        for(int i=0;i<arr.length;i++){
            if(focal==String2Float(arr[i])){
                return i;
            }
        }
        return 0;
    }
    private int[] getImgSize(String text){
        String[] arr= text.split("\\*");
        return  new int[]{String2Int(arr[0]),String2Int(arr[1])};
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
    private int GetImgIndex(String imageType){
        String[] arr= mContext.getResources().getStringArray(R.array.image_type);
        for(int i=0;i<arr.length;i++){
            if(imageType.equals(arr[i])){
                return i;
            }
        }
        return 0;
    }

    public void ClearUI(){
        camera_name.setText("");
        image_type.setSelection(0);
        iso_value.setSelection(0);
        aperture_value.setSelection(0);
        exposure_value.setSelection(0);
        shutter_value.setSelection(0);
        img_Size.setSelection(0);
        img_Size.setEnabled(true);
        opticalFormat.setText("");
        opticalFormat.setFocusable(true);
        camera_focal.setSelection(0);
        camera_focal.setEnabled(true);
        cameraEntity=null;
    }


    public void setDefaultCameraUI(){
        String type=getCameraType();
        if(type!=null){
            if (type.equals("Phantom3")||type.equals("Phantom4")){
                camera_name.setText("DJIPhantom3&4");
                image_type.setSelection(0);
                iso_value.setSelection(0);
                aperture_value.setSelection(0);
                exposure_value.setSelection(0);
                shutter_value.setSelection(0);
                img_Size.setSelection(getSizeIndex(4000,3000));
                opticalFormat.setText("1.5");
                camera_focal.setSelection(getFocalIndex("4"));
                cameraEntity=null;

            }else if(type.equals("Phantom4+")){ //精灵4+系列，相机焦距是8mm, 像素是5472*3648
                camera_name.setText("DJIPhantom4+");
                image_type.setSelection(0);
                iso_value.setSelection(0);
                aperture_value.setSelection(0);
                exposure_value.setSelection(0);
                shutter_value.setSelection(0);
                img_Size.setSelection(getSizeIndex(5472,3648));
                opticalFormat.setText("2.4");
                camera_focal.setSelection(getFocalIndex("9"));
                cameraEntity=null;
            }
        }
        /*精灵3系列和精灵4相机焦距是4mm，分辨率是4000*3000，像元尺寸是1.54μm*/
    }

    /*
    * 精灵3系列和精灵4的相机镜头相同，精灵4Pro，4A和4prv2相同
    * */
    public String getCameraType(){
        String cameraType=null;
        BaseProduct product=DJIApplication.getProductInstance();
        if(product!=null){
            String name=product.getCamera().getDisplayName();
            //相机为精灵3系列
            if (name.equals(DisplayNamePhantom3StandardCamera)||
                    name.equals(DisplayNamePhantom3AdvancedCamera)||
                    name.equals(DisplayNamePhantom3ProfessionalCamera)||
                    name.equals(DisplayNamePhantom34KCamera)){
                cameraType="Phantom3";
                return cameraType;
            }else if (name.equals(DisplayNamePhantom4Camera)){//相机为精灵4
                cameraType="Phantom4";
                return cameraType;
            }else if (name.equals(DisplaynamePhantom4ProCamera)||//相机为精灵4pro,精灵4A，精灵4PV2
                    name.equals(DisplaynamePhantom4AdvancedCamera)||
                    name.equals(DisplaynamePhantom4PV2Camera)){
                cameraType="Phantom4+";
            }
        }
        return cameraType;
    }
    public FlyCamera getCamera(){
        return cameraEntity;
    }

}
