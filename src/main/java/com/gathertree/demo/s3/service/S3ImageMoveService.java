package com.gathertree.demo.s3.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;

@Service
@RequiredArgsConstructor
public class S3ImageMoveService {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public String moveTmpToPublic(String tmpKey) {

        if (!tmpKey.startsWith("eval/tmp/")) {
            throw new IllegalArgumentException("tmp 경로만 이동 가능합니다.");
        }

        String publicKey = tmpKey.replace("eval/tmp/", "eval/public/");

        // 1️⃣ copy
        s3Client.copyObject(
                b -> b.sourceBucket(bucket)
                        .sourceKey(tmpKey)
                        .destinationBucket(bucket)
                        .destinationKey(publicKey)
        );

        // 2️⃣ delete
        s3Client.deleteObject(
                b -> b.bucket(bucket)
                        .key(tmpKey)
        );

        return publicKey;
    }
}

