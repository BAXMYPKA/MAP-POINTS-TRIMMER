package mrbaxmypka.gmail.com.LocusPOIconverter.services;

import mrbaxmypka.gmail.com.LocusPOIconverter.entitiesDto.MultipartDto;
import mrbaxmypka.gmail.com.LocusPOIconverter.klm.HtmlHandler;
import mrbaxmypka.gmail.com.LocusPOIconverter.klm.XmlHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.MessageSource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class KmlKmzServiceTest {
	
	private static XmlHandler xmlHandler = new XmlHandler(new HtmlHandler());
	private static KmlKmzService kmlKmzService;
	private static MessageSource messageSource;
	private static MultipartDto multipartDto;
	private static Path tmpKmlFile;
	private static String testKml;
	private static MultipartFile multipartFile;
	
	@BeforeAll
	public static void beforeAll() {
		messageSource = Mockito.mock(MessageSource.class);
		Mockito.when(messageSource.getMessage("exception.nullFilename", null, null))
			.thenReturn("Filename cannot be null!");
		xmlHandler = Mockito.mock(XmlHandler.class);
		kmlKmzService = new KmlKmzService(xmlHandler, messageSource);
		testKml = "<kml>test</kml>";
		multipartFile = new MockMultipartFile(
			"MockKml.kml", "MockKml.kml", null, testKml.getBytes());
		multipartDto = new MultipartDto(multipartFile);
	}
	
	@AfterEach
	public void afterEach() throws IOException {
		Files.deleteIfExists(tmpKmlFile);
	}
	
	/**
	 * In reality a temporary file has to be deleted by
	 * {@link mrbaxmypka.gmail.com.LocusPOIconverter.controllers.ShutdownController#shutdownApp(Model, Locale)}
	 * when called. That controller obtains the {@link Path} by {@link KmlKmzService#getTempKmlFile()} and deletes it
	 * in same manner.
	 */
	@Test
	public void kml_File_Should_Be_Saved_Temporarily_then_Deleted()
		throws IOException, TransformerException, ParserConfigurationException, SAXException, XMLStreamException {
		// GIVEN
		Mockito.when(xmlHandler.processKml(multipartDto)).thenReturn(testKml);
		
		//WHEN
		tmpKmlFile = kmlKmzService.processMultipartDto(multipartDto, null);
		
		//THEN
		assertEquals(tmpKmlFile, kmlKmzService.getTempKmlFile());
		assertTrue(Files.isReadable(tmpKmlFile));
		
		Files.deleteIfExists(tmpKmlFile);
		
		//Temp .kml file should be deleted
		assertFalse(Files.isReadable(tmpKmlFile));
	}
	
	@Test
	public void kml_File_Should_Be_Returned_Same()
		throws IOException, TransformerException, ParserConfigurationException, SAXException, XMLStreamException {
		// GIVEN
		Mockito.when(xmlHandler.processKml(multipartDto)).thenReturn(testKml);
		
		//WHEN
		tmpKmlFile = kmlKmzService.processMultipartDto(multipartDto, null);
		
		//THEN
		assertEquals(testKml, Files.readString(tmpKmlFile, StandardCharsets.UTF_8));
	}
	
	@Test
	public void kmz_File_Should_Be_Recognized_And_Extracted()
		throws IOException, ParserConfigurationException, TransformerException, SAXException, XMLStreamException {
		//GIVEN
		InputStream kmzInputStream = new FileInputStream(new File("src/test/java/resources/LocusTestKmz.kmz"));
		MultipartFile multipartFileWIthKmz = new MockMultipartFile(
			"LocusTestKmz", "LocusTestKmz.kmz", null, kmzInputStream);
		multipartDto = new MultipartDto(multipartFileWIthKmz);
		
		String kmlTest = "<kml>success</kml>";
		Mockito.when(xmlHandler.processKml(multipartDto)).thenReturn(kmlTest);
		
		//WHEN
		tmpKmlFile = kmlKmzService.processMultipartDto(multipartDto, null);
		String kmlResult = Files.readString(tmpKmlFile, StandardCharsets.UTF_8);
		
		//THEN
		assertAll(
			() -> assertEquals(kmlTest, kmlResult)
		);
	}
	
	/**
	 * The given 'LocusTestKmz.kmz' contains 'doc.kml'
	 */
	@Test
	public void kmz_File_Should_Be_Extracted_as_Kml_And_Processed()
		throws IOException, ParserConfigurationException, TransformerException, SAXException, XMLStreamException {
		//GIVEN
		InputStream kmzInputStream = new FileInputStream(new File("src/test/java/resources/LocusTestKmz.kmz"));
		MultipartFile multipartFileWIthKmz = new MockMultipartFile(
			"LocusTestKmz", "LocusTestKmz.kmz", null, kmzInputStream);
		multipartDto = new MultipartDto(multipartFileWIthKmz);
		xmlHandler = new XmlHandler(new HtmlHandler());
		kmlKmzService = new KmlKmzService(xmlHandler, messageSource);
		
		//WHEN .kmz is fully processed without Mocks and additional conditions
		tmpKmlFile = kmlKmzService.processMultipartDto(multipartDto, null);
		String kmlResult = Files.readString(tmpKmlFile, StandardCharsets.UTF_8);
		
		//THEN The resulting 'doc.kml' should be the same
		assertAll(
			() -> assertTrue(kmlResult.startsWith("<?xml version=\"1.0\" encoding=\"utf-8\"?>")),
			() -> assertTrue(kmlResult.endsWith("</Placemark>\n" +
				"</Document>\n" +
				"</kml>"))
		);
	}
}