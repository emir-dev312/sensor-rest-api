package com.amangay.sensor_rest_api.service;

import org.springframework.stereotype.Service;
import com.amangay.sensor_rest_api.DTO.EnvironmentDashboardDto;
import com.amangay.sensor_rest_api.client.NoaaSpaceWeatherClient;
import com.amangay.sensor_rest_api.client.OpenMeteoClient;
import com.amangay.sensor_rest_api.model.SensorData;
import com.amangay.sensor_rest_api.repository.SensorRepository;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class DashboardAggregationService {

    private final OpenMeteoClient weatherClient;
    private final NoaaSpaceWeatherClient spaceWeatherClient;
    private final SensorRepository sensorRepository;

    public DashboardAggregationService(OpenMeteoClient weatherClient, 
                                    NoaaSpaceWeatherClient spaceWeatherClient, 
                                    SensorRepository sensorRepository) {
        this.weatherClient = weatherClient;
        this.spaceWeatherClient = spaceWeatherClient;
        this.sensorRepository = sensorRepository;
    }

    public EnvironmentDashboardDto getFullDashboard() {
        EnvironmentDashboardDto dashboard = new EnvironmentDashboardDto();

        dashboard.setOutdoor(weatherClient.getCurrentWeather());

        dashboard.setSpaceWeather(spaceWeatherClient.getCurrentSpaceWeather());

        Optional<SensorData> optionalSensor = sensorRepository.findTopByOrderByIdDesc();
        SensorData latestIndoorStats;

        if (optionalSensor.isPresent()) {
            latestIndoorStats = optionalSensor.get();
        } else {
            
            latestIndoorStats = new SensorData();
            
            
            latestIndoorStats.setId(1L);
            latestIndoorStats.setTemperature(23.5); 
            latestIndoorStats.setHumidity(45.0);    
            latestIndoorStats.setCreatedAt(LocalDateTime.now());
        }
        
        dashboard.setIndoor(latestIndoorStats);

        
        dashboard.setGeneratedAt(LocalDateTime.now());

        return dashboard;
    }
}