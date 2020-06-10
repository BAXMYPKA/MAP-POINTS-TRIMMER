package mrbaxmypka.gmail.com.mapPointsTrimmer.controllers;

import mrbaxmypka.gmail.com.mapPointsTrimmer.entitiesDto.MultipartDto;
import mrbaxmypka.gmail.com.mapPointsTrimmer.services.MultipartFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
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
import java.nio.file.Path;
import java.util.Locale;

@RestController
public class FilesController {
	
	@Autowired
	private MessageSource messageSource;
	
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
	public ResponseEntity<FileSystemResource> postKml(@Valid @ModelAttribute MultipartDto file, Locale locale)
		throws IOException, SAXException, ParserConfigurationException, TransformerException {
		
		Path tempFile = multipartFileService.processMultipartDto(file, locale);
		FileSystemResource resource = new FileSystemResource(tempFile);
		return ResponseEntity.ok()
			.header("Content-Disposition", "attachment; filename=" + tempFile.getFileName()).body(resource);
	}
}