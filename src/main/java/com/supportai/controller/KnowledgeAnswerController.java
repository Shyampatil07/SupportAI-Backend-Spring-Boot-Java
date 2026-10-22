package com.supportai.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.supportai.service.KnowledgeAnswerService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
public class KnowledgeAnswerController {

    private final KnowledgeAnswerService knowledgeAnswerService;

    @GetMapping("/ask")
    public String ask(
            @RequestParam String question) {

        return knowledgeAnswerService.answerQuestion(question, null);
    }
}