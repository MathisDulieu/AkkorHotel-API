package com.akkorhotel.hotel.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@RequiredArgsConstructor
public enum HotelRoomType {

    SINGLE("Single", "A room with one bed for a single person."),
    DOUBLE("Double", "A room with one double bed for two people."),
    TWIN("Twin", "A room with two single beds for two people."),
    SUITE("Suite", "A larger room with additional living space, typically for multiple guests."),
    FAMILY("Family", "A room designed for families, often with multiple beds and additional space."),
    PENTHOUSE("Penthouse", "A luxurious top-floor room with premium amenities and panoramic views."),
    EXECUTIVE("Executive", "A room designed for business travelers, with workspaces and additional facilities."),
    DELUXE("Deluxe", "A high-end room offering premium amenities and more space than a standard room."),
    STANDARD("Standard", "A basic room with standard amenities.");

    private final String name;
    private final String description;

}
