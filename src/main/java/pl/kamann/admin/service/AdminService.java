package pl.kamann.admin.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.kamann.admin.repository.AdminRepository;
import pl.kamann.admin.repository.ClientEventRepository;
import pl.kamann.attendance.model.Attendance;
import pl.kamann.attendance.model.AttendanceStatus;
import pl.kamann.attendance.repository.AttendanceRepository;
import pl.kamann.auth.role.model.Role;
import pl.kamann.auth.service.AuthService;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.config.global.Codes;
import pl.kamann.event.dto.EventDto;
import pl.kamann.event.mapper.EventMapper;
import pl.kamann.event.model.Event;
import pl.kamann.event.model.EventStatus;
import pl.kamann.event.model.EventType;
import pl.kamann.event.repository.EventRepository;
import pl.kamann.history.model.ClientEventHistory;
import pl.kamann.history.model.ClientMembershipCardHistory;
import pl.kamann.history.repository.UserEventHistoryRepository;
import pl.kamann.membershipcard.model.MembershipCard;
import pl.kamann.membershipcard.model.MembershipCardType;
import pl.kamann.membershipcard.repository.MembershipCardRepository;
import pl.kamann.user.model.AppUser;
import pl.kamann.user.model.AppUserStatus;
import pl.kamann.user.repository.AppUserRepository;
import pl.kamann.utility.EntityLookupService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository adminRepository;
    private final EventRepository eventRepository;
    private final EntityLookupService lookupService;
    private final AuthService authService;

    private final EventMapper eventMapper;

    private final AppUserRepository appUserRepository;
    private final AttendanceRepository attendanceRepository;
    private final MembershipCardRepository membershipCardRepository;
    private final ClientEventRepository clientEventRepository;
    private final UserEventHistoryRepository userEventHistoryRepository;
    private final ClientMemberShipCardHistoryRepository clientMembershipCardHistoryRepository;

    // Event Management
    public EventDto createEvent(EventDto eventDto) {
        AppUser createdBy = lookupService.findUserById(eventDto.getCreatedById());
        AppUser instructor = lookupService.findUserById(eventDto.getInstructorId());
        EventType eventType = lookupService.findEventTypeById(eventDto.getEventTypeId());
        Event event = eventMapper.toEntity(eventDto, createdBy, instructor, eventType);
        return eventMapper.toDto(eventRepository.save(event), List.of());
    }

    public EventDto updateEvent(Long eventId, EventDto updatedEventDto) {
        Event existingEvent = lookupService.findEventById(eventId);
        AppUser instructor = lookupService.findUserById(updatedEventDto.getInstructorId());
        EventType eventType = lookupService.findEventTypeById(updatedEventDto.getEventTypeId());
        eventMapper.updateEventFromDto(existingEvent, updatedEventDto, instructor, eventType);
        Event updatedEvent = eventRepository.save(existingEvent);
        return eventMapper.toDto(updatedEvent, List.of());
    }

    public void deleteEvent(Long eventId) {
        Event event = lookupService.findEventById(eventId);
        eventRepository.delete(event);
    }

    public void cancelEvent(Long eventId) {
        Event event = lookupService.findEventById(eventId);
        if (event.getStartTime().isBefore(LocalDateTime.now())) {
            throw new ApiException("Cannot cancel an event that has already started.", HttpStatus.BAD_REQUEST, Codes.CANNOT_CANCEL_STARTED_EVENT);
        }
        event.setStatus(EventStatus.CANCELLED);
        eventRepository.save(event);
    }

    public EventDto getEventById(Long eventId) {
        Event event = lookupService.findEventById(eventId);
        return eventMapper.toDto(event, List.of());
    }

    public Page<EventDto> searchEvents(LocalDate startDate, LocalDate endDate, String keyword, Pageable pageable) {
        return eventRepository.findFilteredEvents(startDate, endDate, null, null, keyword, pageable)
                .map(event -> eventMapper.toDto(event, List.of()));
    }

    public List<EventDto> getUpcomingEvents(Long userId) {
        AppUser user = lookupService.findUserById(userId);
        List<Event> events = eventRepository.findUpcomingEventsForUser(user, LocalDateTime.now());
        return events.stream()
                .map(event -> eventMapper.toDto(event, List.of()))
                .collect(Collectors.toList());
    }

    @Transactional
    public AppUser approveInstructor(Long userId) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND, Codes.USER_NOT_FOUND));

        if (!user.getStatus().equals(AppUserStatus.PENDING_APPROVAL)) {
            throw new ApiException("User is not pending approval", HttpStatus.BAD_REQUEST, Codes.INVALID_USER_STATUS);
        }

        // Assign the INSTRUCTOR role
        Role instructorRole = authService.findRoleByName(Codes.INSTRUCTOR);
        user.getRoles().add(instructorRole);
        user.setStatus(AppUserStatus.ACTIVE);

        AppUser updatedUser = appUserRepository.save(user);

        // Notify the instructor about approval
        log.info("Send instructor the approval email");

        log.info("Instructor approved: email={}", user.getEmail());
        return updatedUser;
    }

    // Attendance Management
    @Transactional
    public void markAttendance(Long eventId, Long userId, AttendanceStatus status) {
        AppUser user = lookupService.findUserById(userId);
        Event event = lookupService.findEventById(eventId);
        Attendance attendance = attendanceRepository.findByUserAndEvent(user, event)
                .orElse(new Attendance(null, user, event, status, LocalDateTime.now()));
        attendance.setStatus(status);
        attendanceRepository.save(attendance);
    }

    @Transactional
    public Attendance cancelAttendance(Long eventId, Long userId, boolean isInstructorAction) {
        AppUser user = lookupService.findUserById(userId);
        Event event = lookupService.findEventById(eventId);
        Attendance attendance = attendanceRepository.findByUserAndEvent(user, event)
                .orElseThrow(() -> new ApiException("Attendance not found", HttpStatus.NOT_FOUND, Codes.ATTENDANCE_NOT_FOUND));
        if (attendance.getStatus() == AttendanceStatus.PRESENT) {
            throw new ApiException("Cannot cancel attendance already marked as PRESENT.", HttpStatus.BAD_REQUEST, Codes.INVALID_ATTENDANCE_STATE);
        }
        boolean lateCancel = LocalDateTime.now().isAfter(event.getStartTime().minusHours(6));
        if (lateCancel) {
            deductMembershipEntry(user.getId());
            attendance.setStatus(AttendanceStatus.LATE_CANCEL);
        } else {
            attendance.setStatus(AttendanceStatus.EARLY_CANCEL);
        }
        attendance.setCancelledByInstructor(isInstructorAction);
        attendanceRepository.save(attendance);
        return attendance;
    }

    private void deductMembershipEntry(Long userId) {
        MembershipCard card = membershipCardRepository.findActiveCardByUserId(userId)
                .orElseThrow(() -> new ApiException("No active membership card found.", HttpStatus.BAD_REQUEST, Codes.CARD_NOT_ACTIVE));
        if (card.getEntrancesLeft() <= 0) {
            throw new ApiException("No entrances left on the membership card.", HttpStatus.BAD_REQUEST, Codes.NO_ENTRANCES_LEFT);
        }
        card.setEntrancesLeft(card.getEntrancesLeft() - 1);
        membershipCardRepository.save(card);
    }

    // Membership Card Management
    public MembershipCard purchaseMembershipCard(Long userId, MembershipCardType type) {
        AppUser user = lookupService.findUserById(userId);
        MembershipCard card = new MembershipCard();
        card.setUser(user);
        card.setMembershipCardType(type);
        card.setEntrancesLeft(type.getMaxEntrances());
        card.setStartDate(LocalDateTime.now());
        card.setEndDate(LocalDateTime.now().plusDays(type.getValidDays()));
        card.setPurchaseDate(LocalDateTime.now());
        return membershipCardRepository.save(card);
    }

    public List<MembershipCard> getMembershipCardHistory(Long userId) {
        AppUser user = lookupService.findUserById(userId);
        return membershipCardRepository.findByUser(user);
    }

    public List<ClientMembershipCardHistory> getCardHistoryByUser(Long userId) {
        return adminRepository.findClientMembershipCardHistoryByUserId(userId);
    }

    // User History
    public List<ClientEventHistory> getEventHistoryByUser(Long userId) {
        return adminRepository.findClientEventHistoryByUserId(userId);
    }

    public List<ClientEventHistory> getEventHistoryByEvent(Long eventId) {
        return adminRepository.findClientEventHistoryByEventId(eventId);
    }

    public ClientEventHistory addEventHistory(Long userId, Long eventId, AttendanceStatus status, int entrancesUsed) {
        AppUser user = lookupService.findUserById(userId);
        Event event = lookupService.findEventById(eventId);
        ClientEventHistory history = new ClientEventHistory(null, user, event, status, LocalDateTime.now(), entrancesUsed);
        return clientEventRepository.save(history);
    }

    public List<ClientEventHistory> getOwnEventHistory() {
        AppUser user = lookupService.getLoggedInUser();
        return userEventHistoryRepository.findByUser(user);
    }

    public ClientMembershipCardHistory addCardHistory(Long userId, MembershipCardType cardType, LocalDateTime startDate,
                                                      LocalDateTime endDate, int entrances, int remainingEntrances, boolean paid) {
        // Retrieve the user by ID
        AppUser user = lookupService.findUserById(userId);

        // Create a new history entry
        ClientMembershipCardHistory history = new ClientMembershipCardHistory();
        history.setUser(user);
        history.setMembershipCardType(cardType);
        history.setStartDate(startDate);
        history.setEndDate(endDate);
        history.setEntrances(entrances);
        history.setRemainingEntrances(remainingEntrances);
        history.setPaid(paid);

        return clientMembershipCardHistoryRepository.save(history);
    }
}
