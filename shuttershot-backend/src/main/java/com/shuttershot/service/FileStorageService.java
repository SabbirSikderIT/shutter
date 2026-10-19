package com.shuttershot.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.shuttershot.exception.FileStorageException;
import com.shuttershot.exception.InvalidFileException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final Cloudinary cloudinary;

    @Value("${cloudinary.folder}")
    private String cloudFolder;

    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("Uploaded file must not be empty");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new InvalidFileException("Only image files are allowed");
        }

        try {
            String originalFilename = StringUtils.cleanPath(
                    file.getOriginalFilename() == null ? "image" : file.getOriginalFilename());
            String extension = "";
            int dotIndex = originalFilename.lastIndexOf('.');
            if (dotIndex >= 0) {
                extension = originalFilename.substring(dotIndex);
            }
            String storedFilename = UUID.randomUUID() + extension;

            // Upload to Cloudinary
            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "public_id", cloudFolder + "/" + storedFilename,
                            "resource_type", "image"
                    )
            );

            return uploadResult.get("secure_url").toString();
        } catch (IOException e) {
            throw new FileStorageException("Failed to store uploaded file", e);
        }
    }

    public String storeBytes(byte[] bytes, String extension) {
        try {
            String storedFilename = UUID.randomUUID() + extension;
            
            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    bytes,
                    ObjectUtils.asMap(
                            "public_id", cloudFolder + "/" + storedFilename,
                            "resource_type", "image"
                    )
            );
            
            return uploadResult.get("secure_url").toString();
        } catch (IOException e) {
            throw new FileStorageException("Failed to store file", e);
        }
    }

    public byte[] loadBytes(String imageUrl) {
        try {
            // URL থেকে ফাইলের নাম বা Public ID বের করা
            // উদাহরণ: https://res.cloudinary.com/fcaagnjo/image/upload/shuttercloud/uuid.jpg
            // থেকে শুধু "shuttercloud/uuid.jpg" নিতে হবে
            String publicId = imageUrl.substring(imageUrl.indexOf(cloudFolder));
            
            // Cloudinary থেকে ছবি ডাউনলোড করা (এআই চেকিং এর জন্য)
            java.net.URL url = java.net.URI.create(imageUrl).toURL();            try (java.io.InputStream in = url.openStream()) {
                return in.readAllBytes();
            }
        } catch (Exception e) {
            throw new FileStorageException("Failed to read stored file from Cloudinary", e);
        }
    }

    public void delete(String imageUrl) {
        if (imageUrl == null || !imageUrl.contains(cloudFolder)) {
            return;
        }
        try {
            // URL থেকে public_id বের করা
            String publicId = imageUrl.substring(imageUrl.indexOf(cloudFolder));
            // এক্সটেনশন বাদ দেওয়া (Cloudinary ডিলিট করার সময় এক্সটেনশন ছাড়া public_id চায়)
            int dotIndex = publicId.lastIndexOf('.');
            if (dotIndex >= 0) {
                publicId = publicId.substring(0, dotIndex);
            }
            
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (IOException e) {
            throw new FileStorageException("Failed to delete file from Cloudinary", e);
        }
    }
}