package ch.difty.sipamato.lib;

import java.sql.Timestamp;
import java.time.LocalDateTime;

public final class DateUtils {

    private DateUtils() {
    }

    public static Timestamp tsOf(LocalDateTime ldt) {
        return ldt != null ? Timestamp.valueOf(ldt) : null;
    }
}
