package com.study.platform.web.dto;

public class UserProfileDto {
    private Long userId;
    private String name;
    private String email;
    private String phone;  // === Добавлено поле для телефона ===

    public UserProfileDto() {}

    public UserProfileDto(Long userId, String name, String email, String phone) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    // Геттеры и сеттеры
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}
