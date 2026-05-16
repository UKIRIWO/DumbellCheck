package com.agg.dumbellcheck.services;

import com.agg.dumbellcheck.exceptions.ResourceConflictException;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

@Service
public class MediaStorageService {

    private static final long MAX_SIZE_BYTES = 20L * 1024L * 1024L;
    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif",
            "video/mp4",
            "video/webm",
            "video/quicktime"
    );
    private static final Set<String> IMAGE_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif"
    );

    private final Cloudinary cloudinary;

    public MediaStorageService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public String storePublicationMedia(MultipartFile file) {
        return upload(file, "dumbellcheck/publicaciones", ALLOWED_TYPES, MAX_SIZE_BYTES);
    }

    public String storeProfilePicture(MultipartFile file) {
        return upload(file, "dumbellcheck/perfiles", IMAGE_TYPES, MAX_SIZE_BYTES);
    }

    public String storeBanner(MultipartFile file) {
        return upload(file, "dumbellcheck/banners", IMAGE_TYPES, MAX_SIZE_BYTES);
    }

    public String storeGroupPhoto(MultipartFile file) {
        return upload(file, "dumbellcheck/grupos", IMAGE_TYPES, MAX_SIZE_BYTES);
    }

    private String upload(MultipartFile file, String folder, Set<String> allowedTypes, long maxBytes) {
        if (file == null || file.isEmpty()) {
            throw new ResourceConflictException("El archivo es obligatorio");
        }
        if (file.getSize() > maxBytes) {
            throw new ResourceConflictException("El archivo supera el máximo de 20MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !allowedTypes.contains(contentType)) {
            throw new ResourceConflictException("Tipo de archivo no permitido");
        }

        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "resource_type", "auto",
                            "folder", folder,
                            "use_filename", false,
                            "unique_filename", true
                    )
            );
            Object secureUrl = uploadResult.get("secure_url");
            if (secureUrl == null) {
                throw new ResourceConflictException("Cloudinary no devolvió URL del archivo");
            }
            return secureUrl.toString();
        } catch (IOException e) {
            throw new ResourceConflictException("No se pudo subir el archivo");
        }
    }
}
