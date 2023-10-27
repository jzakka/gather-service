package com.example.gatherservice.scheduler;

import com.example.gatherservice.entity.GatherEntity;
import com.example.gatherservice.enums.GatherState;
import com.example.gatherservice.repository.GatherRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;

class GatherSchedulerTest {
    Clock clock;
    GatherRepository gatherRepository;
    GatherScheduler gatherScheduler;

    List<GatherEntity> gathers = gathers();

    private List<GatherEntity> gathers() {
        List<GatherEntity> gathers = List.of(new GatherEntity(), new GatherEntity(), new GatherEntity());

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
        clock = Mockito.mock(Clock.class);
        gatherRepository = Mockito.mock(GatherRepository.class);
        gatherScheduler = new GatherScheduler(gatherRepository, clock);

        Instant futureInstant = Instant.parse("2099-01-01T00:00:00Z");
        Mockito.when(clock.instant()).thenReturn(futureInstant);
        Mockito.when(clock.getZone()).thenReturn(ZoneOffset.UTC);

        Mockito.when(gatherRepository.findAllByState(any())).thenReturn(gathers);
        Mockito.when(gatherRepository.save(any(GatherEntity.class))).thenAnswer(invocation->{
            GatherEntity saveGather = invocation.getArgument(0, GatherEntity.class);

            gathers.stream()
                    .filter(gatherEntity -> saveGather.getGatherId().equals(gatherEntity.getGatherId()))
                    .findAny()
                    .ifPresent(gatherEntity -> gatherEntity.setState(saveGather.getState()));

            return saveGather;
        });
    }

    @Test
    @DisplayName("마감기한 지난 모임 닫기")
    void closeGather() {
        gatherScheduler.checkGather();

        assertThat(gathers.get(0).getState()).isEqualTo(GatherState.CLOSED);
        assertThat(gathers.get(1).getState()).isEqualTo(GatherState.OPEN);
        assertThat(gathers.get(2).getState()).isEqualTo(GatherState.CLOSED);
    }
}