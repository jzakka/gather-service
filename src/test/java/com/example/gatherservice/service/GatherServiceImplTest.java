package com.example.gatherservice.service;

import com.example.gatherservice.dto.GatherDto;
import com.example.gatherservice.dto.GatherMemberDto;
import com.example.gatherservice.dto.SelectDateTimeDto;
import com.example.gatherservice.rule.Rule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class GatherServiceImplTest {
    @Autowired
    GatherService gatherService;

    @Autowired
    Environment env;

    // 테스트용 더미 날짜
    LocalDate startDate = LocalDate.of(2077, 10, 3);
    LocalDate endDate = LocalDate.of(2077, 10, 10);
    LocalTime startTime = LocalTime.of(3, 30);
    LocalTime endTime = LocalTime.of(15, 30);
    LocalTime duration = LocalTime.of(1, 30);
    LocalDateTime deadLine = LocalDateTime.of(2077, 10, 2, 0,0);

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

    @Test
    @DisplayName("방 입장 테스트 -> 성공")
    void joinGatherTestSuccess() {
        /**
         * 모임 참가 가능 날짜 2077/10/3 ~ 2077/10/10
         * 참여 가능 시간     03:30 ~ 15:30
         * 모임 진행 기간     01:30
         * 모임 참여 마감     2077/10/2 00:00
         */
        GatherDto dto = dummyGatherDto(
                "테스트 모임", "테스트 설명",
                startDate, endDate,
                startTime, endTime,
                duration,
                deadLine
        );

        GatherDto createdGather = gatherService.createGather(dto);

        /**
         * 2077년 10월 3일 4시 5분 ~ 2077년 10월 3일 5시 40분
         * 2077년 10월 5일 5시 7분 ~ 2077년 10월 5일 6시 52분
         *
         * test유저가 위의 시간에 모임이 가능하다는 참여 의사를 요청
         */
        GatherMemberDto joinDto = dummyGatherMemberDto(
                createdGather.getGatherId(),
                "test-user-id",
                Rule.MEMBER,
                LocalDateTime.of(2077, 10, 3, 4, 5),
                LocalDateTime.of(2077, 10, 3, 5, 40),
                LocalDateTime.of(2077, 10, 5, 5, 7),
                LocalDateTime.of(2077, 10, 5, 6, 52)
        );

        GatherMemberDto joinedResult = gatherService.joinGather(joinDto);

        assertThat(joinedResult.getUserId()).isEqualTo("test-user-id");
        assertThat(joinedResult.getRule()).isEqualTo(Rule.MEMBER);
        assertThat(joinedResult.getSelectDateTimes().size()).isEqualTo(2); // 선택한 시간대가 2개임
    }

    @Test
    @DisplayName("너무 이른 시간 선택 입장 테스트")
    void joinGatherInvalidTest1() {
        /**
         * 모임 참가 가능 날짜 2077/10/3 ~ 2077/10/10
         * 참여 가능 시간     03:30 ~ 15:30
         * 모임 진행 기간     01:30
         * 모임 참여 마감     2077/10/2 00:00
         */
        GatherDto dto = dummyGatherDto(
                "테스트 모임", "테스트 설명",
                startDate, endDate,
                startTime, endTime,
                duration,
                deadLine
        );

        GatherDto createdGather = gatherService.createGather(dto);

        /**
         * 2077년 10월 3일 4시 5분 ~ 2077년 10월 3일 5시 40분
         * 2077년 10월 5일 2시 7분 ~ 2077년 10월 5일 3시 52분
         *
         * test유저가 위의 시간에 모임이 가능하다는 참여 의사를 요청
         * 두 번째 선택 시간이 걸려야함
         */
        GatherMemberDto joinDto = dummyGatherMemberDto(
                createdGather.getGatherId(),
                "test-user-id",
                Rule.MEMBER,
                LocalDateTime.of(2077, 10, 3, 4, 5),
                LocalDateTime.of(2077, 10, 3, 5, 40),
                LocalDateTime.of(2077, 10, 5, 2, 7),
                LocalDateTime.of(2077, 10, 5, 3, 52)
        );

        assertThatThrownBy(() -> gatherService.joinGather(joinDto))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining(env.getProperty("select-time.validation.select-invalid-msg"));
    }

    @Test
    @DisplayName("너무 늦은 시간 입장 테스트")
    void joinGatherInvalidTest2() {
        /**
         * 모임 참가 가능 날짜 2077/10/3 ~ 2077/10/10
         * 참여 가능 시간     03:30 ~ 15:30
         * 모임 진행 기간     01:30
         * 모임 참여 마감     2077/10/2 00:00
         */
        GatherDto dto = dummyGatherDto(
                "테스트 모임", "테스트 설명",
                startDate, endDate,
                startTime, endTime,
                duration,
                deadLine
        );

        GatherDto createdGather = gatherService.createGather(dto);

        /**
         * 2077년 10월 3일 4시 5분 ~ 2077년 10월 3일 5시 40분
         * 2077년 10월 11일 4시 7분 ~ 2077년 10월 5일 5시 52분
         *
         * test유저가 위의 시간에 모임이 가능하다는 참여 의사를 요청
         * 두 번째 선택 시간이 걸려야함
         */
        GatherMemberDto joinDto = dummyGatherMemberDto(
                createdGather.getGatherId(),
                "test-user-id",
                Rule.MEMBER,
                LocalDateTime.of(2077, 10, 3, 4, 5),
                LocalDateTime.of(2077, 10, 3, 5, 40),
                LocalDateTime.of(2077, 10, 5, 4, 7),
                LocalDateTime.of(2077, 11, 6, 5, 52)
        );

        assertThatThrownBy(() -> gatherService.joinGather(joinDto))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining(env.getProperty("select-time.validation.select-invalid-msg"));
    }

    @Test
    @DisplayName("마감 지나서 입장 테스트")
    void joinGatherAfterDeadLineTest() {
        /**
         * 모임 참가 가능 날짜 2077/10/3 ~ 2077/10/10
         * 참여 가능 시간     03:30 ~ 15:30
         * 모임 진행 기간     01:30
         * 모임 참여 마감     2077/10/2 00:00
         */
        GatherDto dto = dummyGatherDto(
                "테스트 모임", "테스트 설명",
                startDate, endDate,
                startTime, endTime,
                duration,
                deadLine
        );

        GatherDto createdGather = gatherService.createGather(dto);

        gatherService.closeGather(createdGather.getGatherId());

        /**
         * 2077년 10월 3일 4시 5분 ~ 2077년 10월 3일 5시 40분
         * 2077년 10월 5일 5시 7분 ~ 2077년 10월 5일 6시 52분
         *
         * test유저가 위의 시간에 모임이 가능하다는 참여 의사를 요청
         */
        GatherMemberDto joinDto = dummyGatherMemberDto(
                createdGather.getGatherId(),
                "test-user-id",
                Rule.MEMBER,
                LocalDateTime.of(2077, 10, 3, 4, 5),
                LocalDateTime.of(2077, 10, 3, 5, 40),
                LocalDateTime.of(2077, 10, 5, 5, 7),
                LocalDateTime.of(2077, 10, 5, 6, 52)
        );

        assertThatThrownBy(() -> gatherService.joinGather(joinDto))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining(env.getProperty("select-time.validation.deadline-msg"));
    }

    private GatherMemberDto dummyGatherMemberDto(String gatherId,
                                                 String userId, Rule rule, LocalDateTime ... selectDateTimes) {
        GatherMemberDto joinDto = new GatherMemberDto();
        joinDto.setGatherId(gatherId);
        joinDto.setUserId(userId);
        joinDto.setRule(rule);

        for (int i = 0; i < selectDateTimes.length; i+=2) {
            SelectDateTimeDto selectDateTimeDto = new SelectDateTimeDto();
            selectDateTimeDto.setStartDateTime(selectDateTimes[i]);
            selectDateTimeDto.setEndDateTime(selectDateTimes[i + 1]);

            joinDto.getSelectDateTimes().add(selectDateTimeDto);
        }

        return joinDto;
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