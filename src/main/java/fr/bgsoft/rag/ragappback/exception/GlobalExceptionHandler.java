package fr.bgsoft.rag.ragappback.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import fr.bgsoft.rag.ragappback.dto.ChatResponseDto;
import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(DocumentImportException.class)
    public ResponseEntity<ChatResponseDto> handleDocumentImportException(DocumentImportException ex) {
        log.error("Document import failed", ex);
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ChatResponseDto.builder()
                .message("Erreur lors de l'indexation du PDF : " + ex.getMessage())
                .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ChatResponseDto> handleGenericException(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ChatResponseDto.builder()
                .message("Une erreur interne est survenue.")
                .build());
    }
}
