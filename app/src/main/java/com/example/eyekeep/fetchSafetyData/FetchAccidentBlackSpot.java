package com.example.eyekeep.fetchSafetyData;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.eyekeep.DTO.AccidentBlackSpotDTO;
import com.example.eyekeep.R;
import com.example.eyekeep.request.RequestAccessIsActive;
import com.example.eyekeep.retrofit.RetrofitClient;
import com.example.eyekeep.service.MapService;
import com.example.eyekeep.repository.Utils;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.overlay.CircleOverlay;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.OverlayImage;
import com.naver.maps.map.util.MarkerIcons;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FetchAccidentBlackSpot {
    private final Context context;
    private final NaverMap naverMap;
    private final List<Marker> AccidentBlackSpotMarkers = new ArrayList<>(); //
    private final List<Marker> AccidentBlackSpot2Markers = new ArrayList<>(); //
    private final List<CircleOverlay> AccidentBlackSpotCircles = new ArrayList<>();


    public FetchAccidentBlackSpot(Context context, NaverMap naverMap) {
        this.context = context;
        this.naverMap = naverMap;
    }

    public void fetchAccidentBlackSpot() {
        // 기존 마커 리스트 확인.
        if (checkMarker()) {
            return;
        }
        // 저장된 액세스 토큰의 유효성을 확인 후 불러옵니다.
        RequestAccessIsActive.checkAccessToken();
        String accessToken = Utils.getAccessToken(null);
        MapService service = RetrofitClient.getRetrofitInstance().create(MapService.class);
        Call<AccidentBlackSpotDTO> call = service.getAccidentBlackSpot("Bearer " + accessToken);

        call.enqueue(new Callback<AccidentBlackSpotDTO>() {
            @Override
            public void onResponse(@NonNull Call<AccidentBlackSpotDTO> call, @NonNull Response<AccidentBlackSpotDTO> response) {
                if (!response.isSuccessful()) {
                    int statusCode = response.code();
                    String errorMessage = response.message();
                    Log.e("HttpRequestError", "Fetch AccidentBlackSpot Error code : Code " + statusCode + " - " + errorMessage);
                    return;
                }

                AccidentBlackSpotDTO body = response.body();
                if(body == null){
                    Log.e("HttpRequestError", "Fetch AccidentBlackSpot Response Body is null");
                    Toast.makeText(context, "응답 오류: 응답 메세지가 없습니다.", Toast.LENGTH_LONG).show();
                    return;
                }
                List<AccidentBlackSpotDTO.data> spotList = body.getData();

                if(spotList == null || spotList.isEmpty()){
                    Log.e("MainActivity", "AccidentBlackSpot list is empty or null");
                    Toast.makeText(context, "사고다발구역 데이터를 가져오지 못했습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }

                for (AccidentBlackSpotDTO.data spot : spotList) {
                    try {
                        LatLng latLng = new LatLng(Double.parseDouble(spot.getLatitude()), Double.parseDouble(spot.getLongitude()));
                        addAccidentBlackSpotMarker(latLng, spot.getAccidentType());
                        if (spot.getAccidentType().equals("자전거")) {
                            addAccidentBlackSpot2Marker(latLng, spot.getAccidentType()); // 자전거
                        } else {
                            addAccidentBlackSpotMarker(latLng, spot.getAccidentType()); // 보행어린이, 무단횡단, 스쿨존어린이
                        }

                    } catch (NumberFormatException e) {
                        Log.e("MainActivity", "AccidentBlackSpot NumberFormatException: " + e.getMessage());
                        Toast.makeText(context, "사고다발구역 좌표 변환 오류", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<AccidentBlackSpotDTO> call, @NonNull Throwable t) {
                Log.e("NetworkError", "Error occurred: " + t.getMessage());
                Toast.makeText(context, "네트워크 에러", Toast.LENGTH_SHORT).show();
            }
            // 보행어린이, 무단횡단, 스쿨존어린이
            private void addAccidentBlackSpotMarker(LatLng latLng, String name) {
                Marker marker = new Marker();
                marker.setPosition(latLng);
                marker.setCaptionText(name); // 마커에 사고다발구역 이름을 캡션으로 표시
                marker.setWidth(40);
                marker.setHeight(40);
                marker.setIcon(MarkerIcons.RED); // 사고다발구역 마커 색상 설정
                marker.setMap(naverMap); // 마커를 지도에 추가
                marker.setIcon(OverlayImage.fromResource(R.drawable.image_accident_black_spot_marker));
                // 마커 중심으로 반지름 50m 짜리 원 추가

                CircleOverlay circle = new CircleOverlay();
                circle.setCenter(latLng); // 원의 중심 좌표 설정
                circle.setRadius(50); // 반지름 50m 설정
                circle.setColor(0x40FF0000); // 반투명한 빨간색 (ARGB: 0x40이 알파 값, 나머지가 RGB)
                circle.setMap(naverMap); // 원을 지도에 추가

                AccidentBlackSpotMarkers.add(marker); // 리스트에 추가하여 추후 제거할 수 있도록 관리
                AccidentBlackSpotCircles.add(circle);
            }
            //자전거
            private void addAccidentBlackSpot2Marker(LatLng latLng, String name) {
                Marker marker = new Marker();
                marker.setPosition(latLng);
                marker.setCaptionText(name); // 마커에 사고다발구역 이름을 캡션으로 표시
                marker.setWidth(40);
                marker.setHeight(40);
                marker.setMap(naverMap); // 마커를 지도에 추가
                marker.setIcon(OverlayImage.fromResource(R.drawable.image_accident_black_spot_bicycle_marker));
                // 마커 중심으로 반지름 50m 짜리 원 추가

                CircleOverlay circle = new CircleOverlay();
                circle.setCenter(latLng); // 원의 중심 좌표 설정
                circle.setRadius(50); // 반지름 50m 설정
                circle.setColor(0x40FF0000); // 반투명한 빨간색 (ARGB: 0x40이 알파 값, 나머지가 RGB)
                circle.setMap(naverMap); // 원을 지도에 추가

                AccidentBlackSpot2Markers.add(marker); // 리스트에 추가하여 추후 제거할 수 있도록 관리
                AccidentBlackSpotCircles.add(circle);


            }
        });
    }

    public void removeAccidentBlackSpotMarkers() {
        for (Marker marker : AccidentBlackSpotMarkers) {
            marker.setMap(null); // 마커를 지도에서 제거

        }
    }
    public void removeAccidentBlackSpot2Markers() {
        for (Marker marker : AccidentBlackSpot2Markers) {
            marker.setMap(null); // 마커를 지도에서 제거
        }
    }
    public void removeAccidentBlackSpotCircles() {
        for (CircleOverlay circle : AccidentBlackSpotCircles) {
            circle.setMap(null); // 원을 지도에서 제거
        }
    }

    public boolean checkMarker() {
        // 기존 마커 기록이 남아있을 경우 서버에 데이터 요청을 하지 않음.
        if (!AccidentBlackSpotMarkers.isEmpty() && !AccidentBlackSpot2Markers.isEmpty() && !AccidentBlackSpotCircles.isEmpty()) {
            for (Marker marker : AccidentBlackSpotMarkers) {
                marker.setMap(naverMap);
            }
            for (Marker marker : AccidentBlackSpot2Markers) {
                marker.setMap(naverMap);
            }
            for (CircleOverlay circleOverlay : AccidentBlackSpotCircles) {
                circleOverlay.setMap(naverMap);
            }

            return true;
        }
        return false;
    }
}
