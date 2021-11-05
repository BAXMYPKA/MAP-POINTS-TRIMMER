package mrbaxmypka.gmail.com.mapPointsTrimmer.utils;

import org.springframework.core.convert.converter.Converter;

/**
 * To give Spring Boot the ability to convert lower case definitions like 'meters' to {@link DistanceUnits#METERS} (etc) enums.
 */
public class StringToDistanceUnitsConverter implements Converter<String, DistanceUnits> {
    @Override
    public DistanceUnits convert(String s) {
        return DistanceUnits.valueOf(s.toUpperCase());
    }
}
