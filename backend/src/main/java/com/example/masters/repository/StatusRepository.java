package com.example.masters.repository;

import com.example.masters.entity.Control;
import com.example.masters.entity.Status;
import com.example.masters.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StatusRepository extends JpaRepository<Status, UUID> {
    Optional<Status> findFirstByDeviceInventoryNumberOrderByDateTimeDesc(String deviceInventoryNumber);

}