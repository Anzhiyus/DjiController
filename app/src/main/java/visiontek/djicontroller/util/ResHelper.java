package visiontek.djicontroller.util;

import android.content.Context;
import android.content.res.Resources;
import android.widget.ArrayAdapter;

import dji.common.camera.SettingsDefinitions;
import visiontek.djicontroller.R;

public class ResHelper {//处理界面和内容部的值映射
    Resources res;
    public ResHelper(Context context){
        res= context.getResources();
    }
    public SettingsDefinitions.ISO getIsoValues(int index){
        switch (index){
            case 0:return SettingsDefinitions.ISO.ISO_100;
            case 1:return SettingsDefinitions.ISO.ISO_200;
            case 2:return SettingsDefinitions.ISO.ISO_400;
            case 3:return SettingsDefinitions.ISO.ISO_800;
            case 4:return SettingsDefinitions.ISO.ISO_1600;
            case 5:return SettingsDefinitions.ISO.ISO_3200;
            case 6:return SettingsDefinitions.ISO.ISO_6400;
            case 7:return SettingsDefinitions.ISO.ISO_12800;
            case 8:return SettingsDefinitions.ISO.ISO_25600;
            default:return SettingsDefinitions.ISO.UNKNOWN;
        }
    }
    public SettingsDefinitions.Aperture getApertureValues(int index){
        switch (index){
            case 0:return SettingsDefinitions.Aperture.F_1_DOT_6;
            case 1:return SettingsDefinitions.Aperture.F_1_DOT_8;
            case 2:return SettingsDefinitions.Aperture.F_2;
            case 3:return SettingsDefinitions.Aperture.F_2_DOT_2;
            case 4:return SettingsDefinitions.Aperture.F_2_DOT_4;
            case 5:return SettingsDefinitions.Aperture.F_2_DOT_8;
            case 6:return SettingsDefinitions.Aperture.F_3_DOT_2;
            case 7:return SettingsDefinitions.Aperture.F_3_DOT_4;
            case 8:return SettingsDefinitions.Aperture.F_3_DOT_5;
            case 9:return SettingsDefinitions.Aperture.F_4;
            case 10:return SettingsDefinitions.Aperture.F_4_DOT_5;
            case 11:return SettingsDefinitions.Aperture.F_4_DOT_8;
            case 12:return SettingsDefinitions.Aperture.F_5;
            case 13:return SettingsDefinitions.Aperture.F_6_DOT_3;
            case 14:return SettingsDefinitions.Aperture.F_8;
            case 15:return SettingsDefinitions.Aperture.F_11;
            case 16:return SettingsDefinitions.Aperture.F_16;
            case 17:return SettingsDefinitions.Aperture.F_22;
            default:return SettingsDefinitions.Aperture.UNKNOWN;
        }
    }
    public SettingsDefinitions.ExposureCompensation getExposureValues(int index){
        switch (index){
            case 0:return SettingsDefinitions.ExposureCompensation.N_5_0;
            case 1:return SettingsDefinitions.ExposureCompensation.N_4_0;
            case 2:return SettingsDefinitions.ExposureCompensation.N_3_0;
            case 3:return SettingsDefinitions.ExposureCompensation.N_2_0;
            case 4:return SettingsDefinitions.ExposureCompensation.N_1_0;
            case 5:return SettingsDefinitions.ExposureCompensation.N_0_0;
            case 6:return SettingsDefinitions.ExposureCompensation.P_1_0;
            case 7:return SettingsDefinitions.ExposureCompensation.P_2_0;
            case 8:return SettingsDefinitions.ExposureCompensation.P_3_0;
            case 9:return SettingsDefinitions.ExposureCompensation.P_4_0;
            case 10:return SettingsDefinitions.ExposureCompensation.P_5_0;
            default:return SettingsDefinitions.ExposureCompensation.UNKNOWN;
        }
    }
    public SettingsDefinitions.ShutterSpeed getShutterValues(int index){
        SettingsDefinitions.ShutterSpeed[] shutterSpeeds=SettingsDefinitions.ShutterSpeed.values();
        return shutterSpeeds[index];
    }
    public SettingsDefinitions.PhotoFileFormat getImageTypes(String imgType){
        String[] arr= res.getStringArray(R.array.image_type);
        int index=0;
        for(int i=0;i<arr.length;i++){
            if(arr[i].equals(imgType)){
                index=i;break;
            }
        }
        switch (index){
            case 0:return SettingsDefinitions.PhotoFileFormat.JPEG;
            case 1:return SettingsDefinitions.PhotoFileFormat.RAW;
            case 2:return SettingsDefinitions.PhotoFileFormat.RAW_AND_JPEG;
            case 3:return SettingsDefinitions.PhotoFileFormat.TIFF_8_BIT;
            case 4:return SettingsDefinitions.PhotoFileFormat.TIFF_14_BIT;
            case 5:return SettingsDefinitions.PhotoFileFormat.TIFF_14_BIT_LINEAR_LOW_TEMP_RESOLUTION;
            case 6:return SettingsDefinitions.PhotoFileFormat.TIFF_14_BIT_LINEAR_HIGH_TEMP_RESOLUTION;
            case 7:return SettingsDefinitions.PhotoFileFormat.RADIOMETRIC_JPEG;
            default:return SettingsDefinitions.PhotoFileFormat.UNKNOWN;
        }
    }
}
