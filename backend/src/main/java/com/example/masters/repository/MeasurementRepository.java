package com.example.masters.repository;

import com.example.masters.entity.Measurement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface MeasurementRepository extends JpaRepository<Measurement, UUID>, JpaSpecificationExecutor<Measurement> {

    @Query(value =
            "SELECT AVG(value) FROM (" +
                    "  SELECT value FROM measurements " +
                    "  WHERE device_inventory_number = :deviceNumber " +
                    "  AND parameter_name = :parameterName " +
                    "  ORDER BY date_time DESC " +
                    "  LIMIT 10" +
                    ") AS last_ten",
            nativeQuery = true)
    Double getAverageOfLastTen(
            @Param("deviceNumber") String deviceNumber,
            @Param("parameterName") String parameterName
    );

    List<Measurement> findByDeviceInventoryNumber(String deviceInventoryNumber);

}
