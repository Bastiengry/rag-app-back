package fr.bgsoft.rag.ragappback.service.impl;

import java.util.List;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import fr.bgsoft.rag.ragappback.exception.DocumentImportException;
import fr.bgsoft.rag.ragappback.service.DocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentServiceImpl implements DocumentService {
    private final VectorStore vectorStore;

    @Override
    public void importPdf(@RequestParam("file") final MultipartFile file) throws DocumentImportException {
        if (file == null) {
            throw new DocumentImportException("No file provided for import.");
        }

        try {
            log.info("Starting to upload PDF file");

            // Loads the PDF file
            final byte[] bytes = file.getBytes();
            try (PDDocument document = Loader.loadPDF(bytes)) {
                PDFTextStripper stripper = new PDFTextStripper();
                String plainText = stripper.getText(document);

                if (plainText == null || plainText.isBlank()) {
                    throw new RuntimeException("Document does not contain text or the text cannot be extracted.");
                }

                log.info("Read text in document: " + plainText.substring(0, Math.min(100, plainText.length())));

                // 2. Conversion pour Spring AI
                Document springDoc = new Document(plainText);
                
                // 3. Découpage en fragments (chunks)
                TokenTextSplitter textSplitter = new TokenTextSplitter(800, 100, 5, 10000, true);
                List<Document> documents = textSplitter.apply(List.of(springDoc));


                log.info("Created text segments (after splitting) : " + documents.size());

                if (documents.isEmpty()) {
                    throw new RuntimeException("Unable to split text from PDF !");
                }

                log.info("Text sample: " + documents.get(0).getContent().substring(0, 100));

                documents.forEach(doc -> {
                    doc.getMetadata().put("source", file.getOriginalFilename());
                });

                // Sends the fragments to the database
                // Spring AI automatically generates the embeddings
                vectorStore.accept(documents);

                log.info("=> Succeeded to upload PDF file");
            } 
        } catch (Exception e) {
            throw new DocumentImportException(String.format("Failed to import document %s",file.getOriginalFilename()), e);
        }
    }
}
