package mrbaxmypka.gmail.com.LocusPOIconverter.utils;

import org.springframework.core.convert.converter.Converter;

/**
 * To give Spring Boot the ability to convert lower case definitions like 'relative' to {@link PathTypes#RELATIVE} enum
 */
public class StringToPathTypesConverter implements Converter<String, PathTypes> {
	@Override
	public PathTypes convert(String s) {
		return PathTypes.valueOf(s.toUpperCase());
	}
}
