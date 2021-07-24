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

class LocusIconsHandlerStyleTest {

    private static FileService fileService;
    private static KmlUtils kmlUtils;
    private static LocusIconsHandler locusIconsHandler;
    private static MultipartDto multipartDto;
    private static MultipartFile multipartFile;
    private static Document document;
    private static final String PIC1 = "Pictogram1";
    private static final String PIC2 = "Pictogram2";
    private static final String KML_DEFAULT_PATH = "files/";
    private static final String PIC1_KML_PATH = KML_DEFAULT_PATH + PIC1;
    private static final String PIC2_KML_PATH = KML_DEFAULT_PATH + PIC2;
    private static final ArrayList<String> PICTOGRAM_NAMES = new ArrayList<>(Arrays.asList(PIC1, PIC2));

    @BeforeAll
    public static void beforeAll() {
        fileService = Mockito.mock(FileService.class);
        Mockito.when(fileService.getPictogramsNames()).thenReturn(PICTOGRAM_NAMES);
    }

    @Test
    public void style_With_Photo_Id_should_be_Deleted()
            throws IOException, ParserConfigurationException, SAXException {
        //GIVEN
        final String photoId = "file:///sdcard/Locus/cache/images/1604137344718";
        final String photoIconStyle = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" +
                "<Document>\n" +
                "\t<name>LOCUS03.07.2021</name>\n" +
                "\t<atom:author><atom:name>Locus (Android)</atom:name></atom:author>\n" +
                "\t\t<Style id=\"" +
                photoId +
                "\">\n" +
                "\t\t<IconStyle>\n" +
                "\t\t\t<Icon><href>files/file-sdcardLocuscacheimages1604137344718.png</href></Icon>\n" +
                "\t\t\t<hotSpot x=\"0.5\" y=\"0.0\" xunits=\"fraction\" yunits=\"fraction\" />\n" +
                "\t\t</IconStyle>\n" +
                "\t</Style>\n" +
                "</Document>\n" +
                "</kml>\n";
        multipartFile = new MockMultipartFile("LocusTestPoi.kml", "LocusTestPoi.kml",
                null, new ByteArrayInputStream(photoIconStyle.getBytes(StandardCharsets.UTF_8)));
        multipartDto = new MultipartDto(multipartFile);
        multipartDto.setReplaceLocusIcons(true);
        multipartDto.setPictogramName(PIC1);
        document = XmlTestUtils.getDocument(multipartDto);
        kmlUtils = new KmlUtils(document, new XmlDomUtils(document));
        locusIconsHandler = new LocusIconsHandler(fileService, kmlUtils);
        Mockito.when(fileService.getPath(Mockito.anyString())).thenReturn(KML_DEFAULT_PATH);


        //WHEN

        locusIconsHandler.replaceLocusIcons(document.getDocumentElement(), multipartDto);

        //THEN
//        System.out.println(XmlTestUtils.getAsText(document));
        assertFalse(XmlTestUtils.containsTagWithId(document, "Style", "id", photoId));
        //        assertFalse(processedKml.contains("<href>files/file-sdcardLocuscacheimages1589191676952.png</href>"));
    }

