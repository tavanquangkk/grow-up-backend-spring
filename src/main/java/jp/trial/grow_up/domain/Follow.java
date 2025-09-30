package jp.trial.grow_up.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "follows",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"follower_id", "following_id"})
        }
)
public class Follow {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne()
    @JoinColumn(name = "follower_id",nullable = false)
    private User follower;
    @ManyToOne()
    @JoinColumn(name = "following_id",nullable = false)
    private User following;

    private Instant createdAt = Instant.now();
}
