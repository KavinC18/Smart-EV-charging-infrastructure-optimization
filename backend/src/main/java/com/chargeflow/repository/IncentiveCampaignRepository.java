package com.chargeflow.repository;

import com.chargeflow.entity.IncentiveCampaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface IncentiveCampaignRepository extends JpaRepository<IncentiveCampaign, Long> {
    List<IncentiveCampaign> findByActiveTrue();
    List<IncentiveCampaign> findBySourceStationIdAndActiveTrue(Long sourceStationId);
}
