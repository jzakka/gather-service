package com.example.gatherservice.controller;

import com.example.gatherservice.vo.RequestGather;
import com.example.gatherservice.vo.ResponseGather;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class GatherController {
    private final Environment env;

    @PostMapping("/gathers")
    public ResponseEntity<ResponseGather> createGather(@RequestBody RequestGather gather) {
        return null;
    }
}
