package mrbaxmypka.gmail.com.LocusPOIconverter.klm;

import lombok.NoArgsConstructor;
import mrbaxmypka.gmail.com.LocusPOIconverter.entitiesDto.MultipartDto;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities;
import org.jsoup.parser.Parser;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;
import org.jsoup.select.Evaluator;
import org.jsoup.select.NodeFilter;
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
	
	private XMLEventFactory eventFactory;
	private XMLEventWriter eventWriter;
	
	/**
	 * All the additional information for a User (preview size, outdated descriptions etc) are placed inside the
	 * CDATA[[]]. Which is an HTML document.
	 * So the main goal for this method is the extracting this data.
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
		
		if (!multipartDto.isSetPath() && !multipartDto.isTrimDescriptions() && !multipartDto.isSetPreviewSize() && !multipartDto.isTrimXml()) {
			return new String(multipartDto.getMultipartFile().getBytes()); //If no conditions are set not to waste time
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
	
	private void writeXmlEvent(Characters characters) throws XMLStreamException {
		eventWriter.add(characters);
	}
	
	private XMLEvent processCdata(Characters characters, MultipartDto multipartDto) throws XMLStreamException {
		
		Element parsedHtmlFragment;
		
		if (characters.isWhiteSpace()) {
//			writeXmlEvent(characters);
			return eventFactory.createCharacters(characters.getData());
		}
		if (characters.getData().startsWith("<!-- desc_gen:start -->")) { //Obtain an inner CDATA text as HTML elements
			parsedHtmlFragment = Jsoup.parseBodyFragment(characters.getData()).body();
		} else {
//			writeXmlEvent(characters);
			return eventFactory.createCharacters(characters.getData());
		}
		if (multipartDto.isClearDescriptions()) {
			//TODO: to process
		}
		if (multipartDto.isSetPath()) {
			setPath(parsedHtmlFragment, multipartDto.getPath());
		}
		if (multipartDto.isSetPreviewSize()) {
			Integer previewSize = multipartDto.getPreviewSize() == null ? 0 : multipartDto.getPreviewSize();
			setPreviewSize(parsedHtmlFragment, previewSize);
		}
		if (multipartDto.isTrimDescriptions()) { // MUST be the last part of the code
			String trimmedString = parsedHtmlFragment.html().replaceAll("\\s{2,}", "").trim();
			return eventFactory.createCData(trimmedString);
			
		}
		return eventFactory.createCData(parsedHtmlFragment.html());
	}
	
	//TODO: To treat LocusPOI Images started with 'file://'
	
	private void setPath(Element parsedHtmlFragment, String path) {
		Elements aElements = parsedHtmlFragment.select("a[href]");
		Elements imgElements = parsedHtmlFragment.select("img[src]");
		
		final String href = path == null ? "" : path;
		
		aElements.stream()
			.filter(a -> !a.attr("href").startsWith("www.") && !a.attr("href").startsWith("http:"))
			.forEach((a) -> {
				String newPathWithFilename = getNewHrefWithOldFilename(a.attr("href"), href);
				a.attr("href", newPathWithFilename);
			});
		imgElements.stream()
			.filter(img -> !img.attr("src").startsWith("www.") && !img.attr("src").startsWith("http:"))
			.forEach((img) -> {
				String newPathWithFilename = getNewHrefWithOldFilename(img.attr("src"), href);
				img.attr("src", newPathWithFilename);
			});
	}
	
	private String getNewHrefWithOldFilename(String oldHrefWithFilename, String newHrefWithoutFilename) {
//		Each existing a[href] contains a full path with the filename as the last text element.
//		Here we have to replace only the URL and leave the original filename.
		if (!newHrefWithoutFilename.endsWith("/")) {
//			Every new href has to end with '/'
			newHrefWithoutFilename = newHrefWithoutFilename.concat("/");
		}
		int lastIndexOFSlash = oldHrefWithFilename.lastIndexOf("/");
		String filename = oldHrefWithFilename.substring(lastIndexOFSlash + 1, oldHrefWithFilename.length());
		
		return newHrefWithoutFilename.concat(filename);
	}
	
	private void setPreviewSize(Element parsedHtmlFragment, Integer previewSize) {
		Elements imgElements = parsedHtmlFragment.select("img[width]");
		imgElements.forEach(img -> img.attr("width", previewSize.toString() + "px"));
	}
	
	private void validateXml(Document document) throws SAXException, IOException {
		SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Schema schema = schemaFactory.newSchema(
			new File("src/main/resources/static/xsd/kml-2.2.0/ogckml22.xsd"));
		Validator validator = schema.newValidator();
		validator.validate(new DOMSource(document));
	}
}
