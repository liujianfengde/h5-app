package cn.liuxiaoer.webview;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.view.View;

import androidx.core.content.FileProvider;

import java.io.File;

import cn.liuxiaoer.util.AlertDialogTools;
import cn.liuxiaoer.webview.lxewebview.activity.LXEWebViewActivity;
import cn.liuxiaoer.webview.webview.downloader.DownLoadService;

import static cn.liuxiaoer.util.PermissionCheckUtil.verifyInstallPermission;

public class NewAppDownloadedReceiver extends BroadcastReceiver {
    private LXEWebViewActivity activity;

    public NewAppDownloadedReceiver(LXEWebViewActivity activity) {
        this.activity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final Dialog openSettingDialog = new AlertDialogTools().normalDialog(activity, "新版本已经下载完成，是否安装！", "安装", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateAndInstall(activity);
            }
        });
    }

    /**
     * 通过隐式意图调用系统安装程序安装APK
     */
    public static void install(LXEWebViewActivity context) {
        File file = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                , DownLoadService.threadLocal.get());
        Intent intent = new Intent(Intent.ACTION_VIEW);
        // 由于没有在Activity环境下启动Activity,设置下面的标签
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 24) { //判读版本是否在7.0以上
            //参数1 上下文, 参数2 Provider主机地址 和配置文件中保持一致   参数3  共享的文件
            Uri apkUri = FileProvider.getUriForFile(context, "com.liuxiaoer.webview.fileprovider", file);
            //添加这一句表示对目标应用临时授权该Uri所代表的文件
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        }
        context.startActivity(intent);
    }

    /**
     * 8.0验证权限并调用系统安装程序安装APK
     */
    public void validateAndInstall(LXEWebViewActivity context) {
        //检测权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (context.getPackageManager().canRequestPackageInstalls()) {
                install(context);
            } else {
                verifyInstallPermission(context);
            }
        } else {
            install(context);
        }
    }
}