package jp.trial.grow_up.dto.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseFollowDTO {
    private UUID id;
    private UUID followerId;
    private String followerName;
    private UUID followingId;
    private String followingName;
}