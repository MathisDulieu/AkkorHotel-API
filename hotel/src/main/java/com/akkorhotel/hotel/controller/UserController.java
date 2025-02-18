package com.akkorhotel.hotel.controller;

import com.akkorhotel.hotel.model.response.GetAuthenticatedUserResponse;
import com.akkorhotel.hotel.service.JwtAuthenticationService;
import com.akkorhotel.hotel.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
                                                "userRole": "USER"
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


}
