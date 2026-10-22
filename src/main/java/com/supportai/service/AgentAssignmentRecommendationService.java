package com.supportai.service;

import java.util.List;

import com.supportai.dto.AgentRecommendationResponse;
import com.supportai.dto.TicketResponse;

public interface AgentAssignmentRecommendationService {

	List<AgentRecommendationResponse> recommendAgent(Long ticketId);
	
	
	TicketResponse autoAssignTicket(
	        Long ticketId,
	        String adminEmail);
	
}