package pl.kamann.mappers;

import org.springframework.stereotype.Component;
import pl.kamann.dtos.RevenueReportDto;
import pl.kamann.dtos.RevenueStat;

@Component
public class RevenueReportMapper {

    public RevenueReportDto toDto(RevenueStat stat) {
        return new RevenueReportDto(
                stat.getMembershipType(),
                stat.getTotalRevenue(),
                stat.getCount()
        );
    }
}
