package com.akkorhotel.hotel.model.response;

import com.akkorhotel.hotel.model.User;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class GetUserByIdResponse {
    private User user;
    private String error;
}
