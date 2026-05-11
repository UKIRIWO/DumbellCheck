package com.agg.dumbellcheck.controllers;

import com.agg.dumbellcheck.dto.ApiSuccessResponse;
import com.agg.dumbellcheck.dto.CommentCreateRequest;
import com.agg.dumbellcheck.dto.CommentResponse;
import com.agg.dumbellcheck.dto.LikeToggleResponse;
import com.agg.dumbellcheck.services.CommentService;
import com.agg.dumbellcheck.services.LikeService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/publicaciones/{publicId}/comentarios")
public class CommentController {

    private final CommentService commentService;
    private final LikeService likeService;

    public CommentController(CommentService commentService, LikeService likeService) {
        this.commentService = commentService;
        this.likeService = likeService;
    }

    @GetMapping
    public ApiSuccessResponse<List<CommentResponse>> getComments(
            @PathVariable String publicId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ApiSuccessResponse.of(commentService.getCommentsByPublicId(publicId, userDetails.getUsername()));
    }

    @PostMapping
    public ApiSuccessResponse<CommentResponse> createComment(
            @PathVariable String publicId,
            @Valid @RequestBody CommentCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ApiSuccessResponse.of(
                commentService.createComment(publicId, userDetails.getUsername(), request));
    }

    @DeleteMapping("/{commentId}")
    public ApiSuccessResponse<Void> deleteComment(
            @PathVariable String publicId,
            @PathVariable Integer commentId,
            @AuthenticationPrincipal UserDetails userDetails) {
        commentService.deleteComment(publicId, commentId, userDetails.getUsername());
        return ApiSuccessResponse.of(null);
    }

    @PostMapping("/{commentId}/like")
    public ApiSuccessResponse<LikeToggleResponse> toggleLike(
            @PathVariable String publicId,
            @PathVariable Integer commentId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ApiSuccessResponse.of(
                likeService.toggleCommentLike(publicId, commentId, userDetails.getUsername()));
    }
}
