package pl.kamann.dtos.reports;

import java.math.BigDecimal;

public record RevenueReportDto(
        String membershipType,
        BigDecimal totalRevenue,
        long count
) {
}
