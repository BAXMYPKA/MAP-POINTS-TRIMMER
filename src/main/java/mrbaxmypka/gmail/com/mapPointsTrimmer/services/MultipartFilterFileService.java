package mrbaxmypka.gmail.com.mapPointsTrimmer.services;

import lombok.extern.slf4j.Slf4j;
import mrbaxmypka.gmail.com.mapPointsTrimmer.entitiesDto.MultipartFilterDto;
import mrbaxmypka.gmail.com.mapPointsTrimmer.entitiesDto.MultipartMainDto;
import mrbaxmypka.gmail.com.mapPointsTrimmer.utils.DownloadAs;
import mrbaxmypka.gmail.com.mapPointsTrimmer.xml.KmlHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
public class MultipartFilterFileService extends MultipartMainFileService {
	
	private final KmlHandler kmlHandler;
	
	@Autowired
	public MultipartFilterFileService(FileService fileService, MessageSource messageSource, KmlHandler kmlHandler) {
		super(kmlHandler, fileService, messageSource);
		this.kmlHandler = kmlHandler;
	}
	
	public Path processMultipartFilterDto(MultipartFilterDto multipartFilterDto, Locale locale)
			throws IOException, ParserConfigurationException, SAXException, TransformerException {
		String zipFilename = getFileService().getFileName(multipartFilterDto.getMultipartZipFile().getOriginalFilename());
		String xmlFilename = getFileService().getFileName(multipartFilterDto.getMultipartXmlFile().getOriginalFilename());
		
		checkCorrectZipFilename(zipFilename, locale);
		checkCorrectXmlFilename(xmlFilename, locale);
		
		multipartFilterDto.setXmlFilename(xmlFilename);
		String xmlExtension = getFileService().getExtension(xmlFilename);
		
		if (xmlExtension.equals("kml") || xmlExtension.equals("xml")) {
			return processXml(multipartFilterDto, locale);
		} else if (getFileService().getExtension(xmlFilename).equals("txt")) {
			return processTxt(multipartFilterDto, locale);
		} else if (xmlExtension.equals("kmz")) {
			return processKmz(multipartFilterDto, locale);
		} else {
			throw new FileNotFoundException(getMessageSource().getMessage(
					"exception.filenameNotSupported(1)", new Object[]{xmlFilename}, locale));
		}
	}
	
	private Path processXml(MultipartFilterDto multipartFilterDto, Locale locale)
			throws IOException, ParserConfigurationException, SAXException, TransformerException {
		Document document = kmlHandler.getDocument(multipartFilterDto.getMultipartXmlFile().getInputStream());
		String documentAsString = kmlHandler.getAsString(document);
//        setImagesNamesFromZip(multipartFilterDto);
		return createTempZip(documentAsString, multipartFilterDto, locale);
	}
	
	private Path processKmz(MultipartFilterDto multipartFilterDto, Locale locale)
			throws IOException, ParserConfigurationException, SAXException, TransformerException {
		InputStream inputXmlStream = getXmlFromKmz(multipartFilterDto, locale);
		Document document = kmlHandler.getDocument(inputXmlStream);
		String documentAsString = kmlHandler.getAsString(document);
//        setImagesNamesFromZip(multipartFilterDto);
		return createTempZip(documentAsString, multipartFilterDto, locale);
	}
	
	private Path processTxt(MultipartFilterDto multipartFilterDto, Locale locale) throws IOException {
		try (BufferedReader br = new BufferedReader(
				new InputStreamReader(
						multipartFilterDto.getMultipartXmlFile().getInputStream(), StandardCharsets.UTF_8))) {
			String txt = br.lines().collect(Collectors.joining("\n"));
			return createTempZip(txt, multipartFilterDto, locale);
		} catch (IOException e) {
			throw new IOException(getMessageSource().getMessage(
					"exception.fileException(1)",
					new Object[]{multipartFilterDto.getMultipartXmlFile().getOriginalFilename()},
					locale));
		}
	}
	
	private void checkCorrectZipFilename(String zipFilename, Locale locale) throws IllegalArgumentException {
		checkNullEmptyFilename(zipFilename, locale);
		if (!getFileService().getAllowedZipExtensions().contains(getFileService().getExtension(zipFilename))) {
			throw new IllegalArgumentException(getMessageSource().getMessage(
					"exception.fileExtensionNotSupported", new Object[]{zipFilename}, locale));
		}
	}
	
