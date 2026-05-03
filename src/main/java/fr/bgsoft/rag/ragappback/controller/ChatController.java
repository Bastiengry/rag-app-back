package fr.bgsoft.rag.ragappback.controller;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import fr.bgsoft.rag.ragappback.dto.ChatResponseDto;
import fr.bgsoft.rag.ragappback.dto.ConversationMessageDto;
import fr.bgsoft.rag.ragappback.dto.LightConversationDto;
import fr.bgsoft.rag.ragappback.exception.DocumentImportException;
import fr.bgsoft.rag.ragappback.service.ChatService;
import fr.bgsoft.rag.ragappback.service.DocumentService;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "http://localhost:5173") // For react application with "vite"
@Slf4j
public class ChatController {

    private final ChatService chatService;

    private final DocumentService documentService;

    public ChatController(final ChatService chatService, final DocumentService documentService) {
        this.chatService = chatService;
        this.documentService = documentService;
    }

    @PostMapping("/upload")
    public ResponseEntity<ChatResponseDto> uploadPdf(@RequestParam("file") final MultipartFile file) throws DocumentImportException {
        Objects.requireNonNull(file, "file must not be null");
        documentService.importPdf(file);

        return ResponseEntity.ok(
            ChatResponseDto.builder()
                .message("Document '" + file.getOriginalFilename() + "' indexed successfully !")
                .build()
        );
    }

    @PostMapping("/new")
    public ResponseEntity<LightConversationDto> newConversation(@RequestParam(required = false) final String title) {
        return ResponseEntity.ok(chatService.createConversation(title));
    }

    @GetMapping("/conversations")
    public ResponseEntity<List<LightConversationDto>> listConversations() {
        return ResponseEntity.ok(chatService.listConversations());
    }

    
    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<List<ConversationMessageDto>> getMessagesByConversationId(@PathVariable final UUID conversationId) {
        return ResponseEntity.ok(chatService.getMessagesByConversationId(conversationId));
    }

    @DeleteMapping("/conversations/{conversationId}")
    public ResponseEntity<ChatResponseDto> deleteConversation(@PathVariable final UUID conversationId) {
        Objects.requireNonNull(conversationId, "conversationId must not be null");
        chatService.deleteConversation(conversationId);
        return ResponseEntity.ok(
            ChatResponseDto.builder()
                .message("Conversation deleted.")
                .build()
        );
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatResponseDto> stream(@RequestParam final UUID conversationId, @RequestParam final String message) {
        Objects.requireNonNull(conversationId, "conversationId must not be null");
        Objects.requireNonNull(message, "message must not be null");
        return chatService.stream(conversationId, message);
    }
}
