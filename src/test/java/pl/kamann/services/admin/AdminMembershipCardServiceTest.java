package pl.kamann.services.admin;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.entities.membershipcard.MembershipCard;
import pl.kamann.entities.membershipcard.MembershipCardType;
import pl.kamann.repositories.MembershipCardRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminMembershipCardServiceTest {

    @Mock
    private MembershipCardRepository membershipCardRepository;

    @InjectMocks
    private AdminMembershipCardService adminMembershipCardService;

    public AdminMembershipCardServiceTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createMembershipCard_shouldCreateAndReturnCard() {
        var type = MembershipCardType.MONTHLY_4;
        var price = BigDecimal.valueOf(30.00);

        var expectedCard = MembershipCard.builder()
                .membershipCardType(type)
                .entrancesLeft(type.getMaxEntrances())
                .price(price)
                .paid(false)
                .active(false)
                .build();

        when(membershipCardRepository.save(any(MembershipCard.class))).thenReturn(expectedCard);

        var result = adminMembershipCardService.createMembershipCard(type, price);

        assertNotNull(result);
        assertEquals(type, result.getMembershipCardType());
        assertEquals(type.getMaxEntrances(), result.getEntrancesLeft());
        assertEquals(price, result.getPrice());
        assertFalse(result.isPaid());
        assertFalse(result.isActive());
        verify(membershipCardRepository, times(1)).save(any(MembershipCard.class));
    }

    @Test
    void approveClientCardRequest_shouldApproveCard() {
        var cardId = 1L;
        var card = MembershipCard.builder()
                .id(cardId)
                .membershipCardType(MembershipCardType.MONTHLY_8)
                .paid(false)
                .active(false)
                .build();

        when(membershipCardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(membershipCardRepository.save(any(MembershipCard.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = adminMembershipCardService.approveClientCardRequest(cardId);

        assertNotNull(result);
        assertTrue(result.isPaid());
        assertTrue(result.isActive());
        assertNotNull(result.getStartDate());
        assertNotNull(result.getEndDate());
        assertEquals(LocalDateTime.now().plusDays(result.getMembershipCardType().getValidDays()).toLocalDate(),
                result.getEndDate().toLocalDate());

        verify(membershipCardRepository, times(1)).findById(cardId);
        verify(membershipCardRepository, times(1)).save(card);
    }

    @Test
    void approveClientCardRequest_shouldThrowExceptionWhenCardNotFound() {
        Long cardId = 1L;

        when(membershipCardRepository.findById(cardId)).thenReturn(Optional.empty());

        var exception = assertThrows(ApiException.class,
                () -> adminMembershipCardService.approveClientCardRequest(cardId));

        assertEquals("Membership card not found.", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        verify(membershipCardRepository, times(1)).findById(cardId);
        verify(membershipCardRepository, never()).save(any(MembershipCard.class));
    }
}
