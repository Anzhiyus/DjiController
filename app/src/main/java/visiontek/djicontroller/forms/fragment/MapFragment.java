package visiontek.djicontroller.forms.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amap.api.maps.MapView;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.TextureMapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;

import java.lang.reflect.Field;

import visiontek.djicontroller.R;

public abstract  class MapFragment extends Fragment {//实现在Fragmeng中加载高德地图
    public static final String ON_TASK_LOAD= "djiController_task_load";
    private TextureMapView textureMapView;
    private AMap aMap;
    abstract void onMapInit(AMap aMap);//在地图初始化后调用
    abstract void setOnTaskLoad(String taskid);
    protected BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(ON_TASK_LOAD)){//接收消息让地图加载当前要执行的任务
                Bundle bundle = intent.getExtras();
                String taskid= bundle.getString("id");
                setOnTaskLoad(taskid);
            }
        }
    };
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        textureMapView = getView().findViewById(R.id.map);
        if (textureMapView != null) {
            textureMapView.onCreate(savedInstanceState);
            aMap = textureMapView.getMap();
            UiSettings mUiSettings = aMap.getUiSettings();//实例化UiSettings类对象
            mUiSettings.setScaleControlsEnabled(true);
            mUiSettings.setZoomControlsEnabled(false);
            onMapInit(aMap);//回调传出地图对象
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(ON_TASK_LOAD);
        getContext().registerReceiver(mReceiver, filter);
    }

    /**
     * 方法必须重写
     */
    @Override
    public void onResume() {
        super.onResume();
        textureMapView.onResume();
    }

    /**
     * 方法必须重写
     */
    @Override
    public void onPause() {
        super.onPause();
        textureMapView.onPause();
    }

    /**
     * 方法必须重写
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        textureMapView.onSaveInstanceState(outState);
    }

    /**
     * 方法必须重写
     */
    @Override
    public void onDestroy() {
        getContext().unregisterReceiver(mReceiver);
        super.onDestroy();
        textureMapView.onDestroy();
    }
    @Override
    public void onDetach() {
        super.onDetach();
        try {
            Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
            childFragmentManager.setAccessible(true);
            childFragmentManager.set(this, null);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
