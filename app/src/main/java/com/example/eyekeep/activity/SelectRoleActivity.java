package com.example.eyekeep.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.eyekeep.DTO.RoleDTO;
import com.example.eyekeep.MainChildActivity;
import com.example.eyekeep.MainParentActivity;
import com.example.eyekeep.R;
import com.example.eyekeep.retrofit.RetrofitClient;
import com.example.eyekeep.service.ExtractToken;
import com.example.eyekeep.service.UserService;
import com.example.eyekeep.repository.Utils;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SelectRoleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_role);

        RadioButton parentRadioButton = findViewById(R.id.parentRadioButton);
        RadioButton childRadioButton = findViewById(R.id.childRadioButton);
        AppCompatButton continueButton = findViewById(R.id.continueButton);

        // 시작 시 continueButton을 비활성화하고 초기 배경 설정
        continueButton.setEnabled(false);
        continueButton.setBackgroundResource(R.drawable.button_loginn2);

        // 라디오 그룹 가져오기 (둘 중 하나가 RadioGroup에 포함되어야 합니다)
        RadioGroup roleRadioGroup = findViewById(R.id.roleRadioGroup);

        // 라디오 버튼 선택 변경 리스너 설정
        roleRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            // 라디오 버튼이 선택되면 continueButton을 활성화하고 배경을 변경
            if (checkedId != -1) {
                continueButton.setEnabled(true);
                continueButton.setBackgroundResource(R.drawable.button_loginn);
            }
        });

        continueButton.setOnClickListener(v -> {
            String role;
            if (parentRadioButton.isChecked()) {
                role = "Parent";
            } else if (childRadioButton.isChecked()) {
                role = "Child";
            } else {
                Toast.makeText(this, "역할을 선택하세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            // 역할을 서버로 전송
            sendUserRole(role);
        });
    }


    private void sendUserRole(String role) {
        // 저장된 액세스 토큰을 불러옵니다.
        String accessToken = Utils.getAccessToken(null);
        RoleDTO roleDTO = new RoleDTO();
        roleDTO.setRole(role);
        UserService service = RetrofitClient.getRetrofitInstance().create(UserService.class);
        Call<Map<String, Object>> call = service.role("Bearer " + accessToken, roleDTO);
        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, Object>> call, @NonNull Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(SelectRoleActivity.this, "역할 전송 성공: " + role, Toast.LENGTH_SHORT).show();
                    ExtractToken.SaveToken(response);
                    String responseRole = response.body().get("message").toString();
                    if (responseRole.equals("Parent")) {
                        // 메인 화면으로 이동
                        Intent intent = new Intent(SelectRoleActivity.this, MainParentActivity.class);
                        startActivity(intent);
                        finish(); // 현재 액티비티 종료
                    }
                    else if (responseRole.equals("Child")) {
                        // 메인 화면으로 이동
                        Intent intent = new Intent(SelectRoleActivity.this, MainChildActivity.class);
                        startActivity(intent);
                        finish(); // 현재 액티비티 종료
                    }
                }
                else {
                    int statusCode = response.code(); // HTTP 상태 코드

                    if (statusCode == 401) {
                        reissue(role);
                        return;
                    }

                    Log.e("HttpRequestError", "Send role error: Code " + statusCode);
                    Toast.makeText(SelectRoleActivity.this, "Send role Error: Code " + statusCode, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, Object>> call, @NonNull Throwable t) {
                Log.e("NetworkError", "Error occurred: " + t.getMessage());
                new AlertDialog.Builder(SelectRoleActivity.this)
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

    private void reissue(String role) {
        String refreshToken = Utils.getRefreshToken(null);
        UserService service = RetrofitClient.getRetrofitInstance().create(UserService.class);
        Call<Map<String, Object>> call = service.reissue("refresh=" + refreshToken);
        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, Object>> call, @NonNull Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    ExtractToken.SaveToken(response);
                    sendUserRole(role);
                }
                else {
                    // 실패 처리 (HTTP 상태 코드가 200-299 범위가 아닐 때)
                    int statusCode = response.code();
                    Log.e("HttpRequestError", "Reissue failed with status code: " + statusCode);
                    new AlertDialog.Builder(SelectRoleActivity.this)
                            .setTitle("오류 발생")
                            .setMessage("로그인 정보가 말소되어 재로그인이 필요합니다.")
                            .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(SelectRoleActivity.this, LoginActivity.class);
                                    startActivity(intent);
                                    finish(); // 현재 액티비티 종료
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, Object>> call, @NonNull Throwable t) {
                Log.e("NetworkError", "Error occurred: " + t.getMessage());
                new AlertDialog.Builder(SelectRoleActivity.this)
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