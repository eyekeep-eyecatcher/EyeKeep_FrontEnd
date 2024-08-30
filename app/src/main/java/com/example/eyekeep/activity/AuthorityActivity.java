package com.example.eyekeep.activity;

import android.Manifest;  // 이 부분을 수정하여 Android의 기본 Manifest를 가져옵니다.
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
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

public class AuthorityActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_CODE = 100;
    private static final int NOTIFICATION_PERMISSION_CODE = 101;
    private static final int SMS_PERMISSION_CODE = 102;
    private static final int BLUETOOTH_PERMISSION_CODE = 103;

    private boolean isLocationPermissionGranted = false;
    private boolean isNotificationPermissionGranted = false;
    private boolean isSmsPermissionGranted = false;
    private boolean isBluetoothPermissionGranted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authority);

        AppCompatButton nextButton = findViewById(R.id.btn_authrity_next);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissionsAndProceed();
            }
        });
    }

    private void checkPermissionsAndProceed() {
        isLocationPermissionGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        // Android 13 이상에서만 POST_NOTIFICATIONS 권한 필요
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            isNotificationPermissionGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
        } else {
            isNotificationPermissionGranted = true; // 알림 권한이 필요하지 않음
        }

        isSmsPermissionGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;
        //isBluetoothPermissionGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED;

        if (isLocationPermissionGranted && isNotificationPermissionGranted && isSmsPermissionGranted && isBluetoothPermissionGranted) {
            // 모든 권한이 허용된 경우 다음 화면으로 이동
            checkTokenRole();
        } else {
            // 권한 요청
            requestPermissionsIfNeeded();
        }
    }

    private void requestPermissionsIfNeeded() {
        if (!isLocationPermissionGranted) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
        } else if (!isNotificationPermissionGranted && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_CODE);
        } else if (!isSmsPermissionGranted) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_CODE);
        } else if (!isBluetoothPermissionGranted) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH}, BLUETOOTH_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case LOCATION_PERMISSION_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    isLocationPermissionGranted = true;
                }
                break;
            case NOTIFICATION_PERMISSION_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    isNotificationPermissionGranted = true;
                }
                break;
            case SMS_PERMISSION_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    isSmsPermissionGranted = true;
                }
                break;
            case BLUETOOTH_PERMISSION_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    isBluetoothPermissionGranted = true;
                }
                break;
        }

        // 모든 권한이 승인되었는지 다시 확인하고 처리
        checkPermissionsAndProceed();
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
                        Intent intent = new Intent(AuthorityActivity.this, MainParentActivity.class);
                        startActivity(intent);
                        finish(); //현재 액티비티 파괴
                    }
                    else if (role.equals("Child")) {
                        Intent intent = new Intent(AuthorityActivity.this, MainChildActivity.class);
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
                        Intent intent = new Intent(AuthorityActivity.this, SelectRoleActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, Object>> call, @NonNull Throwable t) {
                Log.e("NetworkError", "Error occurred: " + t.getMessage());
                new AlertDialog.Builder(AuthorityActivity.this)
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

                    Intent intent = new Intent(AuthorityActivity.this, LogintestActivity.class);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, Object>> call, @NonNull Throwable t) {
                Log.e("NetworkError", "Error occurred: " + t.getMessage());
                new AlertDialog.Builder(AuthorityActivity.this)
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
}
