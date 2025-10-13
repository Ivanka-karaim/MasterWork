package com.example.masters.repository;


import com.example.masters.entity.User;
import com.example.masters.entity.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    boolean existsByEmail(String email);

    List<User> findByRole(Role role);

    Optional<User> findByEmail(String email);

}
