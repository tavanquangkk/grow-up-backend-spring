package jp.trial.grow_up.dto.auth;

import lombok.Data;

@Data
public class RefreshResponseDTO {
    private  String newAccessToken;
}
