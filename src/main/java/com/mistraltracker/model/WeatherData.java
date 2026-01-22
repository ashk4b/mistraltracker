package com.mistraltracker.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "weather_data")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeatherData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime timestamp;
    private Double temperature;
    private Double humidity;
    private Double windSpeed;
    private Double windDirection;
    private Double lightIntensity;
    private Double rainLevel;
    private Double uvIntensity;
    private Double pressure;
    private String deviceEui;
    private Integer batteryLevel;
    private Double rainScore;
    private Double windScore;
    private Double uvScore;
    private Double fogScore;
    private boolean goodForWaterSports;
    private boolean goodForSwimming;
    private boolean goodForFishing;
    private boolean goodForBoating;
}