package cn.liuxiaoer.webview.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.cameraview.AspectRatio;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.zxing.Result;

import org.reactnative.camera.RNCameraView;
import org.reactnative.camera.tasks.BarCodeScannerAsyncTaskDelegate;

import java.util.ArrayList;
import java.util.List;

public class QRCodeActivity extends Activity implements BarCodeScannerAsyncTaskDelegate {
    private static final String TAG = QRCodeActivity.class.getSimpleName();
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 10;
    private static final int SETTING_REQUEST_CODE = 10;
    private RNCameraView cameraView;
    private static final String PACKAGE_URL_SCHEME = "package:";
    private boolean isFirstTime = true;//是不是第一次设置权限

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        cameraView = new RNCameraView(this);


        cameraView.setBarCodeScannerAsyncTaskDelegate(this);

        List<String> barCodeTypes = new ArrayList<>();
        barCodeTypes.add("QR_CODE");

        cameraView.setBarCodeTypes(barCodeTypes);


        cameraView.setGoogleVisionBarcodeType(Barcode.QR_CODE);
        cameraView.setAspectRatio(AspectRatio.of(1, 1));
        cameraView.setShouldScanBarCodes(true);
        cameraView.setAutoFocus(true);
        setContentView(cameraView);

        requestPermission();
    }

    @Override
    public void onBarCodeRead(Result barCode, int width, int height) {
        cameraView.stop();
        Intent intent = new Intent();
        String barCodeType = barCode.getBarcodeFormat().toString();
        intent.putExtra("type", barCodeType);
        intent.putExtra("text", barCode.getText());
        setResult(RESULT_OK, intent);
        onActivityResult(100, RESULT_OK, intent);

        finish();
    }

    @Override
    public void onBarCodeScanningTaskCompleted() {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        int gr = 0;
        for (int i = 0; i < grantResults.length; i++) {
            gr += grantResults[i];
        }
        boolean allGranted = gr == 0;
        if (allGranted) {
            if (!cameraView.isCameraOpened()) {
                cameraView.start();
            }
        } else {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                showDialog();
            }else {
                requestPermission();
            }
        }
    }

    private void showDialog() {
        AlertDialog dialog = new AlertDialog.Builder(this).setTitle("没有相机使用权限或存储权限无法使用扫描二维码").setMessage("可以前往设置->应用->opt协同办公->权限打开").setPositiveButton("去设置", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                startAppSettings();
            }
        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        }).show();
    }

    // 启动应用的设置
    private void startAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse(PACKAGE_URL_SCHEME + getPackageName()));
        startActivityForResult(intent, SETTING_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SETTING_REQUEST_CODE) {
            requestPermission();
        }
    }

    //第一次请求权限时ActivityCompat.shouldShowRequestPermissionRationale=false;
    //第一次请求权限被禁止，但未选择【不再提醒】ActivityCompat.shouldShowRequestPermissionRationale=true;
    //允许某权限后ActivityCompat.shouldShowRequestPermissionRationale=false;
    //禁止权限，并选中【禁止后不再询问】ActivityCompat.shouldShowRequestPermissionRationale=false；
    private void requestPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
        } else {
            if (!cameraView.isCameraOpened()) {
                cameraView.start();
            }
        }
    }
}