    @Test
    public void style_With_Photo_Id_should_be_Replaced_With_New_Style_With_Pictogram()
            throws IOException, ParserConfigurationException, SAXException {
        //GIVEN
        final String photoId = "file:///sdcard/Locus/cache/images/1604137344718";
        final String hrefPath = "files/";
        final String photoIconStyle = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" +
                "<Document>\n" +
                "\t<name>LOCUS03.07.2021</name>\n" +
                "\t<atom:author><atom:name>Locus (Android)</atom:name></atom:author>\n" +
                "\t\t<Style id=\"" +
                photoId +
                "\">\n" +
                "\t\t<IconStyle>\n" +
                "\t\t\t<Icon><href>files/file-sdcardLocuscacheimages1604137344718.png</href></Icon>\n" +
                "\t\t\t<hotSpot x=\"0.5\" y=\"0.0\" xunits=\"fraction\" yunits=\"fraction\" />\n" +
                "\t\t</IconStyle>\n" +
                "\t</Style>\n" +
                "</Document>\n" +
                "</kml>\n";
        multipartFile = new MockMultipartFile("LocusTestPoi.kml", "LocusTestPoi.kml",
                null, new ByteArrayInputStream(photoIconStyle.getBytes(StandardCharsets.UTF_8)));
        multipartDto = new MultipartDto(multipartFile);
        multipartDto.setReplaceLocusIcons(true);
        multipartDto.setPictogramName(PIC1);
        document = XmlTestUtils.getDocument(multipartDto);
        kmlUtils = new KmlUtils(document, new XmlDomUtils(document));
        locusIconsHandler = new LocusIconsHandler(fileService, kmlUtils);
        Mockito.when(fileService.getPath(Mockito.anyString())).thenReturn(hrefPath);


        //WHEN

        locusIconsHandler.replaceLocusIcons(document.getDocumentElement(), multipartDto);

        //THEN
//        System.out.println(XmlTestUtils.getAsText(document));
        assertTrue(XmlTestUtils.containsTagWithId(document, "Style", "id", PIC1));
    }

    @Test
    public void existing_Style_With_Pictogram_Should_Be_Preserved_As_A_Single_One()
            throws IOException, ParserConfigurationException, SAXException {
        //GIVEN
        final String photoId = "file:///sdcard/Locus/cache/images/1604137344718";
        final String photoIconAndPictogramStyles = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" +
                "<Document>\n" +
                "\t<name>LOCUS03.07.2021</name>\n" +
                "\t<atom:author><atom:name>Locus (Android)</atom:name></atom:author>\n" +
                "\t\t<Style id=\"" +
                photoId +
                "\">\n" +
                "\t\t<IconStyle>\n" +
                "\t\t\t<Icon><href>files/file-sdcardLocuscacheimages1604137344718.png</href></Icon>\n" +
                "\t\t\t<hotSpot x=\"0.5\" y=\"0.0\" xunits=\"fraction\" yunits=\"fraction\" />\n" +
                "\t\t</IconStyle>\n" +
                "\t</Style>\n" +
                "\t\t<Style id=\"" +
                PIC1 +
                "\">\n" +
                "\t\t<IconStyle>\n" +
                "\t\t\t<Icon><href>" +
                PIC1_KML_PATH +
                "</href></Icon>\n" +
                "\t\t\t<hotSpot x=\"0.5\" y=\"0.0\" xunits=\"fraction\" yunits=\"fraction\" />\n" +
                "\t\t</IconStyle>\n" +
                "\t</Style>\n" +
                "</Document>\n" +
                "</kml>\n";
        multipartFile = new MockMultipartFile("LocusTestPoi.kml", "LocusTestPoi.kml",
                null, new ByteArrayInputStream(photoIconAndPictogramStyles.getBytes(StandardCharsets.UTF_8)));
        multipartDto = new MultipartDto(multipartFile);
        multipartDto.setReplaceLocusIcons(true);
        multipartDto.setPictogramName(PIC1);
        document = XmlTestUtils.getDocument(multipartDto);
        kmlUtils = new KmlUtils(document, new XmlDomUtils(document));
        locusIconsHandler = new LocusIconsHandler(fileService, kmlUtils);
        Mockito.when(fileService.getPath(Mockito.anyString())).thenReturn(KML_DEFAULT_PATH);


        //WHEN
        locusIconsHandler.replaceLocusIcons(document.getDocumentElement(), multipartDto);

        //THEN
//        System.out.println(XmlTestUtils.getAsText(document));
        assertEquals(1, document.getElementsByTagName("Style").getLength());
        assertTrue(XmlTestUtils.containsTagWithId(document, "Style", "id", PIC1));
        assertTrue(XmlTestUtils.containsTagWithChild(document, "Icon", "href", PIC1_KML_PATH));
    }

