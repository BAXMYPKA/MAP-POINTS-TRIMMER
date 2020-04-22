package mrbaxmypka.gmail.com.LocusPOIconverter.klm;

import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

@Component
public class XmlHandler {
	
	public void validateXml(Document document) throws SAXException, IOException {
			SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema schema = schemaFactory.newSchema(
				new File(this.getClass().getResource("static/xml/ogckml23.xsd").getFile()));
			Validator validator = schema.newValidator();
			validator.validate(new DOMSource(document));
	}
	
	public void setPath(InputStream kmlInputStream) throws XMLStreamException {
		readXml(kmlInputStream);
	}
	
	public Document getDocument(InputStream kmlInputStream) throws ParserConfigurationException, IOException, SAXException {
		DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document kmlDocument = documentBuilder.parse(kmlInputStream);
		return kmlDocument;
	}
	
	private void readXml(InputStream inputStream) throws XMLStreamException {
		XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
		XMLEventReader eventReader = xmlInputFactory.createXMLEventReader(inputStream);
	}
	
}
