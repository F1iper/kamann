package pl.kamann.dtos;

import lombok.*;
import pl.kamann.entities.AttendanceStatus;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEventHistoryDto {
    private Long id;
    private Long userId;
    private Long eventId;
    private AttendanceStatus status;
    private LocalDateTime attendedDate;
    private int entrancesUsed;
}
