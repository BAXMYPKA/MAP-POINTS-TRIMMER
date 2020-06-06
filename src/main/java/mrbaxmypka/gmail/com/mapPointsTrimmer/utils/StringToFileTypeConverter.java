package mrbaxmypka.gmail.com.mapPointsTrimmer.utils;

import org.springframework.core.convert.converter.Converter;

public class StringToFileTypeConverter implements Converter<String, FileTypes> {
	@Override
	public FileTypes convert(String s) {
		return FileTypes.valueOf(s.toUpperCase());
	}
}
