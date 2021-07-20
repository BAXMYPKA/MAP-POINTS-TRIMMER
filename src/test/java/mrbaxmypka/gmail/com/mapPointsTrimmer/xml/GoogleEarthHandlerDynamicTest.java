package mrbaxmypka.gmail.com.mapPointsTrimmer.xml;

import mrbaxmypka.gmail.com.mapPointsTrimmer.entitiesDto.MultipartDto;
import mrbaxmypka.gmail.com.mapPointsTrimmer.services.FileService;
import mrbaxmypka.gmail.com.mapPointsTrimmer.services.GoogleIconsService;
import mrbaxmypka.gmail.com.mapPointsTrimmer.utils.GoogleIconsCache;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for dynamic display with StyleMap for various Styles
 * {@code
 * "Static" means either unmapped <Style> or <key>normal</key> url to <Style>.
 * "Dynamic" means how to display it on mouse over (hovering) from <key>highlight</key> url to <Style>.
 * <StyleMap id="styleMap1">
 * <Pair>
 * ===>>> <key>normal</key> <<<=== STATIC PARAMETER
 * <styleUrl>#style1</styleUrl>
 * </Pair>
 * <Pair>
 * ===>>> <key>highlight</key> <<<=== DYNAMIC PARAMETER
 * <styleUrl>#style3</styleUrl>
 * </Pair>
 * </StyleMap>
 * ===>>> <Style id="style1"> <<<=== STATIC DISPLAY STYLE
 * <IconStyle>
 * <Icon>
 * <href>http://maps.google.com/mapfiles/kml/shapes/poi.png</href>
 * </Icon>
 * </IconStyle>
 * </Style>
 * ===>>> <Style id="style2"> <<<=== ON MOUSE OVER (DYNAMIC) DISPLAY STYLE
 * <IconStyle>
 * <Icon>
 * <href>http://maps.google.com/mapfiles/kml/shapes/poi.png</href>
 * </Icon>
 * </IconStyle>
 * </Style>
 * ===>>> <Style id="style3"> <<<=== UNMAPPED STATIC DISPLAY STYLE
 * <IconStyle>
 * <Icon>
 * <href>http://maps.google.com/mapfiles/kml/shapes/earthquake.png</href>
 * </Icon>
 * </IconStyle>
 * <LabelStyle>
 * <scale>0.8</scale>
 * </LabelStyle>
 * </Style>
 * }
 * {@code
 * <StyleMap id="styleMap1">
 * <Pair>
 * <key>normal</key>
 * <styleUrl>#style1</styleUrl>
 * </Pair>
 * <Pair>
 * <key>highlight</key>
 * <styleUrl>#style-1-cloned</styleUrl>
 * </Pair>
 * </StyleMap>
 * <Style id="style1">
 * <IconStyle>
 * <scale>0.8</scale>
 * <Icon>
 * <href>http://maps.google.com/mapfiles/kml/shapes/poi.png</href>
 * </Icon>
 * <hotSpot x="0.5" y="0" xunits="fraction" yunits="fraction"/>
 * </IconStyle>
 * <LabelStyle>
 * <scale>0.7</scale>
 * </LabelStyle>
 * </Style>
 * <Style id="style-1-cloned">
 * <IconStyle>
 * <scale>0.8</scale>
 * <Icon>
 * <href>http://maps.google.com/mapfiles/kml/shapes/poi.png</href>
 * </Icon>
 * <hotSpot x="0.5" y="0" xunits="fraction" yunits="fraction"/>
 * </IconStyle>
 * <LabelStyle>
 * <scale>0.7</scale>
 * </LabelStyle>
 * </Style>
 * }
 */
public class GoogleEarthHandlerDynamicTest {
	
