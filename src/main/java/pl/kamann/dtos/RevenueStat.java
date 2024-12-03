package pl.kamann.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RevenueStat {
    private String membershipType;
    private double totalRevenue;
    private long count;
}
