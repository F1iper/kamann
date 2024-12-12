package pl.kamann.services.instructor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.dtos.MembershipCardResponse;
import pl.kamann.dtos.MembershipCardValidationResponse;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.entities.membershipcard.MembershipCard;
import pl.kamann.entities.membershipcard.MembershipCardAction;
import pl.kamann.mappers.MembershipCardMapper;
import pl.kamann.repositories.MembershipCardRepository;
import pl.kamann.services.MembershipCardService;
import pl.kamann.utility.EntityLookupService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InstructorMembershipCardServiceTest {

    @Mock
    private MembershipCardRepository membershipCardRepository;

    @Mock
    private MembershipCardService membershipCardService;

    @Mock
    private MembershipCardMapper membershipCardMapper;

    @Mock
    private EntityLookupService lookupService;

    @InjectMocks
    private InstructorMembershipCardService instructorMembershipCardService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getClientMembershipCards_shouldReturnCardResponses() {
        Long clientId = 1L;
        AppUser client = new AppUser();
        MembershipCard card1 = new MembershipCard();
        MembershipCard card2 = new MembershipCard();
        MembershipCardResponse response1 = new MembershipCardResponse(1L, clientId, "Monthly_4", 4, null, null, false, true);
        MembershipCardResponse response2 = new MembershipCardResponse(2L, clientId, "Monthly_8", 8, null, null, true, false);

        when(lookupService.findUserById(clientId)).thenReturn(client);
        when(membershipCardRepository.findAllByUser(client)).thenReturn(List.of(card1, card2));
        when(membershipCardMapper.toResponse(card1)).thenReturn(response1);
        when(membershipCardMapper.toResponse(card2)).thenReturn(response2);

        List<MembershipCardResponse> result = instructorMembershipCardService.getClientMembershipCards(clientId);

        assertEquals(2, result.size());
        assertEquals(response1, result.get(0));
        assertEquals(response2, result.get(1));
        verify(lookupService, times(1)).findUserById(clientId);
        verify(membershipCardRepository, times(1)).findAllByUser(client);
        verify(membershipCardMapper, times(2)).toResponse(any(MembershipCard.class));
    }

    @Test
    void getClientMembershipCards_shouldThrowExceptionWhenNoCardsFound() {
        Long clientId = 1L;
        AppUser client = new AppUser();

        when(lookupService.findUserById(clientId)).thenReturn(client);
        when(membershipCardRepository.findAllByUser(client)).thenReturn(List.of());

        ApiException exception = assertThrows(ApiException.class, () -> instructorMembershipCardService.getClientMembershipCards(clientId));

        assertEquals("No membership cards found for the client.", exception.getMessage());
        verify(lookupService, times(1)).findUserById(clientId);
        verify(membershipCardRepository, times(1)).findAllByUser(client);
        verify(membershipCardMapper, never()).toResponse(any(MembershipCard.class));
    }

    @Test
    void validateMembershipForEvent_shouldValidateAndLogUsedAction() {
        Long clientId = 1L;
        Long eventId = 1L;
        MembershipCard activeCard = MembershipCard.builder()
                .entrancesLeft(5)
                .build();

        when(membershipCardService.validateActiveCard(clientId)).thenReturn(activeCard);

        MembershipCardValidationResponse result = instructorMembershipCardService.validateMembershipForEvent(clientId, eventId);

        assertAll(
                () -> assertTrue(result.valid()),
                () -> assertEquals("Membership card validated successfully for event: " + eventId, result.message()),
                () -> assertEquals(clientId, result.clientId()),
                () -> assertEquals(eventId, result.eventId()),
                () -> assertEquals(5, result.remainingEntrances())
        );

        verify(membershipCardService, times(1)).validateActiveCard(clientId);
        verify(membershipCardService, times(1)).useEntrance(activeCard);
        verify(membershipCardService, times(1)).logAction(activeCard, activeCard.getUser(), MembershipCardAction.USED, 1);
    }
}
