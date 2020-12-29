package cn.liuxiaoer.webview.lxewebview.util;

import android.util.Log;

public class LogUtil {
    private static final String TAG = LogUtil.class.getSimpleName();

    public static void d(String tag, String... message) {
        StringBuilder sb = new StringBuilder();
        sb.append(TAG);
        sb.append("\t");
        for (int i = 0; i < message.length; i++) {
            sb.append(message[i]);
        }
        Log.e(tag, sb.toString());
    }
}
