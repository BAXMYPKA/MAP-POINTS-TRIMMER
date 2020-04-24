package mrbaxmypka.gmail.com.LocusPOIconverter.services;

import mrbaxmypka.gmail.com.LocusPOIconverter.entitiesDto.MultipartDto;
import mrbaxmypka.gmail.com.LocusPOIconverter.klm.XmlHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.InputStream;
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
	public void treatMultipartDto(@NonNull MultipartDto multipartDto, @Nullable Locale locale)
		throws IOException, ParserConfigurationException, SAXException, XMLStreamException, TransformerException {
		
		locale = locale == null ? this.locale : locale;
		
		Objects.requireNonNull(
			multipartDto.getMultipartFile().getOriginalFilename(),
			messageSource.getMessage("exception.nullFilename", null, locale));
		
		if (multipartDto.getMultipartFile().getOriginalFilename().endsWith(".kml")) {
			xmlHandler.treatXml(multipartDto);
		} else if (multipartDto.getMultipartFile().getOriginalFilename().endsWith(".kmz")) {
			//TODO: to proceed with a .kmz file
		} else {
			throw new IllegalArgumentException(messageSource.getMessage(
				"exception.fileExtensionNotSupported",
				new Object[]{multipartDto.getMultipartFile().getOriginalFilename()},
				locale));
		}
		
	}
	
/*
	private void proceedWithKml(MultipartDto multipartDto)
		throws IOException, ParserConfigurationException, SAXException, XMLStreamException {
		
		InputStream kmlInputStream = xmlHandler.getInputStream(multipartDto.getMultipartFile());
		Document document = xmlHandler.getDocument(kmlInputStream);
		
		if (multipartDto.isValidateXml()) {
			xmlHandler.validateXml(document);
		}
		if (multipartDto.isSetPath()) {
			xmlHandler.setPath(kmlInputStream);
		}
	}
*/
}
