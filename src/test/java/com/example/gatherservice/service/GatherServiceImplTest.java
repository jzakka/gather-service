package com.example.gatherservice.service;

import com.example.gatherservice.dto.GatherDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class GatherServiceImplTest {
    @Autowired
    GatherService gatherService;

    @Autowired
    Environment env;

    @Test
    @DisplayName("모임 생성 테스트 -> 성공")
    void createGatherSuccess() {
        GatherDto dto = dummyGatherDto(
                "테스트 모임", "테스트 설명",
                LocalDate.now().plusDays(10), LocalDate.now().plusDays(11),
                LocalTime.now(), LocalTime.now().plusHours(3),
                LocalTime.of(1, 30),
                LocalDateTime.now().plusDays(7)
        );

        GatherDto result = gatherService.createGather(dto);

        assertThat(result.getName()).isEqualTo("테스트 모임");
    }

    @Test
    @DisplayName("잘못된 날짜 테스트")
    void createInvalidDateGather() {
        GatherDto dto = dummyGatherDto(
                "테스트 모임", "테스트 설명",
                LocalDate.now().plusDays(1), LocalDate.now(),
                LocalTime.now(), LocalTime.now().plusHours(1),
                LocalTime.of(1, 30),
                LocalDateTime.now().plusDays(7)
        );

        assertThatThrownBy(() -> gatherService.createGather(dto))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining(env.getProperty("gather.validation.date-invalid-msg"));
    }

    @Test
    @DisplayName("잘못된 시간 테스트")
    void createInvalidTimeGather() {
        GatherDto dto = dummyGatherDto(
                "테스트 모임", "테스트 설명",
                LocalDate.now(), LocalDate.now().plusDays(1),
                LocalTime.now().plusHours(1), LocalTime.now(),
                LocalTime.of(1, 30),
                LocalDateTime.now().plusDays(7)
        );

        assertThatThrownBy(() -> gatherService.createGather(dto))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining(env.getProperty("gather.validation.time-invalid-msg"));
    }

    @Test
    @DisplayName("잘못된 모임기간 테스트")
    void createInvalidDurationGather() {
        GatherDto dto = dummyGatherDto(
                "테스트 모임", "테스트 설명",
                LocalDate.now().plusDays(9), LocalDate.now().plusDays(9),
                LocalTime.now(), LocalTime.now().plusHours(1),
                LocalTime.of(1, 30),
                LocalDateTime.now().plusDays(7)
        );

        assertThatThrownBy(() -> gatherService.createGather(dto))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining(env.getProperty("gather.validation.duration-invalid-msg"));
    }

    @Test
    @DisplayName("잘못된 마감기간 테스트")
    void createInvalidDeadLineGather() {
        GatherDto dto = dummyGatherDto(
                "테스트 모임", "테스트 설명",
                LocalDate.now().plusDays(9), LocalDate.now().plusDays(10),
                LocalTime.now(), LocalTime.now().plusHours(2),
                LocalTime.of(1, 30),
                LocalDateTime.now().plusDays(9)
        );

        assertThatThrownBy(() -> gatherService.createGather(dto))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining(env.getProperty("gather.validation.deadline-invalid-msg"));
    }

    private GatherDto dummyGatherDto(String name,
                                     String description,
                                     LocalDate startDate,
                                     LocalDate endDate,
                                     LocalTime startTime,
                                     LocalTime endTime,
                                     LocalTime duration,
                                     LocalDateTime deadLine) {
        GatherDto gatherDto = new GatherDto();

        gatherDto.setName(name);
        gatherDto.setDescription(description);
        gatherDto.setStartDate(startDate);
        gatherDto.setEndDate(endDate);
        gatherDto.setStartTime(startTime);
        gatherDto.setEndTime(endTime);
        gatherDto.setDuration(duration);
        gatherDto.setDeadLine(deadLine);

        return gatherDto;
    }
}