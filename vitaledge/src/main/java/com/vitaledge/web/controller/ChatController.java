package com.vitaledge.web.controller;

import com.vitaledge.security.SecurityUtils;
import com.vitaledge.service.ChatService;
import com.vitaledge.web.dto.conversation.ChatRequest;
import com.vitaledge.web.dto.conversation.ChatResponse;
import com.vitaledge.web.dto.conversation.ConversationResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public ResponseEntity<ChatResponse> send(@Valid @RequestBody ChatRequest request,
                                             @RequestParam(required = false) UUID conversationId) {
        UUID userId = SecurityUtils.currentUserId();
        return ResponseEntity.ok(chatService.sendMessage(userId, conversationId, request));
    }

    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationResponse>> list() {
        UUID userId = SecurityUtils.currentUserId();
        return ResponseEntity.ok(chatService.listConversations(userId));
    }

    @GetMapping("/conversations/{id}")
    public ResponseEntity<ConversationResponse> get(@PathVariable UUID id) {
        UUID userId = SecurityUtils.currentUserId();
        return ResponseEntity.ok(chatService.getConversation(userId, id));
    }
}