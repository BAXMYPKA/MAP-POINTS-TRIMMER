package mrbaxmypka.gmail.com.mapPointsTrimmer.xml;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import mrbaxmypka.gmail.com.mapPointsTrimmer.entitiesDto.MultipartDto;
import mrbaxmypka.gmail.com.mapPointsTrimmer.services.FileService;
import mrbaxmypka.gmail.com.mapPointsTrimmer.services.GoogleIconsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.*;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public abstract class XmlHandler {
	
	@Getter(value = AccessLevel.PROTECTED)
	private HtmlHandler htmlHandler;
	@Getter(value = AccessLevel.PROTECTED)
	private GoogleIconsService googleIconsService;
	@Getter(value = AccessLevel.PROTECTED)
	private FileService fileService;

	@Autowired
	public XmlHandler(HtmlHandler htmlHandler, GoogleIconsService googleIconsService, FileService fileService) {
		this.htmlHandler = htmlHandler;
		this.googleIconsService = googleIconsService;
		this.fileService = fileService;
	}
	
	/**
	 * {@link DocumentBuilderFactory#setNamespaceAware(boolean)} true is crucial for getting
	 * {@link Node#getLocalName()}!
	 *
	 * @param inputStream  An xml file (kml, gpx etc) as the {@link InputStream} from a given {@link MultipartDto}
	 * @param multipartDto The main object to get data from.
	 * @return A fully processed xml string.
	 */
	public abstract String processXml(InputStream inputStream, MultipartDto multipartDto)
            throws IOException, ParserConfigurationException, SAXException, TransformerException, InterruptedException;
	
	protected Document getDocument(InputStream xmlInputStream) throws ParserConfigurationException, IOException, SAXException {
		log.info("Getting 'document' from InputStream from a MultipartFile...");
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		//IMPORTANT! This is essential part of getting localNames of xml tags.
		documentBuilderFactory.setNamespaceAware(true);
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document document;
		try {
			document = documentBuilder.parse(xmlInputStream);
		} catch (SAXParseException e) {
			//KML exception
			if (e.getMessage().contains("The prefix \"lc\" for element \"lc:attachment\" is not bound")) {
				log.info("The prefix 'lc' for element 'lc:attachment' is not bound within XML file." +
						" 'lc:' namespace will be added into xml header...");
				xmlInputStream.reset();
				xmlInputStream = fixNamespaceForLcPrefixMethod(xmlInputStream);
//				document = documentBuilder.parse(xmlInputStream);
				return getDocument(xmlInputStream);
			} else if (e.getMessage().contains("The prefix \"xsi\" for attribute \"xsi:schemaLocation\" associated with an element type \"Document\" is not bound")) {
				//KML exception
				log.info("The prefix \"xsi\" for attribute \"xsi:schemaLocation\" associated with an element type \"Document\" is not bound." +
						"The attribute \"xsi:schemaLocation\" will be added into xml header...");
				xmlInputStream.reset();
				xmlInputStream = fixXsiSchemaLocationAttributeMethod(xmlInputStream);
//				document = documentBuilder.parse(xmlInputStream);
				return getDocument(xmlInputStream);
			} else {
				throw e;
			}
		}
		document.normalizeDocument();
		log.info("Xml Document has been extracted and normalized.");
		return document;
	}

	/**
	 * Quick and simple transformation of a given {@link Document} to a {@link String} without any setting, as it.
	 * @param document
	 * @return A raw {@link String} without any optimizations, settings, crearFixing etc.
	 * @throws TransformerException
	 */
	protected String getAsString(Document document) throws TransformerException {
		log.info("Getting the quick document to be transformed and written as String...");
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		DOMSource domSource = new DOMSource(document);
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		StringWriter stringWriter = new StringWriter();
		Result result = new StreamResult(stringWriter);
		transformer.transform(domSource, result);
		String xmlResult = stringWriter.toString();
		log.info("The resulting XML String has been written into memory.");
		return xmlResult;
	}

	/**
	 * @param document    {@link Document} to be transformed into String.
	 * @param prettyPrint To format the resulting xml String with indents.
	 * @return {@link String} where
	 * 1) All {@link Transformer} linebreaks as "\r\n" are replaced with default "\n"
	 * 2) As Locus may spread orphan signs {@literal ">"} occasionally (especially after {@code <ExtendedData> tag})
	 * before returning the resulting String is checked with "&gt;\t" regexp (or {@code '\\s*>\\s*'} within original
	 * kml for those signs to be deleted.
	 * @throws TransformerException When xml is not valid
	 */
	protected String writeTransformedDocument(Document document, boolean prettyPrint) throws TransformerException {
		log.info("Getting the resulting document to be transformed and written as String...");
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		DOMSource domSource = new DOMSource(document);
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
//		transformer.setOutputProperty(OutputKeys.CDATA_SECTION_ELEMENTS, "description");
		if (prettyPrint) {
			trimWhitespaces(document); //To delete all the previous whitespaces
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		}
		StringWriter stringWriter = new StringWriter();
		Result result = new StreamResult(stringWriter);
		transformer.transform(domSource, result);
		String xmlResult = clearFix(stringWriter);
		log.info("The resulting XML String has been written into memory. After some cleaning it will be returned...");
		return xmlResult;
	}
	
	/**
	 * This happens when:
	 * {@code kml.contains("<lc:attachment>") &&
	 * (!kml.contains("<ExtendedData xmlns:lc=\"http://www.locusmap.eu\">") ||
	 * !kml.contains("<lc:attachment xmlns:lc=\"http://www.locusmap.eu\">") ||
	 * !kml.contains("xmlns:lc="http://www.locusmap.eu""))}
	 * A really stupid quickfix for GoogleEarth reproducing <ExtendedData> without Locus specific prefix "lc"
	 * with corresponding namespace for the following <lc:attachments> tags
	 *
	 * @param xmlInputStream Initial {@link InputStream} with malformed .kml
	 * @return .kml String as InputStream with the <kml (...) xmlns:lc="http://www.locusmap.eu"></kml>
	 * in the header.
	 */
	private InputStream fixNamespaceForLcPrefixMethod(InputStream xmlInputStream) throws IOException {
		String kml = new String(xmlInputStream.readAllBytes(), StandardCharsets.UTF_8);
		int firstHeaderIndex = kml.indexOf("<kml");
		int lastHeaderIndex = kml.indexOf(">", firstHeaderIndex);
		log.info("The xml header '{}' without 'lc:' prefix is being fixed...", kml.substring(firstHeaderIndex, lastHeaderIndex + 1));
		String header = kml.substring(firstHeaderIndex, lastHeaderIndex + 1);
		if (!header.contains("xmlns:lc=\"http://www.locusmap.eu\"")) {
			String newHeader = header.replace(">", " xmlns:lc=\"http://www.locusmap.eu\">\n");
			kml = kml.replace(header, newHeader);
		}
		log.info("Namespace 'lc:' has been fixed into XML header.");
		return new ByteArrayInputStream(kml.getBytes(StandardCharsets.UTF_8));
	}
	
	/**
	 * This happens when:
	 * {@code kml.contains(<Document xsi:schemaLocation="http://www.opengis.net/kml/2.2 http://schemas.opengis.net/kml/2.2.0/ogckml22.xsd">) &&
	 * !kmlHeader.contains("xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance") for bounding that namespace}
	 * A really stupid quickfix for GoogleEarth reproducing
	 * Document xsi:schemaLocation="http://www.opengis.net/kml/2.2 http://schemas.opengis.net/kml/2.2.0/ogckml22.xsd"
	 * without bounding that namespace.
	 *
	 * @param xmlInputStream Initial {@link InputStream} with malformed .kml
	 * @return .kml String as InputStream with the <kml (...) xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"></kml>
	 * in the header.
	 */
	private InputStream fixXsiSchemaLocationAttributeMethod(InputStream xmlInputStream) throws IOException {
		String kml = new String(xmlInputStream.readAllBytes(), StandardCharsets.UTF_8);
		int firstHeaderIndex = kml.indexOf("<kml");
		int lastHeaderIndex = kml.indexOf(">", firstHeaderIndex);
		log.info("The xml header '{}' without \"xsi:schemaLocation\" attribute is being fixed...", kml.substring(firstHeaderIndex, lastHeaderIndex + 1));
		String header = kml.substring(firstHeaderIndex, lastHeaderIndex + 1);
		if (!header.contains("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"")) {
			String newHeader = header.replace(">", " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n");
			kml = kml.replace(header, newHeader);
		}
		log.info("'xmlns:xsi=' attribute has been fixed into XML header.");
		return new ByteArrayInputStream(kml.getBytes(StandardCharsets.UTF_8));
	}
	
	/**
	 * Deletes all whitespaces between all nodes deep recursively from the given {@link Node}
	 *
	 * @param node The root {@link Node} whose all the children should be cleared from whitespaces between
	 */
	void trimWhitespaces(Node node) {
		NodeList childNodes = node.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node childNode = childNodes.item(i);
			if (childNode.getNodeType() == Node.TEXT_NODE) {
				childNode.setTextContent(childNode.getTextContent().trim());
			}
			trimWhitespaces(childNode);
		}
		log.trace("Whitespaces have been trimmed from KML");
	}
	
	/**
	 * First checks Locus artifacts as the "&gt;\t" regexp (or {@code '\\s*>\\s*'} within original kml)
	 * as Locus may spread those signs ">" occasionally (especially after {@code <ExtendedData> tag}).
	 * Second replaces all the {@link Transformer}'s "\r\n" indents with standard "\r"
	 *
	 * @param rawXml A newly transformed raw Xml.
	 * @return The cleared xml String.
	 */
	private String clearFix(StringWriter rawXml) {
		String xmlResult = rawXml.toString().replaceAll("\r\n", "\n").replaceAll("&gt;\t", "");
		log.info(
				"Clear fix to delete all the unnecessary '>\\t' and replace '\\r\\n' with standard '\\n' has been completed");
		return xmlResult;
	}
	
	protected void validateDocument(Document document, File schemaFile) throws SAXException, IOException {
		SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Schema schema = factory.newSchema(schemaFile);
		Validator validator = schema.newValidator();
		validator.validate(new DOMSource(document));
	}
}
