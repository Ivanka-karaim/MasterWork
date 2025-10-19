package com.example.masters.dto.device;

import com.example.masters.entity.enums.Type;
import jakarta.persistence.Basic;
import jakarta.persistence.FetchType;
import jakarta.persistence.Lob;
import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
public class DeviceRequest {
    private String title;
    private String description;
    private String inventoryNumber;
    private String type;
    private MultipartFile image;
}