	private MultipartDto multipartDto;
	private MultipartFile multipartFile;
	private GoogleEarthHandler googleEarthHandler;
	private GoogleIconsCache googleIconsCache = new GoogleIconsCache();
	private FileService fileService = new FileService();
	private HtmlHandler htmlHandler = new HtmlHandler(fileService);
	private LocusMapHandler locusMapHandler;
	private GoogleIconsService googleIconsService = new GoogleIconsService(googleIconsCache);
	private KmlHandler kmlHandler = new KmlHandler(htmlHandler, googleIconsService, fileService);
	private String googleEarthKml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
		"<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:kml=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" +
		"\t<Document>\n" +
		"\t\t<name>Google Earth Test Poi</name>\n" +
		"\t\t<StyleMap id=\"styleMap1\">\n" +
		"\t\t\t<Pair>\n" +
		"\t\t\t\t<key>normal</key>\n" +
		"\t\t\t\t<styleUrl>#style1</styleUrl>\n" +
		"\t\t\t</Pair>\n" +
		"\t\t\t<Pair>\n" +
		"\t\t\t\t<key>highlight</key>\n" +
		"\t\t\t\t<styleUrl>#style3</styleUrl>\n" +
		"\t\t\t</Pair>\n" +
		"\t\t</StyleMap>\n" +
		"\t\t<StyleMap id=\"styleMap2\">\n" +
		"\t\t\t<Pair>\n" +
		"\t\t\t\t<key>normal</key>\n" +
		"\t\t\t\t<styleUrl>#style2</styleUrl>\n" +
		"\t\t\t</Pair>\n" +
		"\t\t\t<Pair>\n" +
		"\t\t\t\t<key>highlight</key>\n" +
		"\t\t\t\t<styleUrl>#style3</styleUrl>\n" +
		"\t\t\t</Pair>\n" +
		"\t\t</StyleMap>\n" +
		"\t\t<Style id=\"style1\">\n" +
		"\t\t\t<IconStyle>\n" +
		"\t\t\t\t<scale>0.8</scale>\n" +
		"\t\t\t\t<Icon>\n" +
		"\t\t\t\t\t<href>http://maps.google.com/mapfiles/kml/shapes/poi.png</href>\n" +
		"\t\t\t\t</Icon>\n" +
		"\t\t\t</IconStyle>\n" +
		"\t\t</Style>\n" +
		"\t\t<Style id=\"style2\">\n" +
		"\t\t\t<IconStyle>\n" +
		"\t\t\t\t<scale>0.8</scale>\n" +
		"\t\t\t\t<Icon>\n" +
		"\t\t\t\t\t<href>http://maps.google.com/mapfiles/kml/shapes/poi.png</href>\n" +
		"\t\t\t\t</Icon>\n" +
		"\t\t\t\t<hotSpot x=\"0.5\" y=\"0\" xunits=\"fraction\" yunits=\"fraction\"/>\n" +
		"\t\t\t</IconStyle>\n" +
		"\t\t</Style>\n" +
		"\t\t<Style id=\"style3\">\n" +
		"\t\t\t<IconStyle>\n" +
		"\t\t\t\t<scale>0.6</scale>\n" +
		"\t\t\t\t<Icon>\n" +
		"\t\t\t\t\t<href>http://maps.google.com/mapfiles/kml/shapes/earthquake.png</href>\n" +
		"\t\t\t\t</Icon>\n" +
		"\t\t\t</IconStyle>\n" +
		"\t\t</Style>\n" +
		"\t\t<Style id=\"style4\">\n" +
		"\t\t\t<IconStyle>\n" +
		"\t\t\t\t<scale>0.709091</scale>\n" +
		"\t\t\t\t<Icon>\n" +
		"\t\t\t\t\t<href>http://maps.google.com/mapfiles/kml/shapes/earthquake.png</href>\n" +
		"\t\t\t\t</Icon>\n" +
		"\t\t\t\t<hotSpot x=\"0.5\" y=\"0\" xunits=\"fraction\" yunits=\"fraction\"/>\n" +
		"\t\t\t</IconStyle>\n" +
		"\t\t\t<LabelStyle>\n" +
		"\t\t\t\t<scale>0.8</scale>\n" +
		"\t\t\t</LabelStyle>\n" +
		"\t\t</Style>\n" +
		"\t\t<Folder>\n" +
		"\t\t\t<name>Folder with POI</name>\n" +
		"\t\t\t<Placemark>\n" +
		"\t\t\t\t<name>Test Placemark 1</name>\n" +
		"\t\t\t\t<styleUrl>#styleMap1</styleUrl>\n" +
		"\t\t\t\t<Point>\n" +
		"\t\t\t\t\t<coordinates>38.547163,55.88113662000001,133</coordinates>\n" +
		"\t\t\t\t</Point>\n" +
		"\t\t\t</Placemark>\n" +
		"\t\t\t<Placemark>\n" +
		"\t\t\t\t<name>Test Placemark 2</name>\n" +
		"\t\t\t\t<styleUrl>#styleMap2</styleUrl>\n" +
		"\t\t\t\t<Point>\n" +
		"\t\t\t\t\t<coordinates>38.54269981,55.88994587,145</coordinates>\n" +
		"\t\t\t\t</Point>\n" +
		"\t\t\t</Placemark>\n" +
		"\t\t\t<Placemark>\n" +
		"\t\t\t\t<name>Test Placemark 3</name>\n" +
		"\t\t\t\t<styleUrl>#style3</styleUrl>\n" +
		"\t\t\t\t<Point>\n" +
		"\t\t\t\t\t<coordinates>38.5409832,55.89456632,143</coordinates>\n" +
		"\t\t\t\t</Point>\n" +
		"\t\t\t</Placemark>\n" +
		"\t\t\t<Placemark>\n" +
		"\t\t\t\t<name>Test Placemark 4</name>\n" +
		"\t\t\t\t<styleUrl>#style4</styleUrl>\n" +
		"\t\t\t\t<Point>\n" +
		"\t\t\t\t\t<coordinates>38.53305459,55.91967435,136</coordinates>\n" +
		"\t\t\t\t</Point>\n" +
		"\t\t\t</Placemark>\n" +
		"\t\t</Folder>\n" +
		"\t</Document>\n" +
		"</kml>";
	
