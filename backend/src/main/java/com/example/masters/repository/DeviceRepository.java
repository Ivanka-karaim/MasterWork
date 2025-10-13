package com.example.masters.repository;

import com.example.masters.entity.Device;

import com.example.masters.entity.enums.Type;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeviceRepository extends JpaRepository<Device, UUID> {
    Optional<Device> findByTitle(String title);
    Optional<Device> findByInventoryNumber(String inventoryNumber);
    List<Device> findByType(Type type);

    @Query("SELECT d FROM Device d WHERE d.inventoryNumber = :inv")
    @EntityGraph(attributePaths = {}) // нічого не вантажимо додатково
    Optional<Device> findByInventoryNumberWithoutImage(@Param("inv") String inv);



}