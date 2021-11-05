package mrbaxmypka.gmail.com.mapPointsTrimmer.utils;

import org.springframework.core.convert.converter.Converter;

/**
 * To give Spring Boot the ability to convert lower case definitions like 'all' to {@link ThinOutTypes#ALL} (etc) enums.
 */
public class StringToThinOutTypesConverter implements Converter<String, ThinOutTypes> {
    @Override
    public ThinOutTypes convert(String s) {
        return ThinOutTypes.valueOf(s.toUpperCase());
    }
}
