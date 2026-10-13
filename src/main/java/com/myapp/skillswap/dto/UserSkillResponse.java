package com.myapp.skillswap.dto;

import com.myapp.skillswap.entity.ProficiencyLevel;
import com.myapp.skillswap.entity.SkillType;

import java.time.LocalDateTime;

public class UserSkillResponse {

    private Long id;
    private Long skillId;
    private String skillName;
    private String skillDescription;
    private SkillType skillType;
    private ProficiencyLevel proficiencyLevel;
    private String description;
    private LocalDateTime createdAt;

    public UserSkillResponse() {
    }

    public UserSkillResponse(Long id, Long skillId, String skillName, String skillDescription,
                             SkillType skillType, ProficiencyLevel proficiencyLevel,
                             String description, LocalDateTime createdAt) {
        this.id = id;
        this.skillId = skillId;
        this.skillName = skillName;
        this.skillDescription = skillDescription;
        this.skillType = skillType;
        this.proficiencyLevel = proficiencyLevel;
        this.description = description;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSkillId() {
        return skillId;
    }

    public void setSkillId(Long skillId) {
        this.skillId = skillId;
    }

    public String getSkillName() {
        return skillName;
    }

    public void setSkillName(String skillName) {
        this.skillName = skillName;
    }

    public String getSkillDescription() {
        return skillDescription;
    }

    public void setSkillDescription(String skillDescription) {
        this.skillDescription = skillDescription;
    }

    public SkillType getSkillType() {
        return skillType;
    }

    public void setSkillType(SkillType skillType) {
        this.skillType = skillType;
    }

    public ProficiencyLevel getProficiencyLevel() {
        return proficiencyLevel;
    }

    public void setProficiencyLevel(ProficiencyLevel proficiencyLevel) {
        this.proficiencyLevel = proficiencyLevel;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
