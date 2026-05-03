package fr.bgsoft.rag.ragappback.service.impl;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import fr.bgsoft.rag.ragappback.dto.ChatResponseDto;
import fr.bgsoft.rag.ragappback.dto.ConversationMessageDto;
import fr.bgsoft.rag.ragappback.dto.LightConversationDto;
import fr.bgsoft.rag.ragappback.entity.Conversation;
import fr.bgsoft.rag.ragappback.exception.ConversationNotFoundException;
import fr.bgsoft.rag.ragappback.mapper.ConversationMapper;
import fr.bgsoft.rag.ragappback.repository.ConversationMessageRepository;
import fr.bgsoft.rag.ragappback.repository.ConversationRepository;
import fr.bgsoft.rag.ragappback.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Service
@Slf4j
public class ChatServiceImpl implements ChatService {

    private final ChatClient chatClient;
    private final ConversationRepository conversationRepository;
    private final ConversationMessageRepository messageRepository;
    private final ConversationMapper conversationMapper;

    public ChatServiceImpl(
            final ChatClient.Builder builder,
            final VectorStore vectorStore,
            final ChatMemory chatMemory,
            final ConversationRepository conversationRepository,
            final ConversationMessageRepository messageRepository,
            final ConversationMapper conversationMapper) {
        
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.conversationMapper = conversationMapper;

        this.chatClient = builder
                .defaultSystem("Tu es un assistant expert. Réponds en français. Utilise le contexte fourni pour répondre avec précision.")
                .defaultAdvisors(
                        // Advisor 1 : conversation history (Memory)
                        new MessageChatMemoryAdvisor(chatMemory),
                        // Advisor 2 : document reading (RAG)
                        new QuestionAnswerAdvisor(vectorStore, SearchRequest.defaults().withTopK(5))
                )
                .build();
    }

    @Override
    public Flux<ChatResponseDto> stream(final UUID conversationId, final String message) {
        if (!conversationRepository.existsById(conversationId)) {
            throw new ConversationNotFoundException("The conversation " + conversationId + " does not exist.");
        }

        return this.chatClient.prompt()
            .user(message)
            .advisors(a -> a
                // Conversion explicite en String pour la mémoire
                .param(MessageChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, conversationId.toString())
                // On peut aussi limiter la mémoire pour éviter que l'ancien contexte pollue trop
                .param(MessageChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10)
            )
            .stream()
            .content()
            .map(chunk -> ChatResponseDto.builder()
                    .message(chunk)
                    .build())
            .doOnComplete(() -> updateConversationTimestamp(conversationId));
            
    }

    private void updateConversationTimestamp(UUID conversationId) {
        conversationRepository.findById(conversationId).ifPresent(conv -> {
            conv.setUpdatedAt(Instant.now());
            conversationRepository.save(conv);
            log.debug("Timestamp updated for conversation : {}", conversationId);
        });
    }

    @Override
    public LightConversationDto createConversation(String title) {
        Conversation conversation = Conversation.builder()
                .title(title != null && !title.isBlank() ? title : "New conversation")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        
        return conversationMapper.toLightDto(conversationRepository.save(conversation));
    }

    @Override
    public List<ConversationMessageDto> getMessagesByConversationId(UUID conversationId) {
        return messageRepository.findByConversationIdOrderBySequenceNumberAsc(conversationId)
                .stream()
                .map(conversationMapper::toMessageDto)
                .toList();
    }

    @Override
    public List<LightConversationDto> listConversations() {
        return conversationRepository.findAll().stream()
                .map(conversationMapper::toLightDto)
                .toList();
    }

    @Override
    public void deleteConversation(UUID id) {
        conversationRepository.deleteById(id);
    }
}