	private void checkCorrectXmlFilename(String xmlFilename, Locale locale) {
		checkNullEmptyFilename(xmlFilename, locale);
		if (!getFileService().getAllowedXmlExtensions().contains(getFileService().getExtension(xmlFilename))) {
			throw new IllegalArgumentException(getMessageSource().getMessage(
					"exception.fileExtensionNotSupported", new Object[]{xmlFilename}, locale));
		}
	}
	
	private void checkNullEmptyFilename(String filename, Locale locale) throws IllegalArgumentException {
		if (filename == null || filename.isBlank()) {
			throw new IllegalArgumentException(getMessageSource().getMessage(
					"exception.nullFilename", null, locale));
		} else if (getFileService().getExtension(filename).isBlank()) {
			throw new IllegalArgumentException(getMessageSource().getMessage(
					"exception.fileExtensionNotSupported", new Object[]{filename}, locale));
		}
	}
	
	/**
	 * Looks through all the given .zip file for images and adds them into the instant cache in {@link MultipartFilterDto#getImagesNamesFromZip()}
	 *
	 * @param multipartFilterDto Which contains .gpz, .kmz or any other ZIP archives
	 * @throws IOException If something wrong with the given .zip file
	 */
/*
    private void setImagesNamesFromZip(MultipartFilterDto multipartFilterDto)
            throws IOException {
        try (ZipInputStream zis = new ZipInputStream(multipartFilterDto.getMultipartZipFile().getInputStream())) {
            ZipEntry zipEntry;
            while ((zipEntry = zis.getNextEntry()) != null) {
                addImageNameFromZip(zipEntry, multipartFilterDto);
            }
        }
        log.info("All images files are being processed from the given MultipartFilterDto");
    }
*/
	
	/**
	 * https://stackoverflow.com/a/43836969/11592202
	 * Relocates all the internal files from a given .kmz or .gpz from a User to the temp directory as a new kmz
	 * with replacing an old inner .kml or .gpx with the new one.
	 *
	 * @param processedXml       A processed xml String to be written to the resulting file.
	 * @param multipartFilterDto
	 * @param locale             To return a locale-oriented messages
	 * @return {@link Path} to a temp archive file with photos to be returned to a User.
	 * @throws IOException With the localized message to be returned to a User if the temp file writing is failed.
	 */
	private Path createTempZip(String processedXml, MultipartFilterDto multipartFilterDto, Locale locale)
			throws IOException {
		//The name of the zip file
		String zipFilename = getZipFilename(multipartFilterDto);
		
		//It is important to save tempFile as the field to get an opportunity to delete it case of app crashing
		Path tempFile = Paths.get(getTEMP_DIR().concat(zipFilename));
		multipartFilterDto.setTempFile(tempFile);
		
		//Copy original images from the temp .zip to the new temp .zip
		filterTempZip(processedXml, multipartFilterDto);
		
		log.info("Temp zip file {} has been written to the temp dir", tempFile);
		return multipartFilterDto.getTempFile();
	}
	
