package com.example.gatherservice.client;

import com.example.gatherservice.vo.ResponseJoin;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient("join-service")
public interface JoinServiceClient {
    @GetMapping("/{gatherId}/joins")
    List<ResponseJoin> getJoins(@PathVariable String gatherId);
}
