package pl.kamann.client.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.kamann.admin.service.AdminService;
import pl.kamann.attendance.model.Attendance;
import pl.kamann.attendance.service.AttendanceService;
import pl.kamann.client.service.ClientService;
import pl.kamann.event.dto.EventDto;
import pl.kamann.event.service.EventService;
import pl.kamann.history.model.ClientMembershipCardHistory;
import pl.kamann.history.model.ClientEventHistory;
import pl.kamann.history.service.client.ClientUserEventHistoryService;
import pl.kamann.membershipcard.model.MembershipCard;
import pl.kamann.membershipcard.model.MembershipCardType;
import pl.kamann.membershipcard.service.MembershipCardService;
import pl.kamann.registration.model.UserEventRegistration;
import pl.kamann.registration.service.ClientEventRegistrationService;
import pl.kamann.utility.EntityLookupService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/client")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CLIENT')")
public class ClientController {

    private final EventService eventService;
    private final AdminService adminService;
    private final ClientService clientService;
    private final AttendanceService attendanceService;
    private final MembershipCardService membershipCardService;
    private final EntityLookupService lookupService;
    private final ClientUserEventHistoryService clientUserEventHistoryService;
    private final ClientEventRegistrationService clientEventRegistrationService;

    // Events
    @GetMapping("/events/{eventId}")
    public ResponseEntity<EventDto> getEventDetails(@PathVariable Long eventId) {
        EventDto eventDto = eventService.getEventById(eventId);
        return ResponseEntity.ok(eventDto);
    }

    @GetMapping("/events/upcoming")
    public ResponseEntity<List<EventDto>> getUpcomingEvents() {
        Long userId = lookupService.getLoggedInUser().getId();
        List<EventDto> events = eventService.getUpcomingEvents(userId);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/events/search")
    public ResponseEntity<Page<EventDto>> searchEvents(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<EventDto> events = eventService.searchEvents(startDate, endDate, keyword, pageable);
        return ResponseEntity.ok(events);
    }

    @PostMapping("/events/{eventId}/join")
    public ResponseEntity<Attendance> joinEvent(@PathVariable Long eventId) {
        Long userId = lookupService.getLoggedInUser().getId();
        Attendance attendance = clientService.joinEvent(eventId, userId);
        return ResponseEntity.ok(attendance);
    }

    // Attendances
    @PostMapping("/attendance/events/{eventId}/cancel")
    public ResponseEntity<Attendance> cancelEventAttendance(@PathVariable Long eventId) {
        Long userId = lookupService.getLoggedInUser().getId();
        Attendance attendance = attendanceService.cancelAttendance(eventId, userId, false);
        return ResponseEntity.ok(attendance);
    }

    @GetMapping("/attendance/summary")
    public ResponseEntity<Map<String, Object>> getAttendanceSummary() {
        Map<String, Object> summary = attendanceService.getPersonalAttendanceSummary();
        return ResponseEntity.ok(summary);
    }

    // Membership cards
    @GetMapping("/membership-cards/history")
    public ResponseEntity<List<MembershipCard>> getMembershipCardHistory() {
        Long userId = lookupService.getLoggedInUser().getId();
        return ResponseEntity.ok(membershipCardService.getMembershipCardHistory(userId));
    }

    @PostMapping("/membership-cards/request")
    public ResponseEntity<MembershipCard> requestMembershipCard(
            @RequestParam MembershipCardType type) {
        Long userId = lookupService.getLoggedInUser().getId();
        MembershipCard card = membershipCardService.purchaseMembershipCard(userId, type);
        return ResponseEntity.status(HttpStatus.CREATED).body(card);
    }

    // Client history
    @GetMapping("/history/events")
    public ResponseEntity<List<ClientEventHistory>> getOwnEventHistory() {
        Long userId = lookupService.getLoggedInUser().getId();
        List<ClientEventHistory> history = clientUserEventHistoryService.getEventHistoryByUser(userId);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/history/cards")
    public ResponseEntity<List<ClientMembershipCardHistory>> getOwnCardHistory() {
        Long userId = lookupService.getLoggedInUser().getId();
        List<ClientMembershipCardHistory> history = adminService.getCardHistoryByUser(userId);
        return ResponseEntity.ok(history);
    }
    @PostMapping("/{eventId}/register")
    public ResponseEntity<String> registerForEvent(@PathVariable Long eventId, @RequestBody Long userId) {
        boolean success = clientEventRegistrationService.registerUserForEvent(userId, eventId);

        return success ? ResponseEntity.ok("Registration successful.") :
                ResponseEntity.badRequest().body("Registration failed.");
    }

    @PostMapping("/{eventId}/cancel")
    public ResponseEntity<Void> cancelRegistration(@PathVariable Long eventId, @RequestBody Long userId) {
        boolean cancelled = clientEventRegistrationService.cancelUserRegistration(userId, eventId);
        return cancelled ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @GetMapping("/{eventId}/waitlist")
    public ResponseEntity<List<UserEventRegistration>> viewWaitlist(@PathVariable Long eventId) {
        List<UserEventRegistration> waitlist = clientEventRegistrationService.getWaitlistForEvent(eventId);
        return ResponseEntity.ok(waitlist);
    }
}
