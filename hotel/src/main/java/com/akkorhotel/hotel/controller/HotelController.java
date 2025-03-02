package com.akkorhotel.hotel.controller;

import com.akkorhotel.hotel.model.response.GetAllHotelsResponse;
import com.akkorhotel.hotel.model.response.GetHotelResponse;
import com.akkorhotel.hotel.service.HotelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/hotel")
@RequiredArgsConstructor
public class HotelController {

    private final HotelService hotelService;

    @GetMapping("/{hotelId}")
    @Operation(
            tags = {"Hotel"},
            summary = "Get information about an existing hotel",
            description = """
            Allows an authenticated user to retrieve the information of an existing hotel.
            
            ## Notes:
            - The hotel must already exist.
            - If the hotel does not exist, an error message is returned.
            """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Hotel information retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Successful Hotel Information Retrieval",
                                    value = """
                                    {
                                        "_id": "f2cccd2f-5711-4356-a13a-f687dc983ce1",
                                        "name": "LuxuryHotel",
                                        "description": "A five-star experience.",
                                        "picture_list": ["https://mocked-image-url.com/hotel1.jpg", "https://mocked-image-url.com/hotel2.jpg"],
                                        "amenities": ["POOL", "WIFI"],
                                        "rooms": [
                                            {
                                                "_id": "f2cccd2f-5711-4356-a13a-f687dc983ce2",
                                                "type": "SINGLE",
                                                "price": 120,
                                                "maxOccupancy": 3,
                                                "features": ["ROOM_SERVICE", "BALCONY"]
                                            },
                                            {
                                                "_id": "f2cccd2f-5711-4356-a13a-f687dc983ce3",
                                                "type": "DOUBLE",
                                                "price": 150,
                                                "maxOccupancy": 5,
                                                "features": ["WIFI", "HAIR_DRYER"]
                                            }
                                        ],
                                        "location": {
                                            "_id": "f2cccd2f-5711-4356-a13a-f687dc983ce4",
                                            "address": "123 Rue de la Paix",
                                            "city": "Paris",
                                            "state": "Île-de-France",
                                            "country": "France",
                                            "postalCode": "75001",
                                            "googleMapsUrl": "https://maps.google.com/?q=LuxuryHotel"
                                        }
                                    }
                                    """
                            )
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
            )
    })
    public ResponseEntity<Map<String, GetHotelResponse>> getHotel(
            @PathVariable String hotelId
    ) {
        return hotelService.getHotel(hotelId);
    }

    @GetMapping
    @Operation(
            tags = {"Hotel"},
            summary = "Get all hotels with name matching prefix",
            description = """
            Retrieves all hotels whose name starts with the specified keyword.
            Results are paginated. If pageSize is 0, a default page size of 10 will be used.
            
            ## Parameters:
            - keyword: Prefix to search for in hotel names
            - page: Zero-based page index
            - pageSize: Number of hotels per page
            """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Hotels retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Successful Hotels Retrieval",
                                    value = """
                                    {
                                        "informations": {
                                            "hotels": [
                                                {
                                                    "id": "hotelId1",
                                                    "name": "LuxuryHotel",
                                                    "description": "A five-star experience.",
                                                    "picture_list": ["https://mocked-image-url.com/hotel1.jpg"],
                                                    "amenities": ["POOL", "WIFI"],
                                                    "rooms": [],
                                                    "location": {
                                                        "id": "locationId1",
                                                        "address": "123 Rue de la Paix",
                                                        "city": "Paris",
                                                        "state": "Île-de-France",
                                                        "country": "France",
                                                        "postalCode": "75001",
                                                        "googleMapsUrl": "https://maps.google.com/?q=LuxuryHotel"
                                                    }
                                                },
                                                {
                                                    "id": "hotelId2",
                                                    "name": "LuxurySpa",
                                                    "description": "Relaxation at its finest.",
                                                    "picture_list": ["https://mocked-image-url.com/spa1.jpg"],
                                                    "amenities": ["SPA", "WIFI"],
                                                    "rooms": [],
                                                    "location": {
                                                        "id": "locationId2",
                                                        "address": "456 Avenue des Champs-Élysées",
                                                        "city": "Paris",
                                                        "state": "Île-de-France",
                                                        "country": "France",
                                                        "postalCode": "75008",
                                                        "googleMapsUrl": "https://maps.google.com/?q=LuxurySpa"
                                                    }
                                                }
                                            ],
                                            "totalPages": 3,
                                            "error": null
                                        }
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "200",
                    description = "No hotels found matching the criteria",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "No Hotels Found",
                                    value = """
                                    {
                                        "informations": {
                                            "hotels": [],
                                            "totalPages": 0,
                                            "error": "No hotel found"
                                        }
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "200",
                    description = "Requested page exceeds available pages",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Page Exceeds Total",
                                    value = """
                                    {
                                        "warning": {
                                            "hotels": [],
                                            "totalPages": 5,
                                            "error": "Requested page exceeds the total number of available pages"
                                        }
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request parameters",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Negative Page Size",
                                            value = """
                                            {
                                                "error": {
                                                    "hotels": [],
                                                    "totalPages": 0,
                                                    "error": "Page size must be greater than or equal to zero"
                                                }
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Negative Page Number",
                                            value = """
                                            {
                                                "error": {
                                                    "hotels": [],
                                                    "totalPages": 0,
                                                    "error": "Page number must be greater than or equal to zero"
                                                }
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Invalid Keyword",
                                            value = """
                                            {
                                                "error": {
                                                    "hotels": [],
                                                    "totalPages": 0,
                                                    "error": "Search keyword cannot contain spaces"
                                                }
                                            }
                                            """
                                    )
                            }
                    )
            ),
    })
    public ResponseEntity<Map<String, GetAllHotelsResponse>> getHotels(
            @Parameter(description = "Hotel name prefix to search for", example = "Luxury")
            @RequestParam(required = false, defaultValue = "") String keyword,

            @Parameter(description = "Zero-based page index", example = "0")
            @RequestParam(required = false, defaultValue = "0") int page,

            @Parameter(description = "Page size (number of hotels per page). Default is 10 if set to 0", example = "10")
            @RequestParam(required = false, defaultValue = "0") int pageSize) {

        return hotelService.getHotels(keyword, page, pageSize);
    }

}
