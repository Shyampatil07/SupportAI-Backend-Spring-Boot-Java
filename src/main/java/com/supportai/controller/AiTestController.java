package com.supportai.controller;

import com.supportai.dto.AiClassificationResponse;
import com.supportai.service.AiClassificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiTestController {

    private final AiClassificationService aiClassificationService;

    @GetMapping("/classify")
    public AiClassificationResponse classify(
            @RequestParam String title,
            @RequestParam String description) {

        return aiClassificationService.classifyTicket(
                title,
                description
        );
    }
}