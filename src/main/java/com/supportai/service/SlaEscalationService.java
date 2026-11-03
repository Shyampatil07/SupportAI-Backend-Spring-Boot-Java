package com.supportai.service;

import com.supportai.entity.Ticket;

public interface SlaEscalationService {

    void escalate(Ticket ticket);
}