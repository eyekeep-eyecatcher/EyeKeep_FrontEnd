package com.example.eyekeep.DTO;


import java.util.List;

import lombok.Getter;

@Getter
public class ChildrenProtectionZoneDTO {
    private List<data> data;

    @Getter
    public static class data {
        private String name;
        private String latitude;
        private String longitude;
    }
}
