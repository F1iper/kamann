package pl.kamann.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.entities.membershipcard.MembershipCard;
import pl.kamann.entities.membershipcard.MembershipCardAction;
import pl.kamann.repositories.MembershipCardRepository;
import pl.kamann.systemevents.MembershipCardEvent;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MembershipCardExpirationServiceTest {

    @Mock
    private MembershipCardRepository membershipCardRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private MembershipCardExpirationService membershipCardExpirationService;

    @Test
    void expireMembershipCardsShouldDeactivateAllExpiredCardsAndPublishEvents() {
        AppUser user1 = AppUser.builder().id(1L).build();
        AppUser user2 = AppUser.builder().id(2L).build();

        MembershipCard expiredCard1 = MembershipCard.builder()
                .id(1L)
                .active(true)
                .user(user1)
                .build();

        MembershipCard expiredCard2 = MembershipCard.builder()
                .id(2L)
                .active(true)
                .user(user2)
                .build();

        List<MembershipCard> expiredCards = List.of(expiredCard1, expiredCard2);

        when(membershipCardRepository.findExpiringCards(any(LocalDateTime.class)))
                .thenReturn(expiredCards);

        membershipCardExpirationService.expireMembershipCards();

        verify(membershipCardRepository).save(expiredCard1);
        verify(membershipCardRepository).save(expiredCard2);

        ArgumentCaptor<MembershipCardEvent> eventCaptor = ArgumentCaptor.forClass(MembershipCardEvent.class);
        verify(eventPublisher, times(2)).publishEvent(eventCaptor.capture());

        List<MembershipCardEvent> publishedEvents = eventCaptor.getAllValues();
        assertThat(publishedEvents).hasSize(2);

        MembershipCardEvent event1 = publishedEvents.get(0);
        assertThat(event1.getUserId()).isEqualTo(1L);
        assertThat(event1.getAction()).isEqualTo(MembershipCardAction.EXPIRE);

        MembershipCardEvent event2 = publishedEvents.get(1);
        assertThat(event2.getUserId()).isEqualTo(2L);
        assertThat(event2.getAction()).isEqualTo(MembershipCardAction.EXPIRE);
    }

    @Test
    void expireMembershipCardsShouldNotDeactivateWhenNoExpiredCards() {
        when(membershipCardRepository.findExpiringCards(any(LocalDateTime.class)))
                .thenReturn(List.of());

        membershipCardExpirationService.expireMembershipCards();

        verify(membershipCardRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }
}