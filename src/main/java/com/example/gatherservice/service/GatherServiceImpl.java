package com.example.gatherservice.service;

import com.example.gatherservice.dto.GatherDto;
import com.example.gatherservice.dto.JoinGatherDto;
import com.example.gatherservice.entity.GatherEntity;
import com.example.gatherservice.repository.GatherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class GatherServiceImpl implements GatherService {
    private final GatherRepository gatherRepository;
    private final ModelMapper mapper;
    private final Environment env;

    @Override
    public GatherDto createGather(GatherDto gatherDto) {
        validate(gatherDto);

        gatherDto.setGatherId(UUID.randomUUID().toString());
        GatherEntity gather = mapper.map(gatherDto, GatherEntity.class);

        GatherEntity savedGather = gatherRepository.save(gather);

        GatherDto savedGatherDto = mapper.map(savedGather, GatherDto.class);
        return savedGatherDto;
    }

    private void validate(GatherDto gatherDto) {
        /**
         * 모임 검증
         *
         * 모임 시작날짜가 끝날짜보다 앞서야함
         * 모임 시작시간이 끝시간보다 앞서야함 (정오를 걸치는 모임을 생성할 수 없음)
         * 모임 시간이 시작시간 ~ 끝시간보다 작아야함
         * 모임 마감일자가 모임 시간시간보다 하루 이상
         */
        String errorMessage = null;
        LocalDateTime startDateTime = LocalDateTime.of(gatherDto.getStartDate(), gatherDto.getStartTime());
        long hoursToMillis = gatherDto.getDuration().getHour() * 60 * 60 * 1000;
        long minutesToMillis = gatherDto.getDuration().getMinute() * 60 * 1000;
        long secondsToMillis = gatherDto.getDuration().getSecond() * 1000;
        long durationTimeStamp = hoursToMillis + minutesToMillis + secondsToMillis;

        if (gatherDto.getStartDate().isAfter(gatherDto.getEndDate())) {
            errorMessage = env.getProperty("gather.validation.date-invalid-msg");
        } else if (gatherDto.getStartTime().isAfter(gatherDto.getEndTime())) {
            errorMessage = env.getProperty("gather.validation.time-invalid-msg");
        } else if (durationTimeStamp > Duration.between(gatherDto.getStartTime(), gatherDto.getEndTime()).toMillis()) {
            errorMessage = env.getProperty("gather.validation.duration-invalid-msg");
        } else if (!gatherDto.getDeadLine().plusDays(1).isBefore(startDateTime)) {
            errorMessage = env.getProperty("gather.validation.deadline-invalid-msg");
        }

        if (errorMessage != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errorMessage);
        }
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
