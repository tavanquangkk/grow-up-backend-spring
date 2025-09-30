package jp.trial.grow_up.config;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jp.trial.grow_up.util.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
public class JwtExceptionHandler {

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ApiResponse> handleExpiredJwt(ExpiredJwtException ex) {
        ApiResponse res = new ApiResponse();
        res.setStatus("error");
        res.setMessage("トークンの有効期限が切れています。再ログインしてください。");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(res);
    }

    @ExceptionHandler({MalformedJwtException.class, SignatureException.class})
    public ResponseEntity<ApiResponse> handleInvalidJwt(Exception ex) {
        ApiResponse res = new ApiResponse();
        res.setStatus("error");
        res.setMessage("不正なトークンです。再ログインしてください。");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(res);
    }
}
