package jp.trial.grow_up.controller.client;

import jp.trial.grow_up.domain.User;
import jp.trial.grow_up.domain.Workshop;
import jp.trial.grow_up.dto.workshop.WorkshopDTO;
import jp.trial.grow_up.service.client.UserService;
import jp.trial.grow_up.service.client.WorkshopService;
import jp.trial.grow_up.util.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/workshops")
public class WorkshopController {

    private final WorkshopService workshopService;
    private final UserService userService;

    public WorkshopController(WorkshopService workshopService,UserService userService) {
        this.workshopService = workshopService;
        this.userService = userService;
    }
    //最新勉強会　5件　GET /api/v1/workshops/recent?limit=5


    //全ての勉強会　GET /api/v1/workshops
    @GetMapping
    public ResponseEntity<ApiResponse<List<WorkshopDTO>>> getAllWorkshop(){
        ApiResponse rs = new ApiResponse<>();
        List<WorkshopDTO> workshopList =  workshopService.handleGetAllWorkshop();

        if(workshopList.size()>0){
            rs.setStatus("success");
            rs.setMessage("勉強会の情報を取得できました");
            rs.setData(workshopList);
            return ResponseEntity.ok(rs);
        }
        rs.setStatus("error");
        rs.setMessage("勉強会のデータがありません");
        return ResponseEntity.badRequest().body(rs);
    }

    //該当の勉強会　GET /api/v1/workshops/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WorkshopDTO>> handleGetWorkshopById(@PathVariable("id") UUID id){
        ApiResponse res = new ApiResponse<>();
        WorkshopDTO workshop = workshopService.getWorkshopById(id);
        if(workshop == null){
            res.setStatus("error");
            res.setMessage("勉強会を取得できませんでした");
            return ResponseEntity.badRequest().body(res);
        }
        res.setStatus("success");
        res.setMessage("勉強会を取得できました");
        res.setData(workshop);

        return ResponseEntity.ok().body(res);

    }


    //勉強会作成　Post /api/v1/workshops
    @PostMapping
    public ResponseEntity<ApiResponse<WorkshopDTO>> createWorkshop(@RequestBody  Workshop workshop, Authentication authentication){
        ApiResponse rs = new ApiResponse<>();
        String email = authentication.getName();
        WorkshopDTO resData = workshopService.handleCreateWorkshop(workshop,email);

        if(resData != null){
            rs.setStatus("success");
            rs.setMessage("勉強会が作成されました");
            rs.setData(resData);
            return ResponseEntity.ok(rs);
        }
        rs.setStatus("error");
        rs.setMessage("勉強会が作成できませんでした。別名で再度作成してください");
        return ResponseEntity.badRequest().body(rs);

    }

    //勉強会編集　Put /api/v1/workshops/{id}
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<WorkshopDTO>> updateWorkshop(@PathVariable("id") UUID id,@RequestBody Workshop workshop,Authentication authentication){
        ApiResponse res = new ApiResponse<>();
        String email = authentication.getName();
        User currentUser = this.userService.getUserByEmail(email);
        User updateUser = this.userService.getUserById(this.workshopService.getWorkshopById(id).getHost().getId()).orElseThrow(()-> new RuntimeException("この勉強会の編集権限がありません。管理者へ連絡してください"));
        if(!currentUser.equals(updateUser)){
            res.setStatus("error");
            res.setMessage("編集権限がありません。管理者へ連絡してください");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(res);
        }
        Workshop updatedWorkshop = this.workshopService.updateWorkshop(id,workshop);
        if(updatedWorkshop == null ){
            res.setStatus("error");
            res.setMessage("編集できませんでした");
            return ResponseEntity.badRequest().body(res);
        }
        WorkshopDTO resData = workshopService.convertToWorkshopDTO(updatedWorkshop);
        res.setStatus("success");
        res.setMessage("勉強会の編集ができました");
        res.setData(resData);
        return ResponseEntity.ok(res);
    }

    //勉強会削除　Delete /api/v1/workshops/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteWorkshop(@PathVariable("id") UUID id){
        ApiResponse res = new ApiResponse<>();
        boolean isDeleted = workshopService.deleteWorkshop(id);
        if(isDeleted)
        {
            res.setStatus("success");
            res.setMessage("勉強会を削除しました");
            return ResponseEntity.ok(res);
        }
        res.setStatus("error");
        res.setMessage("勉強会を削除できませんでした");
        return ResponseEntity.badRequest().body(res);

    }

    //近い開催予定勉強会　取得　Get /api/v1/workshops/upcoming
    @GetMapping("/upcoming")
    public ResponseEntity<ApiResponse<List<WorkshopDTO>>> getUpcomingWorkshops(){
        ApiResponse<List<WorkshopDTO>> rs = new ApiResponse<>();
        List<WorkshopDTO> resData = this.workshopService.getUpComingWorkshops();
        if(resData.size() > 0){
            rs.setStatus("success");
            rs.setMessage("勉強会の取得できました");
            rs.setData(resData);
            return ResponseEntity.ok(rs);
        }
        rs.setStatus("error");
        rs.setMessage("勉強会の取得に失敗しました");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(rs);

    }

    //自分の作成した勉強会一覧　取得　Get /api/v1/workshops/me
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<WorkshopDTO>>> getMyWorkshops(Authentication authentication){
        ApiResponse<List<WorkshopDTO>> rs = new ApiResponse<>();
        String email = authentication.getName();
        List<WorkshopDTO> resData = this.workshopService.getAllMyWorkshops(email);
        if(resData.size() > 0){
            rs.setStatus("success");
            rs.setMessage("勉強会の取得できました");
            rs.setData(resData);
            return ResponseEntity.ok(rs);
        }
        rs.setStatus("error");
        rs.setMessage("勉強会の取得に失敗しました");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(rs);

    }
}
