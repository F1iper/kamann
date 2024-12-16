package pl.kamann.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.entities.membershipcard.MembershipCard;
import pl.kamann.entities.membershipcard.MembershipCardAction;
import pl.kamann.entities.membershipcard.MembershipCardHistory;
import pl.kamann.events.membershipcard.MembershipCardChangeEvent;
import pl.kamann.repositories.MembershipCardHistoryRepository;
import pl.kamann.repositories.MembershipCardRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MembershipCardServiceTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private MembershipCardRepository membershipCardRepository;

    @Mock
    private MembershipCardHistoryRepository membershipCardHistoryRepository;

    @InjectMocks
    private MembershipCardService membershipCardService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void validateActiveCardShouldReturnCardWhenActiveCardExists() {
        var clientId = 1L;
        var activeCard = new MembershipCard();
        activeCard.setActive(true);

        when(membershipCardRepository.findActiveCardByUserId(clientId)).thenReturn(Optional.of(activeCard));

        var result = membershipCardService.validateActiveCard(clientId);

        assertNotNull(result);
        assertTrue(result.isActive());
        verify(membershipCardRepository, times(1)).findActiveCardByUserId(clientId);
    }

    @Test
    void validateActiveCardShouldThrowExceptionWhenNoActiveCardExists() {
        var clientId = 1L;

        when(membershipCardRepository.findActiveCardByUserId(clientId)).thenReturn(Optional.empty());

        var exception = assertThrows(ApiException.class, () -> membershipCardService.validateActiveCard(clientId));

        assertEquals("No active membership card found.", exception.getMessage());
        verify(membershipCardRepository, times(1)).findActiveCardByUserId(clientId);
    }

    @Test
    void logActionShouldLogUsedAction() {
        var card = new MembershipCard();
        var user = new AppUser();
        card.setUser(user);

        membershipCardService.logAction(card, user, MembershipCardAction.USED, 1);

        verify(membershipCardHistoryRepository, times(1)).save(any(MembershipCardHistory.class));
        verify(rabbitTemplate, times(1))
                .convertAndSend(eq("membership-card-exchange"), eq("membership.card.changed"), any(MembershipCardChangeEvent.class));
    }


    @Test
    void logActionShouldLogExpiredAction() {
        var card = new MembershipCard();
        card.setId(1L);
        card.setEntrancesLeft(0);

        var user = new AppUser();
        user.setId(101L);
        card.setUser(user);

        membershipCardService.logAction(card, user, MembershipCardAction.EXPIRE, 0);

        verify(membershipCardHistoryRepository, times(1)).save(any(MembershipCardHistory.class));

        verify(rabbitTemplate, times(1)).convertAndSend(
                eq("membership-card-exchange"),
                eq("membership.card.changed"),
                argThat((Object event) -> {
                    if (event instanceof MembershipCardChangeEvent e) {
                        return e.getMembershipCardId().equals(1L) &&
                                e.getUserId().equals(101L) &&
                                e.getChangeType().equals(MembershipCardAction.EXPIRE) &&
                                e.getRemainingEntrances() == 0 &&
                                e.getTimestamp() != null;
                    }
                    return false;
                })
        );
    }



    @Test
    void logActionShouldThrowExceptionForInvalidUsedEntries() {
        var card = new MembershipCard();
        var user = new AppUser();
        card.setUser(user);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                membershipCardService.logAction(card, user, MembershipCardAction.USED, 0));

        assertEquals("Entries used must be greater than 0 for USED action.", exception.getMessage());
        verify(membershipCardHistoryRepository, never()).save(any());
    }

    @Test
    void useEntranceShouldDeductEntranceAndLogAction() {
        var card = new MembershipCard();
        card.setId(1L);
        card.setEntrancesLeft(5);

        var user = new AppUser();
        user.setId(101L);
        card.setUser(user);

        when(membershipCardRepository.save(any(MembershipCard.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = membershipCardService.useEntrance(card);

        assertNotNull(result);
        assertEquals(4, result.getEntrancesLeft());

        verify(membershipCardRepository, times(1)).save(card);
        verify(membershipCardHistoryRepository, times(1)).save(any(MembershipCardHistory.class));

        verify(rabbitTemplate, times(1)).convertAndSend(
                eq("membership-card-exchange"),
                eq("membership.card.changed"),
                argThat((Object event) -> {
                    if (event instanceof MembershipCardChangeEvent e) {
                        return e.getMembershipCardId().equals(1L) &&
                                e.getUserId().equals(101L) &&
                                e.getChangeType().equals(MembershipCardAction.USED) &&
                                e.getRemainingEntrances() == 4 &&
                                e.getTimestamp() != null;
                    }
                    return false;
                })
        );
    }


    @Test
    void useEntranceShouldThrowExceptionWhenNoEntrancesLeft() {
        var card = new MembershipCard();
        card.setEntrancesLeft(0);

        var exception = assertThrows(ApiException.class, () -> membershipCardService.useEntrance(card));

        assertEquals("No remaining entrances on this membership card.", exception.getMessage());
        verify(membershipCardRepository, never()).save(any());
        verify(membershipCardHistoryRepository, never()).save(any());
    }

    @Test
    void expireCardShouldSetInactiveAndLogAction() {
        var card = new MembershipCard();
        card.setId(1L);
        card.setActive(true);
        card.setEntrancesLeft(0);

        var user = new AppUser();
        user.setId(101L);
        card.setUser(user);

        when(membershipCardRepository.save(any(MembershipCard.class))).thenAnswer(invocation -> invocation.getArgument(0));

        membershipCardService.expireCard(card);

        assertFalse(card.isActive());
        verify(membershipCardRepository, times(1)).save(card);
        verify(membershipCardHistoryRepository, times(1)).save(any(MembershipCardHistory.class));

        verify(rabbitTemplate, times(1)).convertAndSend(
                eq("membership-card-exchange"),
                eq("membership.card.changed"),
                argThat((MembershipCardChangeEvent event) ->
                        event.getMembershipCardId().equals(1L) &&
                                event.getUserId().equals(101L) &&
                                event.getChangeType().equals(MembershipCardAction.EXPIRE) &&
                                event.getRemainingEntrances() == 0 &&
                                event.getTimestamp() != null
                )
        );
    }

}
