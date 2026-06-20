package com.chargeflow.repository;

import com.chargeflow.entity.ChargingHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChargingHistoryRepository extends JpaRepository<ChargingHistory, Long> {
    List<ChargingHistory> findByUserId(Long userId);
    List<ChargingHistory> findByStationId(Long stationId);
}
