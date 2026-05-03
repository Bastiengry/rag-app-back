package fr.bgsoft.rag.ragappback.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationMessageDto {
    private UUID id;
    private String content;
    private String role; // "USER" or "ASSISTANT"
    private Instant createdAt;
    private Integer sequenceNumber;
}