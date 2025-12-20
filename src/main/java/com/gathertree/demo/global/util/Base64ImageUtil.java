package com.gathertree.demo.global.util;

import java.util.Base64;

public class Base64ImageUtil {

    public static byte[] decode(String base64) {
        if (!base64.contains(",")) {
            throw new IllegalArgumentException("Invalid base64 image format");
        }
        return Base64.getDecoder().decode(base64.split(",")[1]);
    }
}
