package com.resumeoptimizer.service.storage;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@ConditionalOnProperty(name = "storage.provider", havingValue = "cloudinary", matchIfMissing = true)
public class CloudinaryStorageService implements StorageService {

    private final Cloudinary cloudinary;

    public CloudinaryStorageService(
            @Value("${cloudinary.cloud-name}") String cloudName,
            @Value("${cloudinary.api-key}") String apiKey,
            @Value("${cloudinary.api-secret}") String apiSecret) {
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true
        ));
    }

    @Override
    public String uploadFile(String originalFilename, byte[] content, String contentType) {
        try {
            // Provide a unique public_id
            String publicId = "resumes/" + UUID.randomUUID().toString();
            
            // For raw files like PDF or DOCX, specify resource_type = auto or raw
            String resourceType = contentType.startsWith("image/") ? "image" : "raw";

            log.info("Uploading file to Cloudinary: {}", publicId);
            Map<?, ?> uploadResult = cloudinary.uploader().upload(content, ObjectUtils.asMap(
                    "public_id", publicId,
                    "resource_type", resourceType,
                    "use_filename", true
            ));

            return (String) uploadResult.get("public_id");
        } catch (Exception e) {
            log.error("Failed to upload file to Cloudinary", e);
            throw new RuntimeException("Failed to upload file to Cloudinary", e);
        }
    }

    @Override
    public byte[] downloadFile(String storageReference) {
        try {
            log.info("Downloading file from Cloudinary: {}", storageReference);
            // We need to figure out the resource_type. If we stored it as 'raw', the URL will reflect that.
            // Cloudinary's API doesn't easily return byte[] directly without a URL fetch for raw files
            // Let's generate the secure URL and fetch it manually.
            // Since resumes are typically PDFs (raw), we default to raw unless we know better.
            
            // To make it robust, we construct the download URL using the cloudinary SDK.
            String urlString = cloudinary.url().resourceType("raw").secure(true).generate(storageReference);
            
            URL url = new URL(urlString);
            try (InputStream in = url.openStream(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
                return out.toByteArray();
            }
        } catch (Exception e) {
            log.error("Failed to download file from Cloudinary", e);
            throw new RuntimeException("Failed to download file from Cloudinary", e);
        }
    }
}
