package com.softec.poc;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import com.microblink.MicroblinkSDK;
import com.microblink.directApi.DirectApiErrorListener;
import com.microblink.directApi.RecognizerRunner;
import com.microblink.entities.recognizers.Recognizer;
import com.microblink.entities.recognizers.RecognizerBundle;
import com.microblink.entities.recognizers.blinkid.generic.BlinkIdCombinedRecognizer;
import com.microblink.hardware.orientation.Orientation;
import com.microblink.recognition.RecognitionSuccessType;
import com.microblink.view.recognition.ScanResultListener;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class ScanImageActivity extends Activity {

    /** Request code for built-in camera activity. */
    public static final int TAKE_PHOTO_REQUEST_CODE = 0x101;

    private static final String TAG = "BlinkIDDemo";
    private static final Bitmap.Config BITMAP_CONFIG = Bitmap.Config.ARGB_8888;
    private static final String ASSETS_BITMAP_NAME = "croID.jpg";

    /** File that will hold the image taken from camera. */
    private String mCameraFile = "";

    private Button mScanButton;

    /** Image view which shows current image that will be scanned. */
    private ImageView mImgView;

    /** Current bitmap for recognition. */
    private Bitmap mBitmap;

    private RecognizerRunner mRecognizerRunner;
    private BlinkIdCombinedRecognizer mRecognizer;
    private RecognizerBundle mRecognizerBundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_image);

        mScanButton = findViewById(R.id.btnScan);
        mImgView = findViewById(R.id.ivImageFront);

        // initial bitmap is loaded from assets
        AssetManager assets = getAssets();
        InputStream istr = null;
        try {
            istr = assets.open(ASSETS_BITMAP_NAME);
            // load initial bitmap from assets
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = BITMAP_CONFIG;
            mBitmap = BitmapFactory.decodeStream(istr, null, options);
        } catch (IOException e) {
            // handle exception
            Log.e(TAG, "Failed to load image from assets!");
            Toast.makeText(this, "Failed to load image from assets!", Toast.LENGTH_LONG).show();
            finish();
            return;
        } finally {
            try {
                istr.close();
            } catch (IOException ignorable) { }
        }
        // show loaded bitmap
        mImgView.setImageBitmap(mBitmap);





        // initialize your activity here
        // create BlinkIdCombinedRecognizer
        mRecognizer = new BlinkIdCombinedRecognizer();

        // bundle recognizers into RecognizerBundle
        mRecognizerBundle = new RecognizerBundle(mRecognizer);

        try {
            mRecognizerRunner = RecognizerRunner.getSingletonInstance();
        } catch (Error e) {
            Toast.makeText(this, "Feature not supported! Reason: " + e, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        mRecognizerRunner.initialize(this, mRecognizerBundle, new DirectApiErrorListener() {
            @Override
            public void onRecognizerError(Throwable t) {
                Toast.makeText(ScanImageActivity.this, "There was an error in initialization of Recognizer: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        // start recognition
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream(getAssets().open("croID.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        mRecognizerRunner.recognizeBitmap(bitmap, Orientation.ORIENTATION_LANDSCAPE_RIGHT, mScanResultListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRecognizerRunner.terminate();
    }

    private final ScanResultListener mScanResultListener = new ScanResultListener() {
        @Override
        public void onScanningDone(@NonNull RecognitionSuccessType recognitionSuccessType) {
            // this method is from ScanResultListener and will be called
            // when scanning completes
            // you can obtain scanning result by calling getResult on each
            // recognizer that you bundled into RecognizerBundle.
            // for example:

            BlinkIdCombinedRecognizer.Result result = mRecognizer.getResult();
            if (result.getResultState() == Recognizer.Result.State.Valid) {
                // result is valid, you can use it however you wish
            }
        }

        @Override
        public void onUnrecoverableError(@NonNull Throwable var1) {

        }
    };
//    @Override
//    protected void onStart() {
//        super.onStart();
//
//
        // get the recognizer runner instance
//        mRecognizerRunner = RecognizerRunner.getSingletonInstance();
//
//        // initialize recognizer runner singleton
//        mRecognizerRunner.initialize(this, mRecognizerBundle, new DirectApiErrorListener() {
//            @Override
//            public void onRecognizerError(Throwable t) {
//                Log.e(TAG, "Failed to initialize recognizer.", t);
//                Toast.makeText(com.softec.poc.ScanImageActivity.this, "Failed to initialize recognizer. Reason: "
//                        + t.getMessage(), Toast.LENGTH_LONG).show();
//                finish();
//            }
//        });
//    }
//
//    /**
//     * Starts built-in camera intent for taking scan images.
//     */
//    private void startCamera() {
//        // Starts built-in camera intent for taking scan images
//        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        File photoFile = new File(getFilesDir(), "photo.jpg");
//        mCameraFile = photoFile.getAbsolutePath();
//        Uri photoURI = FileProvider.getUriForFile(this,
//                "com.microblink.blinkid.provider",
//                photoFile);
//        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
//
//        List<ResolveInfo> resolveInfoList = getPackageManager().queryIntentActivities(takePictureIntent, PackageManager.MATCH_DEFAULT_ONLY);
//        for (ResolveInfo resolveInfo : resolveInfoList) {
//            String packageName = resolveInfo.activityInfo.packageName;
//            grantUriPermission(packageName, photoURI, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
//        }
//
//        startActivityForResult(takePictureIntent, TAKE_PHOTO_REQUEST_CODE);
//    }
//
//    /**
//     * Handler for button "Take Photo"
//     */
//    public void takePhotoHandler(View view) {
//        startCamera();
//    }
//
//    /**
//     * Handler for button "Scan"
//     */
//    public void scanButtonHandler(View view) {
//        if (mBitmap == null) {
//            return;
//        }
//
//        mRecognizerRunner.recognizeBitmap(mBitmap, Orientation.ORIENTATION_LANDSCAPE_RIGHT, new ScanResultListener() {
//            @Override
//            public void onScanningDone(@NonNull RecognitionSuccessType recognitionSuccessType) {
//                // this method is from ScanResultListener and will be called
//                // when scanning completes
//                // you can obtain scanning result by calling getResult on each
//                // recognizer that you bundled into RecognizerBundle.
//                // for example:
//
//                BlinkIdCombinedRecognizer.Result result = mRecognizer.getResult();
//                if (result.getResultState() == Recognizer.Result.State.Valid) {
//                    System.out.println(result);
//                    // result is valid, you can use it however you wish
//                }
//            }
//
//            @Override
//            public void onUnrecoverableError(@NonNull Throwable throwable) {
//                Toast.makeText(com.softec.poc.ScanImageActivity.this, throwable.toString(), Toast.LENGTH_LONG).show();
//            }
//        });
//    }

//    @Override
//    protected void onStop() {
//        super.onStop();
//        if (mRecognizerRunner != null) {
//            // terminate the native library
//            mRecognizerRunner.terminate();
//        }
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode == TAKE_PHOTO_REQUEST_CODE) {
//            if (resultCode == RESULT_OK) {
//                // obtain image that was saved to external storage by camera activity
//                try {
//                    Uri imageUri = Uri.fromFile(new File(mCameraFile));
//                    InputStream imageStream = getContentResolver().openInputStream(imageUri);
//                    mBitmap = BitmapFactory.decodeStream(imageStream);
//                    //noinspection ResultOfMethodCallIgnored
//                    imageStream.close();
//                    new File(mCameraFile).delete();
//                    // show camera image
//                    mImgView.setImageBitmap(mBitmap);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
//                }
//            } else {
//                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
//            }
//        }
//    }

}
