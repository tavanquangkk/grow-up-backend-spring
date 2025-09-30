package jp.trial.grow_up.service.admin;

import jp.trial.grow_up.domain.Skill;
import jp.trial.grow_up.domain.User;
import jp.trial.grow_up.domain.Workshop;
import jp.trial.grow_up.repository.client.SkillRepository;
import jp.trial.grow_up.repository.client.UserRepository;
import jp.trial.grow_up.repository.client.WorkshopRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminService {
    private final SkillRepository skillRepository;
    private final WorkshopRepository workshopRepository;
    private final UserRepository userRepository;

    public AdminService(SkillRepository skillRepository,
                        WorkshopRepository workshopRepository,
                        UserRepository userRepository)
    {
        this.skillRepository = skillRepository;
        this.workshopRepository = workshopRepository;
        this.userRepository = userRepository;
    }


    //skills logic
    public List<Skill> getAllSkills(){
        return this.skillRepository.findAll();
    }

    //users logic
    public List<User> getAllUsers(){
        return this.userRepository.findAll();
    }


    //workshops logic
    public List<Workshop> getAllWorkshops(){
        return this.workshopRepository.findAll();
    }

}

