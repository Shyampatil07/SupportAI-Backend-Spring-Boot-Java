package com.supportai.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.supportai.specification.TicketSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import com.supportai.dto.AiClassificationResponse;
import com.supportai.dto.TicketClassificationUpdateRequest;
import com.supportai.dto.TicketCreateRequest;
import com.supportai.dto.TicketManagementRequest;
import com.supportai.dto.TicketResponse;
import com.supportai.dto.TicketUpdateRequest;
import com.supportai.entity.Role;
import com.supportai.entity.Ticket;
import com.supportai.entity.TicketCategory;
import com.supportai.entity.TicketHistory;
import com.supportai.entity.TicketHistoryAction;
import com.supportai.entity.TicketPriority;
import com.supportai.entity.TicketStatus;
import com.supportai.entity.User;
import com.supportai.enums.NotificationType;
import com.supportai.exception.ResourceNotFoundException;
import com.supportai.repository.TicketHistoryRepository;
import com.supportai.repository.TicketRepository;
import com.supportai.repository.UserRepository;
import com.supportai.service.AiClassificationService;
import com.supportai.service.NotificationService;
import com.supportai.service.SlaService;
import com.supportai.service.TicketService;


import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.security.access.AccessDeniedException;

import com.supportai.specification.TicketSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;


