package com.supportai.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Map;

@Getter
@AllArgsConstructor
public class DateRangeAnalyticsResponse {

    private LocalDate from;

    private LocalDate to;

    private long ticketsCreated;

    private Map<String, Long> statusDistribution;

    private Map<String, Long> priorityDistribution;

    private Map<String, Long> categoryDistribution;
}