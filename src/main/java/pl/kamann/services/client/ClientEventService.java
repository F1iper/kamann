package pl.kamann.services.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.kamann.dtos.OccurrenceEventLightDto;
import pl.kamann.mappers.OccurrenceEventMapper;
import pl.kamann.entities.event.OccurrenceEvent;
import pl.kamann.repositories.OccurrenceEventRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientEventService {
    private final OccurrenceEventRepository occurrenceEventRepository;
    private final OccurrenceEventMapper occurrenceEventMapper;

    public List<OccurrenceEventLightDto> getUpcomingEvents() {
        LocalDateTime now = LocalDateTime.now();
        List<OccurrenceEvent> events = occurrenceEventRepository.findByStartAfterAndCanceledFalse(now);
        return occurrenceEventMapper.toLightDtoList(events);
    }

    public List<OccurrenceEventLightDto> getRegisteredEvents(Long userId) {
        List<OccurrenceEvent> events = occurrenceEventRepository.findByParticipants_IdAndCanceledFalse(userId);
        return occurrenceEventMapper.toLightDtoList(events);
    }

    public List<OccurrenceEventLightDto> getPastEvents() {
        LocalDateTime now = LocalDateTime.now();
        List<OccurrenceEvent> events = occurrenceEventRepository.findByStartBeforeAndCanceledFalse(now);
        return occurrenceEventMapper.toLightDtoList(events);
    }
}