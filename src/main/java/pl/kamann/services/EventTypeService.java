package pl.kamann.services;

import org.springframework.stereotype.Service;
import pl.kamann.entities.event.EventType;
import pl.kamann.repositories.EventTypeRepository;

@Service
public class EventTypeService {

    private final EventTypeRepository eventTypeRepository;

    public EventTypeService(EventTypeRepository eventTypeRepository) {
        this.eventTypeRepository = eventTypeRepository;
    }

    public EventType findOrCreateEventType(String eventTypeName) {
        String normalizedName = eventTypeName.trim().toLowerCase();
        return eventTypeRepository.findByName(normalizedName)
                .orElseGet(() -> {
                    EventType newEventType = EventType.builder()
                            .name(normalizedName)
                            .build();
                    return eventTypeRepository.save(newEventType);
                });
    }
}