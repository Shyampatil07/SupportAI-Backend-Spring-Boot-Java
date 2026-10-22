package com.supportai.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KnowledgeDocumentUpdateRequest {

    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    private String content;

    @Size(max = 100, message = "Category must not exceed 100 characters")
    private String category;
}