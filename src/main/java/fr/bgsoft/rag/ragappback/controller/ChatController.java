package fr.bgsoft.rag.ragappback.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import fr.bgsoft.rag.ragappback.dto.ChatResponseDto;
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
    public ResponseEntity<ChatResponseDto> uploadPdf(@RequestParam("file") MultipartFile file) throws DocumentImportException {
        documentService.importPdf(file);

        return ResponseEntity.ok(
            ChatResponseDto.builder()
                .message("Document '" + file.getOriginalFilename() + "' indexé avec succès !")
                .build()
        );
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatResponseDto> stream(@RequestParam String message) {
        return chatService.stream(message);
    }
}