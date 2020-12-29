package cn.liuxiaoer.webview.lxewebview.client;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Message;
import android.view.KeyEvent;
import android.webkit.ClientCertRequest;
import android.webkit.HttpAuthHandler;
import android.webkit.RenderProcessGoneDetail;
import android.webkit.SafeBrowsingResponse;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Set;

import cn.liuxiaoer.LxeApplication;
import cn.liuxiaoer.util.FileUtil;
import cn.liuxiaoer.webview.lxewebview.activity.LXEWebViewActivity;
import cn.liuxiaoer.webview.lxewebview.LXEWebView;
import cn.liuxiaoer.webview.lxewebview.downloader.DownLoadService;
import cn.liuxiaoer.webview.lxewebview.proxy.LXENativeCallHtmlJavascriptProxy;
import cn.liuxiaoer.webview.lxewebview.util.LogUtil;


public class LXEWebViewClient extends WebViewClient {
    private static final String TAG = LXEWebViewClient.class.getSimpleName();
    private final LXEWebViewActivity activity;
    private LXEWebView lxeWebView;

    public LXEWebViewClient(LXEWebViewActivity activity, LXEWebView view) {
        this.activity = activity;
        this.lxeWebView = view;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        LogUtil.d(TAG, "shouldOverrideUrlLoading url:", url);
        if (url.startsWith("tel:") || url.startsWith("sms:") || url.startsWith("mailto:")) {
            // 处理h5页面中的 打电话、发短信、发邮件 请求
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
//            context.startActivity(intent);
        } else if (url.startsWith("http:") || url.startsWith("https:")) {
            activity.loadUrl(url);
        }

        // 清除中科院科技网通行证登录的cookie
//        CookieUtil.deleteCookiesForDomain(context.getApplicationContext(), "https://passport.escience.cn");
        // 中科院科技网通行证登录校验后，响应的url，其响应结果在参数中
        if (url.startsWith("protocol://android?code=CasLogin")) {
            final JSONObject resultJson = new JSONObject();
            try {
                String queryParams = url.substring(url.indexOf("?") + 1);
                String[] paramArray = queryParams.split("&");
                for (String paramStr : paramArray) {
                    String[] paramEntryArray = paramStr.split("=");
                    resultJson.put(paramEntryArray[0], paramEntryArray.length > 1 ? paramEntryArray[1] : "");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        LogUtil.d(TAG, "TargetApi21 shouldOverrideUrlLoading url:", request.getUrl().toString());
        String url = request.getUrl().toString();
        shouldOverrideUrlLoading(view, url);
        return true;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        LogUtil.d(TAG, "onPageStarted url:", url);
        if (url.indexOf("https://passport.escience.cn/oauth2/authorize") != -1) {
            lxeWebView.setTitleBarVisible();
        } else {
            lxeWebView.setTitleBarGone();
        }
        super.onPageStarted(view, url, favicon);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        LogUtil.d(TAG, "onPageFinished url:", url);
        super.onPageFinished(view, url);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            view.evaluateJavascript(String.format("window.appVersion='%s'", LxeApplication.getPhoneInfo().getVersionName()), null);
            view.evaluateJavascript("window.history.back=function(){CustomJSBridge.goBack()}", null);
            view.evaluateJavascript("window.pageLoaded && window.pageLoaded()", null);
        } else {
            view.loadUrl(String.format("window.appVersion='%s'", LxeApplication.getPhoneInfo().getVersionName()));
            view.loadUrl("window.history.back=function(){CustomJSBridge.goBack()}");
            view.loadUrl("window.pageLoaded && window.pageLoaded()");
        }
        lxeWebView.hideLoadingView();
        String title;
        title = view.getTitle();
        if (title.length() > 8) {
            title = title.substring(0, 8);
            title += "...";
        }
        lxeWebView.setTitle(title);

        if (view.canGoBack()) {
            lxeWebView.showHeaderBarPre();
        } else {
            lxeWebView.hideHeaderBarPre();
        }

        if (view.canGoForward()) {
            lxeWebView.showHeaderBarNext();
        } else {
            lxeWebView.hideHeaderBarNext();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            view.evaluateJavascript(LXENativeCallHtmlJavascriptProxy.canGoBack(), new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String s) {
                    lxeWebView.setCurrentHtmlPageCanGoBack(Boolean.valueOf(s));
                }
            });
        }
    }

    @Override
    public void onLoadResource(WebView view, String url) {
        LogUtil.d(TAG, "onLoadResource url:", url);
        super.onLoadResource(view, url);
    }

