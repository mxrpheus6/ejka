package by.kazachenko.ejka.product.cache;

import by.kazachenko.ejka.product.rabbitmq.ImageProcessingResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScanResultCache {

    private static final Duration TTL = Duration.ofMinutes(10);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public void put(String scanId, ImageProcessingResponse response) {
        try {
            String json = objectMapper.writeValueAsString(response);
            redisTemplate.opsForValue().set(scanId, json, TTL);
        } catch (JsonProcessingException e) {
            log.error("Error serializing response for Redis", e);
        }
    }

    public ImageProcessingResponse getAndRemove(String scanId) {
        try {
            String json = redisTemplate.opsForValue().getAndDelete(scanId);
            if (json != null) {
                return objectMapper.readValue(json, ImageProcessingResponse.class);
            }
        } catch (JsonProcessingException e) {
            log.error("Error deserializing response from Redis", e);
        }
        return null;
    }

}
