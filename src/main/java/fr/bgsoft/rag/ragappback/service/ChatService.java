package fr.bgsoft.rag.ragappback.service;

import fr.bgsoft.rag.ragappback.dto.ChatResponseDto;
import reactor.core.publisher.Flux;

public interface ChatService {
    Flux<ChatResponseDto> stream(String message);
}
