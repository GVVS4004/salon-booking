package com.salon.booking.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.salon.booking.config.SalonProperties;
import com.salon.booking.domain.Customer;
import com.salon.booking.dto.AuthResponse;
import com.salon.booking.exception.UnauthorizedException;
import com.salon.booking.security.GoogleTokenVerifier;
import com.salon.booking.security.JwtService;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final GoogleTokenVerifier googleVerifier;
    private final CustomerService customerService;
    private final JwtService jwtService;
    private final SalonProperties props;

    public AuthService(GoogleTokenVerifier googleVerifier,
                       CustomerService customerService,
                       JwtService jwtService,
                       SalonProperties props) {
        this.googleVerifier = googleVerifier;
        this.customerService = customerService;
        this.jwtService = jwtService;
        this.props = props;
    }

    /** Exchanges a Google ID token for an app session token, creating the customer if needed. */
    public AuthResponse googleLogin(String credential) {
        GoogleIdToken.Payload payload = googleVerifier.verify(credential);
        String sub = payload.getSubject();
        String email = payload.getEmail();
        String name = (String) payload.get("name");
        if (Boolean.FALSE.equals(payload.getEmailVerified())) {
            throw new UnauthorizedException("Your Google email is not verified.");
        }

        Customer c = customerService.findOrCreateFromGoogle(sub, email, name);
        String token = jwtService.issue(c.getEmail(), "CUSTOMER", c.getId(), c.getName(), c.getEmail());
        return new AuthResponse(token, "CUSTOMER", c.getName(), c.getEmail(), Mapper.toDto(c));
    }

    public AuthResponse adminLogin(String email, String password) {
        SalonProperties.Auth auth = props.getAuth();
        boolean ok = auth.getAdminEmail() != null
                && auth.getAdminEmail().equalsIgnoreCase(email)
                && auth.getAdminPassword() != null
                && auth.getAdminPassword().equals(password);
        if (!ok) {
            throw new UnauthorizedException("Invalid admin credentials.");
        }
        String token = jwtService.issue(email, "ADMIN", null, "Administrator", email);
        return new AuthResponse(token, "ADMIN", "Administrator", email, null);
    }

    public boolean isGoogleEnabled() {
        return googleVerifier.isEnabled();
    }
}
