package jp.trial.grow_up.dto.auth;

import lombok.Data;

@Data
public class JwtDTO {
    String token;
    long expiresIn;
    String userRole;
}
