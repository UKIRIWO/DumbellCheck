package com.agg.dumbellcheck.dto;

public record ApiErrorResponse(boolean success, String error, String errorCode) {

    public static ApiErrorResponse of(String error, String errorCode) {
        return new ApiErrorResponse(false, error, errorCode);
    }
}
