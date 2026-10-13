package com.myapp.skillswap.service;

import com.myapp.skillswap.dto.CreateSwapRequest;
import com.myapp.skillswap.dto.SkillSummaryDto;
import com.myapp.skillswap.dto.SwapRequestResponse;
import com.myapp.skillswap.dto.UserResponse;
import com.myapp.skillswap.entity.*;
import com.myapp.skillswap.exception.InvalidSwapRequestException;
import com.myapp.skillswap.exception.ResourceNotFoundException;
import com.myapp.skillswap.repository.SkillRepository;
import com.myapp.skillswap.repository.SwapRequestRepository;
import com.myapp.skillswap.repository.UserRepository;
import com.myapp.skillswap.repository.UserSkillRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SwapRequestService {

    private final SwapRequestRepository swapRequestRepository;
    private final UserRepository userRepository;
    private final SkillRepository skillRepository;
    private final UserSkillRepository userSkillRepository;

    public SwapRequestService(SwapRequestRepository swapRequestRepository,
                              UserRepository userRepository,
                              SkillRepository skillRepository,
                              UserSkillRepository userSkillRepository) {
        this.swapRequestRepository = swapRequestRepository;
        this.userRepository = userRepository;
        this.skillRepository = skillRepository;
        this.userSkillRepository = userSkillRepository;
    }

    @Transactional
    public SwapRequestResponse createSwapRequest(User requester, CreateSwapRequest request) {
        if (requester.getId().equals(request.getRecipientId())) {
            throw new InvalidSwapRequestException("You cannot create a swap request with yourself");
        }

        User recipient = userRepository.findById(request.getRecipientId())
                .orElseThrow(() -> new ResourceNotFoundException("Recipient user not found with ID: " + request.getRecipientId()));

        Skill offeredSkill = skillRepository.findById(request.getOfferedSkillId())
                .orElseThrow(() -> new ResourceNotFoundException("Offered skill not found with ID: " + request.getOfferedSkillId()));

        Skill requestedSkill = skillRepository.findById(request.getRequestedSkillId())
                .orElseThrow(() -> new ResourceNotFoundException("Requested skill not found with ID: " + request.getRequestedSkillId()));

        if (offeredSkill.getId().equals(requestedSkill.getId())) {
            throw new InvalidSwapRequestException("Offered skill and requested skill cannot be the same");
        }

        // Requester must have offered skill in TEACH and requested skill in LEARN
        if (!userSkillRepository.existsByUserIdAndSkillIdAndSkillType(requester.getId(), offeredSkill.getId(), SkillType.TEACH)) {
            throw new InvalidSwapRequestException("You can only offer skills that are in your offered (TEACH) skills list");
        }
        if (!userSkillRepository.existsByUserIdAndSkillIdAndSkillType(requester.getId(), requestedSkill.getId(), SkillType.LEARN)) {
            throw new InvalidSwapRequestException("You can only request skills that are in your learning (LEARN) skills list");
        }

        // Recipient must have requested skill in TEACH and offered skill in LEARN
        if (!userSkillRepository.existsByUserIdAndSkillIdAndSkillType(recipient.getId(), requestedSkill.getId(), SkillType.TEACH)) {
            throw new InvalidSwapRequestException("Recipient does not offer the requested skill");
        }
        if (!userSkillRepository.existsByUserIdAndSkillIdAndSkillType(recipient.getId(), offeredSkill.getId(), SkillType.LEARN)) {
            throw new InvalidSwapRequestException("Recipient is not seeking to learn the offered skill");
        }

        // Check for duplicate pending requests between these users for these skills in either direction
        if (swapRequestRepository.existsPendingExchange(
                requester.getId(), recipient.getId(), offeredSkill.getId(), requestedSkill.getId(), SwapRequestStatus.PENDING)) {
            throw new InvalidSwapRequestException("A pending swap request already exists for these skills between these users");
        }

        SwapRequest swapRequest = new SwapRequest(
                requester,
                recipient,
                offeredSkill,
                requestedSkill,
                request.getMessage() != null ? request.getMessage().trim() : null
        );

        SwapRequest saved = swapRequestRepository.save(swapRequest);
        return mapToResponse(saved);
    }

    public List<SwapRequestResponse> getIncomingSwapRequests(User recipient) {
        return swapRequestRepository.findByRecipientIdOrderByCreatedAtDesc(recipient.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<SwapRequestResponse> getOutgoingSwapRequests(User requester) {
        return swapRequestRepository.findByRequesterIdOrderByCreatedAtDesc(requester.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public SwapRequestResponse acceptSwapRequest(User currentUser, Long swapRequestId) {
        SwapRequest swap = swapRequestRepository.findById(swapRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("Swap request not found with ID: " + swapRequestId));

        if (!swap.getRecipient().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Only the recipient can accept this swap request");
        }

        if (swap.getStatus() != SwapRequestStatus.PENDING) {
            throw new InvalidSwapRequestException("Cannot accept swap request that is not pending (current status: " + swap.getStatus() + ")");
        }

        swap.setStatus(SwapRequestStatus.ACCEPTED);
        SwapRequest updated = swapRequestRepository.save(swap);
        return mapToResponse(updated);
    }

    @Transactional
    public SwapRequestResponse rejectSwapRequest(User currentUser, Long swapRequestId) {
        SwapRequest swap = swapRequestRepository.findById(swapRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("Swap request not found with ID: " + swapRequestId));

        if (!swap.getRecipient().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Only the recipient can reject this swap request");
        }

        if (swap.getStatus() != SwapRequestStatus.PENDING) {
            throw new InvalidSwapRequestException("Cannot reject swap request that is not pending (current status: " + swap.getStatus() + ")");
        }

        swap.setStatus(SwapRequestStatus.REJECTED);
        SwapRequest updated = swapRequestRepository.save(swap);
        return mapToResponse(updated);
    }

    @Transactional
    public SwapRequestResponse cancelSwapRequest(User currentUser, Long swapRequestId) {
        SwapRequest swap = swapRequestRepository.findById(swapRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("Swap request not found with ID: " + swapRequestId));

        if (!swap.getRequester().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Only the requester can cancel this swap request");
        }

        if (swap.getStatus() != SwapRequestStatus.PENDING) {
            throw new InvalidSwapRequestException("Cannot cancel swap request that is not pending (current status: " + swap.getStatus() + ")");
        }

        swap.setStatus(SwapRequestStatus.CANCELLED);
        SwapRequest updated = swapRequestRepository.save(swap);
        return mapToResponse(updated);
    }

    private SwapRequestResponse mapToResponse(SwapRequest swap) {
        ProficiencyLevel offeredProficiency = userSkillRepository
                .findByUserIdAndSkillIdAndSkillType(swap.getRequester().getId(), swap.getOfferedSkill().getId(), SkillType.TEACH)
                .map(UserSkill::getProficiencyLevel)
                .orElse(null);

        ProficiencyLevel requestedProficiency = userSkillRepository
                .findByUserIdAndSkillIdAndSkillType(swap.getRecipient().getId(), swap.getRequestedSkill().getId(), SkillType.TEACH)
                .map(UserSkill::getProficiencyLevel)
                .orElse(null);

        UserResponse requesterDto = new UserResponse(
                swap.getRequester().getId(),
                swap.getRequester().getName(),
                swap.getRequester().getEmail()
        );

        UserResponse recipientDto = new UserResponse(
                swap.getRecipient().getId(),
                swap.getRecipient().getName(),
                swap.getRecipient().getEmail()
        );

        SkillSummaryDto offeredSkillDto = new SkillSummaryDto(
                swap.getOfferedSkill().getId(),
                swap.getOfferedSkill().getName(),
                offeredProficiency
        );

        SkillSummaryDto requestedSkillDto = new SkillSummaryDto(
                swap.getRequestedSkill().getId(),
                swap.getRequestedSkill().getName(),
                requestedProficiency
        );

        return new SwapRequestResponse(
                swap.getId(),
                requesterDto,
                recipientDto,
                offeredSkillDto,
                requestedSkillDto,
                swap.getStatus(),
                swap.getMessage(),
                swap.getCreatedAt(),
                swap.getUpdatedAt()
        );
    }
}
