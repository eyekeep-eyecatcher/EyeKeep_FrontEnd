package com.example.eyekeep.DTO;

import java.util.List;

import lombok.Getter;

@Getter
public class CCTVDTO {
    private List<data> data;

    @Getter
    public static class data {
        private String address;
        private String latitude;
        private String longitude;
    }
}