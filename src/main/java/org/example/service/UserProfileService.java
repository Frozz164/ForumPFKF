package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.dto.UserProfileRequest;
import org.example.dto.UserProfileResponse;
import org.example.model.User;
import org.example.repository.DonationRepository;
import org.example.repository.RecurringPaymentRepository;
import org.example.repository.UserRepository;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserProfileService {
    private final UserRepository userRepository;
    private final DonationRepository donationRepository;
    private final RecurringPaymentRepository recurringPaymentRepository;

    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserProfileResponse response = new UserProfileResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setPhone(user.getPhone());
        response.setCreatedAt(user.getCreatedAt());
        response.setLastLogin(user.getLastLogin());
        response.setEmailVerified(user.isEmailVerified());

        // Добавляем статистику
        response.setTotalDonations(donationRepository.countByUserId(userId));
        response.setTotalDonationAmount(donationRepository.sumAmountByUserId(userId));
        response.setActiveRecurringPayments(recurringPaymentRepository.countByUserIdAndIsActiveTrue(userId));

        return response;
    }

    @Transactional
    public UserProfileResponse updateUserProfile(Long userId, UserProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("Email already in use");
            }
            user.setEmail(request.getEmail());
            user.setEmailVerified(false);
        }

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }

        user = userRepository.save(user);
        return getUserProfile(user.getId());
    }

    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!BCrypt.checkpw(currentPassword, user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        user.setPassword(BCrypt.hashpw(newPassword, BCrypt.gensalt()));
        userRepository.save(user);
    }
} 