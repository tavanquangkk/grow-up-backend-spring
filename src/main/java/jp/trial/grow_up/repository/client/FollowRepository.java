package jp.trial.grow_up.repository.client;

import jp.trial.grow_up.domain.Follow;
import jp.trial.grow_up.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FollowRepository extends JpaRepository<Follow, UUID> {

    @Query("SELECT f.follower FROM Follow f WHERE f.following.id = :userId")
    List<User> findAllFollowers(@Param("userId") UUID userID);

    @Query("SELECT f.following FROM Follow f WHERE f.follower.id = :userId")
    List<User> findAllFollowings(@Param("userId") UUID userID);

    boolean existsByFollowerIdAndFollowingId(UUID followerId, UUID followingId);

    @Query("SELECT COUNT(f) FROM Follow f WHERE f.following.id = :userId")
    long countFollowers(@Param("userId") UUID userID);

    @Query("SELECT COUNT(f) FROM Follow f WHERE f.follower.id = :userId")
    long countFollowings(@Param("userId") UUID userID);

    Follow findByFollowerIdAndFollowingId(UUID followerId, UUID followingId);
}
