package com.supportai.service;

import com.supportai.dto.AgentDashboardResponse;

public interface AgentDashboardService {

    AgentDashboardResponse getDashboard(
            String userEmail
    );
}