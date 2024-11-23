package pl.kamann.registration.mapper;

import org.springframework.stereotype.Component;
import pl.kamann.event.model.Event;
import pl.kamann.registration.dto.UserEventRegistrationDto;
import pl.kamann.registration.model.UserEventRegistration;
import pl.kamann.user.model.AppUser;

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
        registration.setId(dto.getId());
        registration.setUser(user);
        registration.setEvent(event);
        registration.setStatus(dto.getStatus());
        registration.setWaitlistPosition(dto.getWaitlistPosition());
        registration.setRegistrationDate(dto.getRegistrationDate());
        return registration;
    }
}
