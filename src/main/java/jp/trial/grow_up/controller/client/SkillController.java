package jp.trial.grow_up.controller.client;

import jp.trial.grow_up.domain.Skill;
import jp.trial.grow_up.service.client.SkillService;
import jp.trial.grow_up.util.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/skills")
public class SkillController {
    private final SkillService skillService;

    public SkillController(SkillService skillService) {
        this.skillService = skillService;
    }

    //スキル一覧を取得し、表示に使用する
    @GetMapping
    public ResponseEntity<ApiResponse<List<Skill>>> getAllSkill(){
        ApiResponse res = new ApiResponse<>();
        List<Skill> skills = this.skillService.getAllSkills();
        if(skills.size() > 0){
            res.setStatus("success");
            res.setMessage("スキル一覧を取得できました");
            res.setData(skills);
            return ResponseEntity.ok(res);
        }
        res.setStatus("error");
        res.setMessage("スキル一覧を取得できませんでした");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(res);
    }


}
