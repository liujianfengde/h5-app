package cn.liuxiaoer.webview.webview;


import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import cn.liuxiaoer.LxeApplication;
import cn.liuxiaoer.R;
import cn.liuxiaoer.webview.activity.BaseActivity;
import cn.liuxiaoer.webview.webview.client.CustomWebChromeClient;
import cn.liuxiaoer.webview.webview.client.CustomWebViewClient;
import cn.liuxiaoer.webview.webview.downloader.CustomWebViewDownloadListener;
import cn.liuxiaoer.webview.webview.proxy.CustomHtmlJavascriptCallNativeProxy;
import cn.liuxiaoer.webview.webview.proxy.CustomNativeCallHtmlJavascriptProxy;
import cn.liuxiaoer.webview.webview.util.CookieUtil;
import cn.liuxiaoer.webview.webview.util.LogUtil;
import cn.liuxiaoer.webview.webview.util.NetworkInfoUtil;

import static cn.liuxiaoer.webview.webview.client.CustomWebChromeClient.FILE_CHOOSER_RESULT_CODE;
import static cn.liuxiaoer.webview.webview.proxy.CustomNativeCallHtmlJavascriptProxy.qrCodeScanResult;
import static cn.liuxiaoer.webview.webview.util.SystemSetting.wifiSetting;


