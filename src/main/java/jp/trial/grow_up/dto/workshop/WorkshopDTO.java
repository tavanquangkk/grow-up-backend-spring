package jp.trial.grow_up.dto.workshop;

import jp.trial.grow_up.dto.client.ResponseWorkshopHostDTO;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class WorkshopDTO {
    private UUID id;
    private String name;
    private String description;
    private Instant date;
    private ResponseWorkshopHostDTO host;
}
