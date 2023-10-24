package com.example.gatherservice.service;

import com.example.gatherservice.dto.GatherDto;
import com.example.gatherservice.dto.GatherMemberDto;
import com.example.gatherservice.dto.SelectDateTimeDto;
import com.example.gatherservice.entity.GatherEntity;
import com.example.gatherservice.entity.GatherMemberEntity;
import com.example.gatherservice.repository.GatherMemberRepository;
import com.example.gatherservice.repository.GatherRepository;
import com.example.gatherservice.rule.GatherState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class GatherServiceImpl implements GatherService {
    private final GatherRepository gatherRepository;
    private final GatherMemberRepository gatherMemberRepository;
    private final ModelMapper mapper;
    private final Environment env;

    @Override
    public GatherDto createGather(GatherDto gatherDto) {
        validate(gatherDto);

        gatherDto.setState(GatherState.OPEN);
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
        GatherEntity gather = gatherRepository
                .findByGatherId(gatherId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, env.getProperty("gather.not-found-msg")));

        return mapper.map(gather, GatherDto.class);
    }

    @Override
    public GatherMemberDto joinGather(GatherMemberDto joinGatherDto) {
        GatherMemberEntity member = mapper.map(joinGatherDto, GatherMemberEntity.class);
        validate(joinGatherDto);

        GatherMemberEntity savedResult = gatherMemberRepository.save(member);

        return mapper.map(savedResult, GatherMemberDto.class);
    }

    private void validate(GatherMemberDto joinGatherDto) {
        /**
         * 사용자 선택 날짜, 시간은 모임의 시작 날짜, 시간보다 이를 수 없다.
         * 사용자 선택 날짜, 시간은 모임의 끝 날짜, 시간보다 늦을 수 없다.
         * 현재 시간이 모임 마감날짜보다 늦다면 참여가 불가능하다.
         */
        String errorMessage = null;

        GatherEntity gather = gatherRepository
                .findByGatherId(joinGatherDto.getGatherId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, env.getProperty("gather.not-found-msg")));

        for (SelectDateTimeDto selectDateTime : joinGatherDto.getSelectDateTimes()) {
            LocalDateTime startDateTime = selectDateTime.getStartDateTime();
            LocalDateTime endDateTime = selectDateTime.getEndDateTime();

            if (startDateTime.toLocalDate().isBefore(gather.getStartDate())
                    || startDateTime.toLocalTime().isBefore(gather.getStartTime())
                    || endDateTime.toLocalDate().isAfter(gather.getEndDate())
                    || endDateTime.toLocalTime().isAfter(gather.getEndTime())) {
                errorMessage = env.getProperty("select-time.validation.select-invalid-msg");
                break;
            } else if (!gather.getState().equals(GatherState.OPEN)) {
                errorMessage = env.getProperty("select-time.validation.deadline-msg");
                break;
            }
        }

        if (errorMessage != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errorMessage);
        }
    }

    @Override
    public void cancelGather(String gatherId, String userId) {

    }

    @Override
    public void closeGather(String gatherId) {
        GatherEntity gather = gatherRepository.findByGatherId(gatherId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, env.getProperty("gather.not-found-msg")));

        gather.setState(GatherState.CLOSED);

        gatherRepository.save(gather);
    }
}
