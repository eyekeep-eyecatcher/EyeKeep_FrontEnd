package com.example.eyekeep.fetchSafetyData;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.eyekeep.DTO.SafetyEmergencyBellDTO;
import com.example.eyekeep.R;
import com.example.eyekeep.request.RequestAccessIsActive;
import com.example.eyekeep.retrofit.RetrofitClient;
import com.example.eyekeep.service.MapService;
import com.example.eyekeep.repository.Utils;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.OverlayImage;
import com.naver.maps.map.util.MarkerIcons;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FetchSafetyEmergencyBell {
    private final Context context;
    private final NaverMap naverMap;
    private final List<Marker> SafetyEmergencyBellMarkers = new ArrayList<>(); // 안전비상벨 마커들을 저장할 리스트

    public FetchSafetyEmergencyBell(Context context, NaverMap naverMap) {
        this.context = context;
        this.naverMap = naverMap;
    }

    public void fetchSafetyEmergencyBells() {
        if (checkMarker()) {
            return;
        }
        // 저장된 액세스 토큰의 유효성을 확인 후 불러옵니다.
        RequestAccessIsActive.checkAccessToken();
        String accessToken = Utils.getAccessToken(null);

        MapService service = RetrofitClient.getRetrofitInstance().create(MapService.class);
        Call<SafetyEmergencyBellDTO> call = service.getSafetyEmergencyBell("Bearer " + accessToken);

        call.enqueue(new Callback<SafetyEmergencyBellDTO>() {
            @Override
            public void onResponse(@NonNull Call<SafetyEmergencyBellDTO> call, @NonNull Response<SafetyEmergencyBellDTO> response) {
                if (!response.isSuccessful()) {
                    int statusCode = response.code();
                    String errorMessage = response.message();
                    Log.e("HttpRequestError", "Fetch SafetyEmergencyBell Error code : Code " + statusCode + " - " + errorMessage);
                    Toast.makeText(context, "응답 오류: 코드 " + statusCode + " - " + errorMessage, Toast.LENGTH_LONG).show();
                    return;
                }

                SafetyEmergencyBellDTO body = response.body();
                if(body == null){
                    Log.e("HttpRequestError", "Fetch SafetyEmergencyBell Response Body is null");
                    Toast.makeText(context, "응답 오류: 응답 메세지가 없습니다.", Toast.LENGTH_LONG).show();
                    return;
                }

                List<SafetyEmergencyBellDTO.data> bellList = body.getData();

                if(bellList == null || bellList.isEmpty()){
                    Log.e("MainActivity", "SafetyEmergencyBell list is empty or null");
                    Toast.makeText(context, "안전비상벨 데이터를 가져오지 못했습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }

                for (SafetyEmergencyBellDTO.data bell : bellList) {
                    try {
                        LatLng latLng = new LatLng(Double.parseDouble(bell.getLatitude()), Double.parseDouble(bell.getLongitude()));
                        addSafeEmergencyBellMarker(latLng, bell.getLocation()); //  마커 추가
                    } catch (NumberFormatException e) {
                        Log.e("MainActivity", "SafetyEmergencyBell NumberFormatException: " + e.getMessage());
                        Toast.makeText(context, "안전비상벨 좌표 변환 오류", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<SafetyEmergencyBellDTO> call, @NonNull Throwable t) {
                Log.e("NetworkError", "Error occurred: " + t.getMessage());
                Toast.makeText(context, "네트워크 에러", Toast.LENGTH_SHORT).show();
            }

            private void addSafeEmergencyBellMarker(LatLng latLng, String name) {
                Marker marker = new Marker();
                marker.setPosition(latLng);
                //marker.setCaptionText(name); // 마커에 안전비상벨 이름을 캡션으로 표시
                marker.setWidth(20);
                marker.setHeight(28);
                marker.setIcon(OverlayImage.fromResource(R.drawable.image_safety_emergency_bell_marker));
                marker.setIcon(MarkerIcons.PINK); // 안전비상벨 마커 색상 설정
                marker.setMap(naverMap); // 마커를 지도에 추가

                SafetyEmergencyBellMarkers.add(marker); // 리스트에 추가하여 추후 제거할 수 있도록 관리
            }
        });
    }

    public void removeSafetyEmergencyBellMarkers() {
        for (Marker marker : SafetyEmergencyBellMarkers) {
            marker.setMap(null); // 마커를 지도에서 제거
        }
    }

    public boolean checkMarker() {
        // 기존 마커 기록이 남아있을 경우 서버에 데이터 요청을 하지 않음.
        if (!SafetyEmergencyBellMarkers.isEmpty()) {
            for (Marker marker : SafetyEmergencyBellMarkers) {
                marker.setMap(naverMap);
            }

            return true;
        }
        return false;
    }
}
