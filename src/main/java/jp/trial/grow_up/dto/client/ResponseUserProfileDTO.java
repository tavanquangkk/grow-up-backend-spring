package jp.trial.grow_up.dto.client;

import jp.trial.grow_up.domain.Skill;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

//password が含まれないレスポンス
@Getter
@Setter
public class ResponseUserProfileDTO {
    private UUID id;
    private String name;
    private String role;
    private String email;
    private String department;
    private String position;
    private String introduction;
    private String profileImageUrl;
    private String backgroundImageUrl;
    private long followerCount;
    private long followingCount;
    private List<Skill> teachableSkills;
    private List<Skill> learningSkills;
}
