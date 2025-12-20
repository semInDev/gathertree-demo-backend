package com.gathertree.demo.image.service;

import com.gathertree.demo.global.response.exception.GeneralException;
import com.gathertree.demo.global.response.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@RequiredArgsConstructor
public class S3ImageService {

    private final S3Client s3Client;
    private final String bucket = "gathertree-prod"; // TODO: @Value로 분리

    public String upload(byte[] image, String key) {
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType("image/png")
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(image));

            return "https://" + bucket + ".s3.amazonaws.com/" + key;

        } catch (Exception e) {
            throw new GeneralException(ErrorStatus.IMAGE_UPLOAD_FAIL, e);
        }
    }
}
