package visiontek.djicontroller.orm;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class FlyCamera {
    @Id
    public String id;
    @NotNull
    public float focallength;//相机焦距 mm
    @NotNull
    public String name;//名称
    public int iso;//iso感光度
    public int aperture;//光圈av
    public int exposurecompensation;//曝光ev
    public int shutterspeed;//快门
    public int imagewidth;//照片宽度 px
    public int imageheight;//照片高度 px
    public float opticalFormat;//像元尺寸
    //public float opticalFormatHeight;//感光像元高度 mm
    public String imgtype;//图片格式 JPEG;RAW
    @Generated(hash = 1292744425)
    public FlyCamera(String id, float focallength, @NotNull String name, int iso,
            int aperture, int exposurecompensation, int shutterspeed,
            int imagewidth, int imageheight, float opticalFormat, String imgtype) {
        this.id = id;
        this.focallength = focallength;
        this.name = name;
        this.iso = iso;
        this.aperture = aperture;
        this.exposurecompensation = exposurecompensation;
        this.shutterspeed = shutterspeed;
        this.imagewidth = imagewidth;
        this.imageheight = imageheight;
        this.opticalFormat = opticalFormat;
        this.imgtype = imgtype;
    }
    @Generated(hash = 165716801)
    public FlyCamera() {
    }
    public String getId() {
        return this.id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public float getFocallength() {
        return this.focallength;
    }
    public void setFocallength(float focallength) {
        this.focallength = focallength;
    }
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getIso() {
        return this.iso;
    }
    public void setIso(int iso) {
        this.iso = iso;
    }
    public int getAperture() {
        return this.aperture;
    }
    public void setAperture(int aperture) {
        this.aperture = aperture;
    }
    public int getExposurecompensation() {
        return this.exposurecompensation;
    }
    public void setExposurecompensation(int exposurecompensation) {
        this.exposurecompensation = exposurecompensation;
    }
    public int getShutterspeed() {
        return this.shutterspeed;
    }
    public void setShutterspeed(int shutterspeed) {
        this.shutterspeed = shutterspeed;
    }
    public int getImagewidth() {
        return this.imagewidth;
    }
    public void setImagewidth(int imagewidth) {
        this.imagewidth = imagewidth;
    }
    public int getImageheight() {
        return this.imageheight;
    }
    public void setImageheight(int imageheight) {
        this.imageheight = imageheight;
    }
    public float getOpticalFormat() {
        return this.opticalFormat;
    }
    public void setOpticalFormat(float opticalFormat) {
        this.opticalFormat = opticalFormat;
    }
    public String getImgtype() {
        return this.imgtype;
    }
    public void setImgtype(String imgtype) {
        this.imgtype = imgtype;
    }

}
