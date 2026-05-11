package com.agg.dumbellcheck.controllers;

import com.agg.dumbellcheck.dto.ApiSuccessResponse;
import com.agg.dumbellcheck.dto.StatsResponse;
import com.agg.dumbellcheck.services.StatsService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/estadisticas")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/me")
    public ApiSuccessResponse<StatsResponse> getMyStats(
            @RequestParam(defaultValue = "3") int months,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ApiSuccessResponse.of(statsService.getMyStats(userDetails.getUsername(), months));
    }
}
