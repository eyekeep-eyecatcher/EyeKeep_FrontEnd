package com.example.eyekeep.service;

import com.example.eyekeep.DTO.AccidentBlackSpotDTO;
import com.example.eyekeep.DTO.CCTVDTO;
import com.example.eyekeep.DTO.ChildLocationDTO;
import com.example.eyekeep.DTO.ChildrenProtectionZoneDTO;
import com.example.eyekeep.DTO.PoliceOfficeDTO;
import com.example.eyekeep.DTO.RoadNodeDTO;
import com.example.eyekeep.DTO.SafetyEmergencyBellDTO;
import com.example.eyekeep.DTO.SecurityLightDTO;
import com.example.eyekeep.DTO.ChildrenGuardHouseDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface MapService {

    @GET("/request/police")
    Call<PoliceOfficeDTO> getPoliceStations(@Header("access") String accessToken);

    @GET("/request/security")
    Call<SecurityLightDTO> getSecurityLamps(@Header("access") String accessToken);

    @GET("/request/cctv")
    Call<CCTVDTO> getCCTV(@Header("access") String accessToken);

    @GET("/request/children/protection")
    Call<ChildrenProtectionZoneDTO> getChildrenProtectionZone(@Header("access") String accessToken);

    @GET("/request/safetybell")
    Call<SafetyEmergencyBellDTO> getSafetyEmergencyBell(@Header("access") String accessToken);

    @GET("/request/children/guardhouse")
    Call<ChildrenGuardHouseDTO> getChildrenGuardHouse(@Header("access") String accessToken);

    @GET("/request/accident/blackspot")
    Call<AccidentBlackSpotDTO> getAccidentBlackSpot(@Header("access") String accessToken);

    @POST("/save/location")
    Call<Void> updateLocation(@Header("access") String accessToken, @Body ChildLocationDTO childLocationDTO);

    @GET("/request/location/now")
    Call<ChildLocationDTO> getLocation(@Header("access") String accessToken);

    @POST("/find/path")
    Call<List<RoadNodeDTO>> findPath(@Header("access") String accessToken, @Body List<RoadNodeDTO> roadNodeDTOList);
}
