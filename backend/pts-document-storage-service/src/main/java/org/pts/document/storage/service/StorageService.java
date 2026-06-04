package org.pts.document.storage.service;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

interface StorageService {
    PutObjectResponse putObject(PutObjectRequest request, RequestBody body);

    PutObjectResponse putObject(String bucket, String key, String contentType, InputStream stream) throws IOException;

    ResponseInputStream<GetObjectResponse> getObject(GetObjectRequest request);

    void deleteObject(DeleteObjectRequest request);

    Map<String, String> getMediaUrls(String bucket, List<String> key);
}
