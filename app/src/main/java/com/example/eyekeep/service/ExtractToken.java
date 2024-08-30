package com.example.eyekeep.service;

import com.example.eyekeep.repository.Utils;

import java.util.Map;

import retrofit2.Response;

public class ExtractToken {

    public static void SaveToken(Response<Map<String, Object>> response){
        // 액세스 토큰 추출 (Bearer 제거)
        String accessTokenValue = response.headers().get("access");
        if (accessTokenValue != null && accessTokenValue.startsWith("Bearer ")) {
            accessTokenValue = accessTokenValue.substring(7); // "Bearer " 문자열 이후부터 시작
        }
        Utils.setAccessToken(accessTokenValue);

        // 응답에서 쿠키 추출
        String cookieHeader = response.headers().get("Set-Cookie");
        if (cookieHeader != null) {
            String[] cookies = cookieHeader.split(";"); // 쿠키는 세미콜론으로 분리된 문자열
            for (String cookie : cookies) {
                if (cookie.trim().startsWith("refresh=")) {
                    String refreshTokenValue = cookie.substring("refresh=".length());
                    Utils.setRefreshToken(refreshTokenValue);
                    break;
                }
            }
        }
    }
}
