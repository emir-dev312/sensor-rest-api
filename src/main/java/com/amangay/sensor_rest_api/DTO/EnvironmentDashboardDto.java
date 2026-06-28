package com.amangay.sensor_rest_api.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.time.LocalDateTime;

import com.amangay.sensor_rest_api.model.SensorData;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnvironmentDashboardDto {
    private SensorData indoor;
    private OutdoorWeatherDto outdoor;
    private SpaceWeatherDto spaceWeather;
    private LocalDateTime generatedAt;
}