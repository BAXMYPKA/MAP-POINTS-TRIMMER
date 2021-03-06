package mrbaxmypka.gmail.com.mapPointsTrimmer.services;

import mrbaxmypka.gmail.com.mapPointsTrimmer.entitiesDto.MultipartDto;
import mrbaxmypka.gmail.com.mapPointsTrimmer.utils.DownloadAs;
import mrbaxmypka.gmail.com.mapPointsTrimmer.utils.GoogleIconsCache;
import mrbaxmypka.gmail.com.mapPointsTrimmer.utils.PathTypes;
import mrbaxmypka.gmail.com.mapPointsTrimmer.xml.HtmlHandler;
import mrbaxmypka.gmail.com.mapPointsTrimmer.xml.KmlHandler;
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
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.*;

class MultipartFileServiceTest {
	
	private KmlHandler mockKmlHandler;
	private FileService mockFileService;
	private GoogleIconsCache googleIconsCache;
	private FileService fileService;
	private HtmlHandler htmlHandler;
	private MultipartFileService multipartFileService;
	private MessageSource mockMessageSource;
	private MultipartDto multipartDto;
	private Path tmpFile;
	private String originalKmlFilename = "MockKml.kml";
	private String testKml = "<kml>test</kml>";
	private MultipartFile multipartFile;
	private Path testKmz = Paths.get("src/test/java/resources/TestKmz.kmz");
	
	
	@BeforeEach
	public void beforeEach() throws ParserConfigurationException, TransformerException, SAXException,
			IOException, ClassNotFoundException {
		mockMessageSource = Mockito.mock(MessageSource.class);
		Mockito.when(mockMessageSource.getMessage("exception.nullFilename", null, null))
				.thenReturn("Filename cannot be null!");
		
		mockKmlHandler = Mockito.mock(KmlHandler.class);
		Mockito.when(mockKmlHandler.processXml(Mockito.any(InputStream.class), Mockito.any(MultipartDto.class))).thenReturn(testKml);
		
		mockFileService = Mockito.mock(FileService.class);
		
		multipartFileService = new MultipartFileService(mockKmlHandler, mockFileService, mockMessageSource);
		
		googleIconsCache = new GoogleIconsCache();
		
		fileService = new FileService();
		
		htmlHandler = new HtmlHandler(fileService);
		
		multipartFile = new MockMultipartFile(originalKmlFilename, originalKmlFilename, null, testKml.getBytes());
		
		multipartDto = new MultipartDto(multipartFile);
		multipartDto.setDownloadAs(DownloadAs.KML);
	}
	
