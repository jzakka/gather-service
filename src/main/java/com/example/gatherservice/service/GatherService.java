package com.example.gatherservice.service;

import com.example.gatherservice.dto.GatherDto;
import com.example.gatherservice.dto.GatherMemberDto;

public interface GatherService {
    GatherDto createGather(GatherDto gatherDto);

    GatherDto getGatherByGatherId(String gatherId);

    GatherMemberDto joinGather(GatherMemberDto joinGatherDto);

    void cancelGather(String gatherId, String userId);

    void closeGather(String gatherId);
}
