package com.example.eyekeep.request;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.eyekeep.retrofit.RetrofitClient;
import com.example.eyekeep.service.ExtractToken;
import com.example.eyekeep.service.UserService;
import com.example.eyekeep.repository.Utils;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RequestReissue {

    public static void reissue() {
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
