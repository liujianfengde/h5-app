package org.reactnative.camera.utils;

import android.view.Surface;

import com.google.zxing.BarcodeFormat;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CameraSetting {

    public static final Map<String, Map<String,Object>> constants;


    public static final int VIDEO_2160P = 0;
    public static final int VIDEO_1080P = 1;
    public static final int VIDEO_720P = 2;
    public static final int VIDEO_480P = 3;
    public static final int VIDEO_4x3 = 4;

    public static final int CAMERA_ASPECT_FILL = 0;
    public static final int CAMERA_ASPECT_FIT = 1;
    public static final int CAMERA_ASPECT_STRETCH = 2;
    public static final int CAMERA_CAPTURE_MODE_STILL = 0;
    public static final int CAMERA_CAPTURE_MODE_VIDEO = 1;
    public static final int CAMERA_CAPTURE_TARGET_MEMORY = 0;
    public static final int CAMERA_CAPTURE_TARGET_DISK = 1;
    public static final int CAMERA_CAPTURE_TARGET_CAMERA_ROLL = 2;
    public static final int CAMERA_CAPTURE_TARGET_TEMP = 3;
    public static final int CAMERA_ORIENTATION_AUTO = Integer.MAX_VALUE;
    public static final int CAMERA_ORIENTATION_PORTRAIT = Surface.ROTATION_0;
    public static final int CAMERA_ORIENTATION_PORTRAIT_UPSIDE_DOWN = Surface.ROTATION_180;
    public static final int CAMERA_ORIENTATION_LANDSCAPE_LEFT = Surface.ROTATION_90;
    public static final int CAMERA_ORIENTATION_LANDSCAPE_RIGHT = Surface.ROTATION_270;
    public static final int CAMERA_TYPE_FRONT = 1;
    public static final int CAMERA_TYPE_BACK = 2;
    public static final int CAMERA_FLASH_MODE_OFF = 0;
    public static final int CAMERA_FLASH_MODE_ON = 1;
    public static final int CAMERA_FLASH_MODE_AUTO = 2;
    public static final int CAMERA_TORCH_MODE_OFF = 0;
    public static final int CAMERA_TORCH_MODE_ON = 1;
    public static final int CAMERA_TORCH_MODE_AUTO = 2;
    public static final String CAMERA_CAPTURE_QUALITY_PREVIEW = "preview";
    public static final String CAMERA_CAPTURE_QUALITY_HIGH = "high";
    public static final String CAMERA_CAPTURE_QUALITY_MEDIUM = "medium";
    public static final String CAMERA_CAPTURE_QUALITY_LOW = "low";
    public static final String CAMERA_CAPTURE_QUALITY_1080P = "1080p";
    public static final String CAMERA_CAPTURE_QUALITY_720P = "720p";
    public static final String CAMERA_CAPTURE_QUALITY_480P = "480p";
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    static {
        constants = Collections.unmodifiableMap(new HashMap<String, Map<String,Object>>() {
            {
                put("Aspect", getAspectConstants());
                put("BarCodeType", getBarCodeConstants());
                put("Type", getTypeConstants());
                put("CaptureQuality", getCaptureQualityConstants());
                put("CaptureMode", getCaptureModeConstants());
                put("CaptureTarget", getCaptureTargetConstants());
                put("Orientation", getOrientationConstants());
                put("FlashMode", getFlashModeConstants());
                put("TorchMode", getTorchModeConstants());
            }

            private Map<String, Object> getAspectConstants() {
                return Collections.unmodifiableMap(new HashMap<String, Object>() {
                    {
                        put("stretch", CAMERA_ASPECT_STRETCH);
                        put("fit", CAMERA_ASPECT_FIT);
                        put("fill", CAMERA_ASPECT_FILL);
                    }
                });
            }

            private Map<String, Object> getBarCodeConstants() {
                return Collections.unmodifiableMap(new HashMap<String, Object>() {
                    {
                        // @TODO add barcode types
                    }
                });
            }

            private Map<String, Object> getTypeConstants() {
                return Collections.unmodifiableMap(new HashMap<String, Object>() {
                    {
                        put("front", CAMERA_TYPE_FRONT);
                        put("back", CAMERA_TYPE_BACK);
                    }
                });
            }

            private Map<String, Object> getCaptureQualityConstants() {
                return Collections.unmodifiableMap(new HashMap<String, Object>() {
                    {
                        put("low", CAMERA_CAPTURE_QUALITY_LOW);
                        put("medium", CAMERA_CAPTURE_QUALITY_MEDIUM);
                        put("high", CAMERA_CAPTURE_QUALITY_HIGH);
                        put("photo", CAMERA_CAPTURE_QUALITY_HIGH);
                        put("preview", CAMERA_CAPTURE_QUALITY_PREVIEW);
                        put("480p", CAMERA_CAPTURE_QUALITY_480P);
                        put("720p", CAMERA_CAPTURE_QUALITY_720P);
                        put("1080p", CAMERA_CAPTURE_QUALITY_1080P);
                    }
                });
            }

            private Map<String, Object> getCaptureModeConstants() {
                return Collections.unmodifiableMap(new HashMap<String, Object>() {
                    {
                        put("still", CAMERA_CAPTURE_MODE_STILL);
                        put("video", CAMERA_CAPTURE_MODE_VIDEO);
                    }
                });
            }

            private Map<String, Object> getCaptureTargetConstants() {
                return Collections.unmodifiableMap(new HashMap<String, Object>() {
                    {
                        put("memory", CAMERA_CAPTURE_TARGET_MEMORY);
                        put("disk", CAMERA_CAPTURE_TARGET_DISK);
                        put("cameraRoll", CAMERA_CAPTURE_TARGET_CAMERA_ROLL);
                        put("temp", CAMERA_CAPTURE_TARGET_TEMP);
                    }
                });
            }

            private Map<String, Object> getOrientationConstants() {
                return Collections.unmodifiableMap(new HashMap<String, Object>() {
                    {
                        put("auto", CAMERA_ORIENTATION_AUTO);
                        put("landscapeLeft", CAMERA_ORIENTATION_LANDSCAPE_LEFT);
                        put("landscapeRight", CAMERA_ORIENTATION_LANDSCAPE_RIGHT);
                        put("portrait", CAMERA_ORIENTATION_PORTRAIT);
                        put("portraitUpsideDown", CAMERA_ORIENTATION_PORTRAIT_UPSIDE_DOWN);
                    }
                });
            }

            private Map<String, Object> getFlashModeConstants() {
                return Collections.unmodifiableMap(new HashMap<String, Object>() {
                    {
                        put("off", CAMERA_FLASH_MODE_OFF);
                        put("on", CAMERA_FLASH_MODE_ON);
                        put("auto", CAMERA_FLASH_MODE_AUTO);
                    }
                });
            }

            private Map<String, Object> getTorchModeConstants() {
                return Collections.unmodifiableMap(new HashMap<String, Object>() {
                    {
                        put("off", CAMERA_TORCH_MODE_OFF);
                        put("on", CAMERA_TORCH_MODE_ON);
                        put("auto", CAMERA_TORCH_MODE_AUTO);
                    }
                });
            }
        });
    }

    public static final Map<String, Object> VALID_BARCODE_TYPES =
            Collections.unmodifiableMap(new HashMap<String, Object>() {
                {
                    put("aztec", BarcodeFormat.AZTEC.toString());
                    put("ean13", BarcodeFormat.EAN_13.toString());
                    put("ean8", BarcodeFormat.EAN_8.toString());
                    put("qr", BarcodeFormat.QR_CODE.toString());
                    put("pdf417", BarcodeFormat.PDF_417.toString());
                    put("upc_e", BarcodeFormat.UPC_E.toString());
                    put("datamatrix", BarcodeFormat.DATA_MATRIX.toString());
                    put("code39", BarcodeFormat.CODE_39.toString());
                    put("code93", BarcodeFormat.CODE_93.toString());
                    put("interleaved2of5", BarcodeFormat.ITF.toString());
                    put("codabar", BarcodeFormat.CODABAR.toString());
                    put("code128", BarcodeFormat.CODE_128.toString());
                    put("maxicode", BarcodeFormat.MAXICODE.toString());
                    put("rss14", BarcodeFormat.RSS_14.toString());
                    put("rssexpanded", BarcodeFormat.RSS_EXPANDED.toString());
                    put("upc_a", BarcodeFormat.UPC_A.toString());
                    put("upc_ean", BarcodeFormat.UPC_EAN_EXTENSION.toString());
                }
            });
}
