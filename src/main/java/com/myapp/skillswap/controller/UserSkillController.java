package com.myapp.skillswap.controller;

import com.myapp.skillswap.dto.AddUserSkillRequest;
import com.myapp.skillswap.dto.UserProfileResponse;
import com.myapp.skillswap.dto.UserSkillResponse;
import com.myapp.skillswap.entity.SkillType;
import com.myapp.skillswap.entity.User;
import com.myapp.skillswap.exception.ResourceNotFoundException;
import com.myapp.skillswap.repository.UserRepository;
import com.myapp.skillswap.service.SkillService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserSkillController {

    private final SkillService skillService;
    private final UserRepository userRepository;

    public UserSkillController(SkillService skillService, UserRepository userRepository) {
        this.skillService = skillService;
        this.userRepository = userRepository;
    }

    // -------------------------------------------------------------
    // Authenticated User Skills (/api/users/me/skills/...)
    // -------------------------------------------------------------

    @GetMapping("/me/skills")
    public ResponseEntity<List<UserSkillResponse>> getMySkills(Principal principal) {
        User user = getAuthenticatedUser(principal);
        List<UserSkillResponse> skills = skillService.getUserSkills(user.getId(), null);
        return ResponseEntity.ok(skills);
    }

    @GetMapping("/me/skills/offered")
    public ResponseEntity<List<UserSkillResponse>> getMyOfferedSkills(Principal principal) {
        User user = getAuthenticatedUser(principal);
        List<UserSkillResponse> skills = skillService.getUserSkills(user.getId(), SkillType.TEACH);
        return ResponseEntity.ok(skills);
    }

    @PostMapping("/me/skills/offered")
    public ResponseEntity<UserSkillResponse> addMyOfferedSkill(
            @Valid @RequestBody AddUserSkillRequest request,
            Principal principal) {
        User user = getAuthenticatedUser(principal);
        UserSkillResponse response = skillService.addSkillToUser(user, request, SkillType.TEACH);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/me/skills/offered/{skillId}")
    public ResponseEntity<Void> removeMyOfferedSkill(
            @PathVariable Long skillId,
            Principal principal) {
        User user = getAuthenticatedUser(principal);
        skillService.removeSkillFromUser(user, skillId, SkillType.TEACH);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me/skills/learning")
    public ResponseEntity<List<UserSkillResponse>> getMyLearningSkills(Principal principal) {
        User user = getAuthenticatedUser(principal);
        List<UserSkillResponse> skills = skillService.getUserSkills(user.getId(), SkillType.LEARN);
        return ResponseEntity.ok(skills);
    }

    @PostMapping("/me/skills/learning")
    public ResponseEntity<UserSkillResponse> addMyLearningSkill(
            @Valid @RequestBody AddUserSkillRequest request,
            Principal principal) {
        User user = getAuthenticatedUser(principal);
        UserSkillResponse response = skillService.addSkillToUser(user, request, SkillType.LEARN);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/me/skills/learning/{skillId}")
    public ResponseEntity<Void> removeMyLearningSkill(
            @PathVariable Long skillId,
            Principal principal) {
        User user = getAuthenticatedUser(principal);
        skillService.removeSkillFromUser(user, skillId, SkillType.LEARN);
        return ResponseEntity.noContent().build();
    }

    // -------------------------------------------------------------
    // Public Skill Profile (/api/users/{userId}/profile or skills)
    // -------------------------------------------------------------

    @GetMapping("/{userId}/profile")
    public ResponseEntity<UserProfileResponse> getUserProfile(@PathVariable Long userId) {
        UserProfileResponse profile = skillService.getUserPublicProfile(userId);
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/{userId}/skills")
    public ResponseEntity<UserProfileResponse> getUserSkillsProfile(@PathVariable Long userId) {
        UserProfileResponse profile = skillService.getUserPublicProfile(userId);
        return ResponseEntity.ok(profile);
    }

    // -------------------------------------------------------------
    // User-Specific Endpoints With Ownership Verification
    // -------------------------------------------------------------

    @PostMapping("/{userId}/skills/offered")
    public ResponseEntity<UserSkillResponse> addUserOfferedSkill(
            @PathVariable Long userId,
            @Valid @RequestBody AddUserSkillRequest request,
            Principal principal) {
        User user = verifyUserOwnership(userId, principal);
        UserSkillResponse response = skillService.addSkillToUser(user, request, SkillType.TEACH);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{userId}/skills/offered/{skillId}")
    public ResponseEntity<Void> removeUserOfferedSkill(
            @PathVariable Long userId,
            @PathVariable Long skillId,
            Principal principal) {
        User user = verifyUserOwnership(userId, principal);
        skillService.removeSkillFromUser(user, skillId, SkillType.TEACH);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{userId}/skills/learning")
    public ResponseEntity<UserSkillResponse> addUserLearningSkill(
            @PathVariable Long userId,
            @Valid @RequestBody AddUserSkillRequest request,
            Principal principal) {
        User user = verifyUserOwnership(userId, principal);
        UserSkillResponse response = skillService.addSkillToUser(user, request, SkillType.LEARN);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{userId}/skills/learning/{skillId}")
    public ResponseEntity<Void> removeUserLearningSkill(
            @PathVariable Long userId,
            @PathVariable Long skillId,
            Principal principal) {
        User user = verifyUserOwnership(userId, principal);
        skillService.removeSkillFromUser(user, skillId, SkillType.LEARN);
        return ResponseEntity.noContent().build();
    }

    // -------------------------------------------------------------
    // Security Helpers
    // -------------------------------------------------------------

    private User getAuthenticatedUser(Principal principal) {
        if (principal == null) {
            throw new AccessDeniedException("Authentication required");
        }
        return userRepository.findByEmail(principal.getName().trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }

    private User verifyUserOwnership(Long targetUserId, Principal principal) {
        User currentUser = getAuthenticatedUser(principal);
        if (!currentUser.getId().equals(targetUserId)) {
            throw new AccessDeniedException("You cannot modify another user's skill associations");
        }
        return currentUser;
    }
}
