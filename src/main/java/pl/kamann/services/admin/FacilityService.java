package pl.kamann.services.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import pl.kamann.config.codes.EventCodes;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.dtos.FacilityDto;
import pl.kamann.mappers.FacilityMapper;
import pl.kamann.repositories.FacilityRepository;

@Service
@RequiredArgsConstructor
public class FacilityService {
    private final FacilityRepository facilityRepository;

    public FacilityDto getFacility(Long id) {
        return FacilityMapper.mapToDto(facilityRepository.findById(id).orElseThrow(() -> new ApiException(
                "Facility not found with id: " + id,
                HttpStatus.NOT_FOUND,
                EventCodes.EVENT_NOT_FOUND.name()
        )));
    }

    public FacilityDto createFacility(FacilityDto facilityDto) {
        return FacilityMapper.mapToDto(facilityRepository.save(FacilityMapper.mapToEntity(facilityDto)));
    }

    public FacilityDto updateFacility(Long id, FacilityDto facilityDto) {
        return facilityRepository.findById(id)
                .map(facility -> {
                    facility.setName(facilityDto.name());
                    facility.setAddress(facilityDto.address());
                    facility.setOpeningHours(facilityDto.openingHours());
                    facility.setClosingHours(facilityDto.closingHours());
                    return FacilityMapper.mapToDto(facilityRepository.save(facility));
                })
                .orElseThrow(() -> new ApiException(
                        "Facility not found with id: " + id,
                        HttpStatus.NOT_FOUND,
                        EventCodes.EVENT_NOT_FOUND.name()
                ));
    }

    public void deleteFacility(Long id) {
        if (!facilityRepository.existsById(id)) {
            throw new ApiException(
                    "Facility not found with id: " + id,
                    HttpStatus.NOT_FOUND,
                    EventCodes.EVENT_NOT_FOUND.name()
            );
        }
        facilityRepository.deleteById(id);
    }
}