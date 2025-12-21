package com.gathertree.demo.global.util;

import com.gathertree.demo.global.response.exception.GeneralException;
import com.gathertree.demo.global.response.status.ErrorStatus;

import java.util.Base64;

public class Base64ImageUtil {

    public static byte[] decode(String base64) {
        if (!base64.contains(",")) {
            throw new GeneralException(ErrorStatus.INVALID_IMAGE_FORMAT);
        }
        return Base64.getDecoder().decode(base64.split(",")[1]);
    }
}
