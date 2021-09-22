package mrbaxmypka.gmail.com.mapPointsTrimmer.services;

import mrbaxmypka.gmail.com.mapPointsTrimmer.entitiesDto.MultipartFilterDto;
import mrbaxmypka.gmail.com.mapPointsTrimmer.utils.DownloadAs;
import mrbaxmypka.gmail.com.mapPointsTrimmer.xml.KmlHandler;
import mrbaxmypka.gmail.com.mapPointsTrimmer.xml.XmlHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.context.MessageSource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.*;

class MultipartFilterFileServiceTest {

    private MultipartFilterFileService multipartFilterFileService;
    private Path tmpFile;
    private XmlHandler mockXmlHandler;
    private KmlHandler mockKmlHandler;
    private MessageSource mockMessageSource;
    private MultipartFilterDto multipartFilterDto;
    private final String originalKmlFilename = "MockKml.kml";
    private final String originalZipFilename = "photosToFilter.zip";
    private MultipartFile multipartXmlFile;
    private MultipartFile multipartZipFile;
    private final Path ZIPPED_PHOTOS_ZIP = Paths.get("src/test/java/resources/photosToFilter.zip");
    private final Path KMZ_WITH_ALL_PHOTOS_IN_DOC = Paths.get("src/test/java/resources/allPhotosInDoc.kmz");
    private final Path KMZ_WITH_ADDITIONAL_PHOTOS_IN_KMZ = Paths.get("src/test/java/resources/additionalPhotosInKmz.kmz");
    final String KMZ_ADDITIONAL_PHOTO_NAME1 = "_2016-08-01#68664196896.jpg";
    final String KMZ_ADDITIONAL_PHOTO_NAME2 = "_1468672663037.jpg";
    private final String KMZ_PHOTOS_FOLDER_NAME = "files/";
    private final String ZIPPED_PHOTOS_FOLDER_NAME = "photosToFilter/";
    private final String ZIPPED_PHOTO_NAME1 = "@#$%.jpg";
    private final String ZIPPED_PHOTO_NAME2 = "_140.jpg";
    private final String ZIPPED_PHOTO_NAME3 = "aNew-Photo.jpg";
    private final String ZIPPED_PHOTO_NAME4 = "cabs.png";
    private final String ZIPPED_PHOTO_NAME5 = "transport-bus.png";

    @BeforeEach
    public void beforeEach() throws ParserConfigurationException, SAXException,
            IOException {
        mockMessageSource = Mockito.mock(MessageSource.class);

        Resource resource = Mockito.mock(Resource.class);
        Mockito.when(resource.getInputStream()).thenReturn(new ByteArrayInputStream("Pictogram1.png".getBytes(StandardCharsets.UTF_8)));

        ResourceLoader resourceLoader = Mockito.mock(ResourceLoader.class);
        Mockito.when(resourceLoader.getResource("classpath:static/pictograms")).thenReturn(resource);

        FileService mockFileService = Mockito.mock(FileService.class);
        Mockito.when(mockFileService.getFileName(Mockito.anyString())).thenCallRealMethod();
        Mockito.when(mockFileService.getExtension(Mockito.anyString())).thenCallRealMethod();
        Mockito.when(mockFileService.getAllowedZipExtensions()).thenReturn(Arrays.asList("zip", "kmz", "jar", "gz"));
        Mockito.when(mockFileService.getAllowedXmlExtensions()).thenReturn(Arrays.asList("kmz", "kml", "xml", "txt"));

        mockXmlHandler = Mockito.mock(XmlHandler.class);
        mockKmlHandler = Mockito.mock(KmlHandler.class);

        MultipartMainFileService multipartMainFileService = new MultipartMainFileService(mockKmlHandler, mockFileService, mockMessageSource);
        multipartFilterFileService = new MultipartFilterFileService(mockFileService, mockMessageSource, mockKmlHandler);

        //The following will be use if DownloadAs.KMZ will be tested
//        FileService fileService = new FileService(mockMessageSource, resourceLoader);
//        HtmlHandler htmlHandler = new HtmlHandler(fileService);
//        Document mockDocument = XmlTestUtils.getMockDocument();
//        XmlDomUtils xmlDomUtils = new XmlDomUtils(mockDocument);
//        KmlUtils kmlUtils = new KmlUtils(mockDocument, xmlDomUtils);

        multipartZipFile = new MockMultipartFile(originalZipFilename, originalZipFilename, null, Files.readAllBytes(ZIPPED_PHOTOS_ZIP));

        multipartFilterDto = new MultipartFilterDto();
        multipartFilterDto.setDownloadAs(DownloadAs.ZIP);
        multipartFilterDto.setMultipartZipFile(multipartZipFile);
    }

    @AfterEach
    public void afterEach() {
        try {
            Files.deleteIfExists(tmpFile);
        } catch (IOException | NullPointerException e) {
            System.out.println("");
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"test.kml", "test.kmz", "test.txt", "test.xml"})
    public void filter_Should_Receive_Only_Supported_Text_Files_Extensions(String textFilename) {
        //GIVEN
        multipartXmlFile = new MockMultipartFile(textFilename, textFilename, null, (byte[]) null);
        multipartFilterDto.setMultipartXmlFile(multipartXmlFile);

        String notSupportedFilename = "Such a filename (test) is not supported!";
        Mockito.when(mockMessageSource.getMessage(
                "exception.fileExtensionNotSupported", new Object[]{textFilename}, Locale.ENGLISH))
                .thenReturn(notSupportedFilename);

        //WHEN
        //THEN
        try {
            tmpFile = multipartFilterFileService.processMultipartFilterDto(multipartFilterDto, Locale.ENGLISH);
        } catch (IOException | ParserConfigurationException | SAXException | TransformerException | NullPointerException e) {
            //Do nothing
        } catch (IllegalArgumentException e) {
            assertNotEquals(notSupportedFilename, e.getMessage());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"test.gz", "test.string", "test.loc", "test.html"})
    public void filter_Should_Not_Receive_Not_Supported_Text_Files_Extensions(String textFilename) {
        //GIVEN
        multipartXmlFile = new MockMultipartFile(textFilename, textFilename, null, textFilename.getBytes());
        multipartFilterDto.setMultipartXmlFile(multipartXmlFile);

        String notSupportedFilename = "Such a filename (test) is not supported!";
        Mockito.when(mockMessageSource.getMessage(
                "exception.fileExtensionNotSupported", new Object[]{textFilename}, Locale.ENGLISH))
                .thenReturn(notSupportedFilename);

        //WHEN
        IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
                () -> tmpFile = multipartFilterFileService.processMultipartFilterDto(multipartFilterDto, Locale.ENGLISH));

