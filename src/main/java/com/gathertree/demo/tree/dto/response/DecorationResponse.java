package com.gathertree.demo.tree.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DecorationResponse {

    private String id;
    private String authorName;
    private int orderIndex;
    private String imageUrl;
}
