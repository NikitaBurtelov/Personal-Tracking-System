package org.pts.document.storage.service;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pts.document.storage.service.document.DocumentService;
import org.pts.document.storage.service.dto.UploadResult;
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
    public List<UploadResult> uploadDocumentAsync(List<UUID> documentsId) {
        List<CompletableFuture<UploadResult>> futures = documentsId.stream()
                .map(docId ->
                        CompletableFuture.supplyAsync(() -> {
                                    try {
                                        var result = documentService.upload(docId);
                                        return new UploadResult(
                                                docId,
                                                result,
                                                "Upload is done"
                                        );
                                    } catch (Exception e) {
                                        return new UploadResult(
                                                docId,
                                                null,
                                                "Upload is failed" + e.getMessage()
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
    public List<UploadResult> getDocumentAsync(List<UUID> documentsId) {
        List<CompletableFuture<UploadResult>> futures = documentsId.stream()
                .map(docId ->
                        CompletableFuture.supplyAsync(() -> {
                                    try {
                                        var result = documentService.getDocument(docId);
                                        return new UploadResult(
                                                docId,
                                                result,
                                                "Upload is done"
                                        );
                                    } catch (Exception e) {
                                        return new UploadResult(
                                                docId,
                                                null,
                                                "Upload is failed" + e.getMessage()
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
