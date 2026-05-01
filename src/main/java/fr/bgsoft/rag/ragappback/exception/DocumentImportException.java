package fr.bgsoft.rag.ragappback.exception;

public class DocumentImportException extends Exception {
    public DocumentImportException(String message) {
        super(message);
    }

    public DocumentImportException(String message, Throwable cause) {
        super(message, cause);
    }
}
