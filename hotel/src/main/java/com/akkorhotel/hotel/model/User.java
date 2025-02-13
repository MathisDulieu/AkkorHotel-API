package com.akkorhotel.hotel.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;

@Getter
@Setter
@Builder
@EqualsAndHashCode
public class User {

    @Id
    private String id;

    private String username;
    private String email;

    @Builder.Default
    private Boolean isValidEmail = false;

    private String password;

    @Builder.Default
    private UserRole role = UserRole.USER;

}
