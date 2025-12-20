package com.gathertree.demo.tree.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class DecorationCreateRequest {

    @NotBlank(message = "장식 이미지는 필수입니다.")
    private String imageBase64;

    @NotBlank(message = "닉네임은 필수입니다.")
    private String authorName;
}
