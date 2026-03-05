package by.kazachenko.ejka.product.rabbitmq;

import by.kazachenko.ejka.product.service.ProductService;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ImageProcessingListener {

    private final ProductService productService;

    @RabbitListener(queues = "q.image.processing.responses")
    public void handleImageProcessingResponse(ImageProcessingResponse response) {
        log.info("Received processing response for objectKey: {}. Status: {}",
                response.objectKey(), response.status());

        if ("SUCCESS".equalsIgnoreCase(response.status())) {
            try {
                productService.updateProductAdditives(
                        response.id(),
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

    private UUID extractProductId(String objectKey) {
        Pattern pattern = Pattern.compile("([0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12})");
        Matcher matcher = pattern.matcher(objectKey);

        if (matcher.find()) {
            return UUID.fromString(matcher.group(1));
        }

        throw new IllegalArgumentException("Cannot extract Product UUID from objectKey: " + objectKey);
    }

}
