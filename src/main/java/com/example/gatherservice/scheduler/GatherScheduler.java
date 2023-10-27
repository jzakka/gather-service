package com.example.gatherservice.scheduler;

import com.example.gatherservice.dto.GatherDto;
import com.example.gatherservice.enums.GatherState;
import com.example.gatherservice.service.GatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Transactional
@Component
@RequiredArgsConstructor
public class GatherScheduler {
    private final GatherService gatherService;
    private final Clock clock;

    @Scheduled(fixedRateString = "${scheduler.check-rate}")
    public void checkGather() {
        List<GatherDto> openGathers = gatherService.getOpenGathers();

        for (GatherDto openGather : openGathers) {
            if (openGather.getState().equals(GatherState.OPEN) && LocalDateTime.now(clock).isAfter(openGather.getDeadLine())) {
                closeGather(openGather.getGatherId());
                confirmGather(openGather.getGatherId());
            }
        }
    }

    private void confirmGather(String gatherId) {
        gatherService.confirmTime(gatherId);
    }

    private void closeGather(String gatherId) {
        gatherService.closeGather(gatherId);
    }
}
