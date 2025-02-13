package pl.kamann.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import pl.kamann.config.codes.EventCodes;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.dtos.EventUpdateRequest;
import pl.kamann.dtos.EventUpdateResponse;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.entities.event.Event;
import pl.kamann.mappers.EventMapper;
import pl.kamann.repositories.EventRepository;
import pl.kamann.utility.EntityLookupService;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EventUpdateService {

    private final EventRepository eventRepository;
    private final EntityLookupService lookupService;
    private final EventMapper eventMapper;

    @Transactional
    public EventUpdateResponse updateEventDetails(Long eventId, EventUpdateRequest requestDto) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ApiException(
                        "Event not found with id: " + eventId,
                        HttpStatus.NOT_FOUND,
                        EventCodes.EVENT_NOT_FOUND.name()));

        if (StringUtils.hasText(requestDto.title())) {
            if (requestDto.title().length() > 255) {
                throw new ApiException("Title cannot exceed 255 characters.",
                        HttpStatus.BAD_REQUEST,
                        EventCodes.INVALID_EVENT_TITLE.name());
            }
            event.setTitle(requestDto.title().trim());
        }

        if (StringUtils.hasText(requestDto.description())) {
            if (requestDto.description().length() > 1000) {
                throw new ApiException("Description cannot exceed 1000 characters.",
                        HttpStatus.BAD_REQUEST,
                        EventCodes.INVALID_EVENT_DESCRIPTION.name());
            }
            event.setDescription(requestDto.description().trim());
        }

        if (requestDto.start() != null) {
            //todo cannot be in the past, but how far into future?
            if (requestDto.start().isBefore(LocalDateTime.now())) {
                throw new ApiException("Start time cannot be in the past.",
                        HttpStatus.BAD_REQUEST,
                        EventCodes.INVALID_EVENT_START.name());
            }
            event.setStart(requestDto.start());
        }

        if (requestDto.durationMinutes() != null) {
            // todo use system variable to setup max event duration (?)
            if (requestDto.durationMinutes() <= 0) {
                throw new ApiException("Duration must be a positive number.",
                        HttpStatus.BAD_REQUEST,
                        EventCodes.INVALID_EVENT_DURATION.name());
            }
            event.setDurationMinutes(requestDto.durationMinutes());
        }

        if (requestDto.instructorId() != null) {
            AppUser instructor = lookupService.findUserById(requestDto.instructorId());
            if (instructor == null) {
                throw new ApiException("Instructor not found.",
                        HttpStatus.BAD_REQUEST,
                        EventCodes.INVALID_INSTRUCTOR.name());
            }
            event.setInstructor(instructor);
        }

        if (requestDto.maxParticipants() != null) {
            //todo use system variable for max participants
            if (requestDto.maxParticipants() <= 0) {
                throw new ApiException("Max participants must be greater than zero.",
                        HttpStatus.BAD_REQUEST,
                        EventCodes.INVALID_MAX_PARTICIPANTS.name());
            }
            event.setMaxParticipants(requestDto.maxParticipants());
        }

        Event updatedEvent = eventRepository.saveAndFlush(event);
        return eventMapper.toEventUpdateResponse(updatedEvent);
    }
}
