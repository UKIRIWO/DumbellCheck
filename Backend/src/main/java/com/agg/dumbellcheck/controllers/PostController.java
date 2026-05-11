package com.agg.dumbellcheck.controllers;

import com.agg.dumbellcheck.dto.ApiSuccessResponse;
import com.agg.dumbellcheck.dto.CursorPageResponse;
import com.agg.dumbellcheck.dto.LikeToggleResponse;
import com.agg.dumbellcheck.dto.PostCreateRequest;
import com.agg.dumbellcheck.dto.PostFeedItemResponse;
import com.agg.dumbellcheck.services.LikeService;
import com.agg.dumbellcheck.services.MediaStorageService;
import com.agg.dumbellcheck.services.PostService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/publicaciones")
public class PostController {

    private final PostService postService;
    private final MediaStorageService mediaStorageService;
    private final LikeService likeService;

    public PostController(PostService postService, MediaStorageService mediaStorageService, LikeService likeService) {
        this.postService = postService;
        this.mediaStorageService = mediaStorageService;
        this.likeService = likeService;
    }

    @PostMapping
    public ApiSuccessResponse<PostFeedItemResponse> createPost(
            @Valid @RequestBody PostCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ApiSuccessResponse.of(postService.createPost(userDetails.getUsername(), request));
    }

    @GetMapping("/publico")
    public ApiSuccessResponse<Page<PostFeedItemResponse>> getFeedPublico(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ApiSuccessResponse.of(postService.getFeedPublico(
                userDetails.getUsername(),
                PageRequest.of(page, Math.min(size, 50))));
    }

    @GetMapping("/amigos")
    public ApiSuccessResponse<Page<PostFeedItemResponse>> getFeedAmigos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ApiSuccessResponse.of(postService.getFeedAmigos(userDetails.getUsername(),
                PageRequest.of(page, Math.min(size, 50))));
    }

    @GetMapping("/descubrir")
    public ApiSuccessResponse<Page<PostFeedItemResponse>> getFeedDescubrir(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ApiSuccessResponse.of(postService.getFeedDescubrir(userDetails.getUsername(),
                PageRequest.of(page, Math.min(size, 50))));
    }

    @GetMapping("/{publicId}")
    public ApiSuccessResponse<PostFeedItemResponse> getByPublicId(
            @PathVariable String publicId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ApiSuccessResponse.of(postService.getPostByPublicId(publicId, userDetails.getUsername()));
    }

    @PostMapping("/{publicId}/like")
    public ApiSuccessResponse<LikeToggleResponse> toggleLike(
            @PathVariable String publicId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ApiSuccessResponse.of(likeService.togglePublicationLike(publicId, userDetails.getUsername()));
    }

    @PostMapping("/media")
    public ApiSuccessResponse<String> uploadMedia(@RequestParam("file") MultipartFile file) {
        return ApiSuccessResponse.of(mediaStorageService.storePublicationMedia(file));
    }

    @GetMapping("/usuario/{username}")
    public ApiSuccessResponse<CursorPageResponse<PostFeedItemResponse>> getByUser(
            @PathVariable String username,
            @RequestParam(required = false) Integer cursor,
            @RequestParam(defaultValue = "18") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ApiSuccessResponse.of(postService.getPostsByUser(
                username,
                userDetails.getUsername(),
                cursor,
                size));
    }
}
