package visiontek.djicontroller.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CollectLog implements Thread.UncaughtExceptionHandler {

    public static final String TAG = CollectLog.class.getCanonicalName();

    //The system default UncaughtException handler class
    private Thread.UncaughtExceptionHandler mDefaultHandler;
    //CrashHandler instance
    private static CollectLog INSTANCE = new CollectLog();
    //Context
    private Context mContext;
    //Used to store device information and exception information
    private Map<String, String> infos = new HashMap<String, String>();

    //Used to format the date as part of the log file name
    private DateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmmss");
    //Custom the path
    private String filePath = "";

    /**
     *
     */
    private CollectLog() {
    }


    /**
     * Get instances, singleton pattern
     *
     * @return instances
     */
    public static CollectLog getInstance() {
        return INSTANCE;
    }

    /**
     * initialization
     *
     * @param context context
     */
    public void init(Context context) {
        mContext = context;
        //Gets the system's default UncaughtException handler
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        //Set the CrashHandler as the default handler for the program
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    /**
     * initialization ,Can custom the path
     *
     * @param context context
     * @param path    custom the path
     */
    public void init(Context context, String path) {
        init(context);
        filePath = path;
    }

    /**
     * When the UncaughtException occurs, it is passed to the function to process
     */
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        if (!handleException(ex) && mDefaultHandler != null) {
            // If the user does not deal with the system is the default exception handler to handle
            mDefaultHandler.uncaughtException(thread, ex);
        } else {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
//                Log.e(TAG, "error : ", e);
            }
            // exit app
            android.os.Process.killProcess(android.os.Process.myPid());
//            System.exit(0);
        }
    }

    /**
     * Custom error handling, error message collection error reporting and other operations are completed here.
     *
     * @param ex Throwable
     * @return If the exception is processed return true; false otherwise.
     */
    private boolean handleException(Throwable ex) {
        if (ex == null) {
            return false;
        }
        // Collect device parameter information
        collectDeviceInfo(mContext);
        //Save the log file
        String str = saveCrashInfo2File(ex);
        Log.e(TAG, str);
        return false;
    }

    /**
     * Collect device parameter information
     *
     * @param ctx context
     */
    public void collectDeviceInfo(Context ctx) {
        try {
            PackageManager pm = ctx.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES);
            if (pi != null) {
                String versionName = pi.versionName == null ? "null"
                        : pi.versionName;
                String versionCode = pi.versionCode + "";
                infos.put("versionName", versionName);
                infos.put("versionCode", versionCode);
            }

        } catch (PackageManager.NameNotFoundException e) {
//            Log.e(TAG, "an error occured when collect package info", e);
        }
        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                infos.put(field.getName(), field.get(null).toString());
//                Log.d(TAG, field.getName() + " : " + field.get(null));
            } catch (Exception e) {
//                Log.e(TAG, "an error occured when collect crash info", e);
            }
        }
    }
    public void saveLogInfo(String str,String id){
        StringBuffer sb = new StringBuffer();
        for (Map.Entry<String, String> entry : infos.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append("[" + key + ", " + value + "]\n");
        }
        sb.append("\n" + str);
        try {
            String time = formatter.format(new Date());
            String fileName = "TASK_" + id + ".txt";
            File sdDir = null;
            sdDir = mContext.getExternalFilesDir("logs").getAbsoluteFile();
            File file = null;
            if (!TextUtils.isEmpty(filePath)) {
                File files = new File(filePath);
                if (!files.exists()) {
                    //Create a directory
                    Boolean res= files.mkdirs();
                }
                file = new File(filePath + File.separator + fileName);
            } else {
                file = new File(sdDir + File.separator + fileName);
            }
            if (file == null) {
                file = new File(sdDir + File.separator + fileName);
            }
            //if(!file.exists()){file.createNewFile();}
            FileOutputStream fos = new FileOutputStream(file,true);//追加
            fos.write(sb.toString().getBytes());
            fos.close();

            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            intent.setData(Uri.fromFile(file));
            mContext.sendBroadcast(intent);//发广播通知系统刷新文件系统
        } catch (Exception e) {
        }
    }
    /**
     * Save the error message to a file
     *
     * @param ex Throwable
     * @return Returns the name of the file to facilitate transfer of the file to the server
     */
    private String saveCrashInfo2File(Throwable ex) {

        StringBuffer sb = new StringBuffer();
        for (Map.Entry<String, String> entry : infos.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append("[" + key + ", " + value + "]\n");
        }
        sb.append("\n" + getStackTraceString(ex));
        try {
            String time = formatter.format(new Date());
            String fileName = "CRASH_" + time + ".txt";
            File sdDir = null;
            sdDir = mContext.getExternalFilesDir("logs").getAbsoluteFile();
            File file = null;
            if (!TextUtils.isEmpty(filePath)) {
                File files = new File(filePath);
                if (!files.exists()) {
                    //Create a directory
                    Boolean res= files.mkdirs();
                }
                file = new File(filePath + File.separator + fileName);
            } else {
                file = new File(sdDir + File.separator + fileName);
            }
            if (file == null) {
                file = new File(sdDir + File.separator + fileName);
            }
            //if(!file.exists()){file.createNewFile();}
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(sb.toString().getBytes());
            fos.close();

            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            intent.setData(Uri.fromFile(file));
            mContext.sendBroadcast(intent);//发广播通知系统刷新文件系统
            /*MediaScannerConnection.scanFile(mContext.getApplicationContext(), new String[]{file.getAbsolutePath()},
                    new String[]{"application/octet-stream"},
                    new MediaScannerConnection.OnScanCompletedListener() {
                @Override
                public void onScanCompleted(final String path, final Uri uri) {
                    Log.i("Log","logRefreshed");
                }
            });*/
            return file.getAbsolutePath();
        } catch (Exception e) {
        }
        return null;
    }

    /**
     * Gets the string of the caught exception
     *
     * @param tr throwable
     * @return the string of the caught exception
     */
    public static String getStackTraceString(Throwable tr) {
        try {
            if (tr == null) {
                return "";
            }
            Throwable t = tr;
            while (t != null) {
                if (t instanceof UnknownHostException) {
                    return "";
                }
                t = t.getCause();
            }
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            tr.printStackTrace(pw);
            return sw.toString();
        } catch (Exception e) {
            return "";
        }
    }
}