package pl.kamann.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RevenueReportDto {
    private String membershipType;
    private double totalRevenue;
    private long count;
}
