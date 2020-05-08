package mrbaxmypka.gmail.com.LocusPOIconverter.services;

import mrbaxmypka.gmail.com.LocusPOIconverter.entitiesDto.MultipartDto;
import mrbaxmypka.gmail.com.LocusPOIconverter.klm.HtmlHandler;
import mrbaxmypka.gmail.com.LocusPOIconverter.klm.XmlHandler;
import mrbaxmypka.gmail.com.LocusPOIconverter.utils.PathTypes;
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
import java.io.IOException;
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
		
		multipartDto = new MultipartDto(
			multipartFile,
			false,
			false,
			false,
			false,
			true,
			PathTypes.RELATIVE,
			"new path",
			false,
			null);
		
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
	public void test_Kml_File_Should_Be_Saved_Temporarily_then_Deleted()
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
	public void test_Kml_File_Should_Be_Returned_Same()
		throws IOException, TransformerException, ParserConfigurationException, SAXException, XMLStreamException {
		// GIVEN
		Mockito.when(xmlHandler.processKml(multipartDto)).thenReturn(testKml);
		
		//WHEN
		tmpKmlFile = kmlKmzService.processMultipartDto(multipartDto, null);
		
		//THEN
		assertEquals(testKml, Files.readString(tmpKmlFile, StandardCharsets.UTF_8));
	}
}