package fr.bgsoft.rag.ragappback.service;

import org.springframework.web.multipart.MultipartFile;

import fr.bgsoft.rag.ragappback.exception.DocumentImportException;

public interface DocumentService {
    void importPdf(final MultipartFile file) throws DocumentImportException;
}