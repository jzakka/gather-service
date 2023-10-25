package com.example.gatherservice.controller;

import com.example.gatherservice.dto.GatherDto;
import com.example.gatherservice.service.GatherService;
import com.example.gatherservice.vo.RequestGather;
import com.example.gatherservice.vo.ResponseGather;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class GatherController {
    private final Environment env;
    private final GatherService gatherService;
    private final ModelMapper mapper;

    @PostMapping("/gathers")
    public ResponseEntity<ResponseGather> createGather(@RequestBody RequestGather gather) {
        GatherDto requestDto = mapper.map(gather, GatherDto.class);

        GatherDto createdGatherDto = gatherService.createGather(requestDto);

        ResponseGather body = mapper.map(createdGatherDto, ResponseGather.class);

        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @GetMapping("/gathers/{gatherId}")
    public ResponseEntity<ResponseGather> getGather(@PathVariable String gatherId) {
        GatherDto gatherDto = gatherService.getGatherByGatherId(gatherId);

        ResponseGather body = mapper.map(gatherDto, ResponseGather.class);

        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }
}
