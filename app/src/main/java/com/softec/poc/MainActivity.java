package com.softec.poc;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.microblink.directApi.DirectApiErrorListener;
import com.microblink.directApi.RecognizerRunner;
import com.microblink.entities.recognizers.RecognizerBundle;
import com.microblink.entities.recognizers.blinkid.generic.BlinkIdCombinedRecognizer;
import com.microblink.entities.recognizers.blinkid.imageoptions.FaceImageOptions;
import com.microblink.entities.recognizers.blinkid.imageoptions.FullDocumentImageOptions;
import com.microblink.hardware.orientation.Orientation;
import com.microblink.recognition.RecognitionSuccessType;
import com.microblink.view.recognition.ScanResultListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;


public class MainActivity extends AppCompatActivity {

    private RecognizerRunner mRecognizerRunner;
    private BlinkIdCombinedRecognizer mRecognizer;
    private RecognizerBundle mRecognizerBundle;

    private static final Bitmap.Config BITMAP_CONFIG = Bitmap.Config.ARGB_8888;
    private static final int CAMERA_PIC_REQUEST = 1337;
    private static final String[] CAMERA_PERMISSION = new String[]{Manifest.permission.CAMERA};
    private static final int CAMERA_REQUEST_CODE = 10;
    private Bitmap mBitmap;
    ImageView img_front;
    Bitmap bitmap_front;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        img_front = findViewById(R.id.img);
        FloatingActionButton send = findViewById(R.id.send);
        FloatingActionButton fab = findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (hasCameraPermission()) {
                    startActivityForResult(new Intent(MainActivity.this, CameraActivity.class),CAMERA_PIC_REQUEST);
                } else {
                    requestPermission();
                }
            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                startMicroblink();

                startInovatrics();

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mRecognizer = new BlinkIdCombinedRecognizer();
        if(mRecognizer instanceof FullDocumentImageOptions) {
            FullDocumentImageOptions options = (FullDocumentImageOptions) mRecognizer;
            options.setReturnFullDocumentImage(true);
        }
        if(mRecognizer instanceof FaceImageOptions) {
            FaceImageOptions options = (FaceImageOptions) mRecognizer;
            options.setReturnFaceImage(true);
        }
        mRecognizerBundle = new RecognizerBundle(mRecognizer);
        mRecognizerRunner = RecognizerRunner.getSingletonInstance();

        mRecognizerRunner.initialize(this, mRecognizerBundle, new DirectApiErrorListener() {
            @Override
            public void onRecognizerError(Throwable t) {
                Toast.makeText(MainActivity.this, "There was an error in initialization of Recognizer: ", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private final ScanResultListener mScanResultListener = new ScanResultListener() {
        @Override
        public void onScanningDone(@NonNull RecognitionSuccessType recognitionSuccessType) {
            BlinkIdCombinedRecognizer.Result result = mRecognizer.getResult();
            if (recognitionSuccessType != RecognitionSuccessType.UNSUCCESSFUL) {
                Log.d("s", "onScanningDone: ");
            } else {
                Toast.makeText(MainActivity.this, "Nothing scanned!", Toast.LENGTH_SHORT).show();
            };
        }
        @Override
        public void onUnrecoverableError(@NonNull Throwable var1) {
            Toast.makeText(MainActivity.this, "Error! Nothing scanned!", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onStop() {
        super.onStop();
        if (mRecognizerRunner != null) {
            mRecognizerRunner.terminate();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
       if (requestCode == CAMERA_PIC_REQUEST){
           BitmapFactory.Options options = new BitmapFactory.Options();
           options.inPreferredConfig = BITMAP_CONFIG;
           bitmap_front = BitmapFactory.decodeFile(data.getData().toString(), options);
           img_front.setImageBitmap(bitmap_front);
       }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, CAMERA_PERMISSION, CAMERA_REQUEST_CODE );
    }

    private void startMicroblink() {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = BITMAP_CONFIG;

            mBitmap = BitmapFactory.decodeStream(getAssets().open("back.jpg"), null, options);

            mRecognizerRunner.recognizeBitmap(bitmap_front, Orientation.ORIENTATION_LANDSCAPE_RIGHT, mScanResultListener);

        } catch (Error | IOException e) {
            Toast.makeText(MainActivity.this, "Fokin", Toast.LENGTH_SHORT).show();
        }
    }

    private void startInovatrics() {
        String url = "http://ocrpoc.softec.sk:9001/";
    }

}