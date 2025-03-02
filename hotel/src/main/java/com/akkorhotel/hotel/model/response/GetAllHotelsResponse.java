package com.akkorhotel.hotel.model.response;

import com.akkorhotel.hotel.model.Hotel;
import lombok.*;

import java.util.List;

import static java.util.Collections.emptyList;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class GetAllHotelsResponse {

    @Builder.Default
    private List<Hotel> hotels = emptyList();

    private int totalPages;
    private String error;
}
