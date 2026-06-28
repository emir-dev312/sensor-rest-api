package com.amangay.sensor_rest_api.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OutdoorWeatherDto {
    
    private String city;
    private String temp; 
    private String condition;
    private String humidity;
    private String pressure;
    private String wind;
    
    private List<WeatherCardDto> hourlyCards; 
    private List<WeatherCardDto> pastCards;
    private List<WeatherCardDto> forecastCards;

    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WeatherCardDto {
        private String title;
        private String temp;
        private String day;
        private String month;
        private String time;
        private String condition; 
        private String humidity;  
        private String pressure;  
        private String wind;
        
        
        private List<WeatherCardDto> hourlyDetails; 
    }
}