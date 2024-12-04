package pl.kamann.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.kamann.entities.membershipcard.MembershipCard;
import pl.kamann.repositories.MembershipCardHistoryRepository;
import pl.kamann.repositories.MembershipCardRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
//todo: Add missing test cases, need to redefine expireCard logic probably
class MembershipCardExpirationServiceTest {

    @Mock
    private MembershipCardRepository membershipCardRepository;

    @Mock
    private MembershipCardService membershipCardService;

    @Mock
    private MembershipCardHistoryRepository membershipCardHistoryRepository;

    @InjectMocks
    private MembershipCardExpirationService membershipCardExpirationService;

    @Test
    void expireMembershipCardsShouldCallExpireForAllExpiredCards() {
        MembershipCard expiredCard1 = MembershipCard.builder().id(1L).active(true).build();
        MembershipCard expiredCard2 = MembershipCard.builder().id(2L).active(true).build();
        List<MembershipCard> expiredCards = List.of(expiredCard1, expiredCard2);

        when(membershipCardRepository.findByEndDateBeforeAndActiveTrue(any(LocalDateTime.class)))
                .thenReturn(expiredCards);

        membershipCardExpirationService.expireMembershipCards();

        verify(membershipCardService, times(1)).expireCard(expiredCard1);
        verify(membershipCardService, times(1)).expireCard(expiredCard2);
        verify(membershipCardRepository, times(1)).findByEndDateBeforeAndActiveTrue(any(LocalDateTime.class));
    }

    @Test
    void expireMembershipCardsShouldNotCallExpireWhenNoExpiredCards() {
        when(membershipCardRepository.findByEndDateBeforeAndActiveTrue(any(LocalDateTime.class)))
                .thenReturn(List.of());

        membershipCardExpirationService.expireMembershipCards();

        verify(membershipCardService, never()).expireCard(any());
        verify(membershipCardRepository, times(1)).findByEndDateBeforeAndActiveTrue(any(LocalDateTime.class));
    }
}
