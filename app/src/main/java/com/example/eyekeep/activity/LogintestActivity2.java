package com.example.eyekeep.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.eyekeep.DTO.JoinDTO;
import com.example.eyekeep.R;
import com.example.eyekeep.retrofit.RetrofitClient;
import com.example.eyekeep.service.UserService;

import java.util.Map;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LogintestActivity2 extends AppCompatActivity {
    private EditText etEmail, etPassword, etPasswordCheck, etNickName;
    private TextView wrongEmail, wrongPassword, wrongPasswordCheck, wrongNickname;
    private AppCompatButton btnRegister;
    private ImageButton btngoback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_logintest2);

        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etPasswordCheck = findViewById(R.id.et_password_check);
        etNickName = findViewById(R.id.et_nickname);
        btnRegister = findViewById(R.id.btn_register);
        wrongEmail = findViewById(R.id.worng_email);
        wrongPassword = findViewById(R.id.worng_password);
        wrongPasswordCheck = findViewById(R.id.worng_password_check);
        wrongNickname = findViewById(R.id.worng_nickname);
        btngoback = findViewById(R.id.btn_go_back);
        btngoback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 로그인 화면으로 이동
                Intent intent = new Intent(LogintestActivity2.this, LogintestActivity.class);
                startActivity(intent);
            }
        });

        btnRegister.setOnClickListener(view -> registerUser());



        // 이메일 입력 필드 TextWatcher
        etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String email = etEmail.getText().toString();
                if (!TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    wrongEmail.setVisibility(View.GONE);
                } else {
                    wrongEmail.setText("올바른 이메일 형식이 아닙니다.");
                    wrongEmail.setVisibility(View.VISIBLE);
                }
                checkAllFields();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // 비밀번호 입력 필드 TextWatcher
        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String password = etPassword.getText().toString();
                if (isPasswordValid(password)) {
                    wrongPassword.setVisibility(View.GONE);
                } else {
                    wrongPassword.setText("대문자, 소문자, 숫자, 특수문자 포함된 8자리 이상.");
                    wrongPassword.setVisibility(View.VISIBLE);
                }
                checkAllFields();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // 비밀번호 확인 입력 필드 TextWatcher
        etPasswordCheck.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String password = etPassword.getText().toString();
                String confirmPassword = etPasswordCheck.getText().toString();
                if (password.equals(confirmPassword) && !password.isEmpty()) {
                    wrongPasswordCheck.setVisibility(View.GONE);
                } else {
                    wrongPasswordCheck.setText("비밀번호가 일치하지 않습니다.");
                    wrongPasswordCheck.setVisibility(View.VISIBLE);
                }
                checkAllFields();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // 닉네임 입력 필드 TextWatcher
        etNickName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String nickname = etNickName.getText().toString();
                if (isNicknameValid(nickname)) {
                    wrongNickname.setVisibility(View.GONE);
                } else {
                    wrongNickname.setText("올바른 닉네임이 아닙니다.\n공백, 특수 문자는 사용할 수 없으며, 3~15자리로 만들어 주세요.");
                    wrongNickname.setVisibility(View.VISIBLE);
                }
                checkAllFields();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }






    private void registerUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String passwordCheck = etPasswordCheck.getText().toString().trim();
        String nickname = etNickName.getText().toString().trim();
        JoinDTO newUser = new JoinDTO(email, password, passwordCheck, nickname);

        UserService service = RetrofitClient.getRetrofitInstance().create(UserService.class);
        Call<Map<String, Object>> call = service.registerUser(newUser);
        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, Object>> call, @NonNull Response<Map<String, Object>> response) {
                if (response.isSuccessful()){
                    String message = response.body().get("message").toString();
                    switch (message) {
                        case "Success" :
                            Toast.makeText(LogintestActivity2.this, "회원가입 성공", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LogintestActivity2.this, LogintestActivity.class);
                            startActivity(intent);
                            break;

                        case "Email" :
                            // 여기다 실패 코드 채워넣으면 댐.
                            wrongEmail.setText("중복된 이메일 입니다.");
                            wrongEmail.setVisibility(View.VISIBLE);
                            break;

                        case "NickName" :
                            // 여기다 실패 코드 채워넣으면 댐.
                            wrongNickname.setText("중복된 닉네임 입니다.");
                            wrongNickname.setVisibility(View.VISIBLE);
                            break;

                        case "Password" :
                            // 여기다 실패 코드 채워넣으면 댐.
                            wrongPassword.setText("올바른 비밀번호 형식이 아닙니다.\n대문자, 소문자, 숫자, 특수문자가 들어간 형식을 8자리 이상으로 만들어 주세요.");
                            wrongPassword.setVisibility(View.VISIBLE);
                            break;
                        default: break;
                    }

                }
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, Object>> call, @NonNull Throwable t) {
                Log.e("NetworkError", "Error occurred: " + t.getMessage());
                Toast.makeText(LogintestActivity2.this, "네트워크 에러", Toast.LENGTH_SHORT).show();
                new AlertDialog.Builder(LogintestActivity2.this)
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

    private void checkAllFields() {
        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();
        String confirmPassword = etPasswordCheck.getText().toString();
        String nickname = etNickName.getText().toString();

        boolean isEmailValid = !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
        boolean isPasswordValid = isPasswordValid(password) && password.equals(confirmPassword);
        boolean isNicknameValid = isNicknameValid(nickname);

        btnRegister.setEnabled(isEmailValid && isPasswordValid && isNicknameValid);
    }
    // 비밀번호 검증 메서드
    private boolean isPasswordValid(String password) {
        if (password.length() < 8) return false;

        String upperCaseChars = "(.*[A-Z].*)";
        String lowerCaseChars = "(.*[a-z].*)";
        String numbers = "(.*[0-9].*)";
        String specialChars = "(.*[!@#$%^&*].*)";

        if (!password.matches(upperCaseChars) ||
                !password.matches(lowerCaseChars) ||
                !password.matches(numbers) ||
                !password.matches(specialChars)) {
            return false;
        }

        if (hasRepeatedChars(password) || isSequential(password)) {
            return false;
        }

        return true;
    }

    // 반복 문자 검증
    private boolean hasRepeatedChars(String password) {
        for (int i = 0; i < password.length() - 2; i++) {
            if (password.charAt(i) == password.charAt(i + 1) && password.charAt(i) == password.charAt(i + 2)) {
                return true;
            }
        }
        return false;
    }

    // 순차 문자 검증
    private boolean isSequential(String password) {
        for (int i = 0; i < password.length() - 2; i++) {
            char c1 = password.charAt(i);
            char c2 = password.charAt(i + 1);
            char c3 = password.charAt(i + 2);

            if ((c2 == c1 + 1 && c3 == c2 + 1) || (c2 == c1 - 1 && c3 == c2 - 1)) {
                return true;
            }
        }
        return false;
    }

    // 닉네임 검증 메서드
    private boolean isNicknameValid(String nickname) {
        // 길이 검증 (최소 3자, 최대 15자)
        if (nickname.length() < 3 || nickname.length() > 15) {
            return false;
        }

        // 영문 대소문자, 숫자, 밑줄, 하이픈만 허용
        String nicknamePattern = "^[a-zA-Z0-9_-]+$";
        if (!Pattern.matches(nicknamePattern, nickname)) {
            return false;
        }

        // 공백 금지
        if (nickname.contains(" ")) {
            return false;
        }

        // 중복 닉네임 검증 (서버와의 통신이 필요함, 여기선 생략)
        // 예를 들어, 서버 API를 호출해 닉네임 중복 여부를 확인할 수 있음
        // if (isNicknameDuplicate(nickname)) {
        //     return false;
        // }

        return true;
    }
}