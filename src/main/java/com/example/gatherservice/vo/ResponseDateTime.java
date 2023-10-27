package com.example.gatherservice.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseDateTime {
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
}
