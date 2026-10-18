package com.supportai.repository;

import com.supportai.entity.ChatConversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatConversationRepository
        extends JpaRepository<ChatConversation, Long> {

    Optional<ChatConversation> findByIdAndUserEmail(
            Long id,
            String email
    );
}