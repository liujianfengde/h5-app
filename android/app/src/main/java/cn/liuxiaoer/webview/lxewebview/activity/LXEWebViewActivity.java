package cn.liuxiaoer.webview.lxewebview.activity;

import android.app.Dialog;
import android.app.DownloadManager;
import android.content.ContentValues;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.ValueCallback;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import cn.liuxiaoer.R;
import cn.liuxiaoer.net.subscriber.CheckupVersionSubscriber;
import cn.liuxiaoer.util.AlertDialogTools;
import cn.liuxiaoer.util.ApiManager;
import cn.liuxiaoer.util.PermissionCheckUtil;
import cn.liuxiaoer.webview.NewAppDownloadedReceiver;
import cn.liuxiaoer.webview.activity.BaseActivity;
import cn.liuxiaoer.webview.lxewebview.LXEWebView;
import cn.liuxiaoer.webview.webview.downloader.DownLoadService;

public class LXEWebViewActivity extends BaseActivity {
    static final String TAG = LXEWebViewActivity.class.getSimpleName();

    private FrameLayout content;

    //key:分组ID(名),value:组成员
    Map<String, Stack<LXEWebView>> webViewCache;

    String currentGroup;
    private String newAppUrl;
    private NewAppDownloadedReceiver receiver;

    private ValueCallback<Uri[]> uploadFiles;
    private ValueCallback<Uri> uploadFile;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        webViewCache = new HashMap<>();

        LayoutInflater inflater = LayoutInflater.from(this);
        content = (FrameLayout) inflater.inflate(R.layout.lxe_web_view_activity, null);
        setContentView(content);

