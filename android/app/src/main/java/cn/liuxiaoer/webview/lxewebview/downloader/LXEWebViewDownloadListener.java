package cn.liuxiaoer.webview.lxewebview.downloader;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.webkit.DownloadListener;

public class LXEWebViewDownloadListener implements DownloadListener {
    private Context mContext;

    public LXEWebViewDownloadListener(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
        //调用系统浏览器下载
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        mContext.startActivity(intent);
    }

    //自定义下载器
    private void customDownload() {

    }
}
