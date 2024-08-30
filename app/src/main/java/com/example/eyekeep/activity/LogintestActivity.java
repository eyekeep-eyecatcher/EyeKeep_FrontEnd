package com.example.eyekeep.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.eyekeep.DTO.LoginDTO;
import com.example.eyekeep.MainChildActivity;
import com.example.eyekeep.MainParentActivity;
import com.example.eyekeep.R;
import com.example.eyekeep.request.RequestAccessIsActive;
import com.example.eyekeep.request.RequestFCMToken;
import com.example.eyekeep.retrofit.RetrofitClient;
import com.example.eyekeep.service.ExtractToken;
import com.example.eyekeep.repository.Utils;
import com.example.eyekeep.service.UserService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LogintestActivity extends AppCompatActivity {
    private EditText etEmail, etPassword;
    private AppCompatButton btnLogin, btnRegister;
    private TextView worngEmailPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_logintest);

        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        worngEmailPassword = findViewById(R.id.worng_email_password);

        btnLogin = findViewById(R.id.btn_login);
        btnRegister = findViewById(R.id.btn_register);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 회원가입 화면으로 이동
                Intent intent = new Intent(LogintestActivity.this, LogintestActivity2.class);
                startActivity(intent);
            }
        });

        btnLogin.setOnClickListener(view -> loginUser());

    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        LoginDTO newUser = new LoginDTO(email, password);

        UserService service = RetrofitClient.getRetrofitInstance().create(UserService.class);
        Call<Map<String, Object>> call = service.loginUser(newUser);
        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, Object>> call, @NonNull Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    ExtractToken.SaveToken(response);

                    // Send FCM Token to Server
                    FirebaseMessaging.getInstance().getToken()
                            .addOnCompleteListener(new OnCompleteListener<String>() {
                                @Override
                                public void onComplete(@NonNull Task<String> task) {
                                    if (!task.isSuccessful()) {
                                        Log.w("FCM", "Fetching FCM registration token failed", task.getException());
                                        return;
                                    }

                                    // Get new FCM registration token
                                    String token = task.getResult();
                                    RequestAccessIsActive.checkAccessToken();
                                    RequestFCMToken.saveFCMToken(token);
                                    Log.d("FCM", "FCM Token: " + token);
                                }
                            });

                    checkTokenRole();
                } else {
                    worngEmailPassword.setVisibility(View.VISIBLE);
                    Toast.makeText(LogintestActivity.this, "로그인 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, Object>> call, @NonNull Throwable t) {
                Log.e("NetworkError", "Error occurred: " + t.getMessage());
                new AlertDialog.Builder(LogintestActivity.this)
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
                        Intent intent = new Intent(LogintestActivity.this, MainParentActivity.class);
                        startActivity(intent);
                        finish(); //현재 액티비티 파괴
                    }
                    else if (role.equals("Child")) {
                        Intent intent = new Intent(LogintestActivity.this, MainChildActivity.class);
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
                        Intent intent = new Intent(LogintestActivity.this, AuthorityActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, Object>> call, @NonNull Throwable t) {
                Log.e("NetworkError", "Error occurred: " + t.getMessage());
                new AlertDialog.Builder(LogintestActivity.this)
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
                    // 토큰에 문제가 있기에 저장된 토큰 삭제.
                    Utils.clearToken();
                    Toast.makeText(LogintestActivity.this, "로그인 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, Object>> call, @NonNull Throwable t) {
                Log.e("NetworkError", "Error occurred: " + t.getMessage());
                new AlertDialog.Builder(LogintestActivity.this)
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