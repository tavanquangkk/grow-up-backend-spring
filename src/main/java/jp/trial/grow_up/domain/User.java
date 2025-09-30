package jp.trial.grow_up.domain;

import jakarta.persistence.*;
import jp.trial.grow_up.util.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
public class User {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String name;
    @Enumerated(EnumType.STRING)
    private UserRole role ;
    // デフォルトで USER にしておく
    @PrePersist
    public void prePersist() {
        if (role == null) {
            role = UserRole.USER;
        }
    }
    private String email;
    private String password;
    private String department;
    private String position;
    private String introduction;
    private String profileImageUrl;
    private String backgroundImageUrl;
    @OneToMany(mappedBy = "following", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Follow> followers = new ArrayList<>();

    @OneToMany(mappedBy = "follower", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Follow> following = new ArrayList<>();

    private long followerCount = 0;
    private long followingCount = 0;
    // 教えられるスキル
    @ManyToMany
    @JoinTable(
            name = "user_teachable_skills",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    private List<Skill> teachableSkills = new ArrayList<>();
    // 学びたいスキル
    @ManyToMany
    @JoinTable(
            name = "user_learning_skills",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    private List<Skill> learningSkills = new ArrayList<>();

    // private List<Commission> sentCommissions;
    // private List<Commission> receivedCommissions;
    // private List<QaPost> qaPosts;

}
