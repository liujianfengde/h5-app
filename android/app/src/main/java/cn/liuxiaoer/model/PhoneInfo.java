package cn.liuxiaoer.model;

/**
 * Created by jchvip on 2017/10/19.
 */

public class PhoneInfo {
    public String networkType;
    public String dpi;
    public String resolution;
    public String versionName;
    public long versionCode;
    public String deviceId;

    public String getDeviceId() {
        if (deviceId == null) {
            return "未获取到设备id信息";
        }
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public String getNetworkType() {
        return networkType;
    }

    public void setNetworkType(String networkType) {
        this.networkType = networkType;
    }

    public String getDpi() {
        return dpi;
    }

    public void setDpi(String dpi) {
        this.dpi = dpi;
    }

    public long getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(long versionCode) {
        this.versionCode = versionCode;
    }
}
