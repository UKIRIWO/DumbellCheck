package com.agg.dumbellcheck.dto;

public record LikeToggleResponse(
        boolean meGusta,
        Integer contadorLikes
) {}
