package com.example.eyekeep.DTO;

import java.util.List;

import lombok.Getter;

@Getter
public class PoliceOfficeDTO {
    private List<data> data;

    @Getter
    public static class data {
        private String officeName;
        private String latitude;
        private String longitude;
    }
}
