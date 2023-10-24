package com.example.gatherservice.repository;

import com.example.gatherservice.entity.GatherEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GatherRepository extends JpaRepository<GatherEntity, Long> {
    Optional<GatherEntity> findByGatherId(String gatherId);
}
