package org.pts.document.storage.service.document;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pts.document.storage.service.dto.DocumentContext;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class DocumentManagerServiceImpl implements DocumentManagerService {
    private final DocumentService documentService;
    private final ExecutorService documentExecutorService;

    @Override
    public List<DocumentContext> uploadDocumentsAsync(List<UUID> documentIds) {
        log.info("Performing the document upload process, documentIds: {}", documentIds);
        List<CompletableFuture<DocumentContext>> futures = documentIds.stream()
                .map(docId ->
                        CompletableFuture.supplyAsync(() -> {
                                    try {
                                        var result = documentService.upload(docId);
                                        log.info("Document {} uploaded successfully", docId);
                                        return new DocumentContext(
                                                docId,
                                                result,
                                                "Document uploaded successfully"
                                        );
                                    } catch (Exception e) {
                                        log.error("Document {} failed to upload.", docId, e);
                                        return new DocumentContext(
                                                docId,
                                                null,
                                                "Document failed to upload: " + e.getMessage()
                                        );
                                    }
                                }, documentExecutorService
                        )
                ).toList();

        return CompletableFuture
                .allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v ->
                        futures.stream()
                                .map(CompletableFuture::join)
                                .toList()
                )
                .join();
    }

    @Override
    public List<DocumentContext> fetchDocumentsAsync(List<UUID> documentIds) {
        List<CompletableFuture<DocumentContext>> futures = documentIds.stream()
                .map(docId ->
                        CompletableFuture.supplyAsync(() -> {
                                    try {
                                        var result = documentService.getDocument(docId);
                                        return new DocumentContext(
                                                docId,
                                                result,
                                                "Fetch is done"
                                        );
                                    } catch (Exception e) {
                                        return new DocumentContext(
                                                docId,
                                                null,
                                                "Fetch failed: " + e.getMessage()
                                        );
                                    }
                                }, documentExecutorService
                        )
                ).toList();

        return CompletableFuture
                .allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v ->
                        futures.stream()
                                .map(CompletableFuture::join)
                                .toList()
                )
                .join();
    }

    @PreDestroy
    public void shutdown() throws InterruptedException {
        documentExecutorService.shutdown();

        if (!documentExecutorService.awaitTermination(30, TimeUnit.SECONDS)) {
            documentExecutorService.shutdownNow();
        }
    }
}

