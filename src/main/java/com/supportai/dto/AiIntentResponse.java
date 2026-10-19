package com.supportai.dto;


import com.supportai.entity.AiIntent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonAlias;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiIntentResponse {

    private AiIntent intent;

    private Long ticketId;

    private String title;

    private String description;

    private String comment;

    private String updateField;

    @JsonAlias({
        "newValue",
        "newTitle",
        "newDescription",
        "newPriority"
    })
    private String updateValue;
}