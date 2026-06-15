package com.resumeoptimizer.service.storage;

public interface StorageService {
    String uploadFile(String originalFilename, byte[] content, String contentType);
    byte[] downloadFile(String storageReference);
}
