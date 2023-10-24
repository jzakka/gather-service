package com.example.gatherservice.repository;

import com.example.gatherservice.entity.GatherMemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GatherMemberRepository extends JpaRepository<GatherMemberEntity, Long> {
}
