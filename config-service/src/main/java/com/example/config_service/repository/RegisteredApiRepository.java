package com.example.config_service.repository;

import com.example.config_service.entity.RegisteredApi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegisteredApiRepository extends JpaRepository<RegisteredApi, Long> {
    List<RegisteredApi> findByOwnerId(Long ownerId);
    Optional<RegisteredApi> findByOwnerIdAndName(Long ownerId, String name);
}