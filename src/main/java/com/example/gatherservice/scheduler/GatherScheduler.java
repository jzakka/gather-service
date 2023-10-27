package com.example.gatherservice.scheduler;

import com.example.gatherservice.entity.GatherEntity;
import com.example.gatherservice.enums.GatherState;
import com.example.gatherservice.repository.GatherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
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
    private final GatherRepository gatherRepository;
    private final Clock clock;

    @Scheduled(fixedRateString = "${scheduler.check-rate}")
    public void checkGather() {
        List<GatherEntity> openGathers = gatherRepository.findAllByState(GatherState.OPEN);

        for (GatherEntity openGather : openGathers) {
            if (LocalDateTime.now(clock).isAfter(openGather.getDeadLine())) {
                closeGather(openGather);
            }
        }
    }

    private void closeGather(GatherEntity openGather) {
        openGather.setState(GatherState.CLOSED);
        gatherRepository.save(openGather);
    }
}
