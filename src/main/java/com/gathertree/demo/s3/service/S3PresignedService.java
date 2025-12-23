package com.gathertree.demo.s3.service;

import com.gathertree.demo.global.response.exception.GeneralException;
import com.gathertree.demo.global.response.status.ErrorStatus;
import com.gathertree.demo.s3.dto.PresignedUrlResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class S3PresignedService {

    private static final Duration EXPIRE_TIME = Duration.ofMinutes(5);

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.s3.public-url}")
    private String publicUrl;

    public PresignedUrlResponse generatePresignedPutUrl(String key) {

        try (S3Presigner presigner = S3Presigner.create()) {

            PutObjectRequest objectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType("image/png")
                    .build();

            PutObjectPresignRequest presignRequest =
                    PutObjectPresignRequest.builder()
                            .signatureDuration(EXPIRE_TIME)
                            .putObjectRequest(objectRequest)
                            .build();

            String uploadUrl = presigner
                    .presignPutObject(presignRequest)
                    .url()
                    .toString();

            String finalPublicUrl = publicUrl + "/" + key;

            return new PresignedUrlResponse(uploadUrl, finalPublicUrl);

        } catch (Exception e) {
            throw new GeneralException(
                    ErrorStatus.IMAGE_UPLOAD_FAIL,
                    "Presigned URL 발급 중 오류가 발생했습니다.",
                    e
            );
        }
    }
}
