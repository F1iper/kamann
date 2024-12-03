package pl.kamann.services.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.config.global.Codes;
import pl.kamann.dtos.EventReportDto;
import pl.kamann.dtos.EventStat;
import pl.kamann.mappers.EventReportMapper;
import pl.kamann.repositories.EventRepository;

@Service
@RequiredArgsConstructor
public class AdminReportsService {

    private final EventRepository eventRepository;
    private final EventReportMapper eventReportMapper;

    public Page<EventReportDto> getEventReports(Pageable pageable) {
        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
        Page<EventStat> statsPage = eventRepository.findEventStats(pageRequest);
        if (statsPage.isEmpty()) {
            throw new ApiException(
                    "No event statistics found",
                    HttpStatus.NOT_FOUND,
                    Codes.NO_EVENT_STATS
            );
        }
        return statsPage.map(eventReportMapper::toDto);
    }
}