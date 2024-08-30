package com.example.eyekeep.request;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.eyekeep.retrofit.RetrofitClient;
import com.example.eyekeep.service.UserService;
import com.example.eyekeep.repository.Utils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RequestFCMToken {

    public static void saveFCMToken(String token) {
        String accessToken = Utils.getAccessToken(null);
        UserService service = RetrofitClient.getRetrofitInstance().create(UserService.class);
        Call<Void> call = service.saveFCM("Bearer " + accessToken, token);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.i("FCMToken", "Send FCM Token Success");
                }
                else {
                    // 실패 처리 (HTTP 상태 코드가 200-299 범위가 아닐 때)
                    int statusCode = response.code();
                    Log.e("HttpRequestError", "Register FCM Token failed with status code: " + statusCode);

                    // Reissue
                    RequestReissue.reissue();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Log.e("NetworkError", "Error occurred: " + t.getMessage());
            }
        });
    }
}
