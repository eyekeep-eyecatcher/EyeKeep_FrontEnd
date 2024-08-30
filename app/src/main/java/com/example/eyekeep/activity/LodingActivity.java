package com.example.eyekeep.activity;

import android.Manifest;  // 이 부분을 수정하여 Android의 기본 Manifest를 가져옵니다.
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.eyekeep.MainChildActivity;
import com.example.eyekeep.MainParentActivity;
import com.example.eyekeep.R;
import com.example.eyekeep.repository.Utils;
import com.example.eyekeep.retrofit.RetrofitClient;
import com.example.eyekeep.service.ExtractToken;
import com.example.eyekeep.service.UserService;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LodingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_loding);

        // Utils 클래스 초기화
        Context appContext = getApplicationContext();
        Utils.init(appContext);


        // 권한이 모두 허용되었는지 확인
        if (arePermissionsGranted()) {
            // 권한이 모두 허용된 경우에만 TokenRole 체크
            checkTokenRole();
        } else {
            // 권한이 허용되지 않은 경우 AuthorityActivity로 이동
            Intent intent = new Intent(LodingActivity.this, AuthorityActivity.class);
            startActivity(intent);
            finish(); // 현재 액티비티 종료
        }
    }


    private void checkTokenRole() {
        String accessToken = Utils.getAccessToken(null);
        UserService service = RetrofitClient.getRetrofitInstance().create(UserService.class);
        Call<Map<String, Object>> call = service.checkRole("Bearer " + accessToken);
        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, Object>> call, @NonNull Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    // role  선택까지 완료된 사용자일 경우 메인 화면으로 이동.
                    String role = response.body().get("message").toString();
                    if (role.equals("Parent")) {
                        Intent intent = new Intent(LodingActivity.this, MainParentActivity.class);
                        startActivity(intent);
                        finish(); //현재 액티비티 파괴
                    }
                    else if (role.equals("Child")) {
                        Intent intent = new Intent(LodingActivity.this, MainChildActivity.class);
                        startActivity(intent);
                        finish(); //현재 액티비티 파괴
                    }
                }
                else {
                    // 실패 처리 (HTTP 상태 코드가 200-299 범위가 아닐 때)
                    int statusCode = response.code();
                    Log.e("HttpRequestError", "Check accessToken failed with status code: " + statusCode);

                    if (statusCode == 401) {
                        // AccessToken이 만료된 사용자일 경우 reissue 후 다시 체크.
                        reissue();
                    }
                    else if (statusCode == 403) {
                        // role 선택이 안된 사용자일 경우 role 선택 화면으로 이동
                        Intent intent = new Intent(LodingActivity.this, SelectRoleActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, Object>> call, @NonNull Throwable t) {
                Log.e("NetworkError", "Error occurred: " + t.getMessage());
                new AlertDialog.Builder(LodingActivity.this)
                        .setTitle("오류 발생")
                        .setMessage("앱에 오류가 발생하여 종료됩니다.")
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                finish(); // 현재 액티비티 종료
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });
    }

    private void reissue() {
        String refreshToken = Utils.getRefreshToken(null);
        UserService service = RetrofitClient.getRetrofitInstance().create(UserService.class);
        Call<Map<String, Object>> call = service.reissue("refresh=" + refreshToken);
        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, Object>> call, @NonNull Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    ExtractToken.SaveToken(response);
                    checkTokenRole();
                }
                else {
                    // 실패 처리 (HTTP 상태 코드가 200-299 범위가 아닐 때)
                    int statusCode = response.code();
                    Log.e("HttpRequestError", "Reissue failed with status code: " + statusCode);

                    Intent intent = new Intent(LodingActivity.this, LogintestActivity.class);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, Object>> call, @NonNull Throwable t) {
                Log.e("NetworkError", "Error occurred: " + t.getMessage());
                new AlertDialog.Builder(LodingActivity.this)
                        .setTitle("오류 발생")
                        .setMessage("앱에 오류가 발생하여 종료됩니다.")
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                finish(); // 현재 액티비티 종료
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });
    }

    private boolean arePermissionsGranted() {
        boolean isLocationPermissionGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean isNotificationPermissionGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
        boolean isSmsPermissionGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;
        boolean isBluetoothPermissionGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED;

        // 필요한 모든 권한이 허용되었는지 확인
        return isLocationPermissionGranted && isNotificationPermissionGranted && isSmsPermissionGranted && isBluetoothPermissionGranted;
    }
}
