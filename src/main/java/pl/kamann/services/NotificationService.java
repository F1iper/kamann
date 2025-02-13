package pl.kamann.services;

import org.springframework.stereotype.Service;
import pl.kamann.entities.event.Event;

@Service
public class NotificationService {
    public void notifyParticipants(Event event) {
        System.out.println("Notifying participants of event: " + event.getTitle());
    }
}