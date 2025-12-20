package com.gathertree.demo.tree.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
public class Tree {

    private String uuid;
    private String baseImageUrl;
    private List<Decoration> decorations;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // updatedAt 수정
    public void touch() {
        this.updatedAt = LocalDateTime.now();
    }
}
