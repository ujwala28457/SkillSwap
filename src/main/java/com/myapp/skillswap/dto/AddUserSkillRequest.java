package com.myapp.skillswap.dto;

import com.myapp.skillswap.entity.ProficiencyLevel;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class AddUserSkillRequest {

    private Long skillId;

    @Size(max = 50, message = "Skill name cannot exceed 50 characters")
    private String skillName;

    @NotNull(message = "Proficiency level is required")
    private ProficiencyLevel proficiencyLevel;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    public AddUserSkillRequest() {
    }

    public AddUserSkillRequest(Long skillId, ProficiencyLevel proficiencyLevel, String description) {
        this.skillId = skillId;
        this.proficiencyLevel = proficiencyLevel;
        this.description = description;
    }

    public AddUserSkillRequest(String skillName, ProficiencyLevel proficiencyLevel, String description) {
        this.skillName = skillName;
        this.proficiencyLevel = proficiencyLevel;
        this.description = description;
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
}
