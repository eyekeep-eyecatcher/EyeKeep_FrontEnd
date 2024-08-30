package com.example.eyekeep.retrofit;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String Base_URL = "http://192.168.219.112:8080";
    private static final String NaverSearchURL = "https://openapi.naver.com";
    private static final String NaverGeocodingURL = "https://naveropenapi.apigw.ntruss.com";
    private static Retrofit retrofit = null;

    public static Retrofit getRetrofitInstance() {
        retrofit = new Retrofit.Builder()
                .baseUrl(Base_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        return retrofit;
    }

    public static Retrofit getRetrofitInstanceSearchNaver() {
        retrofit = new Retrofit.Builder()
                .baseUrl(NaverSearchURL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        return retrofit;
    }

    public static Retrofit getRetrofitInstanceSearchGeocoding() {
        retrofit = new Retrofit.Builder()
                .baseUrl(NaverGeocodingURL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        return retrofit;
    }


}
