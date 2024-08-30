package com.example.eyekeep.retrofit;

import static com.example.eyekeep.BuildConfig.BASE_URL;
import static com.example.eyekeep.BuildConfig.NAVER_GEOCODING_URL;
import static com.example.eyekeep.BuildConfig.NAVER_SEARCH_URL;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String Base_URL = BASE_URL;
    private static final String NaverSearchURL = NAVER_SEARCH_URL;
    private static final String NaverGeocodingURL = NAVER_GEOCODING_URL;
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
