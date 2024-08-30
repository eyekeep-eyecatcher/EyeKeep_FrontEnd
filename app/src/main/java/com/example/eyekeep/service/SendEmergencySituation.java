package com.example.eyekeep.service;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.eyekeep.repository.Utils;
import com.example.eyekeep.request.RequestAccessIsActive;
import com.example.eyekeep.retrofit.RetrofitClient;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SendEmergencySituation {

    public void sendEmergencySituation() {
        RequestAccessIsActive.checkAccessToken();
        String accessToken = Utils.getAccessToken(null);
        UserService service = RetrofitClient.getRetrofitInstance().create(UserService.class);
        Call<Void> call = service.sendEmergencySituation("Bearer " + accessToken);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (!response.isSuccessful()) {
                    // 실패 처리 (HTTP 상태 코드가 200-299 범위가 아닐 때)
                    int statusCode = response.code();
                    if(statusCode == 401) {
                        Log.e("HttpRequestError", "Send Emergency Situation failed with status code: " + statusCode);
                        reissue();
                    }
                    else if(statusCode == 403) {
                        Log.e("HttpRequestError", "Send Emergency Situation failed with status code: " + statusCode);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Log.e("NetworkError", "Error occurred: " + t.getMessage());
            }
        });
    }

    public void reissue() {
        String refreshToken = Utils.getRefreshToken(null);
        if(refreshToken == null) {
            return;
        }
        UserService service = RetrofitClient.getRetrofitInstance().create(UserService.class);
        Call<Map<String, Object>> call = service.reissue("refresh=" + refreshToken);
        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, Object>> call, @NonNull Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    ExtractToken.SaveToken(response);
                    Log.i("ReissueAccessToken", "Reissue Access Token Success");
                    sendEmergencySituation();
                }
                else {
                    // 실패 처리 (HTTP 상태 코드가 200-299 범위가 아닐 때)
                    int statusCode = response.code();
                    Log.e("HttpRequestError", "Reissue Token failed with status code: " + statusCode);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, Object>> call, @NonNull Throwable t) {
                Log.e("NetworkError", "Error occurred: " + t.getMessage());
            }
        });
    }
}
