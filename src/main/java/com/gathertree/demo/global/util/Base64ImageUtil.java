package com.gathertree.demo.global.util;

import com.gathertree.demo.global.response.exception.GeneralException;
import com.gathertree.demo.global.response.status.ErrorStatus;

import java.util.Base64;

public class Base64ImageUtil {

    public static byte[] decode(String base64) {
        // 1️⃣ null / blank 방어
        if (base64 == null || base64.isBlank()) {
            throw new GeneralException(ErrorStatus.INVALID_IMAGE_FORMAT);
        }

        // 2️⃣ data URL 형식 검증
        if (!base64.startsWith("data:image")) {
            throw new GeneralException(ErrorStatus.INVALID_IMAGE_FORMAT);
        }

        // 3️⃣ 콤마 존재 여부
        int commaIndex = base64.indexOf(',');
        if (commaIndex < 0) {
            throw new GeneralException(ErrorStatus.INVALID_IMAGE_FORMAT);
        }

        // 4️⃣ 실제 base64 부분 추출
        String encoded = base64.substring(commaIndex + 1);

        try {
            return Base64.getDecoder().decode(encoded);
        } catch (IllegalArgumentException e) {
            // 5️⃣ base64 자체가 깨진 경우
            throw new GeneralException(ErrorStatus.INVALID_IMAGE_FORMAT, e);
        }
    }
}
