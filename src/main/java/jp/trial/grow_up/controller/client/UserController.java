package jp.trial.grow_up.controller.client;

import com.cloudinary.Api;
import jp.trial.grow_up.domain.Follow;
import jp.trial.grow_up.domain.Skill;
import jp.trial.grow_up.domain.User;
import jp.trial.grow_up.dto.client.RequestFollow;
import jp.trial.grow_up.dto.client.RequestUpdateInfor;
import jp.trial.grow_up.dto.client.ResponseFollowDTO;
import jp.trial.grow_up.dto.client.ResponseUserProfileDTO;
import jp.trial.grow_up.service.client.FollowService;
import jp.trial.grow_up.service.client.SkillService;
import jp.trial.grow_up.service.client.UserService;
import jp.trial.grow_up.service.uploadFile.CloudinaryService;
import jp.trial.grow_up.util.ApiResponse;
import jp.trial.grow_up.util.convert.UserConvert;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
class UserController {

    private final UserService userService;
    private final CloudinaryService cloudinaryService;
    private final FollowService followSevice;
    private final SkillService skillService;
    public UserController(UserService userService,CloudinaryService cloudinaryService,FollowService followSevice,SkillService skillService) {
        this.userService = userService;
        this.cloudinaryService = cloudinaryService;
        this.followSevice = followSevice;
        this.skillService = skillService;
    }


    //オススメユーザー一覧
    @GetMapping("/recommend")
    public ResponseEntity<ApiResponse<List<ResponseUserProfileDTO>>> handleGetRecommendedUsers(Authentication authentication){
        ApiResponse<List<ResponseUserProfileDTO>> res = new ApiResponse<>();
        String email = authentication.getName();
        User me = userService.getUserByEmail(email);
        List<ResponseUserProfileDTO> userData = this.userService.getRecommendedUsers(me);
        if(userData.size() > 0){
            res.setStatus("success");
            res.setMessage("オススメユーザー一覧を取得しました");
            res.setData(userData);
            return ResponseEntity.ok(res);
        }
        res.setStatus("error");
        res.setMessage("オススメユーザー一覧の取得に失敗しました");
        return ResponseEntity.status(HttpStatus.NOT_FOUND.value()).build();
    }

