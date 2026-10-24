package com.supportai.service;

import java.time.LocalDate;

import com.supportai.dto.SlaAnalyticsResponse;
import com.supportai.dto.SlaReportResponse;
import com.supportai.entity.TicketCategory;
import com.supportai.entity.TicketPriority;

public interface SlaAnalyticsService {

    SlaAnalyticsResponse getSlaAnalytics();
    
    SlaReportResponse getSlaReport(
            LocalDate from,
            LocalDate to,
            TicketPriority priority,
            TicketCategory category,
            String slaStatus,
            Boolean escalated
    );
    
    
}