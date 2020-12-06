package com.revinad.iwantadelivery.ApiFcmNotification;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ApiInterface {
    @Headers({"Authorization: key=AAAAIWjnCiQ:APA91bEOo4x5TTGpYFeONL1mFgogUr0Rt503NUbte2vUa1qAiUINHdYu9ZZBHqmIeEPwos3V8CfqBsHVvh2_xVHo6sM3KABVPLIyp28EAhfbA9wE8iKwP54kBdEs3DDW--SlQRBL_4Kg",
            "Content-Type:application/json"})
    @POST("fcm/send")
    Call<ResponseBody> sendChatNotification(@Body RequestNotification requestNotification);
}