	@Test
	public void with_Dynamic_Parameters_Should_Create_StyleMaps_And_Replace_StyleUrls_In_Placemarks()
		throws IOException, SAXException, ParserConfigurationException, TransformerException {
		//GIVEN The googleKml with only 2 <Style>
		String googleKml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:kml=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" +
			"\t<Document>\n" +
			"\t\t<name>Google Earth Test Poi</name>\n" +
			"\t\t<Style id=\"style1\">\n" +
			"\t\t\t<IconStyle>\n" +
			"\t\t\t\t<Icon/>\n" +
			"\t\t\t</IconStyle>\n" +
			"\t\t</Style>\n" +
			"\t\t<Style id=\"style2\">\n" +
			"\t\t\t<IconStyle>\n" +
			"\t\t\t\t<Icon/>\n" +
			"\t\t\t</IconStyle>\n" +
			"\t\t</Style>\n" +
			"\t\t<Placemark>\n" +
			"\t\t\t<name>Test Placemark 1</name>\n" +
			"\t\t\t<styleUrl>#style1</styleUrl>\n" +
			"\t\t\t<Point/>\n" +
			"\t\t</Placemark>\n" +
			"\t\t<Placemark>\n" +
			"\t\t\t<name>Test Placemark 2</name>\n" +
			"\t\t\t<styleUrl>#style2</styleUrl>\n" +
			"\t\t\t<Point/>\n" +
			"\t\t</Placemark>\n" +
			"\t</Document>\n" +
			"</kml>";
		multipartFile = new MockMultipartFile("GoogleEarth.kml", googleKml.getBytes(StandardCharsets.UTF_8));
		multipartDto = new MultipartDto(multipartFile);
		multipartDto.setPointIconSizeDynamic(50);
		Document document = XmlTestUtils.getDocument(multipartDto);
		googleEarthHandler = new GoogleEarthHandler(new KmlUtils(document, new XmlDomUtils(document)));
		
		//WHEN set any dynamic
		Document processedDocument = googleEarthHandler.processKml(document, multipartDto);
		
		String resultingKml = kmlHandler.writeTransformedDocument(processedDocument, true);
//		System.out.println(resultingKml);
		
		//THEN
		// 2 <StyleMap> have to be created
		assertTrue(
			XmlTestUtils.containsParentsWithChildren(processedDocument, "StyleMap", 2, "Pair", null));
		// 2 additional <Style> 4 have to be created as "highlight" styles (4 <Style> in total>
		assertTrue(
			XmlTestUtils.containsParentsWithChildren(processedDocument, "Style", 4, "IconStyle", null));
		// <Placemark>'s are no more reference to <Style>'s
		assertFalse(
			XmlTestUtils.containsParentWithChild(processedDocument, "Placemark", "styleUrl", "#style1"));
		assertFalse(XmlTestUtils.containsParentWithChild(processedDocument, "Placemark", "styleUrl", "#style2"));
		// <Placemark>'s are reference to <StyleMap>'s
		assertTrue(
			XmlTestUtils.containsParentWithChild(processedDocument, "Placemark", "styleUrl", "#styleMapOf:style1"));
		assertTrue(
			XmlTestUtils.containsParentWithChild(processedDocument, "Placemark", "styleUrl", "#styleMapOf:style2"));
	}
	
	@Test
	public void with_Dynamic_Parameters_Should_Replace_Styles_With_StyleMaps_In_StyleUrls_In_Placemarks()
		throws IOException, SAXException, ParserConfigurationException, TransformerException {
		//GIVEN The googleKml with <Placemark>'s
		String googleKml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:kml=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" +
			"<Document>\n" +
			"\t<name>Google Earth Test Poi</name>\n" +
			"\t<StyleMap id=\"styleMap1\">\n" +
			"\t\t<Pair>\n" +
			"\t\t\t<key>normal</key>\n" +
			"\t\t\t<styleUrl>#style1</styleUrl>\n" +
			"\t\t</Pair>\n" +
			"\t\t<Pair>\n" +
			"\t\t\t<key>highlight</key>\n" +
			"\t\t\t<styleUrl>#style2</styleUrl>\n" +
			"\t\t</Pair>\n" +
			"\t</StyleMap>\n" +
			"\t<Style id=\"style1\">\n" +
			"\t\t<IconStyle></IconStyle>\n" +
			"\t</Style>\n" +
			"\t<Style id=\"style2\">\n" +
			"\t\t<IconStyle></IconStyle>\n" +
			"\t</Style>\n" +
			"\t<Style id=\"style3\">\n" +
			"\t\t<IconStyle></IconStyle>\n" +
			"\t</Style>\n" +
			"\t<Placemark>\n" +
			"\t\t\t<name>Test Placemark 1</name>\n" +
			"\t\t\t<styleUrl>#styleMap1</styleUrl>\n" +
			"\t</Placemark>\n" +
			"\t<Placemark>\n" +
			"\t\t\t<name>Test Placemark 2</name>\n" +
			"\t\t\t<styleUrl>#style3</styleUrl>\n" +
			"\t</Placemark>\n" +
			"</Document>\n" +
			"</kml>";
		multipartFile = new MockMultipartFile("GoogleEarth.kml", googleKml.getBytes(StandardCharsets.UTF_8));
		multipartDto = new MultipartDto(multipartFile);
		multipartDto.setPointIconSizeDynamic(50);
		Document document = XmlTestUtils.getDocument(multipartDto);
		googleEarthHandler = new GoogleEarthHandler(new KmlUtils(document, new XmlDomUtils(document)));

		//WHEN set any dynamic
		Document processedDocument = googleEarthHandler.processKml(document, multipartDto);
		
		String resultingKml = kmlHandler.writeTransformedDocument(processedDocument, true);
//		System.out.println(resultingKml);
		
		//THEN
		// <Placemark> 1 still references to #styleMap1
		assertTrue(
			XmlTestUtils.containsParentWithChild(processedDocument, "Placemark", "styleUrl", "#styleMap1"));
		// <Placemark> 2 no more references to #style3
		assertFalse(
			XmlTestUtils.containsParentWithChild(processedDocument, "Placemark", "styleUrl", "#style3"));
		// <Placemark> 2 references to a new #styleMapOf:style3
		assertTrue(
			XmlTestUtils.containsParentWithChild(processedDocument, "Placemark", "styleUrl", "#styleMapOf:style3"));
		
	}
	