    //自分の情報を習得(jwtからemailを取得→ 情報取得)
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<ResponseUserProfileDTO>> getMyProfile(Authentication authentication) {
        ApiResponse<ResponseUserProfileDTO> res = new ApiResponse<ResponseUserProfileDTO>();
        String email = authentication.getName();
        //
        User currentUser = this.userService.getUserByEmail(email);
        if(currentUser == null){
            // ユーザーが見つからなかった場合の処理 (念のため)
            res.setStatus("error");
            res.setMessage("あなたの情報が見つかりませんでした");
            res.setData(null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(res);
        }
        ResponseUserProfileDTO resData = UserConvert.convertToResponseUserProfileDTO(currentUser);
        res.setData(resData);
        res.setStatus("success");
        res.setMessage("プロフィールの取得に成功しました");
        return ResponseEntity.ok(res);
    }

    //自分の情報を更新する　Put /api/v1/users/me
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<ResponseUserProfileDTO>> updateMyInfor(@RequestBody RequestUpdateInfor requestUpdateInfor,Authentication authentication){
           ApiResponse res = new ApiResponse<>();
           String email = authentication.getName();

           User currentUser = this.userService.getUserByEmail(email);
           if(currentUser == null){
               res.setStatus("error");
               res.setMessage("ユーザーの情報が見つかりませんでした");
               return ResponseEntity.badRequest().body(res);
           }
           User updatedUser = this.userService.updateUserInfo(email,requestUpdateInfor);
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

    //自分のアバターを更新　Put /api/v1/users/me/avatar
    @PutMapping("/me/avatar")
    public ResponseEntity<ApiResponse<ResponseUserProfileDTO>> updateMyAvatar(Authentication authentication ,@RequestParam("file") MultipartFile file){
        ApiResponse res = new ApiResponse<>();
        String email = authentication.getName();
        User currentUser = this.userService.getUserByEmail(email);
        if(currentUser == null){
            res.setStatus("error");
            res.setMessage("あなたの情報が見つかりませんでした");
            return ResponseEntity.badRequest().body(res);
        }
        try{
            String currentImageUrl = currentUser.getProfileImageUrl();
            String newImageURl = cloudinaryService.uploadFile(file,"profileImages");
            currentUser.setProfileImageUrl(newImageURl);
            User updatedUser = this.userService.handleSaveUser(currentUser);
            if(currentImageUrl!= null && !currentImageUrl.isEmpty()){
                String imagePublicId = cloudinaryService.getPublicIdFromUrl(currentImageUrl);
                cloudinaryService.deleteOldImage(imagePublicId);
            }

            ResponseUserProfileDTO resData = UserConvert.convertToResponseUserProfileDTO(updatedUser);
            res.setStatus("success");
            res.setMessage("プロフィール画像の更新が完了しました");

            res.setData(resData);

            return ResponseEntity.ok(res);

        }catch (Exception e){
            res.setStatus("error");
            res.setMessage("アバターの更新に失敗しました");
            return  ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
        }

    }

    //自分のバックグラウンド画像を更新　Post /api/v1/users/me/bgImage
    @PutMapping("/me/bgImage")
    public ResponseEntity<ApiResponse<ResponseUserProfileDTO>> updateMyBackgroundImg(Authentication authentication ,@RequestParam("file") MultipartFile file){
        ApiResponse res = new ApiResponse<>();
        String email = authentication.getName();
        User currentUser = this.userService.getUserByEmail(email);
        if(currentUser == null){
            res.setStatus("error");
            res.setMessage("あなたの情報が見つかりませんでした");
            return ResponseEntity.badRequest().body(res);
        }
        try{
            String imageURl = cloudinaryService.uploadFile(file,"backgroundImages");
            currentUser.setBackgroundImageUrl(imageURl);
            User updatedUser = this.userService.handleSaveUser(currentUser);
            ResponseUserProfileDTO resData = UserConvert.convertToResponseUserProfileDTO(updatedUser);
            res.setStatus("success");
            res.setMessage("バックグラウンド画像の更新が完了しました");
            res.setData(resData);
            return ResponseEntity.ok(res);

        }catch (Exception e){
            res.setStatus("error");
            res.setMessage("バックグラウンド画像の更新に失敗しました");
            return  ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
        }

    }

    //自分の学習したいスキル登録　Post /api/v1/users/me/learnSkills
    @PostMapping("/me/learnSkills")
    public ResponseEntity<ApiResponse<List<Skill>>> addLearnSkill(
            @RequestBody String skillName,
            Authentication authentication
    ) {
        ApiResponse res = new ApiResponse<>();
        String skillNameUpper = skillName.toUpperCase().trim();

        // check auth user
        String email = authentication.getName();
        User user = this.userService.getUserByEmail(email);
        if (user == null) {
            res.setStatus("error");
            res.setMessage("再度ログインする必要があります");
            return ResponseEntity.badRequest().body(res);
        }

        List<Skill> currentLearnSkills = user.getLearningSkills();

        // 既にユーザーが登録済みか確認
        boolean alreadyLearning = currentLearnSkills.stream()
                .anyMatch(s -> s.getName().equalsIgnoreCase(skillNameUpper));
        if (alreadyLearning) {
            res.setStatus("error");
            res.setMessage("このスキルはすでに登録済みです");
            res.setData(currentLearnSkills);
            return ResponseEntity.badRequest().body(res);
        }

        // DBにスキルが存在するか確認
        Skill skill = skillService.getSkillByName(skillNameUpper);
        if (skill == null) {
            // 存在しないので新規作成
            Skill newSkill = new Skill();
            newSkill.setName(skillNameUpper);
            skill = this.skillService.handleSave(newSkill);
        }

        // ユーザーに追加
        currentLearnSkills.add(skill);
        user.setLearningSkills(currentLearnSkills);
        this.userService.handleSaveUser(user);

        res.setStatus("success");
        res.setMessage("スキルを登録できました");
        res.setData(currentLearnSkills);
        return ResponseEntity.ok(res);
    }

    //自分のシェアしたいスキル登録　Post /api/v1/users/me/teachableSkills
    @PostMapping("/me/teachableSkills")
    public ResponseEntity<ApiResponse<List<Skill>>> addTeachSkill(
            @RequestBody String skillName,
            Authentication authentication
    ) {
        ApiResponse res = new ApiResponse<>();
        String skillNameUpper = skillName.toUpperCase().trim();

        // check auth user
        User user = this.userService.getUserByEmail(authentication.getName());
        if (user == null) {
            res.setStatus("error");
            res.setMessage("再度ログインする必要があります");
            return ResponseEntity.badRequest().body(res);
        }

        List<Skill> currentTeachableSkills = user.getTeachableSkills();

        // すでにユーザーが登録済みかチェック
        boolean alreadyRegistered = currentTeachableSkills.stream()
                .anyMatch(s -> s.getName().equalsIgnoreCase(skillNameUpper));
        if (alreadyRegistered) {
            res.setStatus("error");
            res.setMessage("このスキルはすでに登録済みです");
            res.setData(currentTeachableSkills);
            return ResponseEntity.badRequest().body(res);
        }

        // Skill テーブルから取得 or 新規作成
        Skill skill = skillService.getSkillByName(skillNameUpper);
        if (skill == null) {
            Skill newSkill = new Skill();
            newSkill.setName(skillNameUpper);
            skill = skillService.handleSave(newSkill);
        }

        // ユーザーに追加
        currentTeachableSkills.add(skill);
        user.setTeachableSkills(currentTeachableSkills);
        this.userService.handleSaveUser(user);

        res.setStatus("success");
        res.setMessage("スキルを登録できました");
        res.setData(currentTeachableSkills);
        return ResponseEntity.ok(res);
    }

    //自分の学習したいスキル削除　Delete /api/v1/users/me/learnSkills/{id}
    @DeleteMapping("/me/learnSkills/{skillName}")
    public ResponseEntity<ApiResponse<List<Skill>>> deleteLearningSkill(@PathVariable("skillName") String skillName,Authentication authentication){
        ApiResponse res = new ApiResponse<>();
        String skillNameUpper = skillName.toUpperCase().trim();
        //check auth user
        String email = authentication.getName();
        User user = this.userService.getUserByEmail(email);
        if(user == null ){
            res.setStatus("error");
            res.setMessage("再度ログインする必要があります");
            return ResponseEntity.badRequest().body(res);
        }
        List<Skill> currentTeachableSkills = user.getTeachableSkills();
        //現在の登録したスキル一覧
        List<String> skills = this.skillService.getAllSkills().stream().map((skill) -> skill.getName()).toList();
        if(!skills.contains(skillNameUpper)){
            res.setStatus("error");
            res.setMessage("スキル削除に失敗しました");
            return ResponseEntity.badRequest().body(res);
        }
        Skill skill = skillService.getSkillByName(skillNameUpper);
        currentTeachableSkills.remove(skill);
        user.setTeachableSkills(currentTeachableSkills);
        this.userService.handleSaveUser(user);
        res.setStatus("success");
        res.setMessage("スキルを削除できました");
        return ResponseEntity.ok(res);

    }

    //自分のシェアしたいスキル削除　Delete /api/v1/users/me/teachableSkills/{skillName}
    @DeleteMapping("/me/teachableSkills/{skillName}")
    public ResponseEntity<ApiResponse<List<Skill>>> deleteTeachableSkill(@PathVariable("skillName") String skillName,Authentication authentication){
        ApiResponse res = new ApiResponse<>();
        String skillNameUpper = skillName.toUpperCase().trim();
        //check auth user
        String email = authentication.getName();
        User user = this.userService.getUserByEmail(email);
        if(user == null ){
            res.setStatus("error");
            res.setMessage("再度ログインする必要があります");
            return ResponseEntity.badRequest().body(res);
        }
        List<Skill> currentTeachableSkills = user.getTeachableSkills();
        //現在の登録したスキル一覧
        List<String> skills = this.skillService.getAllSkills().stream().map((skill) -> skill.getName()).toList();
        if(!skills.contains(skillNameUpper)){
            res.setStatus("error");
            res.setMessage("スキル削除に失敗しました");
            return ResponseEntity.badRequest().body(res);
        }
        Skill skill = skillService.getSkillByName(skillNameUpper);
        currentTeachableSkills.remove(skill);
        user.setTeachableSkills(currentTeachableSkills);
        this.userService.handleSaveUser(user);
        res.setStatus("success");
        res.setMessage("スキルを削除できました");
        return ResponseEntity.ok(res);

    }


    //フォローする　Post /api/v1/users/me/follow/{id}
    @PostMapping("/me/follow/{id}")
    public ResponseEntity<ApiResponse<Follow>> followOther(Authentication authentication, @PathVariable("id") UUID id){
        ApiResponse res = new ApiResponse<>();
        String email = authentication.getName();
        UUID myID = this.userService.getUserByEmail(email).getId();
        UUID followID = id;
        //フォローしているかをチェックく
        if(this.followSevice.isFollowed(myID,id)){
            res.setStatus("error");
            res.setMessage("すでにフォローしている状態です");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(res);
        }
        if(myID.equals(followID)){
            res.setStatus("error");
            res.setMessage("自分へのフォローができません");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(res);
        }
        Follow followedData = this.followSevice.follow(myID,followID);
        if(followedData == null){
            res.setStatus("error");
            res.setMessage("フォロー機能に問題がありません。再度試してくださいね");
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).body(res);
        }
        res.setStatus("success");
        res.setMessage("フォローしました");
        ResponseFollowDTO resData = new ResponseFollowDTO(followedData.getId(),myID,"",id,"");
        res.setData(resData);
        //

        return ResponseEntity.status(HttpStatus.OK).body(res);


    }

    //フォローを外す　Delete /api/v1/users/me/follow/{id}
    @DeleteMapping("/me/follow/{id}")
    public ResponseEntity<ApiResponse<?>> unFollow(Authentication authentication,@PathVariable("id") UUID id){
        ApiResponse res = new ApiResponse<>();
        UUID myId = this.userService.getUserByEmail(authentication.getName()).getId();
        boolean isUnfollowed = this.followSevice.unFollow(myId,id);
        if(!isUnfollowed){
            res.setStatus("error");
            res.setMessage("フォローを外せない状態です。管理者へ連絡してください");

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }
        res.setStatus("success");
        res.setMessage("フォローを外しました");

        return ResponseEntity.ok(res);
    }
    //フォロワー一覧取得　Get /api/users/{userId}/followers
    @GetMapping("/me/followers")
    public ResponseEntity<ApiResponse<List<ResponseUserProfileDTO>>> getFollowerList(Authentication authentication){
        ApiResponse res = new ApiResponse<>();
        String email = authentication.getName();
        UUID myID = this.userService.getUserByEmail(email).getId();
        List<User> followers = this.followSevice.getFollowers(myID);
        if(followers.isEmpty()){
            res.setStatus("error");
            res.setMessage("フォロワー一覧の取得に失敗しました");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(res);
        }
        List<ResponseUserProfileDTO> resData = new ArrayList<>();
        for(User user: followers){
            resData.add(UserConvert.convertToResponseUserProfileDTO(user));
        }
        if(resData.isEmpty()){
            res.setStatus("error");
            res.setMessage("フォロワー一覧の取得に失敗しました");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(res);
        }
        res.setStatus("success");
        res.setMessage("フォロワー一覧の取得に成功しました");
        res.setData(resData);
        return ResponseEntity.ok().body(res);
    }

    //フォロー中一覧取得　Get /api/users/{userId}/following
    @GetMapping("/me/followings")
    public ResponseEntity<ApiResponse<List<ResponseUserProfileDTO>>> getFollowingList(Authentication authentication){
        ApiResponse res = new ApiResponse<>();
        String email = authentication.getName();
        UUID myID = this.userService.getUserByEmail(email).getId();
        List<User> followings = this.followSevice.getFollowings(myID);
        if(followings.size()  < 1){
            res.setStatus("error");
            res.setMessage("フォロー中一覧の取得に失敗しました");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(res);
        }
        List<ResponseUserProfileDTO> resData = new ArrayList<>();
        for(User user: followings){
            resData.add(UserConvert.convertToResponseUserProfileDTO(user));
        }
        if(resData.size() < 1){
            res.setStatus("error");
            res.setMessage("フォロー中一覧の取得に失敗しました");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(res);
        }
        res.setStatus("success");
        res.setMessage("フォロー中一覧の取得に成功しました");
        res.setData(resData);
        return ResponseEntity.ok().body(res);
    }


    //他のユーザーの公開プロフィール情報を取得する
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<ResponseUserProfileDTO>> getOtherUserProfile(Authentication authentication,@PathVariable UUID userId) {
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
        res.setMessage("プロフィールの取得に成功しました");
        res.setData(resData);

        return ResponseEntity.ok(res);

    }

    //フォロワー一覧取得　Get /api/v1/followers/{id}
    @GetMapping("/followers/{id}")
    public ResponseEntity<ApiResponse<List<ResponseUserProfileDTO>>> getFollowerOfOther(@PathVariable("id") UUID id){
        ApiResponse res = new ApiResponse<>();
        List<User> followers = this.followSevice.getFollowers(id);
        if(followers.isEmpty()){
            res.setStatus("error");
            res.setMessage("フォロワー一覧の取得に失敗しました");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(res);
        }
        List<ResponseUserProfileDTO> resData = new ArrayList<>();
        for(User user: followers){
            resData.add(UserConvert.convertToResponseUserProfileDTO(user));
        }
        if(resData.isEmpty()){
            res.setStatus("error");
            res.setMessage("フォロワー一覧の取得に失敗しました");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(res);
        }
        res.setStatus("success");
        res.setMessage("フォロワー一覧の取得に成功しました");
        res.setData(resData);
        return ResponseEntity.ok().body(res);
    }

    //フォロー中一覧取得　Get /api/v1/following/{id}
    @GetMapping("/followings/{id}")
    public ResponseEntity<ApiResponse<List<ResponseUserProfileDTO>>> getFollowingOfOther(@PathVariable("id") UUID id){
        ApiResponse res = new ApiResponse<>();
        List<User> followings = this.followSevice.getFollowings(id);
        if(followings.size()  < 1){
            res.setStatus("error");
            res.setMessage("フォロー中一覧の取得に失敗しました");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(res);
        }
        List<ResponseUserProfileDTO> resData = new ArrayList<>();
        for(User user: followings){
            resData.add(UserConvert.convertToResponseUserProfileDTO(user));
        }
        if(resData.size() < 1){
            res.setStatus("error");
            res.setMessage("フォロー中一覧の取得に失敗しました");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(res);
        }
        res.setStatus("success");
        res.setMessage("フォロー中一覧の取得に成功しました");
        res.setData(resData);
        return ResponseEntity.ok().body(res);
    }





}