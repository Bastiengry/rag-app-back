package fr.bgsoft.rag.ragappback.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.bgsoft.rag.ragappback.entity.ConversationMessage;

public interface ConversationMessageRepository extends JpaRepository<ConversationMessage, UUID> {

    List<ConversationMessage> findByConversationIdOrderBySequenceNumberAsc(UUID conversationId);

    void deleteByConversationId(UUID conversationId);
}