	@Test
	public void pointIconSizeDynamic_Should_Update_Only_Highlighted_Styles()
		throws IOException, SAXException, ParserConfigurationException, TransformerException {
		//GIVEN <Style id="style2"> is the only "highligh" style
		String googleKml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:kml=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" +
			"<Document>\n" +
			"\t<name>Google Earth Test Poi</name>\n" +
			"\t<StyleMap id=\"styleMap1\">\n" +
			"\t\t<Pair>\n" +
			"\t\t\t<key>normal</key>\n" +
			"\t\t\t<styleUrl>#style1</styleUrl>\n" +
			"\t\t</Pair>\n" +
			"\t\t<Pair>\n" +
			"\t\t\t<key>highlight</key>\n" +
			"\t\t\t<styleUrl>#style2</styleUrl>\n" +
			"\t\t</Pair>\n" +
			"\t</StyleMap>\n" +
			"\t<Style id=\"style1\">\n" +
			"\t\t<IconStyle>\n" +
			"\t\t\t<scale>0.0</scale>\n" +
			"\t\t</IconStyle>\n" +
			"\t</Style>\n" +
			"\t<Style id=\"style2\">\n" +
			"\t\t<IconStyle>\n" +
			"\t\t\t<scale>0.0</scale>\n" +
			"\t\t</IconStyle>\n" +
			"\t</Style>\n" +
			"\t<Placemark>\n" +
			"\t\t\t<name>Test Placemark 1</name>\n" +
			"\t\t\t<styleUrl>#styleMap1</styleUrl>\n" +
			"\t</Placemark>\n" +
			"</Document>\n" +
			"</kml>";
		multipartFile = new MockMultipartFile("GoogleEarth.kml", googleKml.getBytes(StandardCharsets.UTF_8));
		multipartDto = new MultipartDto(multipartFile);
		multipartDto.setPointIconSizeDynamic(50); //0.5 as the scale
		Document document = XmlTestUtils.getDocument(multipartDto);
		googleEarthHandler = new GoogleEarthHandler(new KmlUtils(document, new XmlDomUtils(document)));

		//WHEN set any dynamic
		Document processedDocument = googleEarthHandler.processKml(document, multipartDto);
		
		String resultingKml = kmlHandler.writeTransformedDocument(processedDocument, true);
//		System.out.println(resultingKml);
		
		//THEN
		//Normal <Style id="style1"> IconStyle scale should be unchanged (0.0)
		assertTrue(XmlTestUtils.containsParentsWithChildren(processedDocument, "IconStyle", 1, "scale", "0.0"));
		//Highlight <Style id="style2"> IconStyle scale should be "0.5"
		assertTrue(XmlTestUtils.containsParentsWithChildren(processedDocument, "IconStyle", 1, "scale", "0.5"));
	}
	
	@Test
	public void pointTextSizeDynamic_Should_Update_Only_Highlighted_Styles()
		throws IOException, SAXException, ParserConfigurationException, TransformerException {
		//GIVEN <Style id="style2"> is the only "highligh" style
		String googleKml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:kml=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" +
			"<Document>\n" +
			"\t<name>Google Earth Test Poi</name>\n" +
			"\t<StyleMap id=\"styleMap1\">\n" +
			"\t\t<Pair>\n" +
			"\t\t\t<key>normal</key>\n" +
			"\t\t\t<styleUrl>#style1</styleUrl>\n" +
			"\t\t</Pair>\n" +
			"\t\t<Pair>\n" +
			"\t\t\t<key>highlight</key>\n" +
			"\t\t\t<styleUrl>#style2</styleUrl>\n" +
			"\t\t</Pair>\n" +
			"\t</StyleMap>\n" +
			"\t<Style id=\"style1\">\n" +
			"\t\t<IconStyle>\n" +
			"\t\t\t<scale>0.0</scale>\n" +
			"\t\t</IconStyle>\n" +
			"\t\t<LabelStyle>\n" +
			"\t\t\t<scale>0.0</scale>\n" +
			"\t\t\t<color>00000000</color>\n" +
			"\t\t</LabelStyle>\n" +
			"\t</Style>\n" +
			"\t<Style id=\"style2\">\n" +
			"\t\t<IconStyle>\n" +
			"\t\t\t<scale>0.0</scale>\n" +
			"\t\t</IconStyle>\n" +
			"\t\t<LabelStyle>\n" +
			"\t\t\t<scale>0.0</scale>\n" +
			"\t\t\t<color>00000000</color>\n" +
			"\t\t</LabelStyle>\n" +
			"\t</Style>\n" +
			"\t<Placemark>\n" +
			"\t\t\t<name>Test Placemark 1</name>\n" +
			"\t\t\t<styleUrl>#styleMap1</styleUrl>\n" +
			"\t</Placemark>\n" +
			"</Document>\n" +
			"</kml>";
		multipartFile = new MockMultipartFile("GoogleEarth.kml", googleKml.getBytes(StandardCharsets.UTF_8));
		multipartDto = new MultipartDto(multipartFile);
		multipartDto.setPointTextSizeDynamic(60); //0.6 as the scale
		Document document = XmlTestUtils.getDocument(multipartDto);
		googleEarthHandler = new GoogleEarthHandler(new KmlUtils(document, new XmlDomUtils(document)));

		//WHEN
		Document processedDocument = googleEarthHandler.processKml(document, multipartDto);
		
		String resultingKml = kmlHandler.writeTransformedDocument(processedDocument, true);
//		System.out.println(resultingKml);
		
		//THEN
		//Normal <Style id="style1"> LabelStyle scale should be unchanged (0.0)
		assertTrue(XmlTestUtils.containsParentsWithChildren(processedDocument, "LabelStyle", 1, "scale", "0.0"));
		//Highlight <Style id="style2"> LabelStyle scale should be "0.6"
		assertTrue(XmlTestUtils.containsParentsWithChildren(processedDocument, "LabelStyle", 1, "scale", "0.6"));
	}
	
