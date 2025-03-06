package com.akkorhotel.hotel.model.request;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetHotelsFilters {
    private boolean oneStar;
    private boolean twoStars;
    private boolean threeStars;
    private boolean fourStars;
    private boolean fiveStars;
    private List<String> hotelAmenities;
    private int minPrice;

    @Builder.Default
    private int maxPrice = 2000;

    @Builder.Default
    private int guests = 1;

    @Builder.Default
    private int bedrooms = 1;
    private String city;
}
