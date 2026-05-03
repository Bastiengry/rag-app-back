package fr.bgsoft.rag.ragappback.service;

import java.util.List;
import java.util.UUID;

import fr.bgsoft.rag.ragappback.dto.ChatResponseDto;
import fr.bgsoft.rag.ragappback.dto.LightConversationDto;
import fr.bgsoft.rag.ragappback.dto.ConversationMessageDto;
import reactor.core.publisher.Flux;

public interface ChatService {

    Flux<ChatResponseDto> stream(final UUID conversationId, final String message);

    LightConversationDto createConversation(final String title);

    List<LightConversationDto> listConversations();

    void deleteConversation(final UUID conversationId);

    List<ConversationMessageDto> getMessagesByConversationId(UUID conversationId);
}
