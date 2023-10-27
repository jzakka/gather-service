package com.example.gatherservice.repository;

import com.example.gatherservice.entity.ConfirmedGatherEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConfirmedGatherRepository extends JpaRepository<ConfirmedGatherEntity, Long> {
}
