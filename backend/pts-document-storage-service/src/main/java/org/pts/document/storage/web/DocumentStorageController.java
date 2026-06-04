package org.pts.document.storage.web;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.pts.document.storage.dto.DocumentUploadRequest;
import org.pts.document.storage.dto.DocumentUploadResponse;
import org.pts.document.storage.service.DocumentService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("api/storage")
@RequiredArgsConstructor
@Log4j2
public class DocumentStorageController {
    private final DocumentService documentService;

    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @Operation(summary = "Upload images")
    public ResponseEntity<DocumentUploadResponse> create(
            @Valid
            @RequestPart("request")
            DocumentUploadRequest request,
            @RequestPart("file")
            MultipartFile file
    ) {
        log.info("Multiple file upload request received");

        var response = documentService.upload(file, request);

        return ResponseEntity.ok().body(response);
    }
}
