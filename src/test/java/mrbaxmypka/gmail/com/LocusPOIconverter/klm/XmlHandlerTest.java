package mrbaxmypka.gmail.com.LocusPOIconverter.klm;

import mrbaxmypka.gmail.com.LocusPOIconverter.entitiesDto.MultipartDto;
import org.junit.jupiter.api.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerException;
import java.io.*;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class XmlHandlerTest {
	
	private static InputStream inputStream;
	private static MultipartDto multipartDto;
	private static MultipartFile multipartFile;
	private XmlHandler xmlHandler = new XmlHandler();
	
//	@BeforeAll
	static void beforeAll() throws IOException {
		inputStream = new FileInputStream("src/test/java/resources/LocusTestPois.kml");
//		inputStream = XmlHandlerTest.class.getClassLoader().getResourceAsStream("TestTrimmedPois.xml");
		multipartFile = new MockMultipartFile(
			"TestTrimmedPois.kml", "TestTrimmedPois.kml", null, inputStream);
		multipartDto = new MultipartDto(
			multipartFile, true, true, true, "", 340);
		
	}
	
	@Test
	@Disabled
	public void valid_LocusPOI_Kml_Should_Be_Validated() throws IOException {
		//GIVEN Only to validate xml
		inputStream = new FileInputStream("src/test/java/resources/LocusTestPois.kml");
		multipartFile = new MockMultipartFile(
			"LocusTestPois.kml", "LocusTestPois.kml", null, inputStream);
		multipartDto = new MultipartDto(
			multipartFile, false, true, false, null, null);
		
		//WHEN
		
		Assertions.assertDoesNotThrow(() -> xmlHandler.processKml(multipartDto));
	}
	
	@Test
	public void setPath_Should_Replace_All_Hrefs()
		throws IOException, ParserConfigurationException, SAXException, XMLStreamException, TransformerException {
		//GIVEN
		String newPath = "/A NEW PATH/";
		inputStream = new FileInputStream("src/test/java/resources/LocusTestPois.kml");
		multipartFile = new MockMultipartFile(
			"LocusTestPois.kml", "LocusTestPois.kml", null, inputStream);
		multipartDto = new MultipartDto(
			multipartFile, false, false, true, newPath, null);
		
		//WHEN
		
		xmlHandler.processKml(multipartDto);
		
		//THEN
		
		
	}
}