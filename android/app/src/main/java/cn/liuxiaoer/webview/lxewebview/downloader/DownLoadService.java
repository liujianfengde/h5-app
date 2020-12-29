package cn.liuxiaoer.webview.lxewebview.downloader;

import android.app.Dialog;
import android.app.DownloadManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.Toast;

import java.util.Date;

import cn.liuxiaoer.util.FileUtil;
import cn.liuxiaoer.webview.webview.util.CookieUtil;
import cn.liuxiaoer.webview.webview.util.LogUtil;

import static android.widget.Toast.LENGTH_SHORT;

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
    private String fileName;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        downloadUrl = intent.getStringExtra("downLoadUrl");
        fileName = intent.getStringExtra("fileName");
        startDownload(downloadUrl);

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Toast.makeText(context, "文件下载成功！", LENGTH_SHORT).show();
                //销毁当前的Service
                stopSelf();
            }
        };
        registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
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

        CookieSyncManager.createInstance(this);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setCookie(downUrl, CookieUtil.getParam(this).toString());
        CookieSyncManager.getInstance().sync();
        request.setDestinationInExternalPublicDir(FileUtil.DOWNLOAD_PATH, fileName);
        //设置下载时或者下载完成时，通知栏是否显示
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setTitle("文件下载");
        //执行下载，并返回任务唯一id
        enqueue = dm.enqueue(request);
    }
}