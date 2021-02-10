package com.softec.poc;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.microblink.MicroblinkSDK;
import com.microblink.directApi.DirectApiErrorListener;
import com.microblink.directApi.RecognizerRunner;
import com.microblink.entities.Entity;
import com.microblink.entities.recognizers.Recognizer;
import com.microblink.entities.recognizers.RecognizerBundle;
import com.microblink.entities.recognizers.blinkid.generic.BlinkIdCombinedRecognizer;
import com.microblink.entities.recognizers.blinkid.generic.BlinkIdRecognizer;
import com.microblink.entities.recognizers.blinkid.mrtd.MrtdRecognizer;
import com.microblink.entities.recognizers.blinkid.mrtd.MrzResult;
import com.microblink.hardware.orientation.Orientation;
import com.microblink.recognition.RecognitionSuccessType;
import com.microblink.view.recognition.ScanResultListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;


public class MainActivity extends AppCompatActivity {

    private static final int MY_REQUEST_CODE = 1337;
    private static final int PERMISSION_REQUEST_CODE = 235;
    private RecognizerRunner mRecognizerRunner;
    private BlinkIdRecognizer mRecognizer;
    private RecognizerBundle mRecognizerBundle;

    private static final Bitmap.Config BITMAP_CONFIG = Bitmap.Config.ARGB_8888;
    private static final String ASSETS_BITMAP_NAME = "croID.jpg";
    private Bitmap mBitmap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecognizer = new BlinkIdRecognizer();
        MrtdRecognizer mrtdRecognizer = new MrtdRecognizer();
        mRecognizerBundle = new RecognizerBundle(mrtdRecognizer);
//        mRecognizerBundle = new RecognizerBundle(mRecognizer);
        mRecognizerRunner = RecognizerRunner.getSingletonInstance();

        mRecognizerRunner.initialize(this, mRecognizerBundle, new DirectApiErrorListener() {
            @Override
            public void onRecognizerError(Throwable t) {
                Toast.makeText(MainActivity.this, "There was an error in initialization of Recognizer: ", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
        //androidCameraX

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "Fab clicked", Toast.LENGTH_SHORT).show();

            }
        });
    }



    @Override
    protected void onResume() {
        super.onResume();
        // start recognition
        InputStream istr = null;
        try {
            istr = getAssets().open(ASSETS_BITMAP_NAME);
            // load initial bitmap from assets
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = BITMAP_CONFIG;
            mBitmap = BitmapFactory.decodeStream(istr, null, options);
            mRecognizerRunner.recognizeBitmap(mBitmap, Orientation.ORIENTATION_LANDSCAPE_RIGHT, mScanResultListener);

            } catch (Error | IOException e) {
            Toast.makeText(MainActivity.this, "Fokin", Toast.LENGTH_SHORT).show();
        }

    }

    private final ScanResultListener mScanResultListener = new ScanResultListener() {
        @Override
        public void onScanningDone(@NonNull RecognitionSuccessType recognitionSuccessType) {
            // this method is from ScanResultListener and will be called
            // when scanning completes
            // you can obtain scanning result by calling getResult on each
            // recognizer that you bundled into RecognizerBundle.
            // for example:
            Log.d("blahos", "blahos: ");


            BlinkIdRecognizer.Result result = mRecognizer.getResult();
            if (result.getResultState() == Recognizer.Result.State.Valid) {
                // result is valid, you can use it however you wish
            }
            if (recognitionSuccessType != RecognitionSuccessType.UNSUCCESSFUL) {
                // return results (if successful or partial)
                finish();
            } else {
                Toast.makeText(MainActivity.this, "Nothing scanned!", Toast.LENGTH_SHORT).show();
                // enable button again
            };

            Recognizer recognizer = mRecognizerBundle.getRecognizers()[0];
            Entity.Result resultt = recognizer.getResult();
            if (!(resultt instanceof MrtdRecognizer.Result)) {
                Toast.makeText(MainActivity.this, "Nothing scanned!", Toast.LENGTH_SHORT).show();
                return;
            }

            MrzResult mrzResult = ((MrtdRecognizer.Result)resultt).getMrzResult();
            String scanResults =
                    "First name: " + mrzResult.getSecondaryId() +
                            "\nLast name: " + mrzResult.getPrimaryId();
        }
        @Override
        public void onUnrecoverableError(@NonNull Throwable var1) {
            Log.d("dlahos", "dlahos");
        }
    };

    @Override
    protected void onStop() {
        super.onStop();
        if (mRecognizerRunner != null) {
            // terminate the native library
            mRecognizerRunner.terminate();
        }
    }
}