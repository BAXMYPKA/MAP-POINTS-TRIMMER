package mrbaxmypka.gmail.com.mapPointsTrimmer.xml;

import mrbaxmypka.gmail.com.mapPointsTrimmer.entitiesDto.MultipartDto;
import mrbaxmypka.gmail.com.mapPointsTrimmer.services.FileService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class LocusIconsHandlereTest {

    private static FileService fileService;
    private static KmlUtils kmlUtils;
    private static LocusIconsHandler locusIconsHandler;
    private static MultipartDto multipartDto;
    private static MultipartFile multipartFile;
    private static Document document;
    private static final String PICTOGRAM1_PNG = "Pictogram1.png";
    private static final String PICTOGRAM2_PNG = "Pictogram2.png";
    private static final String KML_DEFAULT_PATH = "files/";
    private static final String PICTOGRAM1_KML_PATH = KML_DEFAULT_PATH + PICTOGRAM1_PNG;
    private static final String PICTOGRAM2_KML_PATH = KML_DEFAULT_PATH + PICTOGRAM2_PNG;
    private final String STYLEMAP_ID_ATTRIBUTE_PREFIX = "styleMapOf:";
    private final String HIGHLIGHT_STYLE_ID_ATTRIBUTE_PREFIX = "highlightOf:";
    private static final ArrayList<String> PICTOGRAM_NAMES = new ArrayList<>(Arrays.asList(PICTOGRAM1_PNG, PICTOGRAM2_PNG));

    @BeforeAll
    public static void beforeAll() {
        fileService = Mockito.mock(FileService.class);
        Mockito.when(fileService.getPictogramsNames()).thenReturn(PICTOGRAM_NAMES);
        Mockito.when(fileService.getFileName(Mockito.anyString())).thenCallRealMethod();
    }

    @ParameterizedTest
    @ValueSource(strings = {"file:///sdcard/Locus/cache/images/1234567", "file:///usb/locus/cache/images/12345678901"})
    public void styles_With_Old_Basic_Photo_Id_should_Return_True(String photoId)
            throws IOException, ParserConfigurationException, SAXException {
        //GIVEN
        final String photoIconStyle = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" +
                "<Document>\n" +
                "\t<name>LOCUS03.07.2021</name>\n" +
                "\t<atom:author><atom:name>Locus (Android)</atom:name></atom:author>\n" +
                "\t\t<Style id=\"" +
                photoId +
                "\">\n" +
                "\t\t<IconStyle>\n" +
                "\t\t\t<Icon><href>" +
                "files/file-sdcardLocuscacheimages1604137344718.png" +
                "</href></Icon>\n" +
                "\t\t\t<hotSpot x=\"0.5\" y=\"0.0\" xunits=\"fraction\" yunits=\"fraction\" />\n" +
                "\t\t</IconStyle>\n" +
                "\t</Style>\n" +
                "</Document>\n" +
                "</kml>\n";
        multipartFile = new MockMultipartFile("LocusTestPoi.kml", "LocusTestPoi.kml",
                null, new ByteArrayInputStream(photoIconStyle.getBytes(StandardCharsets.UTF_8)));
        multipartDto = new MultipartDto(multipartFile);
        multipartDto.setReplaceLocusIcons(true);
        multipartDto.setPictogramName(PICTOGRAM1_PNG);
        document = XmlTestUtils.getDocument(multipartDto);
        kmlUtils = new KmlUtils(document, new XmlDomUtils(document));
        locusIconsHandler = new LocusIconsHandler(fileService, kmlUtils);
        Mockito.when(fileService.getPath(Mockito.anyString())).thenReturn(KML_DEFAULT_PATH);
        Node style = document.getElementsByTagName("Style").item(0);

        //WHEN
        boolean isLocusPhotoIconThumbnail = locusIconsHandler.isLocusPhotoIconThumbnail(style);

        //THEN
//        System.out.println(XmlTestUtils.getAsText(document));
        assertTrue(isLocusPhotoIconThumbnail);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "files/file-sdcardLocuscacheimages1234567.png",
            "../myFiles/file-sdcardLocuscacheimages12345678.png",
            "C://%myFiles%/photos+/file-sdcardLocuscacheimages12345679.png"})
    public void styles_With_Old_Basic_Href_To_Photo_Icon_Should_Return_True(String pathToPhotoIcon)
            throws IOException, ParserConfigurationException, SAXException {
        //GIVEN
        String styleId = "NonBasicId";
        final String photoIconStyle = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" +
                "<Document>\n" +
                "\t<name>LOCUS03.07.2021</name>\n" +
                "\t<atom:author><atom:name>Locus (Android)</atom:name></atom:author>\n" +
                "\t\t<Style id=\"" +
                styleId +
                "\">\n" +
                "\t\t<IconStyle>\n" +
                "\t\t\t<Icon><href>" +
                pathToPhotoIcon +
                "</href></Icon>\n" +
                "\t\t\t<hotSpot x=\"0.5\" y=\"0.0\" xunits=\"fraction\" yunits=\"fraction\" />\n" +
                "\t\t</IconStyle>\n" +
                "\t</Style>\n" +
                "</Document>\n" +
                "</kml>\n";
        multipartFile = new MockMultipartFile("LocusTestPoi.kml", "LocusTestPoi.kml",
                null, new ByteArrayInputStream(photoIconStyle.getBytes(StandardCharsets.UTF_8)));
        multipartDto = new MultipartDto(multipartFile);
        multipartDto.setReplaceLocusIcons(true);
        multipartDto.setPictogramName(PICTOGRAM1_PNG);
        document = XmlTestUtils.getDocument(multipartDto);
        kmlUtils = new KmlUtils(document, new XmlDomUtils(document));
        locusIconsHandler = new LocusIconsHandler(fileService, kmlUtils);
        Mockito.when(fileService.getPath(Mockito.anyString())).thenReturn(KML_DEFAULT_PATH);
        Node style = document.getElementsByTagName("Style").item(0);

        //WHEN
        boolean isLocusPhotoIconThumbnail = locusIconsHandler.isLocusPhotoIconThumbnail(style);

        //THEN
//        System.out.println(XmlTestUtils.getAsText(document));
        assertTrue(isLocusPhotoIconThumbnail);
    }

    @ParameterizedTest
    @ValueSource(strings = {"files/-1234567.png", "../myFiles/-12345678.png", "D://myFiles/photos+/-1234567890.png"})
    public void styles_With_Hyphenated_Href_To_Photo_Icon_Should_Return_True(String pathToPhotoIcon)
            throws IOException, ParserConfigurationException, SAXException {
        //GIVEN
        String fileName = fileService.getFileName(pathToPhotoIcon);
        String styleId = fileName.substring(0, fileName.lastIndexOf("."));
        final String photoIconStyle = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" +
                "<Document>\n" +
                "\t<name>LOCUS03.07.2021</name>\n" +
                "\t<atom:author><atom:name>Locus (Android)</atom:name></atom:author>\n" +
                "\t\t<Style id=\"" +
                styleId +
                "\">\n" +
                "\t\t<IconStyle>\n" +
                "\t\t\t<Icon><href>" +
                pathToPhotoIcon +
                "</href></Icon>\n" +
                "\t\t\t<hotSpot x=\"0.5\" y=\"0.0\" xunits=\"fraction\" yunits=\"fraction\" />\n" +
                "\t\t</IconStyle>\n" +
                "\t</Style>\n" +
                "</Document>\n" +
                "</kml>\n";
        multipartFile = new MockMultipartFile("LocusTestPoi.kml", "LocusTestPoi.kml",
                null, new ByteArrayInputStream(photoIconStyle.getBytes(StandardCharsets.UTF_8)));
        multipartDto = new MultipartDto(multipartFile);
        multipartDto.setReplaceLocusIcons(true);
        multipartDto.setPictogramName(PICTOGRAM1_PNG);
        document = XmlTestUtils.getDocument(multipartDto);
        kmlUtils = new KmlUtils(document, new XmlDomUtils(document));
        locusIconsHandler = new LocusIconsHandler(fileService, kmlUtils);
        Mockito.when(fileService.getPath(Mockito.anyString())).thenReturn(KML_DEFAULT_PATH);
        Node style = document.getElementsByTagName("Style").item(0);

        //WHEN
        boolean isLocusPhotoIconThumbnail = locusIconsHandler.isLocusPhotoIconThumbnail(style);

        //THEN
//        System.out.println(XmlTestUtils.getAsText(document));
        assertTrue(isLocusPhotoIconThumbnail);
    }

    @ParameterizedTest
    @ValueSource(strings = {"files/1234567.png", "../myFiles/12345678.png", "C://$myFiles%/photos+/1234567890.png"})
    public void styles_With_Non_Hyphenated_Href_To_Photo_Icon_Should_Return_True(String pathToPhotoIcon)
            throws IOException, ParserConfigurationException, SAXException {
        //GIVEN
        String fileName = fileService.getFileName(pathToPhotoIcon);
        String styleId = fileName.substring(0, fileName.lastIndexOf("."));
        final String photoIconStyle = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" +
                "<Document>\n" +
                "\t<name>LOCUS03.07.2021</name>\n" +
                "\t<atom:author><atom:name>Locus (Android)</atom:name></atom:author>\n" +
                "\t\t<Style id=\"" +
                styleId +
                "\">\n" +
                "\t\t<IconStyle>\n" +
                "\t\t\t<Icon><href>" +
                pathToPhotoIcon +
                "</href></Icon>\n" +
                "\t\t\t<hotSpot x=\"0.5\" y=\"0.0\" xunits=\"fraction\" yunits=\"fraction\" />\n" +
                "\t\t</IconStyle>\n" +
                "\t</Style>\n" +
                "</Document>\n" +
                "</kml>\n";
        multipartFile = new MockMultipartFile("LocusTestPoi.kml", "LocusTestPoi.kml",
                null, new ByteArrayInputStream(photoIconStyle.getBytes(StandardCharsets.UTF_8)));
        multipartDto = new MultipartDto(multipartFile);
        multipartDto.setReplaceLocusIcons(true);
        multipartDto.setPictogramName(PICTOGRAM1_PNG);
        document = XmlTestUtils.getDocument(multipartDto);
        kmlUtils = new KmlUtils(document, new XmlDomUtils(document));
        locusIconsHandler = new LocusIconsHandler(fileService, kmlUtils);
        Mockito.when(fileService.getPath(Mockito.anyString())).thenReturn(KML_DEFAULT_PATH);
        Node style = document.getElementsByTagName("Style").item(0);

        //WHEN
        boolean isLocusPhotoIconThumbnail = locusIconsHandler.isLocusPhotoIconThumbnail(style);

        //THEN
//        System.out.println(XmlTestUtils.getAsText(document));
        assertTrue(isLocusPhotoIconThumbnail);
    }


    @ParameterizedTest
    @ValueSource(strings = {"styleMapOf:file:///sdcard/Locus/cache/images/1234567", "styleMapOf:file:///usb/Locus/cache/images/12345678901"})
    public void styleMaps_With_Old_Basic_Photo_Id_should_Return_True(String photoId)
            throws IOException, ParserConfigurationException, SAXException {
        //GIVEN
        final String photoIconStyle = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" +
                "<Document>\n" +
                "\t<name>LOCUS03.07.2021</name>\n" +
                "\t<atom:author><atom:name>Locus (Android)</atom:name></atom:author>\n" +
                "\t\t\t<StyleMap id=\"" +
                photoId +
                "\">\n" +
                "\t\t\t\t<Pair>\n" +
                "\t\t\t\t\t<key>normal</key>\n" +
                "\t\t\t\t\t<styleUrl>" +
                "#" + photoId + ".png" +
                "</styleUrl>\n" +
                "\t\t\t\t</Pair>\n" +
                "\t\t\t\t<Pair>\n" +
                "\t\t\t\t\t<key>highlight</key>\n" +
                "\t\t\t\t\t<styleUrl>" +
                "#" + HIGHLIGHT_STYLE_ID_ATTRIBUTE_PREFIX + photoId + ".png" +
                "</styleUrl>\n" +
                "\t\t\t\t</Pair>\n" +
                "\t\t\t</StyleMap>\n" +

/*
                "\t<Style id=\"" +
                photoId +
                "\">\n" +
                "\t\t<IconStyle>\n" +
                "\t\t\t<Icon><href>files/file-sdcardLocuscacheimages1234567.png</href></Icon>\n" +
                "\t\t\t<hotSpot x=\"0.5\" y=\"0.0\" xunits=\"fraction\" yunits=\"fraction\" />\n" +
                "\t\t</IconStyle>\n" +
                "\t</Style>\n" +
                "\t<Style id=\"" +
                HIGHLIGHT_STYLE_ID_ATTRIBUTE_PREFIX + photoId +
                "\">\n" +
                "\t\t<IconStyle>\n" +
                "\t\t\t<Icon><href>files/file-sdcardLocuscacheimages1234567.png</href></Icon>\n" +
                "\t\t\t<hotSpot x=\"0.5\" y=\"0.0\" xunits=\"fraction\" yunits=\"fraction\" />\n" +
                "\t\t</IconStyle>\n" +
                "\t</Style>\n" +
*/

                "</Document>\n" +
                "</kml>\n";
        multipartFile = new MockMultipartFile("LocusTestPoi.kml", "LocusTestPoi.kml",
                null, new ByteArrayInputStream(photoIconStyle.getBytes(StandardCharsets.UTF_8)));
        multipartDto = new MultipartDto(multipartFile);
        multipartDto.setReplaceLocusIcons(true);
        multipartDto.setPictogramName(PICTOGRAM1_PNG);
        document = XmlTestUtils.getDocument(multipartDto);
        kmlUtils = new KmlUtils(document, new XmlDomUtils(document));
        locusIconsHandler = new LocusIconsHandler(fileService, kmlUtils);
        Mockito.when(fileService.getPath(Mockito.anyString())).thenReturn(KML_DEFAULT_PATH);
        Node style = document.getElementsByTagName("StyleMap").item(0);

        //WHEN
        boolean isLocusPhotoIconThumbnail = locusIconsHandler.isLocusPhotoIconThumbnail(style);

        //THEN
        assertTrue(isLocusPhotoIconThumbnail);
    }

    @ParameterizedTest
    @ValueSource(strings = {"files/-1234567.png", "../myFiles/0/-12345678.png", "C://$myFiles%/photos+/-1234567890.png"})
    public void styleMaps_Normal_Styles_With_Hyphenated_Icon_Names_should_Return_True(String href)
            throws IOException, ParserConfigurationException, SAXException {
        //GIVEN
        final String styleMapId = "styleMapOf:-1234567.png";
        String fileName = fileService.getFileName(href);
        final String photoIconStyle = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" +
                "<Document>\n" +
                "\t<name>LOCUS03.07.2021</name>\n" +
                "\t<atom:author><atom:name>Locus (Android)</atom:name></atom:author>\n" +
                "\t\t\t<StyleMap id=\"" +
                styleMapId +
                "\">\n" +
                "\t\t\t\t<Pair>\n" +
                "\t\t\t\t\t<key>normal</key>\n" +
                "\t\t\t\t\t<styleUrl>" +
                "#" + fileName +
                "</styleUrl>\n" +
                "\t\t\t\t</Pair>\n" +
                "\t\t\t\t<Pair>\n" +
                "\t\t\t\t\t<key>highlight</key>\n" +
                "\t\t\t\t\t<styleUrl>" +
                "#" + HIGHLIGHT_STYLE_ID_ATTRIBUTE_PREFIX + fileName +
                "</styleUrl>\n" +
                "\t\t\t\t</Pair>\n" +
                "\t\t\t</StyleMap>\n" +

                "\t<Style id=\"" +
                fileName +
                "\">\n" +
                "\t\t<IconStyle>\n" +
                "\t\t\t<Icon><href>" +
                href +
                "</href></Icon>\n" +
                "\t\t\t<hotSpot x=\"0.5\" y=\"0.0\" xunits=\"fraction\" yunits=\"fraction\" />\n" +
                "\t\t</IconStyle>\n" +
                "\t</Style>\n" +
                "\t<Style id=\"" +
                HIGHLIGHT_STYLE_ID_ATTRIBUTE_PREFIX + fileName +
                "\">\n" +
                "\t\t<IconStyle>\n" +
                "\t\t\t<Icon><href>" +
                href +
                "</href></Icon>\n" +
                "\t\t\t<hotSpot x=\"0.5\" y=\"0.0\" xunits=\"fraction\" yunits=\"fraction\" />\n" +
                "\t\t</IconStyle>\n" +
                "\t</Style>\n" +

                "</Document>\n" +
                "</kml>\n";
        multipartFile = new MockMultipartFile("LocusTestPoi.kml", "LocusTestPoi.kml",
                null, new ByteArrayInputStream(photoIconStyle.getBytes(StandardCharsets.UTF_8)));
        multipartDto = new MultipartDto(multipartFile);
        multipartDto.setReplaceLocusIcons(true);
        multipartDto.setPictogramName(PICTOGRAM1_PNG);
        document = XmlTestUtils.getDocument(multipartDto);
        kmlUtils = new KmlUtils(document, new XmlDomUtils(document));
        locusIconsHandler = new LocusIconsHandler(fileService, kmlUtils);
        Mockito.when(fileService.getPath(Mockito.anyString())).thenReturn(KML_DEFAULT_PATH);
        Node style = document.getElementsByTagName("StyleMap").item(0);

        //WHEN
        boolean isLocusPhotoIconThumbnail = locusIconsHandler.isLocusPhotoIconThumbnail(style);

        //THEN
        assertTrue(isLocusPhotoIconThumbnail);
    }

    @ParameterizedTest
    @ValueSource(strings = {"files/1234567.png", "../myFiles/0/12345678.png", "C://$myFiles%/photos+/1234567890.png"})
    public void styleMaps_Normal_Styles_With_Non_Hyphenated_Icon_Names_should_Return_True(String href)
            throws IOException, ParserConfigurationException, SAXException {
        //GIVEN
        final String styleMapId = "styleMapOf:-1234567.png";
        String fileName = fileService.getFileName(href);
        final String photoIconStyle = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" +
                "<Document>\n" +
                "\t<name>LOCUS03.07.2021</name>\n" +
                "\t<atom:author><atom:name>Locus (Android)</atom:name></atom:author>\n" +
                "\t\t\t<StyleMap id=\"" +
                styleMapId +
                "\">\n" +
                "\t\t\t\t<Pair>\n" +
                "\t\t\t\t\t<key>normal</key>\n" +
                "\t\t\t\t\t<styleUrl>" +
                "#" + fileName +
                "</styleUrl>\n" +
                "\t\t\t\t</Pair>\n" +
                "\t\t\t\t<Pair>\n" +
                "\t\t\t\t\t<key>highlight</key>\n" +
                "\t\t\t\t\t<styleUrl>" +
                "#" + HIGHLIGHT_STYLE_ID_ATTRIBUTE_PREFIX + fileName +
                "</styleUrl>\n" +
                "\t\t\t\t</Pair>\n" +
                "\t\t\t</StyleMap>\n" +

                "\t<Style id=\"" +
                fileName +
                "\">\n" +
                "\t\t<IconStyle>\n" +
                "\t\t\t<Icon><href>" +
                href +
                "</href></Icon>\n" +
                "\t\t\t<hotSpot x=\"0.5\" y=\"0.0\" xunits=\"fraction\" yunits=\"fraction\" />\n" +
                "\t\t</IconStyle>\n" +
                "\t</Style>\n" +
                "\t<Style id=\"" +
                HIGHLIGHT_STYLE_ID_ATTRIBUTE_PREFIX + fileName +
                "\">\n" +
                "\t\t<IconStyle>\n" +
                "\t\t\t<Icon><href>" +
                href +
                "</href></Icon>\n" +
                "\t\t\t<hotSpot x=\"0.5\" y=\"0.0\" xunits=\"fraction\" yunits=\"fraction\" />\n" +
                "\t\t</IconStyle>\n" +
                "\t</Style>\n" +

                "</Document>\n" +
                "</kml>\n";
        multipartFile = new MockMultipartFile("LocusTestPoi.kml", "LocusTestPoi.kml",
                null, new ByteArrayInputStream(photoIconStyle.getBytes(StandardCharsets.UTF_8)));
        multipartDto = new MultipartDto(multipartFile);
        multipartDto.setReplaceLocusIcons(true);
        multipartDto.setPictogramName(PICTOGRAM1_PNG);
        document = XmlTestUtils.getDocument(multipartDto);
        kmlUtils = new KmlUtils(document, new XmlDomUtils(document));
        locusIconsHandler = new LocusIconsHandler(fileService, kmlUtils);
        Mockito.when(fileService.getPath(Mockito.anyString())).thenReturn(KML_DEFAULT_PATH);
        Node style = document.getElementsByTagName("StyleMap").item(0);

        //WHEN
        boolean isLocusPhotoIconThumbnail = locusIconsHandler.isLocusPhotoIconThumbnail(style);

        //THEN
        assertTrue(isLocusPhotoIconThumbnail);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "123456,files/123456.png",
            "12345678901,../myFiles/12345678901.png",
            "123b4567,C://$myFiles%/photos+/123b4567.png",
            "12345678+9,C://$MY_Files$/photos+/12345678+9.png",
            "12345678,files/12345678.jpg"})
    public void styles_With_Hrefs_To_Non_Photo_Icons_Should_Return_False(String styleId, String pathToNonPhotoIcon)
            throws IOException, ParserConfigurationException, SAXException {
        //GIVEN
        final String photoIconStyle = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" +
                "<Document>\n" +
                "\t<name>LOCUS03.07.2021</name>\n" +
                "\t<atom:author><atom:name>Locus (Android)</atom:name></atom:author>\n" +
                "\t\t<Style id=\"" +
                styleId +
                "\">\n" +
                "\t\t<IconStyle>\n" +
                "\t\t\t<Icon><href>" +
                pathToNonPhotoIcon +
                "</href></Icon>\n" +
                "\t\t\t<hotSpot x=\"0.5\" y=\"0.0\" xunits=\"fraction\" yunits=\"fraction\" />\n" +
                "\t\t</IconStyle>\n" +
                "\t</Style>\n" +
                "</Document>\n" +
                "</kml>\n";
        multipartFile = new MockMultipartFile("LocusTestPoi.kml", "LocusTestPoi.kml",
                null, new ByteArrayInputStream(photoIconStyle.getBytes(StandardCharsets.UTF_8)));
        multipartDto = new MultipartDto(multipartFile);
        multipartDto.setReplaceLocusIcons(true);
        multipartDto.setPictogramName(PICTOGRAM1_PNG);
        document = XmlTestUtils.getDocument(multipartDto);
        kmlUtils = new KmlUtils(document, new XmlDomUtils(document));
        locusIconsHandler = new LocusIconsHandler(fileService, kmlUtils);
        Mockito.when(fileService.getPath(Mockito.anyString())).thenReturn(KML_DEFAULT_PATH);
        Node style = document.getElementsByTagName("Style").item(0);

        //WHEN
        boolean isLocusPhotoIconThumbnail = locusIconsHandler.isLocusPhotoIconThumbnail(style);

        //THEN
//        System.out.println(XmlTestUtils.getAsText(document));
        assertFalse(isLocusPhotoIconThumbnail);
    }

}