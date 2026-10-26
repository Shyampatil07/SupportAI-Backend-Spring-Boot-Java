package com.supportai.specification;

import com.supportai.entity.Ticket;
import com.supportai.entity.TicketCategory;
import com.supportai.entity.TicketPriority;
import com.supportai.entity.TicketStatus;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TicketSpecification {

    private TicketSpecification() {
    }
    
    public static Specification<Ticket> slaReportFilter(
            LocalDate from,
            LocalDate to,
            TicketPriority priority,
            TicketCategory category,
            String slaStatus,
            Boolean escalated) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            // -----------------------------
            // DATE RANGE
            // -----------------------------

            if (from != null) {

                LocalDateTime start =
                        from.atStartOfDay();

                predicates.add(
                        cb.greaterThanOrEqualTo(
                                root.get("createdAt"),
                                start
                        )
                );
            }

            if (to != null) {

                LocalDateTime end =
                        to.plusDays(1).atStartOfDay();

                predicates.add(
                        cb.lessThan(
                                root.get("createdAt"),
                                end
                        )
                );
            }


            // -----------------------------
            // PRIORITY
            // -----------------------------

            if (priority != null) {

                predicates.add(
                        cb.equal(
                                root.get("priority"),
                                priority
                        )
                );
            }


            // -----------------------------
            // CATEGORY
            // -----------------------------

            if (category != null) {

                predicates.add(
                        cb.equal(
                                root.get("category"),
                                category
                        )
                );
            }


            // -----------------------------
            // ESCALATED
            // -----------------------------

            if (escalated != null) {

                predicates.add(
                        cb.equal(
                                root.get("escalated"),
                                escalated
                        )
                );
            }


            // -----------------------------
            // SLA STATUS
            // -----------------------------

            if (slaStatus != null) {

                LocalDateTime now =
                        LocalDateTime.now();

                switch (slaStatus.toUpperCase()) {

                    case "BREACHED":

                        predicates.add(
                                cb.isTrue(
                                        root.get("slaBreached")
                                )
                        );

                        break;


                    case "AT_RISK":

                        predicates.add(
                                cb.and(
                                        cb.isFalse(
                                                root.get("slaBreached")
                                        ),
                                        cb.greaterThan(
                                                root.get("slaDeadline"),
                                                now
                                        ),
                                        cb.lessThanOrEqualTo(
                                                root.get("slaDeadline"),
                                                now.plusHours(1)
                                        )
                                )
                        );

                        break;


                    case "COMPLIANT":

                        predicates.add(
                                cb.and(
                                        cb.isFalse(
                                                root.get("slaBreached")
                                        ),
                                        cb.greaterThan(
                                                root.get("slaDeadline"),
                                                now.plusHours(1)
                                        )
                                )
                        );

                        break;


                    default:

                        throw new IllegalArgumentException(
                                "Invalid SLA status: " + slaStatus
                        );
                }
            }

            return cb.and(
                    predicates.toArray(new Predicate[0])
            );
        };
    }

    public static Specification<Ticket> assignedTo(Long agentId) {

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        root.get("assignedAgent").get("id"),
                        agentId
                );
    }

    public static Specification<Ticket> hasStatus(
            TicketStatus status) {

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        root.get("status"),
                        status
                );
    }

    public static Specification<Ticket> hasPriority(
            TicketPriority priority) {

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        root.get("priority"),
                        priority
                );
    }

    public static Specification<Ticket> hasCategory(
            TicketCategory category) {

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        root.get("category"),
                        category
                );
    }

    public static Specification<Ticket> search(
            String search) {

        return (root, query, criteriaBuilder) -> {

            String pattern =
                    "%" + search.toLowerCase() + "%";

            return criteriaBuilder.or(

                    criteriaBuilder.like(
                            criteriaBuilder.lower(
                                    root.get("title")
                            ),
                            pattern
                    ),

                    criteriaBuilder.like(
                            criteriaBuilder.lower(
                                    root.get("description")
                            ),
                            pattern
                    )
            );
        };
    }
    
    public static Specification<Ticket> assignedTicketSearch(
            Long agentId,
            TicketStatus status,
            TicketPriority priority,
            TicketCategory category,
            String search) {

        Specification<Ticket> specification =
                assignedTo(agentId);

        if (status != null) {
            specification = specification.and(
                    hasStatus(status)
            );
        }

        if (priority != null) {
            specification = specification.and(
                    hasPriority(priority)
            );
        }

        if (category != null) {
            specification = specification.and(
                    hasCategory(category)
            );
        }

        if (search != null && !search.isBlank()) {
            specification = specification.and(
                    search(search.trim())
            );
        }

        return specification;
    }
    
    public static Specification<Ticket> createdBy(
            Long userId) {

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        root.get("createdBy").get("id"),
                        userId
                );
    }
    
    public static Specification<Ticket> customerTicketSearch(
            Long userId,
            TicketStatus status,
            TicketPriority priority,
            TicketCategory category,
            String search) {

        Specification<Ticket> specification =
                createdBy(userId);

        if (status != null) {
            specification = specification.and(
                    hasStatus(status)
            );
        }

        if (priority != null) {
            specification = specification.and(
                    hasPriority(priority)
            );
        }

        if (category != null) {
            specification = specification.and(
                    hasCategory(category)
            );
        }

        if (search != null && !search.isBlank()) {
            specification = specification.and(
                    search(search.trim())
            );
        }

        return specification;
    }
    
    
}