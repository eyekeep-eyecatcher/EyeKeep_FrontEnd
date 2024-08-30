package com.example.eyekeep.DTO;

public class LoginDTO {
    private String username; // 이메일 아이디
    private String password; // 비밀번호

    public LoginDTO(String username, String password) {
        this.username = username;
        this.password = password;

    }
}
