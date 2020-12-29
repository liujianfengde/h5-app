package cn.liuxiaoer.webview.lxewebview.proxy;

import android.content.Intent;
import android.webkit.JavascriptInterface;

import cn.liuxiaoer.DownloadFileActivity;
import cn.liuxiaoer.LxeApplication;
import cn.liuxiaoer.net.subscriber.CheckupVersionSubscriber;
import cn.liuxiaoer.util.ApiManager;
import cn.liuxiaoer.webview.lxewebview.activity.LXEWebViewActivity;
import cn.liuxiaoer.webview.activity.QRCodeActivity;
import cn.liuxiaoer.webview.lxewebview.LXEWebView;


public class LXEHtmlJavascriptCallNativeProxy {
    private static final String TAG = LXEHtmlJavascriptCallNativeProxy.class.getSimpleName();

    LXEWebViewActivity activity;
    LXEWebView lxeWebView;

    public LXEHtmlJavascriptCallNativeProxy(LXEWebViewActivity activity, LXEWebView lxeWebView) {
        this.activity = activity;
        this.lxeWebView = lxeWebView;
    }

    //二维码扫描
    @JavascriptInterface
    public void qrCodeScan() {
        Intent intent = new Intent(lxeWebView.getContext(), QRCodeActivity.class);
        activity.startActivityForResult(intent, 100);
    }

    //显示WebView标头栏
    @JavascriptInterface
    public void showTitle() {
        lxeWebView.setTitleBarVisible();
    }

    //不显示WebView标头栏
    @JavascriptInterface
    public void hideTitle() {
        lxeWebView.setTitleBarGone();
    }

    //返回应用版本号
    @JavascriptInterface
    public String version() {
        return LxeApplication.getPhoneInfo().getVersionName();
    }

    @JavascriptInterface
    public void checkVersion() {
        ApiManager.getInstance().checkupVersion(new CheckupVersionSubscriber(activity));
    }

    @JavascriptInterface
    public void goBack() {
        lxeWebView.goBack();
    }

    @JavascriptInterface
    public void goBackAndRefreshPrePage() {
        lxeWebView.goBackAndRefreshPrePage();
    }

    @JavascriptInterface
    public void openDownloadPage() {
        Intent intent = new Intent(activity, DownloadFileActivity.class);
        activity.startActivity(intent);
    }
}