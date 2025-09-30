package jp.trial.grow_up.dto.client;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseUserDTO {

    private UUID id;

    private String name;

    private String email;

    private String role;

    private String department;

    private String position;

    private String introduction;

    private String profileImageUrl;

}