public class CustomWebViewActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = CustomWebViewActivity.class.getSimpleName();

    private static final String PROTOCOL_HTTP = "http://";
    private static final String PROTOCOL_HTTPS = "https://";
    private static final String JAVASCRIPT_PROXY_NAME = "CustomJSBridge";

    CustomWebChromeClient customWebChromeClient;
    private CustomHtmlJavascriptCallNativeProxy jsProxy;
    //网络状态监控

    //标头栏
    private View headerBarView; //标头栏
    private View headerBarBack;//返回控制视图
    private ImageView headerBarPre;//返回前一个网页控制视图
    private TextView headerBarTitle;//标题视图
    private ImageView headerBarNext;//进入下一个网页控制视图
    private ImageView headerBarRefresh;//刷新控制视图

    //进度条
    private View progressBarContainer;
    private ProgressBar progressBar;

    //错误视图
    private View errorView;
    private View reloadBtn;

    //加载视图
    private View loadingView;


    //网络不可用视图
    private View networkUnavailableView;
    private View goNetworkSettingBtn;

    private WebView webView;
    private String url;
    private CookieManager cookieManager;

    //上传文件表单数据存储
    private ValueCallback<Uri> mUploadMessage;// 表单的数据信息, for Android <5.0
    private ValueCallback<Uri[]> mUploadMessages; // 表单的数据信息, for Android 5.0+
    private Uri imageUri;


    //声明LocationClient类
    LocationClient mLocationClient;

    private Map<String, String> headers;
    private Map<String, String> params;
    //记录h5界面能否再返回，如果不能返回上一个界面，点击两次返回按键退出系统
    private Boolean currentHtmlPageCanGoBack = true;

    //点击两次返回按键退出应用，两次点击的时间间隔
    private static final int CLICK_BACK_2_TIME_INTERVAL = 2000;
    private long lastBackButtonClickedTime;
    private LinearLayout content;

    @TargetApi(19)
    private void setTranslucentStatus(boolean on) {
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

    public LinearLayout getContentView() {
        return content;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_web_view);

        Intent intent = getIntent();
        url = intent.getStringExtra("url");
        final String token = intent.getStringExtra("token");
        setHeaders(new HashMap<String, String>() {{
            put("token", token);
            put("tokenId", token);
        }});

        setParams(new HashMap<String, String>() {{
            put("token", token);
            put("tokenId", token);
        }});

        initView(savedInstanceState);
        bindEvent();
        loadPage(url);
    }

    private void initView(Bundle savedInstanceState) {
        content = findViewById(R.id.content);
        headerBarView = findViewById(R.id.header_bar);
        headerBarBack = findViewById(R.id.header_bar_back);
        headerBarPre = findViewById(R.id.header_bar_pre);
        headerBarTitle = findViewById(R.id.header_bar_title);
        headerBarNext = findViewById(R.id.header_bar_next);
        headerBarRefresh = findViewById(R.id.header_bar_refresh);
        progressBarContainer = findViewById(R.id.web_view_progress_bar_layout);
        progressBar = findViewById(R.id.web_view_progress_bar);
        loadingView = findViewById(R.id.web_view_loading_view);
        errorView = findViewById(R.id.error_view);
        reloadBtn = findViewById(R.id.url_reload_btn);

        networkUnavailableView = findViewById(R.id.network_unavailable_view);
        goNetworkSettingBtn = findViewById(R.id.go_network_setting_btn);

        webView = (WebView) findViewById(R.id.web_view);
        initWebView(savedInstanceState);
        initBaiduLocationClient();
    }

    private void bindEvent() {
        headerBarBack.setOnClickListener(this);
        headerBarPre.setOnClickListener(this);
        headerBarNext.setOnClickListener(this);
        headerBarRefresh.setOnClickListener(this);
        reloadBtn.setOnClickListener(this);
        goNetworkSettingBtn.setOnClickListener(this);
    }

    private String getUrl(String url) {
        int index = url.indexOf(PROTOCOL_HTTP);
        if (index == -1) {
            index = url.indexOf(PROTOCOL_HTTPS);
        }
        if (index == -1) {
            url = PROTOCOL_HTTP + url;
        }

        Map<String, String> params = getParams();

        Iterator<String> paramNameIterator = params.keySet().iterator();

        StringBuilder psb = new StringBuilder();
        while (paramNameIterator.hasNext()) {
            String paramName = paramNameIterator.next();
            psb.append("&").append(paramName).append("=").append(params.get(paramName));
        }

        if (url.contains("?")) {
            if (url.endsWith("&")) {
                url += psb.subSequence(1, psb.length());
            } else {
                url += psb.toString();
            }
        } else {
            url = url + "?" + psb.subSequence(1, psb.length());
        }
        return url;
    }

    private void loadPage(String url) {
        if (!NetworkInfoUtil.isAvailable(this)) {
            showNetworkNotAvailablePage();
            return;
        }
        LogUtil.d(TAG, "before loadPage url:", url);
        url = getUrl(url);

        webView.loadUrl(url, getHeaders());
        LogUtil.d(TAG, "after loadPage url:", url);
    }

    private Map<String, String> getHeaders() {
        return this.headers;
    }

    private void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    private void setParams(Map<String, String> params) {
        this.params = params;
    }

    private Map<String, String> getParams() {
        return this.params;
    }

    private void initWebView(Bundle savedInstanceState) {

        setCookie(url);

        WebSettings webSettings = webView.getSettings();
        webView.setWebViewClient(new CustomWebViewClient(this));
        customWebChromeClient = new CustomWebChromeClient(this, webView, progressBar);
        webView.setWebChromeClient(customWebChromeClient);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            webView.getSettings().setAllowUniversalAccessFromFileURLs(true);
        } else {
            try {
                Class<?> clazz = webView.getSettings().getClass();
                Method method = clazz.getMethod("setAllowUniversalAccessFromFileURLs", boolean.class);
                if (method != null) {
                    method.invoke(webView.getSettings(), true);
                }
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }


        webSettings.setJavaScriptEnabled(true);
        webSettings.setPluginState(WebSettings.PluginState.ON);
        webSettings.setUseWideViewPort(true); // 设置支持viewport
        webSettings.setAllowFileAccess(true); // 允许访问文件
        webSettings.setSupportZoom(true); // 支持缩放
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE); //
        webSettings.setDomStorageEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        jsProxy = new CustomHtmlJavascriptCallNativeProxy(CustomWebViewActivity.this, webView);
        webView.addJavascriptInterface(jsProxy, JAVASCRIPT_PROXY_NAME);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setUseWideViewPort(true);
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webView.evaluateJavascript(String.format("window.appVersion='%s'", LxeApplication.getPhoneInfo().getVersionName()), null);
        } else {
            webView.loadUrl(String.format("window.appVersion='%s'", LxeApplication.getPhoneInfo().getVersionName()));
        }

        webView.setDownloadListener(new CustomWebViewDownloadListener(this));
    }

    private void initBaiduLocationClient() {
        mLocationClient = new LocationClient(getApplicationContext());
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        int span = 1000;
//        option.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤GPS仿真结果，默认需要
        mLocationClient.setLocOption(option);
        //开启辅助H5定位：
        mLocationClient.start();
        mLocationClient.enableAssistantLocation(webView);
    }

    private void setCookie(String url) {
        CookieSyncManager.createInstance(webView.getContext());
        cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setCookie(url, CookieUtil.getParam(getApplicationContext()).toString());
        CookieSyncManager.getInstance().sync();
    }

    //显示浏览器
    public void showWebView() {
        errorView.setVisibility(View.GONE);
        networkUnavailableView.setVisibility(View.GONE);
        loadingView.setVisibility(View.VISIBLE);
        webView.setVisibility(View.VISIBLE);
        webView.loadUrl(url);
    }

    //显示错误页面
    public void showErrorView() {
        webView.setVisibility(View.GONE);
        networkUnavailableView.setVisibility(View.GONE);
        loadingView.setVisibility(View.GONE);
        errorView.setVisibility(View.VISIBLE);
    }

    //网络不可用页面
    public void showNetworkNotAvailablePage() {
        errorView.setVisibility(View.GONE);
        webView.setVisibility(View.GONE);
        loadingView.setVisibility(View.GONE);
        networkUnavailableView.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) {
            if (customWebChromeClient.onBackPressed()) return true;
            if (webView.canGoBack()) {
                webView.goBack();
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if (!currentHtmlPageCanGoBack) {
                if (lastBackButtonClickedTime + CLICK_BACK_2_TIME_INTERVAL > System.currentTimeMillis()) {
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_HOME);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    android.os.Process.killProcess(android.os.Process.myPid());
                    finish();
                } else {
                    Toast.makeText(this, "再点击一次返回退出程序", Toast.LENGTH_SHORT).show();
                    lastBackButtonClickedTime = System.currentTimeMillis();
                }
                return true;
            }
            if (customWebChromeClient.onBackPressed()) return true;
            if (webView.canGoBack()) {
                webView.goBack();
                return true;
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.header_bar_back) {
            if (webView.canGoBack()) {
                webView.goBack();
            } else {
                webView.loadUrl(CustomNativeCallHtmlJavascriptProxy.GO_BACK);
            }
        } else if (i == R.id.header_bar_pre) {
            webView.goBack();
        } else if (i == R.id.header_bar_next) {
            webView.goForward();
        } else if (i == R.id.header_bar_refresh) {
            webView.reload();
        } else if (i == R.id.url_reload_btn) {
            showWebView();
        } else if (i == R.id.go_network_setting_btn) {
            wifiSetting(this);
        } else {
            LogUtil.d(TAG, "view clicked. id:", String.valueOf(view.getId()));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            String type = data.getStringExtra("type");
            String text = data.getStringExtra("text");
            webView.loadUrl(qrCodeScanResult(text));
        }
        //选择文件处理
        if (requestCode == FILE_CHOOSER_RESULT_CODE) {
            if (null != mUploadMessage || null != mUploadMessages) {
                Uri result = data == null || resultCode != RESULT_OK ? null : data.getData();
                if (mUploadMessages != null) {
                    onActivityResultUploadMessages(requestCode, resultCode, data);
                } else if (mUploadMessage != null) {
                    if (result == null) {
                        mUploadMessage.onReceiveValue(null);
                        mUploadMessage = null;
                    } else {
                        mUploadMessage.onReceiveValue(result);
                        mUploadMessage = null;
                    }
                }
            }
        }
    }

    /**
     * 上传图片 for Android < 5.0+
     *
     * @param requestCode requestCode
     * @param resultCode  resultCode
     * @param data        data
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void onActivityResultUploadMessages(int requestCode, int resultCode, Intent data) {
        if (requestCode != FILE_CHOOSER_RESULT_CODE || mUploadMessages == null) {
            return;
        }
        Uri[] results = null;
        if (resultCode == Activity.RESULT_OK) {
            if (data == null) {
                results = new Uri[]{imageUri};
            } else {
                String dataString = data.getDataString();  //    content://com.android.providers.downloads.documents/document/1
                ClipData clipData = data.getClipData();
                if (clipData != null) {
                    results = new Uri[clipData.getItemCount()];
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        ClipData.Item item = clipData.getItemAt(i);
                        results[i] = item.getUri();
                    }
                }
                if (dataString != null)
                    results = new Uri[]{Uri.parse(dataString)};
            }
        }
        if (results != null) {
            mUploadMessages.onReceiveValue(results);
            mUploadMessages = null;
        } else {
//            results = new Uri[]{imageUri};
            mUploadMessages.onReceiveValue(results); // 没有选择时，result为null
            mUploadMessages = null;
        }
    }

    //设置显示标头栏
    public void setTitleBarVisible() {
        this.headerBarView.setVisibility(View.VISIBLE);
    }

    //设置不显示标头栏
    public void setTitleBarGone() {
        this.headerBarView.setVisibility(View.GONE);
    }

    //设置标题
    public void setTitle(String title) {
        this.headerBarTitle.setText(title);
    }

    //显示加载视图
    public void showLoadingView() {
        this.loadingView.setVisibility(View.VISIBLE);
    }

    //关闭加载视图
    public void hideLoadingView() {
        this.loadingView.setVisibility(View.GONE);
    }

    //显示返回前一个网页控制视图
    public void showHeaderBarPre() {

    }

    //隐藏返回前一个网页控制视图
    public void hideHeaderBarPre() {

    }

    //显示进入下一个网页控制视图
    public void showHeaderBarNext() {

    }

    //隐藏进入下一个网页控制视图
    public void hideHeaderBarNext() {

    }

    public void setUploadMessage(ValueCallback<Uri> uploadMessage) {
        this.mUploadMessage = uploadMessage;
    }

    public void setUploadMessages(ValueCallback<Uri[]> uploadMessages) {
        this.mUploadMessages = uploadMessages;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setImageUri(Uri imageUri) {
        this.imageUri = imageUri;
    }

    //跳转到登录界面
    public void toLogin() {

    }

    public void setCurrentHtmlPageCanGoBack(Boolean currentHtmlPageCanGoBack) {
        this.currentHtmlPageCanGoBack = currentHtmlPageCanGoBack;
    }

    public int getProgressBarProgress(){
        return progressBar.getProgress();
    }

    public void setProgressBarProgress(int progress) {
        progressBar.setProgress(progress);
    }

    public void setProgressBarVisibility(int visibility) {
        progressBarContainer.setVisibility(visibility);
        progressBar.setVisibility(visibility);
    }
}




