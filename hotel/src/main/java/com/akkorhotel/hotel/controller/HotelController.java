package com.akkorhotel.hotel.controller;

import com.akkorhotel.hotel.model.request.GetHotelsRequest;
import com.akkorhotel.hotel.model.response.GetAllHotelsResponse;
import com.akkorhotel.hotel.model.response.GetHotelResponse;
import com.akkorhotel.hotel.service.HotelService;
import io.swagger.v3.oas.annotations.Operation;
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

    @PostMapping
    @Operation(
            tags = {"Hotel"},
            summary = "Get hotels with filters, sorting, and pagination",
            description = """
        Retrieves hotels based on various filters such as number of guests, bedrooms, sorting order, and pagination.
        Results are paginated. If pageSize is 0, a default page size of 10 will be used.

        ## Request Body:
        - page: Zero-based index for pagination.
        - pageSize: Number of hotels per page.
        - filter: Sorting option (PRICE_LOW_TO_HIGH or PRICE_HIGH_TO_LOW).
        - filters: Object containing additional filtering options.
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
                                "hotelId": "hotelId1",
                                "name": "LuxuryHotel",
                                "description": "A five-star experience.",
                                "firstPicture": "https://mocked-image-url.com/hotel1.jpg",
                                "price": 150.0,
                                "address": "123 Rue de la Paix",
                                "googleMapUrl": "https://maps.google.com/?q=LuxuryHotel"
                            },
                            {
                                "hotelId": "hotelId2",
                                "name": "LuxurySpa",
                                "description": "Relaxation at its finest.",
                                "firstPicture": "https://mocked-image-url.com/spa1.jpg",
                                "price": 180.0,
                                "address": "456 Avenue des Champs-Élysées",
                                "googleMapUrl": "https://maps.google.com/?q=LuxurySpa"
                            }
                        ],
                        "hotelsFound": 25,
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
                        "hotelsFound": 0,
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
                                            name = "Invalid Sorting Filter",
                                            value = """
                    {
                        "error": {
                            "hotels": [],
                            "totalPages": 0,
                            "error": "Invalid filter provided"
                        }
                    }
                    """
                                    ),
                                    @ExampleObject(
                                            name = "Negative Minimum Price",
                                            value = """
                    {
                        "error": {
                            "hotels": [],
                            "totalPages": 0,
                            "error": "Minimum price must be greater than or equal to zero"
                        }
                    }
                    """
                                    ),
                                    @ExampleObject(
                                            name = "Minimum Price Greater than Maximum Price",
                                            value = """
                    {
                        "error": {
                            "hotels": [],
                            "totalPages": 0,
                            "error": "Minimum price must be less than maximum price"
                        }
                    }
                    """
                                    ),
                                    @ExampleObject(
                                            name = "Invalid Guests Number",
                                            value = """
                    {
                        "error": {
                            "hotels": [],
                            "totalPages": 0,
                            "error": "Guests number must be greater than zero"
                        }
                    }
                    """
                                    ),
                                    @ExampleObject(
                                            name = "Invalid Bedrooms Number",
                                            value = """
                    {
                        "error": {
                            "hotels": [],
                            "totalPages": 0,
                            "error": "Bedrooms number must be greater than zero"
                        }
                    }
                    """
                                    ),
                                    @ExampleObject(
                                            name = "Invalid City Format",
                                            value = """
                    {
                        "error": {
                            "hotels": [],
                            "totalPages": 0,
                            "error": "City name contains invalid characters"
                        }
                    }
                    """
                                    )
                            }
                    )
            )
    })
    public ResponseEntity<Map<String, GetAllHotelsResponse>> getHotels(
            @RequestBody GetHotelsRequest request
    ) {
        return hotelService.getHotels(request);
    }

}
