package pl.kamann.mappers;

import org.junit.jupiter.api.Test;
import pl.kamann.dtos.FacilityDto;
import pl.kamann.entities.facility.Facility;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class FacilityMapperTest {
    @Test
    public void toDto_shouldMapEverythingCorrectly() {
        Facility facility = Facility.builder()
                .id(1L)
                .name("name")
                .address("address")
                .openingHours(LocalTime.of(8, 0))
                .closingHours(LocalTime.of(16, 0))
                .build();

        FacilityDto facilityDto = FacilityMapper.mapToDto(facility);

        assertEquals(facility.getId(), facilityDto.id());
        assertEquals(facility.getName(), facilityDto.name());
        assertEquals(facility.getAddress(), facilityDto.address());
        assertEquals(facility.getOpeningHours(), facilityDto.openingHours());
        assertEquals(facility.getClosingHours(), facilityDto.closingHours());
    }

    @Test
    public void toEntity_shouldMapEverythingCorrectly() {
        FacilityDto facilityDto = FacilityDto.builder()
                .id(1L)
                .name("name")
                .address("address")
                .openingHours(LocalTime.of(8, 0))
                .closingHours(LocalTime.of(16, 0))
                .build();

        Facility facility = FacilityMapper.mapToEntity(facilityDto);

        assertEquals(facilityDto.id(), facility.getId());
        assertEquals(facilityDto.name(), facility.getName());
        assertEquals(facilityDto.address(), facility.getAddress());
        assertEquals(facilityDto.openingHours(), facility.getOpeningHours());
        assertEquals(facilityDto.closingHours(), facility.getClosingHours());
    }
}
