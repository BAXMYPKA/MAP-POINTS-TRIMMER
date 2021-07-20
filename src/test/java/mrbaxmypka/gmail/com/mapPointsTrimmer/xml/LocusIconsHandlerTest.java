package mrbaxmypka.gmail.com.mapPointsTrimmer.xml;

import mrbaxmypka.gmail.com.mapPointsTrimmer.entitiesDto.MultipartDto;
import mrbaxmypka.gmail.com.mapPointsTrimmer.services.FileService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LocusIconsHandlerTest {

    private static FileService fileService;
    private static KmlUtils kmlUtils;
    private static LocusIconsHandler locusIconsHandler;
    private static MultipartDto multipartDto;
    private static MultipartFile multipartFile;
    private static Document document;
    private static final String PIC1 = "Pictogram1";
    private static final String PIC2 = "Pictogram2";
    private static final ArrayList<String> PICTOGRAM_NAMES = new ArrayList<>(Arrays.asList(PIC1, PIC2));

    @BeforeAll
    public static void beforeAll() {
        fileService = Mockito.mock(FileService.class);
        Mockito.when(fileService.getPictogramsNames()).thenReturn(PICTOGRAM_NAMES);
    }

    @Test
    public void photo_Style_should_be_deleted()
            throws IOException, ParserConfigurationException, SAXException {
        //GIVEN
        final String hrefPath = "files/";
        final String photoIconStyle = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" +
                "<Document>\n" +
                "\t<name>LOCUS03.07.2021</name>\n" +
                "\t<atom:author><atom:name>Locus (Android)</atom:name></atom:author>\n" +
                "\t\t<Style id=\"file:///sdcard/Locus/cache/images/1604137344718\">\n" +
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

        //WHEN
        Mockito.when(fileService.getPath(Mockito.anyString())).thenReturn(hrefPath);

        locusIconsHandler.replaceLocusIcons(document.getDocumentElement(), multipartDto);

        //THEN
        System.out.println(XmlTestUtils.getAsText(document));
//        assertTrue(processedKml.contains("<href>file:///C:/MyPoi/MyPoiImages/file-sdcardLocuscacheimages1571471453728.png</href>"));
//        assertTrue(processedKml.contains("<href>file:///C:/MyPoi/MyPoiImages/file-sdcardLocuscacheimages1589191676952.png</href>"));
//
//        assertFalse(processedKml.contains("<href>files/file-sdcardLocuscacheimages1571471453728.png</href>"));
//        assertFalse(processedKml.contains("<href>files/file-sdcardLocuscacheimages1589191676952.png</href>"));
    }


}