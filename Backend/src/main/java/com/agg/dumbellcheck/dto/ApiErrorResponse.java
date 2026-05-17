package com.agg.dumbellcheck.dto;

public record ApiErrorResponse(boolean success, String error, String errorCode, Object data) {

    public static ApiErrorResponse of(String error, String errorCode) {
        return new ApiErrorResponse(false, error, errorCode, null);
    }

    public static ApiErrorResponse of(String error, String errorCode, Object data) {
        return new ApiErrorResponse(false, error, errorCode, data);
    }
}
