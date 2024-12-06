package pl.kamann.services.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.entities.membershipcard.MembershipCard;
import pl.kamann.entities.membershipcard.MembershipCardAction;
import pl.kamann.entities.membershipcard.MembershipCardType;
import pl.kamann.repositories.MembershipCardRepository;
import pl.kamann.services.MembershipCardService;
import pl.kamann.utility.EntityLookupService;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ClientMembershipCardServiceTest {

    @Mock
    private MembershipCardRepository membershipCardRepository;

    @Mock
    private MembershipCardService membershipCardService;

    @Mock
    private EntityLookupService lookupService;

    @InjectMocks
    private ClientMembershipCardService clientMembershipCardService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void requestMembershipCardShouldCreateNewCard() {
        Long cardId = 1L;
        AppUser client = new AppUser();
        client.setId(1L);
        MembershipCard cardTemplate = MembershipCard.builder()
                .id(cardId)
                .membershipCardType(MembershipCardType.MONTHLY_8)
                .price(BigDecimal.valueOf(50.00))
                .active(false)
                .build();

        when(lookupService.getLoggedInUser()).thenReturn(client);
        when(lookupService.findUserById(client.getId())).thenReturn(client);
        when(membershipCardRepository.findById(cardId)).thenReturn(Optional.of(cardTemplate));
        when(membershipCardRepository.findActiveCardByUserId(client.getId())).thenReturn(Optional.empty());
        when(membershipCardRepository.save(any(MembershipCard.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MembershipCard result = clientMembershipCardService.requestMembershipCard(cardId);

        assertNotNull(result);
        assertEquals(client, result.getUser());
        assertEquals(MembershipCardType.MONTHLY_8, result.getMembershipCardType());
        assertEquals(8, result.getEntrancesLeft());
        assertFalse(result.isPaid());
        assertFalse(result.isActive());
        assertTrue(result.isPendingApproval());
        verify(membershipCardRepository, times(1)).save(any(MembershipCard.class));
    }

    @Test
    void requestMembershipCardShouldThrowExceptionWhenActiveCardExists() {
        Long cardId = 1L;
        AppUser client = new AppUser();
        client.setId(1L);
        MembershipCard activeCard = MembershipCard.builder().active(true).build();

        when(lookupService.getLoggedInUser()).thenReturn(client);
        when(lookupService.findUserById(client.getId())).thenReturn(client);
        when(membershipCardRepository.findActiveCardByUserId(client.getId())).thenReturn(Optional.of(activeCard));

        ApiException exception = assertThrows(ApiException.class, () -> clientMembershipCardService.requestMembershipCard(cardId));

        assertEquals("Client already has an active membership card.", exception.getMessage());
        verify(membershipCardRepository, never()).save(any());
    }


    @Test
    void getAvailableMembershipCardsShouldReturnTemplates() {
        MembershipCard template1 = new MembershipCard();
        MembershipCard template2 = new MembershipCard();

        when(membershipCardRepository.findByUserIsNullAndActiveFalse()).thenReturn(List.of(template1, template2));

        List<MembershipCard> result = clientMembershipCardService.getAvailableMembershipCards();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(membershipCardRepository, times(1)).findByUserIsNullAndActiveFalse();
    }

    @Test
    void getActiveCardShouldReturnActiveCard() {
        Long clientId = 1L;
        MembershipCard activeCard = MembershipCard.builder().active(true).build();
        List<MembershipCard> activeCards = List.of(activeCard);

        when(membershipCardRepository.findByUserIdAndActiveTrue(clientId))
                .thenReturn(activeCards);

        MembershipCard result = clientMembershipCardService.getActiveCard(clientId);

        assertNotNull(result);
        assertTrue(result.isActive());

        verify(membershipCardRepository, times(1)).findByUserIdAndActiveTrue(clientId);
    }

    @Test
    void getActiveCardShouldThrowExceptionWhenMultipleActiveCards() {
        Long clientId = 1L;
        MembershipCard card1 = MembershipCard.builder().active(true).build();
        MembershipCard card2 = MembershipCard.builder().active(true).build();
        List<MembershipCard> activeCards = List.of(card1, card2);

        when(membershipCardRepository.findByUserIdAndActiveTrue(clientId))
                .thenReturn(activeCards);

        ApiException exception = assertThrows(ApiException.class,
                () -> clientMembershipCardService.getActiveCard(clientId));

        assertEquals("Multiple active membership cards found.", exception.getMessage());
        verify(membershipCardRepository, times(1)).findByUserIdAndActiveTrue(clientId);
    }

    @Test
    void getActiveCardShouldThrowExceptionWhenNoActiveCardFound() {
        Long clientId = 1L;

        when(membershipCardRepository.findByUserIdAndActiveTrue(clientId))
                .thenReturn(Collections.emptyList());

        ApiException exception = assertThrows(ApiException.class, () -> clientMembershipCardService.getActiveCard(clientId));

        assertEquals("No active membership card found.", exception.getMessage());

        verify(membershipCardRepository, times(1)).findByUserIdAndActiveTrue(clientId);
    }

    @Test
    void deductEntryShouldDeductOneEntranceAndLogAction() {
        Long clientId = 1L;

        MembershipCard activeCard = MembershipCard.builder()
                .active(true)
                .entrancesLeft(5)
                .build();

        when(membershipCardRepository.findByUserIdAndActiveTrue(clientId))
                .thenReturn(List.of(activeCard));

        when(membershipCardRepository.save(any(MembershipCard.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MembershipCard result = clientMembershipCardService.deductEntry(clientId);

        assertNotNull(result);
        assertEquals(4, result.getEntrancesLeft());

        verify(membershipCardRepository, times(1)).findByUserIdAndActiveTrue(clientId);
        verify(membershipCardRepository, times(1)).save(activeCard);
        verify(membershipCardService, times(1))
                .logAction(activeCard, activeCard.getUser(), MembershipCardAction.USED, 1);
    }

    @Test
    void deductEntryShouldThrowExceptionWhenNoEntrancesLeft() {
        Long clientId = 1L;
        MembershipCard activeCard = MembershipCard.builder()
                .active(true)
                .entrancesLeft(0)
                .build();

        when(membershipCardRepository.findByUserIdAndActiveTrue(clientId))
                .thenReturn(List.of(activeCard));

        ApiException exception = assertThrows(ApiException.class, () -> clientMembershipCardService.deductEntry(clientId));

        assertEquals("The membership card has no remaining entrances.", exception.getMessage());

        verify(membershipCardRepository, never()).save(any());
        verify(membershipCardService, never()).logAction(any(), any(), any(), anyInt());
    }

    @Test
    void requestMembershipCardShouldThrowExceptionWhenTemplateCardNotFound() {
        Long cardId = 1L;
        AppUser client = new AppUser();
        client.setId(1L);

        when(lookupService.getLoggedInUser()).thenReturn(client);
        when(membershipCardRepository.findById(cardId)).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class, () -> clientMembershipCardService.requestMembershipCard(cardId));

        assertEquals("Membership card not found.", exception.getMessage());
        verify(membershipCardRepository, never()).save(any());
    }

    @Test
    void requestMembershipCardShouldThrowExceptionWhenUserLookupFails() {
        Long cardId = 1L;

        when(lookupService.getLoggedInUser()).thenThrow(new ApiException("User not logged in.", HttpStatus.UNAUTHORIZED, "USER_NOT_FOUND"));

        ApiException exception = assertThrows(ApiException.class, () -> clientMembershipCardService.requestMembershipCard(cardId));

        assertEquals("User not logged in.", exception.getMessage());
        verify(membershipCardRepository, never()).findById(any());
    }

    @Test
    void requestMembershipCardShouldThrowExceptionWhenMultipleActiveCardsExist() {
        Long cardId = 1L;
        AppUser client = new AppUser();
        client.setId(1L);
        MembershipCard activeCard1 = new MembershipCard();
        MembershipCard activeCard2 = new MembershipCard();

        when(lookupService.getLoggedInUser()).thenReturn(client);
        when(membershipCardRepository.findActiveCardByUserId(client.getId())).thenReturn(Optional.of(activeCard1), Optional.of(activeCard2));

        ApiException exception = assertThrows(ApiException.class, () -> clientMembershipCardService.requestMembershipCard(cardId));

        assertEquals("Client already has an active membership card.", exception.getMessage());
        verify(membershipCardRepository, never()).save(any());
    }

    @Test
    void getAvailableMembershipCardsShouldReturnEmptyListWhenNoCardsAvailable() {
        when(membershipCardRepository.findByUserIsNullAndActiveFalse()).thenReturn(List.of());

        List<MembershipCard> result = clientMembershipCardService.getAvailableMembershipCards();

        assertTrue(result.isEmpty());
        verify(membershipCardRepository, times(1)).findByUserIsNullAndActiveFalse();
    }

    @Test
    void getActiveCardShouldThrowExceptionWhenUserIdIsNull() {
        ApiException exception = assertThrows(ApiException.class, () -> clientMembershipCardService.getActiveCard(null));

        assertEquals("No active membership card found.", exception.getMessage());
        verify(membershipCardRepository, never()).findActiveCardByUserId(any());
    }

    @Test
    void getActiveCardShouldThrowExceptionWhenMultipleActiveCardsExist() {
        Long clientId = 1L;
        MembershipCard activeCard1 = new MembershipCard();
        MembershipCard activeCard2 = new MembershipCard();

        when(membershipCardRepository.findByUserIdAndActiveTrue(clientId)).thenReturn(List.of(activeCard1, activeCard2));

        ApiException exception = assertThrows(ApiException.class, () -> clientMembershipCardService.getActiveCard(clientId));

        assertEquals("Multiple active membership cards found.", exception.getMessage());
        verify(membershipCardRepository, times(1)).findByUserIdAndActiveTrue(clientId);
    }

    @Test
    void deductEntryShouldThrowExceptionWhenCardHasNoEntrancesLeft() {
        Long clientId = 1L;
        MembershipCard activeCard = MembershipCard.builder()
                .active(true)
                .entrancesLeft(0)
                .build();

        when(membershipCardRepository.findByUserIdAndActiveTrue(clientId))
                .thenReturn(List.of(activeCard));

        ApiException exception = assertThrows(ApiException.class, () -> clientMembershipCardService.deductEntry(clientId));

        assertEquals("The membership card has no remaining entrances.", exception.getMessage());

        verify(membershipCardRepository, never()).save(any());
        verify(membershipCardService, never()).logAction(any(), any(), any(), anyInt());
    }

    @Test
    void deductEntryShouldThrowExceptionWhenMultipleActiveCardsExist() {
        Long clientId = 1L;
        MembershipCard activeCard1 = MembershipCard.builder().active(true).build();
        MembershipCard activeCard2 = MembershipCard.builder().active(true).build();

        when(membershipCardRepository.findByUserIdAndActiveTrue(clientId))
                .thenReturn(List.of(activeCard1, activeCard2));

        ApiException exception = assertThrows(ApiException.class, () -> clientMembershipCardService.deductEntry(clientId));

        assertEquals("Multiple active membership cards found.", exception.getMessage());

        verify(membershipCardRepository, never()).save(any());
        verify(membershipCardService, never()).logAction(any(), any(), any(), anyInt());
    }

    @Test
    void deductEntryShouldThrowExceptionWhenNoActiveCardFound() {
        Long clientId = 1L;

        when(membershipCardRepository.findByUserIdAndActiveTrue(clientId))
                .thenReturn(Collections.emptyList());

        ApiException exception = assertThrows(ApiException.class, () -> clientMembershipCardService.deductEntry(clientId));

        assertEquals("No active membership card found.", exception.getMessage());

        verify(membershipCardRepository, never()).save(any());
        verify(membershipCardService, never()).logAction(any(), any(), any(), anyInt());
    }
}
