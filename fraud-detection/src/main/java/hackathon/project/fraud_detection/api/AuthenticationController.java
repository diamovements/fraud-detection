package hackathon.project.fraud_detection.api;

import hackathon.project.fraud_detection.api.dto.request.SignInRequest;
import hackathon.project.fraud_detection.api.dto.request.SignUpRequest;
import hackathon.project.fraud_detection.api.dto.response.AuthenticationResponse;
import hackathon.project.fraud_detection.security.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthenticationController {
    private final AuthenticationService authenticationService;
    private final static String REFRESH_TOKEN = "refreshToken";

    @PostMapping("/signup")
    public AuthenticationResponse signUp(@RequestBody SignUpRequest request) {
        return authenticationService.signUp(request);
    }

    @PostMapping("/signin")
    public AuthenticationResponse signIn(@RequestBody SignInRequest request) {
        return authenticationService.signIn(request);
    }

    @PostMapping("/refresh")
    public AuthenticationResponse refreshAccessToken(@RequestHeader(REFRESH_TOKEN) String refreshToken) {
        return authenticationService.refreshAccessToken(refreshToken);
    }
}
