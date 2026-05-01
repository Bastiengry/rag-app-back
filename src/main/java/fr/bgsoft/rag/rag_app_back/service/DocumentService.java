package fr.bgsoft.rag.rag_app_back.service;

import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class DocumentService {

    private final VectorStore vectorStore;

    public DocumentService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public void importPdf(Resource resource) {
        // Lecture avec métadonnées pour mieux filtrer plus tard
        PagePdfDocumentReader reader = new PagePdfDocumentReader(resource);
        
        // Découpage intelligent (TokenTextSplitter respecte les limites du LLM)
        TokenTextSplitter splitter = new TokenTextSplitter(800, 150, 5, 1000, true);
        
        List<Document> documents = reader.get();
        List<Document> splitDocs = splitter.apply(documents);
        
        // Envoi en base (Embedding auto-géré par Spring AI)
        vectorStore.accept(splitDocs);
    }
}