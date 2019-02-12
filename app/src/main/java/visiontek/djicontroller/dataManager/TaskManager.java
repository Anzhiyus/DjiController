package visiontek.djicontroller.dataManager;



import com.amap.api.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import visiontek.djicontroller.models.TaskViewModel;
import visiontek.djicontroller.orm.DataRepository;
import visiontek.djicontroller.orm.FlyAreaPoint;
import visiontek.djicontroller.orm.FlyCamera;
import visiontek.djicontroller.orm.FlyTask;
import visiontek.djicontroller.orm.HeightAreaPoint;

public class TaskManager {
    private DataRepository _dataRepository;
    public TaskManager(){
        _dataRepository=new DataRepository();
    }
    public List<FlyAreaPoint> getFlyAreaPoints(String taskid){
        return _dataRepository.getFlyAreaPoint(taskid);
    }
    public List<HeightAreaPoint> getHeightAreaPoints(String taskid){
        return _dataRepository.getHeightAreaPoint(taskid);
    }

    private boolean checkLocation(double latitude,double longitude){
        return (latitude > -90 && latitude < 90 && longitude > -180 && longitude < 180) && (latitude != 0f
                && longitude != 0f);
    }

    private FlyAreaPoint ConvertLatLng2AreaPoint(LatLng latlng,String taskid){
        UUID uuid = UUID.randomUUID();
        return new FlyAreaPoint(uuid.toString(),taskid,latlng.latitude,latlng.longitude);//String id, String taskid, double lat, double lon
    }

    //保存task界面到数据库
    private void SaveTaskAndArea(FlyTask task,List<LatLng> polygon){
        //task.airwayangle=airwayangle//_amapTool.GetFlyLinesAngle();//0-180
        int angle=task.cameradirection==0?90:0;
        task.yaw=task.airwayangle+angle;//飞行器旋转角度
        if(task.GoHomeHeight<task.FlyHeight){
            task.GoHomeHeight=(int)task.FlyHeight;
        }
        _dataRepository.saveTask(task);
        //List<LatLng> area=_amapTool.getPointsFromArea();
        List<FlyAreaPoint> points=new ArrayList<>();
        for (int i=0;i<polygon.size();i++) {
            points.add(ConvertLatLng2AreaPoint(polygon.get(i),task.id));
        }
        _dataRepository.saveTaskPoints(task.id,points);//保存区域信息
    }

    public void RemoveTask(String taskid){
        _dataRepository.removeTask(taskid);
    }
    public List<FlyTask> getTaskList(){
        return _dataRepository.getTaskList();
    }
    //获取单个task
    public TaskViewModel getTask(String id){
        FlyTask task= _dataRepository.getTask(id);
        if(task!=null){
            FlyCamera camera=_dataRepository.getCamera(task.cameraid);
            if(camera==null){
                return null;
            }
            return new TaskViewModel(task,camera);
        }
        return null;
    }
    //保存task
    public void SaveTask(TaskViewModel taskViewModel,List<LatLng> polygon){
        SaveTaskAndArea(taskViewModel,polygon);
    }
    public void SaveTask(TaskViewModel task){
        int angle=task.cameradirection==0?90:0;
        task.yaw=task.airwayangle+angle;//飞行器旋转角度
        if(task.GoHomeHeight<task.FlyHeight){
            task.GoHomeHeight=(int)task.FlyHeight;
        }
        _dataRepository.saveTask(task);
    }
    public void SaveHeightArea(String taskid,List<HeightAreaPoint> list){
        _dataRepository.saveTaskHeightAreaPoints(taskid,list);
    }
}
