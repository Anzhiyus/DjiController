package visiontek.djicontroller.util;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Handler;
import android.util.Log;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.Polygon;
import com.amap.api.maps.model.PolygonOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.maps.model.TileOverlay;
import com.amap.api.maps.model.TileOverlayOptions;
import com.amap.api.maps.model.TileProvider;
import com.amap.api.maps.model.UrlTileProvider;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import retrofit2.http.PUT;
import visiontek.djicontroller.R;
import visiontek.djicontroller.models.TaskViewModel;
import visiontek.djicontroller.orm.FlyAreaPoint;

public class AmapTool {
    private AMap _amap;
    public static final int TOOL_LINE = 0;
    public static final int TOOL_AREA = 1;
    public static final int TOOL_DRAWAREA=2;
    //public static final int TOOL_NONE = 2;

    private List<LatLng> points=new ArrayList<>();//测量线时的顶点
    private Polyline lines;//组合线
    private List<Float> measureResult = new ArrayList<Float>();
    private List<Marker> pointMarkers=new ArrayList<>();//顶点marker

    private List<LatLng> pointsarea=new ArrayList<>();//测量面积时的顶点
    private Polygon area;//区域
    private List<Marker> areaMakers=new ArrayList<>();

    private List<LatLng> pointsDrawArea=new ArrayList<>();//绘制区域时的顶点坐标
    private Polygon drawArea;//区域
    private List<Marker> drawAreaMarkers=new ArrayList<>();//顶点marker

    TileOverlay tileOverlay;//谷歌地图图层
    AMapLocationClient mlocationClient;//地图定位对象
    AMapLocationClientOption mLocationOption = null;

    cpRPAOptions opts;

    private Context _context;
    public AmapTool(AMap amap,Context context){
        _context=context;
        _amap=amap;
        opts=new cpRPAOptions();
        opts.aMap=_amap;
        bindEvents();
    }
    public List<LatLng> getDrawArea(){
        return pointsDrawArea;
    }
    public void ClearDrawArea(){
        ClearMarker(drawAreaMarkers);
        if(drawArea!=null){
            drawArea.remove();
        }
        pointsDrawArea.clear();
    }
    public void LoadDrawArea(List<LatLng> rect,int color){
        ClearMarker(drawAreaMarkers);
        if(drawArea!=null){
            drawArea.remove();
        }
        if(rect!=null){
            pointsDrawArea=rect;
            drawArea=_amap.addPolygon(new PolygonOptions()
                    .addAll(rect)
                    .fillColor(color)
                    .strokeColor(color).strokeWidth(1));
        }
        SetDrawAreaFillColor(color);
    }
    public void SetDrawAreaFillColor(int color){
        if(drawArea!=null){//.fillColor(Color.argb(50, 0, 128, 0))//0,128,0
            int alpha = 50;
            int r   = (color & 0x00ff0000) >> 16;
            int g = (color & 0x0000ff00) >> 8;
            int b  = (color & 0x000000ff);
            int colornew= Color.argb(alpha,r, g, b);//设置半透明alpha
            drawArea.setFillColor(colornew);
            drawArea.setStrokeColor(colornew);
        }
    }
    public LatLng get_homePoint() {
        if(userPosition!=null){
            _homePoint=userPosition.getPosition();
            return _homePoint;
        }
        return null;
    }
    public Marker AddPoint(int res, LatLng point, Boolean draggable, String title, String snippet,float anchor1,float anchor2){
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.draggable(draggable);
        if(title!=null||snippet!=null){
            markerOptions.title(title).snippet(snippet);
        }
        markerOptions.position(point);
        markerOptions.anchor(anchor1,anchor2);//锚点偏移
        markerOptions.icon(BitmapDescriptorFactory.fromResource(res));
        Marker marker= _amap.addMarker(markerOptions);//顶点图标
        return  marker;
    }

