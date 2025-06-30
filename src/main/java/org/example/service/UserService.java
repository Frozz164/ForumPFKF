package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.AuthResponse;
import org.example.dto.LoginRequest;
import org.example.dto.RegisterRequest;
import org.example.model.User;
import org.example.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.mindrot.jbcrypt.BCrypt;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Регистрация нового пользователя с email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Попытка регистрации с существующим email: {}", request.getEmail());
            throw new RuntimeException("Пользователь с таким email уже существует");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(hashPassword(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        user.setCreatedAt(LocalDateTime.now());

        user = userRepository.save(user);
        log.info("Пользователь успешно зарегистрирован, ID: {}", user.getId());

        String token = jwtService.generateToken(user.getId(), user.getEmail());
        log.debug("JWT токен сгенерирован для пользователя: {}", user.getId());

        return new AuthResponse(token, user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("Попытка входа пользователя: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.error("Ошибка входа: пользователь с email {} не найден", request.getEmail());
                    return new RuntimeException("Неверный email или пароль");
                });

        if (!checkPassword(request.getPassword(), user.getPassword())) {
            log.warn("Ошибка входа: неверный пароль для пользователя {}", request.getEmail());
            throw new RuntimeException("Неверный email или пароль");
        }

        String token = jwtService.generateToken(user.getId(), user.getEmail());
        log.info("Пользователь {} успешно вошел в систему", user.getId());
        log.debug("JWT токен сгенерирован для пользователя: {}", user.getId());

        return new AuthResponse(token, user);
    }

    private String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    private boolean checkPassword(String password, String hashedPassword) {
        return BCrypt.checkpw(password, hashedPassword);
    }

    public User getUserById(Long id) {
        log.debug("Поиск пользователя по ID: {}", id);
        return userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Пользователь с ID {} не найден", id);
                    return new RuntimeException("Пользователь не найден");
                });
    }

    @Transactional(readOnly = true)
    public boolean isUserAdmin(Long userId) {
        log.debug("Проверка роли пользователя: {}", userId);
        return userRepository.findById(userId)
                .map(user -> "ADMIN".equals(user.getRole()))
                .orElse(false);
    }
} 