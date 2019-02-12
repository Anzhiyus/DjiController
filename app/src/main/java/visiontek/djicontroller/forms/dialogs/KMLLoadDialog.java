package visiontek.djicontroller.forms.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.PolygonOptions;
import com.amap.api.maps.model.PolylineOptions;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;

import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import visiontek.djicontroller.R;
import visiontek.djicontroller.models.kml.Coordinate;
import visiontek.djicontroller.models.kml.KmlEntity;
import visiontek.djicontroller.models.kml.Placemark;
import visiontek.djicontroller.util.Common;
import visiontek.djicontroller.util.KMLReader;

public class KMLLoadDialog extends DialogFragment {
    public static final String sRoot = Environment.getExternalStorageDirectory().toString();
    public static final String sParent = ".."; //父目录
    public static final String sFolder = "."; //当前文件夹
    public static final String sEmpty = "";
    private static final String sErrorMsg = "访问出错！";
    AMap _amap;
    private KmlEntity kmlEntity;
    public static List<KmlEntity> kmlEntityList;

    public void BindMap(AMap map){
        _amap=map;
    }
    Handler mUIHandler = new Handler(Looper.getMainLooper());
    private QMUITipDialog tip;
    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width=QMUIDisplayHelper.getScreenWidth(getContext());
            dialog.getWindow().setLayout((int) (width * 0.75), ViewGroup.LayoutParams.WRAP_CONTENT);
        }
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
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Map<String, Integer> images = new HashMap<String, Integer>();
        images.put(this.sRoot, R.drawable.disk);//根目录图标
        images.put(this.sParent, R.drawable.up);//返回上一层，父目录图标
        images.put(this.sFolder, R.drawable.folder);//文件夹图标
        images.put("kml", R.drawable.kmlicon);
        images.put("kmz", R.drawable.kmlicon);
        images.put(this.sEmpty, R.drawable.file);
        return new FileSelectView(getContext(), new CallBackBundle() {
            @Override
            public void callBack(Bundle bundle) throws IOException {
                // TODO Auto-generated method stub
                String filePath = bundle.getString("path");
                String fileName = bundle.getString("name");
                //KMLLoadDialog.t.setTitle(filePath);
                final InputStream inputStream;
                if(!filePath.toLowerCase().endsWith("kml")&&
                        !filePath.toLowerCase().endsWith("kmz")){
                    return;
                }
                if (fileName.toLowerCase().endsWith("kmz")){
                    inputStream=getKmzInputStream(filePath);
                }else {
                    inputStream = new FileInputStream(filePath);
                }
                try {
                    tip= Common.QMUITipToast(getContext(),"载入中", QMUITipDialog.Builder.ICON_TYPE_LOADING);
                    mUIHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            startReadingKml(inputStream);
                        }
                    });
                    dismiss();
                    //inputStream.close();
                } catch (Exception e) {
                    if(tip!=null){
                        tip.dismiss();
                        Common.ShowQMUITipToast(getContext(),"KML载入出错", QMUITipDialog.Builder.ICON_TYPE_FAIL,500);
                    }
                    e.printStackTrace();
                }
            }
        }, "*", images);
    }
    public interface CallBackBundle {
        void callBack(Bundle bundle) throws IOException;
    }
    private InputStream getKmzInputStream(String pathName) throws IOException {
        File file =new File(pathName);

        ZipFile zipFile=new ZipFile(file);
        ZipInputStream zipInputStream=null;
        InputStream inputStream=null;
        ZipEntry entry=null;
        zipInputStream=new ZipInputStream(new FileInputStream(file));
        while ((entry=zipInputStream.getNextEntry())!=null){
            String zipEntryName=entry.getName();

            Log.d("nnn","文件名字是"+zipEntryName);
            if (zipEntryName.endsWith("kml")){
                inputStream=zipFile.getInputStream(entry);
                //startReadingKml(inputStream);
            }
        }
        return  inputStream;
    }
    private void startReadingKml(InputStream inputStream) {
        try {
            final KMLReader reader = new KMLReader(new KMLReader.Callback() {
                @Override
                public void onDocumentParsed(List<Placemark> placemarks) {

                    Log.d("ggg","placemarks的大小为:"+String.valueOf(placemarks.size()));
                    if (placemarks.size()==0){
                        Common.ShowQMUITipToast(getContext(),"请检查文件内容或格式是否正确", QMUITipDialog.Builder.ICON_TYPE_FAIL,1000);
                        return;
                    }
                    //清除上一次加载的KML文件
                    if (kmlEntityList!=null){
                        for (int i=0;i<kmlEntityList.size();i++){
                            if (kmlEntityList.get(i).marker!=null){
                                kmlEntityList.get(i).marker.remove();
                            }
                            if (kmlEntityList.get(i).polyline!=null){
                                kmlEntityList.get(i).polyline.remove();
                            }if (kmlEntityList.get(i).polygon!=null){
                                kmlEntityList.get(i).polygon.remove();
                            }
                        }
                    }
                    kmlEntityList=new ArrayList<>();
                    for(int i=0;i<placemarks.size();i++){
                        kmlEntity=new KmlEntity();
                        List<LatLng> rect=ConvertCoordinate2Latlng(placemarks.get(i).coordinates);
                        if(rect!=null&&i==0){
                            _amap.moveCamera(CameraUpdateFactory.newLatLng(rect.get(0)));
                            _amap.moveCamera(CameraUpdateFactory.zoomTo(10));
                        }
                        switch (placemarks.get(i).type) {
                            case 1://将点加载到地图上
                                LatLng point = new LatLng(Double.valueOf(rect.get(0).latitude), Double.valueOf(rect.get(0).longitude));
                                kmlEntity.marker = AddPoint(R.drawable.pointshow, point);
                                kmlEntity.marker.showInfoWindow();
                                kmlEntityList.add(kmlEntity);
                                break;
                            case 2://将线加载到地图上
                                if (placemarks.get(i).color!=null){
                                    kmlEntity.polyline=_amap.addPolyline(new PolylineOptions()//创建边界
                                            .addAll(rect).color(Color.parseColor(placemarks.get(i).color)));
                                }else {
                                    kmlEntity.polyline = _amap.addPolyline(new PolylineOptions()//创建边界
                                            .addAll(rect).color(Color.GREEN));
                                }
                                kmlEntityList.add(kmlEntity);
                                break;
                            case 3://将面加载到地图上
                                if (placemarks.get(i).color!=null){
                                    kmlEntity.polygon=_amap.addPolygon(new PolygonOptions().addAll(rect).
                                            fillColor(Color.parseColor(placemarks.get(i).color)).
                                            strokeColor(Color.parseColor(placemarks.get(i).color)));
                                }else {
                                    kmlEntity.polygon = _amap.addPolygon(new PolygonOptions().addAll(rect).
                                            fillColor(Color.YELLOW).strokeColor(Color.YELLOW));
                                    break;
                                }
                            default:
                                break;
                        }
                        kmlEntityList.add(kmlEntity);
                    }
                    if(tip!=null){
                        tip.dismiss();
                    }
                    //ToastUtils.setResultToToast("范围线导入完成");
                }
            });
            //InputStream fs = new FileInputStream(path);// m_context.getAssets().open(path);
            //reader.read(fs);
            reader.read(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
    }

    public Marker AddPoint(int res, LatLng point){
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.draggable(false);
        markerOptions.position(point);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(res));
        Marker marker= _amap.addMarker(markerOptions);
        return  marker;
    }

    private List<LatLng> ConvertCoordinate2Latlng(List<Coordinate> coordinates){
        List<LatLng> res=new ArrayList<>();
        for(int i=0;i<coordinates.size();i++){
            Coordinate coordinate=coordinates.get(i);
            res.add(new LatLng(coordinate.lat,coordinate.lon));
        }
        return res;
    }


    class FileSelectView extends ListView implements AdapterView.OnItemClickListener
    {
        //文件夹图标，文件夹名称，路径
        private CallBackBundle callBack = null;
        private String path = sRoot;
        private List<Map<String, Object>> list = null;
        private String suffix = null;//文件类型后缀
        private Map<String, Integer> imageMap = null;

        public FileSelectView(Context context, CallBackBundle callBack, String suffix, Map<String, Integer> images) {
            super(context);
            this.imageMap = images;
            this.suffix = suffix==null?"":suffix.toLowerCase();
            this.callBack = callBack;
            this.setOnItemClickListener(this);
            refreshFileList();
        }
        private String getSuffix(String fileName)
        {
            int dix = fileName.lastIndexOf('.');
            if (dix < 0) {
                return "";
            }
            else {
                return fileName.substring(dix+1);
            }
        }
        // 获取某个文件目录（如根目录，父目录等目录）的图标
        private int getImageId(String s)
        {
            if (imageMap == null) {
                return 0;
            }
            else if (imageMap.containsKey(s)) {
                return imageMap.get(s);
            }
            else if (imageMap.containsKey(sEmpty)) {
                return imageMap.get(sEmpty);
            }
            else {
                return 0;
            }
        }
        // 刷新文件列表
        private int refreshFileList()
        {
            File[] files = null;
            try {
                files = new File(path).listFiles();//Environment.getExternalStorageDirectory().listFiles();//new File().listFiles();
            } catch (Exception e) {
                files = null;
            }
            if (files == null) {
                return -1;
            }
            if (list != null) {
                list.clear();
            }
            else {
                list = new ArrayList<Map<String, Object>>(files.length);
            }
            //用来保存文件夹和文件的两个列表
            ArrayList<Map<String, Object>> lfolders = new ArrayList<Map<String,Object>>();
            ArrayList<Map<String, Object>> lfiles = new ArrayList<Map<String,Object>>();
            if (! this.path.equals(sRoot)) {
                //如果当前目录不是根目录，就添加返回上一层
                Map<String, Object> map = new HashMap<String, Object>();
                map = new HashMap<String, Object>();
                map.put("name", sParent);
                map.put("path", path);
                map.put("img", getImageId(sParent));
                list.add(map);
            }
            for (File file:files) {
                if (file.isDirectory() && file.listFiles()!=null) {
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("name", file.getName());
                    map.put("path", file.getPath());
                    map.put("img", getImageId(sFolder));
                    lfolders.add(map);
                }
                else if (file.isFile()) {
                    String fileSuffix = getSuffix(file.getName()).toLowerCase();
                    if(suffix == null || suffix=="*" || suffix.endsWith("."+fileSuffix))
                    {
                        Map<String, Object> map = new HashMap<String, Object>();
                        map.put("name", file.getName());
                        map.put("path", file.getPath());
                        map.put("img", getImageId(fileSuffix));
                        lfiles.add(map);
                    }
                }
            }
            list.addAll(lfolders);//先添加文件夹，确保文件夹显示在list的上面
            list.addAll(lfiles);//再添加文件
            SimpleAdapter adapter = new SimpleAdapter(getContext(), list, R.layout.filedialogitem, new String[]{"img","name","path"},
                    new int[]{R.id.filedialogitem_img, R.id.filedialogitem_name, R.id.filedialogitem_path});
            this.setAdapter(adapter);
            return files.length;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String filePath = (String)list.get(position).get("path");
            String fileName = (String)list.get(position).get("name");
            if (fileName.equals(sRoot) || fileName.equals(sParent)) {
                File file = new File(filePath);
                String pathParent = file.getParent();
                if (pathParent != null) {
                    path = pathParent;
                }
                else {
                    path = sRoot;
                }
            }
            else {
                File file = new File(filePath);
                if (file.isFile()) {
                    //设置回调的返回值
                    Bundle bundle = new Bundle();
                    bundle.putString("path", filePath);
                    bundle.putString("name", fileName);

                    try {
                        this.callBack.callBack(bundle);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return;
                }
                else if (file.isDirectory()) {
                    path = filePath;
                }
            }
            this.refreshFileList();
        }
    }
}
