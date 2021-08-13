package mrbaxmypka.gmail.com.mapPointsTrimmer.xml;

import mrbaxmypka.gmail.com.mapPointsTrimmer.entitiesDto.MultipartDto;
import mrbaxmypka.gmail.com.mapPointsTrimmer.services.FileService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class LocusIconsHandlerStyleMapTest {

    private static FileService fileService;
    private static KmlUtils kmlUtils;
    private static LocusIconsHandler locusIconsHandler;
    private static MultipartDto multipartDto;
    private static MultipartFile multipartFile;
    private static Document document;
    private static final String PICTOGRAM1_PNG = "Pictogram1.png";
    private static final String PICTOGRAM2_PNG = "Pictogram2.PNG";
    private final String STYLEMAP_ID_ATTRIBUTE_PREFIX = "styleMapOf:";
    private final String HIGHLIGHT_STYLE_ID_ATTRIBUTE_PREFIX = "highlightOf:";
    private static final String KML_DEFAULT_PATH = "files/";
    private static final String PICTOGRAM1_KML_PATH = KML_DEFAULT_PATH + PICTOGRAM1_PNG;
    private static final String PICTOGRAM2_KML_PATH = KML_DEFAULT_PATH + PICTOGRAM2_PNG;
    private static final ArrayList<String> PICTOGRAM_NAMES = new ArrayList<>(Arrays.asList(PICTOGRAM1_PNG, PICTOGRAM2_PNG));

    @BeforeAll
    public static void beforeAll() {
        fileService = Mockito.mock(FileService.class);
        Mockito.when(fileService.getPictogramsNames()).thenReturn(PICTOGRAM_NAMES);
    }

    @Test
    public void styleMap_With_Photo_Id_should_be_Deleted()
            throws IOException, ParserConfigurationException, SAXException {
        //GIVEN
        final String styleMapPhotoId = "styleMapOf:file:///sdcard/Locus/cache/images/12345";
        final String photoIconStyleMap = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:kml=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\" xmlns:lc=\"http://www.locusmap.eu\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
                "\t<Document>\n" +
                "\t\t<name>StyleMaps test Document</name>\n" +
                "\t\t\t<StyleMap id=\"" +
                styleMapPhotoId +
                "\">\n" +
                "\t\t\t\t<Pair>\n" +
                "\t\t\t\t\t<key>normal</key>\n" +
                "\t\t\t\t\t<styleUrl>#file:///sdcard/Locus/cache/images/12345</styleUrl>\n" +
                "\t\t\t\t</Pair>\n" +
                "\t\t\t\t<Pair>\n" +
                "\t\t\t\t\t<key>highlight</key>\n" +
                "\t\t\t\t\t<styleUrl>#highlightOf:file:///sdcard/Locus/cache/images/12345</styleUrl>\n" +
                "\t\t\t\t</Pair>\n" +
                "\t\t\t</StyleMap>\n" +
                "\t\t\t<Style id=\"highlightOf:file:///sdcard/Locus/cache/images/12345\">\n" +
                "\t\t\t\t<IconStyle>\n" +
                "\t\t\t\t\t<Icon>\n" +
                "\t\t\t\t\t\t<href>files/file-sdcardLocuscacheimages12345.png</href>\n" +
                "\t\t\t\t\t</Icon>\n" +
                "\t\t\t\t</IconStyle>\n" +
                "\t\t\t</Style>\n" +
                "\t\t\t<Style id=\"file:///sdcard/Locus/cache/images/12345\">\n" +
                "\t\t\t\t<IconStyle>\n" +
                "\t\t\t\t\t<Icon>\n" +
                "\t\t\t\t\t\t<href>files/file-sdcardLocuscacheimages12345.png</href>\n" +
                "\t\t\t\t\t</Icon>\n" +
                "\t\t\t\t</IconStyle>\n" +
                "\t\t\t</Style>\n" +
                "\t</Document>\n" +
                "</kml>";
        multipartFile = new MockMultipartFile("LocusTestPoi.kml", "LocusTestPoi.kml",
                null, new ByteArrayInputStream(photoIconStyleMap.getBytes(StandardCharsets.UTF_8)));
        multipartDto = new MultipartDto(multipartFile);
        multipartDto.setReplaceLocusIcons(true);
        multipartDto.setPictogramName(PICTOGRAM1_PNG);
        document = XmlTestUtils.getDocument(multipartDto);
        kmlUtils = new KmlUtils(document, new XmlDomUtils(document));
        locusIconsHandler = new LocusIconsHandler(fileService, kmlUtils);
        Mockito.when(fileService.getPath(Mockito.anyString())).thenReturn(KML_DEFAULT_PATH);


        //WHEN

        locusIconsHandler.replaceLocusIcons(multipartDto);

        //THEN
//        System.out.println(XmlTestUtils.getAsText(document));
        assertFalse(XmlTestUtils.containsTagWithAttribute(document, "StyleMap", "id", styleMapPhotoId));
        //        assertFalse(processedKml.contains("<href>files/file-sdcardLocuscacheimages1589191676952.png</href>"));
    }

    @Test
    public void styleMap_With_Photo_Id_should_be_Replaced_With_New_Style_With_Pictogram()
            throws IOException, ParserConfigurationException, SAXException {
        //GIVEN
        final String styleMapPhotoId = "styleMapOf:file:///sdcard/Locus/cache/images/12345";
        final String styleMapPictogramId = "styleMapOf:" + PICTOGRAM1_PNG;
        final String photoIconStyleMap = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:kml=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\" xmlns:lc=\"http://www.locusmap.eu\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
                "\t<Document>\n" +
                "\t\t<name>StyleMaps test Document</name>\n" +
                "\t\t\t<StyleMap id=\"" +
                styleMapPhotoId +
                "\">\n" +
                "\t\t\t\t<Pair>\n" +
                "\t\t\t\t\t<key>normal</key>\n" +
                "\t\t\t\t\t<styleUrl>#file:///sdcard/Locus/cache/images/12345</styleUrl>\n" +
                "\t\t\t\t</Pair>\n" +
                "\t\t\t\t<Pair>\n" +
                "\t\t\t\t\t<key>highlight</key>\n" +
                "\t\t\t\t\t<styleUrl>#highlightOf:file:///sdcard/Locus/cache/images/12345</styleUrl>\n" +
                "\t\t\t\t</Pair>\n" +
                "\t\t\t</StyleMap>\n" +
                "\t\t\t<Style id=\"highlightOf:file:///sdcard/Locus/cache/images/12345\">\n" +
                "\t\t\t\t<IconStyle>\n" +
                "\t\t\t\t\t<color>e5ffffff</color>\n" +
                "\t\t\t\t\t<Icon>\n" +
                "\t\t\t\t\t\t<href>files/file-sdcardLocuscacheimages12345.png</href>\n" +
                "\t\t\t\t\t</Icon>\n" +
                "\t\t\t\t</IconStyle>\n" +
                "\t\t\t</Style>\n" +
                "\t\t\t<Style id=\"file:///sdcard/Locus/cache/images/12345\">\n" +
                "\t\t\t\t<IconStyle>\n" +
                "\t\t\t\t\t<Icon>\n" +
                "\t\t\t\t\t\t<href>files/file-sdcardLocuscacheimages12345.png</href>\n" +
                "\t\t\t\t\t</Icon>\n" +
                "\t\t\t\t</IconStyle>\n" +
                "\t\t\t</Style>\n" +
                "\t</Document>\n" +
                "</kml>";
        multipartFile = new MockMultipartFile("LocusTestPoi.kml", "LocusTestPoi.kml",
                null, new ByteArrayInputStream(photoIconStyleMap.getBytes(StandardCharsets.UTF_8)));
        multipartDto = new MultipartDto(multipartFile);
        multipartDto.setReplaceLocusIcons(true);
        multipartDto.setPictogramName(PICTOGRAM1_PNG);
        document = XmlTestUtils.getDocument(multipartDto);
        kmlUtils = new KmlUtils(document, new XmlDomUtils(document));
        locusIconsHandler = new LocusIconsHandler(fileService, kmlUtils);
        Mockito.when(fileService.getPath(Mockito.anyString())).thenReturn(KML_DEFAULT_PATH);


        //WHEN

        locusIconsHandler.replaceLocusIcons(multipartDto);

        //THEN
//        System.out.println(XmlTestUtils.getAsText(document));
        assertTrue(XmlTestUtils.containsTagWithAttribute(document, "StyleMap", "id", styleMapPictogramId));
        assertFalse(XmlTestUtils.containsTagWithAttribute(document, "StyleMap", "id", styleMapPhotoId));
    }

    @Test
    public void new_StyleMap_With_Pictogram_Id_Should_Contain_New_Styles_With_Appropriate_Pictograms()
            throws IOException, ParserConfigurationException, SAXException {
        //GIVEN
        final String styleMapPhotoId = "styleMapOf:file:///sdcard/Locus/cache/images/12345";
        final String photoIconStyleMap = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:kml=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\" xmlns:lc=\"http://www.locusmap.eu\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
                "\t<Document>\n" +
                "\t\t<name>StyleMaps test Document</name>\n" +
                "\t\t\t<StyleMap id=\"" +
                styleMapPhotoId +
                "\">\n" +
                "\t\t\t\t<Pair>\n" +
                "\t\t\t\t\t<key>normal</key>\n" +
                "\t\t\t\t\t<styleUrl>#file:///sdcard/Locus/cache/images/12345</styleUrl>\n" +
                "\t\t\t\t</Pair>\n" +
                "\t\t\t\t<Pair>\n" +
                "\t\t\t\t\t<key>highlight</key>\n" +
                "\t\t\t\t\t<styleUrl>#highlightOf:file:///sdcard/Locus/cache/images/12345</styleUrl>\n" +
                "\t\t\t\t</Pair>\n" +
                "\t\t\t</StyleMap>\n" +
                "\t\t\t<Style id=\"highlightOf:file:///sdcard/Locus/cache/images/12345\">\n" +
                "\t\t\t\t<IconStyle>\n" +
                "\t\t\t\t\t<color>e5ffffff</color>\n" +
                "\t\t\t\t\t<Icon>\n" +
                "\t\t\t\t\t\t<href>files/file-sdcardLocuscacheimages12345.png</href>\n" +
                "\t\t\t\t\t</Icon>\n" +
                "\t\t\t\t</IconStyle>\n" +
                "\t\t\t</Style>\n" +
                "\t\t\t<Style id=\"file:///sdcard/Locus/cache/images/12345\">\n" +
                "\t\t\t\t<IconStyle>\n" +
                "\t\t\t\t\t<Icon>\n" +
                "\t\t\t\t\t\t<href>files/file-sdcardLocuscacheimages12345.png</href>\n" +
                "\t\t\t\t\t</Icon>\n" +
                "\t\t\t\t</IconStyle>\n" +
                "\t\t\t</Style>\n" +
                "\t</Document>\n" +
                "</kml>";
        multipartFile = new MockMultipartFile("LocusTestPoi.kml", "LocusTestPoi.kml",
                null, new ByteArrayInputStream(photoIconStyleMap.getBytes(StandardCharsets.UTF_8)));
        multipartDto = new MultipartDto(multipartFile);
        multipartDto.setReplaceLocusIcons(true);
        multipartDto.setPictogramName(PICTOGRAM1_PNG);
        document = XmlTestUtils.getDocument(multipartDto);
        kmlUtils = new KmlUtils(document, new XmlDomUtils(document));
        locusIconsHandler = new LocusIconsHandler(fileService, kmlUtils);
        Mockito.when(fileService.getPath(Mockito.anyString())).thenReturn(KML_DEFAULT_PATH);

        final String styleMapPictogramId = kmlUtils.getSTYLEMAP_ID_ATTRIBUTE_PREFIX() + PICTOGRAM1_PNG;

        //WHEN
        locusIconsHandler.replaceLocusIcons(multipartDto);

        //THEN
//        System.out.println(XmlTestUtils.getAsText(document));
        assertTrue(XmlTestUtils.containsTagWithAttribute(document, "StyleMap", "id", styleMapPictogramId));
        assertAll(
                () -> assertTrue(XmlTestUtils.containsTagWithAttribute(
                        document, "StyleMap", "id", styleMapPictogramId)),
                () -> assertTrue(XmlTestUtils.containsTagWithChild(
                        document, "Pair", "key", "normal")),
                () -> assertTrue(XmlTestUtils.containsTagWithChild(
                        document, "Pair", "styleUrl", "#" + PICTOGRAM1_PNG)),
                () -> assertTrue(XmlTestUtils.containsTagWithChild(
                        document, "Pair", "key", "highlight")),
                () -> assertTrue(XmlTestUtils.containsTagWithChild(
                        document, "Pair", "styleUrl", "#" + kmlUtils.getHIGHLIGHT_STYLE_ID_ATTRIBUTE_PREFIX() + PICTOGRAM1_PNG))
        );
        assertFalse(XmlTestUtils.containsTagWithAttribute(document, "StyleMap", "id", styleMapPhotoId));
    }

    @Test
    public void existing_StyleMap_With_Pictogram_Should_Be_Preserved_As_A_Single_One()
            throws IOException, ParserConfigurationException, SAXException {
        //GIVEN
        final String styleMapPhotoId = "styleMapOf:file:///sdcard/Locus/cache/images/12345";
        final String styleMapPictogram1Id = STYLEMAP_ID_ATTRIBUTE_PREFIX + PICTOGRAM1_PNG;
        final String photoIconStyleMapsWithExistingPictogramStyleMap = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:kml=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\" xmlns:lc=\"http://www.locusmap.eu\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
                "\t<Document>\n" +
                "\t\t<name>StyleMaps test Document</name>\n" +
                "\t\t\t<StyleMap id=\"" +
                styleMapPhotoId +
                "\">\n" +
                "\t\t\t\t<Pair>\n" +
                "\t\t\t\t\t<key>normal</key>\n" +
                "\t\t\t\t\t<styleUrl>#file:///sdcard/Locus/cache/images/12345</styleUrl>\n" +
                "\t\t\t\t</Pair>\n" +
                "\t\t\t\t<Pair>\n" +
                "\t\t\t\t\t<key>highlight</key>\n" +
                "\t\t\t\t\t<styleUrl>#highlightOf:file:///sdcard/Locus/cache/images/12345</styleUrl>\n" +
                "\t\t\t\t</Pair>\n" +
                "\t\t\t</StyleMap>\n" +
                "\t\t\t<Style id=\"highlightOf:file:///sdcard/Locus/cache/images/12345\">\n" +
                "\t\t\t\t<IconStyle>\n" +
                "\t\t\t\t\t<Icon>\n" +
                "\t\t\t\t\t\t<href>files/file-sdcardLocuscacheimages12345.png</href>\n" +
                "\t\t\t\t\t</Icon>\n" +
                "\t\t\t\t</IconStyle>\n" +
                "\t\t\t</Style>\n" +
                "\t\t\t<Style id=\"file:///sdcard/Locus/cache/images/12345\">\n" +
                "\t\t\t\t<IconStyle>\n" +
                "\t\t\t\t\t<Icon>\n" +
                "\t\t\t\t\t\t<href>files/file-sdcardLocuscacheimages12345.png</href>\n" +
                "\t\t\t\t\t</Icon>\n" +
                "\t\t\t\t</IconStyle>\n" +
                "\t\t\t</Style>\n" +
                "\t\t\t<StyleMap id=\"" +
                styleMapPictogram1Id +
                "\">\n" +
                "\t\t\t\t<Pair>\n" +
                "\t\t\t\t\t<key>normal</key>\n" +
                "\t\t\t\t\t<styleUrl>#Pictogram1.png</styleUrl>\n" +
                "\t\t\t\t</Pair>\n" +
                "\t\t\t\t<Pair>\n" +
                "\t\t\t\t\t<key>highlight</key>\n" +
                "\t\t\t\t\t<styleUrl>#highlightOf:Pictogram1.png</styleUrl>\n" +
                "\t\t\t\t</Pair>\n" +
                "\t\t\t</StyleMap>\n" +
                "\t\t\t<Style id=\"" +
                HIGHLIGHT_STYLE_ID_ATTRIBUTE_PREFIX + PICTOGRAM1_PNG +
                "\">\n" +
                "\t\t\t\t<IconStyle>\n" +
                "\t\t\t\t<Icon>\n" +
                "\t\t\t\t\t<href>" +
                PICTOGRAM1_KML_PATH +
                "</href>\n" +
                "\t\t\t\t</Icon>\n" +
                "\t\t\t\t</IconStyle>\n" +
                "\t\t\t</Style>\n" +
                "\t\t\t<Style id=\"" +
                PICTOGRAM1_PNG +
                "\">\n" +
                "\t\t\t\t<IconStyle>\n" +
                "\t\t\t\t\t<Icon>\n" +
                "\t\t\t\t\t\t<href>" +
                PICTOGRAM1_KML_PATH +
                "</href>\n" +
                "\t\t\t\t\t</Icon>\n" +
                "\t\t\t\t</IconStyle>\n" +
                "\t\t\t</Style>\n" +
                "\t</Document>\n" +
                "</kml>";
        multipartFile = new MockMultipartFile("LocusTestPoi.kml", "LocusTestPoi.kml",
                null, new ByteArrayInputStream(photoIconStyleMapsWithExistingPictogramStyleMap.getBytes(StandardCharsets.UTF_8)));
        multipartDto = new MultipartDto(multipartFile);
        multipartDto.setReplaceLocusIcons(true);
        multipartDto.setPictogramName(PICTOGRAM1_PNG);
        document = XmlTestUtils.getDocument(multipartDto);
        kmlUtils = new KmlUtils(document, new XmlDomUtils(document));
        locusIconsHandler = new LocusIconsHandler(fileService, kmlUtils);
        Mockito.when(fileService.getPath(Mockito.anyString())).thenReturn(KML_DEFAULT_PATH);


        //WHEN
        locusIconsHandler.replaceLocusIcons(multipartDto);

        //THEN
//        System.out.println(XmlTestUtils.getAsText(document));
        assertAll(
                () -> assertEquals(1, document.getElementsByTagName("StyleMap").getLength()),
                () -> assertTrue(XmlTestUtils.containsTagWithAttribute(
                        document, "StyleMap", "id", styleMapPictogram1Id)),
                () -> assertTrue(XmlTestUtils.containsTagWithChild(
                        document, "Pair", "key", "normal")),
                () -> assertTrue(XmlTestUtils.containsTagWithChild(
                        document, "Pair", "styleUrl", "#" + PICTOGRAM1_PNG)),
                () -> assertTrue(XmlTestUtils.containsTagWithChild(
                        document, "Pair", "key", "highlight")),
                () -> assertTrue(XmlTestUtils.containsTagWithChild(
                        document, "Pair", "styleUrl", "#" + kmlUtils.getHIGHLIGHT_STYLE_ID_ATTRIBUTE_PREFIX() + PICTOGRAM1_PNG))
        );
    }


    @Test
    public void second_StyleMap_With_Pictogram_Id_Should_Not_Be_Deleted()
            throws IOException, ParserConfigurationException, SAXException {
        //GIVEN
        final String styleMapPhotoId = "styleMapOf:file:///sdcard/Locus/cache/images/12345";
        final String styleMapPictogram1Id = STYLEMAP_ID_ATTRIBUTE_PREFIX + PICTOGRAM1_PNG;
        final String styleMapPictogram2Id = STYLEMAP_ID_ATTRIBUTE_PREFIX + PICTOGRAM2_PNG;
        final String photoIconStyleMapsWithSecondPictogramStyleMap = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:kml=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\" xmlns:lc=\"http://www.locusmap.eu\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
                "\t<Document>\n" +
                "\t\t<name>StyleMaps test Document</name>\n" +
                "\t\t\t<StyleMap id=\"" +
                styleMapPhotoId +
                "\">\n" +
                "\t\t\t\t<Pair>\n" +
                "\t\t\t\t\t<key>normal</key>\n" +
                "\t\t\t\t\t<styleUrl>#file:///sdcard/Locus/cache/images/12345</styleUrl>\n" +
                "\t\t\t\t</Pair>\n" +
                "\t\t\t\t<Pair>\n" +
                "\t\t\t\t\t<key>highlight</key>\n" +
                "\t\t\t\t\t<styleUrl>#highlightOf:file:///sdcard/Locus/cache/images/12345</styleUrl>\n" +
                "\t\t\t\t</Pair>\n" +
                "\t\t\t</StyleMap>\n" +
                "\t\t\t<Style id=\"highlightOf:file:///sdcard/Locus/cache/images/12345\">\n" +
                "\t\t\t\t<IconStyle>\n" +
                "\t\t\t\t\t<Icon>\n" +
                "\t\t\t\t\t\t<href>files/file-sdcardLocuscacheimages12345.png</href>\n" +
                "\t\t\t\t\t</Icon>\n" +
                "\t\t\t\t</IconStyle>\n" +
                "\t\t\t</Style>\n" +
                "\t\t\t<Style id=\"file:///sdcard/Locus/cache/images/12345\">\n" +
                "\t\t\t\t<IconStyle>\n" +
                "\t\t\t\t\t<Icon>\n" +
                "\t\t\t\t\t\t<href>files/file-sdcardLocuscacheimages12345.png</href>\n" +
                "\t\t\t\t\t</Icon>\n" +
                "\t\t\t\t</IconStyle>\n" +
                "\t\t\t</Style>\n" +
                "\t\t\t<StyleMap id=\"" +
                styleMapPictogram1Id +
                "\">\n" +
                "\t\t\t\t<Pair>\n" +
                "\t\t\t\t\t<key>normal</key>\n" +
                "\t\t\t\t\t<styleUrl>" +
                "#" + PICTOGRAM1_PNG +
                "</styleUrl>\n" +
                "\t\t\t\t</Pair>\n" +
                "\t\t\t\t<Pair>\n" +
                "\t\t\t\t\t<key>highlight</key>\n" +
                "\t\t\t\t\t<styleUrl>" +
                "#" + HIGHLIGHT_STYLE_ID_ATTRIBUTE_PREFIX + PICTOGRAM1_PNG +
                "</styleUrl>\n" +
                "\t\t\t\t</Pair>\n" +
                "\t\t\t</StyleMap>\n" +
                "\t\t\t<Style id=\"" +
                HIGHLIGHT_STYLE_ID_ATTRIBUTE_PREFIX + PICTOGRAM1_PNG +
                "\">\n" +
                "\t\t\t\t<IconStyle>\n" +
                "\t\t\t\t<Icon>\n" +
                "\t\t\t\t\t<href>" +
                PICTOGRAM1_KML_PATH +
                "</href>\n" +
                "\t\t\t\t</Icon>\n" +
                "\t\t\t\t</IconStyle>\n" +
                "\t\t\t</Style>\n" +
                "\t\t\t<Style id=\"" +
                PICTOGRAM1_PNG +
                "\">\n" +
                "\t\t\t\t<IconStyle>\n" +
                "\t\t\t\t\t<Icon>\n" +
                "\t\t\t\t\t\t<href>" +
                PICTOGRAM1_KML_PATH +
                "</href>\n" +
                "\t\t\t\t\t</Icon>\n" +
                "\t\t\t\t</IconStyle>\n" +
                "\t\t\t</Style>\n" +
                "\t\t\t<StyleMap id=\"" +
                styleMapPictogram2Id +
                "\">\n" +
                "\t\t\t\t<Pair>\n" +
                "\t\t\t\t\t<key>normal</key>\n" +
                "\t\t\t\t\t<styleUrl>" +
                "#" + PICTOGRAM2_PNG +
                "</styleUrl>\n" +
                "\t\t\t\t</Pair>\n" +
                "\t\t\t\t<Pair>\n" +
                "\t\t\t\t\t<key>highlight</key>\n" +
                "\t\t\t\t\t<styleUrl>" +
                "#" + HIGHLIGHT_STYLE_ID_ATTRIBUTE_PREFIX + PICTOGRAM2_PNG +
                "</styleUrl>\n" +
                "\t\t\t\t</Pair>\n" +
                "\t\t\t</StyleMap>\n" +
                "\t\t\t<Style id=\"" +
                HIGHLIGHT_STYLE_ID_ATTRIBUTE_PREFIX + PICTOGRAM2_PNG +
                "\">\n" +
                "\t\t\t\t<IconStyle>\n" +
                "\t\t\t\t<Icon>\n" +
                "\t\t\t\t\t<href>" +
                PICTOGRAM2_KML_PATH +
                "</href>\n" +
                "\t\t\t\t</Icon>\n" +
                "\t\t\t\t</IconStyle>\n" +
                "\t\t\t</Style>\n" +
                "\t\t\t<Style id=\"" +
                PICTOGRAM2_PNG +
                "\">\n" +
                "\t\t\t\t<IconStyle>\n" +
                "\t\t\t\t\t<Icon>\n" +
                "\t\t\t\t\t\t<href>" +
                PICTOGRAM2_KML_PATH +
                "</href>\n" +
                "\t\t\t\t\t</Icon>\n" +
                "\t\t\t\t</IconStyle>\n" +
                "\t\t\t</Style>\n" +
                "\t</Document>\n" +
                "</kml>";
        multipartFile = new MockMultipartFile("LocusTestPoi.kml", "LocusTestPoi.kml",
                null, new ByteArrayInputStream(photoIconStyleMapsWithSecondPictogramStyleMap.getBytes(StandardCharsets.UTF_8)));
        multipartDto = new MultipartDto(multipartFile);
        multipartDto.setReplaceLocusIcons(true);
        multipartDto.setPictogramName(PICTOGRAM1_PNG);
        document = XmlTestUtils.getDocument(multipartDto);
        kmlUtils = new KmlUtils(document, new XmlDomUtils(document));
        locusIconsHandler = new LocusIconsHandler(fileService, kmlUtils);
        Mockito.when(fileService.getPath(Mockito.anyString())).thenReturn(KML_DEFAULT_PATH);


        //WHEN
        locusIconsHandler.replaceLocusIcons(multipartDto);

        //THEN
//        System.out.println(XmlTestUtils.getAsText(document));
        assertEquals(2, document.getElementsByTagName("StyleMap").getLength());
        assertAll(
                () -> assertTrue(XmlTestUtils.containsTagWithAttribute(
                        document, "StyleMap", "id", styleMapPictogram1Id)),
                () -> assertTrue(XmlTestUtils.containsTagWithChild(
                        document, "Pair", "key", "normal")),
                () -> assertTrue(XmlTestUtils.containsTagWithChild(
                        document, "Pair", "styleUrl", "#" + PICTOGRAM1_PNG)),
                () -> assertTrue(XmlTestUtils.containsTagWithChild(
                        document, "Pair", "key", "highlight")),
                () -> assertTrue(XmlTestUtils.containsTagWithChild(
                        document, "Pair", "styleUrl", "#" + kmlUtils.getHIGHLIGHT_STYLE_ID_ATTRIBUTE_PREFIX() + PICTOGRAM1_PNG))
        );
        assertAll(
                () -> assertTrue(XmlTestUtils.containsTagWithAttribute(
                        document, "StyleMap", "id", styleMapPictogram2Id)),
                () -> assertTrue(XmlTestUtils.containsTagWithChild(
                        document, "Pair", "key", "normal")),
                () -> assertTrue(XmlTestUtils.containsTagWithChild(
                        document, "Pair", "styleUrl", "#" + PICTOGRAM2_PNG)),
                () -> assertTrue(XmlTestUtils.containsTagWithChild(
                        document, "Pair", "key", "highlight")),
                () -> assertTrue(XmlTestUtils.containsTagWithChild(
                        document, "Pair", "styleUrl", "#" + kmlUtils.getHIGHLIGHT_STYLE_ID_ATTRIBUTE_PREFIX() + PICTOGRAM2_PNG))
        );
    }

    @Test
    public void only_StyleMap_With_Pictogram_Id_should_Be_Left()
            throws IOException, ParserConfigurationException, SAXException {
        //GIVEN
        final String styleMapPhotoId1 = "styleMapOf:file:///sdcard/Locus/cache/images/12345";
        final String styleMapPhotoId2 = "styleMapOf:file:///sdcard/Locus/cache/images/54321";
        final String photoIconStyleMaps = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:kml=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\" xmlns:lc=\"http://www.locusmap.eu\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
                "\t<Document>\n" +
                "\t\t<name>StyleMaps test Document</name>\n" +
                "\t\t\t<StyleMap id=\"" +
                styleMapPhotoId1 +
                "\">\n" +
                "\t\t\t\t<Pair>\n" +
                "\t\t\t\t\t<key>normal</key>\n" +
                "\t\t\t\t\t<styleUrl>#file:///sdcard/Locus/cache/images/12345</styleUrl>\n" +
                "\t\t\t\t</Pair>\n" +
                "\t\t\t\t<Pair>\n" +
                "\t\t\t\t\t<key>highlight</key>\n" +
                "\t\t\t\t\t<styleUrl>#highlightOf:file:///sdcard/Locus/cache/images/12345</styleUrl>\n" +
                "\t\t\t\t</Pair>\n" +
                "\t\t\t</StyleMap>\n" +
                "\t\t\t<Style id=\"highlightOf:file:///sdcard/Locus/cache/images/12345\">\n" +
                "\t\t\t\t<IconStyle>\n" +
                "\t\t\t\t\t<Icon>\n" +
                "\t\t\t\t\t\t<href>files/file-sdcardLocuscacheimages12345.png</href>\n" +
                "\t\t\t\t\t</Icon>\n" +
                "\t\t\t\t</IconStyle>\n" +
                "\t\t\t</Style>\n" +
                "\t\t\t<Style id=\"file:///sdcard/Locus/cache/images/12345\">\n" +
                "\t\t\t\t<IconStyle>\n" +
                "\t\t\t\t\t<Icon>\n" +
                "\t\t\t\t\t\t<href>files/file-sdcardLocuscacheimages12345.png</href>\n" +
                "\t\t\t\t\t</Icon>\n" +
                "\t\t\t\t</IconStyle>\n" +
                "\t\t\t</Style>\n" +
                "\t\t\t<StyleMap id=\"" +
                styleMapPhotoId2 +
                "\">\n" +
                "\t\t\t\t<Pair>\n" +
                "\t\t\t\t\t<key>normal</key>\n" +
                "\t\t\t\t\t<styleUrl>#file:///sdcard/Locus/cache/images/54321</styleUrl>\n" +
                "\t\t\t\t</Pair>\n" +
                "\t\t\t\t<Pair>\n" +
                "\t\t\t\t\t<key>highlight</key>\n" +
                "\t\t\t\t\t<styleUrl>#highlightOf:file:///sdcard/Locus/cache/images/54321</styleUrl>\n" +
                "\t\t\t\t</Pair>\n" +
                "\t\t\t</StyleMap>\n" +
                "\t\t\t<Style id=\"highlightOf:file:///sdcard/Locus/cache/images/54321\">\n" +
                "\t\t\t\t<IconStyle>\n" +
                "\t\t\t\t\t<Icon>\n" +
                "\t\t\t\t\t\t<href>files/file-sdcardLocuscacheimages54321.png</href>\n" +
                "\t\t\t\t\t</Icon>\n" +
                "\t\t\t\t</IconStyle>\n" +
                "\t\t\t</Style>\n" +
                "\t\t\t<Style id=\"file:///sdcard/Locus/cache/images/54321\">\n" +
                "\t\t\t\t<IconStyle>\n" +
                "\t\t\t\t\t<Icon>\n" +
                "\t\t\t\t\t\t<href>files/file-sdcardLocuscacheimages54321.png</href>\n" +
                "\t\t\t\t\t</Icon>\n" +
                "\t\t\t\t</IconStyle>\n" +
                "\t\t\t</Style>\n" +
                "\t</Document>\n" +
                "</kml>";
        multipartFile = new MockMultipartFile("LocusTestPoi.kml", "LocusTestPoi.kml",
                null, new ByteArrayInputStream(photoIconStyleMaps.getBytes(StandardCharsets.UTF_8)));
        multipartDto = new MultipartDto(multipartFile);
        multipartDto.setReplaceLocusIcons(true);
        multipartDto.setPictogramName(PICTOGRAM1_PNG);
        document = XmlTestUtils.getDocument(multipartDto);
        kmlUtils = new KmlUtils(document, new XmlDomUtils(document));
        locusIconsHandler = new LocusIconsHandler(fileService, kmlUtils);
        Mockito.when(fileService.getPath(Mockito.anyString())).thenReturn(KML_DEFAULT_PATH);


        //WHEN
        locusIconsHandler.replaceLocusIcons(multipartDto);

        //THEN
//        System.out.println(XmlTestUtils.getAsText(document));
        assertEquals(1, document.getElementsByTagName("StyleMap").getLength());
    }

    @ParameterizedTest
    @ValueSource(strings = {"files/", "../myFiles/", "/storage/0/Locus/data/media/photo/", "file:///D:/MyFolder/MyPOI/"})
    public void non_Photo_Icon_Locus_Id_Should_Be_Left_The_Same(String customPath)
            throws IOException, ParserConfigurationException, SAXException {
        //GIVEN
        final String existingPhotoPngFilename = "12345.png";
        final String existingPhotoPngFilenameId = "12345";
        final String styleMapPhotoId = STYLEMAP_ID_ATTRIBUTE_PREFIX + customPath + existingPhotoPngFilenameId;
        final String photoIconCustomPathStyleMap = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:kml=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\" xmlns:lc=\"http://www.locusmap.eu\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
                "\t<Document>\n" +
                "\t\t<name>StyleMaps test Document</name>\n" +
                "\t\t\t<StyleMap id=\"" +
                styleMapPhotoId +
                "\">\n" +
                "\t\t\t\t<Pair>\n" +
                "\t\t\t\t\t<key>normal</key>\n" +
                "\t\t\t\t\t<styleUrl>" +
                "#" + customPath + existingPhotoPngFilenameId +
                "</styleUrl>\n" +
                "\t\t\t\t</Pair>\n" +
                "\t\t\t\t<Pair>\n" +
                "\t\t\t\t\t<key>highlight</key>\n" +
                "\t\t\t\t\t<styleUrl>" +
                "#" + HIGHLIGHT_STYLE_ID_ATTRIBUTE_PREFIX + customPath + existingPhotoPngFilenameId +
                "</styleUrl>\n" +
                "\t\t\t\t</Pair>\n" +
                "\t\t\t</StyleMap>\n" +
                "\t\t\t<Style id=\"" +
                customPath + existingPhotoPngFilenameId +
                "\">\n" +
                "\t\t\t\t<IconStyle>\n" +
                "\t\t\t\t\t<Icon>\n" +
                "\t\t\t\t\t\t<href>" +
                customPath + existingPhotoPngFilename +
                "</href>\n" +
                "\t\t\t\t\t</Icon>\n" +
                "\t\t\t\t</IconStyle>\n" +
                "\t\t\t</Style>\n" +
                "\t\t\t<Style id=\"" +
                HIGHLIGHT_STYLE_ID_ATTRIBUTE_PREFIX + customPath + existingPhotoPngFilenameId +
                "\">\n" +
                "\t\t\t\t<IconStyle>\n" +
                "\t\t\t\t\t<Icon>\n" +
                "\t\t\t\t\t\t<href>" +
                customPath + existingPhotoPngFilename +
                "</href>\n" +
                "\t\t\t\t\t</Icon>\n" +
                "\t\t\t\t</IconStyle>\n" +
                "\t\t\t</Style>\n" +
                "\t</Document>\n" +
                "</kml>";
        multipartFile = new MockMultipartFile("LocusTestPoi.kml", "LocusTestPoi.kml",
                null, new ByteArrayInputStream(photoIconCustomPathStyleMap.getBytes(StandardCharsets.UTF_8)));
        multipartDto = new MultipartDto(multipartFile);
        multipartDto.setReplaceLocusIcons(true);
        multipartDto.setPictogramName(PICTOGRAM1_PNG);
        document = XmlTestUtils.getDocument(multipartDto);
        kmlUtils = new KmlUtils(document, new XmlDomUtils(document));
        locusIconsHandler = new LocusIconsHandler(fileService, kmlUtils);
        Mockito.when(fileService.getPath(Mockito.anyString())).thenReturn(KML_DEFAULT_PATH);

        //WHEN
        locusIconsHandler.replaceLocusIcons(multipartDto);

        //THEN
//        System.out.println(XmlTestUtils.getAsText(document));
        assertAll(
                () -> assertTrue(XmlTestUtils.containsTagWithAttribute(
                        document, "StyleMap", "id", STYLEMAP_ID_ATTRIBUTE_PREFIX + customPath + existingPhotoPngFilenameId)),
                () -> assertTrue(XmlTestUtils.containsTagWithChild(
                        document, "Pair", "key", "normal")),
                () -> assertTrue(XmlTestUtils.containsTagWithChild(
                        document, "Pair", "styleUrl", "#" + customPath + existingPhotoPngFilenameId)),
                () -> assertTrue(XmlTestUtils.containsTagWithChild(
                        document, "Pair", "key", "highlight")),
                () -> assertTrue(XmlTestUtils.containsTagWithChild(
                        document, "Pair", "styleUrl", "#" + kmlUtils.getHIGHLIGHT_STYLE_ID_ATTRIBUTE_PREFIX() + customPath + existingPhotoPngFilenameId))
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"../myFiles/", "/storage/0/Locus/data/media/photo/", "file:///D:/MyFolder/MyPOI/", "files/"})
    public void custom_Path_Should_Be_Used_For_StyleMap_Styles(String customPath)
            throws IOException, ParserConfigurationException, SAXException {
        //GIVEN
        final String photoFileId = "file:///sdcard/Locus/cache/images/54321";
        final String photoFileName = "file-sdcardLocuscacheimages54321.png";
        final String photoIconCustomPathStyleMap = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:kml=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\" xmlns:lc=\"http://www.locusmap.eu\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
                "\t<Document>\n" +
                "\t\t\t<StyleMap id=\"" +
                STYLEMAP_ID_ATTRIBUTE_PREFIX + photoFileId +
                "\">\n" +
                "\t\t\t\t<Pair>\n" +
                "\t\t\t\t\t<key>normal</key>\n" +
                "\t\t\t\t\t<styleUrl>" +
                "#" + photoFileId +
                "</styleUrl>\n" +
                "\t\t\t\t</Pair>\n" +
                "\t\t\t\t<Pair>\n" +
                "\t\t\t\t\t<key>highlight</key>\n" +
                "\t\t\t\t\t<styleUrl>" +
                "#" + HIGHLIGHT_STYLE_ID_ATTRIBUTE_PREFIX + photoFileId +
                "</styleUrl>\n" +
                "\t\t\t\t</Pair>\n" +
                "\t\t\t</StyleMap>\n" +
                "\t\t\t<Style id=\"" +
                photoFileId +
                "\">\n" +
                "\t\t\t\t<IconStyle>\n" +
                "\t\t\t\t\t<Icon>\n" +
                "\t\t\t\t\t\t<href>" +
                customPath + photoFileName +
                "</href>\n" +
                "\t\t\t\t\t</Icon>\n" +
                "\t\t\t\t</IconStyle>\n" +
                "\t\t\t</Style>\n" +
                "\t\t\t<Style id=\"" +
                HIGHLIGHT_STYLE_ID_ATTRIBUTE_PREFIX + photoFileId +
                "\">\n" +
                "\t\t\t\t<IconStyle>\n" +
                "\t\t\t\t\t<Icon>\n" +
                "\t\t\t\t\t\t<href>" +
                customPath + photoFileName +
                "</href>\n" +
                "\t\t\t\t\t</Icon>\n" +
                "\t\t\t\t</IconStyle>\n" +
                "\t\t\t</Style>\n" +
                "\t</Document>\n" +
                "</kml>";
        multipartFile = new MockMultipartFile("LocusTestPoi.kml", "LocusTestPoi.kml",
                null, new ByteArrayInputStream(photoIconCustomPathStyleMap.getBytes(StandardCharsets.UTF_8)));
        multipartDto = new MultipartDto(multipartFile);
        multipartDto.setReplaceLocusIcons(true);
        multipartDto.setPictogramName(PICTOGRAM1_PNG);
        document = XmlTestUtils.getDocument(multipartDto);
        kmlUtils = new KmlUtils(document, new XmlDomUtils(document));
        locusIconsHandler = new LocusIconsHandler(fileService, kmlUtils);
        Mockito.when(fileService.getPath(Mockito.anyString())).thenReturn(customPath);

        //WHEN
        locusIconsHandler.replaceLocusIcons(multipartDto);

        //THEN
//        System.out.println(XmlTestUtils.getAsText(document));
        assertAll(
                () -> assertTrue(XmlTestUtils.containsTagWithAttribute(
                        document, "StyleMap", "id", STYLEMAP_ID_ATTRIBUTE_PREFIX + PICTOGRAM1_PNG)),
                () -> assertTrue(XmlTestUtils.containsTagWithChild(
                        document, "Pair", "key", "normal")),
                () -> assertTrue(XmlTestUtils.containsTagWithChild(
                        document, "Pair", "styleUrl", "#" + PICTOGRAM1_PNG)),
                () -> assertTrue(XmlTestUtils.containsTagWithChild(
                        document, "Pair", "key", "highlight")),
                () -> assertTrue(XmlTestUtils.containsTagWithChild(
                        document, "Pair", "styleUrl", "#" + kmlUtils.getHIGHLIGHT_STYLE_ID_ATTRIBUTE_PREFIX() + PICTOGRAM1_PNG))
        );
        assertAll(
                () -> assertTrue(XmlTestUtils.containsTagWithAttribute(document, "Style", "id", PICTOGRAM1_PNG)),
                () -> assertTrue(XmlTestUtils.containsTagWithAttribute(document, "Style", "id", HIGHLIGHT_STYLE_ID_ATTRIBUTE_PREFIX + PICTOGRAM1_PNG)),
                () -> assertTrue(XmlTestUtils.containsTagsWithChildren(document, "Icon", 2, "href", customPath + PICTOGRAM1_PNG))
        );
    }

    @Test
    public void placemark_StyleUrl_Should_Be_Replaced() throws IOException, ParserConfigurationException, SAXException {
        //GIVEN
        final String photoFileId = "file:///sdcard/Locus/cache/images/54321";
        final String photoFileName = "file-sdcardLocuscacheimages54321.png";
        final String photoIconStyleMap = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:kml=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\" xmlns:lc=\"http://www.locusmap.eu\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
                "\t<Document>\n" +
                "\t\t\t<StyleMap id=\"" +
                STYLEMAP_ID_ATTRIBUTE_PREFIX + photoFileId +
                "\">\n" +
                "\t\t\t\t<Pair>\n" +
                "\t\t\t\t\t<key>normal</key>\n" +
                "\t\t\t\t\t<styleUrl>" +
                "#" + photoFileId +
                "</styleUrl>\n" +
                "\t\t\t\t</Pair>\n" +
                "\t\t\t\t<Pair>\n" +
                "\t\t\t\t\t<key>highlight</key>\n" +
                "\t\t\t\t\t<styleUrl>" +
                "#" + HIGHLIGHT_STYLE_ID_ATTRIBUTE_PREFIX + photoFileId +
                "</styleUrl>\n" +
                "\t\t\t\t</Pair>\n" +
                "\t\t\t</StyleMap>\n" +
                "\t\t\t<Style id=\"" +
                photoFileId +
                "\">\n" +
                "\t\t\t\t<IconStyle>\n" +
                "\t\t\t\t\t<Icon>\n" +
                "\t\t\t\t\t\t<href>" +
                KML_DEFAULT_PATH + photoFileName +
                "</href>\n" +
                "\t\t\t\t\t</Icon>\n" +
                "\t\t\t\t</IconStyle>\n" +
                "\t\t\t</Style>\n" +
                "\t\t\t<Style id=\"" +
                HIGHLIGHT_STYLE_ID_ATTRIBUTE_PREFIX + photoFileId +
                "\">\n" +
                "\t\t\t\t<IconStyle>\n" +
                "\t\t\t\t\t<Icon>\n" +
                "\t\t\t\t\t\t<href>" +
                KML_DEFAULT_PATH + photoFileName +
                "</href>\n" +
                "\t\t\t\t\t</Icon>\n" +
                "\t\t\t\t</IconStyle>\n" +
                "\t\t\t</Style>\n" +
                "\t\t\t<Placemark>\n" +
                "\t\t\t\t<name>Photo poi 1</name>\n" +
                "\t\t\t\t<description><![CDATA[]]></description>\n" +
                "\t\t\t\t<styleUrl>" +
                "#" + STYLEMAP_ID_ATTRIBUTE_PREFIX + photoFileId +
                "</styleUrl>\n" +
                "\t\t\t\t<ExtendedData>\n" +
                "\t\t\t\t\t<lc:attachment>files/p__20180819_133740.jpg</lc:attachment>\n" +
                "\t\t\t\t</ExtendedData>\n" +
                "\t\t\t\t<Point>\n" +
                "\t\t\t\t</Point>\n" +
                "\t\t\t</Placemark>\n" +
                "\t</Document>\n" +
                "</kml>";
        multipartFile = new MockMultipartFile("LocusTestPoi.kml", "LocusTestPoi.kml",
                null, new ByteArrayInputStream(photoIconStyleMap.getBytes(StandardCharsets.UTF_8)));
        multipartDto = new MultipartDto(multipartFile);
        multipartDto.setReplaceLocusIcons(true);
        multipartDto.setPictogramName(PICTOGRAM1_PNG);
        document = XmlTestUtils.getDocument(multipartDto);
        kmlUtils = new KmlUtils(document, new XmlDomUtils(document));
        locusIconsHandler = new LocusIconsHandler(fileService, kmlUtils);
        Mockito.when(fileService.getPath(Mockito.anyString())).thenReturn(KML_DEFAULT_PATH);

        //WHEN
        locusIconsHandler.replaceLocusIcons(multipartDto);

        //THEN
//        System.out.println(XmlTestUtils.getAsText(document));
        assertTrue(XmlTestUtils.containsTagWithChild(document, "Placemark", "styleUrl", "#" + STYLEMAP_ID_ATTRIBUTE_PREFIX + PICTOGRAM1_PNG));
    }

    @Test
    public void placemarks_StyleUrls_Should_Be_Replaced() throws IOException, ParserConfigurationException, SAXException {
        //GIVEN
        final String photoFileId1 = "file:///sdcard/Locus/cache/images/12345";
        final String photoFileId2 = "file:///sdcard/Locus/cache/images/54321";
        final String photoFileName1 = "file-sdcardLocuscacheimages12345.png";
        final String photoFileName2 = "file-sdcardLocuscacheimages54321.png";
        final String photoIconCustomPathStyleMap = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:kml=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\" xmlns:lc=\"http://www.locusmap.eu\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
                "\t<Document>\n" +
                "\t\t\t<StyleMap id=\"" +
                STYLEMAP_ID_ATTRIBUTE_PREFIX + photoFileId1 +
                "\">\n" +
                "\t\t\t\t<Pair>\n" +
                "\t\t\t\t\t<key>normal</key>\n" +
                "\t\t\t\t\t<styleUrl>" +
                "#" + photoFileId1 +
                "</styleUrl>\n" +
                "\t\t\t\t</Pair>\n" +
                "\t\t\t\t<Pair>\n" +
                "\t\t\t\t\t<key>highlight</key>\n" +
                "\t\t\t\t\t<styleUrl>" +
                "#" + HIGHLIGHT_STYLE_ID_ATTRIBUTE_PREFIX + photoFileId1 +
                "</styleUrl>\n" +
                "\t\t\t\t</Pair>\n" +
                "\t\t\t</StyleMap>\n" +
                "\t\t\t<Style id=\"" +
                photoFileId1 +
                "\">\n" +
                "\t\t\t\t<IconStyle>\n" +
                "\t\t\t\t\t<Icon>\n" +
                "\t\t\t\t\t\t<href>" +
                KML_DEFAULT_PATH + photoFileName1 +
                "</href>\n" +
                "\t\t\t\t\t</Icon>\n" +
                "\t\t\t\t</IconStyle>\n" +
                "\t\t\t</Style>\n" +
                "\t\t\t<Style id=\"" +
                HIGHLIGHT_STYLE_ID_ATTRIBUTE_PREFIX + photoFileId1 +
                "\">\n" +
                "\t\t\t\t<IconStyle>\n" +
                "\t\t\t\t\t<Icon>\n" +
                "\t\t\t\t\t\t<href>" +
                KML_DEFAULT_PATH + photoFileName1 +
                "</href>\n" +
                "\t\t\t\t\t</Icon>\n" +
                "\t\t\t\t</IconStyle>\n" +
                "\t\t\t</Style>\n" +

                "\t\t\t<StyleMap id=\"" +
                STYLEMAP_ID_ATTRIBUTE_PREFIX + photoFileId2 +
                "\">\n" +
                "\t\t\t\t<Pair>\n" +
                "\t\t\t\t\t<key>normal</key>\n" +
                "\t\t\t\t\t<styleUrl>" +
                "#" + photoFileId2 +
                "</styleUrl>\n" +
                "\t\t\t\t</Pair>\n" +
                "\t\t\t\t<Pair>\n" +
                "\t\t\t\t\t<key>highlight</key>\n" +
                "\t\t\t\t\t<styleUrl>" +
                "#" + HIGHLIGHT_STYLE_ID_ATTRIBUTE_PREFIX + photoFileId2 +
                "</styleUrl>\n" +
                "\t\t\t\t</Pair>\n" +
                "\t\t\t</StyleMap>\n" +
                "\t\t\t<Style id=\"" +
                photoFileId2 +
                "\">\n" +
                "\t\t\t\t<IconStyle>\n" +
                "\t\t\t\t\t<Icon>\n" +
                "\t\t\t\t\t\t<href>" +
                KML_DEFAULT_PATH + photoFileName2 +
                "</href>\n" +
                "\t\t\t\t\t</Icon>\n" +
                "\t\t\t\t</IconStyle>\n" +
                "\t\t\t</Style>\n" +
                "\t\t\t<Style id=\"" +
                HIGHLIGHT_STYLE_ID_ATTRIBUTE_PREFIX + photoFileId2 +
                "\">\n" +
                "\t\t\t\t<IconStyle>\n" +
                "\t\t\t\t\t<Icon>\n" +
                "\t\t\t\t\t\t<href>" +
                KML_DEFAULT_PATH + photoFileName2 +
                "</href>\n" +
                "\t\t\t\t\t</Icon>\n" +
                "\t\t\t\t</IconStyle>\n" +
                "\t\t\t</Style>\n" +

                "\t\t\t<Placemark>\n" +
                "\t\t\t\t<name>Photo poi 1</name>\n" +
                "\t\t\t\t<description><![CDATA[]]></description>\n" +
                "\t\t\t\t<styleUrl>" +
                "#" + STYLEMAP_ID_ATTRIBUTE_PREFIX + photoFileId1 +
                "</styleUrl>\n" +
                "\t\t\t\t<ExtendedData>\n" +
                "\t\t\t\t\t<lc:attachment>files/p__20180819_133740.jpg</lc:attachment>\n" +
                "\t\t\t\t</ExtendedData>\n" +
                "\t\t\t\t<Point>\n" +
                "\t\t\t\t</Point>\n" +
                "\t\t\t</Placemark>\n" +

                "\t\t\t<Placemark>\n" +
                "\t\t\t\t<name>Photo poi 2</name>\n" +
                "\t\t\t\t<description><![CDATA[]]></description>\n" +
                "\t\t\t\t<styleUrl>" +
                "#" + STYLEMAP_ID_ATTRIBUTE_PREFIX + photoFileId2 +
                "</styleUrl>\n" +
                "\t\t\t\t<ExtendedData>\n" +
                "\t\t\t\t\t<lc:attachment>files/p__20180819_133740.jpg</lc:attachment>\n" +
                "\t\t\t\t</ExtendedData>\n" +
                "\t\t\t\t<Point>\n" +
                "\t\t\t\t</Point>\n" +
                "\t\t\t</Placemark>\n" +

                "\t</Document>\n" +
                "</kml>";
        multipartFile = new MockMultipartFile("LocusTestPoi.kml", "LocusTestPoi.kml",
                null, new ByteArrayInputStream(photoIconCustomPathStyleMap.getBytes(StandardCharsets.UTF_8)));
        multipartDto = new MultipartDto(multipartFile);
        multipartDto.setReplaceLocusIcons(true);
        multipartDto.setPictogramName(PICTOGRAM1_PNG);
        document = XmlTestUtils.getDocument(multipartDto);
        kmlUtils = new KmlUtils(document, new XmlDomUtils(document));
        locusIconsHandler = new LocusIconsHandler(fileService, kmlUtils);
        Mockito.when(fileService.getPath(Mockito.anyString())).thenReturn(KML_DEFAULT_PATH);

        //WHEN
        locusIconsHandler.replaceLocusIcons(multipartDto);

        //THEN
//        System.out.println(XmlTestUtils.getAsText(document));
        assertTrue(XmlTestUtils.containsTagsWithChildren(document, "Placemark", 2, "styleUrl", "#" + STYLEMAP_ID_ATTRIBUTE_PREFIX + PICTOGRAM1_PNG));
        assertTrue(XmlTestUtils.containsTagsWithChildren(document, "StyleMap", 1, "Pair", null));
    }

    @Test
    public void only_Placemarks_Photo_StyleUrls_Should_Be_Replaced() throws IOException, ParserConfigurationException, SAXException {
        //GIVEN
        final String photoFileId1 = "file:///sdcard/Locus/cache/images/12345";
        final String photoFileId2 = "file:///sdcard/Locus/cache/images/54321";
        final String photoFileName1 = "file-sdcardLocuscacheimages12345.png";
        final String photoFileName2 = "file-sdcardLocuscacheimages54321.png";
        final String photoIconCustomPathStyleMap = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:kml=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\" xmlns:lc=\"http://www.locusmap.eu\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
                "\t<Document>\n" +
                "\t\t\t<StyleMap id=\"" +
                STYLEMAP_ID_ATTRIBUTE_PREFIX + photoFileId1 +
                "\">\n" +
                "\t\t\t\t<Pair>\n" +
                "\t\t\t\t\t<key>normal</key>\n" +
                "\t\t\t\t\t<styleUrl>" +
                "#" + photoFileId1 +
                "</styleUrl>\n" +
                "\t\t\t\t</Pair>\n" +
                "\t\t\t\t<Pair>\n" +
                "\t\t\t\t\t<key>highlight</key>\n" +
                "\t\t\t\t\t<styleUrl>" +
                "#" + HIGHLIGHT_STYLE_ID_ATTRIBUTE_PREFIX + photoFileId1 +
                "</styleUrl>\n" +
                "\t\t\t\t</Pair>\n" +
                "\t\t\t</StyleMap>\n" +
                "\t\t\t<Style id=\"" +
                photoFileId1 +
                "\">\n" +
                "\t\t\t\t<IconStyle>\n" +
                "\t\t\t\t\t<Icon>\n" +
                "\t\t\t\t\t\t<href>" +
                KML_DEFAULT_PATH + photoFileName1 +
                "</href>\n" +
                "\t\t\t\t\t</Icon>\n" +
                "\t\t\t\t</IconStyle>\n" +
                "\t\t\t</Style>\n" +
                "\t\t\t<Style id=\"" +
                HIGHLIGHT_STYLE_ID_ATTRIBUTE_PREFIX + photoFileId1 +
                "\">\n" +
                "\t\t\t\t<IconStyle>\n" +
                "\t\t\t\t\t<Icon>\n" +
                "\t\t\t\t\t\t<href>" +
                KML_DEFAULT_PATH + photoFileName1 +
                "</href>\n" +
                "\t\t\t\t\t</Icon>\n" +
                "\t\t\t\t</IconStyle>\n" +
                "\t\t\t</Style>\n" +

                "\t\t\t<StyleMap id=\"" +
                STYLEMAP_ID_ATTRIBUTE_PREFIX + photoFileId2 +
                "\">\n" +
                "\t\t\t\t<Pair>\n" +
                "\t\t\t\t\t<key>normal</key>\n" +
                "\t\t\t\t\t<styleUrl>" +
                "#" + photoFileId2 +
                "</styleUrl>\n" +
                "\t\t\t\t</Pair>\n" +
                "\t\t\t\t<Pair>\n" +
                "\t\t\t\t\t<key>highlight</key>\n" +
                "\t\t\t\t\t<styleUrl>" +
                "#" + HIGHLIGHT_STYLE_ID_ATTRIBUTE_PREFIX + photoFileId2 +
                "</styleUrl>\n" +
                "\t\t\t\t</Pair>\n" +
                "\t\t\t</StyleMap>\n" +
                "\t\t\t<Style id=\"" +
                photoFileId2 +
                "\">\n" +
                "\t\t\t\t<IconStyle>\n" +
                "\t\t\t\t\t<Icon>\n" +
                "\t\t\t\t\t\t<href>" +
                KML_DEFAULT_PATH + photoFileName2 +
                "</href>\n" +
                "\t\t\t\t\t</Icon>\n" +
                "\t\t\t\t</IconStyle>\n" +
                "\t\t\t</Style>\n" +
                "\t\t\t<Style id=\"" +
                HIGHLIGHT_STYLE_ID_ATTRIBUTE_PREFIX + photoFileId2 +
                "\">\n" +
                "\t\t\t\t<IconStyle>\n" +
                "\t\t\t\t\t<Icon>\n" +
                "\t\t\t\t\t\t<href>" +
                KML_DEFAULT_PATH + photoFileName2 +
                "</href>\n" +
                "\t\t\t\t\t</Icon>\n" +
                "\t\t\t\t</IconStyle>\n" +
                "\t\t\t</Style>\n" +

                "\t\t\t<StyleMap id=\"" +
                STYLEMAP_ID_ATTRIBUTE_PREFIX + PICTOGRAM2_PNG +
                "\">\n" +
                "\t\t\t\t<Pair>\n" +
                "\t\t\t\t\t<key>normal</key>\n" +
                "\t\t\t\t\t<styleUrl>#Pictogram2.PNG</styleUrl>\n" +
                "\t\t\t\t</Pair>\n" +
                "\t\t\t\t<Pair>\n" +
                "\t\t\t\t\t<key>highlight</key>\n" +
                "\t\t\t\t\t<styleUrl>#highlightOf:Pictogram2.PNG</styleUrl>\n" +
                "\t\t\t\t</Pair>\n" +
                "\t\t\t</StyleMap>\n" +
                "\t\t\t<Style id=\"" +
                PICTOGRAM2_PNG +
                "\">\n" +
                "\t\t\t\t<IconStyle>\n" +
                "\t\t\t\t\t<Icon>\n" +
                "\t\t\t\t\t\t<href>files/Pictogram2.PNG</href>\n" +
                "\t\t\t\t\t</Icon>\n" +
                "\t\t\t\t</IconStyle>\n" +
                "\t\t\t</Style>\n" +
                "\t\t\t<Style id=\"" +
                HIGHLIGHT_STYLE_ID_ATTRIBUTE_PREFIX + PICTOGRAM2_PNG +
                "\">\n" +
                "\t\t\t\t<IconStyle>\n" +
                "\t\t\t\t<Icon>\n" +
                "\t\t\t\t\t<href>files/Pictogram2.PNG</href>\n" +
                "\t\t\t\t</Icon>\n" +
                "\t\t\t\t</IconStyle>\n" +
                "\t\t\t</Style>\n" +

                "\t\t\t<Placemark>\n" +
                "\t\t\t\t<name>Photo poi 1</name>\n" +
                "\t\t\t\t<description><![CDATA[]]></description>\n" +
                "\t\t\t\t<styleUrl>" +
                "#" + STYLEMAP_ID_ATTRIBUTE_PREFIX + photoFileId1 +
                "</styleUrl>\n" +
                "\t\t\t\t<ExtendedData>\n" +
                "\t\t\t\t\t<lc:attachment>files/p__20180819_133740.jpg</lc:attachment>\n" +
                "\t\t\t\t</ExtendedData>\n" +
                "\t\t\t\t<Point>\n" +
                "\t\t\t\t</Point>\n" +
                "\t\t\t</Placemark>\n" +

                "\t\t\t<Placemark>\n" +
                "\t\t\t\t<name>Photo poi 2</name>\n" +
                "\t\t\t\t<description><![CDATA[]]></description>\n" +
                "\t\t\t\t<styleUrl>" +
                "#" + STYLEMAP_ID_ATTRIBUTE_PREFIX + photoFileId2 +
                "</styleUrl>\n" +
                "\t\t\t\t<ExtendedData>\n" +
                "\t\t\t\t\t<lc:attachment>files/p__20180819_133740.jpg</lc:attachment>\n" +
                "\t\t\t\t</ExtendedData>\n" +
                "\t\t\t\t<Point>\n" +
                "\t\t\t\t</Point>\n" +
                "\t\t\t</Placemark>\n" +

                "\t\t\t<Placemark>\n" +
                "\t\t\t\t<name>Pictogram poi 2</name>\n" +
                "\t\t\t\t<description><![CDATA[]]></description>\n" +
                "\t\t\t\t<styleUrl>" +
                "#" + STYLEMAP_ID_ATTRIBUTE_PREFIX + PICTOGRAM2_PNG +
                "</styleUrl>\n" +
                "\t\t\t\t<ExtendedData>\n" +
                "\t\t\t\t\t<lc:attachment>files/p__3.jpg</lc:attachment>\n" +
                "\t\t\t\t</ExtendedData>\n" +
                "\t\t\t\t<Point>\n" +
                "\t\t\t\t</Point>\n" +
                "\t\t\t</Placemark>\n" +

                "\t</Document>\n" +
                "</kml>";
        multipartFile = new MockMultipartFile("LocusTestPoi.kml", "LocusTestPoi.kml",
                null, new ByteArrayInputStream(photoIconCustomPathStyleMap.getBytes(StandardCharsets.UTF_8)));
        multipartDto = new MultipartDto(multipartFile);
        multipartDto.setReplaceLocusIcons(true);
        multipartDto.setPictogramName(PICTOGRAM1_PNG);
        document = XmlTestUtils.getDocument(multipartDto);
        kmlUtils = new KmlUtils(document, new XmlDomUtils(document));
        locusIconsHandler = new LocusIconsHandler(fileService, kmlUtils);
        Mockito.when(fileService.getPath(Mockito.anyString())).thenReturn(KML_DEFAULT_PATH);

        //WHEN
        locusIconsHandler.replaceLocusIcons(multipartDto);

        //THEN
//        System.out.println(XmlTestUtils.getAsText(document));
        assertTrue(XmlTestUtils.containsTagsWithChildren(document, "Placemark", 2, "styleUrl", "#" + STYLEMAP_ID_ATTRIBUTE_PREFIX + PICTOGRAM1_PNG));
        assertTrue(XmlTestUtils.containsTagsWithChildren(document, "Placemark", 1, "styleUrl", "#" + STYLEMAP_ID_ATTRIBUTE_PREFIX + PICTOGRAM2_PNG));
        assertTrue(XmlTestUtils.containsTagsWithChildren(document, "StyleMap", 2, "Pair", null));
        assertTrue(XmlTestUtils.containsTagWithAttribute(document, "Style", "id", PICTOGRAM2_PNG));
        assertTrue(XmlTestUtils.containsTagWithAttribute(document, "Style", "id", HIGHLIGHT_STYLE_ID_ATTRIBUTE_PREFIX + PICTOGRAM2_PNG));
    }

}