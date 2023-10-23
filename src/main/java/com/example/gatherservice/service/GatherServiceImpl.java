package com.example.gatherservice.service;

import com.example.gatherservice.dto.GatherDto;
import com.example.gatherservice.dto.JoinGatherDto;
import com.example.gatherservice.entity.GatherEntity;
import com.example.gatherservice.repository.GatherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class GatherServiceImpl implements GatherService{
    private final GatherRepository gatherRepository;
    private final ModelMapper mapper;

    @Override
    public GatherDto createGather(GatherDto gatherDto) {
        gatherDto.setGatherId(UUID.randomUUID().toString());
        GatherEntity gather = mapper.map(gatherDto, GatherEntity.class);

        GatherEntity savedGather = gatherRepository.save(gather);

        GatherDto savedGatherDto = mapper.map(savedGather, GatherDto.class);
        return savedGatherDto;
    }

    @Override
    public GatherDto getGatherByGatherId(String gatherId) {
        return null;
    }

    @Override
    public JoinGatherDto joinGather(JoinGatherDto joinGatherDto) {
        return null;
    }

    @Override
    public void cancelGather(String gatherId, String userId) {

    }

    @Override
    public void closeGather(String gatherId) {

    }
}
