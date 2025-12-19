package com.gathertree.demo.global.response.exception;

import com.gathertree.demo.global.response.status.ErrorStatus;
import lombok.Getter;

@Getter
public class GeneralException extends RuntimeException {

    private final ErrorStatus errorStatus;
    private final Object data;

    public GeneralException(ErrorStatus errorStatus) {
        super(errorStatus.getMessage());
        this.errorStatus = errorStatus;
        this.data = null;
    }

    public GeneralException(ErrorStatus errorStatus, Object data) {
        super(errorStatus.getMessage());
        this.errorStatus = errorStatus;
        this.data = data;
    }
}