    @Test
    public void styles_With_Photo_Id_Should_Be_Deleted()
            throws IOException, ParserConfigurationException, SAXException {
        //GIVEN
        final String photoId1 = "file:///sdcard/Locus/cache/images/1604137344718";
        final String photoId2 = "file:///sdcard/Locus/cache/images/1604137344719";
        final String photoIconStyles = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" +
                "<Document>\n" +
                "\t<name>LOCUS03.07.2021</name>\n" +
                "\t<atom:author><atom:name>Locus (Android)</atom:name></atom:author>\n" +
                "\t\t<Style id=\"" +
                photoId1 +
                "\">\n" +
                "\t\t<IconStyle>\n" +
                "\t\t\t<Icon><href>files/file-sdcardLocuscacheimages1604137344718.png</href></Icon>\n" +
                "\t\t\t<hotSpot x=\"0.5\" y=\"0.0\" xunits=\"fraction\" yunits=\"fraction\" />\n" +
                "\t\t</IconStyle>\n" +
                "\t</Style>\n" +
                "\t\t<Style id=\"" +
                photoId2 +
                "\">\n" +
                "\t\t<IconStyle>\n" +
                "\t\t\t<Icon><href>files/file-sdcardLocuscacheimages1604137344719.png</href></Icon>\n" +
                "\t\t\t<hotSpot x=\"0.5\" y=\"0.0\" xunits=\"fraction\" yunits=\"fraction\" />\n" +
                "\t\t</IconStyle>\n" +
                "\t</Style>\n" +
                "\t\t<Style id=\"" +
                PIC1 +
                "\">\n" +
                "\t\t<IconStyle>\n" +
                "\t\t\t<Icon><href>" +
                PIC1_KML_PATH +
                "</href></Icon>\n" +
                "\t\t\t<hotSpot x=\"0.5\" y=\"0.0\" xunits=\"fraction\" yunits=\"fraction\" />\n" +
                "\t\t</IconStyle>\n" +
                "\t</Style>\n" +
                "</Document>\n" +
                "</kml>\n";
        multipartFile = new MockMultipartFile("LocusTestPoi.kml", "LocusTestPoi.kml",
                null, new ByteArrayInputStream(photoIconStyles.getBytes(StandardCharsets.UTF_8)));
        multipartDto = new MultipartDto(multipartFile);
        multipartDto.setReplaceLocusIcons(true);
        multipartDto.setPictogramName(PIC1);
        document = XmlTestUtils.getDocument(multipartDto);
        kmlUtils = new KmlUtils(document, new XmlDomUtils(document));
        locusIconsHandler = new LocusIconsHandler(fileService, kmlUtils);
        Mockito.when(fileService.getPath(Mockito.anyString())).thenReturn(KML_DEFAULT_PATH);


        //WHEN
        locusIconsHandler.replaceLocusIcons(document.getDocumentElement(), multipartDto);

        //THEN
//        System.out.println(XmlTestUtils.getAsText(document));
        assertFalse(XmlTestUtils.containsTagWithId(document, "Style", "id", photoId1));
        assertFalse(XmlTestUtils.containsTagWithId(document, "Style", "id", photoId2));
    }

