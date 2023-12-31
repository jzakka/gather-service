package com.example.gatherservice.dto;

import com.example.gatherservice.enums.GatherState;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class GatherDto {
    private String name;
    private String gatherId;
    private String userId;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private LocalTime duration;
    private LocalDateTime deadLine;
    private GatherState state;
}
