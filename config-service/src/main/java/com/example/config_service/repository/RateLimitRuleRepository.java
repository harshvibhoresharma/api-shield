package com.example.config_service.repository;

import com.example.config_service.entity.RateLimitRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RateLimitRuleRepository extends JpaRepository<RateLimitRule, Long> {
    Optional<RateLimitRule> findByApiIdAndTierAndEndpoint(Long apiId, String tier, String endpoint);
}