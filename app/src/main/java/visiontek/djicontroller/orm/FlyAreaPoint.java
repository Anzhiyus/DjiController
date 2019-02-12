package visiontek.djicontroller.orm;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Generated;

@Entity(indexes = {
        @Index(value = "taskid")//建立索引
})
public class FlyAreaPoint {//存储任务飞行区域
    @Id
    public String id;
    @NotNull
    public String taskid;//所属任务id
    public double lat;
    public double lon;

    @Generated(hash = 1398404856)
    public FlyAreaPoint(String id, @NotNull String taskid, double lat, double lon) {
        this.id = id;
        this.taskid = taskid;
        this.lat = lat;
        this.lon = lon;
    }
    @Generated(hash = 1463023451)
    public FlyAreaPoint() {
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
}
