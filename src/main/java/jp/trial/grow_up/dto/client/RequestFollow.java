package jp.trial.grow_up.dto.client;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
public class RequestFollow {
    private UUID id;
    private String name;
}
