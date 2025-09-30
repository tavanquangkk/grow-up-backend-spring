package jp.trial.grow_up.controller.admin;

import com.cloudinary.Api;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jp.trial.grow_up.config.JwtUtil;
import jp.trial.grow_up.domain.Skill;
import jp.trial.grow_up.domain.User;
import jp.trial.grow_up.domain.Workshop;
import jp.trial.grow_up.dto.admin.RequestCreateUserDTO;
import jp.trial.grow_up.dto.auth.JwtDTO;
import jp.trial.grow_up.dto.auth.LoginRequestDTO;
import jp.trial.grow_up.dto.auth.SignupResponseDTO;
import jp.trial.grow_up.dto.client.RequestUpdateInfor;
import jp.trial.grow_up.dto.client.ResponseUserProfileDTO;
import jp.trial.grow_up.dto.workshop.WorkshopDTO;
import jp.trial.grow_up.service.admin.AdminService;
import jp.trial.grow_up.service.client.SkillService;
import jp.trial.grow_up.service.client.UserService;
import jp.trial.grow_up.service.client.WorkshopService;
import jp.trial.grow_up.util.ApiResponse;
import jp.trial.grow_up.util.UserRole;
import jp.trial.grow_up.util.convert.UserConvert;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {
    @Value("${jwt.expiration}")
    private Long expiration;

    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final SkillService skillService;
    private final UserService userService;
    private final AdminService adminService;
    private final WorkshopService workshopService;

    public AdminController(PasswordEncoder passwordEncoder, JwtUtil jwtUtil, SkillService skillService, UserService userService,AdminService adminService, WorkshopService workshopService) {
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.skillService = skillService;
        this.userService = userService;
        this.workshopService = workshopService;
        this.adminService = adminService;
    }

    // ログインロジック
    @PostMapping("/login")
    public  ResponseEntity<ApiResponse<SignupResponseDTO>> login(@RequestBody LoginRequestDTO loginRequestDTO, HttpServletResponse response){
        ApiResponse<SignupResponseDTO> res = new ApiResponse<>();
        User existingUser = this.userService.getUserByEmail(loginRequestDTO.getEmail());

        if(existingUser == null || !passwordEncoder.matches(loginRequestDTO.getPassword(),existingUser.getPassword()) ){
            res.setStatus("error");
            res.setMessage("メールアドレスまたはパスワードが間違っています");
            res.setData(null);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(res);
        }
        if(!existingUser.getRole().name().equals("ADMIN")){
            res.setStatus("error");
            res.setMessage("権限がありませんよ");
            res.setData(null);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(res);
        }
        //認証済　→ jwt発行
        String token = jwtUtil.generateToken(existingUser);
        //refresh token
        String refreshToken = jwtUtil.generateRefreshToken(existingUser);

        // set refresh_token to cookie
        Cookie refreshTokenCookie = new Cookie("refresh_token",refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true); // 本番環境ではtrueに
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(7 * 24 * 60 * 60); // 7日間
        response.addCookie(refreshTokenCookie);

        SignupResponseDTO userData =  UserConvert.convertToSignupUserDTO(existingUser);
        userData.setToken(token);
        userData.setRole(existingUser.getRole().name());

        res.setStatus("success");
        res.setMessage("ログイン成功");
        res.setData(userData);

        return ResponseEntity.ok(res);
    }


    //refresh access_token

    @GetMapping("/refresh")
    public ResponseEntity<?> refreshToken(@CookieValue("refresh_token") String refreshToken) {
        ApiResponse res = new ApiResponse<>();
        try {
            String email = jwtUtil.extractUsername(refreshToken);
            User currentUser = userService.getUserByEmail(email);
            if (currentUser == null) {
                res.setStatus("error");
                res.setMessage("リフレッシュトークンが正しくありません");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(res);
            }
            if (!currentUser.getRole().toString().equals("ADMIN")) {
                res.setStatus("error");
                res.setMessage("権限がありません");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(res);
            }
            String newAccessToken = jwtUtil.generateToken(currentUser);
            JwtDTO resData = new JwtDTO();
            resData.setToken(newAccessToken);
            resData.setUserRole(currentUser.getRole().toString());
            resData.setExpiresIn(expiration / 1000);

            res.setStatus("success");
            res.setMessage("新アクセストークンを発行しました");
            res.setData(resData);
            return ResponseEntity.ok(res);

        } catch (ExpiredJwtException e) {
            res.setStatus("error");
            res.setMessage("リフレッシュトークンの有効期限が切れています。再ログインしてください。");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(res);
        } catch (MalformedJwtException | SignatureException e) {
            res.setStatus("error");
            res.setMessage("トークンが不正です。再ログインしてください。");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(res);
        } catch (Exception e) {
            res.setStatus("error");
            res.setMessage("サーバーエラー");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
        }
    }

    //get sum users
    @GetMapping("/sum/users")
    public long getSumOfUsers(){
        return userService.getSumOfUsers();
    }

    //get sum workshops
    @GetMapping("/sum/workshops")
    public long getSumOfWorks(){
        return workshopService.getSumOfWorks();
    }

    //get sum skills
    @GetMapping("/sum/skills")
    public long getSumOfSkills(){
        return skillService.getSumOfSkills();
    }
    //create user
    @PostMapping("/users")
    public ResponseEntity<ApiResponse<ResponseUserProfileDTO>> createUser(@RequestBody RequestCreateUserDTO requestCreateUserDTO){
            ApiResponse res = new ApiResponse();
            boolean existedUser = userService.userExists(requestCreateUserDTO.getEmail());
            if(existedUser){
                res.setStatus("error");
                res.setMessage("このメールはすでに使われています");
                return ResponseEntity.badRequest().body(res);
            }
            User newUser = new User();
            newUser.setName(requestCreateUserDTO.getName());
            newUser.setEmail(requestCreateUserDTO.getEmail());
            newUser.setDepartment(requestCreateUserDTO.getDepartment());
            newUser.setPosition(requestCreateUserDTO.getPosition());
            newUser.setIntroduction(requestCreateUserDTO.getIntroduction());
            newUser.setProfileImageUrl(requestCreateUserDTO.getProfileImageUrl());
            newUser.setBackgroundImageUrl(requestCreateUserDTO.getBackgroundImageUrl());
            newUser.setRole(UserRole.valueOf(requestCreateUserDTO.getRole()));
            User savedUser = this.userService.handleSaveUser(newUser);
            if(savedUser == null){
                res.setStatus("error");
                res.setMessage("登録できませんでした。再度試してください");
                return ResponseEntity.badRequest().body(res);
            }
            ResponseUserProfileDTO resData = UserConvert.convertToResponseUserProfileDTO(savedUser);
            res.setStatus("success");
            res.setMessage("新規登録できました");
            res.setData(resData);
            return ResponseEntity.ok(res);

    }

    //全てのユーザー一覧を取得
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<ResponseUserProfileDTO>>> getAllUsers(){
        ApiResponse res = new ApiResponse<>();
        List<User> allUsers = this.adminService.getAllUsers();
        if(allUsers.isEmpty()){
            res.setStatus("error");
            res.setMessage("ユーザー一覧の取得に失敗しました");
            return ResponseEntity.badRequest().body(res);
        }
        List<ResponseUserProfileDTO> resData = new ArrayList<>();
        for(User user:allUsers){
            resData.add(UserConvert.convertToResponseUserProfileDTO(user));
        }
        res.setStatus("success");
        res.setMessage("ユーザー一覧を取得できました");
        res.setData(resData);
        return ResponseEntity.ok(res);
    }

    //get a user　by id
    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<ResponseUserProfileDTO>> getUserById(Authentication authentication, @PathVariable("id") UUID userId) {
        ApiResponse<ResponseUserProfileDTO> res = new ApiResponse<>();
        Optional<User> currentUser = this.userService.getUserById(userId);
        if(currentUser.isEmpty()){
            res.setStatus("error");
            res.setMessage("ユーザーが見つかりませんでした");
            res.setData(null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(res);
        }

        ResponseUserProfileDTO resData = UserConvert.convertToResponseUserProfileDTO(currentUser.get());
        res.setStatus("success");
        res.setMessage("ユーザープロフィールの取得に成功しました");
        res.setData(resData);

        return ResponseEntity.ok(res);

    }

    //update user
    @PutMapping("/users/{id}")
    //自分の情報を更新する　Put /api/v1/admin/users/
    public ResponseEntity<ApiResponse<ResponseUserProfileDTO>> updateMyInfor(@PathVariable("id") UUID id,@RequestBody RequestUpdateInfor requestUpdateInfor){
        ApiResponse res = new ApiResponse<>();
        User currentUser = this.userService.getUserById(id).orElseThrow(()-> new RuntimeException("このユーザーが見つかりました"));
        if(currentUser == null){
            res.setStatus("error");
            res.setMessage("ユーザーの情報が見つかりませんでした");
            return ResponseEntity.badRequest().body(res);
        }
        User updatedUser = this.userService.updateUserInfo(currentUser.getEmail(), requestUpdateInfor);
        if(updatedUser == null){
            res.setStatus("error");
            res.setMessage("更新できませんでした");
            return ResponseEntity.badRequest().body(res);
        }
        ResponseUserProfileDTO resData = UserConvert.convertToResponseUserProfileDTO(updatedUser);
        res.setData(resData);
        res.setStatus("success");
        res.setMessage("更新に成功しました");
        return ResponseEntity.ok(res);

    }

    //delete user
    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<?>> deleteUser(@PathVariable("id") UUID id){
        ApiResponse<ResponseUserProfileDTO> res = new ApiResponse<>();
        User currentUser = this.userService.getUserById(id).orElseThrow(()-> new RuntimeException("ユーザーが見つかりませんでした"));
        if(currentUser == null){
            res.setStatus("error");
            res.setMessage("ユーザーが見つかりませんでした");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(res);
        }
        boolean isDeleted = this.userService.deleteUser(currentUser);
        if(!isDeleted){
            res.setStatus("error");
            res.setMessage("ユーザー削除に失敗しました");
            return ResponseEntity.status(HttpStatus.FAILED_DEPENDENCY).body(res);
        }
        res.setStatus("success");
        res.setMessage("ユーザープロフィールの更新に成功しました");
        return ResponseEntity.ok(res);
    }

    //get all workshops
    @GetMapping("/workshops")
    public ResponseEntity<ApiResponse<List<WorkshopDTO>>> getAllWorkshops() {
        ApiResponse res = new ApiResponse<>();
        List<WorkshopDTO> resData = this.workshopService.handleGetAllWorkshop();
        if(resData.size() < 1){
            res.setStatus("error");
            res.setMessage("勉強会一覧の取得に失敗しました");
            return ResponseEntity.badRequest().body(res);
        }
        res.setStatus("success");
        res.setMessage("勉強会一覧の取得ができました");
        res.setData(resData);
        return ResponseEntity.ok(res);
    }

    //create a workshop
    //勉強会作成　Post /api/v1/admin/workshops
    @PostMapping("/workshops")
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

    //update a workshop
    //勉強会編集　Put /api/v1/admin/workshops/{id}
    @PutMapping("/workshops/{id}")
    public ResponseEntity<ApiResponse<WorkshopDTO>> updateWorkshop(@PathVariable("id") UUID id, @RequestBody Workshop workshop, Authentication authentication){
        ApiResponse res = new ApiResponse<>();
        String email = authentication.getName();
        User currentUser = this.userService.getUserByEmail(email);
        boolean isAdmin = currentUser.getRole().toString().equals("ADMIN");
        if(!isAdmin){
            res.setStatus("error");
            res.setMessage("権限ありません");
            return ResponseEntity.badRequest().body(res);
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
    @DeleteMapping("/workshops/{id}")
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

    //create skill
    @PostMapping("/skills")
    public ResponseEntity<ApiResponse<Skill>> addMySkill(@RequestBody Skill skill){
        ApiResponse res = new ApiResponse<>();
        String skillName = skill.getName().toUpperCase().trim();
        Skill createdSkill = this.skillService.createSkill(skillName);//すでに存在している場合、nullが返ってくる
        if(createdSkill == null){
            res.setStatus("error");
            res.setMessage("このスキルの登録ができません");
            return ResponseEntity.badRequest().body(res);
        }
        res.setStatus("success");
        res.setMessage("スキルを登録できました");
        res.setData(createdSkill);
        return ResponseEntity.ok(res);
    }

    //get all skills
    @GetMapping("/skills")
    public ResponseEntity<ApiResponse<List<Skill>>> getAllSkill(){
        ApiResponse res = new ApiResponse<>();
        List<Skill> allSkills = this.skillService.getAllSkills();
        if(allSkills.isEmpty()){
            res.setStatus("error");
            res.setMessage("スキル一覧の取得に失敗しました");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(res);
        }
        res.setStatus("success");
        res.setMessage("スキル一覧の取得に成功しました");
        res.setData(allSkills);
        return ResponseEntity.status(HttpStatus.OK).body(res);
    }

    //get a skill
    @GetMapping("/skills/{id}")
    public ResponseEntity<ApiResponse<Skill>> getSkillById(@PathVariable("id") int id){
        ApiResponse res = new ApiResponse<>();
        Skill currentSkill = this.skillService.getSkill(id);
        if(currentSkill == null){
            res.setStatus("error");
            res.setMessage("このスキルの取得に失敗しました");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(res);
        }
        res.setStatus("success");
        res.setMessage("このスキルの取得に成功しました");
        res.setData(currentSkill);
        return ResponseEntity.status(HttpStatus.OK).body(res);
    }

    //update a skill
    @PutMapping("/skills/{id}")
    public ResponseEntity<ApiResponse<Skill>> updateSkill(@PathVariable("id") int id,@RequestBody Skill updateSkill){
        ApiResponse res = new ApiResponse<>();
        Skill currentSkill = this.skillService.getSkill(id);

        if(currentSkill == null){
            res.setStatus("error");
            res.setMessage("このスキルの取得に失敗しました");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(res);
        }
        Skill resData = this.skillService.updateSkill(id, updateSkill.getName().toUpperCase().trim());
        res.setStatus("success");
        res.setMessage("このスキルの取得に成功しました");
        res.setData(resData);

        return ResponseEntity.status(HttpStatus.OK).body(res);
    }

    //delete a skill
    @DeleteMapping("/skills/{id}")
    public ResponseEntity<ApiResponse<Skill>> deleteSkill(@PathVariable("id") int id){
        ApiResponse res = new ApiResponse<>();
        Skill currentSkill = this.skillService.getSkill(id);

        if(currentSkill == null){
            res.setStatus("error");
            res.setMessage("このスキルの取得に失敗しました");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(res);
        }
        this.skillService.deleteSkill(id);
        res.setStatus("success");
        res.setMessage("このスキルの削除に成功しました");
        return ResponseEntity.status(HttpStatus.OK).body(res);
    }

    @GetMapping("/test")
    public String test (){
    return  "test success";

    }




}
