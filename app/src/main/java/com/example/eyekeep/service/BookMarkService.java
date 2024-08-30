package com.example.eyekeep.service;

import com.example.eyekeep.DTO.BookMarkDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface BookMarkService {
    @GET("/request/bookmark")
    Call<List<BookMarkDTO>> getBookMarkList(@Header("access") String accessToken);

    @POST("/save/bookmark")
    Call<BookMarkDTO> saveBookMark(@Header("access") String accessToken, @Body BookMarkDTO bookMarkDTO);

    @POST("/delete/bookmark")
    Call<Void> deleteBookMark(@Header("access") String accessToken, @Body BookMarkDTO bookMarkDTO);

    @POST("/set/alias")
    Call<BookMarkDTO> setAlias(@Header("access") String accessToken, @Body BookMarkDTO bookMarkDTO);
}
