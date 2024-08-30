package com.example.eyekeep.DTO;

import java.util.List;

import lombok.Getter;

@Getter
public class AccidentBlackSpotDTO {
    private List<data> data;

    @Getter
    public static class data {
        private String location;
        private String latitude;
        private String longitude;
        private String accidentType;
    }
}