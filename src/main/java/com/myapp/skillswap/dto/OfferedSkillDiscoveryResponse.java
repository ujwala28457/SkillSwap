package com.myapp.skillswap.dto;

import com.myapp.skillswap.entity.ProficiencyLevel;

import java.time.LocalDateTime;

public class OfferedSkillDiscoveryResponse {

    private Long userSkillId;
    private Long userId;
    private String userName;
    private Long skillId;
    private String skillName;
    private String skillDescription;
    private ProficiencyLevel proficiencyLevel;
    private String description;
    private LocalDateTime createdAt;

    public OfferedSkillDiscoveryResponse() {
    }

    public OfferedSkillDiscoveryResponse(Long userSkillId, Long userId, String userName,
                                         Long skillId, String skillName, String skillDescription,
                                         ProficiencyLevel proficiencyLevel, String description,
                                         LocalDateTime createdAt) {
        this.userSkillId = userSkillId;
        this.userId = userId;
        this.userName = userName;
        this.skillId = skillId;
        this.skillName = skillName;
        this.skillDescription = skillDescription;
        this.proficiencyLevel = proficiencyLevel;
        this.description = description;
        this.createdAt = createdAt;
    }

    public Long getUserSkillId() {
        return userSkillId;
    }

    public void setUserSkillId(Long userSkillId) {
        this.userSkillId = userSkillId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
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
