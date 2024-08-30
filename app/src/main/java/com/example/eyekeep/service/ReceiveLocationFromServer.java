package com.example.eyekeep.service;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.eyekeep.DTO.ChildLocationDTO;
import com.example.eyekeep.R;
import com.example.eyekeep.request.RequestAccessIsActive;
import com.example.eyekeep.retrofit.RetrofitClient;
import com.example.eyekeep.repository.Utils;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.OverlayImage;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReceiveLocationFromServer {
    private Marker userMarker;
    private final NaverMap naverMap;

    public ReceiveLocationFromServer(NaverMap naverMap) {
        this.naverMap = naverMap;
    }

    public void receiveLocationFromServer() {  //LocationData 는 DTO라 보면될듯
        String accessToken = Utils.getAccessToken(null);
        RequestAccessIsActive.checkAccessToken();

        MapService service = RetrofitClient.getRetrofitInstance().create(MapService.class);
        Call<ChildLocationDTO> call = service.getLocation("Bearer " + accessToken);
        call.enqueue(new Callback<ChildLocationDTO>() {
            @Override
            public void onResponse(@NonNull Call<ChildLocationDTO> call, @NonNull Response<ChildLocationDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ChildLocationDTO locationData = response.body();
                    double latitude = Double.parseDouble(locationData.getLatitude());
                    double longitude = Double.parseDouble(locationData.getLongitude());
                    Log.i("GetLocationSuccess", "Get location request success");
                    // 지도에 위치 표시 업데이트
                    updateMapLocation(latitude, longitude);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ChildLocationDTO> call, @NonNull Throwable t) {
                Log.e("NetworkError", "Network error exception : " + t.getMessage());
            }
        });
    }

    private void updateMapLocation(double latitude, double longitude) {
        LatLng latLng = new LatLng(latitude, longitude);

        if (userMarker == null) {
            userMarker = new Marker();
            userMarker.setPosition(latLng);
            userMarker.setMap(naverMap); // 마커를 지도에 표시
            userMarker.setIcon(OverlayImage.fromResource(R.drawable.image_eye_location_marker));
            naverMap.moveCamera(CameraUpdate.scrollTo(latLng));
        } else {
            userMarker.setPosition(latLng); // 기존 마커 위치 업데이트
        }


    }
}
