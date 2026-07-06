package by.kazachenko.ejka.common.service;

import org.springframework.web.multipart.MultipartFile;

public interface MinioService {

    String uploadFile(MultipartFile file, String bucketName);

    void deleteFile(String bucketName, String fileName);

}
