package by.kazachenko.ejka.product.rabbitmq;

import by.kazachenko.ejka.product.cache.ScanResultCache;
import by.kazachenko.ejka.product.service.ProductService;

import java.util.UUID;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ImageProcessingListener {

    private final ProductService productService;
    private final ScanResultCache scanResultCache;

    @RabbitListener(queues = "q.image.processing.responses")
    public void handleImageProcessingResponse(ImageProcessingResponse response) {
        log.info("Received processing response for objectKey: {}. Status: {}",
                response.objectKey(), response.status());

        if (response.id() != null && response.id().startsWith("temp_")) {
            scanResultCache.put(response.id(), response);
            return;
        }

        if ("SUCCESS".equalsIgnoreCase(response.status())) {
            try {
                productService.updateProductAdditives(
                        UUID.fromString(response.id()),
                        response.additives(),
                        response.parsedText()
                );

                log.info("Successfully updated ingredients for product: {}", response.id());
            } catch (Exception e) {
                log.error("Failed to update product ingredients for objectKey: {}", response.objectKey(), e);
            }
        } else {
            log.error("Error processing image {}: {}", response.objectKey(), response.errorMessage());
        }
    }

}
