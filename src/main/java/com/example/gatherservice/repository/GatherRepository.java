package com.example.gatherservice.repository;

import com.example.gatherservice.entity.GatherEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GatherRepository extends JpaRepository<GatherEntity, Long> {
}