    @Override
    public void onPageCommitVisible(WebView view, String url) {
        LogUtil.d(TAG, "onPageCommitVisible url:", url);
        super.onPageCommitVisible(view, url);
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, String urlString) {
        LogUtil.d(TAG, "shouldInterceptRequest url:", urlString);
        if (!urlString.contains(".html")) {
            return super.shouldInterceptRequest(view, urlString);
        }
        return null;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, final WebResourceRequest request) {
        LogUtil.d(TAG, "shouldInterceptRequest url:", request.getUrl().toString());
        Set<String> keys = request.getRequestHeaders().keySet();
        Iterator<String> iterator = keys.iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            LogUtil.d(TAG, "shouldInterceptRequest header-", key, ":", request.getRequestHeaders().get(key));
        }

        return super.shouldInterceptRequest(view, request);
    }

    @Override
    public void onTooManyRedirects(WebView view, Message cancelMsg, Message continueMsg) {
        LogUtil.d(TAG, "onTooManyRedirects cancelMsg:", String.valueOf(cancelMsg), ",continueMsg:", String.valueOf(continueMsg));
        super.onTooManyRedirects(view, cancelMsg, continueMsg);
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        LogUtil.d(TAG, "onReceivedError errorCode:", String.valueOf(errorCode), ",description:", description, ",failingUrl:", failingUrl);
        super.onReceivedError(view, errorCode, description, failingUrl);
    }

    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        LogUtil.d(TAG, "onReceivedError,url:" + view.getUrl());
        lxeWebView.setUrl(view.getUrl());
        if (error != null) {
            lxeWebView.showErrorView();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
        LogUtil.d(TAG, "onReceivedHttpError url:", request.getUrl().toString(), ",errorResponse:", errorResponse.getReasonPhrase());
        super.onReceivedHttpError(view, request, errorResponse);
    }

    @Override
    public void onFormResubmission(WebView view, Message dontResend, Message resend) {
        LogUtil.d(TAG, "onFormResubmission dontResend:", String.valueOf(dontResend.obj), ",message:", String.valueOf(resend.obj));
        super.onFormResubmission(view, dontResend, resend);
    }

    @Override
    public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
        LogUtil.d(TAG, "doUpdateVisitedHistory url:", url, ",isReload:", String.valueOf(isReload));
        super.doUpdateVisitedHistory(view, url, isReload);
    }

    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        LogUtil.d(TAG, "shouldInterceptRequest url:", error.getUrl());
        super.onReceivedSslError(view, handler, error);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onReceivedClientCertRequest(WebView view, ClientCertRequest request) {
        LogUtil.d(TAG, "onReceivedClientCertRequest host:", request.getHost());
        super.onReceivedClientCertRequest(view, request);
    }

    @Override
    public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
        LogUtil.d(TAG, "onReceivedHttpAuthRequest host:", host, ",realm:", realm);
        super.onReceivedHttpAuthRequest(view, handler, host, realm);
    }

    @Override
    public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
        LogUtil.d(TAG, "shouldOverrideKeyEvent event:", event.toString());
        return super.shouldOverrideKeyEvent(view, event);
    }

    @Override
    public void onUnhandledKeyEvent(WebView view, KeyEvent event) {
        LogUtil.d(TAG, "onUnhandledKeyEvent event:", event.toString());
        super.onUnhandledKeyEvent(view, event);
    }

    @Override
    public void onScaleChanged(WebView view, float oldScale, float newScale) {
        LogUtil.d(TAG, "onScaleChanged oldScale:", String.valueOf(oldScale), ",newScale:", String.valueOf(newScale));
        super.onScaleChanged(view, oldScale, newScale);
    }

    @Override
    public void onReceivedLoginRequest(WebView view, String realm, String account, String args) {
        LogUtil.d(TAG, "onReceivedLoginRequest realm:", realm, ",account:", account, ",args:", args);
        super.onReceivedLoginRequest(view, realm, account, args);
    }

    @Override
    public boolean onRenderProcessGone(WebView view, RenderProcessGoneDetail detail) {
        LogUtil.d(TAG, "onRenderProcessGone detail:", detail.toString());
        return super.onRenderProcessGone(view, detail);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onSafeBrowsingHit(WebView view, WebResourceRequest request, int threatType, SafeBrowsingResponse callback) {
        LogUtil.d(TAG, "onSafeBrowsingHit url:", request.getUrl().toString());
        super.onSafeBrowsingHit(view, request, threatType, callback);
    }

}