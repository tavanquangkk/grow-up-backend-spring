package jp.trial.grow_up.repository.client;

import jp.trial.grow_up.domain.Skill;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SkillRepository extends JpaRepository<Skill,Integer> {

    boolean existsByName(String name);

    Skill findByName(String skillName);
}
