package com.akkorhotel.hotel.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminUpdateUserRequest {
    private String username;
    private String email;
    private String role;
    private String profileImageUrl;
    private Boolean isValidEmail;
}