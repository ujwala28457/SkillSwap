package com.myapp.skillswap.repository;

import com.myapp.skillswap.entity.SkillType;
import com.myapp.skillswap.entity.UserSkill;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserSkillRepository extends JpaRepository<UserSkill, Long> {

    List<UserSkill> findByUserId(Long userId);

    List<UserSkill> findByUserIdAndSkillType(Long userId, SkillType skillType);

    Optional<UserSkill> findByUserIdAndSkillIdAndSkillType(Long userId, Long skillId, SkillType skillType);

    boolean existsByUserIdAndSkillIdAndSkillType(Long userId, Long skillId, SkillType skillType);

    boolean existsByUserIdAndSkillId(Long userId, Long skillId);

    void deleteByUserIdAndSkillIdAndSkillType(Long userId, Long skillId, SkillType skillType);

    Page<UserSkill> findBySkillTypeAndUserIdNot(SkillType skillType, Long userId, Pageable pageable);

    Page<UserSkill> findBySkillTypeAndUserIdNotAndSkill_NameContainingIgnoreCase(SkillType skillType, Long userId, String skillName, Pageable pageable);

    List<UserSkill> findBySkillTypeAndSkillIdInAndUserIdNot(SkillType skillType, Collection<Long> skillIds, Long userId);
}
