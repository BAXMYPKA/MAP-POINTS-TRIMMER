package mrbaxmypka.gmail.com.mapPointsTrimmer.utils;

import org.springframework.core.convert.converter.Converter;

/**
 * To give Spring Boot the ability to convert definitions like 'percentage' to {@link PreviewSizeUnits#PERCENTAGE} enum
 */
public class StringToPreviewSizeUnitConverter implements Converter<String, PreviewSizeUnits> {
	
	@Override
	public PreviewSizeUnits convert(String s) {
		return PreviewSizeUnits.valueOf(s.toUpperCase());
	}
}
