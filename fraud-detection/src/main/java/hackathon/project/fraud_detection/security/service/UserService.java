package hackathon.project.fraud_detection.security.service;

import hackathon.project.fraud_detection.security.dto.User;
import hackathon.project.fraud_detection.storage.entity.UserEntity;
import hackathon.project.fraud_detection.storage.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements UserDetailsService{
    private final UserRepository userRepository;

    @Transactional
    public UserEntity save(UserEntity newUser) {
        return userRepository.save(newUser);
    }

    @Transactional
    public User getUser(String login) {
        UserEntity user = userRepository.findByLogin(login).orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
        return new User(user.getName(), user.getSurname(), user.getLogin());
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        return userRepository.findByLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
    @Transactional
    public UUID getUserId(String login) {
        return userRepository.findByLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException("User not found")).getUserId();
    }
    @Transactional
    public List<UserEntity> getAll() {
        return userRepository.findAll();
    }
}
