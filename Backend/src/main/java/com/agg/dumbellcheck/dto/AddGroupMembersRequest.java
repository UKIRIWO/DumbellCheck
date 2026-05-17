package com.agg.dumbellcheck.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record AddGroupMembersRequest(
        @NotEmpty @Size(max = 9) List<String> usernames
) {}
