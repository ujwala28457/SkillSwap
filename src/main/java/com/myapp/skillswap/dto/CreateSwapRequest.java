package com.myapp.skillswap.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateSwapRequest {

    @NotNull(message = "Recipient ID is required")
    private Long recipientId;

    @NotNull(message = "Offered skill ID is required")
    private Long offeredSkillId;

    @NotNull(message = "Requested skill ID is required")
    private Long requestedSkillId;

    @Size(max = 500, message = "Message cannot exceed 500 characters")
    private String message;

    public CreateSwapRequest() {
    }

    public CreateSwapRequest(Long recipientId, Long offeredSkillId, Long requestedSkillId, String message) {
        this.recipientId = recipientId;
        this.offeredSkillId = offeredSkillId;
        this.requestedSkillId = requestedSkillId;
        this.message = message;
    }

    public Long getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(Long recipientId) {
        this.recipientId = recipientId;
    }

    public Long getOfferedSkillId() {
        return offeredSkillId;
    }

    public void setOfferedSkillId(Long offeredSkillId) {
        this.offeredSkillId = offeredSkillId;
    }

    public Long getRequestedSkillId() {
        return requestedSkillId;
    }

    public void setRequestedSkillId(Long requestedSkillId) {
        this.requestedSkillId = requestedSkillId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
