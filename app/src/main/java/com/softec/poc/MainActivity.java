package com.softec.poc;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.internal.LinkedTreeMap;
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

import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


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
                resetState();
                startMicroblink();
                startInovatrics();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mRecognizerRunner != null) {
            mRecognizerRunner.terminate();
        }

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

            mBitmap = BitmapFactory.decodeStream(getAssets().open("a2.jpg"), null, options);

            mRecognizerRunner.recognizeBitmap(bitmap_front, Orientation.ORIENTATION_LANDSCAPE_RIGHT, mScanResultListener);

        } catch (Error | IOException e) {
            Toast.makeText(MainActivity.this, "Fokin", Toast.LENGTH_SHORT).show();
        }
    }

    private final ScanResultListener mScanResultListener = new ScanResultListener() {
        @Override
        public void onScanningDone(@NonNull RecognitionSuccessType recognitionSuccessType) {
            BlinkIdCombinedRecognizer.Result result = mRecognizer.getResult();
            if (recognitionSuccessType != RecognitionSuccessType.UNSUCCESSFUL) {
                fillMicroData(result);
            } else {
                Toast.makeText(MainActivity.this, "Nothing scanned!", Toast.LENGTH_SHORT).show();
            };
        }
        @Override
        public void onUnrecoverableError(@NonNull Throwable var1) {
            Toast.makeText(MainActivity.this, "Error! Nothing scanned!", Toast.LENGTH_SHORT).show();
        }
    };


    private void startInovatrics() {
        String url = "http://ocrpoc.softec.sk:9001/api/v3/";
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .build();



        InovatricsService service = retrofit.create(InovatricsService.class);

        Request request = createRequest();
        Call<com.softec.poc.Response> call =service.postImage("application/json", request);
        call.enqueue(new Callback<com.softec.poc.Response>() {
            @Override
            public void onResponse(Call<com.softec.poc.Response> call, Response<com.softec.poc.Response> response) {
                Log.d("ss", "DSADA");
                fillInovatricsData(response);
            }

            @Override
            public void onFailure(Call<com.softec.poc.Response> call, Throwable t) {
                Toast.makeText(getApplicationContext(), t.toString(), Toast.LENGTH_LONG).show();
            }

        });


    }

    private Request createRequest() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        String encoded = "";
        Bitmap localBitMap;
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = BITMAP_CONFIG;
            mBitmap = BitmapFactory.decodeStream(getAssets().open("a2.jpg"), null, options);
            localBitMap = bitmap_front;
            localBitMap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream .toByteArray();

            encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
        } catch (Error | IOException e) {
            Toast.makeText(MainActivity.this, "Fokin", Toast.LENGTH_SHORT).show();
        }
        Request request = new Request();
        Image img =  new Image();
        img.setName("document_front_side");
        img.setData(encoded);
        request.getImages().add(img);
        request.getModelMetadataIds().add("fd3d8ded-7bfb-4cd6-8012-d88fffee6ee7");

        return request;
    }

    private void fillMicroData(BlinkIdCombinedRecognizer.Result result) {
        ((TextView)findViewById(R.id.mName)).setText(result.getFirstName());
        ((TextView)findViewById(R.id.mSurname)).setText(result.getLastName());
        ((TextView)findViewById(R.id.mSex)).setText(result.getSex());
        ((TextView)findViewById(R.id.mNo)).setText(result.getDocumentNumber());
        ((TextView)findViewById(R.id.mBirthNo)).setText(result.getPersonalIdNumber());
        ((TextView)findViewById(R.id.mBirthDate)).setText(result.getDateOfBirth().getOriginalDateString());
        ((TextView)findViewById(R.id.mNationality)).setText(result.getNationality());
        ((TextView)findViewById(R.id.mIssuedBy)).setText(result.getIssuingAuthority());
        ((TextView)findViewById(R.id.mIssuedDate)).setText(result.getDateOfIssue().getOriginalDateString());
        ((TextView)findViewById(R.id.mExpiryDate)).setText(result.getDateOfExpiry().getOriginalDateString());

    }
    private void fillInovatricsData(Response<com.softec.poc.Response> response) {

        List<TextFields> list = response.body().getPages().get(0).getTextFields();

        list.forEach(textFields -> {
            if (textFields.getId().equals("date_of_birth")) {
                ((TextView)findViewById(R.id.iBirthDate)).setText(textFields.getLines().get(0).getText());
            }else if (textFields.getId().equals("date_of_expiry")) {
                ((TextView)findViewById(R.id.iExpiryDate)).setText(textFields.getLines().get(0).getText());
            }else if (textFields.getId().equals("date_of_issue")) {
                ((TextView)findViewById(R.id.iIssuedDate)).setText(textFields.getLines().get(0).getText());
            }else if (textFields.getId().equals("document_number")) {
                ((TextView)findViewById(R.id.iNo)).setText(textFields.getLines().get(0).getText());
            }else if (textFields.getId().equals("given_names")) {
                ((TextView)findViewById(R.id.iName)).setText(textFields.getLines().get(0).getText());
            }else if (textFields.getId().equals("issued_by")) {
                ((TextView)findViewById(R.id.iIssuedBy)).setText(textFields.getLines().get(0).getText());
            }else if (textFields.getId().equals("nationality")) {
                ((TextView)findViewById(R.id.iNationality)).setText(textFields.getLines().get(0).getText());
            }else if (textFields.getId().equals("personal_number")) {
                ((TextView)findViewById(R.id.iBirthNo)).setText(textFields.getLines().get(0).getText());
            }else if (textFields.getId().equals("sex")) {
                ((TextView)findViewById(R.id.iSex)).setText(textFields.getLines().get(0).getText());
            }else if (textFields.getId().equals("surname")) {
                ((TextView)findViewById(R.id.iSurname)).setText(textFields.getLines().get(0).getText());

            }
        });
    }

    private void resetState() {
        ((TextView)findViewById(R.id.mName)).setText("Sample");
        ((TextView)findViewById(R.id.mSurname)).setText("Sample");
        ((TextView)findViewById(R.id.mSex)).setText("Sample");
        ((TextView)findViewById(R.id.mNo)).setText("Sample");
        ((TextView)findViewById(R.id.mBirthNo)).setText("Sample");
        ((TextView)findViewById(R.id.mBirthDate)).setText("Sample");
        ((TextView)findViewById(R.id.mNationality)).setText("Sample");
        ((TextView)findViewById(R.id.mIssuedBy)).setText("Sample");
        ((TextView)findViewById(R.id.mIssuedDate)).setText("Sample");
        ((TextView)findViewById(R.id.mExpiryDate)).setText("Sample");

        ((TextView)findViewById(R.id.iName)).setText("Sample");
        ((TextView)findViewById(R.id.iSurname)).setText("Sample");
        ((TextView)findViewById(R.id.iSex)).setText("Sample");
        ((TextView)findViewById(R.id.iNo)).setText("Sample");
        ((TextView)findViewById(R.id.iBirthNo)).setText("Sample");
        ((TextView)findViewById(R.id.iBirthDate)).setText("Sample");
        ((TextView)findViewById(R.id.iNationality)).setText("Sample");
        ((TextView)findViewById(R.id.iIssuedBy)).setText("Sample");
        ((TextView)findViewById(R.id.iIssuedDate)).setText("Sample");
        ((TextView)findViewById(R.id.iExpiryDate)).setText("Sample");
    }
}