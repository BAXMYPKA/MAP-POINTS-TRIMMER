package mrbaxmypka.gmail.com.LocusPOIconverter.controllers.rest;

import mrbaxmypka.gmail.com.LocusPOIconverter.entitiesDto.MultipartDto;
import mrbaxmypka.gmail.com.LocusPOIconverter.services.KmlKmzService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.xml.sax.SAXException;

import javax.validation.Valid;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerException;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Locale;

@RestController
public class KmlController {
	
	@Autowired
	private MessageSource messageSource;
	
	@Autowired
	private KmlKmzService kmlKmzService;
	
	/**
	 * @param poiFile Can receive .kml or .kmz files only
	 * @param locale  For defining a User language
	 * @return
	 */
	@PostMapping(path = "/kml",
				 consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
				 produces = {MediaType.MULTIPART_FORM_DATA_VALUE})
	public ResponseEntity<FileSystemResource> postKml(@Valid @ModelAttribute MultipartDto poiFile,
		BindingResult bindingResult, Locale locale)
		throws IOException, SAXException, ParserConfigurationException, XMLStreamException, TransformerException {
		
		//TODO: to treat validation errors in the ControllerAdvice
		//TODO: to erase tmp file after app turn off
		
		if (bindingResult.hasErrors()) {
//			throw new BindException(bindingResult.get)
		}
		Path tmpFile = kmlKmzService.processMultipartDto(poiFile, locale);
		Resource kmlResource = new PathResource(tmpFile);
		FileSystemResource resource = new FileSystemResource(tmpFile);
		//TODO: to delete
		System.out.println(resource.getFilename());
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return ResponseEntity.ok()
			.header("Content-Disposition", "attachment; filename=" + tmpFile.getFileName()).body(resource);
	}
}
