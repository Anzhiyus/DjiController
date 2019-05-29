package visiontek.djicontroller.util;

import android.util.Log;

import com.amap.api.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import  com.alibaba.fastjson.JSON;
import java.io.IOException;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import visiontek.djicontroller.models.SrtmData;

public class SrtmElevationTool {
    private String serverUrl="http://27.17.16.6:2000";//服务地址
    CollectLog clog = CollectLog.getInstance();
    private String _taskid;
    private int total=0;//线段总数量
    private int currrent=0;//当前下载的线段数量
    private List<SrtmData> result=new ArrayList<>();
    public SrtmElevationTool(String taskid,onSrtmDataDownload srtminterface){
        _taskid=taskid;
        _interface=srtminterface;
    }
    public interface onSrtmDataDownload{
        void onCompelete(List<SrtmData> srtmData);
        void onProgress(int total,int current);
    }
    private onSrtmDataDownload _interface;
    public void StartDownloadSrtmData(List<LatLng> points){
        int j=0;
        result.clear();
        total=(int)Math.ceil(points.size()/2);
        for(int i=0;i<points.size();i++){
            if((i+1)%2==0){//是一条航线的终点
                LatLng start=points.get(i-1);
                LatLng end=points.get(i);
                String url=serverUrl+"/GetElevationPath/srtm/0/"+start.longitude+"/"+start.latitude+"/"+end.longitude+"/"+end.latitude+"/10.0";
                GetHeightDatasAsync(url,j);
                j++;
            }
        }
    }
    //异步http请求地形数据
    private void GetHeightDatasAsync(String url, final int index){
        //String url = "http://wwww.baidu.com";
        OkHttpClient okHttpClient = new OkHttpClient();
        final Request request = new Request.Builder()
                .url(url)
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if(e!=null){
                    Log.d("GetHeightDatasAsync", "onFailure:"+e.getMessage());
                }
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String res=response.body().string();
                Double[] arr=getJsonToDoubleArray(res);
                String cache="";
                for(int i=0;i<arr.length;i++){
                    if((i+1)%3==0){
                        SrtmData data=new SrtmData();
                        data.lon=arr[i-2];
                        data.lat=arr[i-1];
                        data.height=arr[i];
                        data.index=index;//线段顺序
                        result.add(data);
                        currrent++;
                        _interface.onProgress(total,currrent);
                        if(total==currrent){
                            _interface.onCompelete(result);
                            cache=JSON.toJSONString(result);
                            clog.saveLogInfo(cache,_taskid+"_srtm");//保存文件
                        }
                    }
                }
            }
        });
    }
    /**
            * 将json数组转化为Double型
            * @param str
            * @return
       */
      public static Double[] getJsonToDoubleArray(String str) {
          try {
              com.alibaba.fastjson.JSONArray jsonArray = JSON.parseArray(str);
              Double[] arr=new Double[jsonArray.size()];
              for(int i=0;i<jsonArray.size();i++){
                  arr[i]=jsonArray.getDouble(i);
              }
              return arr;
          }
          catch (Exception e){
              return null;
          }
      }
    private void GetHeightDatas(String url){//同步获取
        OkHttpClient okHttpClient = new OkHttpClient();
        final Request request = new Request.Builder()
                .url(url)
                .build();
        final Call call = okHttpClient.newCall(request);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Response response = call.execute();
                    Log.d("GetHeightDatas", "run: " + response.body().string());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
