package hackathon.project.fraud_detection.security.service;

import hackathon.project.fraud_detection.api.dto.request.SignInRequest;
import hackathon.project.fraud_detection.api.dto.request.SignUpRequest;
import hackathon.project.fraud_detection.api.dto.response.AuthenticationResponse;
import hackathon.project.fraud_detection.security.dto.Role;
import hackathon.project.fraud_detection.security.exception.ExpiredTokenException;
import hackathon.project.fraud_detection.storage.entity.UserEntity;
import hackathon.project.fraud_detection.storage.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AuthenticationResponse signUp(SignUpRequest request) {
        UserEntity user = UserEntity.builder()
                .name(request.name())
                .surname(request.surname())
                .login(request.login())
                .role(Role.ROLE_ADMIN)
                .password(passwordEncoder.encode(request.password()))
                .telegramId(request.telegramId())
                .build();
        user = userRepository.save(user);
        log.info("User {} saved, with role {}", user.getLogin(), user.getRole());
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        return new AuthenticationResponse(accessToken, refreshToken);
    }

    @Transactional
    public AuthenticationResponse signIn(SignInRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.login(), request.password()));
        UserEntity user = userRepository.findByLogin(request.login())
                .orElseThrow(() -> new IllegalArgumentException("Incorrect login or password"));
        log.info("User role: {}", user.getRole());
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return new AuthenticationResponse(accessToken, refreshToken);
    }

    @Transactional
    public AuthenticationResponse refreshAccessToken(String refreshToken) {
        log.info("Refreshing access token");
        if (jwtService.isTokenExpired(refreshToken)) {
            throw new ExpiredTokenException("Refresh token expired");
        }

        String login = jwtService.extractUsername(refreshToken);
        UserEntity user = userRepository.findByLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        String newAccessToken = jwtService.generateAccessToken(user);
        log.info("New access token given");
        return new AuthenticationResponse(newAccessToken, refreshToken);
    }
}