        receiver = new NewAppDownloadedReceiver(this);
        registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        Intent data = getIntent();
        String url = data.getStringExtra("url");
        loadUrl(url);
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkupVersion();
    }

    public void checkupVersion() {
        ApiManager.getInstance().checkupVersion(new CheckupVersionSubscriber(this));
    }

    public void loadUrl(String url) {
        String groupId = parseGroupId(url);

        if (groupId != null && !"".equals(groupId) && !"auth".equals(groupId)) {//第一级界面
            if (groupId.equals(currentGroup)) {
                return;//与当前url一样
            } else {
                Stack<LXEWebView> ws = webViewCache.get(groupId);
                if (ws != null && ws.size() == 1) {
                    currentGroup = groupId;
                    LXEWebView current = ws.get(0);
                    if (content.indexOfChild(current) == -1) {
                        content.addView(current);
                    }
                    content.bringChildToFront(current);
                    return;//与当前url一样
                }
            }
            currentGroup = groupId;
        } else {
            groupId = currentGroup;
        }
        Stack<LXEWebView> ws = webViewCache.get(groupId);
        if (ws == null) {
            ws = new Stack<>();
        }

        LXEWebView webView = createWebView(groupId);
        webView.loadUrl(url);
        ws.add(webView);
        webViewCache.put(groupId, ws);
        //添加新的VebView
        content.addView(webView);

        if (webViewCache.get(groupId).size() > 1) {
            final Animation translateIn = AnimationUtils.loadAnimation(this, R.anim.translate_in);

            translateIn.setFillAfter(true);
            translateIn.setDuration(500);
            translateIn.setDetachWallpaper(true);

            webView.setAnimation(translateIn);
        }
    }

    public void goBack(String groupId) {
        if (groupId == null) {
            groupId = currentGroup;
        }
        Stack<LXEWebView> ws = webViewCache.get(groupId);
        if (ws == null || ws.size() == 0) return;
        if (!"auth".equals(groupId) && ws.size() == 1) {
            return;
        }
        //动画
        //动画之后移除WebView
        final LXEWebView webView = ws.pop();
        final Animation translateOut = AnimationUtils.loadAnimation(this, R.anim.translate_out);

        translateOut.setFillAfter(true);
        translateOut.setDuration(500);
        translateOut.setDetachWallpaper(true);

        webView.setAnimation(translateOut);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                content.removeView(webView);
            }
        });
    }

    public void goBackAndRefreshPrePage() {
        Stack<LXEWebView> ws = webViewCache.get(currentGroup);
        if (ws == null || ws.size() == 0) return;
        if (!"auth".equals(currentGroup) && ws.size() == 1) {
            return;
        }
        //动画
        //动画之后移除WebView
        final LXEWebView webView = ws.pop();
        final Animation translateOut = AnimationUtils.loadAnimation(this, R.anim.translate_out);

        translateOut.setFillAfter(true);
        translateOut.setDuration(500);
        translateOut.setDetachWallpaper(true);

        webView.setAnimation(translateOut);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                content.removeView(webView);
            }
        });

        ws = webViewCache.get(currentGroup);
        if (ws != null && ws.size() != 0) {
            ws.get(0).reload();
        }
    }

    private String parseGroupId(String url) {
        if (url.contains("mobile/webapp/login.html?tokenId")) {
            return "authed_login";
        } else if (url.contains("login.html")) {
            clearWebViewCache();
            return "login";
        } else if (url.contains("index.html")) {
            return "index";
        } else if (url.contains("contact.html")) {
            return "contact";
        } else if (url.contains("application.html")) {
            return "application";
        } else if (url.contains("newbuild.html")) {
            return "newbuild";
        } else if (url.contains("mine.html")) {
            return "mine";
        } else if (url.contains("oauth2/authorize")) {
            return "auth";
        } else if (url.contains("app/oauth/callbackMobile.jsp")) {
            return "auth_callback";
        }
        return null;
    }

    //启动下载
    public void update(String url) {
        Intent updateIntent = new Intent(this, DownLoadService.class);
        updateIntent.putExtra("downLoadUrl", url);
        startService(updateIntent);
    }

    @Override
    public void onBackPressed() {
        goBack(currentGroup);
    }

    private LXEWebView createWebView(String groupId) {
        LXEWebView webView = new LXEWebView(this, groupId);
        return webView;
    }

    public void clearWebViewCache() {
        webViewCache = new HashMap<>();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermissionCheckUtil.REQUEST_EXTERNAL_STORAGE) { //存储权限
            if (!PermissionCheckUtil.verifiedStoragePermissions(this)) {
                final Dialog openSettingDialog = new AlertDialogTools().normalDialog(this, "需要手机储存权限，否则无法更新！", "去开启", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent localIntent = new Intent();
                        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        if (Build.VERSION.SDK_INT >= 9) {
                            localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                            localIntent.setData(Uri.fromParts("package", getPackageName(), null));
                        } else if (Build.VERSION.SDK_INT <= 8) {
                            localIntent.setAction(Intent.ACTION_VIEW);
                            localIntent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
                            localIntent.putExtra("com.android.settings.ApplicationPkgName", getPackageName());
                        }
                        startActivity(localIntent);
                    }
                });
            } else {
                if (newAppUrl != null && !"".equals(newAppUrl)) {
                    update(newAppUrl);
                }
            }
        } else if (requestCode == PermissionCheckUtil.REQUEST_EXTERNAL_INSTALL) {//安装权限
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!getPackageManager().canRequestPackageInstalls()) {//手机开启权限
                    final Dialog openSettingDialog = new AlertDialogTools().normalDialog(this, "需要应用安装权限，否则无法安装！", "去开启", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Uri packageURI = Uri.parse("package:" + getPackageName());
                            Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, packageURI);
                            startActivityForResult(intent, PermissionCheckUtil.REQUEST_EXTERNAL_INSTALL);
                            startActivity(intent);
                        }
                    });
                } else {
                    NewAppDownloadedReceiver.install(LXEWebViewActivity.this);
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PermissionCheckUtil.REQUEST_EXTERNAL_STORAGE) {
            if (!PermissionCheckUtil.verifiedStoragePermissions(this)) {
                final Dialog openSettingDialog = new AlertDialogTools().normalDialog(this, "需要手机储存权限，否则无法更新！", "去开启", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent localIntent = new Intent();
                        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        if (Build.VERSION.SDK_INT >= 9) {
                            localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                            localIntent.setData(Uri.fromParts("package", getPackageName(), null));
                        } else if (Build.VERSION.SDK_INT <= 8) {
                            localIntent.setAction(Intent.ACTION_VIEW);
                            localIntent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
                            localIntent.putExtra("com.android.settings.ApplicationPkgName", getPackageName());
                        }
                        startActivity(localIntent);
                    }
                });
            }
        } else if (requestCode == PermissionCheckUtil.REQUEST_EXTERNAL_INSTALL) {//安装权限
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!getPackageManager().canRequestPackageInstalls()) {//手机开启权限
                    final Dialog openSettingDialog = new AlertDialogTools().normalDialog(this, "需要应用安装权限，否则无法安装！", "去开启", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Uri packageURI = Uri.parse("package:" + getPackageName());
                            Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, packageURI);
                            startActivityForResult(intent, PermissionCheckUtil.REQUEST_EXTERNAL_INSTALL);
                            startActivity(intent);
                        }
                    });
                } else {
                    NewAppDownloadedReceiver.install(LXEWebViewActivity.this);
                }
            }
        }
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case 0:
                    if (null != uploadFile) {
                        Uri result = data == null || resultCode != RESULT_OK ? null
                                : data.getData();
                        uploadFile.onReceiveValue(result);
                        uploadFile = null;
                    }
                    if (null != uploadFiles) {
                        Uri result = data == null || resultCode != RESULT_OK ? null
                                : data.getData();
                        uploadFiles.onReceiveValue(new Uri[]{result});
                        uploadFiles = null;
                    }
                    break;
                case 1:
                    if (uploadFiles == null) {
                        return;
                    }
                    afterOpenCamera();
                    Uri[] uris = new Uri[1];
                    uris[0] = cameraUri;
                    uploadFiles.onReceiveValue(uris);
                    break;
                default:
                    break;
            }
        } else if (resultCode == RESULT_CANCELED) {
            if (null != uploadFile) {
                uploadFile.onReceiveValue(null);
                uploadFile = null;
            }
            if (null != uploadFiles) {
                uploadFiles.onReceiveValue(null);
                uploadFiles = null;
            }

        }
    }

    public void setNewAppUlr(String url) {
        this.newAppUrl = url;
    }

    /**
     * 打开照相机
     */
    Uri cameraUri;

    public void openCarcme() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // 必须确保文件夹路径存在，否则拍照后无法完成回调
        File vFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                , System.currentTimeMillis() + ".jpg");
        if (!vFile.exists()) {
            File vDirPath = vFile.getParentFile();
            vDirPath.mkdirs();
        } else {
            if (vFile.exists()) {
                vFile.delete();
            }
        }
        cameraUri = Uri.fromFile(vFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri);
        startActivityForResult(intent, 1);
    }

    /**
     * 拍照结束后
     */
    private void afterOpenCamera() {
        File f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                , (System.currentTimeMillis() + ".jpg"));
        addImageGallery(f);
    }

    /**
     * 解决拍照后在相册中找不到的问题
     */
    public void addImageGallery(File file) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }


    public void openFileChooseProcess() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("*/*");
        startActivityForResult(Intent.createChooser(i, "Chooser"), 0);

    }

    public ValueCallback<Uri[]> getUploadFiles() {
        return uploadFiles;
    }

    public void setUploadFiles(ValueCallback<Uri[]> uploadFiles) {
        this.uploadFiles = uploadFiles;
    }

    public void setUploadFile(ValueCallback<Uri> uploadFile) {
        this.uploadFile = uploadFile;
    }

    public ValueCallback<Uri> getUploadFile() {
        return uploadFile;
    }
}