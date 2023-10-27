package com.example.gatherservice.service;

import com.example.gatherservice.client.JoinServiceClient;
import com.example.gatherservice.dto.ConfirmedGatherDto;
import com.example.gatherservice.dto.GatherDto;
import com.example.gatherservice.entity.GatherEntity;
import com.example.gatherservice.repository.GatherRepository;
import com.example.gatherservice.enums.GatherState;
import com.example.gatherservice.vo.ResponseDateTime;
import com.example.gatherservice.vo.ResponseJoin;
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
import java.time.LocalTime;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class GatherServiceImpl implements GatherService {
    private final GatherRepository gatherRepository;
    private final ModelMapper mapper;
    private final Environment env;
    private final JoinServiceClient joinServiceClient;

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
    public void closeGather(String gatherId) {
        GatherEntity gather = gatherRepository.findByGatherId(gatherId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, env.getProperty("gather.not-found-msg")));

        gather.setState(GatherState.CLOSED);

        gatherRepository.save(gather);
    }

    @Override
    public List<ConfirmedGatherDto> confirmTime(String gatherId) {
        GatherEntity gather = gatherRepository.findByGatherId(gatherId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, env.getProperty("gather.not-found-msg")));

        List<ResponseJoin> joins = joinServiceClient.getJoins(gatherId);

        List<ConfirmedGatherDto> result = calculate(gatherId, joins, gather.getDuration());

        return result;
    }

    private List<ConfirmedGatherDto> calculate(String gatherId, List<ResponseJoin> joins, LocalTime duration) {
        List<ResponseDateTime> joinDateTimes = joins.stream()
                .flatMap(join -> join.getSelectDateTimes().stream())
                .sorted(Comparator.comparing(ResponseDateTime::getStartDateTime))
                .toList();

        PriorityQueue<ResponseDateTime> Queue = new PriorityQueue<>(Comparator.comparing(ResponseDateTime::getEndDateTime));

        List<ConfirmedGatherDto> result = new ArrayList<>();
        long maxQueueSize = 0L;
        for (ResponseDateTime joinDateTime : joinDateTimes) {
            if (Queue.isEmpty()) {
                Queue.add(joinDateTime);
            } else {
                while (isShortOfDuration(duration, Queue, joinDateTime)) {
                    Queue.poll();
                }
                Queue.add(joinDateTime);
            }
            result.add(new ConfirmedGatherDto(gatherId,
                    joinDateTime.getStartDateTime(),
                    addDateTime(joinDateTime.getStartDateTime(), duration),
                    Queue.size()));

            maxQueueSize = Math.max(maxQueueSize, Queue.size());
        }

        final long maxMemberCounts =maxQueueSize;
        return result.stream().filter(res -> res.getMemberCounts() == maxMemberCounts).toList();
    }

    private boolean isShortOfDuration(LocalTime duration, PriorityQueue<ResponseDateTime> Queue, ResponseDateTime joinDateTime) {
        return !Queue.isEmpty()
                && Queue.peek().getEndDateTime().isBefore(addDateTime(joinDateTime.getStartDateTime(), duration));
    }

    private LocalDateTime addDateTime(LocalDateTime localDateTime, LocalTime duration) {
        return localDateTime
                .plusHours(duration.getHour())
                .plusMinutes(duration.getMinute())
                .plusSeconds(duration.getSecond());
    }
}
