package com.example.eyekeep.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookMarkDTO {
        private String alias;
        private String locationName;
        private double latitude;
        private double longitude;

        // 생성자 추가
        public BookMarkDTO(String locationName, double latitude, double longitude) {
                this.locationName = locationName;
                this.latitude = latitude;
                this.longitude = longitude;
        }
}
