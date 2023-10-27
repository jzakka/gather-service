package com.example.gatherservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class ConfirmedGatherDto {
    private String gatherId;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private long memberCounts;
}
