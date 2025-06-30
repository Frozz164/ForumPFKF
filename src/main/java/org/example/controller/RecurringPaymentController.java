package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.model.RecurringPayment;
import org.example.model.User;
import org.example.service.RecurringPaymentService;
import org.example.service.JwtService;
import org.example.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/recurring-payments")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class RecurringPaymentController {
    private final RecurringPaymentService recurringPaymentService;
    private final JwtService jwtService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<RecurringPayment> createRecurringPayment(
            @RequestHeader("Authorization") String token,
            @RequestParam Long fundraisingId,
            @RequestParam BigDecimal amount,
            @RequestParam Integer paymentDay) {
        String jwt = token.substring(7);
        Long userId = jwtService.extractUserId(jwt);
        User user = userService.getUserById(userId);
        RecurringPayment payment = recurringPaymentService.createRecurringPayment(
                user, fundraisingId, amount, paymentDay);
        return ResponseEntity.ok(payment);
    }

    @DeleteMapping("/{paymentId}")
    public ResponseEntity<Void> cancelRecurringPayment(
            @RequestHeader("Authorization") String token,
            @PathVariable Long paymentId) {
        String jwt = token.substring(7);
        Long userId = jwtService.extractUserId(jwt);
        recurringPaymentService.cancelRecurringPayment(paymentId, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user")
    public ResponseEntity<List<RecurringPayment>> getUserRecurringPayments(
            @RequestHeader("Authorization") String token) {
        String jwt = token.substring(7);
        Long userId = jwtService.extractUserId(jwt);
        List<RecurringPayment> payments = recurringPaymentService.getUserRecurringPayments(userId);
        return ResponseEntity.ok(payments);
    }
} 