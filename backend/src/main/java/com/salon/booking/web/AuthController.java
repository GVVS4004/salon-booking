package com.salon.booking.web;

import com.salon.booking.config.SalonProperties;
import com.salon.booking.dto.AdminLoginRequest;
import com.salon.booking.dto.AuthConfigResponse;
import com.salon.booking.dto.AuthResponse;
import com.salon.booking.dto.GoogleLoginRequest;
import com.salon.booking.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final SalonProperties props;

    public AuthController(AuthService authService, SalonProperties props) {
        this.authService = authService;
        this.props = props;
    }

    /** Tells the frontend whether Google Sign-In is available and, if so, the client id. */
    @GetMapping("/config")
    public AuthConfigResponse config() {
        boolean enabled = authService.isGoogleEnabled();
        return new AuthConfigResponse(enabled, enabled ? props.getGoogle().getClientId() : null);
    }

    @PostMapping("/google")
    public AuthResponse googleLogin(@Valid @RequestBody GoogleLoginRequest req) {
        return authService.googleLogin(req.credential());
    }

    @PostMapping("/admin/login")
    public AuthResponse adminLogin(@Valid @RequestBody AdminLoginRequest req) {
        return authService.adminLogin(req.email(), req.password());
    }
}
