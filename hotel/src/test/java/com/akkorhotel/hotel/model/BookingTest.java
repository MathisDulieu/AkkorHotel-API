package com.akkorhotel.hotel.model;

import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

class BookingTest {

    @Test
    void shouldBuildBookingWithDefaultValues() {
        // Arrange
        Booking booking = Booking.builder()
                .user(User.builder()
                        .username("user")
                        .email("user@example.com")
                        .password("password")
                        .build())
                .hotel(Hotel.builder()
                        .name("hotel")
                        .description("description")
                        .build())
                .checkInDate(new Date(1677628800000L))
                .checkOutDate(new Date(1677715200000L))
                .guests(2)
                .specialRequest("specialRequest")
                .build();

        // Assert
        assertThat(booking.getId()).isNull();
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.PENDING);
        assertThat(booking.getCheckInDate()).isEqualTo(new Date(1677628800000L));
        assertThat(booking.getCheckOutDate()).isEqualTo(new Date(1677715200000L));
        assertThat(booking.getGuests()).isEqualTo(2);
        assertThat(booking.getSpecialRequest()).isEqualTo("specialRequest");

        assertThat(booking.getUser().getId()).isNull();
        assertThat(booking.getUser().getUsername()).isEqualTo("user");
        assertThat(booking.getUser().getEmail()).isEqualTo("user@example.com");
        assertThat(booking.getUser().getPassword()).isEqualTo("password");
        assertThat(booking.getUser().getIsValidEmail()).isFalse();
        assertThat(booking.getUser().getRole()).isEqualTo(UserRole.USER);

        assertThat(booking.getHotel().getId()).isNull();
        assertThat(booking.getHotel().getPicture_list()).isEqualTo(emptyList());
        assertThat(booking.getHotel().getName()).isEqualTo("hotel");
        assertThat(booking.getHotel().getDescription()).isEqualTo("description");

        assertThat(booking.getHotel().getLocation()).isNotNull();
        assertThat(booking.getHotel().getLocation().getId()).isNull();
        assertThat(booking.getHotel().getLocation().getAddress()).isNull();
        assertThat(booking.getHotel().getLocation().getCity()).isNull();
        assertThat(booking.getHotel().getLocation().getState()).isNull();
        assertThat(booking.getHotel().getLocation().getCountry()).isNull();
        assertThat(booking.getHotel().getLocation().getPostalCode()).isNull();
        assertThat(booking.getHotel().getLocation().getGoogleMapsUrl()).isNull();
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
                .build();

        Hotel hotel = Hotel.builder()
                .id("id")
                .name("name")
                .description("description")
                .picture_list(List.of("picture1", "picture2"))
                .location(HotelLocation.builder()
                        .id("id")
                        .address("address")
                        .city("city")
                        .state("state")
                        .country("country")
                        .postalCode("postalCode")
                        .googleMapsUrl("googleMapUrl")
                        .build())
                .build();

        Booking booking = Booking.builder()
                .id("id")
                .user(user)
                .hotel(hotel)
                .checkInDate(new Date(1677628800000L))
                .checkOutDate(new Date(1677715200000L))
                .guests(4)
                .status(BookingStatus.CONFIRMED)
                .specialRequest("specialRequest")
                .build();

        // Assert
        assertThat(booking.getId()).isEqualTo("id");
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        assertThat(booking.getCheckInDate()).isEqualTo(new Date(1677628800000L));
        assertThat(booking.getCheckOutDate()).isEqualTo(new Date(1677715200000L));
        assertThat(booking.getGuests()).isEqualTo(4);
        assertThat(booking.getSpecialRequest()).isEqualTo("specialRequest");

        assertThat(booking.getUser().getId()).isEqualTo("id");
        assertThat(booking.getUser().getUsername()).isEqualTo("username");
        assertThat(booking.getUser().getEmail()).isEqualTo("email");
        assertThat(booking.getUser().getPassword()).isEqualTo("password");
        assertThat(booking.getUser().getIsValidEmail()).isTrue();
        assertThat(booking.getUser().getRole()).isEqualTo(UserRole.ADMIN);

        assertThat(booking.getHotel().getId()).isEqualTo("id");
        assertThat(booking.getHotel().getPicture_list()).isEqualTo(List.of("picture1", "picture2"));
        assertThat(booking.getHotel().getName()).isEqualTo("name");
        assertThat(booking.getHotel().getDescription()).isEqualTo("description");

        assertThat(hotel.getLocation()).isNotNull();
        assertThat(hotel.getLocation().getId()).isEqualTo("id");
        assertThat(hotel.getLocation().getAddress()).isEqualTo("address");
        assertThat(hotel.getLocation().getCity()).isEqualTo("city");
        assertThat(hotel.getLocation().getState()).isEqualTo("state");
        assertThat(hotel.getLocation().getCountry()).isEqualTo("country");
        assertThat(hotel.getLocation().getPostalCode()).isEqualTo("postalCode");
        assertThat(hotel.getLocation().getGoogleMapsUrl()).isEqualTo("googleMapUrl");
    }
}
