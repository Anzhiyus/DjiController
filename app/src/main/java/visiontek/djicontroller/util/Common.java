package visiontek.djicontroller.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;

import static android.content.Context.MODE_PRIVATE;

public class Common {
    private static Handler mUIHandler = new Handler(Looper.getMainLooper());
    public static QMUITipDialog QMUITipToast(Context context,
                                             String msg, int iconType){
        final QMUITipDialog tipDialog = new QMUITipDialog.Builder(context)
                .setIconType(iconType)//QMUITipDialog.Builder.ICON_TYPE_LOADING
                .setTipWord(msg)
                .create();
        tipDialog.show();
        return tipDialog;
    }
    public static void ShowQMUITipToast(final Context context,final String msg,final int iconType,final int time){
        mUIHandler.post(new Runnable() {
            @Override
            public void run() {//要用异步，某些情况会卡住
                if(context!=null){
                    final QMUITipDialog tipDialog = new QMUITipDialog.Builder(context)
                            .setIconType(iconType)//QMUITipDialog.Builder.ICON_TYPE_LOADING
                            .setTipWord(msg)
                            .create();
                    tipDialog.show();
                    mUIHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            tipDialog.dismiss();
                        }
                    }, time);
                }
            }
        });
    }
    public static void StoreData(Context context,String key,String data){
        SharedPreferences.Editor editor =context.getSharedPreferences(key,MODE_PRIVATE).edit();
        editor.putString("data", data);
        editor.commit();
    }
    public static String RecoveryData(Context context,String key){
        SharedPreferences read = context.getSharedPreferences(key, MODE_PRIVATE);
        String id = read.getString("data", "");
        return id;
    }
}

