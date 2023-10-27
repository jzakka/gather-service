package com.example.gatherservice.service;

import com.example.gatherservice.dto.ConfirmedGatherDto;
import com.example.gatherservice.dto.GatherDto;
import com.example.gatherservice.vo.ResponseJoin;

import java.util.List;

public interface GatherService {
    GatherDto createGather(GatherDto gatherDto);

    GatherDto getGatherByGatherId(String gatherId);

    void closeGather(String gatherId);

    /**
     * 모임에 참여한 사람들의 참여시간을 종합해서
     * 가장 사람들이 많이 참여 가능한 시간대를 모임시간으로 설정
     */
    List<ConfirmedGatherDto> confirmTime(String gatherId);
}
