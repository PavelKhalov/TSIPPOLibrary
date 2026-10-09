package ru.khalov.tsippolibrary.controller;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.Access;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.web.OffsetScrollPositionHandlerMethodArgumentResolver;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.khalov.tsippolibrary.dto.request.LoginRequest;
import ru.khalov.tsippolibrary.dto.request.RefreshRequest;
import ru.khalov.tsippolibrary.dto.request.RegisterRequest;
import ru.khalov.tsippolibrary.dto.response.AccessTokenResponse;
import ru.khalov.tsippolibrary.dto.response.AuthResponse;
import ru.khalov.tsippolibrary.service.AuthService;

@Tag(name = "Authentication")
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Register successful"),
            @ApiResponse(responseCode = "409", description = "Username already exists"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/register")
    public ResponseEntity<AccessTokenResponse> register(
            @RequestBody
            @Valid
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Username and password for registration")
            RegisterRequest request,
            HttpServletResponse response
    ){
        AuthResponse tokens = authService.register(request);
        setRefreshCookie(response, tokens.refreshToken());
        return ResponseEntity.status(HttpStatus.OK).body(new AccessTokenResponse(tokens.accessToken()));
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Authentication successful"),
            @ApiResponse(responseCode = "409", description = "Bad login or password"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/login")
    public ResponseEntity<AccessTokenResponse> login(
            @RequestBody
            @Valid
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Username and password for login")
            LoginRequest request,
            HttpServletResponse response
    ){
        AuthResponse tokens = authService.login(request);
        setRefreshCookie(response, tokens.refreshToken());

        return ResponseEntity.status(HttpStatus.OK).body(new AccessTokenResponse(tokens.accessToken()));
    }


    @PostMapping("/refresh")
    public ResponseEntity<AccessTokenResponse> refresh(@CookieValue("refreshToken") String refreshToken, HttpServletResponse response){
        AuthResponse tokens = authService.refresh(refreshToken);

        setRefreshCookie(response, tokens.refreshToken());

         return ResponseEntity.status(HttpStatus.OK).body(new AccessTokenResponse(tokens.accessToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@CookieValue (value = "refreshToken", required = true) String refreshToken,
                                         HttpServletResponse response){
        if(refreshToken != null){
            authService.logout(refreshToken);
        }

        clearRefreshToken(response);

        return ResponseEntity.ok("Logout successful");
    }

    private void clearRefreshToken(HttpServletResponse response){
        Cookie cookie = new Cookie("refreshToken", "");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/api/auth");
        cookie.setMaxAge(0);

        response.addCookie(cookie);
    }

    private void setRefreshCookie(HttpServletResponse response, String token){
        Cookie  cookie = new Cookie("refreshToken", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/api/auth");
        cookie.setMaxAge(7*24*3600);
        response.addCookie(cookie);
    }
}
