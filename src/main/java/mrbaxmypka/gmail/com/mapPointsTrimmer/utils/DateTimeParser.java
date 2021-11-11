package mrbaxmypka.gmail.com.mapPointsTrimmer.utils;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Slf4j
@Getter
public class DateTimeParser {

    /**
     * {@literal <Placemark>
     * <gx:TimeStamp>
     * ======> <when>2014-11-21T00:27:31Z</when>
     * </gx:TimeStamp>
     * </Placemark}
     * <p>
     * Warning! The format "yyyy-MM-dd'T'HH:mm:ss'Z'" can only be parsed as {@link java.time.OffsetDateTime}
     * or
     * {@link java.time.Instant#parse(CharSequence)}, e.g. Instant instant = Instant.parse( "2018-01-23T01:23:45.123456789Z" )
     *
     * @return {@link LocalDateTime} parsed from when:
     * {@literal <Placemark>
     * <gx:TimeStamp>
     * =====> <when>2014-11-21T00:27:31Z</when>
     * </gx:TimeStamp>
     * </Placemark} if presented.
     * Otherwise returns {@link LocalDateTime#MIN}
     */
    public LocalDateTime parseWhenLocalDateTime(String dateTimeText) {
        LocalDateTime dateTime = LocalDateTime.MIN;
        try {
            dateTime = LocalDateTime.parse(dateTimeText.trim(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        } catch (DateTimeParseException e) {
            log.trace(DateTimeFormatter.ISO_OFFSET_DATE_TIME.toString() + " cannot be used for parsing DateTime");
            try {
                dateTime = LocalDateTime.parse(dateTimeText.trim(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            } catch (DateTimeException ex) {
                log.trace("Pattern 'yyyy-MM-dd HH:mm:ss' cannot be used for parsing DateTime");
                try {
                    dateTime = LocalDateTime.parse(dateTimeText.trim(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                } catch (DateTimeException exc) {
                    log.trace(DateTimeFormatter.ISO_LOCAL_DATE_TIME.toString() +
                            " cannot be used for parsing DateTime. LocalDateTime.MINIMUM is being returned.");
                }
            }
        }
        log.trace("LocalDateTime from the <when> has been extracted as {}.", dateTime.toString());
        return dateTime;
    }

}
