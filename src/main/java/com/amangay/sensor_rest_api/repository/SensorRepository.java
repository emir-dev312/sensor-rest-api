package com.amangay.sensor_rest_api.repository;

import com.amangay.sensor_rest_api.model.SensorData;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SensorRepository extends JpaRepository<SensorData, Long> {
    
    SensorData findByOrderByIdDesc();

    Optional<SensorData> findTopByOrderByIdDesc();

    @Query("SELECT AVG(s.temperature) FROM SensorData s")
    Double getAverageTemperature();

    @Query("SELECT MIN(s.temperature) FROM SensorData s")
    Double getMinTemperature();

    @Query("SELECT MAX(s.temperature) FROM SensorData s")
    Double getMaxTemperature();

    @Query("SELECT AVG(s.humidity) FROM SensorData s")
    Double getAverageHumidity();

    @Query("SELECT MIN(s.humidity) FROM SensorData s")
    Double getMinHumidity();

    @Query("SELECT MAX(s.humidity) FROM SensorData s")
    Double getMaxHumidity();
}