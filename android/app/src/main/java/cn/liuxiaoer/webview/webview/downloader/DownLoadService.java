package cn.liuxiaoer.webview.webview.downloader;

import android.app.Activity;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import androidx.core.content.FileProvider;

import java.io.File;
import java.util.Date;

import cn.liuxiaoer.webview.webview.util.CookieUtil;
import cn.liuxiaoer.webview.webview.util.LogUtil;

import static cn.liuxiaoer.util.PermissionCheckUtil.verifyInstallPermission;

/**
 * Created by liuxioer@live.cn on 2020/08/05.
 */

public class DownLoadService extends Service {
    private static final String TAG = DownLoadService.class.getSimpleName();
    /**
     * 广播接受者
     */
    private BroadcastReceiver receiver;
    /**
     * 系统下载管理器
     */
    private DownloadManager dm;
    /**
     * 系统下载器分配的唯一下载任务id，可以通过这个id查询或者处理下载任务
     */
    private long enqueue;
    /**
     * TODO下载地址 需要自己修改,这里随便找了一个
     */
    private String downloadUrl;

    Dialog dialog;

    public static ThreadLocal<String> threadLocal = new ThreadLocal<>();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        downloadUrl = intent.getStringExtra("downLoadUrl");
        startDownload(downloadUrl);
        return Service.START_REDELIVER_INTENT;
    }


    @Override
    public void onDestroy() {
        //服务销毁的时候 反注册广播
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    private void startDownload(String downUrl) {
        LogUtil.d(TAG, "startDownload:", downUrl);
        //获得系统下载器
        dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        //设置下载地址
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downUrl));
        //设置下载文件的类型
        request.setMimeType("application/vnd.android.package-archive");

        CookieSyncManager.createInstance(this);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setCookie(downUrl, CookieUtil.getParam(this).toString());
        CookieSyncManager.getInstance().sync();
        //设置下载存放的文件夹和文件名字
        Date now = new Date();
        String apkName = String.format("%s-%d.apk", getPackageName(), now.getTime());
        threadLocal.set(apkName);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, apkName);
        //设置下载时或者下载完成时，通知栏是否显示
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setTitle("下载新版本");
        //执行下载，并返回任务唯一id
        enqueue = dm.enqueue(request);
    }
}