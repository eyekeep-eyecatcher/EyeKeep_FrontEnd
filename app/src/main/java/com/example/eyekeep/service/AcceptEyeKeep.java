package com.example.eyekeep.service;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.eyekeep.request.RequestAccessIsActive;
import com.example.eyekeep.request.RequestReissue;
import com.example.eyekeep.retrofit.RetrofitClient;
import com.example.eyekeep.repository.Utils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AcceptEyeKeep {
    private final Context context;

    public AcceptEyeKeep(Context context) {
        this.context = context;
    }

    //수락
    public void acceptEyeKeep(String email) {
        RequestAccessIsActive.checkAccessToken();
        String accessToken = Utils.getAccessToken(null);
        UserService service = RetrofitClient.getRetrofitInstance().create(UserService.class);
        Call<Void> call = service.acceptEyeKeep("Bearer " + accessToken, email);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(context, "아이킵 설정 성공", Toast.LENGTH_SHORT).show();
                }
                else {
                    // 실패 처리 (HTTP 상태 코드가 200-299 범위가 아닐 때)
                    int statusCode = response.code();
                    if(statusCode == 401) {
                        Log.e("HttpRequestError", "Accept EyeKeep failed with status code: " + statusCode + ", Message : AccessToken Error, check Token");
                        RequestReissue.reissue();
                    }
                    else if(statusCode == 403) {
                        Log.e("HttpRequestError", "Accept EyeKeep failed with status code: " + statusCode + " Message: Already set EyeKeep");
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Log.e("NetworkError", "Error occurred: " + t.getMessage());
            }
        });


    }
}