    @Test
    public void styles_With_Photo_Id_Should_Be_Replaces_With_Style_With_Pictogram()
            throws IOException, ParserConfigurationException, SAXException {
        //GIVEN
        final String photoId1 = "file:///sdcard/Locus/cache/images/1604137344718";
        final String photoId2 = "file:///sdcard/Locus/cache/images/1604137344719";
        final String photoIconAndExistingPictogramStyles = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" +
                "<Document>\n" +
                "\t<name>LOCUS03.07.2021</name>\n" +
                "\t<atom:author><atom:name>Locus (Android)</atom:name></atom:author>\n" +
                "\t\t<Style id=\"" +
                photoId1 +
                "\">\n" +
                "\t\t<IconStyle>\n" +
                "\t\t\t<Icon><href>files/file-sdcardLocuscacheimages1604137344718.png</href></Icon>\n" +
                "\t\t\t<hotSpot x=\"0.5\" y=\"0.0\" xunits=\"fraction\" yunits=\"fraction\" />\n" +
                "\t\t</IconStyle>\n" +
                "\t</Style>\n" +
                "\t\t<Style id=\"" +
                photoId2 +
                "\">\n" +
                "\t\t<IconStyle>\n" +
                "\t\t\t<Icon><href>files/file-sdcardLocuscacheimages1604137344719.png</href></Icon>\n" +
                "\t\t\t<hotSpot x=\"0.5\" y=\"0.0\" xunits=\"fraction\" yunits=\"fraction\" />\n" +
                "\t\t</IconStyle>\n" +
                "\t</Style>\n" +
                "\t\t<Style id=\"" +
                PIC1 +
                "\">\n" +
                "\t\t<IconStyle>\n" +
                "\t\t\t<Icon><href>" +
                PIC1_KML_PATH +
                "</href></Icon>\n" +
                "\t\t\t<hotSpot x=\"0.5\" y=\"0.0\" xunits=\"fraction\" yunits=\"fraction\" />\n" +
                "\t\t</IconStyle>\n" +
                "\t</Style>\n" +
                "</Document>\n" +
                "</kml>\n";
        multipartFile = new MockMultipartFile("LocusTestPoi.kml", "LocusTestPoi.kml",
                null, new ByteArrayInputStream(photoIconAndExistingPictogramStyles.getBytes(StandardCharsets.UTF_8)));
        multipartDto = new MultipartDto(multipartFile);
        multipartDto.setReplaceLocusIcons(true);
        multipartDto.setPictogramName(PIC1);
        document = XmlTestUtils.getDocument(multipartDto);
        kmlUtils = new KmlUtils(document, new XmlDomUtils(document));
        locusIconsHandler = new LocusIconsHandler(fileService, kmlUtils);
        Mockito.when(fileService.getPath(Mockito.anyString())).thenReturn(KML_DEFAULT_PATH);


        //WHEN
        locusIconsHandler.replaceLocusIcons(document.getDocumentElement(), multipartDto);

        //THEN
//        System.out.println(XmlTestUtils.getAsText(document));
        assertEquals(1, document.getElementsByTagName("Style").getLength());
        assertTrue(XmlTestUtils.containsTagWithId(document, "Style", "id", PIC1));
        //        assertFalse(processedKml.contains("<href>files/file-sdcardLocuscacheimages1589191676952.png</href>"));
    }

