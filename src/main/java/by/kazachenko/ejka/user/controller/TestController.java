package by.kazachenko.ejka.user.controller;

import by.kazachenko.ejka.product.rabbitmq.ImagePublisher;
import by.kazachenko.ejka.product.service.ProductImageService;
import io.minio.BucketExistsArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import io.minio.errors.MinioException;
import io.minio.http.Method;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

// TODO: delete this ^^
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TestController {

    private final ProductImageService productImageService;

    private final ImagePublisher imagePublisher;

    @PreAuthorize("hasRole('MODERATOR')")
    @GetMapping("/moder/dashboard")
    public String adminDashboard() {
        return "Admin only";
    }

    @PreAuthorize("hasAnyRole('USER','MODERATOR')")
    @PostMapping("/user/profile")
    public String userProfile() {
        return "User only";
    }
}
