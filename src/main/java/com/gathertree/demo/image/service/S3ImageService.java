package com.gathertree.demo.image.service;

import com.gathertree.demo.global.response.exception.GeneralException;
import com.gathertree.demo.global.response.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@RequiredArgsConstructor
public class S3ImageService {

    private final S3Client s3Client;

    /**
     * S3 버킷 이름
     * - 환경별로 분리 (dev / prod)
     */
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    /**
     * 외부 공개용 base URL
     * - S3 직접 접근 또는 CloudFront URL
     * - ex) https://cdn.beour.store
     */
    @Value("${cloud.aws.s3.public-url}")
    private String publicUrl;

    /**
     * ❌ overwrite 금지
     * - decoration 이미지 업로드용
     * - 한 번 생성되면 절대 변경되지 않아야 함
     */
    public String uploadNew(byte[] image, String key) {
        return upload(image, key);
    }

    /**
     * ✅ overwrite 허용
     * - 트리 base.png 수정
     * - final.png 재합성
     */
    public String overwrite(byte[] image, String key) {
        return upload(image, key);
    }

    /**
     * 실제 S3 업로드 공통 로직
     */
    private String upload(byte[] image, String key) {
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType("image/png")
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(image));

            return publicUrl + "/" + key;

        } catch (Exception e) {
            throw new GeneralException(ErrorStatus.IMAGE_UPLOAD_FAIL, e);
        }
    }
}
