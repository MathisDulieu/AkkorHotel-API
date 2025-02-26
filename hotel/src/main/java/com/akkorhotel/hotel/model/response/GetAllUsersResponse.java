package com.akkorhotel.hotel.model.response;

import com.akkorhotel.hotel.model.User;
import lombok.*;

import java.util.List;

import static java.util.Collections.emptyList;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class GetAllUsersResponse {

    @Builder.Default
    private List<User> users = emptyList();

    private int totalPages;
    private String error;
}
