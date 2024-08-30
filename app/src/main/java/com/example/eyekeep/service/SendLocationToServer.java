package com.example.eyekeep.service;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.eyekeep.DTO.ChildLocationDTO;
import com.example.eyekeep.request.RequestAccessIsActive;
import com.example.eyekeep.retrofit.RetrofitClient;
import com.example.eyekeep.repository.Utils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SendLocationToServer {
    //내위치 전송
    public void sendLocationToServer(double latitude, double longitude) {
        RequestAccessIsActive.checkAccessToken();
        String accessToken = Utils.getAccessToken(null);
        if(accessToken == null) {
            return;
        }
        ChildLocationDTO childLocationDTO = new ChildLocationDTO();
        childLocationDTO.setLatitude(String.valueOf(latitude));
        childLocationDTO.setLongitude(String.valueOf(longitude));
        MapService service = RetrofitClient.getRetrofitInstance().create(MapService.class);

        Call<Void> call = service.updateLocation("Bearer " + accessToken, childLocationDTO);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("Response", "Send location successfully");
                } else {
                    Log.e("Response", "Send location failed, reponse code : " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Log.e("Error", "Network error exception : " + t.getMessage());
            }
        });
    }

}
