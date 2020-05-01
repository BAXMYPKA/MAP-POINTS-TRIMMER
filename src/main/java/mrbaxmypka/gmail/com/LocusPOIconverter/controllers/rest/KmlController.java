package mrbaxmypka.gmail.com.LocusPOIconverter.controllers.rest;

import mrbaxmypka.gmail.com.LocusPOIconverter.entitiesDto.MultipartDto;
import mrbaxmypka.gmail.com.LocusPOIconverter.services.KmlKmzService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.xml.sax.SAXException;

import javax.validation.Valid;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Locale;

@RestController
public class KmlController {
	
	@Autowired
	private MessageSource messageSource;
	
	@Autowired
	private KmlKmzService kmlKmzService;
	
	private Path tmpFile;
	
	/**
	 * @param poiFile Can receive .kml or .kmz files only
	 * @param locale  For defining a User language
	 * @return
	 */
	@PostMapping(path = "/kml", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<String> postKml(@Valid @RequestParam(name = "poiFile") MultipartDto poiFile, Locale locale)
		throws IOException, SAXException, ParserConfigurationException, XMLStreamException, TransformerException {
		
		//TODO: to treat validation errors in the ControllerAdvice
		//TODO: to erase tmp file after app turn off
		
		tmpFile = kmlKmzService.processMultipartDto(poiFile, locale);
		
		return null;
	}
	
	@GetMapping(path = "/download-kml")
	public ResponseEntity<Resource> getKml() {
		return null;
	}
}
