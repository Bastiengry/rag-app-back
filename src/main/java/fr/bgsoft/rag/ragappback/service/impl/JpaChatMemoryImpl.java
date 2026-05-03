package fr.bgsoft.rag.ragappback.service.impl;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;

import fr.bgsoft.rag.ragappback.entity.Conversation;
import fr.bgsoft.rag.ragappback.entity.ConversationMessage;
import fr.bgsoft.rag.ragappback.repository.ConversationMessageRepository;
import fr.bgsoft.rag.ragappback.repository.ConversationRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JpaChatMemoryImpl implements ChatMemory {

    private final ConversationMessageRepository messageRepository;
    private final ConversationRepository conversationRepository;

    @Override
    public void add(final String conversationId, final List<Message> messages) {
        UUID convUuid = UUID.fromString(conversationId);
        Conversation conversation = conversationRepository.findById(convUuid)
                .orElseThrow(() -> new RuntimeException("Conversation introuvable"));

        List<ConversationMessage> existing = messageRepository.findByConversationIdOrderBySequenceNumberAsc(convUuid);
        int nextSequence = existing.size();

        for (Message message : messages) {
            ConversationMessage entity = ConversationMessage.builder()
                    .conversation(conversation)
                    .content(message.getContent())
                    .role(mapRole(message.getMessageType()))
                    .sequenceNumber(nextSequence++)
                    .createdAt(Instant.now())
                    .build();
            messageRepository.save(entity);
        }
    }

    @Override
    public List<Message> get(final String conversationId, final int lastN) {
        UUID convUuid = UUID.fromString(conversationId);
        List<ConversationMessage> entities = messageRepository.findByConversationIdOrderBySequenceNumberAsc(convUuid);

        int start = Math.max(0, entities.size() - lastN);
        return entities.subList(start, entities.size()).stream()
                .map(this::toSpringAiMessage)
                .toList();
    }

    @Override
    public void clear(final String conversationId) {
        messageRepository.deleteByConversationId(UUID.fromString(conversationId));
    }

    // Helpers de mapping
    private ConversationMessage.Role mapRole(final MessageType type) {
        return type == MessageType.USER ? ConversationMessage.Role.USER : ConversationMessage.Role.ASSISTANT;
    }

    private Message toSpringAiMessage(ConversationMessage entity) {
        if (entity.getRole() == ConversationMessage.Role.USER) {
            return new UserMessage(entity.getContent());
        }
        return new AssistantMessage(entity.getContent());
    }
}