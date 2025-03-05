package com.akkorhotel.hotel.controller;

import com.akkorhotel.hotel.model.User;
import com.akkorhotel.hotel.model.request.CreateBookingRequest;
import com.akkorhotel.hotel.model.request.UpdateBookingRequest;
import com.akkorhotel.hotel.model.response.GetBookingResponse;
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
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/{bookingId}")
    @Operation(
            tags = {"Booking"},
            summary = "Retrieve a booking by ID",
            description = """
            Retrieves booking details for the authenticated user.
    
            ## Notes:
            - The user must be authenticated via a bearer token.
            - Only the owner of the booking can access the booking details.
            - Returns 404 if the booking does not exist.
            - Returns 403 if the authenticated user does not own the booking.
            """,
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Booking retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Booking Found",
                                    value = """
                                    {
                                        "informations": {
                                            "_id": "bookingId123",
                                            "userId": "userId456",
                                            "status": "PENDING",
                                            "isPaid": false,
                                            "totalPrice": 600.0,
                                            "checkInDate": "2025-03-10T14:00:00",
                                            "checkOutDate": "2025-03-15T12:00:00",
                                            "guests": 3,
                                            "hotelRoom": {
                                                "_id": "hotelRoomId123",
                                                "price": 120.0,
                                                "maxOccupancy": 3,
                                                "features": ["ROOM_SERVICE", "BALCONY"],
                                                "type": "SINGLE"
                                            },
                                            "hotel": {
                                                "_id": "hotelId123",
                                                "name": "Hotel Paradise",
                                                "picture_list": [
                                                    "https://example.com/pic1.jpg",
                                                    "https://example.com/pic2.jpg"
                                                ],
                                                "amenities": ["PARKING", "BAR", "POOL"],
                                                "location": {
                                                    "address": "123 Paradise St",
                                                    "city": "Paradise City",
                                                    "country": "USA"
                                                }
                                            }
                                        }
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "User does not have permission to access this booking",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Access Denied",
                                    value = """
                                    {
                                        "error": "You do not have permission to access this booking"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Booking not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Booking Not Found",
                                    value = """
                                    {
                                        "error": "Booking not found"
                                    }
                                    """
                            )
                    )
            )
    })
    public ResponseEntity<Map<String, GetBookingResponse>> getBooking(
            @AuthenticationPrincipal User authenticatedUser,
            @PathVariable String bookingId
    ) {
        return bookingService.getBooking(authenticatedUser.getId(), bookingId);
    }

    @PutMapping
    @Operation(
            tags = {"Booking"},
            summary = "Update an existing booking",
            description = """
            Allows a user to update an existing booking.
            
            ## Notes:
            - The user must be authenticated via bearer token.
            - The user can update the number of guests, check-in date, or check-out date.
            - At least one field must be provided for an update.
            - The number of guests must be greater than zero and within the room's max occupancy.
            - The check-in date must be after today's date.
            - The check-out date must be after the check-in date.
            - The user can only update their own bookings.
            """,
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Booking updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Booking Updated",
                                    value = """
                                    {
                                        "message": "Booking updated successfully"
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
                                            name = "Missing Booking ID",
                                            value = """
                                            {
                                                "error": "BookingId is required"
                                            }
                                            """
                                    ),
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
                                                "error": "The number of guests exceeds the maximum occupancy for this hotel room"
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "No Fields Provided",
                                            value = """
                                            {
                                                "errors": [
                                                    "At least one field must be provided for the update"
                                                ]
                                            }
                                            """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "User does not have permission to update this booking",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Forbidden Access",
                                    value = """
                                    {
                                        "error": "You do not have permission to access this booking"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Booking not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Booking Not Found",
                                    value = """
                                    {
                                        "error": "Booking not found"
                                    }
                                    """
                            )
                    )
            )
    })
    public ResponseEntity<Map<String, String>> updateBooking(
            @AuthenticationPrincipal User authenticatedUser,
            @RequestBody(description = "Booking update request", required = true, content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "Update Booking Request",
                            value = """
                            {
                                "bookingId": "bookingId123",
                                "guests": 2,
                                "checkInDate": "2025-04-01T14:00:00",
                                "checkOutDate": "2025-04-05T12:00:00"
                            }
                            """
                    )
            )) @org.springframework.web.bind.annotation.RequestBody UpdateBookingRequest request) {

        return bookingService.updateBooking(authenticatedUser.getId(), request);
    }


}
