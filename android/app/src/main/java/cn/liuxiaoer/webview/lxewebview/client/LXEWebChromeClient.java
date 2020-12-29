package cn.liuxiaoer.webview.lxewebview.client;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.GeolocationPermissions;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.FrameLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.liuxiaoer.R;
import cn.liuxiaoer.webview.lxewebview.activity.LXEWebViewActivity;
import cn.liuxiaoer.webview.lxewebview.LXEWebView;

public class LXEWebChromeClient extends WebChromeClient {
    private static final String TAG = LXEWebChromeClient.class.getSimpleName();
    private final LXEWebViewActivity activity;
    private View mVideoView = null;
    private CustomViewCallback mCustomViewCallback = null;

    private boolean isVideoFullscreen;

    public static final int FILE_CHOOSER_RESULT_CODE = 5173;


    LXEWebView lxeWebView;

    private final FrameLayout.LayoutParams FULLSCREEN_LAYOUT_PARAMS = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER);

    public LXEWebChromeClient(LXEWebViewActivity activity, LXEWebView lxeWebView) {
        this.activity = activity;
        this.lxeWebView = lxeWebView;
    }

    @Override
    public void onShowCustomView(View view, CustomViewCallback callback) {
        if (mCustomViewCallback != null) {
            mCustomViewCallback.onCustomViewHidden();
            return;
        }

        mVideoView = view;
        mCustomViewCallback = callback;

        view.setBackgroundColor(Color.BLACK);
//        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getRootView().addView(mVideoView, FULLSCREEN_LAYOUT_PARAMS);

        isVideoFullscreen = true;
    }

    public void onHideCustomView() {
        if (mVideoView != null) {
            if (mCustomViewCallback != null) {
                mCustomViewCallback.onCustomViewHidden();
                mCustomViewCallback = null;
            }

//            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//            activity.getContentView().getRootView().setVisibility(View.VISIBLE);
            mVideoView.setVisibility(View.GONE);

            // Remove the custom view from its container.
            getRootView().removeView(mVideoView);
            mVideoView = null;
            isVideoFullscreen = false;
        }
    }


    // 处理javascript中的alert
    public boolean onJsAlert(WebView view, String url, String message,
                             final JsResult result) {
        return false;
    }

    // 处理javascript中的confirm
    public boolean onJsConfirm(WebView view, String url,
                               String message, final JsResult result) {
        return true;
    }

    // 处理定位权限请求
    @Override
    public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
        callback.invoke(origin, true, false);
        super.onGeolocationPermissionsShowPrompt(origin, callback);
    }

    @Override
    // 设置网页加载的进度条
    public void onProgressChanged(WebView view, int newProgress) {
        boolean validate = validateAnimate(view.getUrl());
        lxeWebView.setProgressBarVisibility(View.VISIBLE);
        lxeWebView.setProgressBarProgress(newProgress);
        if (newProgress < 0.1) {
            lxeWebView.setVisibility(View.GONE);
        } else {
            lxeWebView.setVisibility(View.VISIBLE);
        }
        if (newProgress == 100) {
            lxeWebView.setProgressBarVisibility(View.GONE);
        }

        super.onProgressChanged(view, newProgress);
    }

    // 设置应用程序的标题title
    public void onReceivedTitle(WebView view, String title) {
        super.onReceivedTitle(view, title);
        // android 6.0 以下通过title获取
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            if (title.contains("404") || title.contains("500") || title.contains("Error")) {
                lxeWebView.showErrorView();
            }
        }
        lxeWebView.setTitle(title);

    }

    //<3.0
    public void openFileChooser(ValueCallback<Uri> uploadMsg) {
        activity.setUploadFile(uploadMsg);
        selectImage();
    }

    //>3.0+
    public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
        activity.setUploadFile(uploadMsg);
        selectImage();
    }

    //>4.1.1
    public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
        activity.setUploadFile(uploadMsg);
        selectImage();
    }

    // 5.0+
    @Override
    public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
        activity.setUploadFiles(filePathCallback);
        selectImage();
        return true;
    }

    private ViewGroup getRootView() {
        return ((ViewGroup) lxeWebView.findViewById(android.R.id.content));
    }

    /**
     * Notifies the class that the back key has been pressed by the user. This must
     * be called from the Activity's onBackPressed(), and if it returns false, the
     * activity itself should handle it. Otherwise don't do anything.
     *
     * @return Returns true if the event was handled, and false if was not (video
     * view is not visible)
     */
    public boolean onBackPressed() {
        if (isVideoFullscreen) {
            onHideCustomView();
            return true;
        } else {
            return false;
        }
    }

    public boolean validateAnimate(String url) {
        String[] urls = {"mobile/webapp/index.html", "mobile/webapp/contact.html", "mobile/webapp/application.html", "mobile/webapp/mine.html"};
        for (int i = 0; i < urls.length; i++) {
            if (url.contains(urls[i])) {
                return false;
            }
        }
        return true;
    }

    protected final void selectImage() {
        String[] selectPicTypeStr = {"相机", "文件/图片"};
        AlertDialog alertDialog = new AlertDialog.Builder(activity)
                .setItems(selectPicTypeStr,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                switch (which) {
                                    // 相机拍摄
                                    case 0:
                                        activity.openCarcme();
                                        break;
                                    // 手机相册
                                    case 1:
                                        activity.openFileChooseProcess();
                                        break;
                                    default:
                                        break;
                                }
                            }
                        }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        if (activity.getUploadFiles() != null) {
                            Uri[] uris = new Uri[1];
                            uris[0] = Uri.parse("");
                            activity.getUploadFiles().onReceiveValue(uris);
                            activity.setUploadFiles(null);
                        } else {
                            activity.getUploadFile().onReceiveValue(Uri.parse(""));
                            activity.setUploadFile(null);
                        }
                    }
                }).show();
    }


}