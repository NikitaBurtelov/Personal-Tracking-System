package org.pts.document.storage.s3;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
class StorageServiceImpl implements StorageService {
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Override
    public PutObjectResponse putObject(PutObjectRequest request, RequestBody body) {
        return s3Client.putObject(request, body);
    }

    @Override
    public PutObjectResponse putObject(
            String bucket,
            String key,
            String contentType,
            InputStream stream
    ) throws IOException {
        return s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .contentType(contentType)
                        .build(),
                RequestBody.fromInputStream(stream, stream.available())
        );
    }

    @Override
    public ResponseInputStream<GetObjectResponse> getObjectStream(GetObjectRequest request) {
        return s3Client.getObject(request);
    }

    @Override
    public byte[] getObjectBytes(GetObjectRequest request) throws IOException {
        return s3Client.getObject(request).readAllBytes();
    }


    @Override
    public HeadObjectResponse getHeadObject(HeadObjectRequest request) {
        return s3Client.headObject(request);
    }


    @Override
    public void deleteObject(DeleteObjectRequest request) {
        s3Client.deleteObject(request);
    }

    @Override
    public Map<String, String> getMediaUrls(String bucket, List<String> keys) {
        return keys.stream()
                .collect(Collectors.toMap(
                        key -> key,
                        key -> {
                            var objectRequest = GetObjectRequest.builder()
                                    .bucket(bucket)
                                    .key(key)
                                    .build();
                            var presignedRequest = GetObjectPresignRequest.builder()
                                    .signatureDuration(Duration.ofMinutes(30))
                                    .getObjectRequest(objectRequest)
                                    .build();

                            return s3Presigner.presignGetObject(presignedRequest)
                                    .url()
                                    .toString();
                        },
                        (a, b) -> a
                ));
    }
}