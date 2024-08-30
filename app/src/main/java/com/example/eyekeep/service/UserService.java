package com.example.eyekeep.service;

import com.example.eyekeep.DTO.JoinDTO;
import com.example.eyekeep.DTO.LoginDTO;
import com.example.eyekeep.DTO.RoleDTO;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface UserService {
    @POST("/signup")
    Call<Map<String, Object>> registerUser(@Body JoinDTO user);

    @POST("/signin")
    Call<Map<String, Object>> loginUser(@Body LoginDTO user);

    @GET("/reissue")
    Call<Map<String, Object>> reissue(@Header("Cookie") String refreshToken);

    @POST("/signout")
    Call<Map<String, Object>> signout(@Header("Cookie") String refreshToken);

    @POST("/role")
    Call<Map<String, Object>> role(@Header("access") String accessToken, @Body RoleDTO roleDTO);

    @POST("/check/access")
    Call<Map<String, Object>> checkAccess(@Header("access") String accessToken);

    @POST("/check/role")
    Call<Map<String, Object>> checkRole(@Header("access") String accessToken);

    @POST("/fcm/token")
    Call<Void> saveFCM(@Header("access") String accessToken, @Query("fcmToken") String fcmToken);

    @POST("/fcm/request")
    Call<Void> requestEyeKeep(@Header("access") String accessToken, @Query("email") String email);

    @POST("/fcm/accept")
    Call<Void> acceptEyeKeep(@Header("access") String accessToken, @Query("email") String email);

    @POST("/fcm/emergency")
    Call<Void> sendEmergencySituation(@Header("access") String accessToken);
}
