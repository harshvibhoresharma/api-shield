package com.example.config_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "rate_limit_rules")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RateLimitRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "api_id")
    private Long apiId;

    @Column(nullable = false)
    private String tier;

    @Column(nullable = false)
    private String endpoint;

    @Column(nullable = false)
    private String algorithm;

    @Column(name = "limit_value", nullable = false)
    private int limitValue;

    @Column(name = "window_seconds", nullable = false)
    private int windowSeconds;
}