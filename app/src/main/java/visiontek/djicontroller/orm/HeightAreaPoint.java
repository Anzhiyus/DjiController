package visiontek.djicontroller.orm;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Generated;

@Entity(indexes = {
        @Index(value = "taskid")//建立索引
})
public class HeightAreaPoint {//存储任务变高区域及其颜色
    @Id
    public String id;
    @NotNull
    public String taskid;//所属任务id
    public double lat;
    public double lon;
    public int color;//颜色
    public int height;//高度
@Generated(hash = 158116592)
public HeightAreaPoint(String id, @NotNull String taskid, double lat,
        double lon, int color, int height) {
    this.id = id;
    this.taskid = taskid;
    this.lat = lat;
    this.lon = lon;
    this.color = color;
    this.height = height;
}
@Generated(hash = 958102596)
public HeightAreaPoint() {
}
public String getId() {
    return this.id;
}
public void setId(String id) {
    this.id = id;
}
public String getTaskid() {
    return this.taskid;
}
public void setTaskid(String taskid) {
    this.taskid = taskid;
}
public double getLat() {
    return this.lat;
}
public void setLat(double lat) {
    this.lat = lat;
}
public double getLon() {
    return this.lon;
}
public void setLon(double lon) {
    this.lon = lon;
}
public int getColor() {
    return this.color;
}
public void setColor(int color) {
    this.color = color;
}
public int getHeight() {
    return this.height;
}
public void setHeight(int height) {
    this.height = height;
}

}
