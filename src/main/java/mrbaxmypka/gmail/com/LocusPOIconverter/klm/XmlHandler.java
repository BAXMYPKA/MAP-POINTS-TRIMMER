package mrbaxmypka.gmail.com.LocusPOIconverter.klm;

import lombok.NoArgsConstructor;
import mrbaxmypka.gmail.com.LocusPOIconverter.entitiesDto.MultipartDto;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
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
import java.io.*;

@NoArgsConstructor
@Component
public class XmlHandler {
	
	private XMLInputFactory inputFactory;
	private XMLEventReader eventReader;
	private XMLOutputFactory outputFactory;
	private XMLEventFactory eventFactory;
	private XMLEventWriter eventWriter;
	private StringWriter stringWriter;
	
	/**
	 * All the additional information for a User (preview size, outdated descriptions etc) are placed inside the
	 * CDATA[[]]. Which is an HTML document.
	 * So the main goal for this method is the extracting this data.
	 */
	public void processKml(MultipartDto multipartDto)
		throws XMLStreamException, IOException, ParserConfigurationException, SAXException, TransformerException {
		
		stringWriter = new StringWriter();
		
		inputFactory = XMLInputFactory.newInstance();
		eventReader = inputFactory.createXMLEventReader(multipartDto.getMultipartFile().getInputStream());
		
		outputFactory = XMLOutputFactory.newInstance();
		eventFactory = XMLEventFactory.newInstance();
		eventWriter = outputFactory.createXMLEventWriter(stringWriter);
		
		if (multipartDto.isValidateXml()) {
			Document document = getDocument(multipartDto.getMultipartFile().getInputStream());
			validateXml(document);
		}
		
		if (!multipartDto.isSetPath() && !multipartDto.isTrimDescriptions() && multipartDto.getPreviewSize() == null) {
			return; //If no conditions are set not to waste time
		}
		
		while (eventReader.hasNext()) {
			XMLEvent event = eventReader.nextEvent();
			switch (event.getEventType()) {
				case XMLEvent.CHARACTERS:
				case XMLEvent.CDATA:
					processCdata(event.asCharacters(), multipartDto);
					continue;
			}
			writeXmlEvent(event);
		}
		System.out.println(stringWriter);//TODO: to delete one
	}
	
	private Document getDocument(InputStream kmlInputStream)
		throws ParserConfigurationException, IOException, SAXException, TransformerException {
		DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document kmlDocument = documentBuilder.parse(kmlInputStream);
		return kmlDocument;
	}
	
	private void writeXmlEvent(XMLEvent event) throws XMLStreamException {
		eventWriter.add(event);
	}
	
	private void writeXmlEvent(Characters characters) throws XMLStreamException {
		eventWriter.add(characters);
	}
	
	private void processCdata(Characters characters, MultipartDto multipartDto) throws XMLStreamException {
		
		org.jsoup.nodes.Document parsedHtml = null;
		
		if (characters.isWhiteSpace()) {
			writeXmlEvent(characters);
			return;
		}
		if (characters.getData().startsWith("<!-- desc_gen:start -->")) { //Obtaining the inner CDATA text
			parsedHtml = Jsoup.parse(characters.getData()); //Get CDATA as HTML document for parsing
//			characters = eventFactory.createCharacters(parsedHtml.html());
		} else {
			writeXmlEvent(characters);
			return;
		}
		if (multipartDto.isTrimDescriptions()) {
			//TODO: to process
		}
		if (multipartDto.isSetPath()) {
			Elements aElements = parsedHtml.select("a[href]");
			setPath(aElements, multipartDto.getPath());
		}
		if (multipartDto.getPreviewSize() != null) {
			//TODO: to process
		}
		
		XMLEvent event = eventFactory.createCData(characters.getData());
		writeXmlEvent(event);
	}
	
	private void setPath(Elements aElements, String path) {
		
		String href = aElements.attr("href");
		if (aElements.hasAttr("href") && !href.startsWith("www.") && !href.startsWith("http:")) {
			href = path == null ? "" : path;
			aElements.attr("href", href);
		}
	}
	
	private void validateXml(Document document) throws SAXException, IOException {
		SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Schema schema = schemaFactory.newSchema(
			new File("src/main/resources/static/xsd/kml-2.2.0/ogckml22.xsd"));
		Validator validator = schema.newValidator();
		validator.validate(new DOMSource(document));
	}
}
