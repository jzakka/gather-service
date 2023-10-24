package com.example.gatherservice.service;

import com.example.gatherservice.dto.GatherDto;

public interface GatherService {
    GatherDto createGather(GatherDto gatherDto);

    GatherDto getGatherByGatherId(String gatherId);

    void closeGather(String gatherId);
}
