package com.supportai.service;

import com.supportai.dto.AdminDashboardResponse;
import com.supportai.dto.DateRangeAnalyticsResponse;
import com.supportai.dto.HistoricalAnalyticsResponse;

import java.time.LocalDate;

public interface AdminDashboardService {

    AdminDashboardResponse getDashboard();
    
    DateRangeAnalyticsResponse getDateRangeAnalytics(
            LocalDate from,
            LocalDate to
    );
    
    HistoricalAnalyticsResponse getHistoricalAnalytics(
            LocalDate from,
            LocalDate to
    );
    
    
}