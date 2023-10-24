package com.example.gatherservice.dto;

import com.example.gatherservice.rule.Rule;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class GatherMemberDto {
    private String gatherId;
    private String userId;
    private Rule rule;
    private List<SelectDateTimeDto> selectDateTimes = new ArrayList<>();
}