	@AfterEach
	public void afterEach() {
		try {
			Files.deleteIfExists(tmpFile);
		} catch (IOException | NullPointerException e) {
//			e.printStackTrace();
		}
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
			throws IOException, ParserConfigurationException, TransformerException, SAXException {
		//GIVEN
		multipartFile = new MockMultipartFile(
				"TestKmz.kmz",
				"TestKmz.kmz",
				null,
				new FileInputStream(new File("src/test/java/resources/TestKmz.kmz")));
		multipartDto = new MultipartDto(multipartFile);
		multipartDto.setDownloadAs(DownloadAs.KML);
		
		KmlHandler kmlHandler = new KmlHandler(
				new HtmlHandler(fileService), new GoogleIconsService(googleIconsCache), fileService);
		multipartFileService = new MultipartFileService(kmlHandler, fileService, mockMessageSource);
		
		//WHEN .kmz is fully processed without Mocks and additional conditions
		tmpFile = multipartFileService.processMultipartDto(multipartDto, null);
		String kmlResult = Files.readString(tmpFile, StandardCharsets.UTF_8);
//		System.out.println(kmlResult);
		
		//THEN The resulting 'doc.kml' should be the same
		assertAll(
				() -> assertTrue(kmlResult.contains("<Style id=\"transport-bus-local\">")),
				() -> assertTrue(kmlResult.contains("<name>Test KMZ</name>")),
				() -> assertTrue(kmlResult.contains("<name>Placemark 1</name>"))
		);
	}
	
	@Test
	public void downloadAsKmz_From_Kml_File_Should_Be_Saved_as_Kmz_With_Original_Filename()
			throws IOException, ParserConfigurationException, TransformerException, SAXException {
		//GIVEN If while uploading KML set "downloadAs KMZ"
		multipartDto.setDownloadAs(DownloadAs.KMZ);
		
		KmlHandler kmlHandler = new KmlHandler(new HtmlHandler(fileService), new GoogleIconsService(googleIconsCache), fileService);
		multipartFileService = new MultipartFileService(kmlHandler, fileService, mockMessageSource);
		
		//WHEN .kmz is fully processed without Mocks and additional conditions
		tmpFile = multipartFileService.processMultipartDto(multipartDto, null);
		
		//THEN .kmz should be a zip file with the single entity as 'doc.kml'
		assertTrue(tmpFile.getFileName().toString().endsWith(".kmz"));
		assertTrue(tmpFile.getFileName().toString().startsWith(originalKmlFilename.substring(0, originalKmlFilename.lastIndexOf("."))));
		assertDoesNotThrow(() -> {
			ZipInputStream zis = new ZipInputStream(Files.newInputStream(tmpFile));
			ZipEntry zipEntry = zis.getNextEntry();
			assertEquals("doc.kml", zipEntry.getName());
		});
	}
	
	@Test
	public void kmz_File_Should_Be_Saved_Temporarily_With_The_Filename_From_Multipart_Then_Deleted()
			throws IOException, ParserConfigurationException, TransformerException, SAXException {
		//GIVEN
		String initialMultipartFileName = "TestKmz.kmz";
		multipartFile = new MockMultipartFile(
				initialMultipartFileName,
				initialMultipartFileName,
				null,
				new FileInputStream(new File("src/test/java/resources/TestKmz.kmz")));
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
			throws IOException, TransformerException, ParserConfigurationException, SAXException {
		//GIVEN download as "KMZ"
		multipartFile = new MockMultipartFile(
				"MockKml.kmz", "MockKml.kmz", null, Files.readAllBytes(testKmz));
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
			throws IOException, TransformerException, ParserConfigurationException, SAXException {
		//GIVEN when "download as KMZ" is selected all the initial files (images) from "LocusTestKmz.kmz"
		// should be preserved
		multipartFile = new MockMultipartFile(
				"MockKml.kmz", "MockKml.kmz", null, Files.readAllBytes(testKmz));
		multipartDto = new MultipartDto(multipartFile);
		multipartDto.setDownloadAs(DownloadAs.KMZ);
		
		List<Path> initialFiles = new ArrayList<>();
		
		//Walk through initial kmz file to collect all the files inside
		FileSystem initialZip = FileSystems.newFileSystem(testKmz, this.getClass().getClassLoader());
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
	
	@Test
	public void new_Kmz_With_Icons_Should_Be_Created_For_Downloaded_Icons_From_Kml()
			throws IOException, TransformerException, SAXException, ParserConfigurationException {
		//GIVEN
		multipartDto = new MultipartDto(new MockMultipartFile(
				"Test.kml", "Test.kml", null, testKml.getBytes(StandardCharsets.UTF_8)));
		multipartDto.setDownloadAs(DownloadAs.KMZ);
		//This should be done by GoogleIconsService
		multipartDto.getGoogleIconsToBeZipped().put("parks.png", new byte[]{12, 12, 123});
		
		Mockito.when(mockKmlHandler.processXml(Mockito.any(InputStream.class), Mockito.any(MultipartDto.class))).thenReturn(testKml);
		
		//WHEN
		tmpFile = multipartFileService.processMultipartDto(multipartDto, null);
		
		//THEN
		assertAll(
				() -> assertTrue(Files.isReadable(tmpFile)),
				() -> assertEquals("Test.kmz", tmpFile.getFileName().toString())
		);
		
		List<String> zipEntriesNames = new ArrayList<>(3);
		
		assertDoesNotThrow(() -> {
			ZipInputStream zis = new ZipInputStream(Files.newInputStream(tmpFile));
			ZipEntry zipEntry;
			while ((zipEntry = zis.getNextEntry()) != null) {
				zipEntriesNames.add(zipEntry.getName());
			}
		});
		
		assertTrue(zipEntriesNames.contains("doc.kml"));
		assertTrue(zipEntriesNames.contains("files/parks.png"));
	}
	
	/**
	 * WARNING! This test requires fast Internet connection otherwise it will fail.
	 * Also this test should be the integration test but...
	 */
	@Test
	public void kml_With_Additional_GoogleMaps_Icons_Should_Be_Downloaded_And_Added_Into_Kmz()
			throws ParserConfigurationException, TransformerException, SAXException, IOException {
		//GIVEN .kml with Google Maps icons hrefs
		String kmlWithIcons = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
				"<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" +
				"<Document>\n" +
				"\t<name>Test KMZ</name>\n" +
				"\t<atom:author><atom:name>Locus (Android)</atom:name></atom:author>\n" +
				"\t<Style id=\"cabs-remote\">\n" +
				"\t\t<IconStyle>\n" +
				"\t\t\t<Icon><href>http://maps.google.com/mapfiles/kml/shapes/cabs.png</href></Icon>\n" +
				"\t\t\t<hotSpot x=\"0.5\" y=\"0.0\" xunits=\"fraction\" yunits=\"fraction\" />\n" +
				"\t\t</IconStyle>\n" +
				"\t</Style>\n" +
				"\t<Style id=\"parks-remote\">\n" +
				"\t\t<IconStyle>\n" +
				"\t\t\t<Icon><href>http://maps.google.com/mapfiles/kml/shapes/parks.png</href></Icon>\n" +
				"\t\t\t<hotSpot x=\"0.5\" y=\"0.0\" xunits=\"fraction\" yunits=\"fraction\" />\n" +
				"\t\t</IconStyle>\n" +
				"\t</Style>\n" +
				"<Placemark>\n" +
				"\t<name>Placemark 1</name>\n" +
				"\t<styleUrl>#transport-bus-local</styleUrl>\n" +
				"</Placemark>\n" +
				"<Placemark>\n" +
				"\t<name>Placemark 2</name>\n" +
				"</Placemark>\n" +
				"</Document>\n" +
				"</kml>";
		multipartDto = new MultipartDto(new MockMultipartFile(
				"Test.kml", "Test.kml", null, kmlWithIcons.getBytes(StandardCharsets.UTF_8)));
		multipartDto.setDownloadAs(DownloadAs.KMZ);
		
		multipartFileService = new MultipartFileService(
				new KmlHandler(new HtmlHandler(fileService), new GoogleIconsService(googleIconsCache), fileService),
				fileService,
				null);
		
		//WHEN
		tmpFile = multipartFileService.processMultipartDto(multipartDto, null);
		
		//THEN
		assertEquals("Test.kmz", tmpFile.getFileName().toString());
		
		List<String> zipEntriesNames = new ArrayList<>(3);
		
		assertDoesNotThrow(() -> {
			ZipInputStream zis = new ZipInputStream(Files.newInputStream(tmpFile));
			ZipEntry zipEntry;
			while ((zipEntry = zis.getNextEntry()) != null) {
				zipEntriesNames.add(zipEntry.getName());
			}
		});
		
		assertTrue(zipEntriesNames.contains("doc.kml"));
		assertTrue(zipEntriesNames.contains("files/parks.png"));
		assertTrue(zipEntriesNames.contains("files/cabs.png"));
	}
	
	/**
	 * WARNING! This test requires fast Internet connection otherwise it will fail.
	 * Also this test should be the integration test but...
	 */
	@Test
	public void kmz_With_Additional_GoogleMaps_Icons_Should_Be_Downloaded_And_Added_Into_Kmz()
			throws ParserConfigurationException, TransformerException, SAXException, IOException {
		//GIVEN .kml with Google Maps icons hrefs
		multipartDto = new MultipartDto(new MockMultipartFile(
				"Test.kmz", "Test.kmz", null, Files.newInputStream(testKmz)));
		multipartDto.setDownloadAs(DownloadAs.KMZ);
		multipartDto.setPath("C:\\images\\");
		multipartDto.setPathType(PathTypes.ABSOLUTE.getType());
		
		multipartFileService = new MultipartFileService(
				new KmlHandler(new HtmlHandler(fileService), new GoogleIconsService(googleIconsCache), fileService),
				fileService,
				null);
		
		//WHEN
		tmpFile = multipartFileService.processMultipartDto(multipartDto, null);
		
		//THEN
		assertEquals("Test.kmz", tmpFile.getFileName().toString());
		
		List<String> zipEntriesNames = new ArrayList<>(3);
		
		assertDoesNotThrow(() -> {
			ZipInputStream zis = new ZipInputStream(Files.newInputStream(tmpFile));
			ZipEntry zipEntry;
			while ((zipEntry = zis.getNextEntry()) != null) {
				zipEntriesNames.add(zipEntry.getName());
			}
		});
		
		assertTrue(zipEntriesNames.contains("doc.kml"));
		assertTrue(zipEntriesNames.contains("files/parks.png"));
		assertTrue(zipEntriesNames.contains("files/cabs.png"));
	}
	
}