package com.myapp.skillswap.controller;

import com.myapp.skillswap.dto.CreateSwapRequest;
import com.myapp.skillswap.dto.SwapRequestResponse;
import com.myapp.skillswap.entity.User;
import com.myapp.skillswap.exception.ResourceNotFoundException;
import com.myapp.skillswap.repository.UserRepository;
import com.myapp.skillswap.service.SwapRequestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/swaps")
public class SwapRequestController {

    private final SwapRequestService swapRequestService;
    private final UserRepository userRepository;

    public SwapRequestController(SwapRequestService swapRequestService, UserRepository userRepository) {
        this.swapRequestService = swapRequestService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<SwapRequestResponse> createSwapRequest(
            @Valid @RequestBody CreateSwapRequest request,
            Principal principal) {
        User currentUser = getAuthenticatedUser(principal);
        SwapRequestResponse response = swapRequestService.createSwapRequest(currentUser, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/incoming")
    public ResponseEntity<List<SwapRequestResponse>> getIncomingSwapRequests(Principal principal) {
        User currentUser = getAuthenticatedUser(principal);
        List<SwapRequestResponse> response = swapRequestService.getIncomingSwapRequests(currentUser);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/outgoing")
    public ResponseEntity<List<SwapRequestResponse>> getOutgoingSwapRequests(Principal principal) {
        User currentUser = getAuthenticatedUser(principal);
        List<SwapRequestResponse> response = swapRequestService.getOutgoingSwapRequests(currentUser);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/accept")
    public ResponseEntity<SwapRequestResponse> acceptSwapRequest(
            @PathVariable Long id,
            Principal principal) {
        User currentUser = getAuthenticatedUser(principal);
        SwapRequestResponse response = swapRequestService.acceptSwapRequest(currentUser, id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<SwapRequestResponse> rejectSwapRequest(
            @PathVariable Long id,
            Principal principal) {
        User currentUser = getAuthenticatedUser(principal);
        SwapRequestResponse response = swapRequestService.rejectSwapRequest(currentUser, id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<SwapRequestResponse> cancelSwapRequest(
            @PathVariable Long id,
            Principal principal) {
        User currentUser = getAuthenticatedUser(principal);
        SwapRequestResponse response = swapRequestService.cancelSwapRequest(currentUser, id);
        return ResponseEntity.ok(response);
    }

    private User getAuthenticatedUser(Principal principal) {
        if (principal == null) {
            throw new AccessDeniedException("Authentication required");
        }
        return userRepository.findByEmail(principal.getName().trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }
}
