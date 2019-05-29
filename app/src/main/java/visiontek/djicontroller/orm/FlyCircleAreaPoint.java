package visiontek.djicontroller.orm;

import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;

public class FlyCircleAreaPoint {//圆形区域
    @Id
    public String id;
    @NotNull
    public String taskid;//所属任务id
    public double centerlat;//圆心位置
    public double centerlon;
    public float radius;//半径
}
