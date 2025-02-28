package com.akkorhotel.hotel.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@RequiredArgsConstructor
public enum HotelRoomFeatures {

    KING_SIZE_BED("King Size Bed", "Large bed for two people with a spacious layout."),
    AIR_CONDITIONING("Air Conditioning", "Climate control for a comfortable temperature."),
    FLAT_SCREEN_TV("Flat Screen TV", "HD TV with satellite channels available."),
    MINI_FRIDGE("Mini Fridge", "Small refrigerator for storing beverages and snacks."),
    DESK("Desk", "Work desk for business travelers."),
    SAFE("Safe", "In-room safe for storing valuables."),
    COFFEE_MACHINE("Coffee Machine", "Coffee maker with free coffee and tea."),
    WIFI("Wi-Fi", "Free wireless internet access throughout the room."),
    ROOM_SERVICE("Room Service", "24/7 room service available for food and beverages."),
    BALCONY("Balcony", "Private outdoor space with seating."),
    SHOWER("Shower", "Shower stall with hot and cold water."),
    BATHTUB("Bathtub", "Full bathtub for a relaxing soak."),
    HAIR_DRYER("Hair Dryer", "Hair dryer available for guest use."),
    CLOSET("Closet", "Storage space for clothes and personal items."),
    SMOKE_DETECTED("Smoke Detector", "Room equipped with smoke detectors for safety."),
    NO_SMOKING("No Smoking", "This room is a non-smoking room."),
    PET_FRIENDLY("Pet Friendly", "Pets are allowed in the room with prior notice.");

    private final String name;
    private final String description;

}
