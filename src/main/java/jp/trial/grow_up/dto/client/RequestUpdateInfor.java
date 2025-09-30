package jp.trial.grow_up.dto.client;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class RequestUpdateInfor {
    private String name;
    private String department;
    private String position;
    private String introduction;
}
