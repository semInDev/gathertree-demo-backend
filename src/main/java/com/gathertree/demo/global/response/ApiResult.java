package com.gathertree.demo.global.response;

import com.gathertree.demo.global.response.status.ErrorStatus;
import com.gathertree.demo.global.response.status.SuccessStatus;

public record ApiResult<T>(
        Boolean isSuccess,
        String code,
        String message,
        T data
) {

    public static <T> ApiResult<T> onSuccess(T data) {
        return new ApiResult<>(
                true,
                SuccessStatus.OK.getCode(),
                SuccessStatus.OK.getMessage(),
                data
        );
    }

    public static <T> ApiResult<T> onFailure(ErrorStatus errorStatus, T data) {
        return new ApiResult<>(
                false,
                errorStatus.getCode(),
                errorStatus.getMessage(),
                data
        );
    }
}
