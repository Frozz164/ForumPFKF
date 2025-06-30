package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.model.User;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private Long userId;
    private String email;
    private String firstName;
    private String lastName;

    public AuthResponse(String token, User user) {
        this.token = token;
        this.userId = user.getId();
        this.email = user.getEmail();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
    }
} 