package com.example.eyekeep.service;

import com.example.eyekeep.DTO.SearchGeocodingDTO;
import com.example.eyekeep.DTO.SearchNaverDTO;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface SearchService {

    @GET("/v1/search/local.json")
    Call<SearchNaverDTO> searchNaver(@Header("X-Naver-Client-Id") String clientId,
                                     @Header("X-Naver-Client-Secret")String clientSecret,
                                     @Query("query") String query,
                                     @Query("display") Integer display,
                                     @Query("start") Integer start,
                                     @Query("sort") String sort);

    @GET("/map-geocode/v2/geocode")
    Call<SearchGeocodingDTO> searchAddress(@Header("X-NCP-APIGW-API-KEY-ID") String APIId,
                                           @Header("X-NCP-APIGW-API-KEY") String APIKey,
                                           @Query("query") String query,
                                           @Query("count") Number count);
}
