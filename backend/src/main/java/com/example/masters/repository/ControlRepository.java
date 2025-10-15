package com.example.masters.repository;


import com.example.masters.entity.Control;
import com.example.masters.entity.Status;
import com.example.masters.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ControlRepository extends JpaRepository<Control, UUID> {
    List<Control> findAllByUser(User user);
    List<Control> findAllByDeviceIdAndDateTimeBetweenOrderByDateTimeDesc(UUID deviceId,
                                                       Timestamp from,
                                                       Timestamp to);
    List<Control> findAllByDeviceId(UUID deviceId);

    Optional<Control> findFirstByDeviceIdOrderByDateTimeDesc(UUID deviceId);

}
