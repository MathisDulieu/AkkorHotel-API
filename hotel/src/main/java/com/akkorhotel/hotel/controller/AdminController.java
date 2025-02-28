package com.akkorhotel.hotel.controller;


import com.akkorhotel.hotel.model.User;
import com.akkorhotel.hotel.model.request.AdminUpdateUserRequest;
import com.akkorhotel.hotel.model.request.CreateHotelRequest;
import com.akkorhotel.hotel.model.response.GetAllUsersResponse;
import com.akkorhotel.hotel.model.response.GetUserByIdResponse;
import com.akkorhotel.hotel.service.AdminService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/private/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    @Operation(
            tags = {"Admin"},
            summary = "Get all users with username matching prefix",
            description = """
            Retrieves all users whose username starts with the specified keyword.
            Results are paginated. If pageSize is 0, a default page size of 10 will be used.
            
            ## Parameters:
            - keyword: Prefix to search for in usernames
            - page: Zero-based page index
            - pageSize: Number of users per page
            """,
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Users retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Successful Users Retrieval",
                                    value = """
                            {
                                "users": {
                                    "users": [
                                        {
                                            "id": "60f7b1e2b3e2a81b5cb4c735",
                                            "username": "alice123",
                                            "email": "alice@example.com",
                                            "profileImageUrl": "https://example.png"
                                        },
                                        {
                                            "id": "60f7b1e2b3e2a81b5cb4c736",
                                            "username": "alicia456",
                                            "email": "alicia@example.com",
                                            "profileImageUrl": "https://example.png"
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
                    description = "No users found matching the criteria",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "No Users Found",
                                    value = """
                            {
                                "users": {
                                    "users": [],
                                    "totalPages": 0,
                                    "error": "No users found"
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
                                    "users": [],
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
                                            "users": [],
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
                                            "users": [],
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
                                            "users": [],
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
    public ResponseEntity<Map<String, GetAllUsersResponse>> getAllUsers(
            @Parameter(description = "Username prefix to search for", example = "al")
            @RequestParam(required = false, defaultValue = "") String keyword,

            @Parameter(description = "Zero-based page index", example = "0")
            @RequestParam(required = false, defaultValue = "0") int page,

            @Parameter(description = "Page size (number of users per page). Default is 10 if set to 0", example = "10")
            @RequestParam(required = false, defaultValue = "10") int pageSize) {

        return adminService.getAllUsers(keyword, page, pageSize);
    }

    @GetMapping("/user/{userId}")
    @Operation(
            tags = {"Admin"},
            summary = "Get a user by ID",
            description = """
            Retrieves a specific user by their unique ID.
            
            ## Responses:
            - **200 OK**: User found and returned.
            - **403 FORBIDDEN**: The user is an admin and cannot be retrieved.
            - **404 NOT FOUND**: No user exists with the given ID.
            """,
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Successful User Retrieval",
                                    value = """
                        {
                            "user": {
                                "id": "60f7b1e2b3e2a81b5cb4c735",
                                "username": "alice123",
                                "email": "alice@example.com",
                                "profileImageUrl": "https://example.png",
                                "role": "USER",
                                "isValidEmail": true
                            }
                        }
                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Admin users cannot be retrieved",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Forbidden Admin User",
                                    value = """
                        {
                            "error": "Admin users cannot be retrieved"
                        }
                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "User Not Found",
                                    value = """
                        {
                            "error": "User not found"
                        }
                        """
                            )
                    )
            )
    })
    public ResponseEntity<Map<String, GetUserByIdResponse>> getUserById(
            @Parameter(description = "The unique identifier of the user", example = "f2cccd2f-5711-4356-a13a-f687dc983ce1")
            @PathVariable String userId) {

        return adminService.getUserById(userId);
    }

    @PutMapping("/user/{userId}")
    @Operation(
            tags = {"Admin"},
            summary = "Update a user's information",
            description = """
    Allows an administrator to update a user's details, including username, email, role, and email validation status.
    
    ## Notes:
    - All fields are optional, but at least one must be provided.
    - Username and email must be **unique**.
    - The new role must be a **valid value** from the `UserRole` enumeration.
    - The new values must be **different** from the current ones.
    """,
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Successful User Update",
                                    value = """
                            {
                                "message": "User with id: 60f7b1e2b3e2a81b5cb4c735 updated successfully"
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
                                            name = "Invalid Username",
                                            value = """
                                    {
                                        "errors": [
                                            "The provided username is invalid. It must be between 3 and 11 characters long and cannot contain spaces."
                                        ]
                                    }
                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Username Already Taken",
                                            value = """
                                    {
                                        "errors": [
                                            "The username 'newUser123' is already in use by another account."
                                        ]
                                    }
                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Email Already Used",
                                            value = """
                                    {
                                        "errors": [
                                            "The email address 'newemail@example.com' is already associated with another account."
                                        ]
                                    }
                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Invalid Role",
                                            value = """
                                    {
                                        "errors": [
                                            "Invalid role: SUPERUSER. Allowed values are: [USER, ADMIN, MODERATOR]"
                                        ]
                                    }
                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Email Verification Unchanged",
                                            value = """
                                    {
                                        "errors": [
                                            "The email verification status is already set to the provided value. No changes were made."
                                        ]
                                    }
                                    """
                                    ),
                                    @ExampleObject(
                                            name = "No Values Provided",
                                            value = """
                                    {
                                        "errors": [
                                            "No values provided for update. Please specify at least one field (email, username, isValidEmail or role)."
                                        ]
                                    }
                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Invalid Profile Image URL",
                                            value = """
                                    {
                                        "errors": [
                                            "You must provide a valid URL for the profileImageUrl field."
                                        ]
                                    }
                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Invalid Profile Image Extension",
                                            value = """
                                    {
                                        "errors": [
                                            "The profileImageUrl must have a valid extension (jpg, png, jpeg, svg, webp)."
                                        ]
                                    }
                                    """
                                    )

                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "User Not Found",
                                    value = """
                            {
                                "error": "User not found"
                            }
                            """
                            )
                    )
            )
    })
    public ResponseEntity<Map<String, String>> updateUser(
            @Parameter(description = "The ID of the user to update", example = "f2cccd2f-5711-4356-a13a-f687dc983ce1")
            @PathVariable String userId,

            @RequestBody(description = "User update request body", required = true, content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "User Update Request",
                            value = """
                        {
                            "username": "newUser123",
                            "email": "new.email@example.com",
                            "role": "ADMIN",
                            "isValidEmail": true,
                            "profileImageUrl": "https://example.png"
                        }
                        """
                    )
            )) @org.springframework.web.bind.annotation.RequestBody AdminUpdateUserRequest request) {

        return adminService.updateUser(userId, request);
    }

    @PostMapping(value = "/hotel", consumes = "multipart/form-data")
    @Operation(
            tags = {"Hotel"},
            summary = "Create a new hotel",
            description = """
        Allows an authenticated user to create a new hotel with a name, description, location, amenities, and pictures.
    
        ## Notes:
        - The hotel name must be between **3 and 25 characters** and **cannot contain spaces**.
        - The description must be **500 characters or fewer**.
        - The location fields (city, address, country, state, postal code, Google Maps URL) are **mandatory**.
        - Amenities must be from a **predefined list**.
        - At least **one valid image** must be uploaded.
        """,
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Hotel created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Successful Hotel Creation",
                                    value = """
                                {
                                    "message": "Hotel created successfully"
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
                                            name = "Invalid Hotel Name",
                                            value = """
                                        {
                                            "errors": [
                                                "The hotel name cannot be null or empty",
                                                "The hotel name must be between 3 and 25 characters long",
                                                "The hotel name cannot contain spaces"
                                            ]
                                        }
                                        """
                                    ),
                                    @ExampleObject(
                                            name = "Invalid Description",
                                            value = """
                                        {
                                            "errors": [
                                                "The hotel description must be less than or equal to 500 characters long"
                                            ]
                                        }
                                        """
                                    ),
                                    @ExampleObject(
                                            name = "Invalid Address",
                                            value = """
                                        {
                                            "errors": [
                                                "The address cannot be null or empty",
                                                "The address must be less than or equal to 100 characters long"
                                            ]
                                        }
                                        """
                                    ),
                                    @ExampleObject(
                                            name = "Invalid City",
                                            value = """
                                        {
                                            "errors": [
                                                "The city cannot be null or empty"
                                            ]
                                        }
                                        """
                                    ),
                                    @ExampleObject(
                                            name = "Invalid Country",
                                            value = """
                                        {
                                            "errors": [
                                                "The country cannot be null or empty"
                                            ]
                                        }
                                        """
                                    ),
                                    @ExampleObject(
                                            name = "Invalid State",
                                            value = """
                                        {
                                            "errors": [
                                                "The state cannot be null or empty",
                                                "The state cannot be longer than 50 characters"
                                            ]
                                        }
                                        """
                                    ),
                                    @ExampleObject(
                                            name = "Invalid Postal Code",
                                            value = """
                                        {
                                            "errors": [
                                                "The postal code cannot be null or empty",
                                                "The postal code cannot be longer than 10 characters"
                                            ]
                                        }
                                        """
                                    ),
                                    @ExampleObject(
                                            name = "Invalid Google Maps URL",
                                            value = """
                                        {
                                            "errors": [
                                                "The Google Maps URL cannot be null or empty",
                                                "Google Maps URL must start with 'https://'"
                                            ]
                                        }
                                        """
                                    ),
                                    @ExampleObject(
                                            name = "Invalid Amenities",
                                            value = """
                                        {
                                            "errors": [
                                                "The amenities list cannot be null or empty",
                                                "Invalid amenity: SPA_SERVICE"
                                            ]
                                        }
                                        """
                                    ),
                                    @ExampleObject(
                                            name = "No Pictures Provided",
                                            value = """
                                        {
                                            "error": "At least one valid picture is required"
                                        }
                                        """
                                    ),
                                    @ExampleObject(
                                            name = "Invalid Image Format",
                                            value = """
                                        {
                                            "error": "Invalid image format. Supported formats: JPG, PNG, WEBP"
                                        }
                                        """
                                    )
                            }
                    )
            )
    })
    public ResponseEntity<Map<String, String>> createHotel(
            @AuthenticationPrincipal User authenticatedUser,
            @RequestPart(value = "request") @Schema(description = "Hotel creation request in JSON format", example = """
            {
                "name": "LuxuryHotel",
                "description": "A five-star experience.",
                "city": "Paris",
                "address": "123 Rue de la Paix",
                "country": "France",
                "googleMapsUrl": "https://maps.google.com/?q=LuxuryHotel",
                "state": "Île-de-France",
                "postalCode": "75001",
                "amenities": ["POOL", "WIFI"]
            }
            """) @Valid String requestJson,
            @RequestPart(value = "pictures") List<MultipartFile> pictureList
    ) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        CreateHotelRequest request = objectMapper.readValue(requestJson, CreateHotelRequest.class);
        return adminService.createHotel(authenticatedUser, request, pictureList);
    }


}
