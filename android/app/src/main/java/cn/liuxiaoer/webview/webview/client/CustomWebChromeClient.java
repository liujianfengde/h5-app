package cn.liuxiaoer.webview.webview.client;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
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
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cn.liuxiaoer.R;
import cn.liuxiaoer.webview.webview.CustomWebViewActivity;

public class CustomWebChromeClient extends WebChromeClient {
    private static final String TAG = CustomWebChromeClient.class.getSimpleName();
    private View mVideoView = null;
    private CustomViewCallback mCustomViewCallback = null;

    private boolean isVideoFullscreen;

    private CustomWebViewActivity activity;
    public static final int FILE_CHOOSER_RESULT_CODE = 5173;

    private ImageView imageView;

    private boolean animantionPlaying;

    private final FrameLayout.LayoutParams FULLSCREEN_LAYOUT_PARAMS = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER);

    public CustomWebChromeClient(CustomWebViewActivity activity, WebView webView, ProgressBar progressBar) {
        this.activity = activity;
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
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getRootView().addView(mVideoView, FULLSCREEN_LAYOUT_PARAMS);

        isVideoFullscreen = true;
    }

    public void onHideCustomView() {
        if (mVideoView != null) {
            if (mCustomViewCallback != null) {
                mCustomViewCallback.onCustomViewHidden();
                mCustomViewCallback = null;
            }

            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            activity.getContentView().getRootView().setVisibility(View.VISIBLE);
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
        activity.setProgressBarVisibility(View.VISIBLE);
        Log.e(TAG, "url is " + view.getUrl() + " oldProgress is " + activity.getProgressBarProgress());
        activity.setProgressBarProgress(newProgress);
        if (newProgress < 0.1) {
            activity.getContentView().setVisibility(View.GONE);
        } else {
            activity.getContentView().setVisibility(View.VISIBLE);
        }
        if (newProgress == 100) {
            Log.e(TAG, "url is " + view.getUrl() + " newProgress is " + newProgress);
            activity.setProgressBarVisibility(View.GONE);
            if (validate) {
                final Animation translateIn = AnimationUtils.loadAnimation(activity, R.anim.translate_in);
                translateIn.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        animantionPlaying = true;
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        animantionPlaying = false;
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });

                translateIn.setFillAfter(true);
                translateIn.setDuration(500);
                translateIn.setDetachWallpaper(true);

                if (animantionPlaying) translateIn.cancel();
                activity.getContentView().setAnimation(translateIn);
            }
        } else {
        }

        super.onProgressChanged(view, newProgress);
    }

    // 设置应用程序的标题title
    public void onReceivedTitle(WebView view, String title) {
        super.onReceivedTitle(view, title);
        // android 6.0 以下通过title获取
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            if (title.contains("404") || title.contains("500") || title.contains("Error")) {
                activity.showErrorView();
            }
        }
        activity.setTitle(title);

    }

    //region 附件选择配置
    //<3.0
    public void openFileChooser(ValueCallback<Uri> uploadMsg) {
        activity.setUploadMessage(uploadMsg);
        fileChooserActivityForResult();
    }

    //>3.0+
    public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
        activity.setUploadMessage(uploadMsg);
        fileChooserActivityForResult();
    }

    //>4.1.1
    public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
        activity.setUploadMessage(uploadMsg);
        fileChooserActivityForResult();
    }

    // 5.0+
    @Override
    public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
        activity.setUploadMessages(filePathCallback);
        fileChooserActivityForResult();
        return true;
    }

    private void fileChooserActivityForResult() {
        File imageStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "liuxiaoer");
        if (!imageStorageDir.exists()) {
            imageStorageDir.mkdirs();
        }
        File file = new File(imageStorageDir + File.separator + "IMG_" + String.valueOf(System.currentTimeMillis()) + ".jpg");
        Uri imageUri = Uri.fromFile(file);
        activity.setImageUri(imageUri);
        final List<Intent> cameraIntents = new ArrayList<Intent>();
        final Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        final PackageManager packageManager = activity.getPackageManager();
        final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        for (ResolveInfo res : listCam) {
            final String packageName = res.activityInfo.packageName;
            final Intent i = new Intent(captureIntent);
            i.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            i.setPackage(packageName);
            i.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            cameraIntents.add(i);
        }

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        Intent chooserIntent = Intent.createChooser(intent, "图片选择");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[]{}));
        activity.startActivityForResult(chooserIntent, FILE_CHOOSER_RESULT_CODE);
    }

    private ViewGroup getRootView() {
        return ((ViewGroup) activity.findViewById(android.R.id.content));
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
}