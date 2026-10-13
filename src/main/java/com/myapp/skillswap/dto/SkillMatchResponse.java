package com.myapp.skillswap.dto;

import java.util.List;

public class SkillMatchResponse {

    private UserResponse matchedUser;
    private List<SkillSummaryDto> skillsTheyTeachYou;
    private List<SkillSummaryDto> skillsYouTeachThem;

    public SkillMatchResponse() {
    }

    public SkillMatchResponse(UserResponse matchedUser, List<SkillSummaryDto> skillsTheyTeachYou, List<SkillSummaryDto> skillsYouTeachThem) {
        this.matchedUser = matchedUser;
        this.skillsTheyTeachYou = skillsTheyTeachYou;
        this.skillsYouTeachThem = skillsYouTeachThem;
    }

    public UserResponse getMatchedUser() {
        return matchedUser;
    }

    public void setMatchedUser(UserResponse matchedUser) {
        this.matchedUser = matchedUser;
    }

    public List<SkillSummaryDto> getSkillsTheyTeachYou() {
        return skillsTheyTeachYou;
    }

    public void setSkillsTheyTeachYou(List<SkillSummaryDto> skillsTheyTeachYou) {
        this.skillsTheyTeachYou = skillsTheyTeachYou;
    }

    public List<SkillSummaryDto> getSkillsYouTeachThem() {
        return skillsYouTeachThem;
    }

    public void setSkillsYouTeachThem(List<SkillSummaryDto> skillsYouTeachThem) {
        this.skillsYouTeachThem = skillsYouTeachThem;
    }
}
