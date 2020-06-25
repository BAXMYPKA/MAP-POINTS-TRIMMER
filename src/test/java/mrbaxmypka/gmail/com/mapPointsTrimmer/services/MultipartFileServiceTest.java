package mrbaxmypka.gmail.com.mapPointsTrimmer.services;

import mrbaxmypka.gmail.com.mapPointsTrimmer.entitiesDto.MultipartDto;
import mrbaxmypka.gmail.com.mapPointsTrimmer.utils.DownloadAs;
import mrbaxmypka.gmail.com.mapPointsTrimmer.xml.GoogleEarthHandler;
import mrbaxmypka.gmail.com.mapPointsTrimmer.xml.HtmlHandler;
import mrbaxmypka.gmail.com.mapPointsTrimmer.xml.KmlHandler;
import mrbaxmypka.gmail.com.mapPointsTrimmer.xml.XmlTestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.MessageSource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class MultipartFileServiceTest {
	
	private static KmlHandler kmlHandler = new KmlHandler(new HtmlHandler(), new GoogleEarthHandler());
	private static MultipartFileService multipartFileService;
	private static MessageSource messageSource;
	private static MultipartDto multipartDto;
	private static Path tmpFile;
	private static String testKml = "<kml>test</kml>";
	;
	private static MultipartFile multipartFile;
	private static Path kmzPath = Paths.get("src/test/java/resources/LocusTestKmz.kmz");
	
	
	@BeforeEach
	public void beforeEach() throws ParserConfigurationException, TransformerException, SAXException,
		IOException, ClassNotFoundException {
		messageSource = Mockito.mock(MessageSource.class);
		Mockito.when(messageSource.getMessage("exception.nullFilename", null, null))
			.thenReturn("Filename cannot be null!");
		
		kmlHandler = Mockito.mock(KmlHandler.class);
		Mockito.when(kmlHandler.processXml(Mockito.any(InputStream.class), Mockito.any(MultipartDto.class))).thenReturn(testKml);
		
		multipartFileService = new MultipartFileService(kmlHandler, messageSource);
		;
		
		multipartFile = new MockMultipartFile(
			"MockKml.kml", "MockKml.kml", null, testKml.getBytes());
		
		multipartDto = new MultipartDto(multipartFile);
		multipartDto.setDownloadAs(DownloadAs.KML);
	}
	
	@AfterEach
	public void afterEach() throws IOException {
		Files.deleteIfExists(tmpFile);
	}
	
	/**
	 * In reality a temporary file has to be deleted by
	 * {@link mrbaxmypka.gmail.com.mapPointsTrimmer.controllers.ShutdownController#shutdownApp(RedirectAttributes, Locale)}
	 * when called. That controller obtains the {@link Path} by {@link MultipartFileService#getTempFile()} and deletes it
	 * in same manner.
	 */
	@Test
	public void kml_File_Should_Be_Saved_Temporarily_then_Deleted()
		throws IOException, TransformerException, ParserConfigurationException, SAXException, ClassNotFoundException {
		// GIVEN
		
		//WHEN
		tmpFile = multipartFileService.processMultipartDto(multipartDto, null);
		
		//THEN
		assertEquals(tmpFile, multipartFileService.getTempFile());
		assertTrue(Files.isReadable(tmpFile));
		
		Files.deleteIfExists(tmpFile);
		
		//Temp .kml file should be deleted
		assertFalse(Files.isReadable(tmpFile));
	}
	
	@Test
	public void kml_File_Should_Be_Returned_Same()
		throws IOException, TransformerException, ParserConfigurationException, SAXException, ClassNotFoundException {
		// GIVEN
		
		//WHEN
		tmpFile = multipartFileService.processMultipartDto(multipartDto, null);
		
		//THEN
		assertEquals(testKml, Files.readString(tmpFile, StandardCharsets.UTF_8));
	}
	
	/**
	 * The given 'LocusTestKmz.kmz' contains 'doc.kml'
	 */
	@Test
	public void downloadAsKml_From_Kmz_File_Should_Be_Extracted_And_Saved_as_Kml()
		throws IOException, ParserConfigurationException, TransformerException, SAXException, ClassNotFoundException {
		//GIVEN
		multipartFile = new MockMultipartFile(
			"LocusTestKmz.kmz",
			"LocusTestKmz.kmz",
			null,
			new FileInputStream(new File("src/test/java/resources/LocusTestKmz.kmz")));
		multipartDto = new MultipartDto(multipartFile);
		multipartDto.setDownloadAs(DownloadAs.KML);
		
		kmlHandler = new KmlHandler(new HtmlHandler(), new GoogleEarthHandler());
		multipartFileService = new MultipartFileService(kmlHandler, messageSource);
		
		//WHEN .kmz is fully processed without Mocks and additional conditions
		tmpFile = multipartFileService.processMultipartDto(multipartDto, null);
		String kmlResult = Files.readString(tmpFile, StandardCharsets.UTF_8);
//		System.out.println(kmlResult);
		
		//THEN The resulting 'doc.kml' should be the same
		assertAll(
			() -> assertTrue(kmlResult.contains("<Style id=\"file:///sdcard/Locus/cache/images/1571471453728\">")),
			() -> assertTrue(kmlResult.contains("<name>Locus17.04.2020</name>")),
			() -> assertTrue(kmlResult.contains("<name>2015-04-10 14:27:17</name>"))
		);
	}
	
	@Test
	public void downloadAsKmz_From_Kml_File_Should_Be_Saved_as_Kml()
		throws IOException, ParserConfigurationException, TransformerException, SAXException, ClassNotFoundException {
		//GIVEN If while uploading KML set "downloadAs KMZ"
		multipartDto.setDownloadAs(DownloadAs.KMZ);
		
		kmlHandler = new KmlHandler(new HtmlHandler(), new GoogleEarthHandler());
		multipartFileService = new MultipartFileService(kmlHandler, messageSource);
		
		//WHEN .kmz is fully processed without Mocks and additional conditions
		tmpFile = multipartFileService.processMultipartDto(multipartDto, null);
		
		//THEN
		assertTrue(tmpFile.getFileName().toString().endsWith(".kml"));
	}
	
	@Test
	public void kmz_File_Should_Be_Saved_Temporarily_With_The_Filename_From_Multipart_Then_Deleted()
		throws IOException, ParserConfigurationException, TransformerException, SAXException, ClassNotFoundException {
		//GIVEN
		String initialMultipartFileName = "LocusTestKmz.kmz";
		multipartFile = new MockMultipartFile(
			initialMultipartFileName,
			initialMultipartFileName,
			null,
			new FileInputStream(new File("src/test/java/resources/LocusTestKmz.kmz")));
		multipartDto = new MultipartDto(multipartFile);
		multipartDto.setDownloadAs(DownloadAs.KMZ);
		
		//WHEN
		tmpFile = multipartFileService.processMultipartDto(multipartDto, null);
		
		//THEN
		assertEquals(initialMultipartFileName, tmpFile.getFileName().toString());
		
		Files.delete(tmpFile);
		
		assertFalse(Files.exists(tmpFile));
	}
	
	
	@Test
	public void kml_In_Kmz_MultipartFile_Should_Be_Returned_Same()
		throws IOException, TransformerException, ParserConfigurationException, SAXException, ClassNotFoundException {
		//GIVEN download as "KMZ"
		multipartFile = new MockMultipartFile(
			"MockKml.kmz", "MockKml.kmz", null, Files.readAllBytes(kmzPath));
		multipartDto = new MultipartDto(multipartFile);
		multipartDto.setDownloadAs(DownloadAs.KMZ);
		
		//WHEN kmz is returned we extract the processed kml from it
		tmpFile = multipartFileService.processMultipartDto(multipartDto, null);
		String processedKml = null;
		FileSystem zip = FileSystems.newFileSystem(tmpFile, this.getClass().getClassLoader());
		rootDir:
		for (Path rootPath : zip.getRootDirectories()) {
			for (Path zipPath : Files.walk(rootPath).collect(Collectors.toCollection(ArrayList::new))) {
				if (Files.isRegularFile(zipPath) && DownloadAs.KML.hasSameExtension(zipPath.getFileName().toString())) {
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
		throws IOException, TransformerException, ParserConfigurationException, SAXException, ClassNotFoundException {
		//GIVEN when "download as KMZ" is selected all the initial files (images) from "LocusTestKmz.kmz"
		// should be preserved
		multipartFile = new MockMultipartFile(
			"MockKml.kmz", "MockKml.kmz", null, Files.readAllBytes(kmzPath));
		multipartDto = new MultipartDto(multipartFile);
		multipartDto.setDownloadAs(DownloadAs.KMZ);
		
		List<Path> initialFiles = new ArrayList<>();
		
		//Walk through initial kmz file to collect all the files inside
		FileSystem initialZip = FileSystems.newFileSystem(kmzPath, this.getClass().getClassLoader());
		initialZip.getRootDirectories().forEach(rootPath -> {
			
			try {
				Files.walk(rootPath).forEach(path -> {
					if (path.getFileName() != null) initialFiles.add(path);
				});
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		
		//WHEN look through the resulting kmz we filter out only files with the same name and parent
		//hen delete them from initial files collection to see that all the initial files were included into the
		//resulting kmz
		tmpFile = multipartFileService.processMultipartDto(multipartDto, null);
		FileSystem resultingZip = FileSystems.newFileSystem(tmpFile, this.getClass().getClassLoader());
		
		for (Path rootPath : resultingZip.getRootDirectories()) {
			for (Path tempPath : Files.walk(rootPath).collect(Collectors.toList())) {
				if (tempPath.getFileName() == null) continue;
				
				long count = initialFiles.stream()
					.filter(initPath -> initPath.getFileName().toString().equals(tempPath.getFileName().toString()))
					.filter(initPath -> initPath.getParent().toString().equals(tempPath.getParent().toString()))
					.count();
				
				assertEquals(1, count);
				initialFiles.removeIf(next -> next.getFileName().toString().equals(tempPath.getFileName().toString()));
			}
		}
		
		//THEN initial files collection doesn't contain more files
		assertTrue(initialFiles.isEmpty());
	}
	
	
}