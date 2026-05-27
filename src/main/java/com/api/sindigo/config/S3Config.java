package com.api.sindigo.config;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
@Slf4j
@Getter
public class S3Config {

    public S3Config() {
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();

        dotenv.entries().forEach(entry -> {
            if (System.getProperty(entry.getKey()) == null && System.getenv(entry.getKey()) == null) {
                System.setProperty(entry.getKey(), entry.getValue());
            }
        });

        log.info("DotEnv loaded if present");
    }

    @Value("${AWS_S3_REGION:sa-east-1}")
    private String region;

    @Value("${AWS_S3_BUCKET_NAME:}")
    private String bucketName;

    @Value("${AWS_ACCESS_KEY_ID:}")
    private String accessKey;

    @Value("${AWS_SECRET_ACCESS_KEY:}")
    private String secretKey;

    @Bean
    public S3Client s3Client() {
        validateS3Config();

        log.info("Initializing S3Client with region: {} and bucket: {}", region, bucketName);

        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(resolveCredentialsProvider())
                .build();
    }

    @Bean
    public S3Presigner s3Presigner() {
        validateS3Config();

        log.info("Initializing S3Presigner with region: {} and bucket: {}", region, bucketName);

        return S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(resolveCredentialsProvider())
                .build();
    }

    private AwsCredentialsProvider resolveCredentialsProvider() {
        boolean hasExplicitCredentials = accessKey != null && !accessKey.isBlank()
                && secretKey != null && !secretKey.isBlank();

        if (hasExplicitCredentials) {
            log.info("Using explicit AWS credentials from local environment/configuration");

            return StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(accessKey, secretKey)
            );
        }

        log.info("Using AWS DefaultCredentialsProvider. In EC2, this should use the IAM Role.");

        return DefaultCredentialsProvider.create();
    }

    private void validateS3Config() {
        if (region == null || region.isBlank()) {
            throw new IllegalStateException("AWS_S3_REGION is not configured.");
        }

        if (bucketName == null || bucketName.isBlank()) {
            throw new IllegalStateException("AWS_S3_BUCKET_NAME is not configured.");
        }
    }
}