        //THEN
        assertEquals(notSupportedFilename, illegalArgumentException.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"test.zp", "test.rar", "test.7zip"})
    public void filter_Should_Not_Receive_Not_Supported_Archive_Files_Extensions(String zipFilename) {
        //GIVEN
        multipartXmlFile = new MockMultipartFile(originalKmlFilename, originalKmlFilename, null, originalKmlFilename.getBytes());
        multipartZipFile = new MockMultipartFile(zipFilename, zipFilename, null, (byte[]) null);

        multipartFilterDto.setMultipartZipFile(multipartZipFile);
        multipartFilterDto.setMultipartXmlFile(multipartXmlFile);

        final String message = "Such a filename (test) is not supported!";
        Mockito.when(mockMessageSource.getMessage(
                "exception.fileExtensionNotSupported", new Object[]{zipFilename}, Locale.ENGLISH))
                .thenReturn(message);

        //WHEN
        assertThrows(IllegalArgumentException.class,
                () -> tmpFile = multipartFilterFileService.processMultipartFilterDto(multipartFilterDto, Locale.ENGLISH),
                message);

        //THEN
    }

    @Test
    public void when_Kml_Contains_All_Photos_Zip_Should_Contain_All_Of_Them()
            throws ParserConfigurationException, TransformerException, SAXException, IOException {
        //GIVEN
        String kmlWithAllPhotos = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" +
                "<Document>\n" +
                "\t<name>Test doc</name>\n" +
                "\t<atom:author><atom:name>Locus (Android)</atom:name></atom:author>\n" +
                "\t\t<Style id=\"" +
                ZIPPED_PHOTO_NAME4 +
                "\">\n" +
                "\t\t<IconStyle>\n" +
                "\t\t\t<Icon><href>" +
                ZIPPED_PHOTOS_FOLDER_NAME + ZIPPED_PHOTO_NAME4 +
                "</href></Icon>\n" +
                "\t\t</IconStyle>\n" +
                "\t</Style>\n" +
                "\t\t<Style id=\"" +
                ZIPPED_PHOTO_NAME4 +
                "\">\n" +
                "\t\t<IconStyle>\n" +
                "\t\t\t<Icon><href>" +
                ZIPPED_PHOTOS_FOLDER_NAME + ZIPPED_PHOTO_NAME5 +
                "</href></Icon>\n" +
                "\t\t</IconStyle>\n" +
                "\t</Style>\n" +
                "\t\t<Style id=\"" +
                ZIPPED_PHOTO_NAME5 +
                "\">\n" +
                "\t\t<IconStyle>\n" +
                "\t\t\t<Icon><href>" +
                ZIPPED_PHOTOS_FOLDER_NAME + ZIPPED_PHOTO_NAME5 +
                "</href></Icon>\n" +
                "\t\t</IconStyle>\n" +
                "\t</Style>\n" +
                "<Placemark>\n" +
                "\t<name>Placemark1</name>\n" +
                "\t<description><![CDATA[]]></description>\n" +
                "\t<styleUrl>" +
                ZIPPED_PHOTO_NAME1 +
                "</styleUrl>\n" +
                "</Placemark>" +
                "<Placemark>\n" +
                "\t<name>Placemark2</name>\n" +
                "\t<description><![CDATA[" +

                "<!-- desc_gen:start -->\n" +
                "<font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\">" +
                "<img src=\"" +
                ZIPPED_PHOTOS_FOLDER_NAME + ZIPPED_PHOTO_NAME2 +
                "\" width=\"60px\" align=\"right\" style=\"border: 3px white solid; color: black; max-width: 300%\"> " +
                "<br /><br /></td></tr><tr><td colspan=\"1\"><hr></td></tr><tr><td><table width=\"100%\"><tr><td align=\"left\" valign=\"center\"><small><b>Высота</b></small></td><td align=\"center\" valign=\"center\">169 m</td></tr>\n" +
                "<tr><td align=\"left\" valign=\"center\"><small><b>Азимут</b></small></td><td align=\"center\" valign=\"center\">147 °</td></tr>\n" +
                "<tr><td align=\"left\" valign=\"center\"><small><b>Точность</b></small></td><td align=\"center\" valign=\"center\">3 m</td></tr>\n" +
                "<tr><td align=\"left\" valign=\"center\"><small><b>Создано</b></small></td><td align=\"center\" valign=\"center\">2018-05-14 15:28:41</td></tr>\n" +
                "</table></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font>\n" +
                "<!-- desc_gen:end -->" +

                "]]></description>\n" +
                "\t<styleUrl>" +
                ZIPPED_PHOTO_NAME4 +
                "</styleUrl>\n" +
                "\t<ExtendedData xmlns:lc=\"http://www.locusmap.eu\">\n" +
                "\t\t<lc:attachment>" +
                ZIPPED_PHOTOS_FOLDER_NAME + ZIPPED_PHOTO_NAME3 +
                "</lc:attachment>\n" +
                "\t</ExtendedData>\n" +
                "</Placemark>" +
                "<Placemark>\n" +
                "\t<name>Placemark3</name>\n" +
                "\t<styleUrl>" +
                ZIPPED_PHOTO_NAME5 + "" +
                "</styleUrl>\n" +
                "</Placemark>" +
                "</Document>\n" +
                "</kml>\n";

        multipartXmlFile = new MockMultipartFile(
                originalKmlFilename, originalKmlFilename, null, kmlWithAllPhotos.getBytes(StandardCharsets.UTF_8));
        multipartZipFile = new MockMultipartFile(
                originalZipFilename, originalZipFilename, null, Files.readAllBytes(ZIPPED_PHOTOS_ZIP));

        multipartFilterDto.setMultipartXmlFile(multipartXmlFile);

        Mockito.when(mockKmlHandler.getDocument(Mockito.any(InputStream.class))).thenCallRealMethod();
        Mockito.when(mockKmlHandler.getAsString(Mockito.any(Document.class))).thenCallRealMethod();

        //WHEN
        tmpFile = multipartFilterFileService.processMultipartFilterDto(multipartFilterDto, Locale.ENGLISH);

        //THEN
        List<String> zipEntriesNames = new ArrayList<>(5);

        assertDoesNotThrow(() -> {
            ZipInputStream zis = new ZipInputStream(Files.newInputStream(tmpFile));
            ZipEntry zipEntry;
            while ((zipEntry = zis.getNextEntry()) != null) {
                //Skip a folder name (which is also a zipEntry
                if (!zipEntry.getName().equals(ZIPPED_PHOTOS_FOLDER_NAME)) {
                    zipEntriesNames.add(zipEntry.getName());
                }
            }
        });
        assertEquals(5, zipEntriesNames.size());

        assertTrue((zipEntriesNames.contains(ZIPPED_PHOTOS_FOLDER_NAME + ZIPPED_PHOTO_NAME1)));
        assertTrue((zipEntriesNames.contains(ZIPPED_PHOTOS_FOLDER_NAME + ZIPPED_PHOTO_NAME2)));
        assertTrue((zipEntriesNames.contains(ZIPPED_PHOTOS_FOLDER_NAME + ZIPPED_PHOTO_NAME3)));
        assertTrue((zipEntriesNames.contains(ZIPPED_PHOTOS_FOLDER_NAME + ZIPPED_PHOTO_NAME4)));
        assertTrue((zipEntriesNames.contains(ZIPPED_PHOTOS_FOLDER_NAME + ZIPPED_PHOTO_NAME5)));
    }

    @Test
    public void when_Kml_Doesnt_Contain_Png_Files_Photos_Zip_Should_Not_Contain_Them()
            throws ParserConfigurationException, TransformerException, SAXException, IOException {
        //GIVEN
        String kmlWithAllPhotos = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" +
                "<Document>\n" +
                "\t<name>Test doc</name>\n" +
                "\t<atom:author><atom:name>Locus (Android)</atom:name></atom:author>\n" +
                "\t\t<Style id=\"" +
                ZIPPED_PHOTO_NAME1 +
                "\">\n" +
                "\t\t<IconStyle>\n" +
                "\t\t\t<Icon><href>" +
                ZIPPED_PHOTOS_FOLDER_NAME + ZIPPED_PHOTO_NAME1 +
                "</href></Icon>\n" +
                "\t\t</IconStyle>\n" +
                "\t</Style>\n" +
                "\t\t<Style id=\"" +
                ZIPPED_PHOTO_NAME1 +
                "\">\n" +
                "\t\t<IconStyle>\n" +
                "\t\t\t<Icon><href>" +
                ZIPPED_PHOTOS_FOLDER_NAME + ZIPPED_PHOTO_NAME1 +
                "</href></Icon>\n" +
                "\t\t</IconStyle>\n" +
                "\t</Style>\n" +
                "\t\t<Style id=\"" +
                ZIPPED_PHOTO_NAME2 +
                "\">\n" +
                "\t\t<IconStyle>\n" +
                "\t\t\t<Icon><href>" +
                ZIPPED_PHOTOS_FOLDER_NAME + ZIPPED_PHOTO_NAME2 +
                "</href></Icon>\n" +
                "\t\t</IconStyle>\n" +
                "\t</Style>\n" +
                "<Placemark>\n" +
                "\t<name>Placemark1</name>\n" +
                "\t<description><![CDATA[]]></description>\n" +
                "\t<styleUrl>" +
                ZIPPED_PHOTO_NAME3 +
                "</styleUrl>\n" +
                "</Placemark>" +
                "<Placemark>\n" +
                "\t<name>Placemark2</name>\n" +
                "\t<description><![CDATA[" +

                "<!-- desc_gen:start -->\n" +
                "<font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\">" +
                "<img src=\"" +
                ZIPPED_PHOTOS_FOLDER_NAME + ZIPPED_PHOTO_NAME2 +
                "\" width=\"60px\" align=\"right\" style=\"border: 3px white solid; color: black; max-width: 300%\"> " +
                "<br /><br /></td></tr><tr><td colspan=\"1\"><hr></td></tr><tr><td><table width=\"100%\"><tr><td align=\"left\" valign=\"center\"><small><b>Высота</b></small></td><td align=\"center\" valign=\"center\">169 m</td></tr>\n" +
                "<tr><td align=\"left\" valign=\"center\"><small><b>Азимут</b></small></td><td align=\"center\" valign=\"center\">147 °</td></tr>\n" +
                "<tr><td align=\"left\" valign=\"center\"><small><b>Точность</b></small></td><td align=\"center\" valign=\"center\">3 m</td></tr>\n" +
                "<tr><td align=\"left\" valign=\"center\"><small><b>Создано</b></small></td><td align=\"center\" valign=\"center\">2018-05-14 15:28:41</td></tr>\n" +
                "</table></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font>\n" +
                "<!-- desc_gen:end -->" +

                "]]></description>\n" +
                "\t<styleUrl>" +
                ZIPPED_PHOTO_NAME1 +
                "</styleUrl>\n" +
                "\t<ExtendedData xmlns:lc=\"http://www.locusmap.eu\">\n" +
                "\t\t<lc:attachment>" +
                ZIPPED_PHOTOS_FOLDER_NAME + ZIPPED_PHOTO_NAME3 +
                "</lc:attachment>\n" +
                "\t</ExtendedData>\n" +
                "</Placemark>" +
                "<Placemark>\n" +
                "\t<name>Placemark3</name>\n" +
                "\t<styleUrl>" +
                ZIPPED_PHOTO_NAME2 + "" +
                "</styleUrl>\n" +
                "</Placemark>" +
                "</Document>\n" +
                "</kml>\n";

        multipartXmlFile = new MockMultipartFile(
                originalKmlFilename, originalKmlFilename, null, kmlWithAllPhotos.getBytes(StandardCharsets.UTF_8));
        multipartZipFile = new MockMultipartFile(
                originalZipFilename, originalZipFilename, null, Files.readAllBytes(ZIPPED_PHOTOS_ZIP));

        multipartFilterDto.setMultipartXmlFile(multipartXmlFile);

        Mockito.when(mockKmlHandler.getDocument(Mockito.any(InputStream.class))).thenCallRealMethod();
        Mockito.when(mockKmlHandler.getAsString(Mockito.any(Document.class))).thenCallRealMethod();

        //WHEN
        tmpFile = multipartFilterFileService.processMultipartFilterDto(multipartFilterDto, Locale.ENGLISH);

        //THEN
        List<String> zipEntriesNames = new ArrayList<>(3);

        assertDoesNotThrow(() -> {
            ZipInputStream zis = new ZipInputStream(Files.newInputStream(tmpFile));
            ZipEntry zipEntry;
            while ((zipEntry = zis.getNextEntry()) != null) {
                //Skip a folder name (which is also a zipEntry
                if (!zipEntry.getName().equals(ZIPPED_PHOTOS_FOLDER_NAME)) {
                    zipEntriesNames.add(zipEntry.getName());
                }
            }
        });
        assertEquals(3, zipEntriesNames.size());

        assertTrue((zipEntriesNames.contains(ZIPPED_PHOTOS_FOLDER_NAME + ZIPPED_PHOTO_NAME1)));
        assertTrue((zipEntriesNames.contains(ZIPPED_PHOTOS_FOLDER_NAME + ZIPPED_PHOTO_NAME2)));
        assertTrue((zipEntriesNames.contains(ZIPPED_PHOTOS_FOLDER_NAME + ZIPPED_PHOTO_NAME3)));

        assertFalse((zipEntriesNames.contains(ZIPPED_PHOTOS_FOLDER_NAME + ZIPPED_PHOTO_NAME4)));
        assertFalse((zipEntriesNames.contains(ZIPPED_PHOTOS_FOLDER_NAME + ZIPPED_PHOTO_NAME5)));
    }

    @Test
    public void when_Txt_Contains_All_Photos_Zip_Should_Contain_All_Of_Them()
            throws ParserConfigurationException, TransformerException, SAXException, IOException {
        //GIVEN
        String txtWithAllPhotos = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" +
                "<Document>\n" +
                "\t<name>Test doc</name>\n" +
                "\t<atom:author><atom:name>Locus (Android)</atom:name></atom:author>\n" +
                "\t\t<Style id=\"" +
                ZIPPED_PHOTO_NAME4 +
                "\">\n" +
                "\t\t<IconStyle>\n" +
                "\t\t\t<Icon><href>" +
                ZIPPED_PHOTOS_FOLDER_NAME + ZIPPED_PHOTO_NAME4 +
                "</href></Icon>\n" +
                "\t\t</IconStyle>\n" +
                "\t</Style>\n" +
                "\t\t<Style id=\"" +
                ZIPPED_PHOTO_NAME4 +
                "\">\n" +
                "\t\t<IconStyle>\n" +
                "\t\t\t<Icon><href>" +
                ZIPPED_PHOTOS_FOLDER_NAME + ZIPPED_PHOTO_NAME5 +
                "</href></Icon>\n" +
                "\t\t</IconStyle>\n" +
                "\t</Style>\n" +
                "\t\t<Style id=\"" +
                ZIPPED_PHOTO_NAME5 +
                "\">\n" +
                "\t\t<IconStyle>\n" +
                "\t\t\t<Icon><href>" +
                ZIPPED_PHOTOS_FOLDER_NAME + ZIPPED_PHOTO_NAME5 +
                "</href></Icon>\n" +
                "\t\t</IconStyle>\n" +
                "\t</Style>\n" +
                "<Placemark>\n" +
                "\t<name>Placemark1</name>\n" +
                "\t<description><![CDATA[]]></description>\n" +
                "\t<styleUrl>" +
                ZIPPED_PHOTO_NAME1 +
                "</styleUrl>\n" +
                "</Placemark>" +
                "<Placemark>\n" +
                "\t<name>Placemark2</name>\n" +
                "\t<description><![CDATA[" +

                "<!-- desc_gen:start -->\n" +
                "<font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\">" +
                "<img src=\"" +
                ZIPPED_PHOTOS_FOLDER_NAME + ZIPPED_PHOTO_NAME2 +
                "\" width=\"60px\" align=\"right\" style=\"border: 3px white solid; color: black; max-width: 300%\"> " +
                "<br /><br /></td></tr><tr><td colspan=\"1\"><hr></td></tr><tr><td><table width=\"100%\"><tr><td align=\"left\" valign=\"center\"><small><b>Высота</b></small></td><td align=\"center\" valign=\"center\">169 m</td></tr>\n" +
                "<tr><td align=\"left\" valign=\"center\"><small><b>Азимут</b></small></td><td align=\"center\" valign=\"center\">147 °</td></tr>\n" +
                "<tr><td align=\"left\" valign=\"center\"><small><b>Точность</b></small></td><td align=\"center\" valign=\"center\">3 m</td></tr>\n" +
                "<tr><td align=\"left\" valign=\"center\"><small><b>Создано</b></small></td><td align=\"center\" valign=\"center\">2018-05-14 15:28:41</td></tr>\n" +
                "</table></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font>\n" +
                "<!-- desc_gen:end -->" +

                "]]></description>\n" +
                "\t<styleUrl>" +
                ZIPPED_PHOTO_NAME4 +
                "</styleUrl>\n" +
                "\t<ExtendedData xmlns:lc=\"http://www.locusmap.eu\">\n" +
                "\t\t<lc:attachment>" +
                ZIPPED_PHOTOS_FOLDER_NAME + ZIPPED_PHOTO_NAME3 +
                "</lc:attachment>\n" +
                "\t</ExtendedData>\n" +
                "</Placemark>" +
                "<Placemark>\n" +
                "\t<name>Placemark3</name>\n" +
                "\t<styleUrl>" +
                ZIPPED_PHOTO_NAME5 + "" +
                "</styleUrl>\n" +
                "</Placemark>" +
                "</Document>\n" +
                "</kml>\n";

        multipartXmlFile = new MockMultipartFile(
                "test.txt", "test.txt", null, txtWithAllPhotos.getBytes(StandardCharsets.UTF_8));
        multipartZipFile = new MockMultipartFile(
                originalZipFilename, originalZipFilename, null, Files.readAllBytes(ZIPPED_PHOTOS_ZIP));

        multipartFilterDto.setMultipartXmlFile(multipartXmlFile);

        //WHEN
        tmpFile = multipartFilterFileService.processMultipartFilterDto(multipartFilterDto, Locale.ENGLISH);

        //THEN
        List<String> zipEntriesNames = new ArrayList<>(5);

        assertDoesNotThrow(() -> {
            ZipInputStream zis = new ZipInputStream(Files.newInputStream(tmpFile));
            ZipEntry zipEntry;
            while ((zipEntry = zis.getNextEntry()) != null) {
                //Skip a folder name (which is also a zipEntry
                if (!zipEntry.getName().equals(ZIPPED_PHOTOS_FOLDER_NAME)) {
                    zipEntriesNames.add(zipEntry.getName());
                }
            }
        });
        assertEquals(5, zipEntriesNames.size());

        assertTrue((zipEntriesNames.contains(ZIPPED_PHOTOS_FOLDER_NAME + ZIPPED_PHOTO_NAME1)));
        assertTrue((zipEntriesNames.contains(ZIPPED_PHOTOS_FOLDER_NAME + ZIPPED_PHOTO_NAME2)));
        assertTrue((zipEntriesNames.contains(ZIPPED_PHOTOS_FOLDER_NAME + ZIPPED_PHOTO_NAME3)));
        assertTrue((zipEntriesNames.contains(ZIPPED_PHOTOS_FOLDER_NAME + ZIPPED_PHOTO_NAME4)));
        assertTrue((zipEntriesNames.contains(ZIPPED_PHOTOS_FOLDER_NAME + ZIPPED_PHOTO_NAME5)));
    }

    @Test
    public void when_Txt_Doesnt_Contain_Png_Files_Photos_Zip_Should_Not_Contain_Them()
            throws ParserConfigurationException, TransformerException, SAXException, IOException {
        //GIVEN
        String notValidTxtWithAllPhotos = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" +
                "\t<atom:author><atom:name>Locus (Android)</atom:name></atom:author>\n" +
                "\t\t<Style id=\"" +
                ZIPPED_PHOTO_NAME1 +
                "\">\n" +
                "\t\t<IconStyle>\n" +
                "\t\t\t<Icon><href>" +
                ZIPPED_PHOTOS_FOLDER_NAME + ZIPPED_PHOTO_NAME1 +
                "</href></Icon>\n" +
                "\t\t</IconStyle>\n" +
                "\t</Style>\n" +
                "\t\t<Style id=\"" +
                ZIPPED_PHOTO_NAME1 +
                "\">\n" +
                "\t\t<IconStyle>\n" +
                "\t\t\t<Icon><href>" +
                ZIPPED_PHOTOS_FOLDER_NAME + ZIPPED_PHOTO_NAME1 +
                "</href></Icon>\n" +
                "\t\t</IconStyle>\n" +
                "\t</Style>\n" +
                "\t\t<Style id=\"" +
                ZIPPED_PHOTO_NAME2 +
                "\">\n" +
                "\t\t<IconStyle>\n" +
                "\t\t\t<Icon><href>" +
                ZIPPED_PHOTOS_FOLDER_NAME + ZIPPED_PHOTO_NAME2 +
                "</href></Icon>\n" +
                "\t\t</IconStyle>\n" +
                "\t</Style>\n" +
                "<Placemark>\n" +
                "\t<name>Placemark1</name>\n" +
                "\t<description><![CDATA[]]></description>\n" +
                "\t<styleUrl>" +
                ZIPPED_PHOTO_NAME3 +
                "</styleUrl>\n" +
                "</Placemark>" +
                "<Placemark>\n" +
                "\t<name>Placemark2</name>\n" +
                "\t<description><![CDATA[" +

                "<!-- desc_gen:start -->\n" +
                "<font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\">" +
                "<img src=\"" +
                ZIPPED_PHOTOS_FOLDER_NAME + ZIPPED_PHOTO_NAME2 +
                "\" width=\"60px\" align=\"right\" style=\"border: 3px white solid; color: black; max-width: 300%\"> " +
                "<br /><br /></td></tr><tr><td colspan=\"1\"><hr></td></tr><tr><td><table width=\"100%\"><tr><td align=\"left\" valign=\"center\"><small><b>Высота</b></small></td><td align=\"center\" valign=\"center\">169 m</td></tr>\n" +
                "<tr><td align=\"left\" valign=\"center\"><small><b>Азимут</b></small></td><td align=\"center\" valign=\"center\">147 °</td></tr>\n" +
                "<tr><td align=\"left\" valign=\"center\"><small><b>Точность</b></small></td><td align=\"center\" valign=\"center\">3 m</td></tr>\n" +
                "<tr><td align=\"left\" valign=\"center\"><small><b>Создано</b></small></td><td align=\"center\" valign=\"center\">2018-05-14 15:28:41</td></tr>\n" +
                "</table></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font>\n" +
                "<!-- desc_gen:end -->" +

                "]]></description>\n" +
                "\t<styleUrl>" +
                ZIPPED_PHOTO_NAME1 +
                "</styleUrl>\n" +
                "\t<ExtendedData xmlns:lc=\"http://www.locusmap.eu\">\n" +
                "\t\t<lc:attachment>" +
                ZIPPED_PHOTOS_FOLDER_NAME + ZIPPED_PHOTO_NAME3 +
                "</lc:attachment>\n" +
                "\t</ExtendedData>\n" +
                "</Placemark>" +
                "<Placemark>\n" +
                "\t<name>Placemark3</name>\n" +
                "\t<styleUrl>" +
                ZIPPED_PHOTO_NAME2 + "" +
                "</styleUrl>\n" +
                "</kml>\n";

        multipartXmlFile = new MockMultipartFile(
                "test.txt", "test.txt", null, notValidTxtWithAllPhotos.getBytes(StandardCharsets.UTF_8));
        multipartZipFile = new MockMultipartFile(
                originalZipFilename, originalZipFilename, null, Files.readAllBytes(ZIPPED_PHOTOS_ZIP));

        multipartFilterDto.setMultipartXmlFile(multipartXmlFile);

        //WHEN
        tmpFile = multipartFilterFileService.processMultipartFilterDto(multipartFilterDto, Locale.ENGLISH);

        //THEN
        List<String> zipEntriesNames = new ArrayList<>(3);

        assertDoesNotThrow(() -> {
            ZipInputStream zis = new ZipInputStream(Files.newInputStream(tmpFile));
            ZipEntry zipEntry;
            while ((zipEntry = zis.getNextEntry()) != null) {
                //Skip a folder name (which is also a zipEntry
                if (!zipEntry.getName().equals(ZIPPED_PHOTOS_FOLDER_NAME)) {
                    zipEntriesNames.add(zipEntry.getName());
                }
            }
        });
        assertEquals(3, zipEntriesNames.size());

        assertTrue((zipEntriesNames.contains(ZIPPED_PHOTOS_FOLDER_NAME + ZIPPED_PHOTO_NAME1)));
        assertTrue((zipEntriesNames.contains(ZIPPED_PHOTOS_FOLDER_NAME + ZIPPED_PHOTO_NAME2)));
        assertTrue((zipEntriesNames.contains(ZIPPED_PHOTOS_FOLDER_NAME + ZIPPED_PHOTO_NAME3)));

        assertFalse((zipEntriesNames.contains(ZIPPED_PHOTOS_FOLDER_NAME + ZIPPED_PHOTO_NAME4)));
        assertFalse((zipEntriesNames.contains(ZIPPED_PHOTOS_FOLDER_NAME + ZIPPED_PHOTO_NAME5)));
    }

    @Test
    public void when_Kmz_Contains_All_Photos_Zip_Should_Contain_All_Of_Them()
            throws ParserConfigurationException, TransformerException, SAXException, IOException {
        //GIVEN
        multipartXmlFile = new MockMultipartFile(
                "allPhotosInDoc.kmz", "allPhotosInDoc.kmz", null, Files.readAllBytes(KMZ_WITH_ALL_PHOTOS_IN_DOC));
        multipartZipFile = new MockMultipartFile(
                originalZipFilename, originalZipFilename, null, Files.readAllBytes(ZIPPED_PHOTOS_ZIP));

        multipartFilterDto.setMultipartXmlFile(multipartXmlFile);

        Mockito.when(mockKmlHandler.getDocument(Mockito.any(InputStream.class))).thenCallRealMethod();
        Mockito.when(mockKmlHandler.getAsString(Mockito.any(Document.class))).thenCallRealMethod();

        //WHEN
        tmpFile = multipartFilterFileService.processMultipartFilterDto(multipartFilterDto, Locale.ENGLISH);

        //THEN
        List<String> zipEntriesNames = new ArrayList<>(5);

        assertDoesNotThrow(() -> {
            ZipInputStream zis = new ZipInputStream(Files.newInputStream(tmpFile));
            ZipEntry zipEntry;
            while ((zipEntry = zis.getNextEntry()) != null) {
                //Skip a folder name (which is also a zipEntry
                if (!zipEntry.getName().equals(ZIPPED_PHOTOS_FOLDER_NAME)) {
                    zipEntriesNames.add(zipEntry.getName());
                }
            }
        });
        assertEquals(5, zipEntriesNames.size());

        assertTrue((zipEntriesNames.contains(ZIPPED_PHOTOS_FOLDER_NAME + ZIPPED_PHOTO_NAME1)));
        assertTrue((zipEntriesNames.contains(ZIPPED_PHOTOS_FOLDER_NAME + ZIPPED_PHOTO_NAME2)));
        assertTrue((zipEntriesNames.contains(ZIPPED_PHOTOS_FOLDER_NAME + ZIPPED_PHOTO_NAME3)));
        assertTrue((zipEntriesNames.contains(ZIPPED_PHOTOS_FOLDER_NAME + ZIPPED_PHOTO_NAME4)));
        assertTrue((zipEntriesNames.contains(ZIPPED_PHOTOS_FOLDER_NAME + ZIPPED_PHOTO_NAME5)));
    }

    @Test
    public void when_Kmz_Contains_Additional_Photos_Zip_Should_Not_Contain_All_Of_Them()
            throws ParserConfigurationException, TransformerException, SAXException, IOException {
        //GIVEN
        multipartXmlFile = new MockMultipartFile(
                "additionalPhotosInKmz.kmz", "additionalPhotosInKmz.kmz", null, Files.readAllBytes(KMZ_WITH_ADDITIONAL_PHOTOS_IN_KMZ));
        multipartZipFile = new MockMultipartFile(
                originalZipFilename, originalZipFilename, null, Files.readAllBytes(ZIPPED_PHOTOS_ZIP));

        multipartFilterDto.setMultipartXmlFile(multipartXmlFile);

        Mockito.when(mockKmlHandler.getDocument(Mockito.any(InputStream.class))).thenCallRealMethod();
        Mockito.when(mockKmlHandler.getAsString(Mockito.any(Document.class))).thenCallRealMethod();

        //WHEN
        tmpFile = multipartFilterFileService.processMultipartFilterDto(multipartFilterDto, Locale.ENGLISH);

        //THEN
        List<String> zipEntriesNames = new ArrayList<>(5);

        assertDoesNotThrow(() -> {
            ZipInputStream zis = new ZipInputStream(Files.newInputStream(tmpFile));
            ZipEntry zipEntry;
            while ((zipEntry = zis.getNextEntry()) != null) {
                //Skip a folder name (which is also a zipEntry
                if (!zipEntry.getName().equals(ZIPPED_PHOTOS_FOLDER_NAME) &&
                        !zipEntry.getName().equals(KMZ_PHOTOS_FOLDER_NAME)) {
                    zipEntriesNames.add(zipEntry.getName());
                }
            }
        });
        assertEquals(5, zipEntriesNames.size());

        assertTrue((zipEntriesNames.contains(ZIPPED_PHOTOS_FOLDER_NAME + ZIPPED_PHOTO_NAME1)));
        assertTrue((zipEntriesNames.contains(ZIPPED_PHOTOS_FOLDER_NAME + ZIPPED_PHOTO_NAME2)));
        assertTrue((zipEntriesNames.contains(ZIPPED_PHOTOS_FOLDER_NAME + ZIPPED_PHOTO_NAME3)));
        assertTrue((zipEntriesNames.contains(ZIPPED_PHOTOS_FOLDER_NAME + ZIPPED_PHOTO_NAME4)));
        assertTrue((zipEntriesNames.contains(ZIPPED_PHOTOS_FOLDER_NAME + ZIPPED_PHOTO_NAME5)));

        assertFalse((zipEntriesNames.contains(ZIPPED_PHOTOS_FOLDER_NAME + KMZ_ADDITIONAL_PHOTO_NAME1)));
        assertFalse((zipEntriesNames.contains(ZIPPED_PHOTOS_FOLDER_NAME + KMZ_ADDITIONAL_PHOTO_NAME2)));
    }

    @Test
    public void when_Initial_Kmz_Has_To_Be_Downloaded_As_Archive_It_Should_Be_Valid_Kmz()
            throws ParserConfigurationException, TransformerException, SAXException, IOException {
        //GIVEN
        String kmlWithThreePhotos = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" +
                "<Document>\n" +
                "\t<name>Test doc</name>\n" +
                "\t<atom:author><atom:name>Locus (Android)</atom:name></atom:author>\n" +
                "\t\t<Style id=\"" +
                ZIPPED_PHOTO_NAME1 +
                "\">\n" +
                "\t\t<IconStyle>\n" +
                "\t\t\t<Icon><href>" +
                ZIPPED_PHOTOS_FOLDER_NAME + ZIPPED_PHOTO_NAME1 +
                "</href></Icon>\n" +
                "\t\t</IconStyle>\n" +
                "\t</Style>\n" +
                "\t\t<Style id=\"" +
                ZIPPED_PHOTO_NAME1 +
                "\">\n" +
                "\t\t<IconStyle>\n" +
                "\t\t\t<Icon><href>" +
                ZIPPED_PHOTOS_FOLDER_NAME + ZIPPED_PHOTO_NAME1 +
                "</href></Icon>\n" +
                "\t\t</IconStyle>\n" +
                "\t</Style>\n" +
                "\t\t<Style id=\"" +
                ZIPPED_PHOTO_NAME2 +
                "\">\n" +
                "\t\t<IconStyle>\n" +
                "\t\t\t<Icon><href>" +
                ZIPPED_PHOTOS_FOLDER_NAME + ZIPPED_PHOTO_NAME2 +
                "</href></Icon>\n" +
                "\t\t</IconStyle>\n" +
                "\t</Style>\n" +
                "<Placemark>\n" +
                "\t<name>Placemark1</name>\n" +
                "\t<description><![CDATA[]]></description>\n" +
                "\t<styleUrl>" +
                ZIPPED_PHOTO_NAME3 +
                "</styleUrl>\n" +
                "</Placemark>" +
                "<Placemark>\n" +
                "\t<name>Placemark2</name>\n" +
                "\t<description><![CDATA[" +

                "<!-- desc_gen:start -->\n" +
                "<font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\">" +
                "<img src=\"" +
                ZIPPED_PHOTOS_FOLDER_NAME + ZIPPED_PHOTO_NAME2 +
                "\" width=\"60px\" align=\"right\" style=\"border: 3px white solid; color: black; max-width: 300%\"> " +
                "<br /><br /></td></tr><tr><td colspan=\"1\"><hr></td></tr><tr><td><table width=\"100%\"><tr><td align=\"left\" valign=\"center\"><small><b>Высота</b></small></td><td align=\"center\" valign=\"center\">169 m</td></tr>\n" +
                "<tr><td align=\"left\" valign=\"center\"><small><b>Азимут</b></small></td><td align=\"center\" valign=\"center\">147 °</td></tr>\n" +
                "<tr><td align=\"left\" valign=\"center\"><small><b>Точность</b></small></td><td align=\"center\" valign=\"center\">3 m</td></tr>\n" +
                "<tr><td align=\"left\" valign=\"center\"><small><b>Создано</b></small></td><td align=\"center\" valign=\"center\">2018-05-14 15:28:41</td></tr>\n" +
                "</table></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font>\n" +
                "<!-- desc_gen:end -->" +

                "]]></description>\n" +
                "\t<styleUrl>" +
                ZIPPED_PHOTO_NAME1 +
                "</styleUrl>\n" +
                "\t<ExtendedData xmlns:lc=\"http://www.locusmap.eu\">\n" +
                "\t\t<lc:attachment>" +
                ZIPPED_PHOTOS_FOLDER_NAME + ZIPPED_PHOTO_NAME3 +
                "</lc:attachment>\n" +
                "\t</ExtendedData>\n" +
                "</Placemark>" +
                "<Placemark>\n" +
                "\t<name>Placemark3</name>\n" +
                "\t<styleUrl>" +
                ZIPPED_PHOTO_NAME2 + "" +
                "</styleUrl>\n" +
                "</Placemark>" +
                "</Document>\n" +
                "</kml>\n";

        multipartXmlFile = new MockMultipartFile(
                originalKmlFilename, originalKmlFilename, null, kmlWithThreePhotos.getBytes(StandardCharsets.UTF_8));
        multipartZipFile = new MockMultipartFile(
                "additionalPhotosInKmz.kmz", "additionalPhotosInKmz.kmz", null, Files.readAllBytes(KMZ_WITH_ADDITIONAL_PHOTOS_IN_KMZ));

        multipartFilterDto.setMultipartXmlFile(multipartXmlFile);
        multipartFilterDto.setMultipartZipFile(multipartZipFile);

        Mockito.when(mockKmlHandler.getDocument(Mockito.any(InputStream.class))).thenCallRealMethod();
        Mockito.when(mockKmlHandler.getAsString(Mockito.any(Document.class))).thenCallRealMethod();

        //WHEN
        tmpFile = multipartFilterFileService.processMultipartFilterDto(multipartFilterDto, Locale.ENGLISH);

        //THEN
        List<String> zipEntriesNames = new ArrayList<>(5);

        assertDoesNotThrow(() -> {
            ZipInputStream zis = new ZipInputStream(Files.newInputStream(tmpFile));
            ZipEntry zipEntry;
            while ((zipEntry = zis.getNextEntry()) != null) {
                //Skip a folder name (which is also a zipEntry
                if (!zipEntry.getName().equals(ZIPPED_PHOTOS_FOLDER_NAME)) {
                    zipEntriesNames.add(zipEntry.getName());
                }
            }
        });
        assertEquals(5, zipEntriesNames.size());

        assertTrue(zipEntriesNames.contains(KMZ_PHOTOS_FOLDER_NAME));

        assertTrue(zipEntriesNames.contains("doc.kml"));

        assertTrue(zipEntriesNames.contains(KMZ_PHOTOS_FOLDER_NAME + ZIPPED_PHOTO_NAME1));
        assertTrue(zipEntriesNames.contains(KMZ_PHOTOS_FOLDER_NAME + ZIPPED_PHOTO_NAME2));
        assertTrue(zipEntriesNames.contains(KMZ_PHOTOS_FOLDER_NAME + ZIPPED_PHOTO_NAME3));

        assertFalse(zipEntriesNames.contains(KMZ_PHOTOS_FOLDER_NAME + ZIPPED_PHOTO_NAME4));
        assertFalse(zipEntriesNames.contains(KMZ_PHOTOS_FOLDER_NAME + ZIPPED_PHOTO_NAME5));

        assertFalse(zipEntriesNames.contains(KMZ_ADDITIONAL_PHOTO_NAME1));
        assertFalse(zipEntriesNames.contains(KMZ_ADDITIONAL_PHOTO_NAME2));
    }

    /**
     * If ZIP or KMZ file are created with non-UTF8 filenames inside {@link ZipInputStream} throws malformed exception.
     * This test is against the following:
     * java.lang.IllegalArgumentException: malformed input off : 6, length : 1
     * 	at java.base/java.lang.StringCoding.throwMalformed(StringCoding.java:698) ~[na:na]
     * 	at java.base/java.lang.StringCoding.decodeUTF8_0(StringCoding.java:885) ~[na:na]
     * 	at java.base/java.lang.StringCoding.newStringUTF8NoRepl(StringCoding.java:978) ~[na:na]
     * 	at java.base/java.lang.System$2.newStringUTF8NoRepl(System.java:2270) ~[na:na]
     * 	at java.base/java.util.zip.ZipCoder$UTF8.toString(ZipCoder.java:60) ~[na:na]
     * 	at java.base/java.util.zip.ZipCoder.toString(ZipCoder.java:87) ~[na:na]
     * 	at java.base/java.util.zip.ZipInputStream.readLOC(ZipInputStream.java:302) ~[na:na]
     * 	at java.base/java.util.zip.ZipInputStream.getNextEntry(ZipInputStream.java:124) ~[na:na]
     */
    @Test
    public void when_Kml_Contains_All_Photos_With_Non_UTF8_Names_Zip_Should_Contain_Initial_Names()
            throws ParserConfigurationException, TransformerException, SAXException, IOException {
        //GIVEN
        final Path ZIPPED_NON_UTF8_PHOTOS_ZIP = Paths.get("src/test/java/resources/№ фото для фильтрации №_7zip_default.zip");
        final String NON_UTF8_FOLDERNAME = "№ фото для фильтрации №";
        final String NON_UTF8_FILENAME1 = "@# $%  тест.jpg";
        final String NON_UTF8_FILENAME2 = "_надо отфильтровать.jpg";
        String kmlWithNonUTF8NamesPhotos = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" +
                "<Document>\n" +
                "\t<name>Test doc</name>\n" +
                "\t<atom:author><atom:name>Locus (Android)</atom:name></atom:author>\n" +
                "\t\t<Style id=\"" +
                NON_UTF8_FILENAME1 +
                "\">\n" +
                "\t\t<IconStyle>\n" +
                "\t\t\t<Icon><href>" +
                NON_UTF8_FOLDERNAME + NON_UTF8_FILENAME1 +
                "</href></Icon>\n" +
                "\t\t</IconStyle>\n" +
                "\t</Style>\n" +
                "\t\t<Style id=\"" +
                NON_UTF8_FILENAME2 +
                "\">\n" +
                "\t\t<IconStyle>\n" +
                "\t\t\t<Icon><href>" +
                NON_UTF8_FOLDERNAME + NON_UTF8_FILENAME2 +
                "</href></Icon>\n" +
                "\t\t</IconStyle>\n" +
                "\t</Style>\n" +
                "</Document>\n" +
                "</kml>\n";

        multipartXmlFile = new MockMultipartFile(
                originalKmlFilename, originalKmlFilename, null, kmlWithNonUTF8NamesPhotos.getBytes(StandardCharsets.UTF_8));
        multipartZipFile = new MockMultipartFile(
                ZIPPED_NON_UTF8_PHOTOS_ZIP.getFileName().toString(), ZIPPED_NON_UTF8_PHOTOS_ZIP.getFileName().toString(), null, Files.readAllBytes(ZIPPED_NON_UTF8_PHOTOS_ZIP));

        multipartFilterDto.setMultipartXmlFile(multipartXmlFile);
        multipartFilterDto.setMultipartZipFile(multipartZipFile);

        multipartFilterDto.setLocale(Locale.forLanguageTag("ru"));

        Mockito.when(mockKmlHandler.getDocument(Mockito.any(InputStream.class))).thenCallRealMethod();
        Mockito.when(mockKmlHandler.getAsString(Mockito.any(Document.class))).thenCallRealMethod();

        //WHEN
        tmpFile = multipartFilterFileService.processMultipartFilterDto(multipartFilterDto, Locale.ENGLISH);

        //THEN
        List<String> zipEntriesNames = new ArrayList<>(5);

        assertDoesNotThrow(() -> {
            ZipInputStream zis = new ZipInputStream(Files.newInputStream(tmpFile));
            ZipEntry zipEntry;
            while ((zipEntry = zis.getNextEntry()) != null) {
                //Skip a folder name (which is also a zipEntry
                if (!zipEntry.getName().equals(ZIPPED_PHOTOS_FOLDER_NAME)) {
                    zipEntriesNames.add(zipEntry.getName());
                }
            }
        });
        assertEquals(3, zipEntriesNames.size());

        assertTrue((zipEntriesNames.contains(ZIPPED_PHOTOS_FOLDER_NAME + ZIPPED_PHOTO_NAME1)));
        assertTrue((zipEntriesNames.contains(ZIPPED_PHOTOS_FOLDER_NAME + ZIPPED_PHOTO_NAME2)));
        assertTrue((zipEntriesNames.contains(ZIPPED_PHOTOS_FOLDER_NAME + ZIPPED_PHOTO_NAME3)));
        assertTrue((zipEntriesNames.contains(ZIPPED_PHOTOS_FOLDER_NAME + ZIPPED_PHOTO_NAME4)));
        assertTrue((zipEntriesNames.contains(ZIPPED_PHOTOS_FOLDER_NAME + ZIPPED_PHOTO_NAME5)));
    }

}