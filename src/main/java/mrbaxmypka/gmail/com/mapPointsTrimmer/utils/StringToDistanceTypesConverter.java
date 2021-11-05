package mrbaxmypka.gmail.com.mapPointsTrimmer.utils;

import org.springframework.core.convert.converter.Converter;

/**
 * To give Spring Boot the ability to convert lower case definitions like 'meters' to {@link DistanceTypes#METERS} (etc) enums.
 */
public class StringToDistanceTypesConverter implements Converter<String, DistanceTypes> {
    @Override
    public DistanceTypes convert(String s) {
        return DistanceTypes.valueOf(s.toUpperCase());
    }
}
