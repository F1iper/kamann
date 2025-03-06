package pl.kamann.mappers;

import org.mapstruct.Mapper;
import pl.kamann.dtos.FacilityDto;
import pl.kamann.entities.facility.Facility;

@Mapper(componentModel = "spring")
public interface FacilityMapper {
    FacilityDto toFacilityDto(Facility facility);
    Facility toFacility(FacilityDto facilityDto);
}
