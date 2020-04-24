package mrbaxmypka.gmail.com.LocusPOIconverter.klm;

import mrbaxmypka.gmail.com.LocusPOIconverter.entitiesDto.MultipartDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import java.io.*;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class XmlHandlerTest {
	
	private static InputStream inputStream;
	private static MultipartDto multipartDto;
	private static MultipartFile multipartFile;
	private XmlHandler xmlHandler = new XmlHandler();
	
	@BeforeAll
	static void beforeAll() throws IOException {
		inputStream = new FileInputStream("src/test/java/resources/TestTrimmedPois.kml");
//		inputStream = XmlHandlerTest.class.getClassLoader().getResourceAsStream("TestTrimmedPois.xml");
		multipartFile = new MockMultipartFile(
			"TestTrimmedPois.kml", "TestTrimmedPois.kml", null, inputStream);
		multipartDto = new MultipartDto(
			multipartFile, true, true, true, "", 340);
		
	}
	
	@Test
	@Order(1)
	public void valid_Kml_Should_Be_Validated() {
		//GIVEN Only to validate xml
		
		multipartDto = new MultipartDto(
			multipartFile, false, true, false, null, null);
		
		//WHEN
		
		Assertions.assertDoesNotThrow(() -> xmlHandler.treatXml(multipartDto));
	}
	
	@Test
	@Order(2)
	public void validation() throws IOException, ParserConfigurationException, SAXException, XMLStreamException, TransformerConfigurationException {
		//GIVEN
		
		multipartDto = new MultipartDto(
			multipartFile, false, true, false, null, null);
		
		//WHEN
		

	}
}