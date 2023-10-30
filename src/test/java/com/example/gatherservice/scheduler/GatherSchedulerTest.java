package com.example.gatherservice.scheduler;

import com.example.gatherservice.dto.GatherDto;
import com.example.gatherservice.enums.GatherState;
import com.example.gatherservice.service.GatherService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class GatherSchedulerTest {
    Clock clock;
    GatherService gatherService;
    GatherScheduler gatherScheduler;

    List<GatherDto> gathers = gathers();

    private List<GatherDto> gathers() {
        List<GatherDto> gathers = List.of(new GatherDto(), new GatherDto(), new GatherDto());

        // 0번째 모임은 스케줄러가 닫아야 함
        gathers.get(0).setGatherId("gather-0");
        gathers.get(0).setDeadLine(LocalDateTime.parse("2077-10-02T00:00:00"));
        gathers.get(0).setState(GatherState.OPEN);

        // 1번째 모임은 스케줄러가 열어둬야 함
        gathers.get(1).setGatherId("gather-1");
        gathers.get(1).setDeadLine(LocalDateTime.parse("2099-12-30T12:00:00"));
        gathers.get(1).setState(GatherState.OPEN);

        // 2번째 모임은 스케줄러가 건들지도 않음
        gathers.get(2).setGatherId("gather-2");
        gathers.get(2).setDeadLine(LocalDateTime.parse("2070-12-30T12:00:00"));
        gathers.get(2).setState(GatherState.CLOSED);

        return gathers;
    }

    @BeforeEach
    void mockSetting() {
        clock = mock(Clock.class);
        gatherService = mock(GatherService.class);
        KafkaTemplate kafkaTemplate = mock(KafkaTemplate.class);

        gatherScheduler = new GatherScheduler(gatherService, clock, kafkaTemplate, null, null);

        Instant futureInstant = Instant.parse("2099-01-01T00:00:00Z");
        when(clock.instant()).thenReturn(futureInstant);
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);

        when(gatherService.getOpenGathers()).thenReturn(gathers);
        when(gatherService.confirmTime(anyString())).thenReturn(new ArrayList<>());
        doNothing().when(gatherService).closeGather(anyString());
    }

    @Test
    @DisplayName("마감기한 지난 모임 닫기")
    void closeGather() {
        gatherScheduler.checkGather();

        verify(gatherService, times(1)).confirmTime(anyString());
        verify(gatherService, times(1)).closeGather(anyString());
    }
}