package mrbaxmypka.gmail.com.mapPointsTrimmer.services;

import lombok.Getter;
import mrbaxmypka.gmail.com.mapPointsTrimmer.entitiesDto.MultipartDto;
import mrbaxmypka.gmail.com.mapPointsTrimmer.xml.KmlHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.mock.web.MockMultipartFile;
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
import java.util.zip.ZipInputStream;

@Service
public class KmlKmzService {
	
	private Locale defaultLocale = Locale.ENGLISH;
	
	private MessageSource messageSource;
	
	private KmlHandler kmlHandler;
	
	private String tempDir = System.getProperty("java.io.tmpdir");
	
	@Getter
	private Path tempKmlFile;
	
	@Autowired
	public KmlKmzService(KmlHandler kmlHandler, MessageSource messageSource) {
		this.kmlHandler = kmlHandler;
		this.messageSource = messageSource;
	}
	
	/**
	 * @param multipartDto
	 * @param locale
	 * @throws IOException To be treated in an ExceptionHandler method or ControllerAdvice level
	 */
	public Path processMultipartDto(@NonNull MultipartDto multipartDto, @Nullable Locale locale)
		  throws IOException, ParserConfigurationException, SAXException, XMLStreamException, TransformerException {
		
		locale = locale == null ? this.defaultLocale : locale;
		String processedKml;
		
		if (multipartDto.getMultipartFile().getOriginalFilename().endsWith(".kml")) {
//			processedKml = kmlHandler.processKml(multipartDto);
			processedKml = kmlHandler.processXml(multipartDto);
			tempKmlFile = writeTempKmlFile(processedKml, multipartDto);
			return tempKmlFile;
		} else if (multipartDto.getMultipartFile().getOriginalFilename().endsWith(".kmz")) {
			MultipartFile multipartWithKml = getMultipartFileWithKml(multipartDto.getMultipartFile(), locale);
			multipartDto.setMultipartFile(multipartWithKml);
			processedKml = kmlHandler.processXml(multipartDto);
			tempKmlFile = writeTempKmlFile(processedKml, multipartDto);
			return tempKmlFile;
		} else {
			throw new IllegalArgumentException(messageSource.getMessage(
				  "exception.fileExtensionNotSupported",
				  new Object[]{multipartDto.getMultipartFile().getOriginalFilename()},
				  locale));
		}
	}
	
	/**
	 * Extracts a .kml file from a given .kmz archive and inserts it into a new {@link MultipartFile} to return.
	 *
	 * @param multipartFileWithKmz Receives {@link MultipartFile} with .kmz file inside it.
	 * @param locale               To return a localized maxFileSizeMb to a User in case of error.
	 * @return New {@link MultipartFile} with only .kml file in it.
	 */
	private MultipartFile getMultipartFileWithKml(MultipartFile multipartFileWithKmz, Locale locale) throws IOException {
		try (ZipInputStream zis = new ZipInputStream(multipartFileWithKmz.getInputStream())) {
			ZipEntry zipEntry;
			while ((zipEntry = zis.getNextEntry()) != null) {
				if (zipEntry.getName().endsWith(".kml")) {
					//Size may be unknown is entry not written to disk!
//					byte[] buffer = new byte[(int) zipEntry.getSize()];
// 					zis.readNBytes(buffer, 0, (int) zipEntry.getSize());
					ByteArrayOutputStream buffer = new ByteArrayOutputStream();
					buffer.writeBytes(zis.readAllBytes());
					multipartFileWithKmz = new MockMultipartFile(
						  zipEntry.getName(), zipEntry.getName(), "text/plain", buffer.toByteArray());
					return multipartFileWithKmz;
				}
			}
		}
		throw new IllegalArgumentException(messageSource.getMessage("exception.kmzNoKml",
			  new Object[]{multipartFileWithKmz.getOriginalFilename()}, locale));
	}
	
	private Path writeTempKmlFile(String kmlDoc, MultipartDto multipartDto) throws IOException {
		Path tempFilePath = Paths.get(tempDir.concat(multipartDto.getMultipartFile().getOriginalFilename()));
		try (BufferedWriter bufferedWriter = Files.newBufferedWriter(tempFilePath, StandardCharsets.UTF_8)) {
			bufferedWriter.write(kmlDoc);
			return tempFilePath;
		}
	}
}
