package com.amangay.sensor_rest_api.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.amangay.sensor_rest_api.DTO.EnvironmentDashboardDto;
import com.amangay.sensor_rest_api.DTO.OutdoorWeatherDto;
import com.amangay.sensor_rest_api.service.DashboardAggregationService;
import com.amangay.sensor_rest_api.client.OpenMeteoClient;

@RestController 
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*") 
public class DashboardController {

    private final DashboardAggregationService dashboardService;
    
    
    private final OpenMeteoClient weatherClient; 

    
    public DashboardController(DashboardAggregationService dashboardService, OpenMeteoClient weatherClient) {
        this.dashboardService = dashboardService;
        this.weatherClient = weatherClient;
    }

    
    @GetMapping("/current")
    public EnvironmentDashboardDto getCurrentDashboard() {
        return dashboardService.getFullDashboard();
    }

    
    @GetMapping("/weather")
    public OutdoorWeatherDto getOnlyWeather() {
        return weatherClient.getCurrentWeather();
    }
}