package cn.liuxiaoer.webview.webview.util;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class SystemSetting {
    /**
     * 设置网络
     *
     * @param context
     */
    public static void wifiSetting(Context context) {
        if (context == null)
            return;
        try {
            if (Build.VERSION.SDK_INT > 10) {
                context.startActivity(new Intent("android.settings.WIFI_SETTINGS"));
                return;
            }
        } catch (Exception localException) {
            localException.printStackTrace();
            return;
        }
        context.startActivity(new Intent("android.settings.WIFI_SETTINGS"));
    }
}
