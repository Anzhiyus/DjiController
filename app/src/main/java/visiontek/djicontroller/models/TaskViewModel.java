package visiontek.djicontroller.models;



import visiontek.djicontroller.orm.FlyCamera;
import visiontek.djicontroller.orm.FlyTask;

public class TaskViewModel extends FlyTask {
    public float FlyHeight;//飞行高度
    public float BasePlaneHeight;//相对基准面的高度
    public FlyCamera cameraInfo;//相机信息
    public float lineSpace;//航线间距
    public float pointSpace;//航点间距
    //获取飞行高度
    private float getFlyHeight(FlyTask task,FlyCamera camera){
        if(task!=null) {
            if(task.FlyHeight>0){
                return task.FlyHeight;
            }
            else{
                return getBasePlaneHeight(task, camera) -(task.homeASL-task.areaASL);//飞行实际高度=参数计算高度-（起飞点海拔-基准面海拔）
            }
        }
        else
            return 0;
    }
    //获取相对基准面高度
    private float getBasePlaneHeight(FlyTask task,FlyCamera camera){
        if(task!=null){
            float gsd=task.gsd;
            if(camera!=null){
                float f= camera.focallength;//焦距
                float a=camera.opticalFormat;//像元尺寸
                float height=f*gsd/a;//飞机相对基准面高度
                return  height*1000;
            }
        }
        return 0;
    }

    public float getLineSpace(FlyTask task,FlyCamera camera){
        if(task!=null){
            float gsd=task.gsd;//单位M
            float spacing=0;
            if(task.cameradirection==0){//相机朝向为0时，长边为旁向
                spacing=(1-task.parallellapping)*(gsd*camera.imagewidth);
            }
            else{//90度时短边为旁向重叠度
                spacing=(1-task.parallellapping)*(gsd*camera.imageheight);
            }
            return spacing;
        }
        return 0;
    }
    public float getPointSpace(FlyTask task,FlyCamera camera){
        if(task!=null){
            float gsd=task.gsd;
            float spacing=0;//航线间距m
            if(task.cameradirection==0){//相机朝向为0时短边为同向
                spacing= (1-task.adjacentverlapping)*(gsd*camera.imageheight);
            }
            else{//90度时长边为同向
                spacing=(1-task.adjacentverlapping)*(gsd*camera.imagewidth);
            }
            return spacing;
        }
        return 0;
    }

    public TaskViewModel(FlyTask task,FlyCamera camera){
        this.BasePlaneHeight=getBasePlaneHeight(task,camera);
        this.cameraInfo=camera;
        this.FlyHeight=this.FlyHeight==0?getFlyHeight(task,camera):this.FlyHeight;
        this.GoHomeHeight=task.GoHomeHeight;
        this.lineSpace=getLineSpace(task,camera);
        this.pointSpace=getPointSpace(task,camera);
        this.adjacentverlapping=task.adjacentverlapping;
        this.airwayangle=task.airwayangle;
        this.yaw=task.yaw;
        this.areaASL=task.areaASL;
        this.cameradirection=task.cameradirection;
        this.cameraid=task.cameraid;
        this.createtime=task.createtime;
        this.finishaction=task.finishaction;
        this.gsd=task.gsd;
        this.homeASL=task.homeASL;
        this.hover=task.hover;
        this.id=task.id;
        this.parallellapping=task.parallellapping;
        this.compeletePointSize=task.compeletePointSize;
        this.pitch=task.pitch;
        this.speed=task.speed;
        this.taskname=task.taskname;
        this.taskstatus=task.taskstatus;
        this.isThridCamera=task.isThridCamera;
        this.currentStart=task.currentStart;
        this.currentEnd=task.currentEnd;
        this.isSrtm=task.isSrtm;
        this.srtmDataFile=task.srtmDataFile;
    }
}