	/**
	 * Extracts files from the user's archive, filters them and writes into a new temporary zip in the temp file.
	 * If no special encoding for filename given tries to use universal UTF8.
	 * When a {@link ZipInputStream} throws {@link MalformedInputException} or {@link IllegalArgumentException} it tries to
	 * change the filenames encoding in the {@link #changeCharset(MultipartFilterDto)} method.
	 * If no luck rethrows an {@link IllegalArgumentException} with the message to a user about filenames encoding info.
	 *
	 * @param processedXml
	 * @param multipartFilterDto
	 * @throws IOException If something wrong while reading a zip archive.
	 */
	private void filterTempZip(String processedXml, MultipartFilterDto multipartFilterDto) throws IOException {
		//Charset for file names into a given zip
		if (multipartFilterDto.getCharset() == null) {
			multipartFilterDto.setCharset(StandardCharsets.UTF_8);
		}
		try (ZipInputStream zis = new ZipInputStream(
				multipartFilterDto.getMultipartZipFile().getInputStream(),
				multipartFilterDto.getCharset());
			  ZipOutputStream zos = new ZipOutputStream(
					  Files.newOutputStream(multipartFilterDto.getTempFile(),
							  StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING),
					  StandardCharsets.UTF_8)) {
			//Copy original images from the MultipartFile to the temp zip
			ZipEntry zipInEntry;
			while ((zipInEntry = zis.getNextEntry()) != null) {
				
				ZipEntry zipOutEntry = new ZipEntry(zipInEntry.getName());
				zipOutEntry.setComment(zipInEntry.getComment());
				zipOutEntry.setExtra(zipInEntry.getExtra());
				
				
				ByteArrayOutputStream buffer = new ByteArrayOutputStream();
				String imageFileName = getFileService().getFileName(zipOutEntry.getName());
				
				//TODO: to delete
				System.out.println(imageFileName);
				
				if (multipartFilterDto.getFilesToBeExcluded().contains(imageFileName)) {
					//If a file has not to be included in the resultant .zip just skip it
					continue;
				} else if (getFileService().getExtension(imageFileName).equals("kml")) {
					//It is an inner doc.kml into the given .kmz archive and has to be included
					zos.putNextEntry(zipOutEntry);
					buffer.writeBytes(zis.readAllBytes());
				} else if (!processedXml.contains(imageFileName)) {
					//This image is redundant as the user's xml doesn't contain it, just skip this image from .zip
					continue;
				} else {
					zos.putNextEntry(zipOutEntry);
					buffer.writeBytes(zis.readAllBytes());
				}
				zos.write(buffer.toByteArray());
				zos.closeEntry();
			}
		} catch (IllegalArgumentException | MalformedInputException ex) {
			if (ex.getClass().isInstance(MalformedInputException.class) || ex.getMessage().contains("malformed input off")) {
				log.info("Unsupported zip filenames encoding found for Locale={} and encoding={}. Trying to change the charset...",
						multipartFilterDto.getLocale(), multipartFilterDto.getCharset().displayName());
				changeCharset(multipartFilterDto);
				filterTempZip(processedXml, multipartFilterDto);
			} else {
				log.info("Unsupported zip filenames encoding found for Locale={} and encoding={}. Stop the processing.",
						multipartFilterDto.getLocale(), multipartFilterDto.getCharset().displayName());
				throw new IOException(getMessageSource().getMessage(
						"exception.fileException(1)",
						new Object[]{multipartFilterDto.getMultipartZipFile().getOriginalFilename()},
						multipartFilterDto.getLocale()));
			}
		}
		log.info("Images from the User's zip have been added to the {}", multipartFilterDto.getTempFile());
	}
	
	/**
	 * If UTF8 filenames encoding has thrown an exception, this method
	 * For RU locale:
	 * 1) Sets primary OEM "IBM866" encoding.
	 * 2) Then tries to use "Windows-1251" encoding if no luck.
	 * 3) Throws an exception with a message for a user if no luck.
	 * For EN locale:
	 * 1) Sets primary "ISO-8859-1"
	 * 2) Throws an exception with a message to a user if no luck.
	 *
	 * @param multipartFilterDto The inner parameter {@link MultipartFilterDto#setCharset(Charset)} has to be set
	 *                           in the {@link #filterTempZip(String, MultipartFilterDto)} method.
	 */
	private void changeCharset(MultipartFilterDto multipartFilterDto) {
		boolean isUtf8Charset = multipartFilterDto.getCharset().equals(StandardCharsets.UTF_8);
		boolean isIbm866RuCharset = multipartFilterDto.getCharset().equals(Charset.forName("IBM866"));
		boolean isWin1251RuCharset = multipartFilterDto.getCharset().equals(Charset.forName("windows-1251"));
		boolean isEuropeanCharset = multipartFilterDto.getCharset().equals(StandardCharsets.ISO_8859_1);
		boolean isRuLocale = multipartFilterDto.getLocale().getISO3Language().contains("ru");
		boolean isEnglishLocale = multipartFilterDto.getLocale().equals(Locale.ENGLISH);
		if ((isRuLocale && isWin1251RuCharset) || (isEnglishLocale && isEuropeanCharset)) {
			//Those combinations have already been processed without success
			throwZipFilenamesEncodingException(multipartFilterDto);
		} else if (isRuLocale && isUtf8Charset) {
			multipartFilterDto.setCharset(Charset.forName("IBM866"));
			log.debug("Zip filenames encoding has been changed. Locale={}, encoding={}.",
					multipartFilterDto.getLocale(), multipartFilterDto.getCharset().displayName());
		} else if (isRuLocale && isIbm866RuCharset) {
			multipartFilterDto.setCharset(Charset.forName("windows-1251"));
			log.debug("Zip filenames encoding has been changed. Locale={}, encoding={}.",
					multipartFilterDto.getLocale(), multipartFilterDto.getCharset().displayName());
		} else if (isEnglishLocale) {
			multipartFilterDto.setCharset(StandardCharsets.ISO_8859_1);
			log.debug("Zip filenames encoding has been changed. Locale={}, encoding={}.",
					multipartFilterDto.getLocale(), multipartFilterDto.getCharset().displayName());
		} else {
			throwZipFilenamesEncodingException(multipartFilterDto);
		}
	}
	
