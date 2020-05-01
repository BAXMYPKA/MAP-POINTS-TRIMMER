package mrbaxmypka.gmail.com.LocusPOIconverter.services;

import mrbaxmypka.gmail.com.LocusPOIconverter.entitiesDto.MultipartDto;
import mrbaxmypka.gmail.com.LocusPOIconverter.klm.XmlHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerException;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Objects;

@Service
public class KmlKmzService {
	
	Locale locale = Locale.ENGLISH;
	
	@Autowired
	private MessageSource messageSource;
	
	@Autowired
	private XmlHandler xmlHandler;
	
	/**
	 * @param multipartDto
	 * @param locale
	 * @throws IOException To be treated in an ExceptionHandler method or ControllerAdvice level
	 */
	public Path processMultipartDto(@NonNull MultipartDto multipartDto, @Nullable Locale locale)
		throws IOException, ParserConfigurationException, SAXException, XMLStreamException, TransformerException {
		
		locale = locale == null ? this.locale : locale;
		
		Objects.requireNonNull(
			multipartDto.getMultipartFile().getOriginalFilename(),
			messageSource.getMessage("exception.nullFilename", null, locale));
		
		if (multipartDto.getMultipartFile().getOriginalFilename().endsWith(".kml")) {
			String processedKml = xmlHandler.processKml(multipartDto);
			Path tempKmlFile = writeTempKmlFile(processedKml, multipartDto);
			return tempKmlFile;
		} else if (multipartDto.getMultipartFile().getOriginalFilename().endsWith(".kmz")) {
			//TODO: to proceed with a .kmz file
		} else {
			throw new IllegalArgumentException(messageSource.getMessage(
				"exception.fileExtensionNotSupported",
				new Object[]{multipartDto.getMultipartFile().getOriginalFilename()},
				locale));
		}
		return Paths.get("file://");
	}
	
	private Path writeTempKmlFile(String kmlDoc, MultipartDto multipartDto) throws IOException {
		
		Path tempFilePath = Paths.get(System.getProperty("java.io.tmpdir")
				.concat(multipartDto.getMultipartFile().getOriginalFilename()));
		try (BufferedWriter bufferedWriter = Files.newBufferedWriter(tempFilePath, StandardCharsets.UTF_8)){
			bufferedWriter.write(kmlDoc);
			return tempFilePath;
		}
	}
}
