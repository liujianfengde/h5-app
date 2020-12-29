package cn.liuxiaoer.webview.webview.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class NetworkInfoUtil {

    public static boolean isAvailable(Context context){
        if (context != null) {
            try {
                ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                        .getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
                if (mNetworkInfo != null) {
                    return mNetworkInfo.isAvailable();
                }
            }catch (Exception e){
                Log.e("isNetworkConnected: ", "获取网络信息出错");
            }
        }
        return false;
    }
}
