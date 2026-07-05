package com.salon.booking.security;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.salon.booking.config.SalonProperties;
import com.salon.booking.exception.BadRequestException;
import com.salon.booking.exception.UnauthorizedException;
import java.util.Collections;
import org.springframework.stereotype.Component;

/** Verifies Google Sign-In ID tokens against Google's public keys and our client id. */
@Component
public class GoogleTokenVerifier {

    private final GoogleIdTokenVerifier verifier;
    private final boolean enabled;

    public GoogleTokenVerifier(SalonProperties props) {
        this.enabled = props.getGoogle().isEnabled();
        this.verifier = enabled
                ? new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
                        .setAudience(Collections.singletonList(props.getGoogle().getClientId()))
                        .build()
                : null;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public GoogleIdToken.Payload verify(String idTokenString) {
        if (!enabled) {
            throw new BadRequestException("Google Sign-In is not configured on the server.");
        }
        try {
            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) {
                throw new UnauthorizedException("Invalid Google token.");
            }
            return idToken.getPayload();
        } catch (UnauthorizedException e) {
            throw e;
        } catch (Exception e) {
            throw new UnauthorizedException("Could not verify Google token.");
        }
    }
}
