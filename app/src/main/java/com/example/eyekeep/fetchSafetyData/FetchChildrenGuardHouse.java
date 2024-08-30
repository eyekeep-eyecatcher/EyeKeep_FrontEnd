package com.example.eyekeep.fetchSafetyData;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.eyekeep.DTO.ChildrenGuardHouseDTO;
import com.example.eyekeep.R;
import com.example.eyekeep.request.RequestAccessIsActive;
import com.example.eyekeep.retrofit.RetrofitClient;
import com.example.eyekeep.service.MapService;
import com.example.eyekeep.repository.Utils;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.OverlayImage;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FetchChildrenGuardHouse {
    private final Context context;
    private final NaverMap naverMap;
    private final List<Marker> ChildrenGuardHouseMarkers = new ArrayList<>(); // 아동안전지킴이집 마커들을 저장할 리스트

    public FetchChildrenGuardHouse(Context context, NaverMap naverMap) {
        this.context = context;
        this.naverMap = naverMap;
    }

    public void fetchChildrenGuardHouses() {
        if (checkMarker()) {
            return;
        }
        // 저장된 액세스 토큰의 유효성을 확인 후 불러옵니다.
        RequestAccessIsActive.checkAccessToken();
        String accessToken = Utils.getAccessToken(null);

        MapService service = RetrofitClient.getRetrofitInstance().create(MapService.class);
        Call<ChildrenGuardHouseDTO> call = service.getChildrenGuardHouse("Bearer " + accessToken);

        call.enqueue(new Callback<ChildrenGuardHouseDTO>() {
            @Override
            public void onResponse(@NonNull Call<ChildrenGuardHouseDTO> call, @NonNull Response<ChildrenGuardHouseDTO> response) {
                if (!response.isSuccessful()) {
                    int statusCode = response.code();
                    String errorMessage = response.message();
                    Log.e("HttpRequestError", "Fetch ChildrenGuardHouse Error code : Code " + statusCode + " - " + errorMessage);
                    Toast.makeText(context, "응답 오류: 코드 " + statusCode + " - " + errorMessage, Toast.LENGTH_LONG).show();
                    return;
                }

                ChildrenGuardHouseDTO body = response.body();
                if(body == null){
                    Log.e("HttpRequestError", "Fetch ChildrenGuardHouse Response Body is null");
                    Toast.makeText(context, "응답 오류: 응답 메세지가 없습니다.", Toast.LENGTH_LONG).show();
                    return;
                }
                List<ChildrenGuardHouseDTO.data> houseList = body.getData();

                if(houseList == null || houseList.isEmpty()){
                    Log.e("MainActivity", "ChildrenGuardHouse list is empty or null");
                    Toast.makeText(context, "아동안전지킴이집 데이터를 가져오지 못했습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }

                for (ChildrenGuardHouseDTO.data house : houseList) {
                    try {
                        LatLng latLng = new LatLng(Double.parseDouble(house.getLatitude()), Double.parseDouble(house.getLongitude()));
                        addChildrenGuardHouseMarker(latLng, house.getName()); // 마커 추가
                    } catch (NumberFormatException e) {
                        Log.e("MainActivity", "ChildrenGuardHouse NumberFormatException: " + e.getMessage());
                        Toast.makeText(context, "돌발상황정보 좌표 변환 오류", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ChildrenGuardHouseDTO> call, @NonNull Throwable t) {
                Log.e("NetworkError", "Error occurred: " + t.getMessage());
                Toast.makeText(context, "네트워크 에러", Toast.LENGTH_SHORT).show();
            }

            private void addChildrenGuardHouseMarker(LatLng latLng, String name) {
                Marker marker = new Marker();
                marker.setPosition(latLng);
                //marker.setCaptionText(name); //아동안전지킴이집 이름을 캡션으로 표시
                marker.setWidth(50);
                marker.setHeight(50);
                marker.setMap(naverMap); // 마커를 지도에 추가

                ChildrenGuardHouseMarkers.add(marker); // 리스트에 추가하여 추후 제거할 수 있도록 관리
                marker.setIcon(OverlayImage.fromResource(R.drawable.image_children_guard_house_marker));

            }
        });
    }

    public void removeChildrenGuardHouseMarkers() {
        for (Marker marker : ChildrenGuardHouseMarkers) {
            marker.setMap(null); // 마커를 지도에서 제거
        }
    }

    public boolean checkMarker() {
        // 기존 마커 기록이 남아있을 경우 서버에 데이터 요청을 하지 않음.
        if (!ChildrenGuardHouseMarkers.isEmpty()) {
            for (Marker marker : ChildrenGuardHouseMarkers) {
                marker.setMap(naverMap);
            }

            return true;
        }
        return false;
    }
}
