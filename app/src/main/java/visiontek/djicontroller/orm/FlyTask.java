package visiontek.djicontroller.orm;

import android.support.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Property;

import java.util.Date;
import org.greenrobot.greendao.annotation.Generated;

@Entity(indexes = {
        @Index(value = "id, createtime DESC", unique = true)//建立索引
})
public class FlyTask {
    @Id
    public String id;//键值
    @NotNull
    public String taskname;//任务名称
    public Date createtime;//创建日期

    public int compeletePointSize;//在全部航点中已经完成的点数量
    public int currentStart;//当前执行的航线起点位置
    public int currentEnd;//当前执行的航线终点位置

    public int taskstatus;//任务状态0.未开始，1正在运行
    public String cameraid;//相机信息键值
    public int cameradirection;//相机朝向0,1代表平行或垂直
    public int hover;//悬停拍照0关闭1打开
    public float speed;//飞行速度
    public int FlyHeight;//飞行理论实际高度=参数计算高度-（起飞点海拔-基准面海拔）也可以手动指定
    public int GoHomeHeight;//手动指定返航高度
    public float adjacentverlapping;//主航线重叠度 同向
    public float parallellapping;//主航线间叠度 旁向
    public int airwayangle;//航线角度
    public int finishaction;//任务完成动作0.返航1.悬停2.原地降落
    //public int interval;//拍照间隔
    public float gsd; //（地面分辨率）
    public int areaASL;//基准面海拔高度
    public int homeASL;//起飞点海拔高度
    public int pitch;//云台俯仰角度
    public int yaw;//偏航角度
    public int isThridCamera;//是否第三方相机
    public int isSrtm;//是否贴地飞行
    public String srtmDataFile;//地形文件数据当区域发生变化保存时此字段必须清空并删除地形文件
@Generated(hash = 1446562163)
public FlyTask(String id, @NotNull String taskname, Date createtime,
        int compeletePointSize, int currentStart, int currentEnd,
        int taskstatus, String cameraid, int cameradirection, int hover,
        float speed, int FlyHeight, int GoHomeHeight, float adjacentverlapping,
        float parallellapping, int airwayangle, int finishaction, float gsd,
        int areaASL, int homeASL, int pitch, int yaw, int isThridCamera,
        int isSrtm, String srtmDataFile) {
    this.id = id;
    this.taskname = taskname;
    this.createtime = createtime;
    this.compeletePointSize = compeletePointSize;
    this.currentStart = currentStart;
    this.currentEnd = currentEnd;
    this.taskstatus = taskstatus;
    this.cameraid = cameraid;
    this.cameradirection = cameradirection;
    this.hover = hover;
    this.speed = speed;
    this.FlyHeight = FlyHeight;
    this.GoHomeHeight = GoHomeHeight;
    this.adjacentverlapping = adjacentverlapping;
    this.parallellapping = parallellapping;
    this.airwayangle = airwayangle;
    this.finishaction = finishaction;
    this.gsd = gsd;
    this.areaASL = areaASL;
    this.homeASL = homeASL;
    this.pitch = pitch;
    this.yaw = yaw;
    this.isThridCamera = isThridCamera;
    this.isSrtm = isSrtm;
    this.srtmDataFile = srtmDataFile;
}
@Generated(hash = 226878336)
public FlyTask() {
}
public String getId() {
    return this.id;
}
public void setId(String id) {
    this.id = id;
}
public String getTaskname() {
    return this.taskname;
}
public void setTaskname(String taskname) {
    this.taskname = taskname;
}
public Date getCreatetime() {
    return this.createtime;
}
public void setCreatetime(Date createtime) {
    this.createtime = createtime;
}
public int getCompeletePointSize() {
    return this.compeletePointSize;
}
public void setCompeletePointSize(int compeletePointSize) {
    this.compeletePointSize = compeletePointSize;
}
public int getCurrentStart() {
    return this.currentStart;
}
public void setCurrentStart(int currentStart) {
    this.currentStart = currentStart;
}
public int getCurrentEnd() {
    return this.currentEnd;
}
public void setCurrentEnd(int currentEnd) {
    this.currentEnd = currentEnd;
}
public int getTaskstatus() {
    return this.taskstatus;
}
public void setTaskstatus(int taskstatus) {
    this.taskstatus = taskstatus;
}
public String getCameraid() {
    return this.cameraid;
}
public void setCameraid(String cameraid) {
    this.cameraid = cameraid;
}
public int getCameradirection() {
    return this.cameradirection;
}
public void setCameradirection(int cameradirection) {
    this.cameradirection = cameradirection;
}
public int getHover() {
    return this.hover;
}
public void setHover(int hover) {
    this.hover = hover;
}
public float getSpeed() {
    return this.speed;
}
public void setSpeed(float speed) {
    this.speed = speed;
}
public int getFlyHeight() {
    return this.FlyHeight;
}
public void setFlyHeight(int FlyHeight) {
    this.FlyHeight = FlyHeight;
}
public int getGoHomeHeight() {
    return this.GoHomeHeight;
}
public void setGoHomeHeight(int GoHomeHeight) {
    this.GoHomeHeight = GoHomeHeight;
}
public float getAdjacentverlapping() {
    return this.adjacentverlapping;
}
public void setAdjacentverlapping(float adjacentverlapping) {
    this.adjacentverlapping = adjacentverlapping;
}
public float getParallellapping() {
    return this.parallellapping;
}
public void setParallellapping(float parallellapping) {
    this.parallellapping = parallellapping;
}
public int getAirwayangle() {
    return this.airwayangle;
}
public void setAirwayangle(int airwayangle) {
    this.airwayangle = airwayangle;
}
public int getFinishaction() {
    return this.finishaction;
}
public void setFinishaction(int finishaction) {
    this.finishaction = finishaction;
}
public float getGsd() {
    return this.gsd;
}
public void setGsd(float gsd) {
    this.gsd = gsd;
}
public int getAreaASL() {
    return this.areaASL;
}
public void setAreaASL(int areaASL) {
    this.areaASL = areaASL;
}
public int getHomeASL() {
    return this.homeASL;
}
public void setHomeASL(int homeASL) {
    this.homeASL = homeASL;
}
public int getPitch() {
    return this.pitch;
}
public void setPitch(int pitch) {
    this.pitch = pitch;
}
public int getYaw() {
    return this.yaw;
}
public void setYaw(int yaw) {
    this.yaw = yaw;
}
public int getIsThridCamera() {
    return this.isThridCamera;
}
public void setIsThridCamera(int isThridCamera) {
    this.isThridCamera = isThridCamera;
}
public int getIsSrtm() {
    return this.isSrtm;
}
public void setIsSrtm(int isSrtm) {
    this.isSrtm = isSrtm;
}
public String getSrtmDataFile() {
    return this.srtmDataFile;
}
public void setSrtmDataFile(String srtmDataFile) {
    this.srtmDataFile = srtmDataFile;
}

}
