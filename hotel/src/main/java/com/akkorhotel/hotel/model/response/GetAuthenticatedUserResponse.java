package com.akkorhotel.hotel.model.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class GetAuthenticatedUserResponse {
    private String username;
    private String email;
    private String userRole;
}
