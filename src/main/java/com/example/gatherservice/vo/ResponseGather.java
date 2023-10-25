package com.example.gatherservice.vo;

import com.example.gatherservice.enums.GatherState;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class ResponseGather {
    private String gatherId;
    private String name;
    private String userId;
    private String description;
    LocalDate startDate;
    LocalDate endDate;
    LocalTime startTime;
    LocalTime endTime;
    LocalTime duration;
    LocalDateTime deadLine;
    private GatherState state;
}
