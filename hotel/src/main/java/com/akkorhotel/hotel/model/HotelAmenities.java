package com.akkorhotel.hotel.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@RequiredArgsConstructor
public enum HotelAmenities {

    WIFI("Wi-Fi", "Wireless internet access available in all rooms and public areas."),
    POOL("Pool", "Outdoor swimming pool available."),
    GYM("Gym", "Fully equipped fitness center."),
    SPA("Spa", "Spa services including massages and relaxation therapies."),
    PARKING("Parking", "Free parking available for guests."),
    RESTAURANT("Restaurant", "On-site dining with local and international cuisine."),
    AIR_CONDITIONING("Air Conditioning", "Climate control with air conditioning in rooms."),
    PET_FRIENDLY("Pet Friendly", "Pets allowed in certain rooms."),
    AIRPORT_SHUTTLE("Airport Shuttle", "Shuttle service to and from the airport."),
    BAR("Bar", "On-site bar with a selection of drinks."),
    BUSINESS_CENTER("Business Center", "Business services available including printing and fax."),
    LAUNDRY("Laundry", "Laundry and dry cleaning services available."),
    SMOKING_AREA("Smoking Area", "Designated areas for smoking.");

    private final String name;
    private final String description;

}
