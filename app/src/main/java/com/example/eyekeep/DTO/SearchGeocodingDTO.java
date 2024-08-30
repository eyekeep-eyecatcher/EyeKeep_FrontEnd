package com.example.eyekeep.DTO;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import lombok.Getter;

@Getter
public class SearchGeocodingDTO {
    @SerializedName("status")
    private String status;

    @SerializedName("addresses")
    private List<addressList> addresses;

    @Getter
    public static class addressList {
        @SerializedName("roadAddress")
        private String roadAddress;
        @SerializedName("jibunAddress")
        private String jibunAddress;
        //@SerializedName("englishAddress")
        //private String englishAddress;
        @SerializedName("x")
        private String longitude;
        @SerializedName("y")
        private String latitude;
    }

    @SerializedName("errorMessage")
    private String errorMessage;
}
