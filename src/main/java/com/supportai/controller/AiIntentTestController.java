package com.supportai.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.supportai.dto.AiIntentResponse;
import com.supportai.service.AiIntentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiIntentTestController {

    private final AiIntentService aiIntentService;

    @GetMapping("/intent")
    public AiIntentResponse detectIntent(
            @RequestParam String message) {

        return aiIntentService.detectIntent(
                message,
                ""
        );
    }
}