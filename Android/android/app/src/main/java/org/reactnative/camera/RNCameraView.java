package org.reactnative.camera;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.util.SparseArray;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.google.android.cameraview.CameraView;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;

import org.reactnative.barcodedetector.RNBarcodeDetector;
import org.reactnative.camera.tasks.BarCodeScannerAsyncTask;
import org.reactnative.camera.tasks.BarCodeScannerAsyncTaskDelegate;
import org.reactnative.camera.tasks.BarcodeDetectorAsyncTask;
import org.reactnative.camera.tasks.BarcodeDetectorAsyncTaskDelegate;
import org.reactnative.camera.tasks.TextRecognizerAsyncTask;
import org.reactnative.camera.tasks.TextRecognizerAsyncTaskDelegate;
import org.reactnative.camera.utils.CameraSetting;
import org.reactnative.camera.utils.ImageDimensions;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;

public class RNCameraView extends CameraView implements BarCodeScannerAsyncTaskDelegate,
        BarcodeDetectorAsyncTaskDelegate, TextRecognizerAsyncTaskDelegate {
    private static final String TAG = RNCameraView.class.getSimpleName();
    private Context context;
    private List<String> mBarCodeTypes = null;
    private boolean invertImageData = false;

    // Concurrency lock for scanners to avoid flooding the runtime
    public volatile boolean barCodeScannerTaskLock = false;
    public volatile boolean googleBarcodeDetectorTaskLock = false;
    public volatile boolean textRecognizerTaskLock = false;

    // Scanning-related properties
    private MultiFormatReader mMultiFormatReader;
    private RNBarcodeDetector mGoogleBarcodeDetector;
    private TextRecognizer mTextRecognizer;
    private boolean mShouldDetectFaces = false;
    private boolean mShouldGoogleDetectBarcodes = false;
    private boolean mShouldScanBarCodes = false;
    private boolean mShouldRecognizeText = false;
    private int mGoogleVisionBarCodeType = Barcode.ALL_FORMATS;
    private int mGoogleVisionBarCodeMode = RNBarcodeDetector.NORMAL_MODE;

    private BarCodeScannerAsyncTaskDelegate barCodeScannerAsyncTaskDelegate;

    public RNCameraView(Context context) {
        super(context, true);
        this.context = context;

        addCallback(new Callback() {
            @Override
            public void onCameraOpened(CameraView cameraView) {
//                RNCameraViewHelper.emitCameraReadyEvent(cameraView);
            }

            @Override
            public void onMountError(CameraView cameraView) {
//                RNCameraViewHelper.emitMountErrorEvent(cameraView, "Camera view threw an error - component could not be rendered.");
            }

            @Override
            public void onPictureTaken(CameraView cameraView, final byte[] data, int deviceOrientation) {

            }

            @Override
            public void onVideoRecorded(CameraView cameraView, String path, int videoOrientation, int deviceOrientation) {

            }

            @Override
            public void onFramePreview(CameraView cameraView, byte[] data, int width, int height, int rotation) {
                int correctRotation = RNCameraViewHelper.getCorrectCameraRotation(rotation, getFacing(), getCameraOrientation());
                boolean willCallBarCodeTask = mShouldScanBarCodes && !barCodeScannerTaskLock && cameraView instanceof BarCodeScannerAsyncTaskDelegate;
                boolean willCallGoogleBarcodeTask = mShouldGoogleDetectBarcodes && !googleBarcodeDetectorTaskLock && cameraView instanceof BarcodeDetectorAsyncTaskDelegate;
                boolean willCallTextTask = mShouldRecognizeText && !textRecognizerTaskLock && cameraView instanceof TextRecognizerAsyncTaskDelegate;
                if (!willCallBarCodeTask && !willCallGoogleBarcodeTask && !willCallTextTask) {
                    return;
                }

                if (data.length < (1.5 * width * height)) {
                    return;
                }

                if (willCallBarCodeTask) {
                    barCodeScannerTaskLock = true;
                    BarCodeScannerAsyncTaskDelegate delegate = (BarCodeScannerAsyncTaskDelegate) cameraView;
                    new BarCodeScannerAsyncTask(delegate, mMultiFormatReader, data, width, height).execute();
                }


                if (willCallGoogleBarcodeTask) {
                    googleBarcodeDetectorTaskLock = true;
                    if (mGoogleVisionBarCodeMode == RNBarcodeDetector.NORMAL_MODE) {
                        invertImageData = false;
                    } else if (mGoogleVisionBarCodeMode == RNBarcodeDetector.ALTERNATE_MODE) {
                        invertImageData = !invertImageData;
                    } else if (mGoogleVisionBarCodeMode == RNBarcodeDetector.INVERTED_MODE) {
                        invertImageData = true;
                    }
                    if (invertImageData) {
                        for (int y = 0; y < data.length; y++) {
                            data[y] = (byte) ~data[y];
                        }
                    }
                    BarcodeDetectorAsyncTaskDelegate delegate = (BarcodeDetectorAsyncTaskDelegate) cameraView;
                    new BarcodeDetectorAsyncTask(delegate, mGoogleBarcodeDetector, data, width, height, correctRotation).execute();
                }

                if (willCallTextTask) {
                    textRecognizerTaskLock = true;
                    TextRecognizerAsyncTaskDelegate delegate = (TextRecognizerAsyncTaskDelegate) cameraView;
                    new TextRecognizerAsyncTask(delegate, mTextRecognizer, data, width, height, correctRotation).execute();
                }
            }
        });
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        View preview = getView();
        if (null == preview) {
            return;
        }
        float width = right - left;
        float height = bottom - top;
        float ratio = getAspectRatio().toFloat();
        int orientation = getResources().getConfiguration().orientation;
        int correctHeight;
        int correctWidth;
        this.setBackgroundColor(Color.BLACK);
        if (orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE) {
            if (ratio * height < width) {
                correctHeight = (int) (width / ratio);
                correctWidth = (int) width;
            } else {
                correctWidth = (int) (height * ratio);
                correctHeight = (int) height;
            }
        } else {
            if (ratio * width > height) {
                correctHeight = (int) (width * ratio);
                correctWidth = (int) width;
            } else {
                correctWidth = (int) (height / ratio);
                correctHeight = (int) height;
            }
        }
        int paddingX = (int) ((width - correctWidth) / 2);
        int paddingY = (int) ((height - correctHeight) / 2);
        preview.layout(paddingX, paddingY, correctWidth + paddingX, correctHeight + paddingY);
    }

    @SuppressLint("all")
    @Override
    public void requestLayout() {
        // React handles this for us, so we don't need to call super.requestLayout();
    }

    public void setBarCodeTypes(List<String> barCodeTypes) {
        mBarCodeTypes = barCodeTypes;
        initBarcodeReader();
    }

    /**
     * Initialize the barcode decoder.
     * Supports all iOS codes except [code138, code39mod43, itf14]
     * Additionally supports [codabar, code128, maxicode, rss14, rssexpanded, upc_a, upc_ean]
     */
    private void initBarcodeReader() {
        mMultiFormatReader = new MultiFormatReader();
        EnumMap<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
        EnumSet<BarcodeFormat> decodeFormats = EnumSet.noneOf(BarcodeFormat.class);

        if (mBarCodeTypes != null) {
            for (String code : mBarCodeTypes) {
                String formatString = (String) CameraSetting.VALID_BARCODE_TYPES.get(code);
                if (formatString != null) {
                    decodeFormats.add(BarcodeFormat.valueOf(code));
                }
            }
        }

        hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);
        mMultiFormatReader.setHints(hints);
    }

    public void setShouldScanBarCodes(boolean shouldScanBarCodes) {
        if (shouldScanBarCodes && mMultiFormatReader == null) {
            initBarcodeReader();
        }
        this.mShouldScanBarCodes = shouldScanBarCodes;
        setScanning(mShouldDetectFaces || mShouldGoogleDetectBarcodes || mShouldScanBarCodes || mShouldRecognizeText);
    }

    public void onBarCodeRead(Result barCode, int width, int height) {
        String barCodeType = barCode.getBarcodeFormat().toString();
        if (!mShouldScanBarCodes || !mBarCodeTypes.contains(barCodeType)) {
            return;
        }
        if (barCodeScannerAsyncTaskDelegate != null) {
            barCodeScannerAsyncTaskDelegate.onBarCodeRead(barCode, width, height);
        }
        //扫描成功回调
//        RNCameraViewHelper.emitBarCodeReadEvent(this, barCode, width, height);
    }

    public void onBarCodeScanningTaskCompleted() {
        barCodeScannerTaskLock = false;
        if (mMultiFormatReader != null) {
            mMultiFormatReader.reset();
        }
        if (barCodeScannerAsyncTaskDelegate != null) {
            barCodeScannerAsyncTaskDelegate.onBarCodeScanningTaskCompleted();
        }
    }


    /**
     * Initial setup of the barcode detector
     */
    private void setupBarcodeDetector() {
        mGoogleBarcodeDetector = new RNBarcodeDetector(context);
        mGoogleBarcodeDetector.setBarcodeType(mGoogleVisionBarCodeType);
    }

    /**
     * Initial setup of the text recongizer
     */
    private void setupTextRecongnizer() {
        mTextRecognizer = new TextRecognizer.Builder(context).build();
    }

    public void setGoogleVisionBarcodeType(int barcodeType) {
        mGoogleVisionBarCodeType = barcodeType;
        if (mGoogleBarcodeDetector != null) {
            mGoogleBarcodeDetector.setBarcodeType(barcodeType);
        }
    }

    public void setGoogleVisionBarcodeMode(int barcodeMode) {
        mGoogleVisionBarCodeMode = barcodeMode;
    }

    public void onBarcodesDetected(SparseArray<Barcode> barcodesReported, int sourceWidth, int sourceHeight, int sourceRotation) {
        if (!mShouldGoogleDetectBarcodes) {
            return;
        }

        SparseArray<Barcode> barcodesDetected = barcodesReported == null ? new SparseArray<Barcode>() : barcodesReported;

//        RNCameraViewHelper.emitBarcodesDetectedEvent(this, barcodesDetected);
    }

    public void onBarcodeDetectionError(RNBarcodeDetector barcodeDetector) {
        if (!mShouldGoogleDetectBarcodes) {
            return;
        }

//        RNCameraViewHelper.emitBarcodeDetectionErrorEvent(this, barcodeDetector);
    }

    @Override
    public void onBarcodeDetectingTaskCompleted() {
        googleBarcodeDetectorTaskLock = false;
    }

    public void setShouldRecognizeText(boolean shouldRecognizeText) {
        if (shouldRecognizeText && mTextRecognizer == null) {
            setupTextRecongnizer();
        }
        this.mShouldRecognizeText = shouldRecognizeText;
        setScanning(mShouldDetectFaces || mShouldGoogleDetectBarcodes || mShouldScanBarCodes || mShouldRecognizeText);
    }

    @Override
    public void onTextRecognized(SparseArray<TextBlock> textBlocks, int sourceWidth, int sourceHeight, int sourceRotation) {
        if (!mShouldRecognizeText) {
            return;
        }

        SparseArray<TextBlock> textBlocksDetected = textBlocks == null ? new SparseArray<TextBlock>() : textBlocks;
        ImageDimensions dimensions = new ImageDimensions(sourceWidth, sourceHeight, sourceRotation, getFacing());

//        RNCameraViewHelper.emitTextRecognizedEvent(this, textBlocksDetected, dimensions);
    }

    @Override
    public void onTextRecognizerTaskCompleted() {
        textRecognizerTaskLock = false;
    }


    private boolean hasCameraPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int result = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA);
            return result == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

    public void setBarCodeScannerAsyncTaskDelegate(BarCodeScannerAsyncTaskDelegate barCodeScannerAsyncTaskDelegate) {
        this.barCodeScannerAsyncTaskDelegate = barCodeScannerAsyncTaskDelegate;
    }
}
