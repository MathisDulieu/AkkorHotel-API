package com.akkorhotel.hotel.model;

import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BookingTest {

    @Test
    void shouldBuildBookingWithDefaultValues() {
        // Arrange
        Booking booking = Booking.builder()
                .id("id")
                .user(User.builder().id("id").build())
                .hotel(Hotel.builder().id("id").build())
                .hotelRoom(HotelRoom.builder().id("id").build())
                .checkInDate(new Date(1677628800000L))
                .checkOutDate(new Date(1677715200000L))
                .guests(2)
                .totalPrice(568)
                .specialRequest("specialRequest")
                .build();

        // Assert
        assertThat(booking.getId()).isEqualTo("id");

        assertThat(booking.getUser()).isNotNull();
        assertThat(booking.getUser().getId()).isEqualTo("id");

        assertThat(booking.getHotel()).isNotNull();
        assertThat(booking.getHotel().getId()).isEqualTo("id");

        assertThat(booking.getHotelRoom()).isNotNull();
        assertThat(booking.getHotelRoom().getId()).isEqualTo("id");

        assertThat(booking.getCheckInDate()).isEqualTo(new Date(1677628800000L));
        assertThat(booking.getCheckOutDate()).isEqualTo(new Date(1677715200000L));
        assertThat(booking.getGuests()).isEqualTo(2);
        assertThat(booking.getTotalPrice()).isEqualTo(568);
        assertThat(booking.getSpecialRequest()).isEqualTo("specialRequest");

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.PENDING);
    }

    @Test
    void shouldOverrideBookingDefaultValuesWhenSpecified() {
        // Arrange
        User user = User.builder()
                .id("id")
                .isValidEmail(true)
                .role(UserRole.ADMIN)
                .username("username")
                .email("email")
                .password("password")
                .profileImageUrl("profileImageUrl")
                .build();

        Hotel hotel = Hotel.builder()
                .id("id")
                .name("name")
                .description("description")
                .location(HotelLocation.builder()
                        .id("id")
                        .address("address")
                        .city("city")
                        .state("state")
                        .country("country")
                        .postalCode("postalCode")
                        .googleMapsUrl("googleMapUrl")
                        .build())
                .picture_list(List.of("picture1", "picture2"))
                .amenities(List.of(HotelAmenities.AIRPORT_SHUTTLE, HotelAmenities.BAR))
                .rooms(List.of(
                                HotelRoom.builder()
                                        .id("id1")
                                        .maxOccupancy(5)
                                        .price(256)
                                        .type(HotelRoomType.DELUXE)
                                        .features(List.of(HotelRoomFeatures.FLAT_SCREEN_TV, HotelRoomFeatures.SAFE))
                                        .build(),
                                HotelRoom.builder()
                                        .id("id2")
                                        .maxOccupancy(2)
                                        .price(150)
                                        .type(HotelRoomType.SINGLE)
                                        .features(List.of(HotelRoomFeatures.ROOM_SERVICE, HotelRoomFeatures.BALCONY))
                                        .build()
                        )
                )
                .build();

        HotelRoom hotelRoom = HotelRoom.builder()
                .id("id2")
                .type(HotelRoomType.SINGLE)
                .price(150)
                .maxOccupancy(2)
                .features(List.of(HotelRoomFeatures.ROOM_SERVICE, HotelRoomFeatures.BALCONY))
                .build();

        Booking booking = Booking.builder()
                .id("id")
                .user(user)
                .hotel(hotel)
                .hotelRoom(hotelRoom)
                .checkInDate(new Date(1677628800000L))
                .checkOutDate(new Date(1677715200000L))
                .guests(2)
                .totalPrice(568)
                .specialRequest("specialRequest")
                .isPaid(true)
                .status(BookingStatus.CONFIRMED)
                .build();

        // Assert
        assertThat(booking.getId()).isEqualTo("id");
        assertThat(booking.getCheckInDate()).isEqualTo(new Date(1677628800000L));
        assertThat(booking.getCheckOutDate()).isEqualTo(new Date(1677715200000L));
        assertThat(booking.getGuests()).isEqualTo(2);
        assertThat(booking.getTotalPrice()).isEqualTo(568);
        assertThat(booking.getSpecialRequest()).isEqualTo("specialRequest");
        assertThat(booking.isPaid()).isTrue();
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);

        assertThat(booking.getUser()).isNotNull();
        assertThat(booking.getUser().getId()).isEqualTo("id");
        assertThat(booking.getUser().getUsername()).isEqualTo("username");
        assertThat(booking.getUser().getEmail()).isEqualTo("email");
        assertThat(booking.getUser().getPassword()).isEqualTo("password");
        assertThat(booking.getUser().getIsValidEmail()).isTrue();
        assertThat(booking.getUser().getRole()).isEqualTo(UserRole.ADMIN);
        assertThat(booking.getUser().getProfileImageUrl()).isEqualTo("profileImageUrl");

        assertThat(booking.getHotel()).isNotNull();
        assertThat(booking.getHotel().getId()).isEqualTo("id");
        assertThat(booking.getHotel().getPicture_list()).isEqualTo(List.of("picture1", "picture2"));
        assertThat(booking.getHotel().getName()).isEqualTo("name");
        assertThat(booking.getHotel().getDescription()).isEqualTo("description");
        assertThat(booking.getHotel().getAmenities()).isEqualTo(List.of(HotelAmenities.AIRPORT_SHUTTLE, HotelAmenities.BAR));
        assertThat(booking.getHotel().getRooms()).isEqualTo(List.of(
                HotelRoom.builder()
                        .id("id1")
                        .maxOccupancy(5)
                        .price(256)
                        .type(HotelRoomType.DELUXE)
                        .features(List.of(HotelRoomFeatures.FLAT_SCREEN_TV, HotelRoomFeatures.SAFE))
                        .build(),
                HotelRoom.builder()
                        .id("id2")
                        .maxOccupancy(2)
                        .price(150)
                        .type(HotelRoomType.SINGLE)
                        .features(List.of(HotelRoomFeatures.ROOM_SERVICE, HotelRoomFeatures.BALCONY))
                        .build()
        ));

        assertThat(booking.getHotel().getLocation()).isNotNull();
        assertThat(booking.getHotel().getLocation().getId()).isEqualTo("id");
        assertThat(booking.getHotel().getLocation().getAddress()).isEqualTo("address");
        assertThat(booking.getHotel().getLocation().getCity()).isEqualTo("city");
        assertThat(booking.getHotel().getLocation().getState()).isEqualTo("state");
        assertThat(booking.getHotel().getLocation().getCountry()).isEqualTo("country");
        assertThat(booking.getHotel().getLocation().getPostalCode()).isEqualTo("postalCode");
        assertThat(booking.getHotel().getLocation().getGoogleMapsUrl()).isEqualTo("googleMapUrl");
    }

}
