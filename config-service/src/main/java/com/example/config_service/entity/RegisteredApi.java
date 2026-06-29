package com.example.config_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "registered_apis")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisteredApi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "backend_url", nullable = false)
    private String backendUrl;

    @Column(name = "owner_id")
    private Long ownerId;

    @Column(name = "cache_ttl_seconds")
    private int cacheTtlSeconds;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}