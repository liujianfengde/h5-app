package cn.liuxiaoer;

import android.app.Application;

import cn.liuxiaoer.model.PhoneInfo;

public class LxeApplication extends Application {
    private static LxeApplication instance;
    private static PhoneInfo phoneInfo;


    public static LxeApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public void setPhoneInfo(PhoneInfo phoneInfo) {
        this.phoneInfo = phoneInfo;
    }

    public static PhoneInfo getPhoneInfo() {
        return phoneInfo;
    }
}
