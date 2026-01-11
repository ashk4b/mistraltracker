package com.mistraltracker.repository;

import com.mistraltracker.model.WeatherData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WeatherDataRepository extends JpaRepository<WeatherData, Long> {

    Optional<WeatherData> findTopByOrderByTimestampDesc();

    List<WeatherData> findTop10ByOrderByTimestampDesc();
}