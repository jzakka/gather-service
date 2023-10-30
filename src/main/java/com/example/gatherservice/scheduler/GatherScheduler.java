package com.example.gatherservice.scheduler;

import com.example.gatherservice.dto.GatherDto;
import com.example.gatherservice.enums.GatherState;
import com.example.gatherservice.service.GatherService;
import com.example.gatherservice.vo.MessageGather;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Transactional
@Component
@RequiredArgsConstructor
@Slf4j
public class GatherScheduler {
    private final GatherService gatherService;
    private final Clock clock;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final Environment env;

    @Scheduled(fixedRateString = "${scheduler.check-rate}")
    public void checkGather() {
        List<GatherDto> openGathers = gatherService.getOpenGathers();

        for (GatherDto openGather : openGathers) {
            if (openGather.getState().equals(GatherState.OPEN) && LocalDateTime.now(clock).isAfter(openGather.getDeadLine())) {
                closeGather(openGather.getGatherId());
                sendMessage(openGather.getGatherId());
                confirmGather(openGather.getGatherId());
            }
        }
    }

    private void sendMessage(String gatherId) {
        MessageGather messageGather = new MessageGather(gatherId);

        try {
            String gatherInString = objectMapper.writeValueAsString(messageGather);

            final String TOPIC_NAME = env.getProperty("kafka.topic-name");

            kafkaTemplate.send(TOPIC_NAME, gatherInString);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    private void confirmGather(String gatherId) {
        gatherService.confirmTime(gatherId);
    }

    private void closeGather(String gatherId) {
        gatherService.closeGather(gatherId);
    }
}
