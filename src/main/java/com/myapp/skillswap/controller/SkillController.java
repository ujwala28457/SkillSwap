package com.myapp.skillswap.controller;

import com.myapp.skillswap.dto.*;
import com.myapp.skillswap.entity.User;
import com.myapp.skillswap.exception.ResourceNotFoundException;
import com.myapp.skillswap.repository.UserRepository;
import com.myapp.skillswap.service.SkillService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/skills")
public class SkillController {

    private final SkillService skillService;
    private final UserRepository userRepository;

    public SkillController(SkillService skillService, UserRepository userRepository) {
        this.skillService = skillService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<SkillResponse> createSkill(@Valid @RequestBody SkillRequest request) {
        SkillResponse response = skillService.createSkill(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<SkillResponse>> searchSkills(
            @RequestParam(required = false) String name,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<SkillResponse> skills = skillService.searchSkills(name, pageable);
        return ResponseEntity.ok(skills);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SkillResponse> getSkillById(@PathVariable Long id) {
        SkillResponse skill = skillService.getSkillById(id);
        return ResponseEntity.ok(skill);
    }

    @GetMapping("/offered")
    public ResponseEntity<Page<OfferedSkillDiscoveryResponse>> discoverOfferedSkills(
            @RequestParam(required = false) String name,
            @PageableDefault(size = 10) Pageable pageable,
            Principal principal) {
        User currentUser = getAuthenticatedUser(principal);
        Page<OfferedSkillDiscoveryResponse> discovered = skillService.discoverOfferedSkills(currentUser.getId(), name, pageable);
        return ResponseEntity.ok(discovered);
    }

    @GetMapping("/matches")
    public ResponseEntity<List<SkillMatchResponse>> getComplementaryMatches(Principal principal) {
        User currentUser = getAuthenticatedUser(principal);
        List<SkillMatchResponse> matches = skillService.findComplementaryMatches(currentUser);
        return ResponseEntity.ok(matches);
    }

    private User getAuthenticatedUser(Principal principal) {
        if (principal == null) {
            throw new AccessDeniedException("Authentication required");
        }
        return userRepository.findByEmail(principal.getName().trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }
}
