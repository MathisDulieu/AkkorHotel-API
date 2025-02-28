package com.akkorhotel.hotel.controller;


import com.akkorhotel.hotel.model.request.AdminUpdateUserRequest;
import com.akkorhotel.hotel.model.response.GetAllUsersResponse;
import com.akkorhotel.hotel.model.response.GetUserByIdResponse;
import com.akkorhotel.hotel.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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



}
