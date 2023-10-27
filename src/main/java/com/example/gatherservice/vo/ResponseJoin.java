package com.example.gatherservice.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class ResponseJoin {
    private String gatherId;
    private String userId;
//    private Rule rule;
    private List<ResponseDateTime> selectDateTimes = new ArrayList<>();
}
