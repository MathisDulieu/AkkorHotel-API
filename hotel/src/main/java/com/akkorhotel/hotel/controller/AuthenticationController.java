package com.akkorhotel.hotel.controller;

import com.akkorhotel.hotel.model.User;
import com.akkorhotel.hotel.model.request.CreateUserRequest;
import com.akkorhotel.hotel.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    @Operation(
            tags = {"Authentication"},
            summary = "Register a new user",
            description = """
    Register a new user in the system.
    The username must be between 3 and 8 characters long,
    the email must be valid, and the password must meet security requirements.

    ## Password Requirements:
    - Must contain at least 8 characters.
    - Must include at least one lowercase letter.
    - Must include at least one uppercase letter.
    - Must include at least one numeric digit.
    - Must include at least one special character from the set: !@#$%^&*()_+-={}';:",.<>?|`~
"""
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User registered successfully and confirmation email sent",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Registration Success Example",
                                    value = """
                                {
                                    "status": 200,
                                    "message": "Registration successful. A confirmation email has been sent."
                                }
                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request due to bad data (e.g., invalid email or weak password)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Invalid Request Example",
                                    value = """
                                {
                                    "status": 400,
                                    "error": "Bad Request",
                                    "message": "Invalid email format or weak password"
                                }
                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Failed to send confirmation email",
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
    public ResponseEntity<String> register(
            @RequestBody(
                    description = "User object containing the registration details.",
                    content = @Content(
                            examples = {
                                    @ExampleObject(
                                            name = "Alice Registration Example",
                                            value = """
                                        {
                                            "username": "alice123",
                                            "email": "alice@example.com",
                                            "password": "AliceStrongP@ss1!"
                                        }
                                        """
                                    ),
                                    @ExampleObject(
                                            name = "Bob Registration Example",
                                            value = """
                                        {
                                            "username": "bobby78",
                                            "email": "bob78@example.com",
                                            "password": "BobbySecure2$"
                                        }
                                        """
                                    ),
                                    @ExampleObject(
                                            name = "Charlie Registration Example",
                                            value = """
                                        {
                                            "username": "chaz",
                                            "email": "charlie@example.org",
                                            "password": "ChazP@ssw0rd3!"
                                        }
                                        """
                                    ),
                                    @ExampleObject(
                                            name = "Diana Registration Example",
                                            value = """
                                        {
                                            "username": "diana",
                                            "email": "diana.user@example.com",
                                            "password": "DIAStrong5@ss"
                                        }
                                        """
                                    ),
                                    @ExampleObject(
                                            name = "Eve Registration Example",
                                            value = """
                                        {
                                            "username": "eve12345",
                                            "email": "eve@example.net",
                                            "password": "EveStrong5@ss"
                                        }
                                        """
                                    )
                            }
                    )
            )
            @org.springframework.web.bind.annotation.RequestBody CreateUserRequest user) {
        User userToSave = User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .email(user.getEmail())
                .build();

        return authenticationService.register(userToSave);
    }

}
