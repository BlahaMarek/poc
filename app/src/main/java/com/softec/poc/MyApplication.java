package com.softec.poc;

import android.app.Application;
import android.os.Bundle;

import com.microblink.MicroblinkSDK;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        /** Set the Base64 license */
        MicroblinkSDK.setLicenseKey("sRwAAAAOY29tLnNvZnRlYy5wb2P9KN95BlaLJSWYryEGFWy7I3pH6GnDBdh9IVcmOkU60pOonJRKKRbJ7w1QTFWTCd3AGJwamT8eCtyG8ONAIlhr2L4BfSK4sdryMijYYtxIWUyZtKbS8qvvqkkHGNe8ZBb63Vd3MCBFadJFoMla1kCSQ/YnMQ0MoIzi12+5C+ouV3RTjwRXOdtmNVMH+ORKact+QDLg8RsNPMt7pkeP1+7UVr8jwIGA3Y7jnJQZzms+Nx7KCVaFdoNlXcM2IT7/Y0LuKqO1JOFyi8VUIAA/zRUYNsvo1djnPPxJYV2jGDpOweuUKNhPSzfyumlrZeCMd0AFU+pFmHg=", this);

    }

}