    @Test
    public void second_Style_With_Pictogram_Should_Not_Be_Deleted()
            throws IOException, ParserConfigurationException, SAXException {
        //GIVEN
        final String photoId1 = "file:///sdcard/Locus/cache/images/1604137344718";
        final String photoId2 = "file:///sdcard/Locus/cache/images/1604137344719";
        final String pictogram1And2Styles = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" +
                "<Document>\n" +
                "\t<name>LOCUS03.07.2021</name>\n" +
                "\t<atom:author><atom:name>Locus (Android)</atom:name></atom:author>\n" +
                "\t\t<Style id=\"" +
                photoId1 +
                "\">\n" +
                "\t\t<IconStyle>\n" +
                "\t\t\t<Icon><href>files/file-sdcardLocuscacheimages1604137344718.png</href></Icon>\n" +
                "\t\t\t<hotSpot x=\"0.5\" y=\"0.0\" xunits=\"fraction\" yunits=\"fraction\" />\n" +
                "\t\t</IconStyle>\n" +
                "\t</Style>\n" +
                "\t\t<Style id=\"" +
                photoId2 +
                "\">\n" +
                "\t\t<IconStyle>\n" +
                "\t\t\t<Icon><href>files/file-sdcardLocuscacheimages1604137344719.png</href></Icon>\n" +
                "\t\t\t<hotSpot x=\"0.5\" y=\"0.0\" xunits=\"fraction\" yunits=\"fraction\" />\n" +
                "\t\t</IconStyle>\n" +
                "\t</Style>\n" +
                "\t\t<Style id=\"" +
                PIC1 +
                "\">\n" +
                "\t\t<IconStyle>\n" +
                "\t\t\t<Icon><href>" +
                PIC1_KML_PATH +
                "</href></Icon>\n" +
                "\t\t\t<hotSpot x=\"0.5\" y=\"0.0\" xunits=\"fraction\" yunits=\"fraction\" />\n" +
                "\t\t</IconStyle>\n" +
                "\t</Style>\n" +
                "\t\t<Style id=\"" +
                PIC2 +
                "\">\n" +
                "\t\t<IconStyle>\n" +
                "\t\t\t<Icon><href>" +
                PIC2_KML_PATH +
                "</href></Icon>\n" +
                "\t\t\t<hotSpot x=\"0.5\" y=\"0.0\" xunits=\"fraction\" yunits=\"fraction\" />\n" +
                "\t\t</IconStyle>\n" +
                "\t</Style>\n" +
                "</Document>\n" +
                "</kml>\n";
        multipartFile = new MockMultipartFile("LocusTestPoi.kml", "LocusTestPoi.kml",
                null, new ByteArrayInputStream(pictogram1And2Styles.getBytes(StandardCharsets.UTF_8)));
        multipartDto = new MultipartDto(multipartFile);
        multipartDto.setReplaceLocusIcons(true);
        multipartDto.setPictogramName(PIC1);
        document = XmlTestUtils.getDocument(multipartDto);
        kmlUtils = new KmlUtils(document, new XmlDomUtils(document));
        locusIconsHandler = new LocusIconsHandler(fileService, kmlUtils);
        Mockito.when(fileService.getPath(Mockito.anyString())).thenReturn(KML_DEFAULT_PATH);


        //WHEN
        locusIconsHandler.replaceLocusIcons(document.getDocumentElement(), multipartDto);

        //THEN
//        System.out.println(XmlTestUtils.getAsText(document));
        assertEquals(2, document.getElementsByTagName("Style").getLength());

        assertTrue(XmlTestUtils.containsTagWithId(document, "Style", "id", PIC1));
        assertTrue(XmlTestUtils.containsTagWithChild(document, "Icon", "href", PIC1_KML_PATH));

        assertTrue(XmlTestUtils.containsTagWithId(document, "Style", "id", PIC2));
        assertTrue(XmlTestUtils.containsTagWithChild(document, "Icon", "href", PIC2_KML_PATH));
        //        assertFalse(processedKml.contains("<href>files/file-sdcardLocuscacheimages1589191676952.png</href>"));
    }

