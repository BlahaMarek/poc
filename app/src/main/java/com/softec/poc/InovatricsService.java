package com.softec.poc;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface InovatricsService {

    @POST("process-document")
    Call<Response> postImage(@Header("Content-Type") String content_type, @Body Request request);



}