	@Test
	public void pointTextColorDynamic_Should_Update_Only_Highlighted_Styles()
		throws IOException, SAXException, ParserConfigurationException, TransformerException {
		//GIVEN <Style id="style2"> is the only "highligh" style
		String googleKml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:kml=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" +
			"<Document>\n" +
			"\t<name>Google Earth Test Poi</name>\n" +
			"\t<StyleMap id=\"styleMap1\">\n" +
			"\t\t<Pair>\n" +
			"\t\t\t<key>normal</key>\n" +
			"\t\t\t<styleUrl>#style1</styleUrl>\n" +
			"\t\t</Pair>\n" +
			"\t\t<Pair>\n" +
			"\t\t\t<key>highlight</key>\n" +
			"\t\t\t<styleUrl>#style2</styleUrl>\n" +
			"\t\t</Pair>\n" +
			"\t</StyleMap>\n" +
			"\t<Style id=\"style1\">\n" +
			"\t\t<IconStyle>\n" +
			"\t\t\t<scale>0.0</scale>\n" +
			"\t\t</IconStyle>\n" +
			"\t\t<LabelStyle>\n" +
			"\t\t\t<scale>0.0</scale>\n" +
			"\t\t\t<color>00000000</color>\n" +
			"\t\t</LabelStyle>\n" +
			"\t</Style>\n" +
			"\t<Style id=\"style2\">\n" +
			"\t\t<IconStyle>\n" +
			"\t\t\t<scale>0.0</scale>\n" +
			"\t\t</IconStyle>\n" +
			"\t\t<LabelStyle>\n" +
			"\t\t\t<scale>0.0</scale>\n" +
			"\t\t\t<color>00000000</color>\n" +
			"\t\t</LabelStyle>\n" +
			"\t</Style>\n" +
			"\t<Placemark>\n" +
			"\t\t\t<name>Test Placemark 1</name>\n" +
			"\t\t\t<styleUrl>#styleMap1</styleUrl>\n" +
			"\t</Placemark>\n" +
			"</Document>\n" +
			"</kml>";
		multipartFile = new MockMultipartFile("GoogleEarth.kml", googleKml.getBytes(StandardCharsets.UTF_8));
		multipartDto = new MultipartDto(multipartFile);
		multipartDto.setPointTextHexColorDynamic("#ffffff");
		Document document = XmlTestUtils.getDocument(multipartDto);
		googleEarthHandler = new GoogleEarthHandler(new KmlUtils(document, new XmlDomUtils(document)));

		//WHEN
		Document processedDocument = googleEarthHandler.processKml(document, multipartDto);
		
		String resultingKml = kmlHandler.writeTransformedDocument(processedDocument, true);
//		System.out.println(resultingKml);
		
		//THEN
		//Normal <Style id="style1"> LabelStyle color should be unchanged (00000000)
		assertTrue(XmlTestUtils.containsParentsWithChildren(processedDocument, "LabelStyle", 1, "color", "00000000"));
		//Highlight <Style id="style2"> LabelStyle color should be "ffffffff"
		assertTrue(XmlTestUtils.containsParentsWithChildren(processedDocument, "LabelStyle", 1, "color", "ffffffff"));
	}
	
