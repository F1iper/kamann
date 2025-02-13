package pl.kamann.services.admin;

class AdminEventServiceTest {

//    private AdminEventService adminEventService;
//    private EventValidationService eventValidationService;
//    private NotificationService notificationService;
//    private EntityLookupService entityLookupService;
//    private PaginationService paginationService;
//    private EventMapper eventMapper;
//    private EventRepository eventRepository;
//    private OccurrenceEventRepository occurrenceEventRepository;
//
//    @BeforeEach
//    void setUp() {
//        eventRepository = mock(EventRepository.class);
//        occurrenceEventRepository = mock(OccurrenceEventRepository.class);
//        eventMapper = mock(EventMapper.class);
//        eventValidationService = mock(EventValidationService.class);
//        notificationService = mock(NotificationService.class);
//        entityLookupService = mock(EntityLookupService.class);
//        paginationService = mock(PaginationService.class);
//
//        adminEventService = new AdminEventService(
//                eventRepository,
//                occurrenceEventRepository,
//                eventMapper,
//                eventValidationService,
//                notificationService,
//                entityLookupService,
//                paginationService
//        );
//    }
//
//    @Test
//    void updateEvent_shouldReturnEventResponseDto() {
//        // Arrange
//        AppUser instructor = AppUser.builder().id(2L).firstName("Jane").lastName("Smith").build();
//        EventType type = EventType.builder().id(1L).name("Yoga").build();
//        Event event = Event.builder()
//                .id(100L)
//                .title("Morning Yoga")
//                .description("Old description")
//                .start(LocalDateTime.now().plusDays(1))
//                .durationMinutes(60)
//                .rrule("FREQ=DAILY")
//                .status(EventStatus.SCHEDULED)
//                .eventType(type)
//                .maxParticipants(15)
//                .instructor(instructor)
//                .build();
//
//        OccurrenceEvent occ = OccurrenceEvent.builder()
//                .id(200L)
//                .event(event)
//                .start(event.getStart().plusDays(2))
//                .durationMinutes(60)
//                .instructor(instructor)
//                .build();
//
//        event.setOccurrences(Collections.singletonList(occ));
//
//        // Mock repository responses
//        when(eventRepository.findById(100L)).thenReturn(Optional.of(event));
//        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));
//        when(occurrenceEventRepository.findAllByEventId(100L)).thenReturn(Collections.singletonList(occ));
//
//        // Mock DTO conversion
//        EventResponseDto mockResponseDto = new EventResponseDto(
//                100L,
//                "Morning Yoga Updated",
//                "New description",
//                event.getStart(),
//                90,
//                "FREQ=DAILY",
//                2L,
//                20
//        );
//
//        when(eventMapper.toResponseDto(any(Event.class))).thenReturn(mockResponseDto);
//
//        // Request DTO
//        EventUpdateRequestDto requestDto = EventUpdateRequestDto.builder()
//                .id(100L)
//                .title("Morning Yoga Updated")
//                .description("New description")
//                .start(event.getStart())
//                .durationMinutes(90)
//                .rrule("FREQ=DAILY")
//                .instructorId(2L)
//                .maxParticipants(20)
//                .build();
//
//        // Act
//        EventResponseDto responseDto = adminEventService.updateEvent(100L, requestDto, EventUpdateScope.ALL_OCCURRENCES);
//
//        // Assert
//        assertNotNull(responseDto, "ResponseDto should not be null");
//        assertEquals("Morning Yoga Updated", responseDto.title(), "Event title should be updated");
//        assertEquals(90, responseDto.durationMinutes(), "Duration should be updated");
//        assertEquals(20, responseDto.maxParticipants(), "Max participants should be updated");
//
//        ArgumentCaptor<List<OccurrenceEvent>> captor = ArgumentCaptor.forClass(List.class);
//        verify(occurrenceEventRepository, times(1)).saveAll(captor.capture());
//
//        List<OccurrenceEvent> updatedOccurrences = captor.getValue();
//        assertEquals(1, updatedOccurrences.size());
//
//        OccurrenceEvent updatedOcc = updatedOccurrences.get(0);
//        assertEquals(90, updatedOcc.getDurationMinutes());
//        assertEquals(20, updatedOcc.getMaxParticipants());
//        assertEquals(instructor, updatedOcc.getInstructor());
//
//        assertEquals(90, updatedOcc.getDurationMinutes());
//        assertEquals(20, updatedOcc.getMaxParticipants());
//        assertEquals(instructor, updatedOcc.getInstructor());
//    }
}