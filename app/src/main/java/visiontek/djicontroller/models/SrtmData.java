package visiontek.djicontroller.models;

public class SrtmData {//根据服务查询的带高度的一系列点
    public double lat;
    public double lon;
    public double height;
    public int index;//当前点所处线段的索引，用于后期排序防止异步请求错乱
}
