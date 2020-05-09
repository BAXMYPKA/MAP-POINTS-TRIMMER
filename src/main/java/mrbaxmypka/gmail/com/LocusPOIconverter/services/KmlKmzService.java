package mrbaxmypka.gmail.com.LocusPOIconverter.services;

import lombok.Getter;
import mrbaxmypka.gmail.com.LocusPOIconverter.entitiesDto.MultipartDto;
import mrbaxmypka.gmail.com.LocusPOIconverter.klm.XmlHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Service
public class KmlKmzService {
	
	private Locale locale = Locale.ENGLISH;
	
	private MessageSource messageSource;
	
	private XmlHandler xmlHandler;
	
	private String tempDir = System.getProperty("java.io.tmpdir");
	
	@Getter
	private Path tempKmlFile;
	
	@Autowired
	public KmlKmzService(XmlHandler xmlHandler, MessageSource messageSource) {
		this.xmlHandler = xmlHandler;
		this.messageSource = messageSource;
	}
	
	/**
	 * @param multipartDto
	 * @param locale
	 * @throws IOException To be treated in an ExceptionHandler method or ControllerAdvice level
	 */
	public Path processMultipartDto(@NonNull MultipartDto multipartDto, @Nullable Locale locale)
		throws IOException, ParserConfigurationException, SAXException, XMLStreamException, TransformerException {
		
		locale = locale == null ? this.locale : locale;
		
		if (multipartDto.getMultipartFile().getOriginalFilename().endsWith(".kml")) {
			String processedKml = xmlHandler.processKml(multipartDto);
			tempKmlFile = writeTempKmlFile(processedKml, multipartDto);
			return tempKmlFile;
		} else if (multipartDto.getMultipartFile().getOriginalFilename().endsWith(".kmz")) {
			unzip(multipartDto.getMultipartFile());
		} else {
			throw new IllegalArgumentException(messageSource.getMessage(
				"exception.fileExtensionNotSupported",
				new Object[]{multipartDto.getMultipartFile().getOriginalFilename()},
				locale));
		}
		return Paths.get("file://");
	}
	
	public void unzip(MultipartFile multipartFile) throws IOException {
		return;
	}
	
	
	private Path writeTempKmlFile(String kmlDoc, MultipartDto multipartDto) throws IOException {
		Path tempFilePath = Paths.get(tempDir.concat(multipartDto.getMultipartFile().getOriginalFilename()));
		try (BufferedWriter bufferedWriter = Files.newBufferedWriter(tempFilePath, StandardCharsets.UTF_8)) {
			bufferedWriter.write(kmlDoc);
			return tempFilePath;
		}
	}
}
