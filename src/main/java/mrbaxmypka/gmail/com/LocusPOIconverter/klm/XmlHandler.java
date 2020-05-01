package mrbaxmypka.gmail.com.LocusPOIconverter.klm;

import lombok.NoArgsConstructor;
import mrbaxmypka.gmail.com.LocusPOIconverter.entitiesDto.MultipartDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.*;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

/**
 * Kml processing class based on the StAX xml-library.
 */
@NoArgsConstructor
@Component
public class XmlHandler {
	
	private XMLEventFactory eventFactory;
	private XMLEventWriter eventWriter;
	private HtmlHandler htmlHandler;
	
	@Autowired
	public XmlHandler(HtmlHandler htmlHandler) {
		this.htmlHandler = htmlHandler;
	}
	
	/**
	 * All the additional information for a User (preview size, outdated descriptions etc) are placed inside the
	 * CDATA[[]] as an HTML markup.
	 * So the main goal for this method is the extracting CDATA and pass it to the HTML parser.
	 */
	public String processKml(MultipartDto multipartDto)
		throws XMLStreamException, IOException, ParserConfigurationException, SAXException, TransformerException {
		
		StringWriter stringWriter = new StringWriter();
		
		XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		XMLEventReader eventReader = inputFactory.createXMLEventReader(multipartDto.getMultipartFile().getInputStream());
		
		XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
		eventFactory = XMLEventFactory.newInstance();
		eventWriter = outputFactory.createXMLEventWriter(stringWriter);
		
		if (multipartDto.isValidateXml()) {
			Document document = getDocument(multipartDto.getMultipartFile().getInputStream());
			validateXml(document);
		}
		//To skip the whole processing if nothing is set
		if (!multipartDto.isSetPath() &&
			!multipartDto.isTrimDescriptions() &&
			!multipartDto.isSetPreviewSize() &&
			!multipartDto.isClearDescriptions() &&
			!multipartDto.isTrimXml()) {
			return new String(multipartDto.getMultipartFile().getBytes());
		}
		
		while (eventReader.hasNext()) {
			XMLEvent event = eventReader.nextEvent();
			switch (event.getEventType()) {
				case XMLEvent.CHARACTERS:
				case XMLEvent.CDATA:
					event = processCdata(event.asCharacters(), multipartDto);
			}
			writeXmlEvent(event);
		}
		System.out.println("\n================ THE RESULT =================================\n");
		System.out.println(stringWriter);//TODO: to delete one
		return stringWriter.toString();
	}
	
	private Document getDocument(InputStream kmlInputStream)
		throws ParserConfigurationException, IOException, SAXException {
		DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document kmlDocument = documentBuilder.parse(kmlInputStream);
		return kmlDocument;
	}
	
	private void writeXmlEvent(XMLEvent event) throws XMLStreamException {
		eventWriter.add(event);
	}
	
	private XMLEvent processCdata(Characters characters, MultipartDto multipartDto) {
		
		if (characters.isWhiteSpace() || !characters.getData().startsWith("<!-- desc_gen:start -->")) {
			return eventFactory.createCharacters(characters.getData());
		}
		//Obtain an inner CDATA text to treat as HTML elements
		String processedHtmlCdata = htmlHandler.processCdata(characters.getData(), multipartDto);
		return eventFactory.createCData(processedHtmlCdata);
	}
	
	//TODO: To treat LocusPOI Images started with 'file://'
	
	private void validateXml(Document document) throws SAXException, IOException {
		SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Schema schema = schemaFactory.newSchema(
			new File("src/main/resources/static/xsd/kml-2.2.0/ogckml22.xsd"));
		Validator validator = schema.newValidator();
		validator.validate(new DOMSource(document));
	}
}
