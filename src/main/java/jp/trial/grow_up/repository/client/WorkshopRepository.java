package jp.trial.grow_up.repository.client;

import jakarta.transaction.Transactional;
import jp.trial.grow_up.domain.Workshop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface WorkshopRepository extends JpaRepository<Workshop, UUID> {
    boolean existsById(UUID id);
    boolean existsByName(String name);

    List<Workshop> findTop10ByDateAfterOrderByDateAsc(Instant now);

    Workshop findByName(String name);

    //自分が作成した勉強会の一覧取得
    List<Workshop> findByHostId(UUID hostId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Workshop w WHERE w.host.id = :id")
    void deleteAllByHostId(@Param("id") UUID id);
}
