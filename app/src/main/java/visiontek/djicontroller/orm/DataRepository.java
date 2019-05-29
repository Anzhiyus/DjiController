package visiontek.djicontroller.orm;
import java.util.List;
import java.util.UUID;

import visiontek.djicontroller.DJIApplication;
//数据仓储包括全部持久化的任务，相机，航点信息操作
public class DataRepository {
    private DaoSession daoSession=DJIApplication.getDaoSession();

    //获取指定任务的全部区域顶点
    public List<FlyAreaPoint> getFlyAreaPoint(String taskid){
        if(taskid==null||taskid.isEmpty()){
            return null;
        }
        else{
            FlyAreaPointDao dao=daoSession.getFlyAreaPointDao();
            return dao.queryBuilder().where(FlyAreaPointDao.Properties.Taskid.eq(taskid)).list();
        }
    }
    //获取指定任务的变高区域范围
    public List<HeightAreaPoint> getHeightAreaPoint(String taskid){
        if(taskid==null||taskid.isEmpty()){
            return null;
        }
        else{
            HeightAreaPointDao dao=daoSession.getHeightAreaPointDao();
            return dao.queryBuilder().where(HeightAreaPointDao.Properties.Taskid.eq(taskid)).list();
        }
    }
    //获取任务列表
    public List<FlyTask> getTaskList(){
        FlyTaskDao dao=daoSession.getFlyTaskDao();
        return  dao.loadAll();
    }
    //获取相机列表
    public List<FlyCamera> getCameraList(){
        FlyCameraDao dao=daoSession.getFlyCameraDao();
        return  dao.loadAll();
    }
    //获取指定相机参数
    public FlyCamera getCamera(String id){
        FlyCameraDao dao=daoSession.getFlyCameraDao();
        return dao.load(id);
    }
    //获取指定任务
    public FlyTask getTask(String id){
        FlyTaskDao dao=daoSession.getFlyTaskDao();
        return dao.load(id);
    }

    //保存相机信息
    public void saveCamera(FlyCamera data){
        FlyCameraDao dao=daoSession.getFlyCameraDao();
        if(data.id==null||data.id.isEmpty()){
            data.id= UUID.randomUUID().toString();
            dao.insert(data);
        }
        else{
            dao.update(data);
        }
        daoSession.clear();
    }
    //删除相机信息
    public void deleteCamera(String id){
        FlyCameraDao dao=daoSession.getFlyCameraDao();
        dao.deleteByKey(id);
    }
    //保存任务信息
    public void saveTask(FlyTask task){
        FlyTaskDao dao = daoSession.getFlyTaskDao();
        if(task.id==null||task.id.isEmpty()){
            task.id= UUID.randomUUID().toString();
            dao.insert(task);
        }
        else{
            dao.update(task);
        }
        daoSession.clear();
    }

    //删除任务信息(同时删除区域)
    public void removeTask(String id){
        FlyTaskDao dao=daoSession.getFlyTaskDao();
        dao.deleteByKey(id);
        final FlyAreaPointDao pointDao=daoSession.getFlyAreaPointDao();
        final List<FlyAreaPoint> exists=getFlyAreaPoint(id);
        daoSession.runInTx(new Runnable() {
            @Override
            public void run() {
                if(exists.size()>0){
                    pointDao.deleteInTx(exists);
                }
            }
        });
        daoSession.clear();
    }
    //保存任务
    public void saveTaskPoints(String taskId, final List<FlyAreaPoint> list){
        if(list == null || list.isEmpty()){
            return;
        }
        final FlyAreaPointDao dao=daoSession.getFlyAreaPointDao();
        final List<FlyAreaPoint> exists=getFlyAreaPoint(taskId);
        daoSession.runInTx(new Runnable() {
            @Override
            public void run() {
                    if(exists.size()>0){
                        dao.deleteInTx(exists);
                    }
                    if(list.size()>0){
                        dao.insertInTx(list);
                    }
            }
        });
        daoSession.clear();
    }
    public void clearTaskHeightArea(String taskId){
        final HeightAreaPointDao dao=daoSession.getHeightAreaPointDao();
        final List<HeightAreaPoint> exists=getHeightAreaPoint(taskId);
        daoSession.runInTx(new Runnable() {
            @Override
            public void run() {
                if(exists.size()>0){
                    dao.deleteInTx(exists);
                }
            }
        });
        daoSession.clear();
    }
    public void saveTaskHeightAreaPoints(String taskId, final List<HeightAreaPoint> list){
        if(list==null){
            return;
        }
        final HeightAreaPointDao dao=daoSession.getHeightAreaPointDao();
        final List<HeightAreaPoint> exists=getHeightAreaPoint(taskId);
        daoSession.runInTx(new Runnable() {
            @Override
            public void run() {
                if(exists.size()>0){
                    dao.deleteInTx(exists);
                }
                if(!list.isEmpty()){
                    dao.insertInTx(list);
                }
            }
        });
        daoSession.clear();
    }
    public void ClearTaskCameraInfo(String cameraid){
        final FlyTaskDao dao=daoSession.getFlyTaskDao();
        final List<FlyTask> list=dao.queryBuilder().where(FlyTaskDao.Properties.Cameraid.eq(cameraid)).list();
        daoSession.runInTx(new Runnable() {
            @Override
            public void run() {
                dao.deleteInTx(list);
            }
        });
        daoSession.clear();
    }

}
