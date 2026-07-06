package by.kazachenko.ejka.product.service.impl;

import by.kazachenko.ejka.common.service.impl.MinioServiceImpl;
import by.kazachenko.ejka.product.cache.ScanResultCache;
import by.kazachenko.ejka.product.rabbitmq.ImageProcessingEvent;
import by.kazachenko.ejka.product.rabbitmq.ImageProcessingResponse;
import by.kazachenko.ejka.product.rabbitmq.ImagePublisher;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class QuickScanServiceImpl {

    private final MinioServiceImpl minioService;
    private final ImagePublisher imagePublisher;
    private final ScanResultCache scanResultCache;
    private final ScanPermissionService scanPermissionService;

    @Value("${minio.buckets.products}")
    private String productsBucketName;

    public String initiateScan(MultipartFile file) {
        scanPermissionService.checkAndRecordScan();

        String objectKey = minioService.uploadFile(file, productsBucketName);
        String scanId = "temp_" + UUID.randomUUID();

        ImageProcessingEvent event = new ImageProcessingEvent(scanId, objectKey);
        imagePublisher.sendImageToQueue(event);

        return scanId;
    }

    public ImageProcessingResponse getResult(String scanId) {
        ImageProcessingResponse response = scanResultCache.getAndRemove(scanId);

        if (response != null) {
            minioService.deleteFile(productsBucketName, response.objectKey());
        }

        return response;
    }

}
