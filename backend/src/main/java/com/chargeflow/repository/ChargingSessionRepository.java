package com.chargeflow.repository;

import com.chargeflow.entity.ChargingSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChargingSessionRepository extends JpaRepository<ChargingSession, Long> {
    List<ChargingSession> findByUserId(Long userId);
    List<ChargingSession> findByStationId(Long stationId);
    List<ChargingSession> findByStatus(String status);
    List<ChargingSession> findByUserIdAndStatus(Long userId, String status);
}