    public void OpenTool(int toolType){
        if(toolType==TOOL_LINE){
            points.clear();
            ClearMarker(pointMarkers);
            _amap.setOnMapClickListener(new AMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    if(lines!=null){
                        lines.remove();
                    }
                    Marker marker=null;
                    if(points.size()==0){
                        marker= AddPoint(R.drawable.point,latLng,false,null,null,0.5f,0.5f);
                    }
                    else{
                        LatLng prev=points.get(points.size()-1);
                        //(这里的100就是2位小数点,如果要其它位,如4位,这里两个100改成10000)
                        float distance=AMapUtils.calculateLineDistance(latLng,prev);
                        measureResult.add((float)(Math.round(distance*100))/100);
                        String content="";
                        float res=0;
                        for (int i=0;i<measureResult.size();i++) {
                            res=res+measureResult.get(i);
                            if(i==0){
                                content=measureResult.get(i)+"";
                            }
                            else{
                                content=content+"+"+measureResult.get(i);
                            }
                        }
                        if(measureResult.size()>1){
                            content=content+"="+res+"m";
                        }
                        else{
                            content=res+"m";
                        }
                        marker=AddPoint(R.drawable.point,latLng,false,"总距离:",content,0.5f,0.5f);
                        marker.showInfoWindow();
                    }
                    pointMarkers.add(marker);
                    points.add(latLng);
                    lines=_amap.addPolyline(new PolylineOptions().addAll(points).width(2).color(Color.RED));
                    lines.setZIndex(10);
                }
            });
            Common.ShowQMUITipToast(_context,"测距工具已打开，请点击屏幕选择", QMUITipDialog.Builder.ICON_TYPE_INFO,1000);
        }
        if(toolType==TOOL_AREA){
            pointsarea.clear();
            ClearMarker(areaMakers);
            _amap.setOnMapClickListener(new AMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    if(area!=null){
                        area.remove();
                    }
                    pointsarea.add(latLng);
                    //LatLng leftTopLatlng=new LatLng(cpRPA.getMaxLatitude(pointsarea),cpRPA.getMinLongitude(pointsarea));
                    //LatLng rightBottomLatlng=new LatLng(cpRPA.getMinLatitude(pointsarea),cpRPA.getMaxLongitude(pointsarea));
                    //float areaRes = AMapUtils.calculateArea(leftTopLatlng,rightBottomLatlng);这是矩形
                    double areaRes=CalculatePolygonArea(pointsarea);
                    areaRes=(double) (Math.round(areaRes*100))/100;
                    Marker marker= AddPoint(R.drawable.point,latLng,false,"总面积:",areaRes+"㎡",0.5f,0.5f);//加顶点
                    area= _amap.addPolygon(new PolygonOptions()
                            .addAll(pointsarea)
                            .fillColor(Color.argb(50, 1, 1, 1))
                            .strokeColor(Color.RED).strokeWidth(1));//绘制区域
                    if(pointsarea.size()>2){
                        marker.showInfoWindow();
                    }
                    areaMakers.add(marker);
                    area.setZIndex(10);
                }
            });
            Common.ShowQMUITipToast(_context,"面积工具已打开，请点击屏幕选择", QMUITipDialog.Builder.ICON_TYPE_INFO,1000);
        }
        if(toolType==TOOL_DRAWAREA){
            pointsDrawArea.clear();
            ClearMarker(drawAreaMarkers);
            _amap.setOnMapClickListener(new AMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    if(drawArea!=null){
                        drawArea.remove();
                    }
                    pointsDrawArea.add(latLng);
                    Marker marker= AddPoint(R.drawable.point,latLng,false,null,null,0.5f,0.5f);//加顶点
                    drawArea= _amap.addPolygon(new PolygonOptions()
                            .addAll(pointsDrawArea)
                            .fillColor(Color.argb(50, 0, 128, 0))//0,128,0
                            .strokeColor(Color.GREEN).strokeWidth(1));//绘制区域
                    drawAreaMarkers.add(marker);
                    drawArea.setZIndex(10);
                }
            });
            Common.ShowQMUITipToast(_context,"点击屏幕选择区域", QMUITipDialog.Builder.ICON_TYPE_INFO,1000);
        }
    }


    private void ClearMarker(List<Marker> markers){
        if(markers!=null){
            for(int i=0;i<markers.size();i++){
                markers.get(i).remove();//清除
            }
            markers.clear();
        }
    }
    private double CalculatePolygonArea(List<LatLng> ring){
        double sJ = 6378137;
        double Hq = 0.017453292519943295;
        double c = sJ *Hq;
        double d = 0;
        if (3 > ring.size()) {
            return 0;
        }
        for (int i = 0; i < ring.size() - 1; i ++){
            LatLng h = ring.get(i);
            LatLng k = ring.get(i + 1);
            double u = h.longitude * c * Math.cos(h.latitude * Hq);
            double hhh = h.latitude * c;
            double v = k.longitude * c * Math.cos(k.latitude *Hq);
            d = d + (u * k.latitude * c - v * hhh);
        }
        LatLng g1 = ring.get(ring.size()-1);
        LatLng point = ring.get(0);
        double eee = g1.longitude * c * Math.cos(g1.latitude * Hq);
        double g2 = g1.latitude * c;
        double k = point.longitude * c * Math.cos(point.latitude * Hq);
        d += eee * point.latitude * c - k * g2;
        return  0.5*Math.abs(d);
    }
    public void CloseTool(int toolType){
        if(toolType==TOOL_LINE) {
            measureResult.clear();
            if (lines != null) {
                lines.remove();
            }
            _amap.setOnMapClickListener(null);
            points.clear();
            ClearMarker(pointMarkers);
            Common.ShowQMUITipToast(_context,"测距工具已关闭", QMUITipDialog.Builder.ICON_TYPE_INFO,500);
        }
        if(toolType==TOOL_AREA){
            pointsarea.clear();
            if(area!=null){
                area.remove();
            }
            ClearMarker(areaMakers);
            _amap.setOnMapClickListener(null);
            Common.ShowQMUITipToast(_context,"面积工具已关闭", QMUITipDialog.Builder.ICON_TYPE_INFO,500);
        }
        if(toolType==TOOL_DRAWAREA){
            _amap.setOnMapClickListener(null);
            ClearMarker(drawAreaMarkers);
            Common.ShowQMUITipToast(_context,"区域绘制关闭", QMUITipDialog.Builder.ICON_TYPE_INFO,500);
        }
    }
    public void ClearAll(){
        _amap.clear();
    }
    public void moveCamera(LatLng point,int zoom){
        if(point!=null){
            _amap.moveCamera(CameraUpdateFactory.zoomTo(zoom));
            _amap.moveCamera(CameraUpdateFactory.newLatLng(point));
        }
    }
    public void EnableGoogleTileOverlay(){
        if(tileOverlay==null) {
            final String url = "http://mt0.google.cn/vt/lyrs=y@198&hl=zh-CN&gl=cn&src=app&x=%d&y=%d&z=%d&s=";
            TileProvider tileProvider = new UrlTileProvider(256, 256) {
                public URL getTileUrl(int x, int y, int zoom) {
                    try {
                        return new URL(String.format(url, x, y, zoom));
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            };
            tileOverlay = _amap
                    .addTileOverlay(new TileOverlayOptions()
                            .tileProvider(tileProvider)
                            .diskCacheEnabled(true)
                            .diskCacheDir("/storage/emulated/0/amap/cache")
                            .diskCacheSize(100000)
                            .memoryCacheEnabled(true)
                            .memCacheSize(100000))
            ;
        }
        tileOverlay.setZIndex(0);
        tileOverlay.setVisible(true);
        float zoom=_amap.getCameraPosition().zoom;
        _amap.moveCamera(CameraUpdateFactory.zoomTo(zoom+1));//不知道为什么不刷新只能这样刷新..
        _amap.moveCamera(CameraUpdateFactory.zoomTo(zoom));
    }
    public void DisableGoogleTileOverlay(){
        if(tileOverlay!=null){
            tileOverlay.setVisible(false);
            float zoom=_amap.getCameraPosition().zoom;
            _amap.moveCamera(CameraUpdateFactory.zoomTo(zoom+1));
            _amap.moveCamera(CameraUpdateFactory.zoomTo(zoom));
        }
    }
    public  interface LocationListener {
        void onLocationSuccess(LatLng var1);
        void onLocationFail(String msg);
    }
    private LatLng _homePoint;//手机所在位置
    private Marker userPosition;//手机所在位置标注
    public void StartLocation(final LocationListener listener){
        _homePoint=null;
        mlocationClient = new AMapLocationClient(_context);
        //初始化定位参数
        mLocationOption = new AMapLocationClientOption();
        //设置定位监听
        mlocationClient.setLocationListener(new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation amapLocation) {
                if (amapLocation != null) {
                    if (amapLocation.getErrorCode() == 0) {
                        if(_homePoint==null){
                            if(userPosition!=null){
                                userPosition.remove();
                            }
                            amapLocation.getLocationType();//获取当前定位结果来源，如网络定位结果，详见定位类型表
                            double lat= amapLocation.getLatitude();//获取纬度
                            double lon= amapLocation.getLongitude();//获取经度
                            _homePoint=new LatLng(lat,lon);
                            userPosition= AddPoint(R.drawable.homelocation,_homePoint,false,null,null,0.5f,0.5f);
                            listener.onLocationSuccess(_homePoint);
                            userPosition.showInfoWindow();
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {//3秒后关闭
                                @Override
                                public void run() {
                                    userPosition.hideInfoWindow();
                                }
                            },3000);
                        }
                    }
                    else{
                        Log.e("amaplocationErr",amapLocation.getErrorInfo());
                    }
                }
            }
        });
        //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        mLocationOption.setOnceLocationLatest(true);
        mlocationClient.setLocationOption(mLocationOption);
        mlocationClient.startLocation();
    }

    //对航飞区域和航线的相关操作
    private List<Marker> polygon=new ArrayList<>();//区域顶点标注
    private List<Marker> centerPoints=new ArrayList<>();//区域边界中心点标注
    private Marker centerMarker;//区域中心点标注
    private Polygon flyarea;//多边形边界图层
    private Polyline flylines;//航线图层
    private Marker startPoint=null;
    private Marker endMarker=null;
    private Boolean editEnabled=true;
    public void enableEditArea(Boolean val){
        editEnabled=val;
        if(val){
            bindEvents();
        }
        else{
            unbindEvents();
        }
        for(int i=0;i<polygon.size();i++){
            polygon.get(i).setVisible(val);
        }
        for(int i=0;i<centerPoints.size();i++){
            centerPoints.get(i).setVisible(val);
        }
        if(centerMarker!=null)
            centerMarker.setVisible(val);
    }
    //private Marker droneMarker = null;//飞机的实时位置标注
    //以中心点创建一个默认矩形
    public void NewArea(int rotate,double space){
        if(_homePoint!=null) {
            ClearArea();//重置
            List<LatLng> rect = createRectangle(_homePoint, 0.01, 0.01);
            LoadArea(rect);
            LoadFlyLines(rotate,space);
        }
        else{
            Common.ShowQMUITipToast(_context,"请先定位", QMUITipDialog.Builder.ICON_TYPE_INFO,500);
        }
    }
    //加载航线
    public void LoadFlyLines(int rotate,double space){
        List<LatLng> rect=getPointsFromArea();
        opts.polygon=rect;
        opts.rotate=rotate;
        opts.space=space;
        UpdateLines(rect);
    }
    private void resetCenterMoveMarker(List<LatLng> points){
        LatLng centerPoint = cpRPA.getCenterPoint(points);
        if(centerMarker!=null){
            centerMarker.remove();
        }
        centerMarker = AddPoint(R.drawable.move, centerPoint, true, "长按拖动", "平移区域", 1, 1);//创建平移点
    }
    public LatLng getAreaCenterPosition(){
        if(centerMarker!=null){
            return centerMarker.getPosition();
        }
        else{
            return null;
        }
    }
    //加载多边形区域
    public void LoadArea(List<LatLng> rect){
        ClearArea();
        flyarea=_amap.addPolygon(new PolygonOptions()//创建边界
                .addAll(rect)
                .fillColor(Color.argb(50, 1, 1, 1))
                .strokeColor(Color.RED).strokeWidth(1));

            resetCenterMoveMarker(rect);
            int size=rect.size();
            for(int i=0;i<size;i++){
                LatLng point=rect.get(i);
                Marker marker=null;
                marker=  AddPoint(R.drawable.corner,point,true,null,null,0.5f,0.5f);//创建顶点
                polygon.add(marker);
                LatLng center;
                if(i==0){
                    LatLng st=rect.get(size-1);
                    LatLng ed=rect.get(i);
                    center=new LatLng((st.latitude+ed.latitude)/2,(st.longitude+ed.longitude)/2);
                }
                else{
                    LatLng st=rect.get(i-1);
                    LatLng ed=rect.get(i);
                    center=new LatLng((st.latitude+ed.latitude)/2,(st.longitude+ed.longitude)/2);
                }
                Marker markerPlus =  AddPoint(R.drawable.corner_add,center,false,null,null,0.5f,0.5f);//创建中心点
                centerPoints.add(markerPlus);

        }
    }

    //更新多边形区域
    private void UpdateCenterPoints(List<LatLng> rect){
        ClearCenterPoints();
        int size=rect.size();
        for(int i=0;i<size;i++){
            LatLng center;
            if(i==0){
                LatLng st=rect.get(size-1);
                LatLng ed=rect.get(i);
                center=new LatLng((st.latitude+ed.latitude)/2,(st.longitude+ed.longitude)/2);
            }
            else{
                LatLng st=rect.get(i-1);
                LatLng ed=rect.get(i);
                center=new LatLng((st.latitude+ed.latitude)/2,(st.longitude+ed.longitude)/2);
            }
            Marker markerPlus =  AddPoint(R.drawable.corner_add,center,false,null,null,0.5f,0.5f);//创建中心点
            centerPoints.add(markerPlus);
        }
    }

    //获取全部航点
    public List<LatLng> getPointsFlyLines(){
        if(flylines!=null) {
            return flylines.getPoints();
        }
        else {
            return null;
        }
    }
    private void UpdateLines(List<LatLng> list){
        if(flylines!=null){
            flylines.remove();
        }
        if(list.size()>0){
            opts.polygon=list;
            flylines =_amap.addPolyline(new PolylineOptions().addAll(cpRPA.setOptions(opts)).width(2).color(Color.GREEN));//创建航线
            flylines.setZIndex(10);
        }
    }

    //设置起点位置
    public void setStartPoint(LatLng position){
        if(startPoint==null){
            startPoint= AddPoint(R.drawable.start_point,position,false,null,null,0.5f,1);
        }
        else{
            startPoint.setPosition(position);
        }
    }
    public void setEndPoint(LatLng position){
        if (endMarker == null) {
            endMarker = AddPoint(R.drawable.endtag, position, false, null, null, 0.5f, 0.5f);
        } else {
            endMarker.setPosition(position);
        }
    }
    //获取航线起始点的位置
    public LatLng getStartPointLocation(){
        if(startPoint!=null){
            startPoint.getPosition();
        }
        return null;
    }
    //获取航飞区域顶点
    public List<LatLng> getPointsFromArea(){
        List<LatLng> list = new ArrayList<LatLng>();
        for (int i = 0; i < polygon.size(); i++) {
            list.add(polygon.get(i).getPosition());
        }
        return list;
    }

    private void UpdateArea(List<LatLng> list){
        if(polygon!=null){
            flyarea.setPoints(list);
            flyarea.setZIndex(10);
        }
    }

    private List<LatLng> createRectangle(LatLng center, double halfWidth, double halfHeight) {
        List<LatLng> latLngs = new ArrayList<LatLng>();
        latLngs.add(new LatLng(center.latitude - halfHeight, center.longitude - halfWidth));
        latLngs.add(new LatLng(center.latitude - halfHeight, center.longitude + halfWidth));
        latLngs.add(new LatLng(center.latitude + halfHeight, center.longitude + halfWidth));
        latLngs.add(new LatLng(center.latitude + halfHeight, center.longitude - halfWidth));
        return latLngs;
    }
    private void ClearCenterPoints(){
        for (int i = 0; i < centerPoints.size(); i++) {
            centerPoints.get(i).remove();
        }
        centerPoints.clear();
    }
    //清空多边形和航线
    public void ClearArea(){
        for (int i = 0; i < polygon.size(); i++) {
            polygon.get(i).remove();
        }
        polygon.clear();
        ClearCenterPoints();
        if(centerMarker!=null){
            centerMarker.remove();
        }
        if(flyarea!=null) {
            flyarea.remove();
        }
        if(flylines!=null){
            flylines.remove();
        }
    }
    //点操作事件绑定
    LatLng positionBeforeDrag;
    LatLng positionOnDrag;
    private void unbindEvents(){
        _amap.setOnMarkerClickListener(null);
        _amap.setOnMarkerDragListener(null);
    }
    private void bindEvents(){
        _amap.setOnMarkerClickListener(new AMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if(polygon.contains(marker)){//点击的是顶点则删除这个点
                    if(polygon.size()==3){
                        Common.ShowQMUITipToast(_context,"多边形至少需要3个点", QMUITipDialog.Builder.ICON_TYPE_INFO,500);
                    }
                    else{
                        marker.remove();
                        polygon.remove(marker);
                    }
                }
                if(centerPoints.contains(marker)){//点击的是新增
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.corner));
                    markerOptions.draggable(true);
                    markerOptions.anchor(0.5f,0.5f);//正中锚点
                    LatLng point=marker.getPosition();
                    markerOptions.position(point);
                    int i=centerPoints.indexOf(marker);//获取所在集合的索引
                    Marker newmarker=_amap.addMarker(markerOptions);
                    polygon.add(i,newmarker);//点要加载正确的位置
                    marker.remove();
                }
                List<LatLng> list = new ArrayList<LatLng>();
                for (int i = 0; i < polygon.size(); i++) {
                    list.add(polygon.get(i).getPosition());
                }
                LoadArea(list);
                LoadFlyLines(opts.rotate,opts.space);
                return true;
            }
        });


        _amap.setOnMarkerDragListener(new AMap.OnMarkerDragListener() {//拖拽图层的处理
            @Override
            public void onMarkerDragStart(Marker marker) {
                positionOnDrag = marker.getPosition();
                positionBeforeDrag=marker.getPosition();
            }
            @Override
            public void onMarkerDrag(Marker marker) {
                positionOnDrag = marker.getPosition();
                if(marker.equals(centerMarker)) {//拖拽中心点平移
                    for (int i = 0; i < polygon.size(); i++) {//平移顶点
                        LatLng position = polygon.get(i).getPosition();
                        LatLng newposition = new LatLng
                                (position.latitude + (positionOnDrag.latitude - positionBeforeDrag.latitude),
                                        position.longitude + (positionOnDrag.longitude - positionBeforeDrag.longitude)
                                );
                        polygon.get(i).setPosition(newposition);
                    }
                    //平移顶点
                    if(startPoint!=null){
                        LatLng position = startPoint.getPosition();
                        LatLng newposition = new LatLng
                                (position.latitude + (positionOnDrag.latitude - positionBeforeDrag.latitude),
                                        position.longitude + (positionOnDrag.longitude - positionBeforeDrag.longitude)
                                );
                        startPoint.setPosition(newposition);
                    }
                    //平移终点
                    if(endMarker!=null){
                        LatLng position = endMarker.getPosition();
                        LatLng newposition = new LatLng
                                (position.latitude + (positionOnDrag.latitude - positionBeforeDrag.latitude),
                                        position.longitude + (positionOnDrag.longitude - positionBeforeDrag.longitude)
                                );
                        endMarker.setPosition(newposition);
                    }
                }
                List<LatLng> list= getPointsFromArea();
                UpdateArea(list);
                UpdateLines(list);
                UpdateCenterPoints(list);
                positionBeforeDrag=positionOnDrag;
            }
            @Override
            public void onMarkerDragEnd(Marker marker) {
                resetCenterMoveMarker(getPointsFromArea());
            }
        });
    }
    public void HomePointDraggable(Boolean bool){
        if(userPosition!=null)
        userPosition.setDraggable(bool);
    }
    //获取航线最优角度
    private int GetFirstBoundRotate(){
        //arctan[(y2-y1)/(x2-x1)]已知两点求角度
        LatLng point0=polygon.get(0).getPosition();
        LatLng point1=polygon.get(1).getPosition();
        Point a = _amap.getProjection().toScreenLocation(point0);
        Point b=_amap.getProjection().toScreenLocation(point1);
        if(b.x==a.x){
            return 90;
        }
        if(b.y==a.y){
            return 0;
        }
        double rotate= Math.atan2((b.y-a.y),(b.x-a.x));
        double angle=rotate*180/Math.PI;
        if(angle<0){
            angle=-angle;
        }
        return (int) Math.round(angle);
    }
    //private int _startPointPosition=0;
    public void SwitchStartPoint(){
        if(polygon.size()>0){
            List<LatLng> points=getPointsFromArea();
            LatLng point=points.get(0);
            points.remove(0);
            points.add(point);
            LoadArea(points);
            //自动设置航线最优
            int rotate=GetFirstBoundRotate();
            LoadFlyLines(rotate,opts.space);
        }
    }
    public int GetFlyLinesAngle(){//获取航线角度
        if(opts!=null){
            return opts.rotate;
        }
        else
            return 0;
    }
    public double GetFlyLinesSpace(){//获取航线间距
        if(opts!=null){
            return opts.space;
        }
        else
            return 0;
    }
    public float GetTotalFlyLinesLength(){//获取航线总距离
       List<LatLng> points= getPointsFlyLines();
       if(points==null) return 0;
       float distance=0;
       for(int i=0;i<points.size();i++){
           if((i+1)%2==0) {//是一条航线的终点
               LatLng prev=points.get(i-1);
               LatLng latLng=points.get(i);
               distance+=AMapUtils.calculateLineDistance(latLng,prev);
           }
       }
       distance=(float)(Math.round(distance/1000*100))/100;//换算为KM 保留2位小数
       return distance;
    }
    //加载一个指定任务到界面上
    public void LoadTask2UI(TaskViewModel task,List<FlyAreaPoint> areaPolygon){
        if(task==null){
            ClearAll();
        }
        else{
            float spacing=10;//航线间距cm
            if(task.cameraInfo!=null){
                spacing=task.lineSpace;//获取航线间距
            }
            else{
                return;
            }
            if(task!=null){
                //获取航飞区域信息
                //List<FlyAreaPoint> points=_dataRepository.getFlyAreaPoint(task.id);
                if(areaPolygon!=null&&areaPolygon.size()>0){
                    List<LatLng> rect=new ArrayList<>();
                    for (int i=0;i<areaPolygon.size();i++){
                        LatLng point=new LatLng(areaPolygon.get(i).lat,areaPolygon.get(i).lon);;
                        rect.add(point);
                    }
                    LoadArea(rect);//加载区域
                    if(spacing>0){
                        LoadFlyLines(task.airwayangle,spacing);//加载航线
                    }
                }
                else{//生成航线
                    NewArea(GetFlyLinesAngle(),spacing);
                }
                LatLng point=getAreaCenterPosition();
                moveCamera(point,15);
            }
        }
    }

}
