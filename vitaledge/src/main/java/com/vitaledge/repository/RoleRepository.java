package com.vitaledge.repository;

import com.vitaledge.domain.user.Role;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, UUID> {

    Optional<Role> findByName(String name);

    List<Role> findByActiveTrue();
}