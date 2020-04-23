package mrbaxmypka.gmail.com.LocusPOIconverter.klm;

import mrbaxmypka.gmail.com.LocusPOIconverter.entitiesDto.MultipartDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.*;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class XmlHandlerTest {
	
	private XmlHandler xmlHandler = new XmlHandler();
	
	private static MultipartDto multipartDto;
	
	@BeforeAll
	static void beforeAll() throws IOException {
		InputStream inputStream = XmlHandlerTest.class.getClassLoader().getResourceAsStream("TestTrimmedPois.xml");
//		InputStream inputStream = new FileInputStream("TestTrimmedPois.xml");
		MultipartFile multipartFile = new MockMultipartFile(
			"Test POIs", "TestTrimmedPois.xml", null, inputStream);
		multipartDto = new MultipartDto(
			multipartFile, true, true, true, "", 340);
	}
	
	@Test
	@Order(1)
	public void validation_method_can_read_xsd_recources_and_schema() {
		Assertions.assertDoesNotThrow(() -> {
			SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema schema = schemaFactory.newSchema(
				new File(this.getClass().getResource("/static/xml/ogckml23.xsd").getFile()));
		});
	}
	
	@Test
	@Order(2)
	public void validation() throws IOException, ParserConfigurationException, SAXException, XMLStreamException {
		//GIVEN
		
		//WHEN
		
		xmlHandler.treatXml(multipartDto);
	}
}