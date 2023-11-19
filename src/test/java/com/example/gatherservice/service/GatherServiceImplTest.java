package com.example.gatherservice.service;

import com.example.gatherservice.client.JoinServiceClient;
import com.example.gatherservice.dto.ConfirmedGatherDto;
import com.example.gatherservice.dto.GatherDto;
import com.example.gatherservice.scheduler.GatherScheduler;
import com.example.gatherservice.vo.ResponseDateTime;
import com.example.gatherservice.vo.ResponseJoin;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.env.Environment;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@MockBean({GatherScheduler.class})
class GatherServiceImplTest {
    @Autowired
    GatherService gatherService;

    @MockBean
    JoinServiceClient joinServiceClient;

    @Autowired
    Environment env;

    // 테스트용 더미 날짜
    LocalDate startDate = LocalDate.of(2077, 10, 3);
    LocalDate endDate = LocalDate.of(2077, 10, 10);
    LocalTime startTime = LocalTime.of(3, 30);
    LocalTime endTime = LocalTime.of(15, 30);
    LocalTime duration = LocalTime.of(1, 30);
    LocalDateTime deadLine = LocalDateTime.of(2077, 10, 2, 0, 0);

    @Test
    @DisplayName("모임 생성 테스트 -> 성공")
    void createGatherSuccess() {
        GatherDto dto = dummyGatherDto(
                "테스트 모임", "테스트 설명",
                "test-user-id",
                startDate, endDate,
                startTime, endTime,
                duration,
                deadLine
        );

        GatherDto result = gatherService.createGather(dto);

        assertThat(result.getName()).isEqualTo("테스트 모임");
    }

    @Test
    @DisplayName("잘못된 날짜 테스트")
    void createInvalidDateGather() {
        GatherDto dto = dummyGatherDto(
                "테스트 모임", "테스트 설명", "test-user-id",
                endDate, startDate,
                startTime, endTime,
                LocalTime.of(1, 30),
                LocalDateTime.now().plusDays(7)
        );

        assertThatThrownBy(() -> gatherService.createGather(dto))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining(env.getProperty("gather.validation.date-invalid-msg"));
    }

