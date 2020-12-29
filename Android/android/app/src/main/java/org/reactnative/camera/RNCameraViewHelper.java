package org.reactnative.camera;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.CamcorderProfile;
import android.media.ExifInterface;
import android.os.Build;

import com.google.android.cameraview.CameraView;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.text.TextBlock;
import com.google.zxing.Result;

import org.reactnative.camera.utils.CameraSetting;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class RNCameraViewHelper {

  public static final String[][] exifTags = new String[][]{
      {"string", ExifInterface.TAG_ARTIST},
      {"int", ExifInterface.TAG_BITS_PER_SAMPLE},
      {"int", ExifInterface.TAG_COMPRESSION},
      {"string", ExifInterface.TAG_COPYRIGHT},
      {"string", ExifInterface.TAG_DATETIME},
      {"string", ExifInterface.TAG_IMAGE_DESCRIPTION},
      {"int", ExifInterface.TAG_IMAGE_LENGTH},
      {"int", ExifInterface.TAG_IMAGE_WIDTH},
      {"int", ExifInterface.TAG_JPEG_INTERCHANGE_FORMAT},
      {"int", ExifInterface.TAG_JPEG_INTERCHANGE_FORMAT_LENGTH},
      {"string", ExifInterface.TAG_MAKE},
      {"string", ExifInterface.TAG_MODEL},
      {"int", ExifInterface.TAG_ORIENTATION},
      {"int", ExifInterface.TAG_PHOTOMETRIC_INTERPRETATION},
      {"int", ExifInterface.TAG_PLANAR_CONFIGURATION},
      {"double", ExifInterface.TAG_PRIMARY_CHROMATICITIES},
      {"double", ExifInterface.TAG_REFERENCE_BLACK_WHITE},
      {"int", ExifInterface.TAG_RESOLUTION_UNIT},
      {"int", ExifInterface.TAG_ROWS_PER_STRIP},
      {"int", ExifInterface.TAG_SAMPLES_PER_PIXEL},
      {"string", ExifInterface.TAG_SOFTWARE},
      {"int", ExifInterface.TAG_STRIP_BYTE_COUNTS},
      {"int", ExifInterface.TAG_STRIP_OFFSETS},
      {"int", ExifInterface.TAG_TRANSFER_FUNCTION},
      {"double", ExifInterface.TAG_WHITE_POINT},
      {"double", ExifInterface.TAG_X_RESOLUTION},
      {"double", ExifInterface.TAG_Y_CB_CR_COEFFICIENTS},
      {"int", ExifInterface.TAG_Y_CB_CR_POSITIONING},
      {"int", ExifInterface.TAG_Y_CB_CR_SUB_SAMPLING},
      {"double", ExifInterface.TAG_Y_RESOLUTION},
      {"double", ExifInterface.TAG_APERTURE_VALUE},
      {"double", ExifInterface.TAG_BRIGHTNESS_VALUE},
      {"string", ExifInterface.TAG_CFA_PATTERN},
      {"int", ExifInterface.TAG_COLOR_SPACE},
      {"string", ExifInterface.TAG_COMPONENTS_CONFIGURATION},
      {"double", ExifInterface.TAG_COMPRESSED_BITS_PER_PIXEL},
      {"int", ExifInterface.TAG_CONTRAST},
      {"int", ExifInterface.TAG_CUSTOM_RENDERED},
      {"string", ExifInterface.TAG_DATETIME_DIGITIZED},
      {"string", ExifInterface.TAG_DATETIME_ORIGINAL},
      {"string", ExifInterface.TAG_DEVICE_SETTING_DESCRIPTION},
      {"double", ExifInterface.TAG_DIGITAL_ZOOM_RATIO},
      {"string", ExifInterface.TAG_EXIF_VERSION},
      {"double", ExifInterface.TAG_EXPOSURE_BIAS_VALUE},
      {"double", ExifInterface.TAG_EXPOSURE_INDEX},
      {"int", ExifInterface.TAG_EXPOSURE_MODE},
      {"int", ExifInterface.TAG_EXPOSURE_PROGRAM},
      {"double", ExifInterface.TAG_EXPOSURE_TIME},
      {"double", ExifInterface.TAG_F_NUMBER},
      {"string", ExifInterface.TAG_FILE_SOURCE},
      {"int", ExifInterface.TAG_FLASH},
      {"double", ExifInterface.TAG_FLASH_ENERGY},
      {"string", ExifInterface.TAG_FLASHPIX_VERSION},
      {"double", ExifInterface.TAG_FOCAL_LENGTH},
      {"int", ExifInterface.TAG_FOCAL_LENGTH_IN_35MM_FILM},
      {"int", ExifInterface.TAG_FOCAL_PLANE_RESOLUTION_UNIT},
      {"double", ExifInterface.TAG_FOCAL_PLANE_X_RESOLUTION},
      {"double", ExifInterface.TAG_FOCAL_PLANE_Y_RESOLUTION},
      {"int", ExifInterface.TAG_GAIN_CONTROL},
      {"int", ExifInterface.TAG_ISO_SPEED_RATINGS},
      {"string", ExifInterface.TAG_IMAGE_UNIQUE_ID},
      {"int", ExifInterface.TAG_LIGHT_SOURCE},
      {"string", ExifInterface.TAG_MAKER_NOTE},
      {"double", ExifInterface.TAG_MAX_APERTURE_VALUE},
      {"int", ExifInterface.TAG_METERING_MODE},
      {"int", ExifInterface.TAG_NEW_SUBFILE_TYPE},
      {"string", ExifInterface.TAG_OECF},
      {"int", ExifInterface.TAG_PIXEL_X_DIMENSION},
      {"int", ExifInterface.TAG_PIXEL_Y_DIMENSION},
      {"string", ExifInterface.TAG_RELATED_SOUND_FILE},
      {"int", ExifInterface.TAG_SATURATION},
      {"int", ExifInterface.TAG_SCENE_CAPTURE_TYPE},
      {"string", ExifInterface.TAG_SCENE_TYPE},
      {"int", ExifInterface.TAG_SENSING_METHOD},
      {"int", ExifInterface.TAG_SHARPNESS},
      {"double", ExifInterface.TAG_SHUTTER_SPEED_VALUE},
      {"string", ExifInterface.TAG_SPATIAL_FREQUENCY_RESPONSE},
      {"string", ExifInterface.TAG_SPECTRAL_SENSITIVITY},
      {"int", ExifInterface.TAG_SUBFILE_TYPE},
      {"string", ExifInterface.TAG_SUBSEC_TIME},
      {"string", ExifInterface.TAG_SUBSEC_TIME_DIGITIZED},
      {"string", ExifInterface.TAG_SUBSEC_TIME_ORIGINAL},
      {"int", ExifInterface.TAG_SUBJECT_AREA},
      {"double", ExifInterface.TAG_SUBJECT_DISTANCE},
      {"int", ExifInterface.TAG_SUBJECT_DISTANCE_RANGE},
      {"int", ExifInterface.TAG_SUBJECT_LOCATION},
      {"string", ExifInterface.TAG_USER_COMMENT},
      {"int", ExifInterface.TAG_WHITE_BALANCE},
      {"int", ExifInterface.TAG_GPS_ALTITUDE_REF},
      {"string", ExifInterface.TAG_GPS_AREA_INFORMATION},
      {"double", ExifInterface.TAG_GPS_DOP},
      {"string", ExifInterface.TAG_GPS_DATESTAMP},
      {"double", ExifInterface.TAG_GPS_DEST_BEARING},
      {"string", ExifInterface.TAG_GPS_DEST_BEARING_REF},
      {"double", ExifInterface.TAG_GPS_DEST_DISTANCE},
      {"string", ExifInterface.TAG_GPS_DEST_DISTANCE_REF},
      {"double", ExifInterface.TAG_GPS_DEST_LATITUDE},
      {"string", ExifInterface.TAG_GPS_DEST_LATITUDE_REF},
      {"double", ExifInterface.TAG_GPS_DEST_LONGITUDE},
      {"string", ExifInterface.TAG_GPS_DEST_LONGITUDE_REF},
      {"int", ExifInterface.TAG_GPS_DIFFERENTIAL},
      {"double", ExifInterface.TAG_GPS_IMG_DIRECTION},
      {"string", ExifInterface.TAG_GPS_IMG_DIRECTION_REF},
      {"string", ExifInterface.TAG_GPS_LATITUDE_REF},
      {"string", ExifInterface.TAG_GPS_LONGITUDE_REF},
      {"string", ExifInterface.TAG_GPS_MAP_DATUM},
      {"string", ExifInterface.TAG_GPS_MEASURE_MODE},
      {"string", ExifInterface.TAG_GPS_PROCESSING_METHOD},
      {"string", ExifInterface.TAG_GPS_SATELLITES},
      {"double", ExifInterface.TAG_GPS_SPEED},
      {"string", ExifInterface.TAG_GPS_SPEED_REF},
      {"string", ExifInterface.TAG_GPS_STATUS},
      {"string", ExifInterface.TAG_GPS_TIMESTAMP},
      {"double", ExifInterface.TAG_GPS_TRACK},
      {"string", ExifInterface.TAG_GPS_TRACK_REF},
      {"string", ExifInterface.TAG_GPS_VERSION_ID},
      {"string", ExifInterface.TAG_INTEROPERABILITY_INDEX},
      {"int", ExifInterface.TAG_THUMBNAIL_IMAGE_LENGTH},
      {"int", ExifInterface.TAG_THUMBNAIL_IMAGE_WIDTH},
      {"int", ExifInterface.TAG_DNG_VERSION},
      {"int", ExifInterface.TAG_DEFAULT_CROP_SIZE},
      {"int", ExifInterface.TAG_ORF_PREVIEW_IMAGE_START},
      {"int", ExifInterface.TAG_ORF_PREVIEW_IMAGE_LENGTH},
      {"int", ExifInterface.TAG_ORF_ASPECT_FRAME},
      {"int", ExifInterface.TAG_RW2_SENSOR_BOTTOM_BORDER},
      {"int", ExifInterface.TAG_RW2_SENSOR_LEFT_BORDER},
      {"int", ExifInterface.TAG_RW2_SENSOR_RIGHT_BORDER},
      {"int", ExifInterface.TAG_RW2_SENSOR_TOP_BORDER},
      {"int", ExifInterface.TAG_RW2_ISO},
  };
  // Mount error event



  // Utilities

  public static int getCorrectCameraRotation(int rotation, int facing, int cameraOrientation) {
    if (facing == CameraView.FACING_FRONT) {
      return (360 - (cameraOrientation + rotation) % 360) % 360;
    } else {
      final int landscapeFlip = rotationIsLandscape(rotation) ? 180 : 0;
      return (cameraOrientation - rotation + landscapeFlip) % 360;
    }
  }
  
  private static boolean rotationIsLandscape(int rotation) {
    return (rotation == Constants.LANDSCAPE_90 ||
            rotation == Constants.LANDSCAPE_270);
  }

  private static int getCamcorderProfileQualityFromCameraModuleConstant(int quality) {
    switch (quality) {
      case CameraSetting.VIDEO_2160P:
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
          return CamcorderProfile.QUALITY_2160P;
        }
      case CameraSetting.VIDEO_1080P:
        return CamcorderProfile.QUALITY_1080P;
      case CameraSetting.VIDEO_720P:
        return CamcorderProfile.QUALITY_720P;
      case CameraSetting.VIDEO_480P:
        return CamcorderProfile.QUALITY_480P;
      case CameraSetting.VIDEO_4x3:
        return CamcorderProfile.QUALITY_480P;
    }
    return CamcorderProfile.QUALITY_HIGH;
  }
  
  public static CamcorderProfile getCamcorderProfile(int quality) {
    CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
    int camcorderQuality = getCamcorderProfileQualityFromCameraModuleConstant(quality);
    if (CamcorderProfile.hasProfile(camcorderQuality)) {
      profile = CamcorderProfile.get(camcorderQuality);
      if (quality == CameraSetting.VIDEO_4x3) {
        profile.videoFrameWidth = 640;
      }
    }
    return profile;
  }



  public static Bitmap generateSimulatorPhoto(int width, int height) {
    Bitmap fakePhoto = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(fakePhoto);
    Paint background = new Paint();
    background.setColor(Color.BLACK);
    canvas.drawRect(0, 0, width, height, background);
    Paint textPaint = new Paint();
    textPaint.setColor(Color.YELLOW);
    textPaint.setTextSize(35);
    Calendar calendar = Calendar.getInstance();
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd G '->' HH:mm:ss z");
    canvas.drawText(simpleDateFormat.format(calendar.getTime()), width * 0.1f, height * 0.2f, textPaint);
    canvas.drawText(simpleDateFormat.format(calendar.getTime()), width * 0.2f, height * 0.4f, textPaint);
    canvas.drawText(simpleDateFormat.format(calendar.getTime()), width * 0.3f, height * 0.6f, textPaint);
    canvas.drawText(simpleDateFormat.format(calendar.getTime()), width * 0.4f, height * 0.8f, textPaint);

    return fakePhoto;
  }
}
