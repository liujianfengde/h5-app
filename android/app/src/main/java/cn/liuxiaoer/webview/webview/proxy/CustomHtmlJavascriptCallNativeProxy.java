package cn.liuxiaoer.webview.webview.proxy;

import android.content.Intent;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import cn.liuxiaoer.LxeApplication;
import cn.liuxiaoer.net.subscriber.CheckupVersionSubscriber;
import cn.liuxiaoer.util.ApiManager;
import cn.liuxiaoer.webview.activity.QRCodeActivity;
import cn.liuxiaoer.webview.webview.CustomWebViewActivity;


public class CustomHtmlJavascriptCallNativeProxy {
    private static final String TAG = CustomHtmlJavascriptCallNativeProxy.class.getSimpleName();

    private CustomWebViewActivity act;
    private WebView webView;


    public CustomHtmlJavascriptCallNativeProxy(CustomWebViewActivity act, WebView webView) {
        this.act = act;
        this.webView = webView;
    }

    //二维码扫描
    @JavascriptInterface
    public void qrCodeScan() {
        Intent intent = new Intent(act, QRCodeActivity.class);
        act.startActivityForResult(intent, 100);
    }

    //显示WebView标头栏
    @JavascriptInterface
    public void showTitle() {
        act.setTitleBarVisible();
    }

    //不显示WebView标头栏
    @JavascriptInterface
    public void hideTitle() {
        act.setTitleBarGone();
    }

    //返回应用版本号
    @JavascriptInterface
    public String version() {
        return LxeApplication.getPhoneInfo().getVersionName();
    }

    @JavascriptInterface
    public void checkVersion() {
        ApiManager.getInstance().checkupVersion(new CheckupVersionSubscriber(act));
    }
}
