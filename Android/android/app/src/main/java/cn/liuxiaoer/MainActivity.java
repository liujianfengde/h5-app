package cn.liuxiaoer;


import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import cn.liuxiaoer.model.PhoneInfo;
import cn.liuxiaoer.util.Util;
import cn.liuxiaoer.webview.activity.QRCodeActivity;
import cn.liuxiaoer.webview.lxewebview.activity.LXEWebViewActivity;
import cn.liuxiaoer.webview.webview.CustomWebViewActivity;

import static android.os.Build.VERSION_CODES.P;
import static cn.liuxiaoer.util.Util.getUniquePsuedoID;


public class MainActivity extends Activity {
    private EditText mUrl;
    private PhoneInfo phoneInfo;
    private DisplayMetrics dm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
            collectInformation();
        mUrl = findViewById(R.id.url);
        String url = "http://116.90.80.67:8003/mobile/webapp/login.html";
        if (BuildConfig.DEBUG) {
            url = "http://116.90.80.67:8003/mobile/webapp/login.html";
        }
//        Intent intent = new Intent(MainActivity.this, CustomWebViewActivity.class);
        Intent intent = new Intent(MainActivity.this, LXEWebViewActivity.class);
        intent.putExtra("url", url);
//      Intent intent = new Intent(MainActivity.this, DownloadFileActivity.class);
        startActivity(intent);

    }

    public void goUrl(View view) {
        if ("".equals(mUrl.getText().toString())) {
            Toast.makeText(this, "请输入访问地址", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(MainActivity.this, CustomWebViewActivity.class);
        intent.putExtra("url", mUrl.getText().toString());
        startActivity(intent);
    }

    public void openQrcode(View view) {
        Intent intent = new Intent(MainActivity.this, QRCodeActivity.class);
        startActivity(intent);
    }

    /**
     * 采集设备信息
     * 用作请求头
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void collectInformation() {
        try {
            phoneInfo = new PhoneInfo();
            phoneInfo.setNetworkType(Util.GetNetworkType(MainActivity.this));
            //分辨率
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
            int width = metrics.widthPixels;
            int height = metrics.heightPixels;
            phoneInfo.setResolution(width + "*" + height);
            // 获取屏幕密度
            dm = new DisplayMetrics();
            dm = getResources().getDisplayMetrics();
            phoneInfo.setDpi(dm.densityDpi + "");
            phoneInfo.setDeviceId(getUniquePsuedoID());
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            phoneInfo.setVersionName(packageInfo.versionName);


            if (Build.VERSION.SDK_INT >= P) {
                phoneInfo.setVersionCode(packageInfo.getLongVersionCode());
            } else {
                phoneInfo.setVersionCode(packageInfo.versionCode);
            }
            LxeApplication.getInstance().setPhoneInfo(phoneInfo);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }
}




