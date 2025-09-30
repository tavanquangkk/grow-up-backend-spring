package jp.trial.grow_up.dto.client;

import lombok.Data;

import java.util.UUID;

@Data
public class ResponseWorkshopHostDTO {
    private UUID id;
    private String name;
    private  String email;

}
