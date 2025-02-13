package pl.kamann.mappers;

import org.springframework.stereotype.Component;
import pl.kamann.dtos.FacilityDto;
import pl.kamann.entities.facility.Facility;

@Component
public class FacilityMapper {
    public static FacilityDto mapToDto(Facility facility) {
        return FacilityDto.builder()
                .id(facility.getId())
                .name(facility.getName())
                .address(facility.getAddress())
                .openingHours(facility.getOpeningHours())
                .closingHours(facility.getClosingHours())
                .build();
    }

    public static Facility mapToEntity(FacilityDto facilityDto) {
        return Facility.builder()
                .id(facilityDto.id())
                .name(facilityDto.name())
                .address(facilityDto.address())
                .openingHours(facilityDto.openingHours())
                .closingHours(facilityDto.closingHours())
                .build();
    }
}
