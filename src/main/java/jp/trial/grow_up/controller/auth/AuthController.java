package jp.trial.grow_up.controller.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jp.trial.grow_up.domain.User;
import jp.trial.grow_up.dto.auth.*;
import jp.trial.grow_up.service.client.UserService;
import jp.trial.grow_up.config.JwtUtil;
import jp.trial.grow_up.util.ApiResponse;
import jp.trial.grow_up.util.convert.UserConvert;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    //DI/DC
   private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Value("${jwt.expiration}")
    private Long expiration;

    public AuthController(UserService userService,PasswordEncoder passwordEncoder,JwtUtil jwtUtil) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    //ユーザー新規登録

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<SignupResponseDTO>> registerUser(@RequestBody SignupRequestDTO signupRequestDTO,HttpServletResponse response) {

        ApiResponse<SignupResponseDTO> res = new ApiResponse<>();

        if (!this.userService.userExists(signupRequestDTO.getEmail())) {
            User convertedUser = UserConvert.convertFromSignupRequestDTO(signupRequestDTO);

            // パスワードのハッシュ化
            convertedUser.setPassword(passwordEncoder.encode(signupRequestDTO.getPassword()));

            User savedUser = this.userService.handleSaveUser(convertedUser);

            String token = jwtUtil.generateToken(savedUser);

            //refresh token 生成　→ cookie に追加する
            String refreshToken = jwtUtil.generateRefreshToken(savedUser);
            Cookie refreshTokenCookie = new Cookie("refresh_token", refreshToken);
            refreshTokenCookie.setHttpOnly(true);
            refreshTokenCookie.setSecure(true);
            refreshTokenCookie.setPath("/");
            refreshTokenCookie.setMaxAge(7 * 24 * 60 * 60); // 7日間
            response.addCookie(refreshTokenCookie);

            SignupResponseDTO rsData = UserConvert.convertToSignupUserDTO(savedUser);
            rsData.setToken(token);
            rsData.setRefreshToken(refreshToken);
            rsData.setRole(savedUser.getRole().name());


            res.setStatus("success");
            res.setMessage("ユーザー登録が完了しました");
            res.setData(rsData);

            return ResponseEntity.status(HttpStatus.CREATED).body(res);
        }

        res.setStatus("error");
        res.setMessage("このメールアドレスは既に登録されています");
        res.setData(null);

        return ResponseEntity.badRequest().body(res);
    }



    // ログインロジック
    @PostMapping("/login")
    public  ResponseEntity<ApiResponse<SignupResponseDTO>> login(@RequestBody LoginRequestDTO loginRequestDTO,HttpServletResponse response){
            ApiResponse<SignupResponseDTO> res = new ApiResponse<>();
            User existingUser = this.userService.getUserByEmail(loginRequestDTO.getEmail());

            if(existingUser == null || !passwordEncoder.matches(loginRequestDTO.getPassword(),existingUser.getPassword())){
                res.setStatus("error");
                res.setMessage("メールアドレスまたはパスワードが間違っています");
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

            //create auth response and set data to it
            SignupResponseDTO userData =  UserConvert.convertToSignupUserDTO(existingUser);
            userData.setToken(token);
            userData.setRefreshToken(refreshToken);
            userData.setRole(existingUser.getRole().name());

            res.setStatus("success");
            res.setMessage("ログイン成功");
            res.setData(userData);

            return ResponseEntity.ok(res);
    }

    //Access tokenを再度取得する（有効期限切れた時モバイル用）

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<RefreshResponseDTO>> refresh(@RequestBody RefreshRequestDTO refreshRequestDTO){
        ApiResponse res = new ApiResponse<>();
        String refreshToken = refreshRequestDTO.getRefreshToken();
        String email = jwtUtil.extractUsername(refreshToken);
        User currentUser = userService.getUserByEmail(email);
        if(currentUser == null){
            res.setStatus("error");
            res.setMessage("リフレッシュトーケンが正しくありません");
            return ResponseEntity.badRequest().body(res);
        }
        String newAccessToken = jwtUtil.generateToken(currentUser);
        RefreshResponseDTO resData = new RefreshResponseDTO();
        resData.setNewAccessToken(newAccessToken);
        res.setStatus("success");
        res.setMessage("Access Tokenが生成された");
        res.setData(resData);

        return ResponseEntity.ok(res);

    }


}
