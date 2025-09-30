package jp.trial.grow_up.dto.admin;

import jp.trial.grow_up.util.UserRole;
import lombok.Data;


@Data
public class RequestCreateUserDTO {
    private String name;
    private String role;
    private String email;
    private String department;
    private String position;
    private String introduction;
    private String profileImageUrl;
    private String backgroundImageUrl;
}
