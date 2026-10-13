package com.myapp.skillswap.dto;

import com.myapp.skillswap.entity.ProficiencyLevel;

public class SkillSummaryDto {

    private Long skillId;
    private String skillName;
    private ProficiencyLevel proficiencyLevel;

    public SkillSummaryDto() {
    }

    public SkillSummaryDto(Long skillId, String skillName, ProficiencyLevel proficiencyLevel) {
        this.skillId = skillId;
        this.skillName = skillName;
        this.proficiencyLevel = proficiencyLevel;
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
}
