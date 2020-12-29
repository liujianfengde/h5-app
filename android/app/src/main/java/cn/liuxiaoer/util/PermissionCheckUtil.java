package cn.liuxiaoer.util;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

public class PermissionCheckUtil {

    public static final int REQUEST_EXTERNAL_APP = 0x0000;
    public static final int REQUEST_EXTERNAL_STORAGE = 0x0001;
    public static final int REQUEST_EXTERNAL_INSTALL = 0x0002;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private static String[] PERMISSIONS_LOCATION = {
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    private static String[] PERMISSIONS_CAMERA = {
            Manifest.permission.CAMERA
    };

    private static String[] PERMISSIONS_ALL = {
            Manifest.permission.CAMERA
    };
    private static String[] PERMISSIONS_REQUEST_INSTALL_PACKAGES = {
            Manifest.permission.REQUEST_INSTALL_PACKAGES
    };

    public static void verifyStoragePermissions(final Activity activity) {
        verifyPermissions(activity, PERMISSIONS_STORAGE);
    }


    public static void verifyLocationPermissions(final Activity activity) {
        verifyPermissions(activity, PERMISSIONS_LOCATION);
    }

    public static void verifyCameraPermissions(final Activity activity) {
        verifyPermissions(activity, PERMISSIONS_CAMERA);
    }

    public static void verifyAppPermissions(final Activity activity) {
        verifyPermissions(activity, PERMISSIONS_ALL);
    }


    public static void verifyPermissions(Activity activity, String[] permissions) {
        ActivityCompat.requestPermissions(activity, permissions, REQUEST_EXTERNAL_STORAGE);
    }

    public static boolean verifiedStoragePermissions(final Activity activity) {
        return allPermissionGranted(activity, PERMISSIONS_STORAGE);

    }

    public static boolean allPermissionGranted(Context context, String[] permissions) {
        if (permissions == null) return true;
        for (int i = 0; i < permissions.length; i++) {
            if (ActivityCompat.checkSelfPermission(context, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void verifyInstallPermission(Activity context) {
        Uri packageURI = Uri.parse("package:"+context.getPackageName());
        Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,packageURI);
        context.startActivityForResult(intent, REQUEST_EXTERNAL_INSTALL);
    }
}