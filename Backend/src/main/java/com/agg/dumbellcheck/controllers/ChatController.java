package com.agg.dumbellcheck.controllers;

import com.agg.dumbellcheck.dto.*;
import com.agg.dumbellcheck.services.ChatService;
import com.agg.dumbellcheck.services.MediaStorageService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/chats")
public class ChatController {

    private final ChatService chatService;
    private final MediaStorageService mediaStorageService;

    public ChatController(ChatService chatService, MediaStorageService mediaStorageService) {
        this.chatService = chatService;
        this.mediaStorageService = mediaStorageService;
    }

    @GetMapping
    public ApiSuccessResponse<List<ChatListItemResponse>> getMisChats(
            @AuthenticationPrincipal UserDetails user) {
        return ApiSuccessResponse.of(chatService.getMisChats(user.getUsername()));
    }

    @GetMapping("/usuarios")
    public ApiSuccessResponse<List<ChatSearchUserResponse>> searchUsers(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails user) {
        return ApiSuccessResponse.of(chatService.searchUsers(user.getUsername(), q, page, size));
    }

    @PostMapping("/directo/{username}")
    public ApiSuccessResponse<ChatDetailResponse> findOrCreateDirectChat(
            @PathVariable String username,
            @AuthenticationPrincipal UserDetails me) {
        return ApiSuccessResponse.of(chatService.findOrCreateDirectChat(me.getUsername(), username));
    }

    @PostMapping("/grupo")
    public ApiSuccessResponse<ChatDetailResponse> createGroupChat(
            @Valid @RequestBody CreateGroupChatRequest request,
            @AuthenticationPrincipal UserDetails user) {
        return ApiSuccessResponse.of(chatService.createGroupChat(user.getUsername(), request));
    }

    @GetMapping("/{publicId}")
    public ApiSuccessResponse<ChatDetailResponse> getChatDetail(
            @PathVariable String publicId,
            @AuthenticationPrincipal UserDetails user) {
        return ApiSuccessResponse.of(chatService.getChatDetail(publicId, user.getUsername()));
    }

    @GetMapping("/{publicId}/mensajes")
    public ApiSuccessResponse<CursorPageResponse<ChatMessageResponse>> getMensajes(
            @PathVariable String publicId,
            @RequestParam(required = false) Integer cursor,
            @RequestParam(defaultValue = "30") int size,
            @AuthenticationPrincipal UserDetails user) {
        return ApiSuccessResponse.of(chatService.getMensajes(publicId, user.getUsername(), cursor, size));
    }

    @PostMapping("/{publicId}/mensajes")
    public ApiSuccessResponse<ChatMessageResponse> sendMessage(
            @PathVariable String publicId,
            @Valid @RequestBody SendMessageRequest request,
            @AuthenticationPrincipal UserDetails user) {
        return ApiSuccessResponse.of(chatService.sendMessage(publicId, user.getUsername(), request));
    }

    @PostMapping("/{publicId}/mensajes/media")
    public ApiSuccessResponse<String> uploadMedia(
            @PathVariable String publicId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails user) {
        chatService.getChatDetail(publicId, user.getUsername());
        return ApiSuccessResponse.of(mediaStorageService.storePublicationMedia(file));
    }

    @PostMapping("/{publicId}/foto")
    public ApiSuccessResponse<ChatDetailResponse> uploadGroupPhoto(
            @PathVariable String publicId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails user) {
        return ApiSuccessResponse.of(chatService.uploadGroupPhoto(publicId, user.getUsername(), file));
    }

    @PutMapping("/{publicId}/mensajes/{mensajeId}")
    public ApiSuccessResponse<ChatMessageResponse> editMessage(
            @PathVariable String publicId,
            @PathVariable Integer mensajeId,
            @Valid @RequestBody UpdateMessageRequest request,
            @AuthenticationPrincipal UserDetails user) {
        return ApiSuccessResponse.of(chatService.editMessage(publicId, mensajeId, user.getUsername(), request));
    }

    @DeleteMapping("/{publicId}/mensajes/{mensajeId}")
    public ApiSuccessResponse<Void> deleteMessage(
            @PathVariable String publicId,
            @PathVariable Integer mensajeId,
            @AuthenticationPrincipal UserDetails user) {
        chatService.deleteMessage(publicId, mensajeId, user.getUsername());
        return ApiSuccessResponse.of(null);
    }

    @PutMapping("/{publicId}/vista")
    public ApiSuccessResponse<Void> updateLastSeen(
            @PathVariable String publicId,
            @AuthenticationPrincipal UserDetails user) {
        chatService.updateLastSeen(publicId, user.getUsername());
        return ApiSuccessResponse.of(null);
    }

    @PutMapping("/{publicId}")
    public ApiSuccessResponse<ChatDetailResponse> updateChat(
            @PathVariable String publicId,
            @Valid @RequestBody UpdateChatRequest request,
            @AuthenticationPrincipal UserDetails user) {
        return ApiSuccessResponse.of(chatService.updateChatNombre(publicId, user.getUsername(), request));
    }

    @DeleteMapping("/{publicId}/salir")
    public ApiSuccessResponse<Void> leaveChat(
            @PathVariable String publicId,
            @AuthenticationPrincipal UserDetails user) {
        chatService.leaveChat(publicId, user.getUsername());
        return ApiSuccessResponse.of(null);
    }

    @PostMapping("/{publicId}/participantes")
    public ApiSuccessResponse<ChatDetailResponse> addGroupMembers(
            @PathVariable String publicId,
            @Valid @RequestBody AddGroupMembersRequest request,
            @AuthenticationPrincipal UserDetails user) {
        return ApiSuccessResponse.of(chatService.addGroupMembers(publicId, user.getUsername(), request));
    }

    @PatchMapping("/{publicId}/participantes/{usuarioId}/rol")
    public ApiSuccessResponse<ChatDetailResponse> updateParticipantRole(
            @PathVariable String publicId,
            @PathVariable Integer usuarioId,
            @Valid @RequestBody UpdateParticipantRoleRequest request,
            @AuthenticationPrincipal UserDetails user) {
        return ApiSuccessResponse.of(
                chatService.updateParticipantRole(publicId, usuarioId, user.getUsername(), request));
    }
}
