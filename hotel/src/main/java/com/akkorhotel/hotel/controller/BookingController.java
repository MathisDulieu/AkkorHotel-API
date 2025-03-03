package com.akkorhotel.hotel.controller;

import com.akkorhotel.hotel.model.User;
import com.akkorhotel.hotel.model.request.CreateBookingRequest;
import com.akkorhotel.hotel.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/private/booking")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @Operation(
            tags = {"Booking"},
            summary = "Create a new booking",
            description = """
            Allows a user to create a new booking for a hotel room.
    
            ## Notes:
            - The user must be authenticated via bearer token.
            - The booking will be created for the specified hotel room.
            - The number of guests must be less than or equal to the maximum occupancy of the room.
            - The check-in and check-out dates must be valid.
            """,
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Booking created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Booking Created",
                                    value = """
                                    {
                                        "message": "Booking created successfully"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request or validation errors",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Invalid Guests Count",
                                            value = """
                                            {
                                                "errors": [
                                                    "The number of guests must be greater than zero"
                                                ]
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Invalid Check-in Date",
                                            value = """
                                            {
                                                "errors": [
                                                    "Check-in date must be after today's date"
                                                ]
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Check-out Date Error",
                                            value = """
                                            {
                                                "errors": [
                                                    "Check-out date must be after check-in date"
                                                ]
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Guests Exceeds Max Occupancy",
                                            value = """
                                            {
                                                "errors": [
                                                    "The number of guests exceeds the maximum occupancy for this hotel room"
                                                ]
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Hotel Room Not Found",
                                            value = """
                                            {
                                                "errors": [
                                                    "The specified hotel room was not found in the hotel's room list"
                                                ]
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Hotel Not Found",
                                            value = """
                                            {
                                                "errors": [
                                                    "Hotel not found"
                                                ]
                                            }
                                            """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Hotel not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Hotel Not Found",
                                    value = """
                                    {
                                        "error": "Hotel not found"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Hotel Room not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Hotel Room Not Found",
                                    value = """
                                    {
                                        "error": "HotelRoom not found"
                                    }
                                    """
                            )
                    )
            )
    })
    public ResponseEntity<Map<String, String>> createBooking(
            @AuthenticationPrincipal User authenticatedUser,

            @RequestBody(description = "Booking creation request", required = true, content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "Create Booking Request",
                            value = """
                            {
                                "hotelId": "hotelId123",
                                "hotelRoomId": "hotelRoomId123",
                                "guests": 3,
                                "checkInDate": "2025-03-10T14:00:00",
                                "checkOutDate": "2025-03-15T12:00:00"
                            }
                            """
                    )
            )) @org.springframework.web.bind.annotation.RequestBody CreateBookingRequest request) {

        return bookingService.createBooking(authenticatedUser.getId(), request);
    }

}
