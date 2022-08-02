package com.JJP.restapiserver.controller.member;

import com.JJP.restapiserver.domain.dto.MessageResponse;
import com.JJP.restapiserver.domain.dto.member.request.*;
import com.JJP.restapiserver.domain.dto.member.response.JwtResponse;
import com.JJP.restapiserver.domain.dto.member.response.MemberInfoResponse;
import com.JJP.restapiserver.domain.dto.member.response.TokenRefreshResponse;
import com.JJP.restapiserver.domain.entity.member.RefreshToken;
import com.JJP.restapiserver.exception.TokenRefreshException;
import com.JJP.restapiserver.security.JwtUtils;
import com.JJP.restapiserver.service.RefreshTokenService;
import com.JJP.restapiserver.service.member.MemberServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@Tag(name = "MemberController", description = "사용자 API")
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/user")
public class MemberController {

    @Autowired
    MemberServiceImpl memberService;

    // RereshToken 생성을 위한 Service
    @Autowired
    RefreshTokenService refreshTokenService;

    // jwt 토큰 생성을 위한 JWT Util
    @Autowired
    JwtUtils jwtUtils;

    @Operation(summary = "로그인", description = "username과 password를 이용하여 로그인 합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = JwtResponse.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "401", description = "UNAUTHORIZED"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) { // 아이디, 비밀번호를 body에 담아 전송
        return memberService.login(loginRequest);
    }

    //    @ApiOperation(value = "회원가입", notes = "사이트 내 정보를 등록하여 회원가입 합니다.")
    @Operation(summary = "회원가입", description = "username, password, nickname는 필수 입력 값 입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        return memberService.register(signUpRequest);
    }

    //    @ApiOperation(value = "회원탈퇴", notes = "회원 탈퇴 신청 시, 비밀번호가 필요하며 회원은 휴면처리 됩니다.")
    @Operation(summary = "회원탈퇴", description = "accessToken으로 사용자 정보를 얻고, 입력한 password와 일치 시 회원은 휴면처리 됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST", content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "401", description = "UNAUTHORIZED"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @PostMapping("/delete")
    public ResponseEntity<?> deleteUser(@Valid @RequestBody DeleteUserRequest deleteUserRequest, HttpServletRequest request) {
        Long user_id = jwtUtils.getUserIdFromJwtToken(request.getHeader("Authorization"));
        return memberService.delete(deleteUserRequest.getPassword(), user_id);
    }

    @Operation(summary = "아이디 중복 체크", description = "username이 중복인지 여부를 판단합니다. 'idcheck/' url 뒤, username을 입력하십시오.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "401", description = "BAD REQUEST", content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @PostMapping("/idcheck/{username}")
    public ResponseEntity<?> usernameCheck(@PathVariable String username) {
        return memberService.usernameCheck(username);
    }

    //    @ApiOperation(value = "nickname 중복체크", notes = "동일한 nickname을 사용할 수 없습니다.")
    @Operation(summary = "닉네임 중복 체크", description = "nickname이 중복인지 여부를 판단합니다. 'nickcheck/' url 뒤, nickname을 입력하십시오.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "401", description = "BAD REQUEST", content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @PostMapping("/nickcheck/{nickname}")
    public ResponseEntity<?> nicknameCheck(@PathVariable String nickname) {
        return memberService.nicknameCheck(nickname);
    }

    @Operation(summary = "로그아웃", description = "id를 통해 사용자 정보를 조회하여, refreshToken을 지웁니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "401", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(@Valid @RequestBody LogoutRequest logoutRequest) {
        refreshTokenService.deleteByMemberId(logoutRequest.getId());
        return ResponseEntity.ok(new MessageResponse("Log out successful"));
    }

    //    @ApiOperation(value = "리프레시토큰 생성", notes = "accessToken 만료시, refreshToken을 발급받습니다. accessToken의 만료시간은 5시간입니다.")
    @Operation(summary = "리프레시토큰 생성", description = "refreshToken을 담아보내면, Token이 새로 생성되어 반환됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = TokenRefreshResponse.class))),
            @ApiResponse(responseCode = "403", description = "FORBIDDEN", content = @Content(schema = @Schema(implementation = TokenRefreshException.class))),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @PostMapping("/refreshToken")
    public ResponseEntity<?> refreshtoken(@Valid @RequestBody TokenRefreshRequest tokenRefreshRequest) {
        String requestRefreshToken = tokenRefreshRequest.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getMember)
                .map(user -> {
                    String token = jwtUtils.generateTokenFromUsername(user.getUsername());
                    return ResponseEntity.ok(new TokenRefreshResponse(token, requestRefreshToken));
                }).
                orElseThrow(() -> new TokenRefreshException(requestRefreshToken
                        , "Refresh token is not in database!"));
    }

    @Operation(summary = "회원정보 수정", description = "회원정보 수정이 가능합니다. 단, 이메일(username)은 변경할 수 없습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST", content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @PostMapping("/update/{userId}")
    public ResponseEntity<?> updateUser(@PathVariable Long userId, @Valid @RequestBody UpdateUserRequest updateUserRequest) {
        return memberService.update(userId, updateUserRequest);
    }


    @Operation(summary = "비밀번호 업데이트", description = "비밀번호를 동일하게 입력하여야만 수정가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST", content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @PostMapping("/updatepw")
    public ResponseEntity<?> updatePassword(@RequestBody PwUpdateRequest pwUpdateRequest, HttpServletRequest request) {
        Long user_id = jwtUtils.getUserIdFromJwtToken(request.getHeader("Authorization"));
        return memberService.updatePassword(pwUpdateRequest, user_id);
    }

    //    @ApiOperation(value = "비밀번호 찾기", notes = "사용자 이메일과, 성명을 정확히 입력해야 비밀번호 갱신이 가능합니다.")
    @Operation(summary = "비밀번호 업데이트", description = "사용자 이메일(username)과, 성명(fullname)을 정확히 입력해야 비밀번호 갱신이 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST", content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR", content = @Content(schema = @Schema(implementation = MessageResponse.class)))
    })
    @PostMapping("/resetpw")
    public ResponseEntity<?> resetPassword(@RequestBody PasswordRequest request) {
        return memberService.resetPassword(request);
    }

    @Operation(summary = "타유저 정보 획득", description = "타 유저의 정보를 알고 싶을 경우 'info/' url 뒤 타 유저의 userId를 통해 사용자 정보를 조회할 수 있습니다. ")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = MemberInfoResponse.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST", content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @GetMapping("/info/{userId}")
    public ResponseEntity<?> getUserInfo(@PathVariable Long userId) {
        return memberService.findUser(userId);
    }

    //    @ApiOperation(value = "자신의 유저 정보 획득", notes = "회원정보 수저을 위한 자신의 개인 정보를 획득할 수 있습니다. ")
    @Operation(summary = "유저 정보(본인) 획득 - 회원정보 수정 시, 본인 정보 조회", description = "본인의 회원 정보를 알고 싶을 경우 accessToken을 통해 사용자 정보를 조회할 수 있습니다. ")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = MemberInfoResponse.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST", content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @GetMapping("/myinfo")
    public ResponseEntity<?> getMyInfo(HttpServletRequest request) {
        //  TOKEN에서 UserID 추출
        Long user_id = jwtUtils.getUserIdFromJwtToken(request.getHeader("Authorization"));
        return memberService.findUser(user_id);
    }
}