	@Test
	public void pointTextOpacityDynamic_Should_Update_Only_Highlighted_Styles()
		throws IOException, SAXException, ParserConfigurationException, TransformerException {
		//GIVEN <Style id="style2"> is the only "highligh" style
		String googleKml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:kml=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" +
			"<Document>\n" +
			"\t<name>Google Earth Test Poi</name>\n" +
			"\t<StyleMap id=\"styleMap1\">\n" +
			"\t\t<Pair>\n" +
			"\t\t\t<key>normal</key>\n" +
			"\t\t\t<styleUrl>#style1</styleUrl>\n" +
			"\t\t</Pair>\n" +
			"\t\t<Pair>\n" +
			"\t\t\t<key>highlight</key>\n" +
			"\t\t\t<styleUrl>#style2</styleUrl>\n" +
			"\t\t</Pair>\n" +
			"\t</StyleMap>\n" +
			"\t<Style id=\"style1\">\n" +
			"\t\t<IconStyle>\n" +
			"\t\t\t<scale>0.0</scale>\n" +
			"\t\t</IconStyle>\n" +
			"\t\t<LabelStyle>\n" +
			"\t\t\t<scale>0.0</scale>\n" +
			"\t\t\t<color>00000000</color>\n" +
			"\t\t</LabelStyle>\n" +
			"\t</Style>\n" +
			"\t<Style id=\"style2\">\n" +
			"\t\t<IconStyle>\n" +
			"\t\t\t<scale>0.0</scale>\n" +
			"\t\t</IconStyle>\n" +
			"\t\t<LabelStyle>\n" +
			"\t\t\t<scale>0.0</scale>\n" +
			"\t\t\t<color>00000000</color>\n" +
			"\t\t</LabelStyle>\n" +
			"\t</Style>\n" +
			"\t<Placemark>\n" +
			"\t\t\t<name>Test Placemark 1</name>\n" +
			"\t\t\t<styleUrl>#styleMap1</styleUrl>\n" +
			"\t</Placemark>\n" +
			"</Document>\n" +
			"</kml>";
		multipartFile = new MockMultipartFile("GoogleEarth.kml", googleKml.getBytes(StandardCharsets.UTF_8));
		multipartDto = new MultipartDto(multipartFile);
		multipartDto.setPointTextHexColorDynamic("#ffffff");
		multipartDto.setPointTextOpacityDynamic(00);
		Document document = XmlTestUtils.getDocument(multipartDto);
		googleEarthHandler = new GoogleEarthHandler(new KmlUtils(document, new XmlDomUtils(document)));

		//WHEN
		Document processedDocument = googleEarthHandler.processKml(document, multipartDto);
		
		String resultingKml = kmlHandler.writeTransformedDocument(processedDocument, true);
//		System.out.println(resultingKml);
		
		//THEN
		//Normal <Style id="style1"> LabelStyle color should be unchanged (00000000)
		assertTrue(XmlTestUtils.containsParentsWithChildren(processedDocument, "LabelStyle", 1, "color", "00000000"));
		//Highlight <Style id="style2"> LabelStyle color should be "ffffffff"
		assertTrue(XmlTestUtils.containsParentsWithChildren(processedDocument, "LabelStyle", 1, "color", "00ffffff"));
	}
	
	@ParameterizedTest
	@ValueSource(ints = {0, 100})
	public void pointIconOpacityDynamic_Should_Update_Only_Highlighted_Styles(Integer opacity)
		throws IOException, SAXException, ParserConfigurationException, TransformerException {
		//GIVEN Only highlighted Style id=style2 contains IconStyle
		String googleKml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:kml=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" +
			"<Document>\n" +
			"\t<name>Google Earth Test Poi</name>\n" +
			"\t<StyleMap id=\"styleMap1\">\n" +
			"\t\t<Pair>\n" +
			"\t\t\t<key>normal</key>\n" +
			"\t\t\t<styleUrl>#style1</styleUrl>\n" +
			"\t\t</Pair>\n" +
			"\t\t<Pair>\n" +
			"\t\t\t<key>highlight</key>\n" +
			"\t\t\t<styleUrl>#style2</styleUrl>\n" +
			"\t\t</Pair>\n" +
			"\t</StyleMap>\n" +
			"\t<Style id=\"style1\">\n" +
			"\t\t<LabelStyle>\n" +
			"\t\t\t<scale>0.0</scale>\n" +
			"\t\t\t<color>00000000</color>\n" +
			"\t\t</LabelStyle>\n" +
			"\t</Style>\n" +
			"\t<Style id=\"style2\">\n" +
			"\t\t<IconStyle>\n" +
			"\t\t\t<scale>0.0</scale>\n" +
			"\t\t</IconStyle>\n" +
			"\t\t<LabelStyle>\n" +
			"\t\t\t<scale>0.0</scale>\n" +
			"\t\t\t<color>00000000</color>\n" +
			"\t\t</LabelStyle>\n" +
			"\t</Style>\n" +
			"\t<Placemark>\n" +
			"\t\t\t<name>Test Placemark 1</name>\n" +
			"\t\t\t<styleUrl>#styleMap1</styleUrl>\n" +
			"\t</Placemark>\n" +
			"</Document>\n" +
			"</kml>";
		multipartFile = new MockMultipartFile("GoogleEarth.kml", googleKml.getBytes(StandardCharsets.UTF_8));
		multipartDto = new MultipartDto(multipartFile);
		multipartDto.setPointIconOpacityDynamic(opacity);
		String iconOpacityWithColor = null;
		if (opacity == 0) {
			iconOpacityWithColor = "00ffffff";
		} else if (opacity == 100){
			iconOpacityWithColor = "ffffffff";
		}
		Document document = XmlTestUtils.getDocument(multipartDto);
		googleEarthHandler = new GoogleEarthHandler(new KmlUtils(document, new XmlDomUtils(document)));

		//WHEN
		Document processedDocument = googleEarthHandler.processKml(document, multipartDto);
		
		String resultingKml = kmlHandler.writeTransformedDocument(processedDocument, true);
//		System.out.println(resultingKml);
		
		//THEN
		//Only highlight <Style id="style2"> IconStyle should exist and be with required <color> text
		assertTrue(XmlTestUtils.containsParentsWithChildren(processedDocument, "IconStyle", 1, "color", iconOpacityWithColor));
	}
	
