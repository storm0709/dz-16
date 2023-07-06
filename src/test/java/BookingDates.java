import lombok.*;

import java.util.Date;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class BookingDates {
    private String checkin;
    private String checkout;
}
