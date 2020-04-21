package mrbaxmypka.gmail.com.LocusPOIconverter.klm;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

@Component
public class XmlValidator {
	
	public Document obtainDocument(InputStream kml) {
		Document kmlDocument = null;
		try {
			DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			kmlDocument = documentBuilder.parse(kml);
		} catch (ParserConfigurationException | IOException | SAXException e) {
			e.printStackTrace();
		}
		return kmlDocument;
	}
	
	public void validateKml(Document document) {
		try {
			SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema schema = schemaFactory.newSchema(
				new File(this.getClass().getResource("static/xml/ogckml23.xsd").getFile()));
			Validator validator = schema.newValidator();
			validator.validate(new DOMSource(document));
		} catch (SAXException | IOException e) {
			e.printStackTrace();
		}
	}
}
