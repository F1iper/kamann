package pl.kamann.mappers;

import org.springframework.stereotype.Component;
import pl.kamann.entities.event.Event;
import pl.kamann.dtos.UserEventRegistrationDto;
import pl.kamann.entities.event.UserEventRegistration;
import pl.kamann.entities.appuser.AppUser;

@Component
public class UserEventRegistrationMapper {

    public UserEventRegistrationDto toDto(UserEventRegistration registration) {
        return UserEventRegistrationDto.builder()
                .id(registration.getId())
                .userId(registration.getUser().getId())
                .eventId(registration.getEvent().getId())
                .status(registration.getStatus())
                .waitlistPosition(registration.getWaitlistPosition())
                .registrationDate(registration.getRegistrationDate())
                .build();
    }

    public UserEventRegistration toEntity(UserEventRegistrationDto dto, AppUser user, Event event) {
        UserEventRegistration registration = new UserEventRegistration();
        registration.setId(dto.id());
        registration.setUser(user);
        registration.setEvent(event);
        registration.setStatus(dto.status());
        registration.setWaitlistPosition(dto.waitlistPosition());
        registration.setRegistrationDate(dto.registrationDate());
        return registration;
    }
}
