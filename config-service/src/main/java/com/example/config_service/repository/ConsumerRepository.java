package com.example.config_service.repository;

import com.example.config_service.entity.Consumer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// repository/ConsumerRepository.java
@Repository
public interface ConsumerRepository extends JpaRepository<Consumer, Long> {
    Optional<Consumer> findByApiKey(String apiKey);
}