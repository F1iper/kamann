package pl.kamann.services;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pl.kamann.entities.appuser.AppUser;
import pl.kamann.entities.membershipcard.MembershipCard;
import pl.kamann.entities.membershipcard.MembershipCardType;
import pl.kamann.mappers.MembershipCardMapper;
import pl.kamann.repositories.MembershipCardRepository;
import pl.kamann.services.client.ClientMembershipCardService;
import pl.kamann.utility.EntityLookupService;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ClientMembershipCardServiceTest {

    @Mock
    private MembershipCardService membershipCardService;

    @Mock
    private EntityLookupService lookupService;

    @Mock
    private MembershipCardRepository membershipCardRepository;

    @Mock
    private MembershipCardMapper membershipCardMapper;

    @InjectMocks
    private ClientMembershipCardService clientMembershipCardService;

    private AppUser mockUser;
    private MembershipCard mockCard;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockUser = new AppUser();
        mockUser.setId(1L);

        mockCard = new MembershipCard();
        mockCard.setId(1L);
        mockCard.setMembershipCardType(MembershipCardType.MONTHLY_8);
        mockCard.setEntrancesLeft(8);
        mockCard.setStartDate(LocalDateTime.now());
        mockCard.setEndDate(LocalDateTime.now().plusDays(30));
    }

//    @Test
//    void getActiveCardForLoggedInUserShouldReturnActiveCard() {
//        when(lookupService.getLoggedInUser()).thenReturn(mockUser);
//        when(membershipCardService.findActiveCardByUserId(mockUser.getId())).thenReturn(mockCard);
//        when(membershipCardMapper.toDto(mockCard)).thenReturn(new MembershipCardResponseDto());
//
//        MembershipCardResponseDto response = clientMembershipCardService.getActiveCardForLoggedInUser();
//
//        assertNotNull(response);
//        verify(lookupService, times(1)).getLoggedInUser();
//        verify(membershipCardService, times(1)).findActiveCardByUserId(mockUser.getId());
//        verify(membershipCardMapper, times(1)).toDto(mockCard);
//    }

//    @Test
//    void purchaseMembershipCardForClientShouldSaveCard() {
//        ClientMembershipCardRequestDto request = new ClientMembershipCardRequestDto(MembershipCardType.MONTHLY_8);
//        when(lookupService.getLoggedInUser()).thenReturn(mockUser);
//        when(membershipCardRepository.save(any(MembershipCard.class))).thenReturn(mockCard);
//        when(membershipCardMapper.toDto(any(MembershipCard.class))).thenReturn(new MembershipCardResponseDto());
//
//        MembershipCardResponseDto response = clientMembershipCardService.purchaseMembershipCard(request);
//
//        assertNotNull(response);
//        verify(membershipCardRepository, times(1)).save(any(MembershipCard.class));
//    }
}
