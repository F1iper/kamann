package pl.kamann.admin.controller;

import jakarta.validation.Valid;
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
import pl.kamann.attendance.model.AttendanceStatus;
import pl.kamann.attendance.service.AttendanceService;
import pl.kamann.event.dto.EventDto;
import pl.kamann.event.service.EventService;
import pl.kamann.history.model.ClientEventHistory;
import pl.kamann.history.model.ClientMembershipCardHistory;
import pl.kamann.membershipcard.model.MembershipCard;
import pl.kamann.membershipcard.model.MembershipCardType;
import pl.kamann.membershipcard.service.MembershipCardService;
import pl.kamann.user.model.AppUser;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN') or hasRole('INSTRUCTOR')")
public class AdminController {

    private final AdminService adminService;
    private final EventService eventService;
    private final AttendanceService attendanceService;
    private final MembershipCardService membershipCardService;

    // Event end-points
    @GetMapping("/events/{eventId}")
    public ResponseEntity<EventDto> getEventDetails(@PathVariable Long eventId) {
        EventDto eventDto = eventService.getEventById(eventId);
        return ResponseEntity.ok(eventDto);
    }

    @PostMapping("/events")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EventDto> createEvent(@RequestBody @Valid EventDto eventDto) {
        EventDto createdEvent = eventService.createEvent(eventDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEvent);
    }

    @PutMapping("/events/{eventId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EventDto> updateEvent(@PathVariable Long eventId, @RequestBody @Valid EventDto eventDto) {
        EventDto updatedEvent = eventService.updateEvent(eventId, eventDto);
        return ResponseEntity.ok(updatedEvent);
    }

    @DeleteMapping("/events/{eventId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long eventId) {
        eventService.deleteEvent(eventId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/events/{eventId}/cancel")
    public ResponseEntity<Void> cancelEvent(@PathVariable Long eventId) {
        eventService.cancelEvent(eventId);
        return ResponseEntity.ok().build();
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

    // Attendance end-points
    @PostMapping("/attendance/events/{eventId}/mark")
    public ResponseEntity<Void> markAttendance(
            @PathVariable Long eventId,
            @RequestParam Long userId,
            @RequestParam AttendanceStatus status) {
        attendanceService.markAttendance(eventId, userId, status);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/attendance/events/{eventId}/cancel")
    public ResponseEntity<Attendance> cancelAttendance(
            @PathVariable Long eventId,
            @RequestParam Long userId) {
        Attendance attendance = attendanceService.cancelAttendance(eventId, userId, true);
        return ResponseEntity.ok(attendance);
    }

    @GetMapping("/attendance/upcoming")
    public ResponseEntity<List<EventDto>> getUpcomingEvents(@RequestParam Long userId) {
        List<EventDto> events = adminService.getUpcomingEvents(userId);
        return ResponseEntity.ok(events);
    }

    // Membership end-points
    @PostMapping("/membership-cards/purchase")
    public ResponseEntity<MembershipCard> purchaseMembershipCard(
            @RequestParam Long userId,
            @RequestParam MembershipCardType type) {
        MembershipCard card = membershipCardService.purchaseMembershipCard(userId, type);
        return ResponseEntity.status(HttpStatus.CREATED).body(card);
    }


    @PostMapping("/approve-instructor/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> approveInstructor(@PathVariable Long userId) {
        AppUser approvedUser = adminService.approveInstructor(userId);
        return ResponseEntity.ok("Instructor approved successfully: " + approvedUser.getEmail());
    }

    @PutMapping("/membership-cards/{cardId}/approve-payment")
    public ResponseEntity<Void> approvePayment(@PathVariable Long cardId) {
        membershipCardService.approvePayment(cardId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/membership-cards/{cardId}/use-entrance")
    public ResponseEntity<Void> useEntrance(@PathVariable Long cardId) {
        membershipCardService.useEntrance(cardId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/membership-cards/{userId}/history")
    public ResponseEntity<List<MembershipCard>> getMembershipCardHistory(@PathVariable Long userId) {
        return ResponseEntity.ok(membershipCardService.getMembershipCardHistory(userId));
    }

    @GetMapping("/membership-cards/expiring")
    public ResponseEntity<Void> notifyExpiringCards() {
        membershipCardService.notifyExpiringCards();
        return ResponseEntity.ok().build();
    }

    // History end-points
    @GetMapping("/history/events/user/{userId}")
    public ResponseEntity<List<ClientEventHistory>> getEventHistoryByUser(@PathVariable Long userId) {
        List<ClientEventHistory> history = adminService.getEventHistoryByUser(userId);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/history/events/{eventId}")
    public ResponseEntity<List<ClientEventHistory>> getEventHistoryByEvent(@PathVariable Long eventId) {
        List<ClientEventHistory> history = adminService.getEventHistoryByEvent(eventId);
        return ResponseEntity.ok(history);
    }

    @PostMapping("/history/events")
    public ResponseEntity<ClientEventHistory> addEventHistory(@RequestParam Long userId,
                                                              @RequestParam Long eventId,
                                                              @RequestParam AttendanceStatus status,
                                                              @RequestParam int entrancesUsed) {
        ClientEventHistory history = adminService.addEventHistory(userId, eventId, status, entrancesUsed);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/history/events/self")
    public ResponseEntity<List<ClientEventHistory>> getOwnEventHistory() {
        List<ClientEventHistory> history = adminService.getOwnEventHistory();
        return ResponseEntity.ok(history);
    }

    @GetMapping("/history/cards/user/{userId}")
    public ResponseEntity<List<ClientMembershipCardHistory>> getCardHistoryByUser(@PathVariable Long userId) {
        List<ClientMembershipCardHistory> history = adminService.getCardHistoryByUser(userId);
        return ResponseEntity.ok(history);
    }

    @PostMapping("/history/cards")
    public ResponseEntity<ClientMembershipCardHistory> addCardHistory(@RequestParam Long userId,
                                                                      @RequestParam MembershipCardType cardType,
                                                                      @RequestParam LocalDateTime startDate,
                                                                      @RequestParam LocalDateTime endDate,
                                                                      @RequestParam int entrances,
                                                                      @RequestParam int remainingEntrances,
                                                                      @RequestParam boolean paid) {
        ClientMembershipCardHistory history = adminService.addCardHistory(userId, cardType, startDate, endDate,
                entrances, remainingEntrances, paid);
        return ResponseEntity.ok(history);
    }
}
