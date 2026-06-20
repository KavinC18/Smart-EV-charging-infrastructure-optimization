package com.chargeflow.controller;

import com.chargeflow.service.RoutingEngine;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RoutingEngine routingEngine;

    public RecommendationController(RoutingEngine routingEngine) {
        this.routingEngine = routingEngine;
    }

    @GetMapping
    public ResponseEntity<List<RoutingEngine.RecommendationResult>> getRecommendations(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam Long vehicleId) {
        
        List<RoutingEngine.RecommendationResult> recommendations = 
                routingEngine.recommendStations(lat, lng, vehicleId);
        
        return ResponseEntity.ok(recommendations);
    }
}
