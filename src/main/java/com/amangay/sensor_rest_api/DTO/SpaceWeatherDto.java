package com.amangay.sensor_rest_api.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpaceWeatherDto {
    private double kpIndex;
    private String statusText;
    private boolean isStormNow;
}
