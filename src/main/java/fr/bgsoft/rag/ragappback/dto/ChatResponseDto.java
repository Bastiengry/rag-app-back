package fr.bgsoft.rag.ragappback.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatResponseDto {
    final String message;
}
