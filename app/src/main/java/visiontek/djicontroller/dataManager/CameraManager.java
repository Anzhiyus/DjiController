package visiontek.djicontroller.dataManager;

import java.util.List;

import visiontek.djicontroller.orm.DataRepository;
import visiontek.djicontroller.orm.FlyCamera;

public class CameraManager {
    private DataRepository _dataRepository;
    public CameraManager(){
        _dataRepository=new DataRepository();
    }
    public List<FlyCamera> getCameraList(){
        return  _dataRepository.getCameraList();
    }
    public void RemoveCamera(String cameraid){
        _dataRepository.deleteCamera(cameraid);
        _dataRepository.ClearTaskCameraInfo(cameraid);
    }
    public FlyCamera GetCamera(String cameraid){
        return _dataRepository.getCamera(cameraid);
    }
    public void SaveCamera(FlyCamera camera){
        _dataRepository.saveCamera(camera);
    }
}
