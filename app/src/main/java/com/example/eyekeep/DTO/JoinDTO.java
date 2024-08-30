package com.example.eyekeep.DTO;

/** 사용자 계정 정보 모델 클래스
 *
 */

public class JoinDTO {
    private String username; // 이메일 아이디
    private String password; // 비밀번호
    private String passwordCheck; //비밀번호 확인
    private String nickname; // 닉네임

    public JoinDTO(String username, String password, String passwordCheck, String nickname) {
        this.username = username;
        this.password = password;
        this.passwordCheck = passwordCheck;
        this.nickname = nickname;

    }

}