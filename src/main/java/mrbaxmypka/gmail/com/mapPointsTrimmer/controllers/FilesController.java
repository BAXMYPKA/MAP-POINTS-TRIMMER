package mrbaxmypka.gmail.com.mapPointsTrimmer.controllers;

import lombok.extern.slf4j.Slf4j;
import mrbaxmypka.gmail.com.mapPointsTrimmer.entitiesDto.MultipartDto;
import mrbaxmypka.gmail.com.mapPointsTrimmer.services.MultipartFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xml.sax.SAXException;

import javax.validation.Valid;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Locale;

@Slf4j
@RestController
public class FilesController extends AbstractController {
	
	@Autowired
	private MultipartFileService multipartFileService;
	
	/**
	 * @param file   Can receive .kml or .kmz files only
	 * @param locale For defining a User language
	 * @return
	 */
	@PostMapping(path = "/poi",
				 consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
				 produces = {MediaType.MULTIPART_FORM_DATA_VALUE})
	public ResponseEntity<FileSystemResource> postKml(
		@Valid @ModelAttribute MultipartDto file, Locale locale)
		throws IOException, SAXException, ParserConfigurationException, TransformerException {
		log.info( "{} file has been received as: {}.", MultipartDto.class.getSimpleName(), file);
		Path tempFile = multipartFileService.processMultipartDto(file, locale);
		log.info("Temp file={}", tempFile);
		FileSystemResource resource = new FileSystemResource(tempFile);
		return ResponseEntity.ok()
			  .header("Content-Disposition", "attachment; filename=\"" + getAsciiEncodedFilename(tempFile) + "\"; filename*=UTF-8''" + getAsciiEncodedFilename(tempFile))
			  .body(resource);
	}
	
	private String getAsciiEncodedFilename(Path pathToFile) {
		try {
			URI encodedFilename = new URI(null, null, pathToFile.getFileName().toString(), null);
			return encodedFilename.toASCIIString();
		} catch (URISyntaxException e) {
			log.info(e.getMessage(), e);
			return pathToFile.getFileName().toString().replaceAll("\\s", "");
		}
	}
}
