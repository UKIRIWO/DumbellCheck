package com.agg.dumbellcheck.dto;

public record ApiSuccessResponse<T>(boolean success, T data) {

    public static <T> ApiSuccessResponse<T> of(T data) {
        return new ApiSuccessResponse<>(true, data);
    }
}
