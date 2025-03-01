package pl.kamann.mappers;

import org.mapstruct.Mapper;
import pl.kamann.dtos.FacilityDto;
import pl.kamann.entities.facility.Facility;

@Mapper(componentModel = "spring")
public interface FacilityMapper {
    FacilityDto mapToDto(Facility facility);
    Facility mapToEntity(FacilityDto facilityDto);
}
