package com.example.eyekeep.request;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.eyekeep.DTO.RoadNodeDTO;
import com.example.eyekeep.MainParentActivity;
import com.example.eyekeep.repository.Utils;
import com.example.eyekeep.retrofit.RetrofitClient;
import com.example.eyekeep.service.MapService;
import com.naver.maps.geometry.LatLng;

import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RequestRoute2 {
    private final MainParentActivity mainParentActivity;

    public RequestRoute2(MainParentActivity mainChildActivity) {
        this.mainParentActivity = mainChildActivity;
    }

    public void getRouteFromServer(List<RoadNodeDTO> startToEnd) {
        RequestAccessIsActive.checkAccessToken();
        String accessToken = Utils.getAccessToken(null);
        MapService service = RetrofitClient.getRetrofitInstance().create(MapService.class);
        Call<List<RoadNodeDTO>> call = service.findPath("Bearer " + accessToken, startToEnd);
        call.enqueue(new Callback<List<RoadNodeDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<RoadNodeDTO>> call, @NonNull Response<List<RoadNodeDTO>> response) {
                if (!response.isSuccessful()) {
                    int statusCode = response.code();
                    Log.e("HttpRequestError", "Find Route failed with status code: " + statusCode);
                }

                List<RoadNodeDTO> routeNodes =  response.body();
                List<LatLng> routePoints = routeNodes.stream()
                        .map(node -> new LatLng(node.getLatitude(), node.getLongitude()))
                        .collect(Collectors.toList());

                // 경로를 지도에 표시하고, 경로에 화살표를 추가
                mainParentActivity.drawRouteOnMap(routePoints);
            }

            @Override
            public void onFailure(@NonNull Call<List<RoadNodeDTO>> call, @NonNull Throwable t) {
                Log.e("NetworkError", "Error occurred: " + t.getMessage());
            }
        });
    }
}
