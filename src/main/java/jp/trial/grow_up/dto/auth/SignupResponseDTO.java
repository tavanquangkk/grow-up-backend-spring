package jp.trial.grow_up.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SignupResponseDTO {

    private UUID id;
    private String name;
    private String email;
    private String token;
    private String refreshToken;
    private String role;

}
