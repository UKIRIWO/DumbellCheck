package com.agg.dumbellcheck.controllers;

import com.agg.dumbellcheck.dto.ApiSuccessResponse;
import com.agg.dumbellcheck.dto.PostCreateRequest;
import com.agg.dumbellcheck.dto.PostFeedItemResponse;
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

    public PostController(PostService postService, MediaStorageService mediaStorageService) {
        this.postService = postService;
        this.mediaStorageService = mediaStorageService;
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
            @RequestParam(defaultValue = "20") int size) {
        return ApiSuccessResponse.of(postService.getFeedPublico(PageRequest.of(page, Math.min(size, 50))));
    }

    @GetMapping("/{id}")
    public ApiSuccessResponse<PostFeedItemResponse> getById(@PathVariable Integer id) {
        return ApiSuccessResponse.of(postService.getPostById(id));
    }

    @PostMapping("/media")
    public ApiSuccessResponse<String> uploadMedia(@RequestParam("file") MultipartFile file) {
        return ApiSuccessResponse.of(mediaStorageService.storePublicationMedia(file));
    }
}
