package cn.liuxiaoer.net.subscriber;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.view.View;

import cn.liuxiaoer.net.VersionModel;
import cn.liuxiaoer.util.AlertDialogTools;
import cn.liuxiaoer.util.PermissionCheckUtil;
import cn.liuxiaoer.webview.lxewebview.activity.LXEWebViewActivity;
import cn.liuxiaoer.webview.webview.downloader.DownLoadService;

import static cn.liuxiaoer.util.PermissionCheckUtil.verifiedStoragePermissions;
import static cn.liuxiaoer.util.PermissionCheckUtil.verifyStoragePermissions;

public class CheckupVersionSubscriber extends BaseSubscriber<VersionModel> {

    private LXEWebViewActivity activity;
    Dialog dialog;

    public CheckupVersionSubscriber(Context mContext) {
        super(mContext);
    }


    public CheckupVersionSubscriber(LXEWebViewActivity activity) {
        super(activity);
        this.activity = activity;
    }

    @Override
    public void onNext(final VersionModel result) {
        super.onNext(result);
//        if (result.needUpdate()) {
//            //检测是否有写文件权限
//            dialog = new AlertDialogTools().normalDialog(activity, "检测到新版本，请点击确定更新" + "\n更新内容：\n" + result.getUpdateMessage(), new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    activity.setNewAppUlr(result.getUrl());
//                    if (verifiedStoragePermissions(activity)) {
//                        activity.update(result.getUrl());
//                        dialog.dismiss();
//                    } else {
//                        verifyStoragePermissions(activity);
//                    }
//                }
//            });
//        }
    }

    @Override
    public void onError(Throwable e) {
        super.onError(e);
    }
}
