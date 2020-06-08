package mrbaxmypka.gmail.com.mapPointsTrimmer.utils;

import org.springframework.core.convert.converter.Converter;

public class StringToFileTypeConverter implements Converter<String, DownloadAs> {
	@Override
	public DownloadAs convert(String s) {
		return DownloadAs.valueOf(s.toUpperCase());
	}
}
