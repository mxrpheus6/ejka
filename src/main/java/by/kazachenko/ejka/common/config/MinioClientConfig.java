package by.kazachenko.ejka.common.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;

import java.util.Map;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "minio")
@Data
public class MinioClientConfig {

    private String endpoint;

    private String accessKey;

    private String secretKey;

    private Map<String, String> buckets;

    @Bean
    public MinioClient minioClient() {
        MinioClient client = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();

        initBuckets(client);

        return client;
    }

    private void initBuckets(MinioClient client) {
        for (String bucket : buckets.values()) {
            try {
                boolean found = client.bucketExists(
                        BucketExistsArgs.builder().bucket(bucket).build()
                );
                if (!found) {
                    client.makeBucket(
                            MakeBucketArgs.builder().bucket(bucket).build()
                    );
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to initialize Minio bucket: " + bucket, e); //TODO
            }
        }
    }

}
