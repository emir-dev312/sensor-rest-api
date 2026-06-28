package com.amangay.sensor_rest_api.client;

import com.amangay.sensor_rest_api.DTO.OutdoorWeatherDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class OpenMeteoClient {

    private final RestTemplate restTemplate;
    private static final String WEATHER_API_URL = "https://api.open-meteo.com/v1/forecast";

    public OpenMeteoClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public OutdoorWeatherDto getCurrentWeather() {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(WEATHER_API_URL)
                    .queryParam("latitude", 42.87)
                    .queryParam("longitude", 74.59)
                    .queryParam("current", "temperature_2m,relative_humidity_2m,wind_speed_10m,surface_pressure,weather_code")
                    .queryParam("hourly", "temperature_2m,weather_code") 
                    .queryParam("daily", "temperature_2m_max,weather_code,relative_humidity_2m_max,wind_speed_10m_max,surface_pressure_max")
                    .queryParam("past_days", 7)
                    .queryParam("forecast_days", 8)
                    .queryParam("timezone", "Asia/Bishkek")
                    .toUriString();

            OpenMeteoResponse response = restTemplate.getForObject(url, OpenMeteoResponse.class);
            if (response != null) return mapToDto(response);
            
        } catch (Exception e) {
            System.err.println("Ошибка маппинга: " + e.getMessage());
            e.printStackTrace();
        }
        return new OutdoorWeatherDto("ОШИБКА", "0", "Неизвестно", "0", "0", "0", new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }

    private OutdoorWeatherDto mapToDto(OpenMeteoResponse response) {
        OutdoorWeatherDto dto = new OutdoorWeatherDto();
        dto.setCity("Бишкек,\nКыргызстан");

        CurrentWeather cur = response.getCurrent();
        dto.setTemp(String.valueOf(Math.round(cur.temp)));
        dto.setCondition(translateWeatherCode(cur.weatherCode));
        dto.setHumidity(cur.humidity + "%");
        dto.setPressure(Math.round(cur.pressure * 0.75006) + " мм");
        dto.setWind(Math.round(cur.wind) + " km/h");

        LocalDate today = LocalDate.now();
        DailyWeather daily = response.getDaily();
        
        List<OutdoorWeatherDto.WeatherCardDto> past = new ArrayList<>();
        List<OutdoorWeatherDto.WeatherCardDto> forecast = new ArrayList<>();

        if (daily != null && daily.time != null) {
            for (int i = 0; i < daily.time.size(); i++) {
                LocalDate date = LocalDate.parse(daily.time.get(i));
                
                
                List<OutdoorWeatherDto.WeatherCardDto> hoursForThisDay = getHourlyForDay(response, i, today);

                
                OutdoorWeatherDto.WeatherCardDto dayCard = new OutdoorWeatherDto.WeatherCardDto(
                    generateTitle(date, today),
                    String.valueOf(Math.round(daily.tempMax.get(i))),
                    String.valueOf(date.getDayOfMonth()),
                    date.format(DateTimeFormatter.ofPattern("MMM", new Locale("ru"))).replace(".", ""),
                    "12:00",
                    translateWeatherCode(daily.weatherCodes.get(i)),
                    daily.humidityMax.get(i) + "%",
                    Math.round(daily.pressureMax.get(i) * 0.75006) + " мм",
                    Math.round(daily.windMax.get(i)) + " km/h",
                    hoursForThisDay 
                );

                if (date.isBefore(today)) past.add(dayCard);
                else forecast.add(dayCard);
                
                
                if (date.isEqual(today)) {
                    dto.setHourlyCards(hoursForThisDay);
                }
            }
        }

        dto.setPastCards(past);
        dto.setForecastCards(forecast);
        return dto;
    }

    
    private List<OutdoorWeatherDto.WeatherCardDto> getHourlyForDay(OpenMeteoResponse resp, int dayIndex, LocalDate today) {
        List<OutdoorWeatherDto.WeatherCardDto> hourlyList = new ArrayList<>();
        HourlyWeather h = resp.getHourly();
        if (h == null || h.time == null) return hourlyList;

        int startOffset = dayIndex * 24; 
        for (int j = startOffset; j < startOffset + 24; j++) {
            if (j >= h.time.size()) break;

            LocalDateTime dt = LocalDateTime.parse(h.time.get(j));
            if (dt.getHour() % 2 == 0) { 
                hourlyList.add(new OutdoorWeatherDto.WeatherCardDto(
                    generateTitle(dt.toLocalDate(), today),
                    String.valueOf(Math.round(h.temp.get(j))),
                    String.valueOf(dt.getDayOfMonth()),
                    dt.format(DateTimeFormatter.ofPattern("MMM", new Locale("ru"))).replace(".", ""),
                    dt.format(DateTimeFormatter.ofPattern("HH:mm")),
                    translateWeatherCode(h.weatherCodes.get(j)),
                    "0%", "0 мм", "0 km/h", 
                    null 
                ));
            }
        }
        return hourlyList;
    }

    private String translateWeatherCode(int code) {
        if (code == 0) return "Ясно";
        if (code >= 1 && code <= 3) return "Облачно";
        if (code >= 45 && code <= 48) return "Туман";
        if (code >= 51 && code <= 67) return "Дождь";
        if (code >= 71 && code <= 77) return "Снег";
        if (code >= 80 && code <= 82) return "Ливень";
        if (code >= 95) return "Гроза";
        return "Ясно"; 
    }

    private String generateTitle(LocalDate date, LocalDate today) {
        if (date.isEqual(today)) return "Сегодня";
        if (date.isEqual(today.plusDays(1))) return "Завтра";
        if (date.isEqual(today.minusDays(1))) return "Вчера";
        String weekday = date.format(DateTimeFormatter.ofPattern("EEEE", new Locale("ru")));
        return weekday.substring(0, 1).toUpperCase() + weekday.substring(1);
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OpenMeteoResponse {
        private CurrentWeather current;
        private DailyWeather daily;
        private HourlyWeather hourly;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CurrentWeather {
        @JsonProperty("temperature_2m") private double temp;
        @JsonProperty("relative_humidity_2m") private int humidity;
        @JsonProperty("wind_speed_10m") private double wind;
        @JsonProperty("surface_pressure") private double pressure;
        @JsonProperty("weather_code") private int weatherCode;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class HourlyWeather {
        private List<String> time;
        @JsonProperty("temperature_2m") private List<Double> temp;
        @JsonProperty("weather_code") private List<Integer> weatherCodes;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DailyWeather {
        private List<String> time;
        @JsonProperty("temperature_2m_max") private List<Double> tempMax;
        @JsonProperty("weather_code") private List<Integer> weatherCodes;
        @JsonProperty("relative_humidity_2m_max") private List<Integer> humidityMax;
        @JsonProperty("wind_speed_10m_max") private List<Double> windMax;
        @JsonProperty("surface_pressure_max") private List<Double> pressureMax;
    }
}