package com.chargeflow.controller;

import com.chargeflow.entity.User;
import com.chargeflow.exception.ResourceNotFoundException;
import com.chargeflow.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository userRepository;
    
    // Simple state for system settings
    private double routingOptimizationFactor = 1.0; // 1.0 = balanced, 0.5 = prioritize travel, 1.5 = prioritize wait time
    private boolean autoLoadBalancingEnabled = true;

    public AdminController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if ("admin".equals(user.getUsername())) {
            return ResponseEntity.badRequest().body("Cannot delete default administrator account.");
        }

        userRepository.delete(user);
        return ResponseEntity.ok().body("User deleted successfully.");
    }

    @GetMapping("/settings")
    public ResponseEntity<Map<String, Object>> getSettings() {
        Map<String, Object> settings = new HashMap<>();
        settings.put("routingOptimizationFactor", routingOptimizationFactor);
        settings.put("autoLoadBalancingEnabled", autoLoadBalancingEnabled);
        settings.put("databaseStatus", "CONNECTED");
        settings.put("systemCpuUsagePercentage", Math.round(15.0 + Math.random() * 20.0));
        settings.put("systemMemoryUsagePercentage", 42.0);
        return ResponseEntity.ok(settings);
    }

    @PostMapping("/settings")
    public ResponseEntity<Map<String, Object>> updateSettings(@RequestBody Map<String, Object> payload) {
        if (payload.containsKey("routingOptimizationFactor")) {
            this.routingOptimizationFactor = Double.parseDouble(payload.get("routingOptimizationFactor").toString());
        }
        if (payload.containsKey("autoLoadBalancingEnabled")) {
            this.autoLoadBalancingEnabled = Boolean.parseBoolean(payload.get("autoLoadBalancingEnabled").toString());
        }
        return getSettings();
    }
}
