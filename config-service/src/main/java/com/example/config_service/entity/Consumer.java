package com.example.config_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "consumers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Consumer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "api_key", unique = true, nullable = false)
    private String apiKey;

    @Column(nullable = false)
    private String tier;

    @Column(name = "owner_email", nullable = false)
    private String ownerEmail;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}