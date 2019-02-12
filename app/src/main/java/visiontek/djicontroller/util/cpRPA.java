package visiontek.djicontroller.util;

import android.graphics.Point;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import visiontek.djicontroller.models.PolygonBounds;
import visiontek.djicontroller.models.latline;

/**
 * Created by Administrator on 2017/12/7.
 */

public class cpRPA {
    public static LatLng getCenterPoint(List<LatLng> mPoints) {
        double latitude = (getMinLatitude(mPoints) + getMaxLatitude(mPoints)) / 2;
        double longitude = (getMinLongitude(mPoints) + getMaxLongitude(mPoints)) / 2;
        return new LatLng(latitude, longitude);
    }

    public static double getMinLatitude(List<LatLng> mPoints) {
        double lat=mPoints.get(0).latitude;
        for(int i=0;i<mPoints.size();i++){
            if(lat>mPoints.get(i).latitude){
                lat=mPoints.get(i).latitude;
            }
        }
        return lat;
    }
    public static double getMinLongitude(List<LatLng> mPoints) {
        double lon=mPoints.get(0).longitude;
        for(int i=0;i<mPoints.size();i++){
            if(lon>mPoints.get(i).longitude){
                lon=mPoints.get(i).longitude;
            }
        }
        return lon;
    }
    public static double getMaxLatitude(List<LatLng> mPoints) {
        double lat=mPoints.get(0).latitude;
        for(int i=0;i<mPoints.size();i++){
            if(lat<mPoints.get(i).latitude){
                lat=mPoints.get(i).latitude;
            }
        }
        return lat;
    }
    public static double getMaxLongitude(List<LatLng> mPoints) {
        double lon=mPoints.get(0).longitude;
        for(int i=0;i<mPoints.size();i++){
            if(lon<mPoints.get(i).longitude){
                lon=mPoints.get(i).longitude;
            }
        }
        return lon;
    }
    public static PolygonBounds createPolygonBounds(List<LatLng> mPoints) {
        PolygonBounds bounds=new PolygonBounds();
        bounds.center=getCenterPoint(mPoints);
        List<LatLng> list=new ArrayList<>();
        list.add(new LatLng(getMaxLatitude(mPoints),getMinLongitude(mPoints)));
        list.add(new LatLng(getMaxLatitude(mPoints),getMaxLongitude(mPoints)));
        list.add(new LatLng(getMinLatitude(mPoints),getMaxLongitude(mPoints)));
        list.add(new LatLng(getMinLatitude(mPoints),getMinLongitude(mPoints)));
        bounds.northLat=getMaxLatitude(mPoints);
        bounds.latlngs=list;
        return bounds;
    }
    public static Point transform(int x,int y,int tx,int ty,int deg1) {
        double deg = deg1 * Math.PI / 180;
        int resx= (int)((x - tx) * Math.cos(deg) - (y - ty) * Math.sin(deg)) + tx;
        int resy= (int)((x - tx) * Math.sin(deg) + (y - ty) * Math.cos(deg)) + ty;
        return new Point(resx,resy);
    }
    public static List<LatLng> createRotatePolygon(List<LatLng> latlngs, PolygonBounds bounds, int rotate, AMap amap) {
        List<LatLng> res=new ArrayList<>();
        Point a; Point b;
        Point c = amap.getProjection().toScreenLocation(bounds.center);
        for (int i = 0; i < latlngs.size(); i++) {
            a = amap.getProjection().toScreenLocation(latlngs.get(i));
            b = transform(a.x, a.y, c.x, c.y,rotate);
            res.add(amap.getProjection().fromScreenLocation(b));
        }
        return res;
    }
    public static latline createLats(PolygonBounds bounds, double space) {
        LatLng nw = bounds.latlngs.get(0);
        LatLng sw = bounds.latlngs.get(3);
        float distance= AMapUtils.calculateLineDistance(nw,sw);//计算两点之间的距离
        int steps = (int)(distance / space );//(int)(distance / space / 2);
        double lats = (nw.latitude - sw.latitude) / steps;
        latline res= new latline();
        res.lat=lats;
        res.len=steps;
        return res;
    }
    public static LatLng createInlinePoint(LatLng p1,LatLng p2,double y) {
        double s = p1.latitude - p2.latitude;
        double x =(y - p1.latitude) * (p1.longitude - p2.longitude) / s +p1.longitude;
        if(Double.isNaN(x) || Double.isInfinite(x)){
            return  null;
        }
        if (x > p1.longitude && x > p2.longitude) {
            return null;
        }
        if (x < p1.longitude && x <p2.longitude) {
            return null;
        }
        return new LatLng(y,x);
    }
    public static int si(int i,int l) {
        if (i > l - 1) {
            return i - l;
        }
        if (i < 0) {
            return l + i;
        }
        return i;
    }
    public  static List<LatLng> setOptions(cpRPAOptions opt) {
        PolygonBounds bounds = createPolygonBounds(opt.polygon);
        List<LatLng> rPolygon = createRotatePolygon(opt.polygon, bounds, -opt.rotate,opt.aMap);
        PolygonBounds rBounds = createPolygonBounds(rPolygon);
        latline latline = createLats(rBounds, opt.space);
        List<LatLng> line =new ArrayList<>();
        LatLng check = null;
        List<LatLng> polyline =new ArrayList<>();
        for (int i = 0; i < latline.len; i++) {
            line.clear();
            for (int j = 0; j < rPolygon.size(); j++) {
                int nt = si(j + 1, rPolygon.size());
                check = createInlinePoint(rPolygon.get(j), rPolygon.get(nt),rBounds.northLat - i * latline.lat);
                if (check!=null) {
                    line.add(check);
                }
            }
            if (line.size() < 2) {
                continue;
            }
            if (line.get(0).longitude == line.get(1).longitude) {
                continue;
            }
            if (i % 2>0) {
                LatLng start=new LatLng(line.get(0).latitude,Math.max(line.get(0).longitude, line.get(1).longitude));
                LatLng end=new LatLng(line.get(0).latitude,Math.min(line.get(0).longitude, line.get(1).longitude));
                polyline.add(start);
                polyline.add(end);
            } else {
                LatLng start=new LatLng(line.get(0).latitude,Math.min(line.get(0).longitude, line.get(1).longitude));
                LatLng end=new LatLng(line.get(0).latitude,Math.max(line.get(0).longitude, line.get(1).longitude));
                polyline.add(start);
                polyline.add(end);
            }
        }
        return  createRotatePolygon(polyline, bounds, opt.rotate,opt.aMap);
    }
}
