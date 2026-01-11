package com.mistraltracker.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LiveObjectsMessage {

    private String id;
    private String streamId;
    private String timestamp;
    private String model;
    private Value value;
    private Metadata metadata;

}