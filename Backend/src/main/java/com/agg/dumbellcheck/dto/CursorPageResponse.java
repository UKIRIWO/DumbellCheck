package com.agg.dumbellcheck.dto;

import java.util.List;

public record CursorPageResponse<T>(
        List<T> content,
        Integer nextCursor,
        boolean hasMore
) {}
