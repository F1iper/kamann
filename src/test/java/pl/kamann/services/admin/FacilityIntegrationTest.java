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
public class FacilityIntegrationTest {

    @Autowired
    private FacilityRepository facilityRepository;

    @Autowired
    private FacilityService facilityService;

    @Test
    public void shouldCreateUpdateAndDeleteFacility() {
        long facilityId = 1L;

        FacilityDto facilityDto = FacilityDto.builder()
                .id(facilityId)
                .name("name")
                .address("address")
                .openingHours(LocalTime.of(8, 0))
                .closingHours(LocalTime.of(16, 0))
                .build();

        facilityService.createFacility(facilityDto);

        assertEquals(1, facilityRepository.count());

        FacilityDto result = facilityService.getFacility(facilityId);

        assertNotNull(result);
        assertEquals(result, facilityDto);

        FacilityDto updatedFacilityDto = FacilityDto.builder()
                .id(facilityId)
                .name("updatedName")
                .address("updatedAddress")
                .openingHours(LocalTime.of(9, 0))
                .closingHours(LocalTime.of(17, 0))
                .build();

        result = facilityService.updateFacility(facilityId, updatedFacilityDto);

        assertEquals(updatedFacilityDto, result);

        facilityService.deleteFacility(facilityId);

        assertEquals(0, facilityRepository.count());
    }
}