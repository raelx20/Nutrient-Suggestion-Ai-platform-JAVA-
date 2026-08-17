package com.vitaledge.repository;

import com.vitaledge.domain.conversation.Counsellor;
import com.vitaledge.domain.conversation.UserCounsellorAssignment;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserCounsellorAssignmentRepository extends JpaRepository<UserCounsellorAssignment, UUID> {

    Optional<UserCounsellorAssignment> findFirstByUserIdAndActiveTrueOrderByAssignedAtDesc(UUID userId);

    Optional<Counsellor> findCounsellorByUserIdAndActiveTrue(UUID userId);
}