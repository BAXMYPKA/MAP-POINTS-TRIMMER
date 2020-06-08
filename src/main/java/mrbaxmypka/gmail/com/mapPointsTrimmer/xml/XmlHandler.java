package mrbaxmypka.gmail.com.mapPointsTrimmer.xml;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mrbaxmypka.gmail.com.mapPointsTrimmer.entitiesDto.MultipartDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
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

@NoArgsConstructor
@Component
public abstract class XmlHandler {
	
	@Getter(value = AccessLevel.PROTECTED)
	protected HtmlHandler htmlHandler;
	
	protected GoogleEarthHandler googleEarthHandler;
	
	@Autowired
	public XmlHandler(HtmlHandler htmlHandler, GoogleEarthHandler googleEarthHandler) {
		this.htmlHandler = htmlHandler;
		this.googleEarthHandler = googleEarthHandler;
	}
	
	/**
	 * {@link DocumentBuilderFactory#setNamespaceAware(boolean)} true is crucial for getting
	 * {@link Node#getLocalName()}!
	 * @param inputStream  An xml file (kml, gpx etc) as the {@link InputStream} from a given {@link MultipartDto}
	 * @param multipartDto
	 * @return
	 */
	public abstract String processXml(InputStream inputStream, MultipartDto multipartDto)
		throws IOException, ParserConfigurationException, SAXException, TransformerException;
	
	protected Document getDocument(InputStream xmlInputStream) throws ParserConfigurationException, IOException, SAXException {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		//IMPORTANT! This is essential part of getting localNames of xml tags.
		documentBuilderFactory.setNamespaceAware(true);
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document document;
		try {
			document = documentBuilder.parse(xmlInputStream);
		} catch (SAXParseException e) {
			if (e.getMessage().contains("The prefix \"lc\" for element \"lc:attachment\" is not bound")) {
				xmlInputStream.reset();
				xmlInputStream = fixNamespaceForLcPrefixMethod(xmlInputStream);
				document = documentBuilder.parse(xmlInputStream);
			} else {
				throw e;
			}
		}
		document.normalizeDocument();
		return document;
	}
	
	/**
	 * @param document {@link Document} to be transformed into String.
	 * @return {@link String} where
	 * 1) All {@link Transformer} linebreaks as "\r\n" are replaced with default "\n"
	 * 2) As Locus may spread orphan signs {@literal ">"} occasionally (especially after {@code <ExtendedData> tag})
	 * before returning the resulting String is checked with "&gt;\t" regexp (or {@code '\\s*>\\s*'} within original
	 * kml for those signs to be deleted.
	 * @throws TransformerException When xml is not valid
	 */
	protected String writeTransformedDocument(Document document) throws TransformerException {
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		DOMSource domSource = new DOMSource(document);
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
//		transformer.setOutputProperty(OutputKeys.CDATA_SECTION_ELEMENTS, "description");
//		transformer.setOutputProperty(OutputKeys.INDENT,  "yes");
		StringWriter stringWriter = new StringWriter();
		Result result = new StreamResult(stringWriter);
		transformer.transform(domSource, result);
		//The first condition checks "&gt;\t" regexp (or {@code '\\s*>\\s*'} within original kml from Locus)
		// as Locus may spread those signs ">" occasionally (especially after {@code <ExtendedData> tag}).
		return stringWriter.toString().replaceAll("\r\n", "\n").replaceAll("&gt;\t", "");
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
		String header = kml.substring(firstHeaderIndex, lastHeaderIndex + 1);
		if (!header.contains("xmlns:lc=\"http://www.locusmap.eu\"")) {
			String newHeader = header.replace(">", " xmlns:lc=\"http://www.locusmap.eu\">\n");
			kml = kml.replace(header, newHeader);
		}
		return new ByteArrayInputStream(kml.getBytes(StandardCharsets.UTF_8));
	}
	
	protected void validateDocument(Document document, File schemaFile) throws SAXException, IOException {
		SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Schema schema = factory.newSchema(schemaFile);
		Validator validator = schema.newValidator();
		validator.validate(new DOMSource(document));
	}
}
