package com.myapp.skillswap.service;

import com.myapp.skillswap.dto.*;
import com.myapp.skillswap.entity.*;
import com.myapp.skillswap.exception.DuplicateSkillAssociationException;
import com.myapp.skillswap.exception.InvalidSkillAssociationException;
import com.myapp.skillswap.exception.ResourceNotFoundException;
import com.myapp.skillswap.repository.SkillRepository;
import com.myapp.skillswap.repository.UserRepository;
import com.myapp.skillswap.repository.UserSkillRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SkillService {

    private final SkillRepository skillRepository;
    private final UserSkillRepository userSkillRepository;
    private final UserRepository userRepository;

    public SkillService(SkillRepository skillRepository,
                        UserSkillRepository userSkillRepository,
                        UserRepository userRepository) {
        this.skillRepository = skillRepository;
        this.userSkillRepository = userSkillRepository;
        this.userRepository = userRepository;
    }

    // -------------------------------------------------------------
    // Skill Catalog Management & Search
    // -------------------------------------------------------------

    @Transactional
    public SkillResponse createSkill(SkillRequest request) {
        String trimmedName = request.getName().trim();
        Skill skill = skillRepository.findByNameIgnoreCase(trimmedName)
                .orElseGet(() -> skillRepository.save(new Skill(trimmedName, request.getDescription())));

        return mapToSkillResponse(skill);
    }

    public SkillResponse getSkillById(Long skillId) {
        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found with ID: " + skillId));
        return mapToSkillResponse(skill);
    }

    public Page<SkillResponse> searchSkills(String name, Pageable pageable) {
        Page<Skill> page;
        if (name != null && !name.trim().isEmpty()) {
            page = skillRepository.findByNameContainingIgnoreCase(name.trim(), pageable);
        } else {
            page = skillRepository.findAll(pageable);
        }
        return page.map(this::mapToSkillResponse);
    }

    // -------------------------------------------------------------
    // User Skill Associations
    // -------------------------------------------------------------

    @Transactional
    public UserSkillResponse addSkillToUser(User user, AddUserSkillRequest request, SkillType skillType) {
        Skill skill = resolveSkill(request);

        // Check if user already has this skill under the requested type
        if (userSkillRepository.existsByUserIdAndSkillIdAndSkillType(user.getId(), skill.getId(), skillType)) {
            String label = skillType == SkillType.TEACH ? "offered" : "learning";
            throw new DuplicateSkillAssociationException(
                    String.format("You already have '%s' in your %s skills list", skill.getName(), label)
            );
        }

        // Prevent contradictory associations (cannot both offer to teach and want to learn the same skill)
        SkillType oppositeType = skillType == SkillType.TEACH ? SkillType.LEARN : SkillType.TEACH;
        if (userSkillRepository.existsByUserIdAndSkillIdAndSkillType(user.getId(), skill.getId(), oppositeType)) {
            String requestedAction = skillType == SkillType.TEACH ? "teach" : "learn";
            String oppositeAction = oppositeType == SkillType.TEACH ? "teach" : "learn";
            throw new InvalidSkillAssociationException(
                    String.format("You cannot %s a skill ('%s') that you already registered to %s",
                            requestedAction, skill.getName(), oppositeAction)
            );
        }

        UserSkill userSkill = new UserSkill(
                user,
                skill,
                skillType,
                request.getProficiencyLevel(),
                request.getDescription()
        );

        UserSkill saved = userSkillRepository.save(userSkill);
        return mapToUserSkillResponse(saved);
    }

    @Transactional
    public void removeSkillFromUser(User user, Long skillId, SkillType skillType) {
        UserSkill userSkill = userSkillRepository.findByUserIdAndSkillIdAndSkillType(user.getId(), skillId, skillType)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("No %s skill association found with ID: %d",
                                skillType == SkillType.TEACH ? "offered" : "learning", skillId)
                ));

        userSkillRepository.delete(userSkill);
    }

    public List<UserSkillResponse> getUserSkills(Long userId, SkillType skillType) {
        List<UserSkill> skills = skillType != null
                ? userSkillRepository.findByUserIdAndSkillType(userId, skillType)
                : userSkillRepository.findByUserId(userId);

        return skills.stream()
                .map(this::mapToUserSkillResponse)
                .collect(Collectors.toList());
    }

    public UserProfileResponse getUserPublicProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        List<UserSkillResponse> offered = userSkillRepository.findByUserIdAndSkillType(userId, SkillType.TEACH).stream()
                .map(this::mapToUserSkillResponse)
                .collect(Collectors.toList());

        List<UserSkillResponse> learning = userSkillRepository.findByUserIdAndSkillType(userId, SkillType.LEARN).stream()
                .map(this::mapToUserSkillResponse)
                .collect(Collectors.toList());

        return new UserProfileResponse(user.getId(), user.getName(), offered, learning);
    }

    // -------------------------------------------------------------
    // Discover Skills Offered by Others
    // -------------------------------------------------------------

    public Page<OfferedSkillDiscoveryResponse> discoverOfferedSkills(Long currentUserId, String skillName, Pageable pageable) {
        Page<UserSkill> page;
        if (skillName != null && !skillName.trim().isEmpty()) {
            page = userSkillRepository.findBySkillTypeAndUserIdNotAndSkill_NameContainingIgnoreCase(
                    SkillType.TEACH, currentUserId, skillName.trim(), pageable
            );
        } else {
            page = userSkillRepository.findBySkillTypeAndUserIdNot(SkillType.TEACH, currentUserId, pageable);
        }

        return page.map(us -> new OfferedSkillDiscoveryResponse(
                us.getId(),
                us.getUser().getId(),
                us.getUser().getName(),
                us.getSkill().getId(),
                us.getSkill().getName(),
                us.getSkill().getDescription(),
                us.getProficiencyLevel(),
                us.getDescription(),
                us.getCreatedAt()
        ));
    }

    // -------------------------------------------------------------
    // Complementary Skill Matching
    // -------------------------------------------------------------

    public List<SkillMatchResponse> findComplementaryMatches(User currentUser) {
        // 1. Current user's learning (wanted) skills
        List<UserSkill> myWantedSkills = userSkillRepository.findByUserIdAndSkillType(currentUser.getId(), SkillType.LEARN);

        // 2. Current user's teaching (offered) skills
        List<UserSkill> myOfferedSkills = userSkillRepository.findByUserIdAndSkillType(currentUser.getId(), SkillType.TEACH);

        if (myWantedSkills.isEmpty() || myOfferedSkills.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> wantedSkillIds = myWantedSkills.stream()
                .map(us -> us.getSkill().getId())
                .collect(Collectors.toSet());

        Set<Long> offeredSkillIds = myOfferedSkills.stream()
                .map(us -> us.getSkill().getId())
                .collect(Collectors.toSet());

        // 3. Find other users who offer to teach what current user wants to learn
        List<UserSkill> othersTeachingWhatIWant = userSkillRepository.findBySkillTypeAndSkillIdInAndUserIdNot(
                SkillType.TEACH, wantedSkillIds, currentUser.getId()
        );

        // 4. Find other users who want to learn what current user offers to teach
        List<UserSkill> othersWantingWhatITeach = userSkillRepository.findBySkillTypeAndSkillIdInAndUserIdNot(
                SkillType.LEARN, offeredSkillIds, currentUser.getId()
        );

        // Group by other user ID
        Map<Long, List<UserSkill>> othersTeachingByUserId = othersTeachingWhatIWant.stream()
                .collect(Collectors.groupingBy(us -> us.getUser().getId()));

        Map<Long, List<UserSkill>> othersWantingByUserId = othersWantingWhatITeach.stream()
                .collect(Collectors.groupingBy(us -> us.getUser().getId()));

        // 5. Intersect to find users who satisfy both sides of the exchange
        Set<Long> matchedUserIds = new HashSet<>(othersTeachingByUserId.keySet());
        matchedUserIds.retainAll(othersWantingByUserId.keySet());

        List<SkillMatchResponse> results = new ArrayList<>();
        for (Long otherUserId : matchedUserIds) {
            List<UserSkill> theyTeach = othersTeachingByUserId.get(otherUserId);
            List<UserSkill> youTeach = othersWantingByUserId.get(otherUserId);
            User otherUser = theyTeach.get(0).getUser();

            List<SkillSummaryDto> theyTeachList = theyTeach.stream()
                    .map(us -> new SkillSummaryDto(us.getSkill().getId(), us.getSkill().getName(), us.getProficiencyLevel()))
                    .collect(Collectors.toList());

            List<SkillSummaryDto> youTeachList = youTeach.stream()
                    .map(us -> new SkillSummaryDto(us.getSkill().getId(), us.getSkill().getName(), us.getProficiencyLevel()))
                    .collect(Collectors.toList());

            UserResponse userResponse = new UserResponse(otherUser.getId(), otherUser.getName(), otherUser.getEmail());
            results.add(new SkillMatchResponse(userResponse, theyTeachList, youTeachList));
        }

        return results;
    }

    // -------------------------------------------------------------
    // Helper Methods
    // -------------------------------------------------------------

    private Skill resolveSkill(AddUserSkillRequest request) {
        if (request.getSkillId() != null) {
            return skillRepository.findById(request.getSkillId())
                    .orElseThrow(() -> new ResourceNotFoundException("Skill not found with ID: " + request.getSkillId()));
        } else if (request.getSkillName() != null && !request.getSkillName().trim().isEmpty()) {
            String trimmedName = request.getSkillName().trim();
            return skillRepository.findByNameIgnoreCase(trimmedName)
                    .orElseGet(() -> skillRepository.save(new Skill(trimmedName, null)));
        } else {
            throw new InvalidSkillAssociationException("Either skillId or skillName must be provided");
        }
    }

    private SkillResponse mapToSkillResponse(Skill skill) {
        return new SkillResponse(
                skill.getId(),
                skill.getName(),
                skill.getDescription(),
                skill.getCreatedAt(),
                skill.getUpdatedAt()
        );
    }

    private UserSkillResponse mapToUserSkillResponse(UserSkill userSkill) {
        return new UserSkillResponse(
                userSkill.getId(),
                userSkill.getSkill().getId(),
                userSkill.getSkill().getName(),
                userSkill.getSkill().getDescription(),
                userSkill.getSkillType(),
                userSkill.getProficiencyLevel(),
                userSkill.getDescription(),
                userSkill.getCreatedAt()
        );
    }
}