	private void throwZipFilenamesEncodingException(MultipartFilterDto multipartFilterDto) {
		log.info("Unsupported zip filenames encoding found for Locale={} and encoding={}. Stop the processing..",
				multipartFilterDto.getLocale(), multipartFilterDto.getCharset().displayName());
		throw new IllegalArgumentException(
				getMessageSource().getMessage("exception.archiveEncodingMismatch", null, multipartFilterDto.getLocale()));
	}
	
	/**
	 * As some old versions of Edge browser (at least Microsoft EdgeHTML 17 +) attach {@link MultipartFile#getOriginalFilename()}
	 * as the full path to the file (e.g. "C:\temp\Points.kmz" instead of just "Points.kmz"), getting just the filename
	 * is a bit more complicated.
	 *
	 * @param multipartFilterDto A filename to be extracted from.
	 * @return The corrected filename of a .zip(.kmz) archive from a given {@link MultipartMainDto}
	 */
	private String getZipFilename(MultipartFilterDto multipartFilterDto) {
		String zipFilename;
		if (multipartFilterDto.getDownloadAs().equals(DownloadAs.KMZ)) {
			zipFilename = Objects.requireNonNullElse(
					multipartFilterDto.getMultipartZipFile().getOriginalFilename(), "default.kmz");
			zipFilename = zipFilename.substring(0, zipFilename.lastIndexOf(".") + 1).concat("kmz");
		} else {
			zipFilename = Objects.requireNonNullElse(
					multipartFilterDto.getMultipartZipFile().getOriginalFilename(), "default.zip");
			zipFilename = zipFilename.substring(0, zipFilename.lastIndexOf(".") + 1).concat("zip");
		}
		
		if (zipFilename.contains("/") || zipFilename.contains("\\")) {
			//MultipartFile contains a full path with the filename
			int slashIndex = zipFilename.lastIndexOf("/") != -1 ?
					zipFilename.lastIndexOf("/") :
					zipFilename.lastIndexOf("\\");
			zipFilename = zipFilename.substring(slashIndex + 1);
		}
		return zipFilename;
	}
	
	/**
	 * 1) Looks through all the .kmz
	 * 2) Searches for .xml (.kml) file inside the given .zip (.kml) to return it in the end
	 * 3) Adds all found images files to the instant cache in {@link MultipartMainDto#getImagesNamesFromZip()}
	 *
	 * @param multipartFilterDto Which contains .gpz, .kmz or any other ZIP archives
	 * @param locale             To send a localized error messages.
	 * @return {@link InputStream} from the file with the given file extension (.kml as usual).
	 * @throws IOException If something wrong with the given .zip file
	 */
	private InputStream getXmlFromKmz(MultipartFilterDto multipartFilterDto, Locale locale)
			throws IOException {
		log.info("'{}' file is being extracted from the given MultipartDto", multipartFilterDto);
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		
		try (ZipInputStream zis = new ZipInputStream(multipartFilterDto.getMultipartXmlFile().getInputStream())) {
			ZipEntry zipEntry;
			while ((zipEntry = zis.getNextEntry()) != null) {
				if (DownloadAs.KML.hasSameExtension(zipEntry.getName())) {
					//Size may be unknown if entry isn't written to disk yet!
//					byte[] buffer = new byte[(int) zipEntry.getSize()];
// 					zis.readNBytes(buffer, 0, (int) zipEntry.getSize());
					buffer.writeBytes(zis.readAllBytes());
					log.info("File '{}' has been extracted from zip and will be returned as InputStream", multipartFilterDto.getXmlFilename());
				} else if (multipartFilterDto.getDownloadAs().equals(DownloadAs.KMZ)) {
					addImageNameFromZip(zipEntry, multipartFilterDto);
				}
			}
		}
		if (buffer.size() > 0) {
			return new ByteArrayInputStream(buffer.toByteArray());
		} else {
			throw new IllegalArgumentException(getMessageSource().getMessage("exception.noXmlInZipFound",
					new Object[]{multipartFilterDto.getMultipartFile().getOriginalFilename()}, locale));
		}
	}
}