	@Test
	public void reference_To_Same_Style_From_Two_Placemarks_Should_Update_Only_Highlighted_Styles()
		throws IOException, SAXException, ParserConfigurationException, TransformerException {
		//GIVEN <Style id="style1"> is referenced from two <Placemakr/>'s
		String googleKml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:kml=\"http://www.opengis.net/kml/2.2\">\n" +
			"<Document>\n" +
			"\t<name>Test poi</name>\n" +
			"\t<atom:author><atom:name>Locus (Android)</atom:name></atom:author>\n" +
			"\t<Style id=\"style1\">\n" +
			"\t\t<IconStyle></IconStyle>\n" +
			"\t\t<LabelStyle></LabelStyle>\n" +
			"\t</Style>\n" +
			"\t<Placemark>\n" +
			"\t\t<name>Test placemark1</name>\n" +
			"\t\t<description></description>\n" +
			"\t\t<styleUrl>#style1</styleUrl>\n" +
			"\t</Placemark>\n" +
			"\t\t<Placemark>\n" +
			"\t\t<name>Test placemark2</name>\n" +
			"\t\t<description></description>\n" +
			"\t\t<styleUrl>#style1</styleUrl>\n" +
			"\t</Placemark>\n" +
			"</Document>\n" +
			"</kml>";
		multipartFile = new MockMultipartFile("GoogleEarth.kml", googleKml.getBytes(StandardCharsets.UTF_8));
		multipartDto = new MultipartDto(multipartFile);
		multipartDto.setPointIconSize(60);
		multipartDto.setPointIconSizeDynamic(80);
		Document document = XmlTestUtils.getDocument(multipartDto);
		googleEarthHandler = new GoogleEarthHandler(new KmlUtils(document, new XmlDomUtils(document)));

		//WHEN
		Document processedDocument = googleEarthHandler.processKml(document, multipartDto);
		
		String resultingKml = kmlHandler.writeTransformedDocument(processedDocument, true);
//		System.out.println(resultingKml);
		
		//THEN
		//Normal <Style id="style1"> IconStyle scale should be 0.6
		assertTrue(XmlTestUtils
			.containsParentsWithChildren(processedDocument, "IconStyle", 1, "scale", "0.6"));
		//Highlight <Style id="#styleMapOf:style1"> IconsStyle scale should be 0.8
		assertTrue(XmlTestUtils
			.containsParentsWithChildren(processedDocument, "IconStyle", 1, "scale", "0.8"));
	}
	
	@Test
	public void reference_To_Same_Style_From_Two_Placemarks_Should_Create_Single_StyleMap()
		throws IOException, SAXException, ParserConfigurationException, TransformerException {
		//GIVEN <Style id="style1"> is referenced from two <Placemakr/>'s
		String googleKml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:kml=\"http://www.opengis.net/kml/2.2\">\n" +
			"<Document>\n" +
			"\t<name>Test poi</name>\n" +
			"\t<atom:author><atom:name>Locus (Android)</atom:name></atom:author>\n" +
			"\t<Style id=\"style1\">\n" +
			"\t\t<IconStyle></IconStyle>\n" +
			"\t\t<LabelStyle></LabelStyle>\n" +
			"\t</Style>\n" +
			"\t<Placemark>\n" +
			"\t\t<name>Test placemark1</name>\n" +
			"\t\t<description></description>\n" +
			"\t\t<styleUrl>#style1</styleUrl>\n" +
			"\t</Placemark>\n" +
			"\t\t<Placemark>\n" +
			"\t\t<name>Test placemark2</name>\n" +
			"\t\t<description></description>\n" +
			"\t\t<styleUrl>#style1</styleUrl>\n" +
			"\t</Placemark>\n" +
			"</Document>\n" +
			"</kml>";
		multipartFile = new MockMultipartFile("GoogleEarth.kml", googleKml.getBytes(StandardCharsets.UTF_8));
		multipartDto = new MultipartDto(multipartFile);
		multipartDto.setPointIconSize(60);
		multipartDto.setPointIconSizeDynamic(80);
		Document document = XmlTestUtils.getDocument(multipartDto);
		googleEarthHandler = new GoogleEarthHandler(new KmlUtils(document, new XmlDomUtils(document)));

		//WHEN
		Document processedDocument = googleEarthHandler.processKml(document, multipartDto);
		
		String resultingKml = kmlHandler.writeTransformedDocument(processedDocument, true);
//		System.out.println(resultingKml);
		
		//THEN
		//Only single <StyleMap/> is presented
		assertTrue(XmlTestUtils
			.containsParentsWithChildren(processedDocument, "StyleMap", 1, "Pair", null));
		//Only two <Style/>'s are presented
		assertTrue(XmlTestUtils
			.containsParentsWithChildren(processedDocument, "Style", 2, "IconStyle", null));
	}
	
