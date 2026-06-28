package com.amangay.sensor_rest_api.controller;

import com.amangay.sensor_rest_api.repository.SensorRepository;
import com.amangay.sensor_rest_api.DTO.SensorStats;
import com.amangay.sensor_rest_api.model.SensorData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;



@CrossOrigin(origins ="*")
@RestController
@RequestMapping("/api/sensor")
public class SensorController{

    @Autowired
    private SensorRepository repository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @PostMapping("/add")
    public ResponseEntity<String> add(@RequestBody SensorData data){

        repository.save(data);
        messagingTemplate.convertAndSend("/topic/sensor",data);
        return ResponseEntity.ok("Сокет пашет");
    }
    


    @GetMapping("/latest")
    public List<SensorData> getLatest(){
        SensorData last = repository.findTopByOrderByIdDesc().orElse(null);

        return last != null ? List.of(last) : List.of();
    }
    @GetMapping("/all")
    public List<SensorData> getAll(){
        return repository.findAll(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC,"createdAt"));
    }
    @GetMapping("/current")
    public SensorData getCurrent(){
        return repository.findByOrderByIdDesc();
    }
    @GetMapping("/stats")
    public List<SensorStats> getStats(){
        SensorStats stats = new SensorStats();
        Optional<SensorData> lastOptional = repository.findTopByOrderByIdDesc();

        if(lastOptional.isPresent()){
            SensorData last = lastOptional.get();
            stats.currentTemp = last.getTemperature();
            stats.currentHum = last.getHumidity();
            
        stats.minTemp = repository.getMinTemperature();
            stats.maxTemp = repository.getMaxTemperature();
            stats.avgTemp = repository.getAverageTemperature();
            
            stats.minHum = repository.getMinHumidity();
            stats.maxHum = repository.getMaxHumidity();
            stats.avgHum = repository.getAverageHumidity();

            if (stats.currentTemp > 30) stats.tempStatus = "Температура выше нормы!";
            else if (stats.currentTemp < 15) stats.tempStatus = "Температура ниже нормы!";
            
            if (stats.currentHum > 60) stats.humStatus = "Влажность выше нормы!";
            else if (stats.currentHum < 40) stats.humStatus = "Влажность ниже нормы!";
        }
        return lastOptional.isPresent() ? List.of(stats) : List.of();



    }



    



}