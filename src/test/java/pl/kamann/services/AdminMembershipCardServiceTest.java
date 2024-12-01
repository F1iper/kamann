package pl.kamann.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pl.kamann.dtos.AdminMembershipCardRequestDto;
import pl.kamann.entities.MembershipCard;
import pl.kamann.entities.MembershipCardType;
import pl.kamann.repositories.MembershipCardRepository;
import pl.kamann.services.admin.AdminMembershipCardService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminMembershipCardServiceTest {

    @Mock
    private MembershipCardService membershipCardService;

    @Mock
    private MembershipCardRepository membershipCardRepository;

    @InjectMocks
    private AdminMembershipCardService adminMembershipCardService;

    private MembershipCard mockCard;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockCard = new MembershipCard();
        mockCard.setId(1L);
        mockCard.setStartDate(LocalDateTime.now());
        mockCard.setMembershipCardType(MembershipCardType.MONTHLY_8);
        mockCard.setEntrancesLeft(8);
        mockCard.setPrice(new BigDecimal("50.00"));
    }

    @Test
    void approvePayment_shouldSetPaidAndActive() {
        when(membershipCardService.findMembershipCardById(1L)).thenReturn(mockCard);

        adminMembershipCardService.approvePayment(1L);

        assertTrue(mockCard.isPaid());
        assertTrue(mockCard.isActive());
        verify(membershipCardRepository, times(1)).save(mockCard);
    }

//    @Test
//    void getExpiringCards_shouldReturnExpiringCards() {
//        when(membershipCardService.findMembershipCardsWithinDates(any(), any()))
//                .thenReturn(Collections.singletonList(mockCard));
//
//        assertEquals(1, adminMembershipCardService.getExpiringCards().size());
//        verify(membershipCardService, times(1))
//                .findMembershipCardsWithinDates(LocalDate.now(), LocalDate.now().plusDays(3));
//    }

//    @Test
//    void createCardForPromotion_shouldSaveNewCard() {
//        var request = new AdminMembershipCardRequestDto(
//                MembershipCardType.MONTHLY_8, new BigDecimal("50.00"));
//
//        adminMembershipCardService.createCardForPromotion(request);
//
//        verify(membershipCardRepository, times(1)).save(any(MembershipCard.class));
//    }

//    @Test
//    void updateMembershipCardShouldUpdateCardDetails() {
//        var request = new AdminMembershipCardRequestDto(
//                MembershipCardType.MONTHLY_4, new BigDecimal("30.00"));
//
//        when(membershipCardRepository.findById(1L)).thenReturn(Optional.of(mockCard));
//        when(membershipCardRepository.save(any(MembershipCard.class))).thenReturn(mockCard);
//
//        var updatedCard = adminMembershipCardService.updateMembershipCard(1L, request);
//
//        assertNotNull(updatedCard);
//        assertEquals(MembershipCardType.MONTHLY_4, updatedCard.getMembershipCardType());
//        assertEquals(new BigDecimal("30.00"), updatedCard.getPrice());
//        verify(membershipCardRepository, times(1)).save(mockCard);
//    }
}