@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;

    private final UserRepository userRepository;
    
    private final TicketHistoryRepository ticketHistoryRepository;
    
    private final AiClassificationService aiClassificationService;
    
    private final NotificationService notificationService;
    
    private final SlaService slaService;
    
    private void saveHistory(
            Ticket ticket,
            User user,
            TicketHistoryAction action,
            String oldValue,
            String newValue) {

        TicketHistory history = TicketHistory.builder()
                .ticket(ticket)
                .changedBy(user)
                .action(action)
                .oldValue(oldValue)
                .newValue(newValue)
                .build();

        ticketHistoryRepository.save(history);
    }
    
    
    @Override
    @Transactional(readOnly = true)
    public Page<TicketResponse> searchAssignedTickets(
            String userEmail,
            TicketStatus status,
            TicketPriority priority,
            TicketCategory category,
            String search,
            Pageable pageable) {

        User agent = userRepository
                .findByEmail(userEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        )
                );

        Specification<Ticket> specification =
                TicketSpecification.assignedTicketSearch(
                        agent.getId(),
                        status,
                        priority,
                        category,
                        search
                );

        return ticketRepository
                .findAll(specification, pageable)
                .map(this::mapToResponse);
    }
    
    
    @Override
    public Optional<TicketResponse> getTicketForChat(
            Long ticketId,
            String userEmail) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        Optional<Ticket> ticketOptional =
                ticketRepository.findById(ticketId);

        if (ticketOptional.isEmpty()) {
            return Optional.empty();
        }

        Ticket ticket = ticketOptional.get();

        boolean isAdmin =
                user.getRole() == Role.ADMIN;

        boolean isOwner =
                ticket.getCreatedBy() != null
                && ticket.getCreatedBy().getId().equals(user.getId());

        boolean isAssignedAgent =
                ticket.getAssignedAgent() != null
                && ticket.getAssignedAgent().getId().equals(user.getId());

        if (!isAdmin && !isOwner && !isAssignedAgent) {
            return Optional.empty();
        }

        return Optional.of(mapToResponse(ticket));
    }
    
    
    @Override
    @Transactional
    public TicketResponse updateTicketPriority(
            Long ticketId,
            TicketPriority priority,
            String userEmail) {
    	
    	

        User agent = userRepository.findByEmail(userEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"));

        if (agent.getRole() != Role.AGENT) {
            throw new AccessDeniedException(
                    "Only agents can update ticket priority");
        }

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Ticket not found with id: " + ticketId));

        if (ticket.getAssignedAgent() == null ||
            !ticket.getAssignedAgent().getId().equals(agent.getId())) {

            throw new AccessDeniedException(
                    "You are not allowed to update this ticket");
        }
        TicketPriority oldPriority = ticket.getPriority();

        ticket.setPriority(priority);

        Ticket updatedTicket = ticketRepository.save(ticket);
        saveHistory(
                updatedTicket,
                agent,
                TicketHistoryAction.PRIORITY_CHANGED,
                oldPriority.name(),
                priority.name()
        );
        
        if (oldPriority != priority) {

            notificationService.createNotification(
            		updatedTicket.getCreatedBy().getId(),
                    "Ticket Priority Updated",
                    "The priority of Ticket #"
                            + updatedTicket.getId()
                            + " has been changed to "
                            + updatedTicket.getPriority()
                            + ".",
                    NotificationType.TICKET_PRIORITY_CHANGED
            );
        }

        return mapToResponse(updatedTicket);
    }
    
    
    
    @Override
    @Transactional
    public TicketResponse updateTicketStatus(
            Long ticketId,
            TicketStatus status,
            String userEmail) {

        User agent = userRepository.findByEmail(userEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"));

        if (agent.getRole() != Role.AGENT) {
            throw new AccessDeniedException(
                    "Only agents can update ticket status");
        }

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Ticket not found with id: " + ticketId));

        // Resource-level authorization
        if (ticket.getAssignedAgent() == null ||
            !ticket.getAssignedAgent().getId().equals(agent.getId())) {

            throw new AccessDeniedException(
                    "You are not allowed to update this ticket");
        }
        
        TicketStatus oldStatus = ticket.getStatus();
        
        

        ticket.setStatus(status);

        Ticket updatedTicket = ticketRepository.save(ticket);
        
        saveHistory(
                updatedTicket,
                agent,
                TicketHistoryAction.STATUS_CHANGED,
                oldStatus.name(),
                status.name()
        );
        
        if (oldStatus != status) {

            notificationService.createNotification(
            		updatedTicket.getCreatedBy().getId(),
                    "Ticket Status Updated",
                    "The status of Ticket #"
                            + updatedTicket.getId()
                            + " has been changed to "
                            + updatedTicket.getStatus()
                            + ".",
                    NotificationType.TICKET_STATUS_CHANGED
            );
        }
        
//        notificationService.createNotification(
//        		updatedTicket.getCreatedBy().getId(),
//                "Ticket Status Updated",
//                "The status of Ticket #"
//                        + updatedTicket.getId()
//                        + " has been changed to "
//                        + updatedTicket.getStatus()
//                        + ".",
//                NotificationType.TICKET_STATUS_CHANGED
//        );

        return mapToResponse(updatedTicket);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<TicketResponse> getAssignedTickets(
            String userEmail) {

        User agent = userRepository.findByEmail(userEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"));

        if (agent.getRole() != Role.AGENT) {
            throw new AccessDeniedException(
                    "Only agents can access assigned tickets");
        }

        List<Ticket> tickets =
                ticketRepository
                        .findByAssignedAgentIdOrderByCreatedAtDesc(
                                agent.getId());

        return tickets.stream()
                .map(this::mapToResponse)
                .toList();
    }
    
    @Override
    @Transactional
    public TicketResponse assignTicket(
            Long ticketId,
            Long agentId,
            String adminEmail) {

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Ticket not found with id: " + ticketId));

        User agent = userRepository.findById(agentId)
                 .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with id: " + agentId));
        
        User adminUser = userRepository.findByEmail(adminEmail)
        						.orElseThrow(() ->
						new ResourceNotFoundException(
								"Admin user not found"));

        if (agent.getRole() != Role.AGENT) {
            throw new IllegalArgumentException(
                    "Selected user is not an agent");
        }

        User oldAgent = ticket.getAssignedAgent();

        ticket.setAssignedAgent(agent);

        Ticket updatedTicket = ticketRepository.save(ticket);
        
        saveHistory(
                updatedTicket,
                adminUser,
                TicketHistoryAction.ASSIGNED,
                oldAgent != null
                        ? oldAgent.getName()
                        : null,
                agent.getName()
        );
        
        notificationService.createNotification(
                agent.getId(),
                "New Ticket Assigned",
                "Ticket #" + updatedTicket.getId()
                        + " has been assigned to you.",
                NotificationType.TICKET_ASSIGNED
        );

        return mapToResponse(updatedTicket);
    }
    
    @Override
    @Transactional
    public TicketResponse manageTicket(
            Long ticketId,
            TicketManagementRequest request,
            String userEmail) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Ticket not found with id: " + ticketId));

        Role role = user.getRole();

        if (role == Role.CUSTOMER) {
            throw new AccessDeniedException(
                    "Customers cannot manage tickets");
        }

        if (role == Role.AGENT) {

            if (ticket.getAssignedAgent() == null ||
                    !ticket.getAssignedAgent()
                            .getId()
                            .equals(user.getId())) {

                throw new AccessDeniedException(
                        "Agent is not assigned to this ticket");
            }
        }

        if (request.getStatus() != null) {
            ticket.setStatus(request.getStatus());
        }

        if (request.getPriority() != null) {
            ticket.setPriority(request.getPriority());
        }

        if (request.getAssignedAgentId() != null) {

            if (role != Role.ADMIN) {
                throw new AccessDeniedException(
                        "Only admins can assign tickets");
            }

            User agent = userRepository
                    .findById(request.getAssignedAgentId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Agent not found"));

            if (agent.getRole() != Role.AGENT) {
                throw new IllegalArgumentException(
                        "Selected user is not an agent");
            }

            ticket.setAssignedAgent(agent);
        }

        Ticket updatedTicket =
                ticketRepository.save(ticket);

        return mapToResponse(updatedTicket);
    }
    
    @Override
    @Transactional
    public TicketResponse updateTicket(
            Long ticketId,
            TicketUpdateRequest request,
            String userEmail) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Ticket not found with id: " + ticketId));

        // Ownership check
        if (!ticket.getCreatedBy().getId().equals(user.getId())) {
            throw new AccessDeniedException(
                    "You are not allowed to update this ticket");
        }

        // Update only fields provided by customer
        if (request.getTitle() != null) {

            if (request.getTitle().isBlank()) {
                throw new IllegalArgumentException(
                        "Title cannot be blank");
            }

            ticket.setTitle(request.getTitle());
        }

        if (request.getDescription() != null) {

            if (request.getDescription().isBlank()) {
                throw new IllegalArgumentException(
                        "Description cannot be blank");
            }

            ticket.setDescription(request.getDescription());
        }

        if (request.getCategory() != null) {
            ticket.setCategory(request.getCategory());
        }

        if (request.getPriority() != null) {
            ticket.setPriority(request.getPriority());
        }

        Ticket updatedTicket = ticketRepository.save(ticket);

        return mapToResponse(updatedTicket);
    }
    
    @Override
    @Transactional(readOnly = true)
    public TicketResponse getTicketById(
            Long ticketId,
            String userEmail) {

        // 1. Find logged-in user
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        )
                );

        // 2. Find ticket
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Ticket not found with id: " + ticketId
                        )
                );

        // 3. Check ownership
        if (!ticket.getCreatedBy().getId()
                .equals(user.getId())) {

            throw new AccessDeniedException(
                    "You are not allowed to access this ticket"
            );
        }

        // 4. Convert entity to response
        return mapToResponse(ticket);
    }
    
    

    @Override
    @Transactional
    public TicketResponse createTicket(
            TicketCreateRequest request,
            String userEmail) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        ));

        // 1. AI classification
        AiClassificationResponse aiResult =
                aiClassificationService.classifyTicket(
                        request.getTitle(),
                        request.getDescription()
                );

        // 2. Create ticket
        Ticket ticket = Ticket.builder()
                .title(request.getTitle())
                .description(request.getDescription())

                // Final/current classification
                .category(aiResult.getCategory())
                .priority(aiResult.getPriority())

                // AI prediction
                .aiCategory(aiResult.getCategory())
                .aiPriority(aiResult.getPriority())
                .aiReason(aiResult.getReason())

                .status(TicketStatus.OPEN)
                .createdBy(user)
                .build();

        // 3. Save ticket
        Ticket savedTicket = ticketRepository.save(ticket);
        
        LocalDateTime slaDeadline =
                slaService.calculateDeadline(
                        ticket.getCreatedAt(),
                        ticket.getPriority());

        ticket.setSlaDeadline(slaDeadline);
        ticket.setSlaBreached(false);

        // 4. Create history
        TicketHistory history = TicketHistory.builder()
                .ticket(savedTicket)
                .changedBy(user)
                .action(TicketHistoryAction.CREATED)
                .oldValue(null)
                .newValue(
                        "Ticket created. AI classified as "
                        + aiResult.getCategory()
                        + " / "
                        + aiResult.getPriority()
                )
                .build();

        ticketHistoryRepository.save(history);
        
        
        notificationService.createNotification(
                savedTicket.getCreatedBy().getId(),
                "Ticket Created",
                "Your Ticket #"
                        + savedTicket.getId()
                        + " has been created successfully.",
                NotificationType.TICKET_CREATED
        );

        // 5. Return response
        return mapToResponse(savedTicket);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<TicketResponse> getMyTickets(
            String userEmail) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        )
                );

        List<Ticket> tickets =
                ticketRepository
                        .findByCreatedByIdOrderByCreatedAtDesc(
                                user.getId()
                        );

        return tickets.stream()
                .map(this::mapToResponse)
                .toList();
    }

    private TicketResponse mapToResponse(Ticket ticket) {

        return TicketResponse.builder()
                .id(ticket.getId())
                .title(ticket.getTitle())
                .description(ticket.getDescription())

                .category(ticket.getCategory())
                .priority(ticket.getPriority())

                .aiCategory(ticket.getAiCategory())
                .aiPriority(ticket.getAiPriority())
                .aiReason(ticket.getAiReason())

                .status(ticket.getStatus())

                .createdById(
                        ticket.getCreatedBy() != null
                                ? ticket.getCreatedBy().getId()
                                : null
                )

                .createdByName(
                        ticket.getCreatedBy() != null
                                ? ticket.getCreatedBy().getName()
                                : null
                )

                .assignedAgentId(
                        ticket.getAssignedAgent() != null
                                ? ticket.getAssignedAgent().getId()
                                : null
                )

                .assignedAgentName(
                        ticket.getAssignedAgent() != null
                                ? ticket.getAssignedAgent().getName()
                                : null
                )

                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())

                .build();
    }
    
    @Override
    @Transactional
    public TicketResponse updateTicketClassification(
            Long ticketId,
            TicketClassificationUpdateRequest request,
            String userEmail) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        ));

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Ticket not found"
                        ));

        TicketCategory oldCategory = ticket.getCategory();
        TicketPriority oldPriority = ticket.getPriority();

        // ADMIN can modify any ticket.
        if (user.getRole() == Role.ADMIN) {

            // allowed
        }

        // AGENT can modify only tickets assigned to them.
        else if (user.getRole() == Role.AGENT) {

            if (ticket.getAssignedAgent() == null ||
                !ticket.getAssignedAgent().getId().equals(user.getId())) {

                throw new AccessDeniedException(
                        "You can modify only tickets assigned to you"
                );
            }

        } else {

            throw new AccessDeniedException(
                    "You are not allowed to change ticket classification"
            );
        }

        // Update FINAL classification.
        ticket.setCategory(request.getCategory());
        ticket.setPriority(request.getPriority());

        Ticket savedTicket = ticketRepository.save(ticket);

        // CATEGORY history
        if (oldCategory != request.getCategory()) {

            TicketHistory categoryHistory =
                    TicketHistory.builder()
                            .ticket(savedTicket)
                            .changedBy(user)
                            .action(
                                    TicketHistoryAction.CATEGORY_CHANGED
                            )
                            .oldValue(
                                    oldCategory != null
                                            ? oldCategory.name()
                                            : null
                            )
                            .newValue(
                                    request.getCategory().name()
                            )
                            .build();

            ticketHistoryRepository.save(categoryHistory);
        }

        // PRIORITY history
        if (oldPriority != request.getPriority()) {

            TicketHistory priorityHistory =
                    TicketHistory.builder()
                            .ticket(savedTicket)
                            .changedBy(user)
                            .action(
                                    TicketHistoryAction.PRIORITY_CHANGED
                            )
                            .oldValue(
                                    oldPriority != null
                                            ? oldPriority.name()
                                            : null
                            )
                            .newValue(
                                    request.getPriority().name()
                            )
                            .build();

            ticketHistoryRepository.save(priorityHistory);
        }

        return mapToResponse(savedTicket);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<TicketResponse> searchMyTickets(
            String userEmail,
            TicketStatus status,
            TicketPriority priority,
            TicketCategory category,
            String search,
            Pageable pageable) {

        User customer = userRepository
                .findByEmail(userEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        )
                );

        Specification<Ticket> specification =
                TicketSpecification.customerTicketSearch(
                        customer.getId(),
                        status,
                        priority,
                        category,
                        search
                );

        return ticketRepository
                .findAll(specification, pageable)
                .map(this::mapToResponse);
    }
    
    
    
}