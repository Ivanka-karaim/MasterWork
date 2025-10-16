package com.example.masters.seeder;
import com.example.masters.entity.Device;
import com.example.masters.entity.Rule;
import com.example.masters.repository.DeviceRepository;
import com.example.masters.repository.RuleRepository;
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

    @Autowired
    private RuleRepository ruleRepository;

    @Override
    public void run(String... args) throws Exception {
        try {
            if (deviceRepository.count() < 2) {
                Device lamp = deviceRepository.save(createDevice(
                        "Лампочка",
                        "Для освітлення кімнати",
                        "INV-001",
                        Type.LIGHTING,
                        "src/main/resources/images/ic_lamp.png"
                ));
                Device sensorTemperature = deviceRepository.save(createDevice(
                        "Датчик температури",
                        "Для вимірювання температури в лабораторії",
                        "TEMP-001",
                        Type.SENSOR,
                        "src/main/resources/images/ic_temperature.png"
                ));
                Device sensorSmoke =  deviceRepository.save(createDevice(
                        "Датчик задимленості",
                        "Для вимірювання рівню задимленості в лабораторії",
                        "SMOKE-001",
                        Type.SENSOR,
                        "src/main/resources/images/ic_smoke.png"
                ));
                Device airConditioning = deviceRepository.save(createDevice(
                        "Кондиціонер",
                        "Для охолодження приміщення",
                        "INV-002",
                        Type.CLIMATE,
                        "src/main/resources/images/ic_air_conditioning.png"
                ));
                Device heating = deviceRepository.save(createDevice(
                        "Опалення",
                        "Для нагрівння приміщення",
                        "INV-003",
                        Type.CLIMATE,
                        "src/main/resources/images/ic_heating.png"
                ));
                Device ventilation = deviceRepository.save(createDevice(
                        "Вентиляція",
                        "Для вентиляції лабораторії",
                        "INV-004",
                        Type.CLIMATE,
                        "src/main/resources/images/ic_ventilation.png"
                ));
                Device battery = deviceRepository.save(createDevice(
                        "Система резервного живлення",
                        "Відповідає автоматичне забезпечення живлення лабораторії",
                        "INV-005",
                        Type.ENERGY,
                        "src/main/resources/images/ic_battery.png"
                ));

                Device grid = deviceRepository.save(createDevice(
                        "Мережа",
                        "Надає живлення лабораторії",
                        "INV-006",
                        Type.GRID,
                        "src/main/resources/images/ic_battery.png"
                ));

                ruleRepository.save(createRule(sensorTemperature, ">", 40, airConditioning, "ON"));
                ruleRepository.save(createRule(sensorSmoke, ">", 40, heating, "OFF"));
                ruleRepository.save(createRule(sensorTemperature, "<", 10, airConditioning, "ON"));
                ruleRepository.save(createRule(sensorSmoke, "<", 10, heating, "OFF"));

                ruleRepository.save(createRule(sensorSmoke, ">", 150, ventilation, "ON"));









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
    private Rule createRule(Device sensor,String operator, double threshold,  Device actionDevice, String action) throws IOException {
        return Rule.builder()
                .device(sensor)
                .operator(operator)
                .threshold(threshold)
                .actionDevice(actionDevice)
                .action(action)
                .ruleType("SENSOR")
                .active(true)
                .strict(true)
                .build();
    }
}