    @Test
    @DisplayName("자정을 걸치는 모임시간 테스트, 성공")
    void createMidnightGather() {
        LocalTime startTime = LocalTime.of(23, 0);
        LocalTime endTime = LocalTime.of(2, 30);

        GatherDto dto = dummyGatherDto(
                "테스트 모임", "테스트 설명", "test-user-id",
                startDate, endDate,
                startTime, endTime,
                duration,
                deadLine
        );

        assertThatCode(()->gatherService.createGather(dto)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("자정을 걸치는 모임시간 테스트, 모임 기간이 너무 큼")
    void createInvalidMidnightGather() {
        LocalTime startTime = LocalTime.of(23, 0);
        LocalTime endTime = LocalTime.of(2, 30);

        // 시작시간과 끝시간이 3시간 30분의 차이를 갖는데 비해 4시간동안 모임진행을 하도록 설정
        LocalTime tooLargeDuration = LocalTime.of(4, 0);


        GatherDto dto = dummyGatherDto(
                "테스트 모임", "테스트 설명", "test-user-id",
                startDate, endDate,
                startTime, endTime,
                tooLargeDuration,
                deadLine
        );

        assertThatThrownBy(()->gatherService.createGather(dto))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining(env.getProperty("gather.validation.duration-invalid-msg"));
    }

    @Test
    @DisplayName("잘못된 모임기간 테스트")
    void createInvalidDurationGather() {
        GatherDto dto = dummyGatherDto(
                "테스트 모임", "테스트 설명", "test-user-id",
                startDate, endDate,
                startTime, endTime,
                duration.plusHours(12),
                deadLine
        );

        assertThatThrownBy(() -> gatherService.createGather(dto))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining(env.getProperty("gather.validation.duration-invalid-msg"));
    }

    @Test
    @DisplayName("잘못된 마감기간 테스트")
    void createInvalidDeadLineGather() {
        GatherDto dto = dummyGatherDto(
                "테스트 모임", "테스트 설명", "test-user-id",
                startDate, endDate,
                startTime, endTime,
                duration,
                LocalDateTime.of(startDate, LocalTime.of(0, 0))
        );

        assertThatThrownBy(() -> gatherService.createGather(dto))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining(env.getProperty("gather.validation.deadline-invalid-msg"));
    }

    @Test
    @DisplayName("모임 시간 계산 테스트")
    void calculate() {
        GatherDto dto = dummyGatherDto(
                "테스트 모임", "테스트 설명",
                "test-user-id",
                startDate, endDate,
                startTime, endTime,
                duration,
                deadLine
        );
        GatherDto gather = gatherService.createGather(dto);

        Mockito.when(joinServiceClient.getJoins(Mockito.anyString()))
                .thenReturn(List.of(
                                ResponseJoin.builder().gatherId(gather.getGatherId()).userId("test-user-id1")
                                        .selectDateTimes(List.of(
                                                new ResponseDateTime(
                                                        LocalDateTime.parse("2077-10-03T03:31:00"),
                                                        LocalDateTime.parse("2077-10-03T14:24:00")
                                                )
                                        )).build(),
                                ResponseJoin.builder().gatherId(gather.getGatherId()).userId("test-user-id2")
                                        .selectDateTimes(List.of(
                                                new ResponseDateTime(
                                                        LocalDateTime.parse("2077-10-03T05:12:00"),
                                                        LocalDateTime.parse("2077-10-03T06:40:00")
                                                ),
                                                new ResponseDateTime(
                                                        LocalDateTime.parse("2077-10-03T12:15:00"),
                                                        LocalDateTime.parse("2077-10-03T14:00:00")
                                                )
                                        )).build(),
                                ResponseJoin.builder().gatherId(gather.getGatherId()).userId("test-user-id3")
                                        .selectDateTimes(List.of(
                                                new ResponseDateTime(
                                                        LocalDateTime.parse("2077-10-03T07:00:00"),
                                                        LocalDateTime.parse("2077-10-03T08:35:00")
                                                ),
                                                new ResponseDateTime(
                                                        LocalDateTime.parse("2077-10-03T09:00:00"),
                                                        LocalDateTime.parse("2077-10-03T10:35:00")
                                                ),
                                                new ResponseDateTime(
                                                        LocalDateTime.parse("2077-10-03T11:00:00"),
                                                        LocalDateTime.parse("2077-10-03T14:00:00")
                                                )
                                        )).build(),
                                ResponseJoin.builder().gatherId(gather.getGatherId()).userId("test-user-id4")
                                        .selectDateTimes(List.of(
                                                new ResponseDateTime(
                                                        LocalDateTime.parse("2077-10-03T08:12:00"),
                                                        LocalDateTime.parse("2077-10-03T01:00:00")
                                                )
                                        )).build(),
                                ResponseJoin.builder().gatherId(gather.getGatherId()).userId("test-user-id5")
                                        .selectDateTimes(List.of(
                                                new ResponseDateTime(
                                                        LocalDateTime.parse("2077-10-03T09:55:00"),
                                                        LocalDateTime.parse("2077-10-03T12:20:00")
                                                ),
                                                new ResponseDateTime(
                                                        LocalDateTime.parse("2077-10-04T03:55:00"),
                                                        LocalDateTime.parse("2077-10-04T05:30:00")
                                                )
                                        )).build(),
                                ResponseJoin.builder().gatherId(gather.getGatherId()).userId("test-user-id6")
                                        .selectDateTimes(List.of(
                                                new ResponseDateTime(
                                                        LocalDateTime.parse("2077-10-03T11:05:00"),
                                                        LocalDateTime.parse("2077-10-03T12:45:00")
                                                ),
                                                new ResponseDateTime(
                                                        LocalDateTime.parse("2077-10-04T03:45:00"),
                                                        LocalDateTime.parse("2077-10-04T07:45:00")
                                                )
                                        )).build(),
                                ResponseJoin.builder().gatherId(gather.getGatherId()).userId("test-user-id7")
                                        .selectDateTimes(List.of(
                                                new ResponseDateTime(
                                                        LocalDateTime.parse("2077-10-03T11:30:00"),
                                                        LocalDateTime.parse("2077-10-03T15:30:00")
                                                ),
                                                new ResponseDateTime(
                                                        LocalDateTime.parse("2077-10-04T03:30:00"),
                                                        LocalDateTime.parse("2077-10-04T05:00:00")
                                                )
                                        )).build()
                        ));

        List<ConfirmedGatherDto> result = gatherService.confirmTime(gather.getGatherId());

        assertThat(result).containsExactly(
                new ConfirmedGatherDto(
                        gather.getGatherId(),
                        LocalDateTime.parse("2077-10-03T12:15:00"),
                        LocalDateTime.parse("2077-10-03T13:45:00"),
                        4)
        );
    }


    private GatherDto dummyGatherDto(String name,
                                     String description,
                                     String userId,
                                     LocalDate startDate,
                                     LocalDate endDate,
                                     LocalTime startTime,
                                     LocalTime endTime,
                                     LocalTime duration,
                                     LocalDateTime deadLine) {
        GatherDto gatherDto = new GatherDto();

        gatherDto.setName(name);
        gatherDto.setUserId(userId);
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