package cn.liuxiaoer.webview.lxewebview;

import android.net.Uri;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
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
import java.util.Map;

import cn.liuxiaoer.R;
import cn.liuxiaoer.webview.lxewebview.activity.LXEWebViewActivity;
import cn.liuxiaoer.webview.lxewebview.client.LXEWebChromeClient;
import cn.liuxiaoer.webview.lxewebview.client.LXEWebViewClient;
import cn.liuxiaoer.webview.lxewebview.proxy.LXEHtmlJavascriptCallNativeProxy;
import cn.liuxiaoer.webview.webview.downloader.CustomWebViewDownloadListener;
import cn.liuxiaoer.webview.webview.util.CookieUtil;
import cn.liuxiaoer.webview.webview.util.LogUtil;

import static cn.liuxiaoer.webview.webview.util.SystemSetting.wifiSetting;

public class LXEWebView extends LinearLayout implements View.OnClickListener {

    private static final String TAG = LXEWebView.class.getSimpleName();
    private final LXEWebViewActivity context;

    private String groupId;
    private static final String PROTOCOL_HTTP = "http://";
    private static final String PROTOCOL_HTTPS = "https://";
    private static final String JAVASCRIPT_PROXY_NAME = "CustomJSBridge";

    LXEWebChromeClient customWebChromeClient;
    private LXEHtmlJavascriptCallNativeProxy jsProxy;
    //网络状态监控

    private View content;

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


    //记录h5界面能否再返回，如果不能返回上一个界面，点击两次返回按键退出系统
    private Boolean currentHtmlPageCanGoBack = true;
    //声明LocationClient类
    LocationClient mLocationClient;

    private Map<String, String> headers;
    private Map<String, String> params;


    public LXEWebView(LXEWebViewActivity context) {
        super(context);
        this.context = context;
        init();
    }

    public LXEWebView(LXEWebViewActivity activity, String groupId) {
        this(activity);
        this.groupId = groupId;
    }

    public void init() {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View lxeWebView = layoutInflater.inflate(R.layout.lxe_web_view, this);
        initView(lxeWebView);
        bindEvent();
    }

    private void initView(View root) {
        content = root.findViewById(R.id.content);
        headerBarView = root.findViewById(R.id.header_bar);
        headerBarBack = root.findViewById(R.id.header_bar_back);
        headerBarPre = root.findViewById(R.id.header_bar_pre);
        headerBarTitle = root.findViewById(R.id.header_bar_title);
        headerBarNext = root.findViewById(R.id.header_bar_next);
        headerBarRefresh = root.findViewById(R.id.header_bar_refresh);
        progressBarContainer = root.findViewById(R.id.web_view_progress_bar_layout);
        progressBar = root.findViewById(R.id.web_view_progress_bar);
        loadingView = root.findViewById(R.id.web_view_loading_view);
        errorView = root.findViewById(R.id.error_view);
        reloadBtn = root.findViewById(R.id.url_reload_btn);

        networkUnavailableView = root.findViewById(R.id.network_unavailable_view);
        goNetworkSettingBtn = root.findViewById(R.id.go_network_setting_btn);

        webView = (WebView) root.findViewById(R.id.web_view);
//        initBaiduLocationClient();

    }

    private void bindEvent() {
        headerBarBack.setOnClickListener(this);
        headerBarPre.setOnClickListener(this);
        headerBarNext.setOnClickListener(this);
        headerBarRefresh.setOnClickListener(this);
        reloadBtn.setOnClickListener(this);
        goNetworkSettingBtn.setOnClickListener(this);
    }

    private void initWebView() {

        setCookie(url);

        WebSettings webSettings = webView.getSettings();
        webView.setWebViewClient(new LXEWebViewClient(context, this));
        customWebChromeClient = new LXEWebChromeClient(context, this);
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
        jsProxy = new LXEHtmlJavascriptCallNativeProxy(context, this);
        webView.addJavascriptInterface(jsProxy, JAVASCRIPT_PROXY_NAME);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setUseWideViewPort(true);
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
//        if (savedInstanceState != null) {
//            webView.restoreState(savedInstanceState);
//        }


        webView.setDownloadListener(new CustomWebViewDownloadListener(getContext()));
    }

    private void setCookie(String url) {
        CookieSyncManager.createInstance(context);
        cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setCookie(url, CookieUtil.getParam(context).toString());
        CookieSyncManager.getInstance().sync();
    }

    private void initBaiduLocationClient() {
        mLocationClient = new LocationClient(getContext());
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

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.header_bar_back) {
            goBack();
        } else if (i == R.id.header_bar_refresh || i == R.id.header_bar_pre) {
            webView.reload();
        } else if (i == R.id.url_reload_btn) {
            showWebView();
        } else if (i == R.id.go_network_setting_btn) {
            wifiSetting(getContext());
        } else {
            LogUtil.d(TAG, "view clicked. id:", String.valueOf(view.getId()));
        }
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

    public void setUrl(String url) {
        this.url = url;
    }


    public void setCurrentHtmlPageCanGoBack(Boolean currentHtmlPageCanGoBack) {
        this.currentHtmlPageCanGoBack = currentHtmlPageCanGoBack;
    }

    public void setProgressBarVisibility(int visibility) {
        progressBarContainer.setVisibility(visibility);
    }

    public void setProgressBarProgress(int newProgress) {
        progressBar.setProgress(newProgress);
    }

    public void loadUrl(String url) {
        this.url = url;
        initWebView();
        webView.loadUrl(url);
    }

    public String getUrl() {
        return url;
    }

    public void goBack() {
        context.goBack(null);
    }

    public void goBackAndRefreshPrePage() {
        context.goBackAndRefreshPrePage();
    }

    public void reload() {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                webView.reload();
            }
        });
    }
}
