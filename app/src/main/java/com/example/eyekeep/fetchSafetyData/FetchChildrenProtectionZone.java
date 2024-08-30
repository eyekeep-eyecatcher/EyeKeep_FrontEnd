package com.example.eyekeep.fetchSafetyData;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.eyekeep.DTO.ChildrenProtectionZoneDTO;
import com.example.eyekeep.request.RequestAccessIsActive;
import com.example.eyekeep.retrofit.RetrofitClient;
import com.example.eyekeep.service.MapService;
import com.example.eyekeep.repository.Utils;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.overlay.CircleOverlay;
import com.naver.maps.map.overlay.Marker;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FetchChildrenProtectionZone {
    private final Context context;
    private final NaverMap naverMap;

    private final List<Marker> ChildrenProtectionZoneMarkers = new ArrayList<>(); // 아동보호구역 마커들을 저장할 리스트
    private final List<CircleOverlay> ChildrenProtectionZoneCircles = new ArrayList<>(); // 아동보호구역 원들을 저장할 리스트

    public FetchChildrenProtectionZone(Context context, NaverMap naverMap) {
        this.context = context;
        this.naverMap = naverMap;
    }

    public void fetchChildrenProtectionZones() {
        if (checkMarker()) {
            return;
        }
        // 저장된 액세스 토큰의 유효성을 확인 후 불러옵니다.
        RequestAccessIsActive.checkAccessToken();
        String accessToken = Utils.getAccessToken(null);

        MapService service = RetrofitClient.getRetrofitInstance().create(MapService.class);
        Call<ChildrenProtectionZoneDTO> call = service.getChildrenProtectionZone("Bearer " + accessToken);

        call.enqueue(new Callback<ChildrenProtectionZoneDTO>() {
            @Override
            public void onResponse(@NonNull Call<ChildrenProtectionZoneDTO> call, @NonNull Response<ChildrenProtectionZoneDTO> response) {
                if (!response.isSuccessful()) {
                    int statusCode = response.code();
                    String errorMessage = response.message();
                    Log.e("HttpRequestError", "Fetch ChildrenProtectionZone Error code : Code " + statusCode + " - " + errorMessage);
                    Toast.makeText(context, "응답 오류: 코드 " + statusCode + " - " + errorMessage, Toast.LENGTH_LONG).show();
                    return;
                }

                ChildrenProtectionZoneDTO body = response.body();
                if(body == null){
                    Log.e("HttpRequestError", "Fetch ChildrenProtectionZone Response Body is null");
                    Toast.makeText(context, "응답 오류: 응답 메세지가 없습니다.", Toast.LENGTH_LONG).show();
                    return;
                }
                List<ChildrenProtectionZoneDTO.data> protectionZoneList = body.getData();

                if(protectionZoneList == null || protectionZoneList.isEmpty()){
                    Log.e("MainActivity", "ChildrenProtectionZone list is empty or null");
                    Toast.makeText(context, "아동보호구역 데이터를 가져오지 못했습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }

                for (ChildrenProtectionZoneDTO.data zoneList : protectionZoneList) {
                    try {
                        LatLng latLng = new LatLng(Double.parseDouble(zoneList.getLatitude()), Double.parseDouble(zoneList.getLongitude()));
                        addChildrenProtectionZoneMarker(latLng, zoneList.getName()); //  마커 추가
                    } catch (NumberFormatException e) {
                        Log.e("MainActivity", "ChildrenProtectionZone NumberFormatException: " + e.getMessage());
                        Toast.makeText(context, "아동보호구역 좌표 변환 오류", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ChildrenProtectionZoneDTO> call, @NonNull Throwable t) {
                Log.e("NetworkError", "Error occurred: " + t.getMessage());
                Toast.makeText(context, "네트워크 에러", Toast.LENGTH_SHORT).show();
            }

            private void addChildrenProtectionZoneMarker(LatLng latLng, String name) {
                /*Marker marker = new Marker();
                marker.setPosition(latLng);
                marker.setCaptionText(name); // 마커에 아동보호구역 이름을 캡션으로 표시
                marker.setWidth(50);  // 너비를 50px로 설정
                marker.setHeight(70); // 높이를 70px로 설정
                marker.setIcon(OverlayImage.fromResource(R.drawable.image));
                marker.setMap(naverMap); // 마커를 지도에 추가
                // 마커 중심으로 반지름 150m 짜리 원 추가*/

                CircleOverlay circle = new CircleOverlay();
                circle.setCenter(latLng); // 원의 중심 좌표 설정
                circle.setRadius(150); // 반지름 150m 설정
                circle.setColor(0x60FFFF00); // 반투명한 노란색 (ARGB: 0x60이 알파 값, 나머지가 RGB)
                circle.setMap(naverMap); // 원을 지도에 추가

                //ChildrenProtectionZoneMarkers.add(marker); // 리스트에 추가하여 추후 제거할 수 있도록 관리
                ChildrenProtectionZoneCircles.add(circle);

            }
        });
    }
    /*
    public void removeChildrenProtectionZoneMarkers() {
        for (Marker marker : ChildrenProtectionZoneMarkers) {
            marker.setMap(null); // 마커를 지도에서 제거
        }
        ChildrenProtectionZoneMarkers.clear(); // 리스트를 비웁니다.
    }

     */
    public void removeChildrenProtectionZoneCircles() {
        for (CircleOverlay circle : ChildrenProtectionZoneCircles) {
            circle.setMap(null); // 원을 지도에서 제거
        }
    }

    public boolean checkMarker() {
        // 기존 마커 기록이 남아있을 경우 서버에 데이터 요청을 하지 않음.
        if (!ChildrenProtectionZoneCircles.isEmpty()) {
            for (CircleOverlay circleOverlay : ChildrenProtectionZoneCircles) {
                circleOverlay.setMap(naverMap);
            }

            return true;
        }
        return false;
    }
}
