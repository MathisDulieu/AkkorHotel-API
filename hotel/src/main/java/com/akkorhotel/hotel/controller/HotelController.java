package com.akkorhotel.hotel.controller;

import com.akkorhotel.hotel.model.response.GetHotelResponse;
import com.akkorhotel.hotel.service.HotelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
