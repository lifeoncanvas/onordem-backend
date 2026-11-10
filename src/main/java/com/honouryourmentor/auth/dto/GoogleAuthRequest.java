package com.honouryourmentor.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Google OAuth2 sign-in request")
public class GoogleAuthRequest {

    @Schema(description = "Google ID Token received from Google Identity Services", example = "eyJhbGciOiJSUzI1NiIs...")
    private String idToken;

    @Schema(description = "User's Google email address", example = "info@onodem.com")
    private String email;

    @Schema(description = "User's full name from Google profile", example = "Onodem")
    private String name;

    @Schema(description = "Avatar image URL from Google profile")
    private String avatarUrl;
}
