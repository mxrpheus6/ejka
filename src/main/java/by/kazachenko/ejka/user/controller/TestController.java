package by.kazachenko.ejka.user.controller;

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
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// TODO: delete this ^^
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TestController {

    private final MinioClient minioClient;

    @PreAuthorize("hasRole('MODERATOR')")
    @GetMapping("/moder/dashboard")
    public String adminDashboard() {
        return "Admin only";
    }

    @PreAuthorize("hasAnyRole('USER','MODERATOR')")
    @GetMapping("/user/profile")
    public String userProfile() {
        try {
            boolean found =
                    minioClient.bucketExists(BucketExistsArgs.builder().bucket("asiatrip").build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket("asiatrip").build());
            } else {
                System.out.println("Bucket 'asiatrip' already exists.");
            }


            minioClient.uploadObject(
                    UploadObjectArgs.builder()
                            .bucket("asiatrip")
                            .object("1.png")
                            .filename("D:\\ejka\\1.png")
                            .build());
            System.out.println("Success");
        } catch (MinioException | InvalidKeyException | IOException | NoSuchAlgorithmException e) {
            System.out.println("Error occurred: " + e);
        }
        try {
            String url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket("asiatrip")
                            .object("1.png")
                            .expiry(1, TimeUnit.HOURS)
                            .build());

            System.out.println("Ссылка на скачивание: " + url);
            return url;
        } catch (Exception e) {
            throw new RuntimeException("Ошибка получения ссылки", e);
        }
    }
}
