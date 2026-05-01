package fr.bgsoft.rag.rag_app_back.controller;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatResponseDto {
    final String message;
}
