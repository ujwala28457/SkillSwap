package com.myapp.skillswap.dto;

import java.util.List;

public class UserProfileResponse {

    private Long userId;
    private String name;
    private List<UserSkillResponse> skillsOffered;
    private List<UserSkillResponse> skillsLearning;

    public UserProfileResponse() {
    }

    public UserProfileResponse(Long userId, String name, List<UserSkillResponse> skillsOffered, List<UserSkillResponse> skillsLearning) {
        this.userId = userId;
        this.name = name;
        this.skillsOffered = skillsOffered;
        this.skillsLearning = skillsLearning;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<UserSkillResponse> getSkillsOffered() {
        return skillsOffered;
    }

    public void setSkillsOffered(List<UserSkillResponse> skillsOffered) {
        this.skillsOffered = skillsOffered;
    }

    public List<UserSkillResponse> getSkillsLearning() {
        return skillsLearning;
    }

    public void setSkillsLearning(List<UserSkillResponse> skillsLearning) {
        this.skillsLearning = skillsLearning;
    }
}
