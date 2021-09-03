package mrbaxmypka.gmail.com.mapPointsTrimmer.services;

import mrbaxmypka.gmail.com.mapPointsTrimmer.entitiesDto.MultipartDto;
import mrbaxmypka.gmail.com.mapPointsTrimmer.utils.DownloadAs;
import mrbaxmypka.gmail.com.mapPointsTrimmer.utils.GoogleIconsCache;
import mrbaxmypka.gmail.com.mapPointsTrimmer.utils.PathTypes;
import mrbaxmypka.gmail.com.mapPointsTrimmer.xml.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.context.MessageSource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.*;

class MultipartFileServiceTest {
	
	private KmlHandler mockKmlHandler;
	private Resource resource;
	private ResourceLoader resourceLoader;
	private FileService mockFileService;
	private GoogleIconsService googleIconsService;
	private GoogleIconsCache googleIconsCache;
	private FileService fileService;
	private HtmlHandler htmlHandler;
	private XmlDomUtils xmlDomUtils;
	private MultipartFileService multipartFileService;
	private KmlUtils kmlUtils;
	private MessageSource mockMessageSource;
	private MultipartDto multipartDto;
	private Path tmpFile;
	private String originalKmlFilename = "MockKml.kml";
	private String testKml = "<kml>test</kml>";
	private MultipartFile multipartFile;
	private Path testKmz = Paths.get("src/test/java/resources/TestKmz.kmz");
	private Path testKmzLocusPhotoIcons = Paths.get("src/test/java/resources/TestKmzLocusPhotoIcons.kmz");
	private final String PICTOGRAM1_PNG = "Pictogram1.png";
	private final String PICTOGRAM2_PNG = "Pictogram2.png";
	private final ArrayList<String> PICTOGRAM_NAMES = new ArrayList<>(Arrays.asList(PICTOGRAM1_PNG, PICTOGRAM2_PNG));
	private final Map<String, String> PICTOGRAMS_NAMES_PATHS = new HashMap<>(2);
	private final String LOCUS_PHOTO_ICON1 = "file-sdcardLocuscacheimages1234567.png";
	private final String LOCUS_PHOTO_ICON2 = "1234567890.png";
	private final String CLASSPATH_TO_DIRECTORY = "classpath:static/pictograms";
	
	
	@BeforeEach
	public void beforeEach() throws ParserConfigurationException, TransformerException, SAXException,
			IOException, InterruptedException {
		mockMessageSource = Mockito.mock(MessageSource.class);
		Mockito.when(mockMessageSource.getMessage("exception.nullFilename", null, null))
				.thenReturn("Filename cannot be null!");
		
		mockKmlHandler = Mockito.mock(KmlHandler.class);
		Mockito.when(mockKmlHandler.processXml(Mockito.any(InputStream.class), Mockito.any(MultipartDto.class))).thenReturn(testKml);
		
		resource = Mockito.mock(Resource.class);
		Mockito.when(resource.getInputStream()).thenReturn(new ByteArrayInputStream("Pictogram1.png".getBytes(StandardCharsets.UTF_8)));
		
		resourceLoader = Mockito.mock(ResourceLoader.class);
		Mockito.when(resourceLoader.getResource(CLASSPATH_TO_DIRECTORY)).thenReturn(resource);
		
		mockFileService = Mockito.mock(FileService.class);
		Mockito.when(mockFileService.getFileName(Mockito.anyString())).thenCallRealMethod();
		
		multipartFileService = new MultipartFileService(mockKmlHandler, mockFileService, mockMessageSource);
		
		googleIconsCache = new GoogleIconsCache();
		
		googleIconsService = new GoogleIconsService(googleIconsCache);
		
		fileService = new FileService(mockMessageSource, resourceLoader);
		
		htmlHandler = new HtmlHandler(fileService);
		
		Document mockDocument = XmlTestUtils.getMockDocument();
		
		xmlDomUtils = new XmlDomUtils(mockDocument);
		
		kmlUtils = new KmlUtils(mockDocument, xmlDomUtils);
		
		multipartFile = new MockMultipartFile(originalKmlFilename, originalKmlFilename, null, testKml.getBytes());
		
		multipartDto = new MultipartDto(multipartFile);
		multipartDto.setDownloadAs(DownloadAs.KML);
		
		PICTOGRAMS_NAMES_PATHS.put(PICTOGRAM1_PNG, "pictograms/" + PICTOGRAM1_PNG);
		PICTOGRAMS_NAMES_PATHS.put(PICTOGRAM2_PNG, "pictograms/" + PICTOGRAM2_PNG);
		
		//When in Maven pom.xml </build> </testResources> </testResource> </directory> is set to "${project.basedir}/src/test/java/resources"
		//Thi following Paths aren't needed and should be replaced with just names
		final String PICTOGRAM1_PATH = "src/test/java/resources/Pictogram1.png";
		final String PICTOGRAM2_PATH = "src/test/java/resources/Pictogram2.png";
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
	 * when called. That controller obtains the {@link Path} by {@link MultipartFileService#getTempFiles()} ()} and deletes it
	 * in same manner.
	 */
	@Test
	public void kml_File_Should_Be_Saved_Temporarily_then_Deleted()
			throws IOException, TransformerException, ParserConfigurationException, SAXException, ClassNotFoundException, InterruptedException {
		// GIVEN
		
		//WHEN
		tmpFile = multipartFileService.processMultipartDto(multipartDto, null);
		
		//THEN
		assertEquals(tmpFile, multipartFileService.getTempFiles().entrySet().iterator().next().getValue().getTempFile());
		assertTrue(Files.isReadable(tmpFile));
		
		Files.deleteIfExists(tmpFile);
		
		//Temp .kml file should be deleted
		assertFalse(Files.isReadable(tmpFile));
	}
	
	@Test
	public void kml_File_Should_Be_Returned_Same()
			throws IOException, TransformerException, ParserConfigurationException, SAXException, ClassNotFoundException, InterruptedException {
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
			throws IOException, ParserConfigurationException, TransformerException, SAXException, InterruptedException {
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
			throws IOException, ParserConfigurationException, TransformerException, SAXException, InterruptedException {
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
			throws IOException, ParserConfigurationException, TransformerException, SAXException, InterruptedException {
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
	
	@ParameterizedTest
	@CsvSource({"C:\\Users\\Dyakov\\Documents\\LOCUS02.08.2021.kmz,LOCUS02.08.2021.kmz",
			"C:\\Users\\Dyakov\\Документы\\$user%\\LOCUS 02.08.2021.kmz,LOCUS 02.08.2021.kmz",
			"../test/dir/LocusTest.kmz,LocusTest.kmz",
			"/test/dir/LocusTest.kmz,LocusTest.kmz",
			"Locus file.kmz, Locus file.kmz"})
	public void kmz_File_From_Multipart_Should_Save_Original_Filename(String originalFilename, String expectedFilename)
			throws IOException, ParserConfigurationException, TransformerException, SAXException, InterruptedException {
		//GIVEN
		multipartFile = new MockMultipartFile(
				originalFilename,
				originalFilename,
				null,
				new FileInputStream(new File("src/test/java/resources/TestKmz.kmz")));
		multipartDto = new MultipartDto(multipartFile);
		multipartDto.setDownloadAs(DownloadAs.KMZ);
		
		//WHEN
		tmpFile = multipartFileService.processMultipartDto(multipartDto, null);
		
		//THEN
		assertEquals(expectedFilename, tmpFile.getFileName().toString());
		
		Files.delete(tmpFile);
		
		assertFalse(Files.exists(tmpFile));
	}
	
	@ParameterizedTest
	@ValueSource(strings = {"wrong", "test.km", "test.jpg"})
	public void kmz_File_From_Multipart_With_Incorrect_Name_Should_Throw_Exception(String wrongFilename) throws IOException {
		//GIVEN
		multipartFile = new MockMultipartFile(
				wrongFilename,
				wrongFilename,
				null,
				new FileInputStream(new File("src/test/java/resources/TestKmz.kmz")));
		multipartDto = new MultipartDto(multipartFile);
		multipartDto.setDownloadAs(DownloadAs.KMZ);
		String wrongFilenameMessage = "File extension not supported!";
		Mockito.when(mockMessageSource.getMessage(
				"exception.fileExtensionNotSupported",
				new Object[]{multipartDto.getMultipartFile().getOriginalFilename()}, Locale.ENGLISH))
				.thenReturn(wrongFilenameMessage);
		
		//WHEN
		IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
				() -> tmpFile = multipartFileService.processMultipartDto(multipartDto, null));
		
		//THEN
		assertEquals(wrongFilenameMessage, illegalArgumentException.getMessage());
	}
	
	@Test
	public void kml_In_Kmz_MultipartFile_Should_Be_Returned_Same()
			throws IOException, TransformerException, ParserConfigurationException, SAXException, InterruptedException {
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
			throws IOException, TransformerException, ParserConfigurationException, SAXException, InterruptedException {
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
			throws IOException, TransformerException, SAXException, ParserConfigurationException, InterruptedException {
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
			throws ParserConfigurationException, TransformerException, SAXException, IOException, InterruptedException {
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
			throws ParserConfigurationException, TransformerException, SAXException, IOException, InterruptedException {
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
	
	@Test
	public void kmz_With_Locus_Photo_Icons_Should_Exclude_Them_From_Kmz_When_Replace_Locus_Photo_Icons()
			throws ParserConfigurationException, TransformerException, SAXException, IOException, InterruptedException {
		multipartDto = new MultipartDto(new MockMultipartFile(
				"TestKmzLocusPhotoIcons.kmz", "TestKmzLocusPhotoIcons.kmz", null, Files.newInputStream(testKmzLocusPhotoIcons)));
		multipartDto.setDownloadAs(DownloadAs.KMZ);
		multipartDto.setReplaceLocusIcons(true);
		multipartDto.setPictogramName("Pictogram1.png");
		Set<String> locusPhotoIconsInKmz = new HashSet<>(Arrays.asList(LOCUS_PHOTO_ICON1, LOCUS_PHOTO_ICON2));
		multipartDto.setFilesToBeExcluded(locusPhotoIconsInKmz);
		
		fileService = Mockito.mock(FileService.class);
		Mockito.when(fileService.getPictogramsNames()).thenReturn(PICTOGRAM_NAMES);
		Mockito.when(fileService.getPictogramsNamesPaths()).thenReturn(PICTOGRAMS_NAMES_PATHS);
		Mockito.when(fileService.getPath(Mockito.anyString())).thenCallRealMethod();
		Mockito.when(fileService.getFileName(Mockito.anyString())).thenCallRealMethod();
		Mockito.when(fileService.getImagesExtensions()).thenReturn(Arrays.asList(
				".jpg", ".jpeg", ".png", ".bmp", ".tif", ".tiff", ".gif", ".raw", ".psd", ".xcf", "cdr"));
		
		multipartFileService = new MultipartFileService(
				new KmlHandler(new HtmlHandler(fileService), new GoogleIconsService(googleIconsCache), fileService),
				fileService,
				null);
		
		//WHEN
		tmpFile = multipartFileService.processMultipartDto(multipartDto, null);
		
		//THEN
		assertEquals("TestKmzLocusPhotoIcons.kmz", tmpFile.getFileName().toString());
		
		List<String> zipEntriesNames = new ArrayList<>(3);
		
		assertDoesNotThrow(() -> {
			ZipInputStream zis = new ZipInputStream(Files.newInputStream(tmpFile));
			ZipEntry zipEntry;
			while ((zipEntry = zis.getNextEntry()) != null) {
				zipEntriesNames.add(zipEntry.getName());
			}
		});
		
		assertFalse((zipEntriesNames.contains("files/file-sdcardLocuscacheimages1234567.png")));
		assertFalse(zipEntriesNames.contains("files/1234567890.png"));
		assertTrue(zipEntriesNames.contains("doc.kml"));//Just an additional check
		assertTrue(zipEntriesNames.contains("files/Pictogram1.png"));//Just an additional check
	}
	
	@Test
	public void kmz_With_Replaced_Locus_Icons_Should_Include_New_Desired_Pictogram_Into_Kmz_From_Server_Pictograms_Directory()
			throws ParserConfigurationException, TransformerException, SAXException, IOException, InterruptedException {
		//GIVEN A .kmz without the 'Pictogram2.png' file
		multipartDto = new MultipartDto(new MockMultipartFile(
				"TestKmzLocusPhotoIcons.kmz", "TestKmzLocusPhotoIcons.kmz", null, Files.newInputStream(testKmzLocusPhotoIcons)));
		multipartDto.setDownloadAs(DownloadAs.KMZ);
		multipartDto.setReplaceLocusIcons(true);
		multipartDto.setPictogramName("Pictogram2.png");
		Set<String> locusPhotoIconsInKmz = new HashSet<>(Arrays.asList(LOCUS_PHOTO_ICON1, LOCUS_PHOTO_ICON2));
		multipartDto.setFilesToBeExcluded(locusPhotoIconsInKmz);
		
		fileService = Mockito.mock(FileService.class);
		Mockito.when(fileService.getPictogramsNames()).thenReturn(PICTOGRAM_NAMES);
		Mockito.when(fileService.getPictogramsNamesPaths()).thenReturn(PICTOGRAMS_NAMES_PATHS);
		Mockito.when(fileService.getPath(Mockito.anyString())).thenCallRealMethod();
		Mockito.when(fileService.getFileName(Mockito.anyString())).thenCallRealMethod();
		
		multipartFileService = new MultipartFileService(
				new KmlHandler(new HtmlHandler(fileService), new GoogleIconsService(googleIconsCache), fileService),
				fileService,
				null);
		
		//WHEN
		tmpFile = multipartFileService.processMultipartDto(multipartDto, null);
		
		//THEN
		assertEquals("TestKmzLocusPhotoIcons.kmz", tmpFile.getFileName().toString());
		
		List<String> zipEntriesNames = new ArrayList<>(3);
		
		assertDoesNotThrow(() -> {
			ZipInputStream zis = new ZipInputStream(Files.newInputStream(tmpFile));
			ZipEntry zipEntry;
			while ((zipEntry = zis.getNextEntry()) != null) {
				zipEntriesNames.add(zipEntry.getName());
			}
		});
		
		assertTrue(zipEntriesNames.contains("files/Pictogram2.png"));
		assertTrue(zipEntriesNames.contains("files/Pictogram1.png"));//Just an additional check
	}
}