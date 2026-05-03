package fr.bgsoft.rag.ragappback.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.bgsoft.rag.ragappback.entity.Conversation;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

    List<Conversation> findAllByOrderByUpdatedAtDesc();
}
