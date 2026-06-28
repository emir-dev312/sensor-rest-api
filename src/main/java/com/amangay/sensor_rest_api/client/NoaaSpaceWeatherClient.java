package com.amangay.sensor_rest_api.client;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import com.amangay.sensor_rest_api.DTO.SpaceWeatherDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class NoaaSpaceWeatherClient {

    private final RestTemplate restTemplate;

    private static final String SPACE_WEATHER_URL =
            "https://services.swpc.noaa.gov/products/noaa-planetary-k-index.json";

    public NoaaSpaceWeatherClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

  
    public SpaceWeatherDto getCurrentSpaceWeather() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<JsonNode> responseEntity = restTemplate.exchange(
                    SPACE_WEATHER_URL,
                    HttpMethod.GET,
                    entity,
                    JsonNode.class
            );

            JsonNode rootNode = responseEntity.getBody();

            if (rootNode != null && rootNode.isArray() && !rootNode.isEmpty()) {
                
                for (int i = rootNode.size() - 1; i >= 0; i--) {
                    JsonNode row = rootNode.get(i);
                    
                    
                    if (row != null && row.has("Kp")) {
                        JsonNode kpNode = row.get("Kp");
                        if (kpNode != null && !kpNode.isNull()) {
                            
                            double kpIndex = kpNode.asDouble();
                            return mapToDto(kpIndex);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Ошибка при получении данных NOAA: " + e.getMessage());
        }

        return new SpaceWeatherDto(0.0, "Данные недоступны", false);
    }

    
    public List<SpaceWeatherHistoryDto> getSpaceWeatherHistory() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<JsonNode> responseEntity = restTemplate.exchange(
                    SPACE_WEATHER_URL,
                    HttpMethod.GET,
                    entity,
                    JsonNode.class
            );

            JsonNode rootNode = responseEntity.getBody();

            if (rootNode != null && rootNode.isArray()) {
                List<SpaceWeatherHistoryDto> history = new ArrayList<>();

                
                for (int i = 0; i < rootNode.size(); i++) {
                    JsonNode row = rootNode.get(i);
                    
                    if (row == null || !row.has("time_tag") || !row.has("Kp")) continue;

                    JsonNode dateNode = row.get("time_tag");
                    JsonNode kpNode = row.get("Kp");

                    if (dateNode == null || dateNode.isNull() || kpNode == null || kpNode.isNull()) continue;

                    try {
                        SpaceWeatherHistoryDto dto = new SpaceWeatherHistoryDto();
                        
                        dto.setTimestamp(LocalDateTime.parse(dateNode.asText()));

                        double kpIndex = kpNode.asDouble();
                        dto.setKpIndex(kpIndex);
                        dto.setStatusText(getStormStatusText(kpIndex));
                        dto.setStorm(kpIndex >= 5.0);

                        history.add(dto);
                    } catch (Exception ignored) {}
                }
                return history;
            }
        } catch (Exception e) {
            System.err.println("Ошибка при получении истории NOAA: " + e.getMessage());
        }

        return Collections.emptyList();
    }

    

    private SpaceWeatherDto mapToDto(double kpIndex) {
        boolean isStorm = kpIndex >= 5.0;
        String statusText = getStormStatusText(kpIndex);
        return new SpaceWeatherDto(kpIndex, statusText, isStorm);
    }

    private String getStormStatusText(double kpIndex) {
        if (kpIndex < 4.0) return "Магнитное поле спокойно";
        if (kpIndex < 5.0) return "Магнитное поле возмущено";
        if (kpIndex < 6.0) return "Слабая буря (G1)";
        if (kpIndex < 7.0) return "Умеренная буря (G2)";
        if (kpIndex < 8.0) return "Сильная буря (G3)";
        return "Геомагнитный шторм (G4-G5)";
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SpaceWeatherHistoryDto {
        private LocalDateTime timestamp;
        private double kpIndex;
        private String statusText;
        private boolean isStorm;
    }
}