package mrbaxmypka.gmail.com.mapPointsTrimmer.services;

import lombok.Getter;
import mrbaxmypka.gmail.com.mapPointsTrimmer.entitiesDto.MultipartDto;
import mrbaxmypka.gmail.com.mapPointsTrimmer.utils.FileTypes;
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
import javax.xml.transform.TransformerException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Service
public class KmlKmzService {
	
	private final String tempDir = System.getProperty("java.io.tmpdir");
	private final Locale defaultLocale = Locale.ENGLISH;
	private final MessageSource messageSource;
	private final KmlHandler kmlHandler;
	@Getter
	private Path tempFile;
	
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
		throws IOException, ParserConfigurationException, SAXException, TransformerException {
		
		locale = locale == null ? this.defaultLocale : locale;
		String processedKml;
		
		if (multipartDto.getMultipartFile().getOriginalFilename().endsWith(".kml")) {
			processedKml = kmlHandler.processXml(multipartDto.getMultipartFile().getInputStream(), multipartDto);
			tempFile = writeTempKmlFile(processedKml, multipartDto);
			return tempFile;
		} else if (multipartDto.getMultipartFile().getOriginalFilename().endsWith(".kmz")) {
			MultipartFile multipartWithKml = getMultipartFileWithKml(multipartDto.getMultipartFile(), locale);
			multipartDto.setMultipartFile(multipartWithKml);
			
			///////////////////////////////////////WRONG METHOD /////////////////////////////////
			
			
			processedKml = kmlHandler.processXml(multipartDto.getMultipartFile().getInputStream(), multipartDto);
			tempFile = writeTempKmlFile(processedKml, multipartDto);
			return tempFile;
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
					//Size may be unknown if entry isn't written to disk!
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
	
	////////////////////////////////////
	////////////////////////////////////
	///////////////////////////////////
	
	public Path processMultipartDto_2(@NonNull MultipartDto multipartDto, @Nullable Locale locale)
		throws IOException, ParserConfigurationException, SAXException, TransformerException {
		
		locale = locale == null ? this.defaultLocale : locale;
		String processedXml;
		
		if (FileTypes.KML.hasSameExtension(multipartDto.getMultipartFile().getOriginalFilename())) {
			processedXml = kmlHandler.processXml(multipartDto.getMultipartFile().getInputStream(), multipartDto);
		} else if (FileTypes.KMZ.hasSameExtension(multipartDto.getMultipartFile().getOriginalFilename())) {
			InputStream kmlInputStream = getXmlFromZip_2(multipartDto, FileTypes.KML, locale);
			processedXml = kmlHandler.processXml(kmlInputStream, multipartDto);
		} else {
			throw new IllegalArgumentException(messageSource.getMessage(
				"exception.fileExtensionNotSupported",
				new Object[]{multipartDto.getMultipartFile().getOriginalFilename()},
				locale));
		}
		return writeTempFile_2(processedXml, multipartDto, locale);
	}
	
	/**
	 * @param multipartDto           Which contains .gpz, .kmz or any other ZIP archives
	 * @param xmlFileExtension .kml, .gpx or any possible extension of the file to be extracted from the given ZIP
	 *                               archive as it must be a single one inside.
	 * @param locale                 To send a localized error messages.
	 * @return {@link InputStream} from the file with the given file extension (.kml as usual).
	 * @throws IOException
	 */
	private InputStream getXmlFromZip_2(MultipartDto multipartDto, FileTypes xmlFileExtension, Locale locale)
		throws IOException {
		
		try (ZipInputStream zis = new ZipInputStream(multipartDto.getMultipartFile().getInputStream())) {
			ZipEntry zipEntry;
			while ((zipEntry = zis.getNextEntry()) != null) {
				if (xmlFileExtension.hasSameExtension(zipEntry.getName())) {
					//Size may be unknown if entry isn't written to disk!
//					byte[] buffer = new byte[(int) zipEntry.getSize()];
// 					zis.readNBytes(buffer, 0, (int) zipEntry.getSize());
					ByteArrayOutputStream buffer = new ByteArrayOutputStream();
					buffer.writeBytes(zis.readAllBytes());
					return new ByteArrayInputStream(buffer.toByteArray());
				}
			}
		}
		throw new IllegalArgumentException(messageSource.getMessage("exception.kmzNoKml",
			new Object[]{multipartDto.getMultipartFile().getOriginalFilename()}, locale));
	}
	
	/**
	 * https://stackoverflow.com/a/43836969/11592202
	 *
	 * @param processedXml     A processed xml String to be written to the resulting file.
	 * @param xmlFileExtension The extension of a future xml file.
	 * @param multipartDto
	 * @param locale           To return a locale-oriented messages
	 * @return {@link Path} to a temp file to be returned to a User.
	 * @throws IOException With the localized message to be returned to a User if the temp file writing is failed.
	 */
	private Path writeTempZip(String processedXml, FileTypes xmlFileExtension, MultipartDto multipartDto, Locale locale)
		throws IOException {
		String originalFilename = multipartDto.getMultipartFile().getOriginalFilename();
		
		Path tempFile = Paths.get(tempDir.concat(originalFilename));
		
		try (ZipInputStream zis = new ZipInputStream(multipartDto.getMultipartFile().getInputStream());
			 ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(tempFile))) {
			ZipEntry zipInEntry;
			while ((zipInEntry = zis.getNextEntry()) != null) {
				
				ZipEntry zipOutEntry = new ZipEntry(zipInEntry.getName());
				zipOutEntry.setComment(zipInEntry.getComment());
				zipOutEntry.setExtra(zipInEntry.getExtra());
				
				ByteArrayOutputStream buffer = new ByteArrayOutputStream();
				
				//Replace existing file with the new one
				if (xmlFileExtension.hasSameExtension(zipInEntry.getName())) {
					zos.putNextEntry(zipOutEntry);
					buffer.writeBytes(processedXml.getBytes(StandardCharsets.UTF_8));
				} else {
					zos.putNextEntry(zipOutEntry);
					buffer.writeBytes(zis.readAllBytes());
				}
				zos.write(buffer.toByteArray());
				zos.closeEntry();
			}
		} catch (Exception e) {
			//TODO: exception.fileException(1) to be created
			throw new IOException(
				messageSource.getMessage("exception.fileException(1)", new Object[]{e.getMessage()}, locale),
				e);
		}
		return tempFile;
	}
	
	private Path writeTempFile_2(String processedXml, MultipartDto multipartDto, Locale locale) throws IOException {
		String originalFilename = multipartDto.getMultipartFile().getOriginalFilename();
		
		if (FileTypes.KML.hasSameExtension(originalFilename) ||
			(FileTypes.KMZ.hasSameExtension(originalFilename) && multipartDto.getDownloadAs().equals(FileTypes.KML))) {
			
			tempFile = Paths.get(tempDir.concat(originalFilename));
			try (BufferedWriter bufferedWriter = Files.newBufferedWriter(tempFile, StandardCharsets.UTF_8)) {
				bufferedWriter.write(processedXml);
			} catch (Exception e) {
				//TODO: exception.fileException(1) to be created
				throw new IOException(
					messageSource.getMessage("exception.fileException(1)", new Object[]{e.getMessage()}, locale),
					e);
			}
		} else if (FileTypes.KMZ.hasSameExtension(originalFilename) && FileTypes.KMZ.equals(multipartDto.getDownloadAs())) {
			tempFile = writeTempZip(processedXml, FileTypes.KML, multipartDto, locale);
		} else {
			//TODO: exception.unexpectedException to be created
			throw new IOException(
				messageSource.getMessage("exception.unexpectedException", null, locale));
		}
		return tempFile;
	}
	
}
