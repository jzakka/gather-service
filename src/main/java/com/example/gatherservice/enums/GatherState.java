package com.example.gatherservice.enums;

public enum GatherState {
    OPEN,   // 모임에 아직 참여 가능함
    CLOSED, // 모임이 마감됨, 참여 불가
    ONGOING,// 모임이 진행 중임
    END     // 모임이 끝남
}
