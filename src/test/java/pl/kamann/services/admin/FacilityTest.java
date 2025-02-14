package pl.kamann.services.admin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pl.kamann.dtos.FacilityDto;
import pl.kamann.entities.facility.Facility;
import pl.kamann.repositories.FacilityRepository;

import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

public class FacilityTest {

    @Mock
    private FacilityRepository facilityRepository;

    @InjectMocks
    private FacilityService facilityService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void getFacility_whenFacilitiesExist_thenShouldReturnFacilityDto() {
        long facilityId = 1L;
        Facility facility = Facility.builder()
                .id(facilityId)
                .name("name")
                .address("address")
                .openingHours(LocalTime.of(8, 0))
                .closingHours(LocalTime.of(16, 0))
                .build();

        when(facilityRepository.findById(facilityId)).thenReturn(Optional.of(facility));

        FacilityDto facilityDto = facilityService.getFacility(facilityId);

        assertNotNull(facilityDto);
        verify(facilityRepository, times(1)).findById(facilityId);
    }

    @Test
    public void creteFacility_whenFacilityDoesNotExist_thenShouldCreateFacility() {
        FacilityDto facilityDto = FacilityDto.builder()
                .name("name")
                .address("address")
                .openingHours(LocalTime.of(8, 0))
                .closingHours(LocalTime.of(16, 0))
                .build();

        Facility facility = Facility.builder()
                .name(facilityDto.name())
                .address(facilityDto.address())
                .openingHours(facilityDto.openingHours())
                .closingHours(facilityDto.closingHours())
                .build();

        when(facilityRepository.save(any(Facility.class))).thenReturn(facility);

        facilityService.createFacility(facilityDto);

        verify(facilityRepository, times(1)).save(any(Facility.class));
    }

    @Test
    public void updateFacility_whenFacilityExists_thenShouldUpdateFacility() {
        long facilityId = 1L;
        FacilityDto facilityDto = FacilityDto.builder()
                .id(facilityId)
                .name("name")
                .address("address")
                .openingHours(LocalTime.of(8, 0))
                .closingHours(LocalTime.of(16, 0))
                .build();

        Facility facility = Facility.builder()
                .id(facilityDto.id())
                .name(facilityDto.name())
                .address(facilityDto.address())
                .openingHours(facilityDto.openingHours())
                .closingHours(facilityDto.closingHours())
                .build();

        when(facilityRepository.findById(facilityId)).thenReturn(Optional.of(facility));
        when(facilityRepository.save(any(Facility.class))).thenReturn(facility);

        facilityService.updateFacility(facilityId, facilityDto);

        verify(facilityRepository, times(1)).findById(facilityId);
        verify(facilityRepository, times(1)).save(any(Facility.class));
    }

    @Test
    public void deleteFacility_whenFacilityExists_thenShouldDeleteFacility() {
        long facilityId = 1L;

        when(facilityRepository.existsById(facilityId)).thenReturn(true);

        facilityService.deleteFacility(facilityId);

        verify(facilityRepository, times(1)).existsById(facilityId);
        verify(facilityRepository, times(1)).deleteById(facilityId);
    }
}
