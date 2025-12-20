package com.gathertree.demo.tree.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;

import java.util.List;

@Getter
public class DecorationReorderRequest {

    @NotEmpty(message = "장식 ID 목록은 비어 있을 수 없습니다.")
    private List<String> order;
}
