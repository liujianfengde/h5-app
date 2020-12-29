package cn.liuxiaoer.webview.webview.downloader;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;

import cn.liuxiaoer.util.FileUtil;
import cn.liuxiaoer.webview.lxewebview.downloader.DownLoadService;

public class CustomWebViewDownloadListener implements DownloadListener {
    private Context mContext;

    public CustomWebViewDownloadListener(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
        customDownload(url, userAgent, contentDisposition, mimetype, contentLength);
    }

    //自定义下载器
    private void customDownload(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
        String fileName = URLUtil.guessFileName(url, contentDisposition, mimetype);
        Intent updateIntent = new Intent(mContext, DownLoadService.class);
        updateIntent.putExtra("downLoadUrl", url);
        updateIntent.putExtra("fileName", fileName);
        mContext.startService(updateIntent);
    }

    private void browserDownload(String url) {
        //调用系统浏览器下载
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        mContext.startActivity(intent);
    }
}