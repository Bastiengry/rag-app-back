package fr.bgsoft.rag.ragappback.dto;

import java.time.Instant;
import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LightConversationDto {
    private UUID id;
    private String title;
    private Instant createdAt;
    private Instant updatedAt;
}
