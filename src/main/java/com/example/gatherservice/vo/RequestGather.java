package com.example.gatherservice.vo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class RequestGather {
    @NotNull(message = "모임 이름을 입력해주세요.")
    private String name;
    @NotNull(message = "방장 id를 입력해주세요.")
    private String userId;
    private String description;
    @NotNull
    LocalDate startDate;
    @NotNull
    LocalDate endDate;
    @NotNull
    LocalTime startTime;
    @NotNull
    LocalTime endTime;
    @NotNull
    LocalTime duration;
    @NotNull
    LocalDateTime deadLine;
}
