package jp.trial.grow_up.repository.client;

import jp.trial.grow_up.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User,UUID> {

    Optional<User> findByName(String name);

    Optional<User> findUserByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findTop10ByOrderByFollowerCountDesc();
}
