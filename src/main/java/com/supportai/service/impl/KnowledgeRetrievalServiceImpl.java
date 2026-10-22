package com.supportai.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.supportai.dto.KnowledgeSearchResult;
import com.supportai.entity.KnowledgeDocument;
import com.supportai.repository.KnowledgeDocumentRepository;
import com.supportai.service.KnowledgeRetrievalService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KnowledgeRetrievalServiceImpl
        implements KnowledgeRetrievalService {

    private final KnowledgeDocumentRepository knowledgeDocumentRepository;

    @Override
    public List<KnowledgeSearchResult> retrieve(String question) {

        if (question == null || question.isBlank()) {
            return List.of();
        }

        List<String> keywords = extractKeywords(question);

        if (keywords.isEmpty()) {
            return List.of();
        }

        Map<Long, Integer> scores = new HashMap<>();

        List<KnowledgeDocument> documents =
                knowledgeDocumentRepository.findAll();

        for (KnowledgeDocument document : documents) {

            String title =
                    document.getTitle().toLowerCase();

            String content =
                    document.getContent().toLowerCase();

            int score = 0;

            for (String keyword : keywords) {

                if (title.contains(keyword)) {
                    score += 3;
                }

                if (content.contains(keyword)) {
                    score += 1;
                }
            }

            if (score > 0) {
                scores.put(document.getId(), score);
            }
        }

        return documents.stream()
                .filter(document ->
                        scores.containsKey(document.getId()))
                .sorted(
                        Comparator.comparingInt(
                                (KnowledgeDocument document) ->
                                        scores.get(document.getId())
                        ).reversed()
                )
                .map(this::mapToResult)
                .toList();
    }

    private List<String> extractKeywords(String question) {

        Set<String> stopWords = Set.of(
                "a",
                "an",
                "the",
                "is",
                "are",
                "am",
                "was",
                "were",
                "how",
                "what",
                "where",
                "when",
                "why",
                "can",
                "could",
                "would",
                "should",
                "i",
                "my",
                "me",
                "to",
                "for",
                "of",
                "in",
                "on",
                "with",
                "and",
                "or",
                "please",
                "tell"
        );

        return Arrays.stream(
                    question
                            .toLowerCase()
                            .replaceAll("[^a-z0-9 ]", " ")
                            .split("\\s+")
                )
                .map(String::trim)
                .filter(word -> !word.isBlank())
                .filter(word -> word.length() > 2)
                .filter(word -> !stopWords.contains(word))
                .distinct()
                .toList();
    }

    private KnowledgeSearchResult mapToResult(
            KnowledgeDocument document) {

        return KnowledgeSearchResult.builder()
                .id(document.getId())
                .title(document.getTitle())
                .content(document.getContent())
                .category(document.getCategory())
                .build();
    }
}