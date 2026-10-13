package com.myapp.skillswap.repository;

import com.myapp.skillswap.entity.SwapRequest;
import com.myapp.skillswap.entity.SwapRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SwapRequestRepository extends JpaRepository<SwapRequest, Long> {

    List<SwapRequest> findByRequesterIdOrderByCreatedAtDesc(Long requesterId);

    List<SwapRequest> findByRecipientIdOrderByCreatedAtDesc(Long recipientId);

    List<SwapRequest> findByRequesterIdAndStatus(Long requesterId, SwapRequestStatus status);

    List<SwapRequest> findByRecipientIdAndStatus(Long recipientId, SwapRequestStatus status);

    boolean existsByRequesterIdAndRecipientIdAndOfferedSkillIdAndRequestedSkillIdAndStatus(
            Long requesterId, Long recipientId, Long offeredSkillId, Long requestedSkillId, SwapRequestStatus status
    );

    @Query("SELECT COUNT(sr) > 0 FROM SwapRequest sr WHERE " +
            "((sr.requester.id = :user1Id AND sr.recipient.id = :user2Id AND sr.offeredSkill.id = :skill1Id AND sr.requestedSkill.id = :skill2Id) OR " +
            "(sr.requester.id = :user2Id AND sr.recipient.id = :user1Id AND sr.offeredSkill.id = :skill2Id AND sr.requestedSkill.id = :skill1Id)) " +
            "AND sr.status = :status")
    boolean existsPendingExchange(
            @Param("user1Id") Long user1Id,
            @Param("user2Id") Long user2Id,
            @Param("skill1Id") Long skill1Id,
            @Param("skill2Id") Long skill2Id,
            @Param("status") SwapRequestStatus status
    );
}
