package com.akkorhotel.hotel.model.response;

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

    private long hotelsFound;
    private int totalPages;

    @Builder.Default
    private List<GetAllHotelsHotelResponse> hotels = emptyList();

    private String error;
}
