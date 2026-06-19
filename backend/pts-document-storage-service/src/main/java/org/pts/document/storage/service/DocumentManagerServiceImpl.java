package org.pts.document.storage.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pts.document.storage.messaging.command.UploadDocumentCommand;
import org.pts.document.storage.service.document.DocumentService;
import org.pts.document.storage.service.dto.UploadResult;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Service
@Slf4j
@RequiredArgsConstructor
public class DocumentManagerServiceImpl implements DocumentManagerService {
    private final DocumentService documentService;
    private final ExecutorService executorService;

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
                                }, executorService
                        )
                ).toList();

        var results = CompletableFuture
                .allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v ->
                        futures.stream()
                                .map(CompletableFuture::join)
                                .toList()
                )
                .join();

        return results;
    }

    @Override
    public List<String> getDocumentAsync(List<UploadDocumentCommand.PayloadDocumentsUpload.Document> documents) {
        List<CompletableFuture<String>> futures = documents.stream()
                .map(doc ->
                        CompletableFuture.supplyAsync(() -> {
                                    try {
                                        return documentService.getDocument(doc.s3TempKey(), doc.bucket());
                                    } catch (Exception e) {
                                        throw new RuntimeException();
                                    }
                                }, executorService
                        )
                ).toList();

        var results = CompletableFuture
                .allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v ->
                        futures.stream()
                                .map(CompletableFuture::join)
                                .toList()
                )
                .join();

        return results;
    }
}
