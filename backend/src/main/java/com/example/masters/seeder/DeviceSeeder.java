package com.example.masters.seeder;
import com.example.masters.entity.Device;
import com.example.masters.repository.DeviceRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.masters.entity.enums.Type;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Component
public class DeviceSeeder implements CommandLineRunner {

    @Autowired
    private DeviceRepository deviceRepository;

    @Override
    public void run(String... args) throws Exception {
        try {
            if (deviceRepository.count() < 2) {
                deviceRepository.save(createDevice(
                        "Лампочка",
                        "Для освітлення кімнати",
                        "INV-001",
                        Type.LIGHTING,
                        "src/main/resources/images/ic_lamp.png"
                ));
                deviceRepository.save(createDevice(
                        "Датчик температури",
                        "Для вимірювання температури в лабораторії",
                        "TEMP-001",
                        Type.SENSOR,
                        "src/main/resources/images/ic_temperature.png"
                ));
                deviceRepository.save(createDevice(
                        "Кондиціонер",
                        "Для охолодження приміщення",
                        "INV-002",
                        Type.CLIMATE,
                        "src/main/resources/images/ic_air_conditioning.png"
                ));
                deviceRepository.save(createDevice(
                        "Опалення",
                        "Для нагрівння приміщення",
                        "INV-003",
                        Type.CLIMATE,
                        "src/main/resources/images/ic_heating.png"
                ));

            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Device createDevice(String title, String description, String inventoryNumber, Type type, String imagePath) throws IOException {
        byte[] imageBytes = Files.readAllBytes(Paths.get(imagePath));
        return Device.builder()
                .title(title)
                .description(description)
                .inventoryNumber(inventoryNumber)
                .type(type)
                .image(imageBytes)
                .build();
    }
}
