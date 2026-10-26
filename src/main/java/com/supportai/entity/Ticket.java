package com.supportai.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;

import lombok.*;

@Entity
@Table(name = "tickets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TicketCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketPriority priority;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "ai_category")
    private TicketCategory aiCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "ai_priority")
    private TicketPriority aiPriority;

    @Column(name = "ai_reason", columnDefinition = "TEXT")
    private String aiReason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_agent")
    private User assignedAgent;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {

        LocalDateTime now = LocalDateTime.now();

        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {

        updatedAt = LocalDateTime.now();
    }
    
    @Column(name = "sla_deadline")
    private LocalDateTime slaDeadline;

    @Column(nullable = false)
    private boolean slaBreached = false;
    
    @Column(nullable = false)
    private boolean escalated = false;

    private LocalDateTime escalatedAt;
    
    
}