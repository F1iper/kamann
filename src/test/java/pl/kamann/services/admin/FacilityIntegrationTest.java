package pl.kamann.services.admin;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import pl.kamann.dtos.FacilityDto;
import pl.kamann.repositories.FacilityRepository;
import pl.kamann.testcontainers.config.TestContainersConfig;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ContextConfiguration(classes = TestContainersConfig.class)
@ActiveProfiles("test")
@Transactional
class FacilityIntegrationTest {

    @Autowired
    private FacilityService facilityService;

    @Autowired
    private FacilityRepository facilityRepository;

    @Test
    void shouldCreateUpdateAndDeleteFacility() {
        // Create
        FacilityDto facilityDto = FacilityDto.builder()
                .name("name")
                .address("address")
                .openingHours(LocalTime.of(8, 0))
                .closingHours(LocalTime.of(16, 0))
                .build();

        FacilityDto createdFacility = facilityService.createFacility(facilityDto);
        Long facilityId = createdFacility.id();

        assertEquals(1, facilityRepository.count());

        FacilityDto result = facilityService.getFacility(facilityId);

        assertNotNull(result);
        assertEquals(facilityDto.name(), result.name());
        assertEquals(facilityDto.address(), result.address());
        assertEquals(facilityDto.openingHours(), result.openingHours());
        assertEquals(facilityDto.closingHours(), result.closingHours());

        FacilityDto updatedFacilityDto = FacilityDto.builder()
                .id(facilityId)
                .name("updatedName")
                .address("updatedAddress")
                .openingHours(LocalTime.of(9, 0))
                .closingHours(LocalTime.of(17, 0))
                .build();

        result = facilityService.updateFacility(facilityId, updatedFacilityDto);

        assertEquals(updatedFacilityDto.name(), result.name());
        assertEquals(updatedFacilityDto.address(), result.address());
        assertEquals(updatedFacilityDto.openingHours(), result.openingHours());
        assertEquals(updatedFacilityDto.closingHours(), result.closingHours());

        facilityService.deleteFacility(facilityId);

        assertEquals(0, facilityRepository.count());
    }
}