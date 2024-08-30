package com.example.eyekeep.DTO;

import java.util.List;

import lombok.Getter;

@Getter
public class ChildrenGuardHouseDTO {
    private List<data> data;

    @Getter
    public static class data {
        private String name;
        private String latitude;
        private String longitude;
    }
}
