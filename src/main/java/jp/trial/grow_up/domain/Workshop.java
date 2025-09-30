package jp.trial.grow_up.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Entity
@Data
@Table(name = "workshops")
public class Workshop {

    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    private String name;
    private String description;
    private Instant date;
    @ManyToOne
    @JoinColumn(name = "host_user_id", nullable = true)
    private User host;
}