	/**
	 * DO NOT DELETE!
	 * May be used for complicated examination
	 */
	@Disabled
	@Test
	public void with_Dynamic_Parameters_Complicated_Test()
		throws IOException, SAXException, ParserConfigurationException, TransformerException {
		//GIVEN The googleKml with 2 <StyleMap>'s and 4 <Style>'s
		String googleKml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:kml=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" +
			"\t<Document>\n" +
			"\t\t<name>Google Earth Test Poi</name>\n" +
			"\t\t<StyleMap id=\"styleMap1\">\n" +
			"\t\t\t<Pair>\n" +
			"\t\t\t\t<key>normal</key>\n" +
			"\t\t\t\t<styleUrl>#style1</styleUrl>\n" +
			"\t\t\t</Pair>\n" +
			"\t\t\t<Pair>\n" +
			"\t\t\t\t<key>highlight</key>\n" +
			"\t\t\t\t<styleUrl>#style3</styleUrl>\n" +
			"\t\t\t</Pair>\n" +
			"\t\t</StyleMap>\n" +
			"\t\t<StyleMap id=\"styleMap2\">\n" +
			"\t\t\t<Pair>\n" +
			"\t\t\t\t<key>normal</key>\n" +
			"\t\t\t\t<styleUrl>#style2</styleUrl>\n" +
			"\t\t\t</Pair>\n" +
			"\t\t\t<Pair>\n" +
			"\t\t\t\t<key>highlight</key>\n" +
			"\t\t\t\t<styleUrl>#style3</styleUrl>\n" +
			"\t\t\t</Pair>\n" +
			"\t\t</StyleMap>\n" +
			"\t\t<Style id=\"style1\">\n" +
			"\t\t\t<IconStyle>\n" +
			"\t\t\t\t<Icon/>\n" +
			"\t\t\t</IconStyle>\n" +
			"\t\t</Style>\n" +
			"\t\t<Style id=\"style2\">\n" +
			"\t\t\t<IconStyle>\n" +
			"\t\t\t\t<Icon/>\n" +
			"\t\t\t</IconStyle>\n" +
			"\t\t</Style>\n" +
			"\t\t<Style id=\"style3\">\n" +
			"\t\t\t<IconStyle>\n" +
			"\t\t\t\t<Icon/>\n" +
			"\t\t\t</IconStyle>\n" +
			"\t\t\t<LabelStyle>\n" +
			"\t\t\t\t<scale>0.8</scale>\n" +
			"\t\t\t</LabelStyle>\n" +
			"\t\t</Style>\n" +
			"\t\t<Style id=\"style4\">\n" +
			"\t\t\t<IconStyle>\n" +
			"\t\t\t\t<Icon/>\n" +
			"\t\t\t</IconStyle>\n" +
			"\t\t</Style>\n" +
			"\t\t<Placemark>\n" +
			"\t\t\t<name>Test Placemark 1</name>\n" +
			"\t\t\t<styleUrl>#styleMap1</styleUrl>\n" +
			"\t\t\t<Point/>\n" +
			"\t\t</Placemark>\n" +
			"\t\t<Placemark>\n" +
			"\t\t\t<name>Test Placemark 2</name>\n" +
			"\t\t\t<styleUrl>#styleMap2</styleUrl>\n" +
			"\t\t\t<Point/>\n" +
			"\t\t</Placemark>\n" +
			"\t\t<Placemark>\n" +
			"\t\t\t<name>Test Placemark 3</name>\n" +
			"\t\t\t<styleUrl>#style1</styleUrl>\n" +
			"\t\t\t<Point/>\n" +
			"\t\t</Placemark>\n" +
			"\t\t<Placemark>\n" +
			"\t\t\t<name>Test Placemark 4</name>\n" +
			"\t\t\t<styleUrl>#style4</styleUrl>\n" +
			"\t\t\t<Point/>\n" +
			"\t\t</Placemark>\n" +
			"\t</Document>\n" +
			"</kml>";
		multipartFile = new MockMultipartFile("GoogleEarth.kml", googleKml.getBytes(StandardCharsets.UTF_8));
		multipartDto = new MultipartDto(multipartFile);
		multipartDto.setPointIconSizeDynamic(50);
		Document document = XmlTestUtils.getDocument(multipartDto);
		googleEarthHandler = new GoogleEarthHandler(new KmlUtils(document, new XmlDomUtils(document)));

		//WHEN set any dynamic
		Document processedDocument = googleEarthHandler.processKml(document, multipartDto);
		
		String resultingKml = kmlHandler.writeTransformedDocument(processedDocument, true);
		System.out.println(resultingKml);
		
		//THEN
		// 2 additional <StyleMap>'s for <Placemark> 3 and 4 have to be created (4 <StyleMap>'s in total)
		assertTrue(
			XmlTestUtils.containsParentsWithChildren(
				processedDocument, "StyleMap", 4, "Pair", null));
		// 2 additional <Style>'s for <Placemark> 3 and 4 have to be created as "highlight" styles (6 <Style> in total>
		assertTrue(
			XmlTestUtils.containsParentsWithChildren(
				processedDocument, "Style", 6, "IconStyle", null));
		// <Placemark>'s 3 and 4 no more references to <Style>'s
		assertFalse(
			XmlTestUtils.containsParentsWithChildren(
				processedDocument, "Placemark", 4, "styleUrl", "#style1"));
		assertFalse(
			XmlTestUtils.containsParentsWithChildren(
				processedDocument, "Placemark", 4, "styleUrl", "#style4"));
		
	}
	
}
