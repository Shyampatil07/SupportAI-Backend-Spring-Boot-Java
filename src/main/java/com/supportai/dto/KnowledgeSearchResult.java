package com.supportai.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class KnowledgeSearchResult {

    private Long id;

    private String title;

    private String content;

    private String category;
}