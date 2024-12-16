package pl.kamann.events.membershipcard;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.kamann.entities.membershipcard.MembershipCardAction;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MembershipCardChangeEvent {
    private Long membershipCardId;

    private Long userId;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private MembershipCardAction changeType;

    private int remainingEntrances;

    private LocalDateTime timestamp;
}