    @Test
    public void only_Styles_With_Pictogram_Id_should_Be_Left()
            throws IOException, ParserConfigurationException, SAXException {
        //GIVEN
        final String photoId = "file:///sdcard/Locus/cache/images/1604137344718";
        final String photoIconAndPictogram1Styles = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" +
                "<Document>\n" +
                "\t<name>LOCUS03.07.2021</name>\n" +
                "\t<atom:author><atom:name>Locus (Android)</atom:name></atom:author>\n" +
                "\t\t<Style id=\"" +
                PIC2 +
                "\">\n" +
                "\t\t<IconStyle>\n" +
                "\t\t\t<Icon><href>" +
                PIC2_KML_PATH +
                "</href></Icon>\n" +
                "\t\t\t<hotSpot x=\"0.5\" y=\"0.0\" xunits=\"fraction\" yunits=\"fraction\" />\n" +
                "\t\t</IconStyle>\n" +
                "\t</Style>\n" +
                "\t\t<Style id=\"" +
                photoId +
                "\">\n" +
                "\t\t<IconStyle>\n" +
                "\t\t\t<Icon><href>files/file-sdcardLocuscacheimages1604137344718.png</href></Icon>\n" +
                "\t\t\t<hotSpot x=\"0.5\" y=\"0.0\" xunits=\"fraction\" yunits=\"fraction\" />\n" +
                "\t\t</IconStyle>\n" +
                "\t</Style>\n" +
                "</Document>\n" +
                "</kml>\n";
        multipartFile = new MockMultipartFile("LocusTestPoi.kml", "LocusTestPoi.kml",
                null, new ByteArrayInputStream(photoIconAndPictogram1Styles.getBytes(StandardCharsets.UTF_8)));
        multipartDto = new MultipartDto(multipartFile);
        multipartDto.setReplaceLocusIcons(true);
        multipartDto.setPictogramName(PIC1);
        document = XmlTestUtils.getDocument(multipartDto);
        kmlUtils = new KmlUtils(document, new XmlDomUtils(document));
        locusIconsHandler = new LocusIconsHandler(fileService, kmlUtils);
        Mockito.when(fileService.getPath(Mockito.anyString())).thenReturn(KML_DEFAULT_PATH);


        //WHEN

        locusIconsHandler.replaceLocusIcons(document.getDocumentElement(), multipartDto);

        //THEN
//        System.out.println(XmlTestUtils.getAsText(document));
        assertFalse(XmlTestUtils.containsTagWithId(document, "Style", "id", photoId));

        assertTrue(XmlTestUtils.containsTagWithId(document, "Style", "id", PIC1));
        assertTrue(XmlTestUtils.containsTagWithChild(document, "Icon", "href", PIC1_KML_PATH));

        assertTrue(XmlTestUtils.containsTagWithId(document, "Style", "id", PIC2));
        assertTrue(XmlTestUtils.containsTagWithChild(document, "Icon", "href", PIC2_KML_PATH));
        //        assertFalse(processedKml.contains("<href>files/file-sdcardLocuscacheimages1589191676952.png</href>"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"files/", "../myFiles/", "/storage/0/Locus/data/media/photo/", "file:///D:/MyFolder/MyPOI/"})
    public void custom_Kml_Path_Should_Be_Preserved(String customPath)
            throws IOException, ParserConfigurationException, SAXException {
        //GIVEN
        final String photoId = "file:///sdcard/Locus/cache/images/1604137344718";
        final String pngFilename = "file-sdcardLocuscacheimages1604137344718.png";
        String existingCustomPathWithFilename = customPath + pngFilename;
        final String photoIconAndPictogramStyles = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" +
                "<Document>\n" +
                "\t<name>LOCUS03.07.2021</name>\n" +
                "\t<atom:author><atom:name>Locus (Android)</atom:name></atom:author>\n" +
                "\t\t<Style id=\"" +
                photoId +
                "\">\n" +
                "\t\t<IconStyle>\n" +
                "\t\t\t<Icon><href>" +
                existingCustomPathWithFilename +
                "</href></Icon>\n" +
                "\t\t\t<hotSpot x=\"0.5\" y=\"0.0\" xunits=\"fraction\" yunits=\"fraction\" />\n" +
                "\t\t</IconStyle>\n" +
                "\t</Style>\n" +
                "</Document>\n" +
                "</kml>\n";
        multipartFile = new MockMultipartFile("LocusTestPoi.kml", "LocusTestPoi.kml",
                null, new ByteArrayInputStream(photoIconAndPictogramStyles.getBytes(StandardCharsets.UTF_8)));
        multipartDto = new MultipartDto(multipartFile);
        multipartDto.setReplaceLocusIcons(true);
        multipartDto.setPictogramName(PIC1);
        document = XmlTestUtils.getDocument(multipartDto);
        kmlUtils = new KmlUtils(document, new XmlDomUtils(document));
        locusIconsHandler = new LocusIconsHandler(fileService, kmlUtils);
        Mockito.when(fileService.getPath(Mockito.anyString())).thenReturn(customPath);


        //WHEN
        locusIconsHandler.replaceLocusIcons(document.getDocumentElement(), multipartDto);

        //THEN
        //Only customPath should be preserved but with the pictogramName
//        System.out.println(XmlTestUtils.getAsText(document));
        assertEquals(1, document.getElementsByTagName("Style").getLength());
        assertTrue(XmlTestUtils.containsTagWithId(document, "Style", "id", PIC1));
        assertTrue(XmlTestUtils.containsTagWithChild(document, "Icon", "href", customPath + PIC1));
    }

}