package com.resumeoptimizer.service.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.InputStream;
import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "storage.provider", havingValue = "s3")
public class S3StorageService implements StorageService {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket:resume-optimizer-bucket}")
    private String bucketName;

    public String uploadFile(String originalFilename, byte[] content, String contentType) {
        String objectKey = UUID.randomUUID().toString() + "-" + originalFilename;

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .contentType(contentType)
                .build();

        log.info("Uploading file to S3: {}", objectKey);
        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(content));

        return objectKey;
    }

    public byte[] downloadFile(String objectKey) {
        log.info("Downloading file from S3: {}", objectKey);
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();

        ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(getObjectRequest);
        return objectBytes.asByteArray();
    }
}
