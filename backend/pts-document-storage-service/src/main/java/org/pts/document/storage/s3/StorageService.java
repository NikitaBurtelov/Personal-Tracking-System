package org.pts.document.storage.s3;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public interface StorageService {
    PutObjectResponse putObject(PutObjectRequest request, RequestBody body);

    PutObjectResponse putObject(String bucket, String key, String contentType, InputStream stream) throws IOException;

    ResponseInputStream<GetObjectResponse> getObjectStream(GetObjectRequest request);

    byte[] getObjectBytes(GetObjectRequest request) throws IOException;

    HeadObjectResponse getHeadObject(HeadObjectRequest request) throws IOException;

    void deleteObject(DeleteObjectRequest request);

    Map<String, String> getMediaUrls(String bucket, List<String> key);
}
