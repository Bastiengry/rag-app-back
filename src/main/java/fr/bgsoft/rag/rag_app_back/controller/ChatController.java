package fr.bgsoft.rag.rag_app_back.controller;

import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "http://localhost:5173") // Pour ton app React (Vite)
public class ChatController {

    private final ChatClient chatClient;

    private final VectorStore vectorStore;

    public ChatController(final ChatClient.Builder builder, final VectorStore vectorStore) {
        // Configuration de l'advisor RAG : il va chercher le contexte tout seul
        this.chatClient = builder
                .defaultAdvisors(new QuestionAnswerAdvisor(vectorStore, SearchRequest.defaults()))
                .build();
        this.vectorStore = vectorStore;
    }

    @PostMapping("/upload")
    public ResponseEntity<ChatResponseDto> uploadPdf(@RequestParam("file") MultipartFile file) {
        try {
            // 1. Charger le PDF depuis le fichier uploadé
            Resource pdfResource = file.getResource();
            PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(pdfResource);

            // 2. Découper le texte en segments (Chunks)
            // Indispensable pour que l'IA ne soit pas submergée par trop de texte d'un coup
            TokenTextSplitter textSplitter = new TokenTextSplitter();
            List<Document> documents = textSplitter.apply(pdfReader.get());

            // 3. Envoyer les segments vers PostgreSQL (pgvector)
            // Spring AI s'occupe automatiquement de générer les embeddings via Mistral
            vectorStore.accept(documents);

            return ResponseEntity.ok(
                ChatResponseDto.builder()
                .message("Document '" + file.getOriginalFilename() + "' indexé avec succès !")
                .build()
            );
        } catch (Exception e) {
            return ResponseEntity
            .internalServerError()
            .body(ChatResponseDto.builder()
            .message("Erreur lors de l'indexation : " + e.getMessage())
            .build());
        }
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatResponseDto> stream(@RequestParam String message) {
        return chatClient.prompt()
        .system("Tu es un assistant expert. Utilise le contexte fourni pour répondre, mais si le contexte est vide, utilise tes connaissances générales.")
        .user(message)
        .stream()
        .content()
        // On s'assure que chaque fragment est bien traité comme une String UTF-8
        .map(chunk -> {
            return ChatResponseDto.builder()
            .message(chunk != null ? chunk : "")
            .build();
        });
    }
}