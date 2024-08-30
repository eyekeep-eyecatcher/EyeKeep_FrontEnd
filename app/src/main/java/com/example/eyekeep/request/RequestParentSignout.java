package com.example.eyekeep.request;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.eyekeep.MainParentActivity;
import com.example.eyekeep.repository.Utils;
import com.example.eyekeep.retrofit.RetrofitClient;
import com.example.eyekeep.service.UserService;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RequestParentSignout {
    private final MainParentActivity mainParentActivity;

    public RequestParentSignout(MainParentActivity mainParentActivity) {
        this.mainParentActivity = mainParentActivity;
    }

    public void requestSignout() {
        String refreshToken = Utils.getRefreshToken(null);
        UserService service = RetrofitClient.getRetrofitInstance().create(UserService.class);
        Call<Map<String, Object>> call = service.signout("refresh=" + refreshToken);
        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, Object>> call, @NonNull Response<Map<String, Object>> response) {
                if (response.isSuccessful()){
                    mainParentActivity.signout();
                } else {
                    int status = response.code();
                    String errorMessage = (String) response.body().get("message");
                    Log.e("RequestSignOutError", "Sign out Failed Error code : " + status + "Message : " + errorMessage);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, Object>> call, @NonNull Throwable t) {
                Log.e("NetworkError", "Error occurred: " + t.getMessage());
            }
        });

    }
}
