package pl.kamann.mappers;

import org.springframework.stereotype.Component;
import pl.kamann.dtos.reports.RevenueReportDto;
import pl.kamann.entities.reports.RevenueStatEntity;

@Component
public class RevenueReportMapper {

    public RevenueReportDto toDto(RevenueStatEntity entity) {
        return new RevenueReportDto(
                entity.getMembershipType().getDisplayName(),
                entity.getTotalRevenue(),
                entity.getTotalTransactions()
        );
    }
}