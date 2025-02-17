package com.akkorhotel.hotel.controller;

import com.akkorhotel.hotel.model.User;
import com.akkorhotel.hotel.model.request.ConfirmEmailRequest;
import com.akkorhotel.hotel.model.request.CreateUserRequest;
import com.akkorhotel.hotel.model.request.LoginRequest;
import com.akkorhotel.hotel.model.request.ResendConfirmationEmailRequest;
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

import java.util.Map;

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
    public ResponseEntity<Map<String, String>> register(
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

    @PostMapping("/login")
    @Operation(
            tags = {"Authentication"},
            summary = "User login",
            description = """
        Authenticates a user using their email and password.
        Returns an access token upon successful login.
    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Successful authentication, access token returned",
                    content = @Content(
                            mediaType = "text/plain",
                            examples = @ExampleObject(
                                    name = "Access Token Example",
                                    value = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid credentials or bad request (e.g., incorrect password or email)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Invalid Login Example",
                                    value = """
                    {
                        "status": 400,
                        "error": "Bad Request",
                        "message": "Invalid email or password"
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
                                    name = "User Not Found Example",
                                    value = """
                    {
                        "status": 404,
                        "error": "Not Found",
                        "message": "User not found"
                    }
                """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Email not validated yet (account not active)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Email Not Validated Example",
                                    value = """
                    {
                        "status": 409,
                        "error": "Conflict",
                        "message": "User's email is not validated yet"
                    }
                """
                            )
                    )
            )
    })
    public ResponseEntity<Map<String, String>> login(
            @RequestBody(
                    description = """
            User credentials for login.
            - **email**: The email of the user.
            - **password**: The user's password.
        """,
                    content = @Content(
                            examples = {
                                    @ExampleObject(
                                            name = "Alice Login Example",
                                            value = """
                        {
                            "email": "alice@example.com",
                            "password": "AliceStrongP@ss1!"
                        }
                    """
                                    ),
                                    @ExampleObject(
                                            name = "Bob Login Example",
                                            value = """
                        {
                            "email": "bob78@example.com",
                            "password": "BobbySecure2$"
                        }
                    """
                                    ),
                                    @ExampleObject(
                                            name = "Charlie Login Example",
                                            value = """
                        {
                            "email": "charlie@example.org",
                            "password": "ChazP@ssw0rd3!"
                        }
                    """
                                    ),
                                    @ExampleObject(
                                            name = "Diana Login Example",
                                            value = """
                        {
                            "email": "diana.user@example.com",
                            "password": "DIAStrong5@ss"
                        }
                    """
                                    ),
                                    @ExampleObject(
                                            name = "Eve Login Example",
                                            value = """
                        {
                            "email": "eve@example.net",
                            "password": "EveStrong5@ss"
                        }
                    """
                                    )
                            }
                    )
            )
            @org.springframework.web.bind.annotation.RequestBody LoginRequest loginRequest) {
        return authenticationService.login(loginRequest);
    }

    @PostMapping("/confirm-email")
    @Operation(
            tags = {"Authentication"},
            summary = "Confirm user email",
            description = "Confirms the user's email address using the provided token."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Email successfully validated",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Email Confirmation Success Example",
                                    value = """
                    {
                        "status": 200,
                        "message": "Email successfully validated"
                    }
                """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid or expired token",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Invalid Token Example",
                                    value = """
                    {
                        "status": 400,
                        "error": "Bad Request",
                        "message": "Invalid or expired token"
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
                                    name = "User Not Found Example",
                                    value = """
                    {
                        "status": 404,
                        "error": "Not Found",
                        "message": "User not found"
                    }
                """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Email already validated",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Email Already Validated Example",
                                    value = """
                    {
                        "status": 409,
                        "error": "Conflict",
                        "message": "Email already validated"
                    }
                """
                            )
                    )
            )
    })
    public ResponseEntity<Map<String, String>> confirmEmail(
            @RequestBody(
                    description = "Request body containing the token.",
                    content = @Content(
                            examples = @ExampleObject(
                                    name = "Confirm Email Request Example",
                                    value = """
                                {
                                    "token": "eyTOKEN"
                                }
                                """
                            )
                    )
            )
            @org.springframework.web.bind.annotation.RequestBody ConfirmEmailRequest request) {
        return authenticationService.confirmEmail(request.getToken());
    }

    @PostMapping("/resend-confirmation-email")
    @Operation(
            tags = {"Authentication"},
            summary = "Resend confirmation email",
            description = "Allows a user to request a new confirmation email if their account email is not yet validated."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Confirmation email sent successfully",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Success Example",
                                    value = """
                                {
                                    "status": 200,
                                    "message": "Confirmation email sent successfully."
                                }
                                """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Email already validated or bad request",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Bad Request Example",
                                    value = """
                                {
                                    "status": 400,
                                    "error": "Bad Request",
                                    "message": "Email already validated."
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
                                    name = "User Not Found Example",
                                    value = """
                                {
                                    "status": 404,
                                    "error": "Not Found",
                                    "message": "User not found."
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
    public ResponseEntity<Map<String, String>> resendConfirmationEmail(
            @RequestBody(
                    description = "Request body containing the email of the user.",
                    content = @Content(
                            examples = @ExampleObject(
                                    name = "Resend Email Request Example",
                                    value = """
                                {
                                    "email": "user@example.com"
                                }
                                """
                            )
                    )
            ) @org.springframework.web.bind.annotation.RequestBody ResendConfirmationEmailRequest request) {
        return authenticationService.resendConfirmationEmail(request.getEmail());
    }

}
