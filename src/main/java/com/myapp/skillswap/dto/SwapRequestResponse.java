package com.myapp.skillswap.dto;

import com.myapp.skillswap.entity.SwapRequestStatus;

import java.time.LocalDateTime;

public class SwapRequestResponse {

    private Long id;
    private UserResponse requester;
    private UserResponse recipient;
    private SkillSummaryDto offeredSkill;
    private SkillSummaryDto requestedSkill;
    private SwapRequestStatus status;
    private String message;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public SwapRequestResponse() {
    }

    public SwapRequestResponse(Long id, UserResponse requester, UserResponse recipient,
                               SkillSummaryDto offeredSkill, SkillSummaryDto requestedSkill,
                               SwapRequestStatus status, String message,
                               LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.requester = requester;
        this.recipient = recipient;
        this.offeredSkill = offeredSkill;
        this.requestedSkill = requestedSkill;
        this.status = status;
        this.message = message;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserResponse getRequester() {
        return requester;
    }

    public void setRequester(UserResponse requester) {
        this.requester = requester;
    }

    public UserResponse getRecipient() {
        return recipient;
    }

    public void setRecipient(UserResponse recipient) {
        this.recipient = recipient;
    }

    public SkillSummaryDto getOfferedSkill() {
        return offeredSkill;
    }

    public void setOfferedSkill(SkillSummaryDto offeredSkill) {
        this.offeredSkill = offeredSkill;
    }

    public SkillSummaryDto getRequestedSkill() {
        return requestedSkill;
    }

    public void setRequestedSkill(SkillSummaryDto requestedSkill) {
        this.requestedSkill = requestedSkill;
    }

    public SwapRequestStatus getStatus() {
        return status;
    }

    public void setStatus(SwapRequestStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
