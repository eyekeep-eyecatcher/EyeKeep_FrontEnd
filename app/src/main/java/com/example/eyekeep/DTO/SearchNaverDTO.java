package com.example.eyekeep.DTO;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import lombok.Getter;

@Getter
public class SearchNaverDTO {
    @SerializedName("total")
    private Integer display;
    @SerializedName("items")
    private List<Item> items;

    @Getter
    public static class Item {
        @SerializedName("title")
        private String title;
        //@SerializedName("link")
        //private String link;
        @SerializedName("category")
        private String category;
        @SerializedName("address")
        private String address;
        @SerializedName("roadAddress")
        private String roadAddress;
        @SerializedName("mapx")
        private String mapx;
        @SerializedName("mapy")
        private String mapy;
    }
}