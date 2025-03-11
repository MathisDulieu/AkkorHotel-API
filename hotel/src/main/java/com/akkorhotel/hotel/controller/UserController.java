package com.akkorhotel.hotel.controller;

import com.akkorhotel.hotel.model.User;
import com.akkorhotel.hotel.model.request.UpdateUserRequest;
import com.akkorhotel.hotel.model.response.GetAuthenticatedUserResponse;
import com.akkorhotel.hotel.service.JwtAuthenticationService;
import com.akkorhotel.hotel.service.UserService;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/private/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtAuthenticationService jwtAuthenticationService;

    @GetMapping
    @Operation(
            tags = {"User"},
            summary = "Retrieve authenticated user details",
            description = """
                Returns detailed information about the currently authenticated user based on the provided authentication token.
                
                - Requires a valid Bearer authentication token.
                - The response includes the username, email, and role of the authenticated user.
                
                If the token is missing or invalid, an `Unauthorized (401)` error is returned.
                If the authenticated user does not exist in the system, a `Not Found (404)` error is returned.
                """,
            security = {
                    @SecurityRequirement(name = "bearerAuth")
            }
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User details retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Successful Response",
                                    value = """
                                        {
                                            "informations": {
                                                "username": "alice123",
                                                "email": "alice@example.com",
                                                "userRole": "USER",
                                                "profileImageUrl": "https://example.png"
                                            }
                                        }
                                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Missing or invalid authentication token",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Unauthorized Response",
                                    value = """
                                        {
                                            "status": 401,
                                            "error": "Unauthorized",
                                            "message": "You must be authenticated to access this resource"
                                        }
                                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User Not Found - Authenticated user does not exist",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "User Not Found Response",
                                    value = """
                                        {
                                            "status": 404,
                                            "error": "Not Found",
                                            "message": "User not found"
                                        }
                                        """
                            )
                    )
            )
    })
    public ResponseEntity<Map<String, GetAuthenticatedUserResponse>> getAuthenticatedUser() {
        return userService.getAuthenticatedUser(jwtAuthenticationService.getAuthenticatedUser());
    }

    @PatchMapping
    @Operation(
            tags = {"User"},
            summary = "Update user details",
            description = "Allows an authenticated user to update their email, username, or password.",
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User details updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Success Example",
                                    value = """
                                    {
                                        "status": 200,
                                        "message": "User details updated successfully"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad request due to validation errors",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Validation Errors Example",
                                    value = """
                                    {
                                        "status": 400,
                                        "error": "Bad Request",
                                        "message": "Invalid username: Must be 3-11 characters and cannot contain spaces. | Email already taken: Please choose a different one."
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error when sending confirmation email",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Email Sending Error Example",
                                    value = """
                                    {
                                        "status": 500,
                                        "error": "Internal Server Error",
                                        "message": "Failed to send confirmation email. Please try again later."
                                    }
                                    """
                            )
                    )
            )
    })
    public ResponseEntity<Map<String, String>> updateUser(
            @RequestBody(
                    description = "Request body containing the fields to update.",
                    content = @Content(
                            examples = @ExampleObject(
                                    name = "Update User Request Example",
                                    value = """
                                    {
                                        "email": "new.email@example.com",
                                        "username": "newUsername",
                                        "oldPassword": "oldPass123",
                                        "newPassword": "newPass456"
                                    }
                                    """
                            )
                    )
            ) @org.springframework.web.bind.annotation.RequestBody UpdateUserRequest request,
            @AuthenticationPrincipal User authenticatedUser) {
        return userService.updateUser(request, authenticatedUser);
    }

    @DeleteMapping
    @Operation(
            tags = {"User"},
            summary = "Delete user account",
            description = "Allows an authenticated user to permanently delete their account.",
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User deleted successfully",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Success Example",
                                    value = """
                                    {
                                        "status": 200,
                                        "message": "User deleted successfully"
                                    }
                                    """
                            )
                    )
            )
    })
    public ResponseEntity<Map<String, String>> deleteUser(@AuthenticationPrincipal User authenticatedUser) {
        return userService.deleteUser(authenticatedUser.getId());
    }

    @PostMapping(value = "/profile-image", consumes = "multipart/form-data")
    @Operation(
            tags = {"User"},
            summary = "Update user profile image",
            description = "Allows an authenticated user to update their profile image.",
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Profile image uploaded successfully",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Success Example",
                                    value = """
                                {
                                    "message": "Profile image uploaded successfully"
                                }
                                """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad request due to validation errors or unsupported image format",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Validation Errors Example",
                                    value = """
                                {
                                    "error": "No file uploaded"
                                }
                                """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad request due to unsupported image format",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Unsupported Image Format Example",
                                    value = """
                                {
                                    "error": "Unsupported image format"
                                }
                                """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error during image upload",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Image Upload Error Example",
                                    value = """
                                {
                                    "error": "Failed to upload the image"
                                }
                                """
                            )
                    )
            )
    })
    public ResponseEntity<Map<String, String>> uploadUserProfileImage(
            @AuthenticationPrincipal User authenticatedUser,
            @RequestParam("file") MultipartFile file) throws IOException {
        return userService.uploadUserProfileImage(authenticatedUser, file);
    }


}
