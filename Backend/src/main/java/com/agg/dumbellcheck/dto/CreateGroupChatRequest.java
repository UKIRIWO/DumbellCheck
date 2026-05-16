package com.agg.dumbellcheck.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateGroupChatRequest(
        @NotEmpty @Size(max = 9) List<String> usernames,
        @Size(max = 100) String nombre
) {}
