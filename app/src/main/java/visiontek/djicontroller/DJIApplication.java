package visiontek.djicontroller;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Environment;
import android.support.multidex.MultiDex;
import android.util.DisplayMetrics;
import android.util.Log;

import com.secneo.sdk.Helper;

import java.io.File;
import java.util.Locale;

import dji.sdk.base.BaseProduct;
import dji.sdk.sdkmanager.DJISDKManager;
import visiontek.djicontroller.models.TaskViewModel;
import visiontek.djicontroller.orm.DaoMaster;
import visiontek.djicontroller.orm.DaoSession;
import visiontek.djicontroller.util.CollectLog;

import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.identityscope.IdentityScopeType;

/**
 * Created by Administrator on 2017/11/2.
 */

public class DJIApplication extends Application {
    private static DJIApplication instance;
    private static BaseProduct mProduct;
    public static DJIApplication getInstance() {
        return instance;
    }

//    @Override
//    protected void attachBaseContext(Context paramContext) {
//        super.attachBaseContext(paramContext);
//        Helper.install(DJIApplication.this);
//        MultiDex.install(this);//geeendao在部分版本下可能导致闪退 此行必须
//    }
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        //setSQLLite
        setDatabase();
        CollectLog clog = CollectLog.getInstance();
        //崩溃日志
        clog.init(this, Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "visiontekLogFiles");
    }
    public static synchronized BaseProduct getProductInstance() {
        if (null == mProduct) {
            mProduct = DJISDKManager.getInstance().getProduct();
        }
        return mProduct;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig){
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            newConfig.setLocale(Locale.CHINESE);
        } else {
            newConfig.locale = Locale.CHINESE;
        }*/
        //语言跟随设备系统
        super.onConfigurationChanged(newConfig);
        Configuration config = getResources().getConfiguration();//获取系统的配置
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
        Log.i("lang",newConfig.locale.toString());
    }

    private static DaoSession daoSession;
    private void setDatabase() {
        // regular SQLite database
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "djicontroller-db");
        Database db = helper.getWritableDb();
        //DaoMaster.dropAllTables(db,false);
        DaoMaster.createAllTables(db,true);

        // encrypted SQLCipher database
        // note: you need to add SQLCipher to your dependencies, check the build.gradle file
        // DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "notes-db-encrypted");
        // Database db = helper.getEncryptedWritableDb("encryption-key");
        DaoMaster dao = new DaoMaster(db);
        daoSession=dao.newSession(IdentityScopeType.None);//不用缓存
    }
    public static DaoSession getDaoSession() {

        return daoSession;

    }
}
