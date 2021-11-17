package mrbaxmypka.gmail.com.mapPointsTrimmer.xml;

import mrbaxmypka.gmail.com.mapPointsTrimmer.entitiesDto.MultipartMainDto;
import mrbaxmypka.gmail.com.mapPointsTrimmer.entitiesDto.PlacemarkNodeDto;
import mrbaxmypka.gmail.com.mapPointsTrimmer.services.FileService;
import mrbaxmypka.gmail.com.mapPointsTrimmer.utils.DistanceUnits;
import mrbaxmypka.gmail.com.mapPointsTrimmer.utils.ThinOutTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;
import org.springframework.context.MessageSource;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ThinOutKmlPointsHandlerTest {

    private ThinOutKmlPointsHandler thinOutKmlPointsHandler;
    private MessageSource messageSource;
    private FileService fileService;
    private HtmlHandler htmlHandler;
    private KmlUtils kmlUtils;
    private XmlDomUtils xmlDomUtils;
    private Document kmlDocument;

    @BeforeEach
    public void beforeEach() throws IOException, ParserConfigurationException, SAXException {
        messageSource = Mockito.mock(MessageSource.class);
        kmlDocument = XmlTestUtils.getMockDocument();
        fileService = new FileService(messageSource);
        htmlHandler = new HtmlHandler(fileService);
        xmlDomUtils = new XmlDomUtils(kmlDocument);
        kmlUtils = new KmlUtils(kmlDocument, xmlDomUtils);
        thinOutKmlPointsHandler = new ThinOutKmlPointsHandler(kmlUtils, fileService, htmlHandler);
    }

    @Test
    public void placemarkNodeDtoList_Should_Be_Ordered_From_Newest_To_Oldest()
            throws IOException, ParserConfigurationException, SAXException {
        //GIVEN
        //Has to be ordered as Placemark 3, Placemark 1, Placemark 2
        String threeOneTwoOrder = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:kml=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" +
                "<Document>\n" +
                "\t<name>Points in 3-1-2 reverse chronological order</name>\n" +
                "\t\t<Placemark>\n" +
                "\t\t\t<name>Point 1</name>\n" +
                "\t\t\t<gx:TimeStamp>\n" +
                "\t\t\t\t<when>2014-08-03T14:13:52Z</when>\n" +
                "\t\t\t</gx:TimeStamp>\n" +
                "\t\t\t<styleUrl>#styleMapOf:tourism-forest.png</styleUrl>\n" +
                "\t\t\t<Point>\n" +
                "\t\t\t\t<coordinates>38.34321845735578,56.27642531546667,194.55</coordinates>\n" +
                "\t\t\t</Point>\n" +
                "\t\t</Placemark>\n" +
                "\t\t<Placemark>\n" +
                "\t\t\t<name>Point 2</name>\n" +
                "\t\t\t<gx:TimeStamp>\n" +
                "\t\t\t\t<when>2013-12-02T11:13:52Z</when>\n" +
                "\t\t\t</gx:TimeStamp>\n" +
                "\t\t\t<styleUrl>#styleMapOf:tourism-forest.png</styleUrl>\n" +
                "\t\t\t<Point>\n" +
                "\t\t\t\t<coordinates>38.34303700795801,56.27559566234272,194.55</coordinates>\n" +
                "\t\t\t</Point>\n" +
                "\t\t</Placemark>\n" +
                "\t\t<Placemark>\n" +
                "\t\t\t<name>Point 3</name>\n" +
                "\t\t\t<gx:TimeStamp>\n" +
                "\t\t\t\t<when>2014-08-03T14:15:52Z</when>\n" +
                "\t\t\t</gx:TimeStamp>\n" +
                "\t\t\t<styleUrl>#styleMapOf:tourism-forest.png</styleUrl>\n" +
                "\t\t\t<Point>\n" +
                "\t\t\t\t<coordinates>38.34545701565047,56.27586260444358,194.55</coordinates>\n" +
                "\t\t\t</Point>\n" +
                "\t\t</Placemark>\n" +
                "</Document>\n" +
                "</kml>\n";
        kmlDocument = XmlTestUtils.getDocument(threeOneTwoOrder);
        MultipartMainDto multipartMainDto = new MultipartMainDto();
        multipartMainDto.setDistanceUnit(DistanceUnits.METERS);
        multipartMainDto.setThinOutDistance(1);

        xmlDomUtils = new XmlDomUtils(kmlDocument);
        kmlUtils = new KmlUtils(kmlDocument, xmlDomUtils);
        thinOutKmlPointsHandler = new ThinOutKmlPointsHandler(kmlUtils, fileService, htmlHandler);

        //WHEN
        thinOutKmlPointsHandler.thinOutPoints(kmlDocument, multipartMainDto);
        List<PlacemarkNodeDto> placemarkNodeDtoList = thinOutKmlPointsHandler.getPlacemarkNodeDtoList();

        //THEN
        assertAll(
                () -> assertTrue(placemarkNodeDtoList.get(0).getWhenTimeStamp().isEqual(LocalDateTime.parse("2014-08-03T14:15:52"))),
                () -> assertTrue(placemarkNodeDtoList.get(1).getWhenTimeStamp().isEqual(LocalDateTime.parse("2014-08-03T14:13:52"))),
                () -> assertTrue(placemarkNodeDtoList.get(2).getWhenTimeStamp().isEqual(LocalDateTime.parse("2013-12-02T11:13:52")))
        );
    }

    /**
     * Every tuple includes the last value as the distance in meters according to the Google Earth ruler.
     *
     * @param googleEarthDistance The basic distance in meters according to Google Earth ruler
     */
    @ParameterizedTest
    @CsvSource(value = {"38.34321845735578,56.27642531546667,38.34303700795801,56.27559566234272,95.00",
            "38.34303700795801,56.27559566234272,38.34545701565047,56.27586260444358,152.0"})
    public void only_Placemarks_Closer_Than_ThinOutDistance_Should_Be_Removed_From_Document(
            String longitude1, String latitude1, String longitude2, String latitude2, String googleEarthDistance)
            throws IOException, ParserConfigurationException, SAXException {
        //GIVEN
        String twoPlacemarks = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:kml=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" +
                "<Document>\n" +
                "\t<name>Points with 93 meters distance</name>\n" +
                "\t\t<Placemark>\n" +
                "\t\t\t<name>Point 1</name>\n" +
                "\t\t\t<description>About 93 meters from Point 2</description>\n" +
                "\t\t\t<gx:TimeStamp>\n" +
                "\t\t\t\t<when>2014-08-03T14:13:52Z</when>\n" +
                "\t\t\t</gx:TimeStamp>\n" +
                "\t\t\t<styleUrl>#styleMapOf:tourism-forest.png</styleUrl>\n" +
                "\t\t\t<Point>\n" +
                "\t\t\t\t<coordinates>" + longitude1 + "," + latitude1 + ",194.55" + "</coordinates>\n" +
                "\t\t\t</Point>\n" +
                "\t\t</Placemark>\n" +
                "\t\t<Placemark>\n" +
                "\t\t\t<name>Point 2</name>\n" +
                "\t\t\t<description>About 93 meters from Point 1</description>\n" +
                "\t\t\t<gx:TimeStamp>\n" +
                "\t\t\t\t<when>2013-12-02T11:13:52Z</when>\n" +
                "\t\t\t</gx:TimeStamp>\n" +
                "\t\t\t<styleUrl>#styleMapOf:tourism-forest.png</styleUrl>\n" +
                "\t\t\t<Point>\n" +
                "\t\t\t\t<coordinates>" + longitude2 + "," + latitude2 + ",194.55" + "</coordinates>\n" +
                "\t\t\t</Point>\n" +
                "\t\t</Placemark>\n" +
                "</Document>\n" +
                "</kml>\n";
        kmlDocument = XmlTestUtils.getDocument(twoPlacemarks);
        MultipartMainDto multipartMainDto = new MultipartMainDto();
        multipartMainDto.setDistanceUnit(DistanceUnits.METERS);

        double realDistance = Double.parseDouble(googleEarthDistance);

        int thinOutDistance = 110; //To thin out Placemarks closer than this
        multipartMainDto.setThinOutDistance(thinOutDistance);

        xmlDomUtils = new XmlDomUtils(kmlDocument);
        kmlUtils = new KmlUtils(kmlDocument, xmlDomUtils);
        thinOutKmlPointsHandler = new ThinOutKmlPointsHandler(kmlUtils, fileService, htmlHandler);

        //WHEN
        thinOutKmlPointsHandler.thinOutPoints(kmlDocument, multipartMainDto);

        //THEN
        if (realDistance == 95) {
            //The distance is closer than the "thinOutDistance", the only newest Placemark has to be left
            assertTrue(
                    XmlTestUtils.containsTagWithChild(kmlDocument, "gx:TimeStamp", "when", "2014-08-03T14:13:52Z"));
            assertFalse(
                    XmlTestUtils.containsTagWithChild(kmlDocument, "gx:TimeStamp", "when", "2013-12-02T11:13:52Z"));
        } else if (realDistance == 152) {
            //The distance is not closer than the "thinOutDistance", so all the Placemarks have to be left
            assertTrue(
                    XmlTestUtils.containsTagWithChild(kmlDocument, "gx:TimeStamp", "when", "2014-08-03T14:13:52Z"));
            assertTrue(
                    XmlTestUtils.containsTagWithChild(kmlDocument, "gx:TimeStamp", "when", "2013-12-02T11:13:52Z"));
        }
    }

    /**
     * Every tuple includes the last value as the distance in meters according to the Google Earth ruler.
     */
    @Test
    public void only_Every_Other_Placemark_In_Chain_Closer_Than_ThinOutDistance_Should_Be_Removed_From_Document()
            throws IOException, ParserConfigurationException, SAXException {
        //GIVEN
        //The five sequential Placemarks ranged by TimeStamp within the "thinOutDistance"
        String fiveSequentialPlacemarks = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:kml=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" +
                "<Document>\n" +
                "\t<name>Five sequential Placemarks within 475-665 meters distance between</name>\n" +
                "\t\t<Placemark>\n" +
                "\t\t\t<name>Point 1</name>\n" +
                "\t\t\t<description>630 meters from Point 2</description>\n" +
                "\t\t\t<gx:TimeStamp>\n" +
                "\t\t\t\t<when>2014-08-03T14:13:40Z</when>\n" +
                "\t\t\t</gx:TimeStamp>\n" +
                "\t\t\t<styleUrl>#styleMapOf:tourism-forest.png</styleUrl>\n" +
                "\t\t\t<Point>\n" +
                "\t\t\t\t<coordinates>38.34646245907663,56.27537585942488,0</coordinates>\n" +
                "\t\t\t</Point>\n" +
                "\t\t</Placemark>\n" +
                "\t\t<Placemark>\n" +
                "\t\t\t<name>Point 2</name>\n" +
                "\t\t\t<description>470 meters from Point 3</description>\n" +
                "\t\t\t<gx:TimeStamp>\n" +
                "\t\t\t\t<when>2014-08-03T14:13:45Z</when>\n" +
                "\t\t\t</gx:TimeStamp>\n" +
                "\t\t\t<styleUrl>#styleMapOf:tourism-forest.png</styleUrl>\n" +
                "\t\t\t<Point>\n" +
                "\t\t\t\t<coordinates>38.33819227546833,56.27861542402659,0</coordinates>\n" +
                "\t\t\t</Point>\n" +
                "\t\t</Placemark>\n" +
                "\t\t<Placemark>\n" +
                "\t\t\t<name>Point 3</name>\n" +
                "\t\t\t<description>660 meters from Point 4</description>\n" +
                "\t\t\t<gx:TimeStamp>\n" +
                "\t\t\t\t<when>2014-08-03T14:13:50Z</when>\n" +
                "\t\t\t</gx:TimeStamp>\n" +
                "\t\t\t<styleUrl>#styleMapOf:tourism-forest.png</styleUrl>\n" +
                "\t\t\t<Point>\n" +
                "\t\t\t\t<coordinates>38.33249357961224,56.28137205601424,0</coordinates>\n" +
                "\t\t\t</Point>\n" +
                "\t\t</Placemark>\n" +
                "\t\t<Placemark>\n" +
                "\t\t\t<name>Point 4</name>\n" +
                "\t\t\t<description>660 meters from Point3</description>\n" +
                "\t\t\t<gx:TimeStamp>\n" +
                "\t\t\t\t<when>2014-08-03T14:13:55Z</when>\n" +
                "\t\t\t</gx:TimeStamp>\n" +
                "\t\t\t<styleUrl>#styleMapOf:tourism-forest.png</styleUrl>\n" +
                "\t\t\t<Point>\n" +
                "\t\t\t\t<coordinates>38.33180819745429,56.28727692540543,0</coordinates>\n" +
                "\t\t\t</Point>\n" +
                "\t\t</Placemark>\n" +
                "\t\t<Placemark>\n" +
                "\t\t\t<name>Point 5</name>\n" +
                "\t\t\t<description>485 meters from Point 5</description>\n" +
                "\t\t\t<gx:TimeStamp>\n" +
                "\t\t\t\t<when>2014-08-03T14:13:57Z</when>\n" +
                "\t\t\t</gx:TimeStamp>\n" +
                "\t\t\t<styleUrl>#styleMapOf:tourism-forest.png</styleUrl>\n" +
                "\t\t\t<Point>\n" +
                "\t\t\t\t<coordinates>38.32519633269865,56.28978221600942,0</coordinates>\n" +
                "\t\t\t</Point>\n" +
                "\t\t</Placemark>\n" +
                "</Document>\n" +
                "</kml>\n";
        kmlDocument = XmlTestUtils.getDocument(fiveSequentialPlacemarks);
        MultipartMainDto multipartMainDto = new MultipartMainDto();
        multipartMainDto.setDistanceUnit(DistanceUnits.METERS);

        int thinOutDistance = 680; //To thin out Placemarks closer than this
        multipartMainDto.setThinOutDistance(thinOutDistance);

        xmlDomUtils = new XmlDomUtils(kmlDocument);
        kmlUtils = new KmlUtils(kmlDocument, xmlDomUtils);
        thinOutKmlPointsHandler = new ThinOutKmlPointsHandler(kmlUtils, fileService, htmlHandler);

        //WHEN
        thinOutKmlPointsHandler.thinOutPoints(kmlDocument, multipartMainDto);

        //THEN
        assertAll(
                () -> assertTrue(XmlTestUtils.containsTagWithChild(kmlDocument, "Placemark", "name", "Point 1")),
                () -> assertTrue(XmlTestUtils.containsTagWithChild(kmlDocument, "Placemark", "name", "Point 3")),
                () -> assertTrue(XmlTestUtils.containsTagWithChild(kmlDocument, "Placemark", "name", "Point 5"))

        );
        assertAll(
                () -> assertFalse(XmlTestUtils.containsTagWithChild(kmlDocument, "Placemark", "name", "Point 2")),
                () -> assertFalse(XmlTestUtils.containsTagWithChild(kmlDocument, "Placemark", "name", "Point 4"))
        );
    }

    @Test
    public void icons_Names_Should_Be_Added_In_PlacemarkNodeDto_IconName()
            throws IOException, ParserConfigurationException, SAXException {
        //GIVEN
        String iconName1 = "tourism-forest.png";
        String iconName2 = "retainMe.png";
        String iconName3 = "deleteMe.png";
        String pointsAsIsoscelesTriangle = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:kml=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" +
                "<Document>\n" +
                "\t<name>Three sequential isosceles triangle Placemarks within 245 meters distance between each</name>\n" +

                "\t<StyleMap id=\"styleMapOf:tourism-forest.png\">\n" +
                "\t\t<Pair>\n" +
                "\t\t\t<key>normal</key>\n" +
                "\t\t\t<styleUrl>#tourism-forest.png</styleUrl>\n" +
                "\t\t</Pair>\n" +
                "\t\t<Pair>\n" +
                "\t\t\t<key>highlight</key>\n" +
                "\t\t\t<styleUrl>#highlighOf:tourism-forest.png</styleUrl>\n" +
                "\t\t</Pair>\n" +
                "\t</StyleMap>\n" +

                "\t<Style id=\"" + iconName1 + "\">\n" +
                "\t\t<IconStyle>\n" +
                "\t\t\t<Icon>\n" +
                "\t\t\t\t<href>files/tourism-forest.png</href>\n" +
                "\t\t\t</Icon>\n" +
                "\t\t</IconStyle>\n" +
                "\t</Style>\n" +

                "\t<Style id=\"highlighOf:tourism-forest.png\">\n" +
                "\t\t<IconStyle>\n" +
                "\t\t\t<Icon>\n" +
                "\t\t\t\t<href>files///C:folder/tourism-forest.png</href>\n" +
                "\t\t\t</Icon>\n" +
                "\t\t</IconStyle>\n" +
                "\t</Style>\n" +

                "\t<Style id=\"retainMe.png\">\n" +
                "\t\t<IconStyle>\n" +
                "\t\t\t<Icon>\n" +
                "\t\t\t\t<href>files/" + iconName2 + "</href>\n" +
                "\t\t\t</Icon>\n" +
                "\t\t</IconStyle>\n" +
                "\t</Style>\n" +

                "\t<Style id=\"deleteMe.png\">\n" +
                "\t\t<IconStyle>\n" +
                "\t\t\t<Icon>\n" +
                "\t\t\t\t<href>files/" + iconName3 + "</href>\n" +
                "\t\t\t</Icon>\n" +
                "\t\t</IconStyle>\n" +
                "\t</Style>\n" +

                "\t\t<Placemark>\n" +
                "\t\t\t<name>Point to be retained by icon 1</name>\n" +
                "\t\t\t<gx:TimeStamp>\n" +
                "\t\t\t\t<when></when>\n" +
                "\t\t\t</gx:TimeStamp>\n" +
                "\t\t\t<styleUrl>#styleMapOf:tourism-forest.png</styleUrl>\n" +
                "\t\t\t<Point>\n" +
                "\t\t\t\t<coordinates>38.34646245907663,56.27537585942488,0</coordinates>\n" +
                "\t\t\t</Point>\n" +
                "\t\t</Placemark>\n" +

                "\t\t<Placemark>\n" +
                "\t\t\t<name>Point to be retained by icon 2</name>\n" +
                "\t\t\t<gx:TimeStamp>\n" +
                "\t\t\t\t<when></when>\n" +
                "\t\t\t</gx:TimeStamp>\n" +
                "\t\t\t<styleUrl>#retainMe.png</styleUrl>\n" +
                "\t\t\t<Point>\n" +
                "\t\t\t\t<coordinates>38.3428861954351,56.27522642196566,0</coordinates>\n" +
                "\t\t\t</Point>\n" +
                "\t\t</Placemark>\n" +

                "\t\t<Placemark>\n" +
                "\t\t\t<name>Point that may by deleted 3</name>\n" +
                "\t\t\t<gx:TimeStamp>\n" +
                "\t\t\t\t<when></when>\n" +
                "\t\t\t</gx:TimeStamp>\n" +
                "\t\t\t<styleUrl>#deleteMe.png</styleUrl>\n" +
                "\t\t\t<Point>\n" +
                "\t\t\t\t<coordinates>38.34439565050742,56.27675775851414,0</coordinates>\n" +
                "\t\t\t</Point>\n" +
                "\t\t</Placemark>\n" +

                "</Document>\n" +
                "</kml>\n";
        kmlDocument = XmlTestUtils.getDocument(pointsAsIsoscelesTriangle);
        MultipartMainDto multipartMainDto = new MultipartMainDto();
        multipartMainDto.setDistanceUnit(DistanceUnits.METERS);

        int thinOutDistance = 50; //To thin out Placemarks closer than this
        multipartMainDto.setThinOutDistance(thinOutDistance);

        xmlDomUtils = new XmlDomUtils(kmlDocument);
        kmlUtils = new KmlUtils(kmlDocument, xmlDomUtils);
        thinOutKmlPointsHandler = new ThinOutKmlPointsHandler(kmlUtils, fileService, htmlHandler);

        //WHEN
        thinOutKmlPointsHandler.thinOutPoints(kmlDocument, multipartMainDto);
        List<PlacemarkNodeDto> placemarkNodeDtoList = thinOutKmlPointsHandler.getPlacemarkNodeDtoList();

        //THEN
        assertTrue(
                placemarkNodeDtoList.stream().allMatch(placemarkNodeDto -> {
                    return placemarkNodeDto.getIconName().equals(iconName1) ||
                            placemarkNodeDto.getIconName().equals(iconName2) ||
                            placemarkNodeDto.getIconName().equals(iconName3);
                })
        );
    }

    @Test
    public void images_Names_From_LcAttachments_Should_Be_Added_In_PlacemarkNodeDto_ImagesNames()
            throws IOException, ParserConfigurationException, SAXException {
        //GIVEN
        String imageName1 = "123.jpg";
        String imageName2 = "Abc.png";
        String imageName3 = "Abc.jpg";
        String audioFile = "Wav.mp3";
        String pointsAsIsoscelesTriangle = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:kml=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" +
                "<Document>\n" +
                "\t<name>Three sequential isosceles triangle Placemarks within 245 meters distance between each</name>\n" +

                "\t\t<Placemark>\n" +
                "\t\t\t<name>Point to be retained by icon 2</name>\n" +
                "\t\t\t<gx:TimeStamp>\n" +
                "\t\t\t\t<when></when>\n" +
                "\t\t\t</gx:TimeStamp>\n" +
                "\t\t\t<styleUrl>#retainMe.png</styleUrl>\n" +

                "\t<ExtendedData xmlns:lc=\"http://www.locusmap.eu\">\n" +
                "\t\t<lc:attachment>files/" + imageName1 + "</lc:attachment>\n" +
                "\t\t<lc:attachment>files/" + audioFile + "</lc:attachment>\n" +
                "\t</ExtendedData>\n" +

                "\t\t\t<Point>\n" +
                "\t\t\t\t<coordinates>38.3428861954351,56.27522642196566,0</coordinates>\n" +
                "\t\t\t</Point>\n" +
                "\t\t</Placemark>\n" +

                "\t\t<Placemark>\n" +
                "\t\t\t<name>Point that may by deleted 3</name>\n" +
                "\t\t\t<gx:TimeStamp>\n" +
                "\t\t\t\t<when></when>\n" +
                "\t\t\t</gx:TimeStamp>\n" +
                "\t\t\t<styleUrl>#deleteMe.png</styleUrl>\n" +

                "\t<ExtendedData xmlns:lc=\"http://www.locusmap.eu\">\n" +
                "\t\t<lc:attachment>files/" + imageName2 + "</lc:attachment>\n" +
                "\t\t<lc:attachment>files/" + imageName3 + "</lc:attachment>\n" +
                "\t</ExtendedData>\n" +

                "\t\t\t<Point>\n" +
                "\t\t\t\t<coordinates>38.34439565050742,56.27675775851414,0</coordinates>\n" +
                "\t\t\t</Point>\n" +
                "\t\t</Placemark>\n" +

                "</Document>\n" +
                "</kml>\n";
        kmlDocument = XmlTestUtils.getDocument(pointsAsIsoscelesTriangle);
        MultipartMainDto multipartMainDto = new MultipartMainDto();
        multipartMainDto.setDistanceUnit(DistanceUnits.METERS);
        multipartMainDto.setThinOutDistance(1);

        xmlDomUtils = new XmlDomUtils(kmlDocument);
        kmlUtils = new KmlUtils(kmlDocument, xmlDomUtils);
        thinOutKmlPointsHandler = new ThinOutKmlPointsHandler(kmlUtils, fileService, htmlHandler);

        //WHEN
        thinOutKmlPointsHandler.thinOutPoints(kmlDocument, multipartMainDto);
        List<PlacemarkNodeDto> placemarkNodeDtoList = thinOutKmlPointsHandler.getPlacemarkNodeDtoList();

        //THEN
        assertTrue(placemarkNodeDtoList.stream().allMatch(placemarkNodeDto ->
                placemarkNodeDto.getImagesNames().contains(imageName1) ||
                        placemarkNodeDto.getImagesNames().contains(imageName2) ||
                        placemarkNodeDto.getImagesNames().contains(imageName3))
        );
        assertFalse(placemarkNodeDtoList.stream().anyMatch(placemarkNodeDto ->
                placemarkNodeDto.getImagesNames().contains(audioFile)));
    }

    /**
     * Every tuple includes the last value as the distance in meters according to the Google Earth ruler.
     */
    @ParameterizedTest
    @CsvSource(value = {"2014-08-03T14:13:40Z,2014-08-03T14:13:50Z,2014-08-03T14:13:45Z",
            "2014-08-03T14:13:45Z,2014-08-03T14:13:50Z,2014-08-03T14:13:40Z",
            "2014-08-03T14:13:50Z,2014-08-03T14:13:40Z,2014-08-03T14:13:45Z",
            "2014-08-03T14:13:50Z,2014-08-03T14:13:45Z,2014-08-03T14:13:40Z"})
    public void only_Placemarks_With_Inclusive_Icons_Names_Should_Be_Removed_From_Document(
            String timeStamp1, String timeStamp2, String timeStamp3)
            throws IOException, ParserConfigurationException, SAXException {
        //GIVEN
        String inclusiveIcon1 = "tourism-forest.png";
        String inclusiveIcon2 = "deleteMe.png";
        String retainableIcon = "retainMe.png";
        //Three isosceles triangle Placemarks within 245 meters distance between each within the "thinOutDistance"
        String pointsAsIsoscelesTriangle = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:kml=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" +
                "<Document>\n" +
                "\t<name>Three sequential isosceles triangle Placemarks within 245 meters distance between each</name>\n" +

                "\t<StyleMap id=\"styleMapOf:" + inclusiveIcon1 + "\">\n" +
                "\t\t<Pair>\n" +
                "\t\t\t<key>normal</key>\n" +
                "\t\t\t<styleUrl>#tourism-forest.png</styleUrl>\n" +
                "\t\t</Pair>\n" +
                "\t\t<Pair>\n" +
                "\t\t\t<key>highlight</key>\n" +
                "\t\t\t<styleUrl>#highlighOf:tourism-forest.png</styleUrl>\n" +
                "\t\t</Pair>\n" +
                "\t</StyleMap>\n" +

                "\t<Style id=\"" + inclusiveIcon1 + "\">\n" +
                "\t\t<IconStyle>\n" +
                "\t\t\t<scale>0.472727</scale>\n" +
                "\t\t\t<Icon>\n" +
                "\t\t\t\t<href>files/tourism-forest.png</href>\n" +
                "\t\t\t</Icon>\n" +
                "\t\t</IconStyle>\n" +
                "\t</Style>\n" +

                "\t<Style id=\"highlighOf:" + inclusiveIcon1 + "\">\n" +
                "\t\t<IconStyle>\n" +
                "\t\t\t<scale>0.4</scale>\n" +
                "\t\t\t<Icon>\n" +
                "\t\t\t\t<href>files///C:folder/" + inclusiveIcon1 + "</href>\n" +
                "\t\t\t</Icon>\n" +
                "\t\t</IconStyle>\n" +
                "\t</Style>\n" +

                "\t<Style id=\"" + inclusiveIcon2 + "\">\n" +
                "\t\t<IconStyle>\n" +
                "\t\t\t<scale>0.472727</scale>\n" +
                "\t\t\t<Icon>\n" +
                "\t\t\t\t<href>files/" + inclusiveIcon2 + "</href>\n" +
                "\t\t\t</Icon>\n" +
                "\t\t</IconStyle>\n" +
                "\t</Style>\n" +

                "\t<Style id=\"" + retainableIcon + "\">\n" +
                "\t\t<IconStyle>\n" +
                "\t\t\t<scale>0.472727</scale>\n" +
                "\t\t\t<Icon>\n" +
                "\t\t\t\t<href>files/" + retainableIcon + "</href>\n" +
                "\t\t\t</Icon>\n" +
                "\t\t</IconStyle>\n" +
                "\t</Style>\n" +

                "\t\t<Placemark>\n" +
                "\t\t\t<name>Point to be removed by icon 1</name>\n" +
                "\t\t\t<description>232 meters from Point 2</description>\n" +
                "\t\t\t<gx:TimeStamp>\n" +
                "\t\t\t\t<when>" + timeStamp1 + "</when>\n" +
                "\t\t\t</gx:TimeStamp>\n" +
                "\t\t\t<styleUrl>#styleMapOf:" + inclusiveIcon1 + "</styleUrl>\n" +
                "\t\t\t<Point>\n" +
                "\t\t\t\t<coordinates>38.34646245907663,56.27537585942488,0</coordinates>\n" +
                "\t\t\t</Point>\n" +
                "\t\t</Placemark>\n" +

                "\t\t<Placemark>\n" +
                "\t\t\t<name>Point to be removed by icon 2</name>\n" +
                "\t\t\t<description>193 meters from Point 3</description>\n" +
                "\t\t\t<gx:TimeStamp>\n" +
                "\t\t\t\t<when>" + timeStamp2 + "</when>\n" +
                "\t\t\t</gx:TimeStamp>\n" +
                "\t\t\t<styleUrl>#" + inclusiveIcon2 + "</styleUrl>\n" +
                "\t\t\t<Point>\n" +
                "\t\t\t\t<coordinates>38.3428861954351,56.27522642196566,0</coordinates>\n" +
                "\t\t\t</Point>\n" +
                "\t\t</Placemark>\n" +

                "\t\t<Placemark>\n" +
                "\t\t\t<name>Point to be retained</name>\n" +
                "\t\t\t<description>202m from Point 1</description>\n" +
                "\t\t\t<gx:TimeStamp>\n" +
                "\t\t\t\t<when>" + timeStamp3 + "</when>\n" +
                "\t\t\t</gx:TimeStamp>\n" +
                "\t\t\t<styleUrl>#" + retainableIcon + "</styleUrl>\n" +
                "\t\t\t<Point>\n" +
                "\t\t\t\t<coordinates>38.34439565050742,56.27675775851414,0</coordinates>\n" +
                "\t\t\t</Point>\n" +
                "\t\t</Placemark>\n" +

                "</Document>\n" +
                "</kml>\n";
        kmlDocument = XmlTestUtils.getDocument(pointsAsIsoscelesTriangle);
        MultipartMainDto multipartMainDto = new MultipartMainDto();
        multipartMainDto.setDistanceUnit(DistanceUnits.METERS);

        int thinOutDistance = 300; //To thin out Placemarks closer than this
        multipartMainDto.setThinOutDistance(thinOutDistance);
        multipartMainDto.setThinOutIconsNames(Arrays.asList(inclusiveIcon1, inclusiveIcon2));
        multipartMainDto.setThinOutType(ThinOutTypes.INCLUSIVE);

        xmlDomUtils = new XmlDomUtils(kmlDocument);
        kmlUtils = new KmlUtils(kmlDocument, xmlDomUtils);
        thinOutKmlPointsHandler = new ThinOutKmlPointsHandler(kmlUtils, fileService, htmlHandler);

        //WHEN
        thinOutKmlPointsHandler.thinOutPoints(kmlDocument, multipartMainDto);

        System.out.println(XmlTestUtils.getAsText(kmlDocument));

        //THEN
        assertTrue(
                !XmlTestUtils.containsTagWithChild(kmlDocument, "Placemark", "name", "Point to be removed by icon 1") ||
                        !XmlTestUtils.containsTagWithChild(kmlDocument, "Placemark", "name", "Point to be removed by icon 2")
        );
        assertTrue(
                XmlTestUtils.containsTagWithChild(kmlDocument, "Placemark", "name", "Point to be retained"));
    }

    /**
     * Every tuple includes the last value as the distance in meters according to the Google Earth ruler.
     */
    @ParameterizedTest
    @CsvSource(value = {"2014-08-03T14:13:40Z,2014-08-03T14:13:50Z,2014-08-03T14:13:45Z",
            "2014-08-03T14:13:45Z,2014-08-03T14:13:50Z,2014-08-03T14:13:40Z",
            "2014-08-03T14:13:50Z,2014-08-03T14:13:40Z,2014-08-03T14:13:45Z",
            "2014-08-03T14:13:50Z,2014-08-03T14:13:45Z,2014-08-03T14:13:40Z"})
    public void placemarks_With_Exclusive_Icons_Names_Should_Be_Retained_In_Document(
            String timeStamp1, String timeStamp2, String timeStamp3)
            throws IOException, ParserConfigurationException, SAXException {
        //GIVEN
        String exclusiveIcon1 = "tourism-forest.png";
        String exclusiveIcon2 = "retainMe.png";
        String removableIcon = "deleteMe.png";
        //Three isosceles triangle Placemarks within 245 meters distance between each within the "thinOutDistance"
        String pointsAsIsoscelesTriangle = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:kml=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" +
                "<Document>\n" +
                "\t<name>Three sequential isosceles triangle Placemarks within 245 meters distance between each</name>\n" +

                "\t<StyleMap id=\"styleMapOf:" + exclusiveIcon1 + "\">\n" +
                "\t\t<Pair>\n" +
                "\t\t\t<key>normal</key>\n" +
                "\t\t\t<styleUrl>#" + exclusiveIcon1 + "</styleUrl>\n" +
                "\t\t</Pair>\n" +
                "\t\t<Pair>\n" +
                "\t\t\t<key>highlight</key>\n" +
                "\t\t\t<styleUrl>#highlighOf:" + exclusiveIcon1 + "</styleUrl>\n" +
                "\t\t</Pair>\n" +
                "\t</StyleMap>\n" +

                "\t<Style id=\"" + exclusiveIcon1 + "\">\n" +
                "\t\t<IconStyle>\n" +
                "\t\t\t<scale>0.472727</scale>\n" +
                "\t\t\t<Icon>\n" +
                "\t\t\t\t<href>files/" + exclusiveIcon1 + "</href>\n" +
                "\t\t\t</Icon>\n" +
                "\t\t</IconStyle>\n" +
                "\t</Style>\n" +

                "\t<Style id=\"highlighOf:" + exclusiveIcon1 + "\">\n" +
                "\t\t<IconStyle>\n" +
                "\t\t\t<scale>0.4</scale>\n" +
                "\t\t\t<Icon>\n" +
                "\t\t\t\t<href>files///C:folder/" + exclusiveIcon1 + "</href>\n" +
                "\t\t\t</Icon>\n" +
                "\t\t</IconStyle>\n" +
                "\t</Style>\n" +

                "\t<Style id=\"" + exclusiveIcon2 + "\">\n" +
                "\t\t<IconStyle>\n" +
                "\t\t\t<scale>0.472727</scale>\n" +
                "\t\t\t<Icon>\n" +
                "\t\t\t\t<href>files/" + exclusiveIcon2 + "</href>\n" +
                "\t\t\t</Icon>\n" +
                "\t\t</IconStyle>\n" +
                "\t</Style>\n" +

                "\t<Style id=\"" + removableIcon + "\">\n" +
                "\t\t<IconStyle>\n" +
                "\t\t\t<scale>0.472727</scale>\n" +
                "\t\t\t<Icon>\n" +
                "\t\t\t\t<href>files/" + removableIcon + "</href>\n" +
                "\t\t\t</Icon>\n" +
                "\t\t</IconStyle>\n" +
                "\t</Style>\n" +

                "\t\t<Placemark>\n" +
                "\t\t\t<name>Point to be retained by icon 1</name>\n" +
                "\t\t\t<description>232 meters from Point 2</description>\n" +
                "\t\t\t<gx:TimeStamp>\n" +
                "\t\t\t\t<when>" + timeStamp1 + "</when>\n" +
                "\t\t\t</gx:TimeStamp>\n" +
                "\t\t\t<styleUrl>#styleMapOf:" + exclusiveIcon1 + "</styleUrl>\n" +
                "\t\t\t<Point>\n" +
                "\t\t\t\t<coordinates>38.34646245907663,56.27537585942488,0</coordinates>\n" +
                "\t\t\t</Point>\n" +
                "\t\t</Placemark>\n" +

                "\t\t<Placemark>\n" +
                "\t\t\t<name>Point to be retained by icon 2</name>\n" +
                "\t\t\t<description>193 meters from Point 3</description>\n" +
                "\t\t\t<gx:TimeStamp>\n" +
                "\t\t\t\t<when>" + timeStamp2 + "</when>\n" +
                "\t\t\t</gx:TimeStamp>\n" +
                "\t\t\t<styleUrl>#" + exclusiveIcon2 + "</styleUrl>\n" +
                "\t\t\t<Point>\n" +
                "\t\t\t\t<coordinates>38.3428861954351,56.27522642196566,0</coordinates>\n" +
                "\t\t\t</Point>\n" +
                "\t\t</Placemark>\n" +

                "\t\t<Placemark>\n" +
                "\t\t\t<name>Point that may by deleted</name>\n" +
                "\t\t\t<description>202m from Point 1</description>\n" +
                "\t\t\t<gx:TimeStamp>\n" +
                "\t\t\t\t<when>" + timeStamp3 + "</when>\n" +
                "\t\t\t</gx:TimeStamp>\n" +
                "\t\t\t<styleUrl>#" + removableIcon + "</styleUrl>\n" +
                "\t\t\t<Point>\n" +
                "\t\t\t\t<coordinates>38.34439565050742,56.27675775851414,0</coordinates>\n" +
                "\t\t\t</Point>\n" +
                "\t\t</Placemark>\n" +

                "</Document>\n" +
                "</kml>\n";
        kmlDocument = XmlTestUtils.getDocument(pointsAsIsoscelesTriangle);
        MultipartMainDto multipartMainDto = new MultipartMainDto();
        multipartMainDto.setDistanceUnit(DistanceUnits.METERS);

        int thinOutDistance = 300; //To thin out Placemarks closer than this
        multipartMainDto.setThinOutDistance(thinOutDistance);
        multipartMainDto.setThinOutIconsNames(Arrays.asList(exclusiveIcon1, exclusiveIcon2));
        multipartMainDto.setThinOutType(ThinOutTypes.EXCLUSIVE);

        xmlDomUtils = new XmlDomUtils(kmlDocument);
        kmlUtils = new KmlUtils(kmlDocument, xmlDomUtils);
        thinOutKmlPointsHandler = new ThinOutKmlPointsHandler(kmlUtils, fileService, htmlHandler);

        //WHEN
        thinOutKmlPointsHandler.thinOutPoints(kmlDocument, multipartMainDto);

        //THEN
        assertTrue(XmlTestUtils.containsTagWithChild(kmlDocument, "Placemark", "name", "Point to be retained by icon 1"));
        assertTrue(XmlTestUtils.containsTagWithChild(kmlDocument, "Placemark", "name", "Point to be retained by icon 2"));

        assertFalse(XmlTestUtils.containsTagWithChild(kmlDocument, "Placemark", "name", "Point that may by deleted"));
    }

    /**
     * Every tuple includes the last value as the distance in meters according to the Google Earth ruler.
     */
    @ParameterizedTest
    @CsvSource(value = {"2014-08-03T14:13:40Z,2014-08-03T14:13:50Z,2014-08-03T14:13:45Z",
            "2014-08-03T14:13:45Z,2014-08-03T14:13:50Z,2014-08-03T14:13:40Z",
            "2014-08-03T14:13:50Z,2014-08-03T14:13:40Z,2014-08-03T14:13:45Z",
            "2014-08-03T14:13:50Z,2014-08-03T14:13:45Z,2014-08-03T14:13:40Z"})
    public void placemarks_With_Any_Icons_Names_May_Be_Deleted_From_Document(
            String timeStamp1, String timeStamp2, String timeStamp3)
            throws IOException, ParserConfigurationException, SAXException {
        //GIVEN
        String iconName1 = "tourism-forest.png";
        String iconName2 = "moto.png";
        String iconName3 = "bicycle.png";
        //Three isosceles triangle Placemarks within 245 meters distance between each within the "thinOutDistance"
        String pointsAsIsoscelesTriangle = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:kml=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" +
                "<Document>\n" +
                "\t<name>Three sequential isosceles triangle Placemarks within 245 meters distance between each</name>\n" +

                "\t<StyleMap id=\"styleMapOf:" + iconName1 + "\">\n" +
                "\t\t<Pair>\n" +
                "\t\t\t<key>normal</key>\n" +
                "\t\t\t<styleUrl>#" + iconName1 + "</styleUrl>\n" +
                "\t\t</Pair>\n" +
                "\t\t<Pair>\n" +
                "\t\t\t<key>highlight</key>\n" +
                "\t\t\t<styleUrl>#highlighOf:" + iconName1 + "</styleUrl>\n" +
                "\t\t</Pair>\n" +
                "\t</StyleMap>\n" +

                "\t<Style id=\"" + iconName1 + "\">\n" +
                "\t\t<IconStyle>\n" +
                "\t\t\t<scale>0.472727</scale>\n" +
                "\t\t\t<Icon>\n" +
                "\t\t\t\t<href>files/" + iconName1 + "</href>\n" +
                "\t\t\t</Icon>\n" +
                "\t\t</IconStyle>\n" +
                "\t</Style>\n" +

                "\t<Style id=\"highlighOf:" + iconName1 + "\">\n" +
                "\t\t<IconStyle>\n" +
                "\t\t\t<scale>0.4</scale>\n" +
                "\t\t\t<Icon>\n" +
                "\t\t\t\t<href>files///C:folder/" + iconName1 + "</href>\n" +
                "\t\t\t</Icon>\n" +
                "\t\t</IconStyle>\n" +
                "\t</Style>\n" +

                "\t<Style id=\"" + iconName2 + "\">\n" +
                "\t\t<IconStyle>\n" +
                "\t\t\t<scale>0.472727</scale>\n" +
                "\t\t\t<Icon>\n" +
                "\t\t\t\t<href>files/" + iconName2 + "</href>\n" +
                "\t\t\t</Icon>\n" +
                "\t\t</IconStyle>\n" +
                "\t</Style>\n" +

                "\t<Style id=\"" + iconName3 + "\">\n" +
                "\t\t<IconStyle>\n" +
                "\t\t\t<scale>0.472727</scale>\n" +
                "\t\t\t<Icon>\n" +
                "\t\t\t\t<href>files/" + iconName3 + "</href>\n" +
                "\t\t\t</Icon>\n" +
                "\t\t</IconStyle>\n" +
                "\t</Style>\n" +

                "\t\t<Placemark>\n" +
                "\t\t\t<name>Point may be deleted by icon 1</name>\n" +
                "\t\t\t<description>232 meters from Point 2</description>\n" +
                "\t\t\t<gx:TimeStamp>\n" +
                "\t\t\t\t<when>" + timeStamp1 + "</when>\n" +
                "\t\t\t</gx:TimeStamp>\n" +
                "\t\t\t<styleUrl>#styleMapOf:" + iconName1 + "</styleUrl>\n" +
                "\t\t\t<Point>\n" +
                "\t\t\t\t<coordinates>38.34646245907663,56.27537585942488,0</coordinates>\n" +
                "\t\t\t</Point>\n" +
                "\t\t</Placemark>\n" +

                "\t\t<Placemark>\n" +
                "\t\t\t<name>Point may be deleted by icon 2</name>\n" +
                "\t\t\t<description>193 meters from Point 3</description>\n" +
                "\t\t\t<gx:TimeStamp>\n" +
                "\t\t\t\t<when>" + timeStamp2 + "</when>\n" +
                "\t\t\t</gx:TimeStamp>\n" +
                "\t\t\t<styleUrl>#" + iconName2 + "</styleUrl>\n" +
                "\t\t\t<Point>\n" +
                "\t\t\t\t<coordinates>38.3428861954351,56.27522642196566,0</coordinates>\n" +
                "\t\t\t</Point>\n" +
                "\t\t</Placemark>\n" +

                "\t\t<Placemark>\n" +
                "\t\t\t<name>Point may be deleted by icon 3</name>\n" +
                "\t\t\t<description>202m from Point 1</description>\n" +
                "\t\t\t<gx:TimeStamp>\n" +
                "\t\t\t\t<when>" + timeStamp3 + "</when>\n" +
                "\t\t\t</gx:TimeStamp>\n" +
                "\t\t\t<styleUrl>#" + iconName3 + "</styleUrl>\n" +
                "\t\t\t<Point>\n" +
                "\t\t\t\t<coordinates>38.34439565050742,56.27675775851414,0</coordinates>\n" +
                "\t\t\t</Point>\n" +
                "\t\t</Placemark>\n" +

                "</Document>\n" +
                "</kml>\n";
        kmlDocument = XmlTestUtils.getDocument(pointsAsIsoscelesTriangle);
        MultipartMainDto multipartMainDto = new MultipartMainDto();
        multipartMainDto.setDistanceUnit(DistanceUnits.METERS);

        int thinOutDistance = 300; //To thin out Placemarks closer than this
        multipartMainDto.setThinOutDistance(thinOutDistance);
        multipartMainDto.setThinOutIconsNames(Arrays.asList(iconName1, iconName2));
        multipartMainDto.setThinOutType(ThinOutTypes.ANY);

        xmlDomUtils = new XmlDomUtils(kmlDocument);
        kmlUtils = new KmlUtils(kmlDocument, xmlDomUtils);
        thinOutKmlPointsHandler = new ThinOutKmlPointsHandler(kmlUtils, fileService, htmlHandler);

        //WHEN
        thinOutKmlPointsHandler.thinOutPoints(kmlDocument, multipartMainDto);

        //THEN any Placemark may be deleted
        assertTrue(
                !XmlTestUtils.containsTagWithChild(kmlDocument, "Placemark", "name", "Point may be deleted by icon 1") ||
                        !XmlTestUtils.containsTagWithChild(kmlDocument, "Placemark", "name", "Point may be deleted by icon 2") ||
                        !XmlTestUtils.containsTagWithChild(kmlDocument, "Placemark", "name", "Point may be deleted by icon 3"));
    }

    /**
     * Every tuple includes the last value as the distance in meters according to the Google Earth ruler.
     */
    @Test
    public void style_With_Unique_Icon_Should_Be_Deleted_From_Document()
            throws IOException, ParserConfigurationException, SAXException {
        //GIVEN
        String iconName1 = "tourism-forest.png";
        String iconName2 = "moto.png";
        //Three isosceles triangle Placemarks within 245 meters distance between each within the "thinOutDistance"
        String pointsAsIsoscelesTriangle = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:kml=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" +
                "<Document>\n" +
                "\t<name>Three sequential isosceles triangle Placemarks within 245 meters distance between each</name>\n" +

                "\t<Style id=\"" + iconName1 + "\">\n" +
                "\t\t<IconStyle>\n" +
                "\t\t\t<scale>0.472727</scale>\n" +
                "\t\t\t<Icon>\n" +
                "\t\t\t\t<href>files/" + iconName1 + "</href>\n" +
                "\t\t\t</Icon>\n" +
                "\t\t</IconStyle>\n" +
                "\t</Style>\n" +

                "\t<Style id=\"" + iconName2 + "\">\n" +
                "\t\t<IconStyle>\n" +
                "\t\t\t<scale>0.4</scale>\n" +
                "\t\t\t<Icon>\n" +
                "\t\t\t\t<href>files///C:folder/" + iconName2 + "</href>\n" +
                "\t\t\t</Icon>\n" +
                "\t\t</IconStyle>\n" +
                "\t</Style>\n" +

                "\t\t<Placemark>\n" +
                "\t\t\t<name>Point has to be retained by date</name>\n" +
                "\t\t\t<description>232 meters from Point 2</description>\n" +
                "\t\t\t<gx:TimeStamp>\n" +
                "\t\t\t\t<when>2014-08-03T14:13:50Z</when>\n" +
                "\t\t\t</gx:TimeStamp>\n" +
                "\t\t\t<styleUrl>#" + iconName1 + "</styleUrl>\n" +
                "\t\t\t<Point>\n" +
                "\t\t\t\t<coordinates>38.34646245907663,56.27537585942488,0</coordinates>\n" +
                "\t\t\t</Point>\n" +
                "\t\t</Placemark>\n" +

                "\t\t<Placemark>\n" +
                "\t\t\t<name>Point has to be removed by date and distance 1</name>\n" +
                "\t\t\t<description>193 meters from Point 3</description>\n" +
                "\t\t\t<gx:TimeStamp>\n" +
                "\t\t\t\t<when>2014-08-03T14:13:45Z</when>\n" +
                "\t\t\t</gx:TimeStamp>\n" +
                "\t\t\t<styleUrl>#" + iconName2 + "</styleUrl>\n" +
                "\t\t\t<Point>\n" +
                "\t\t\t\t<coordinates>38.3428861954351,56.27522642196566,0</coordinates>\n" +
                "\t\t\t</Point>\n" +
                "\t\t</Placemark>\n" +

                "\t\t<Placemark>\n" +
                "\t\t\t<name>Point has to be removed by date and distance 2</name>\n" +
                "\t\t\t<description>202m from Point 1</description>\n" +
                "\t\t\t<gx:TimeStamp>\n" +
                "\t\t\t\t<when>2014-08-03T14:13:40Z</when>\n" +
                "\t\t\t</gx:TimeStamp>\n" +
                "\t\t\t<styleUrl>#" + iconName1 + "</styleUrl>\n" +
                "\t\t\t<Point>\n" +
                "\t\t\t\t<coordinates>38.34439565050742,56.27675775851414,0</coordinates>\n" +
                "\t\t\t</Point>\n" +
                "\t\t</Placemark>\n" +

                "</Document>\n" +
                "</kml>\n";
        kmlDocument = XmlTestUtils.getDocument(pointsAsIsoscelesTriangle);
        MultipartMainDto multipartMainDto = new MultipartMainDto();
        multipartMainDto.setDistanceUnit(DistanceUnits.METERS);

        int thinOutDistance = 300; //To thin out Placemarks closer than this (any from the current Document)
        multipartMainDto.setThinOutDistance(thinOutDistance);
        multipartMainDto.setThinOutType(ThinOutTypes.ANY);

        xmlDomUtils = new XmlDomUtils(kmlDocument);
        kmlUtils = new KmlUtils(kmlDocument, xmlDomUtils);
        thinOutKmlPointsHandler = new ThinOutKmlPointsHandler(kmlUtils, fileService, htmlHandler);

        //WHEN
        thinOutKmlPointsHandler.thinOutPoints(kmlDocument, multipartMainDto);

        System.out.println(XmlTestUtils.getAsText(kmlDocument));

        //THEN
        assertTrue(XmlTestUtils.containsTagWithAttribute(kmlDocument, "Style", "id", iconName1));
        assertFalse(XmlTestUtils.containsTagWithAttribute(kmlDocument, "Style", "id", iconName2));
    }

    //TODO: Styles and StyleMaps also have to be deleted
    //TODO: locus photo thumbnails should be deleted
}