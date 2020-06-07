package mrbaxmypka.gmail.com.mapPointsTrimmer.services;

import mrbaxmypka.gmail.com.mapPointsTrimmer.entitiesDto.MultipartDto;
import mrbaxmypka.gmail.com.mapPointsTrimmer.utils.FileTypes;
import mrbaxmypka.gmail.com.mapPointsTrimmer.xml.GoogleEarthHandler;
import mrbaxmypka.gmail.com.mapPointsTrimmer.xml.HtmlHandler;
import mrbaxmypka.gmail.com.mapPointsTrimmer.xml.KmlHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.MessageSource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Locale;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class KmlKmzServiceTest {
	
	private static KmlHandler kmlHandler = new KmlHandler(new HtmlHandler(), new GoogleEarthHandler());
	private static KmlKmzService kmlKmzService;
	private static MessageSource messageSource;
	private static MultipartDto multipartDto;
	private static Path tmpFile;
	private static String testKml = "<kml>test</kml>";
	;
	private static MultipartFile multipartFile;
	private static Path kmzPath = Paths.get("src/test/java/resources/LocusTestKmz.kmz");
	
	
	@BeforeAll
	public static void beforeAll() throws ParserConfigurationException, TransformerException, SAXException, IOException {
		messageSource = Mockito.mock(MessageSource.class);
		Mockito.when(messageSource.getMessage("exception.nullFilename", null, null))
			.thenReturn("Filename cannot be null!");
		
		kmlHandler = Mockito.mock(KmlHandler.class);
		Mockito.when(kmlHandler.processXml(Mockito.any(InputStream.class), Mockito.any(MultipartDto.class))).thenReturn(testKml);
		
		kmlKmzService = new KmlKmzService(kmlHandler, messageSource);
		;
		
		multipartFile = new MockMultipartFile(
			"MockKml.kml", "MockKml.kml", null, testKml.getBytes());
		
		multipartDto = new MultipartDto(multipartFile);
		multipartDto.setDownloadAs(FileTypes.KML);
	}
	
	@AfterEach
	public void afterEach() throws IOException {
		Files.deleteIfExists(tmpFile);
	}
	
	/**
	 * In reality a temporary file has to be deleted by
	 * {@link mrbaxmypka.gmail.com.mapPointsTrimmer.controllers.ShutdownController#shutdownApp(RedirectAttributes, Locale)}
	 * when called. That controller obtains the {@link Path} by {@link KmlKmzService#getTempFile()} and deletes it
	 * in same manner.
	 */
	@Test
	public void kml_File_Should_Be_Saved_Temporarily_then_Deleted()
		throws IOException, TransformerException, ParserConfigurationException, SAXException {
		// GIVEN
		Mockito.when(kmlHandler.processXml(Mockito.any(InputStream.class), Mockito.any(MultipartDto.class))).thenReturn(testKml);
		
		//WHEN
		tmpFile = kmlKmzService.processMultipartDto_2(multipartDto, null);
		
		//THEN
		assertEquals(tmpFile, kmlKmzService.getTempFile());
		assertTrue(Files.isReadable(tmpFile));
		
		Files.deleteIfExists(tmpFile);
		
		//Temp .kml file should be deleted
		assertFalse(Files.isReadable(tmpFile));
	}
	
	@Test
	public void kml_File_Should_Be_Returned_Same()
		throws IOException, TransformerException, ParserConfigurationException, SAXException {
		// GIVEN
		Mockito.when(kmlHandler.processXml(Mockito.any(InputStream.class), Mockito.any(MultipartDto.class))).thenReturn(testKml);
		
		//WHEN
		tmpFile = kmlKmzService.processMultipartDto(multipartDto, null);
		
		//THEN
		assertEquals(testKml, Files.readString(tmpFile, StandardCharsets.UTF_8));
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
		Mockito.when(kmlHandler.processXml(Mockito.any(InputStream.class), Mockito.any(MultipartDto.class))).thenReturn(kmlTest);
		
		//WHEN
		tmpFile = kmlKmzService.processMultipartDto(multipartDto, null);
		String kmlResult = Files.readString(tmpFile, StandardCharsets.UTF_8);
		
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
		kmlHandler = new KmlHandler(new HtmlHandler(), new GoogleEarthHandler());
		kmlKmzService = new KmlKmzService(kmlHandler, messageSource);
		
		//WHEN .kmz is fully processed without Mocks and additional conditions
		tmpFile = kmlKmzService.processMultipartDto(multipartDto, null);
		String kmlResult = Files.readString(tmpFile, StandardCharsets.UTF_8);
		
		//THEN The resulting 'doc.kml' should be the same
		assertAll(
			() -> assertTrue(kmlResult.startsWith("<?xml version=\"1.0\" encoding=\"utf-8\"")),
			() -> assertTrue(kmlResult.endsWith("</Placemark>\n" +
				"</Document>\n" +
				"</kml>"))
		);
	}
	
	@Test
	public void downloadAsKmz_File_Should_Be_Saved_Temporarily_then_Deleted()
		throws IOException, TransformerException, ParserConfigurationException, SAXException {
		// GIVEN kmz file
		multipartFile = new MockMultipartFile(
			"MockKml.kmz", "MockKml.kmz", null, Files.readAllBytes(kmzPath));
		multipartDto = new MultipartDto(multipartFile);
		multipartDto.setDownloadAs(FileTypes.KMZ);
		
		//WHEN
		tmpFile = kmlKmzService.processMultipartDto_2(multipartDto, null);
		
		//THEN
		assertEquals(tmpFile, kmlKmzService.getTempFile());
		assertTrue(Files.isReadable(tmpFile));
		
		Files.deleteIfExists(tmpFile);
		
		//Temp .kml file should be deleted
		assertFalse(Files.isReadable(tmpFile));
	}
	
	@Test
	public void kml_In_Kmz_MultipartFile_Should_Be_Returned_Same()
		throws IOException, TransformerException, ParserConfigurationException, SAXException {
		//WHEN
		multipartFile = new MockMultipartFile(
			"MockKml.kmz", "MockKml.kmz", null, Files.readAllBytes(kmzPath));
		multipartDto = new MultipartDto(multipartFile);
		multipartDto.setDownloadAs(FileTypes.KMZ);
		
		//WHEN
		tmpFile = kmlKmzService.processMultipartDto_2(multipartDto, null);
		String processedKml = null;
		FileSystem zip = FileSystems.newFileSystem(tmpFile, this.getClass().getClassLoader());
		rootDir:
		for (Path rootPath : zip.getRootDirectories()) {
			for (Path zipPath : Files.walk(rootPath).collect(Collectors.toCollection(ArrayList::new))) {
				if (Files.isRegularFile(zipPath) && FileTypes.KML.hasSameExtension(zipPath.getFileName().toString())) {
					processedKml = String.join("", Files.readAllLines(zipPath, StandardCharsets.UTF_8));
					break rootDir;
				}
			}
		}
		
		//THEN
		assertEquals(processedKml, testKml);
	}
	
	@Test
	public void kmz_MultipartFile_Should_Contain_All_The_Initial_Files()
		throws IOException, TransformerException, ParserConfigurationException, SAXException {
		//WHEN
		multipartFile = new MockMultipartFile(
			"MockKml.kmz", "MockKml.kmz", null, Files.readAllBytes(kmzPath));
		multipartDto = new MultipartDto(multipartFile);
		multipartDto.setDownloadAs(FileTypes.KMZ);
		
		//WHEN
		tmpFile = kmlKmzService.processMultipartDto_2(multipartDto, null);
		String processedKml = null;
		FileSystem zip = FileSystems.newFileSystem(tmpFile, this.getClass().getClassLoader());
		rootDir:
		for (Path rootPath : zip.getRootDirectories()) {
			for (Path zipPath : Files.walk(rootPath).collect(Collectors.toCollection(ArrayList::new))) {
				if (Files.isRegularFile(zipPath) && FileTypes.KML.hasSameExtension(zipPath.getFileName().toString())) {
					processedKml = String.join("", Files.readAllLines(zipPath, StandardCharsets.UTF_8));
					break rootDir;
				}
			}
		}
		
		//THEN
		assertEquals(processedKml, testKml);
	}
	
	//TODO: to test out the deleting of temp file when close unexpectedly
}