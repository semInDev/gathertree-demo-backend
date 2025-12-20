package com.gathertree.demo.tree.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class Decoration {

    private String id;
    private String authorName;
    private String imageUrl;
    private int orderIndex;
    private LocalDateTime createdAt;
}
