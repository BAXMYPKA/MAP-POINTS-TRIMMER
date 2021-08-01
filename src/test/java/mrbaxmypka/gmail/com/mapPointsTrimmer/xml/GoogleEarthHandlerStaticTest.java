package mrbaxmypka.gmail.com.mapPointsTrimmer.xml;

import mrbaxmypka.gmail.com.mapPointsTrimmer.entitiesDto.MultipartDto;
import mrbaxmypka.gmail.com.mapPointsTrimmer.services.FileService;
import mrbaxmypka.gmail.com.mapPointsTrimmer.utils.DownloadAs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import mrbaxmypka.gmail.com.mapPointsTrimmer.services.GoogleIconsService;
import mrbaxmypka.gmail.com.mapPointsTrimmer.utils.GoogleIconsCache;
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
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for standard static Styles
 * {@code
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
 * }
 * https://developers.google.com/kml/documentation/kmlreference
 */
class GoogleEarthHandlerStaticTest {
	
	private MultipartDto multipartDto;
	private MultipartFile multipartFile;
	private MessageSource messageSource;
	private ResourceLoader resourceLoader;
	private FileService fileService;
	private GoogleIconsCache googleIconsCache;
	private HtmlHandler htmlHandler;
	private GoogleIconsService googleIconsService;
	private Resource resource;
	private KmlHandler kmlHandler;
	private GoogleEarthHandler googleEarthHandler;
	private final String CLASSPATH_TO_DIRECTORY = "classpath:static/pictograms";
	
	
	@BeforeEach
	public void beforeEach() throws IOException {
		messageSource = Mockito.mock(MessageSource.class);
		resourceLoader = Mockito.mock(ResourceLoader.class);
		resource = Mockito.mock(Resource.class);
		Mockito.when(resourceLoader.getResource(CLASSPATH_TO_DIRECTORY)).thenReturn(resource);
		Mockito.when(resource.getInputStream()).thenReturn(new ByteArrayInputStream("Pictogram1.png".getBytes(StandardCharsets.UTF_8)));
		fileService = new FileService(messageSource, resourceLoader);
		
		htmlHandler = new HtmlHandler(fileService);
		googleIconsCache = new GoogleIconsCache();
		googleIconsService = new GoogleIconsService(googleIconsCache);
		kmlHandler = new KmlHandler(htmlHandler, googleIconsService, fileService);
	}
	
	@ParameterizedTest
	@ValueSource(ints = {12, 57, 111})
	public void percentage_PointIconSize_Should_Update_Existing_Scales_Into_Existing_IconStyles(int percentageSize)
		  throws IOException, ParserConfigurationException, SAXException, TransformerException {
		//GIVEN kml with existing <IconStyle><scale/></IconStyle>
		String googleKml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
			  "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:kml=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\"><Document>\n" +
			  "\t<name>FullTestKmzExport01</name>\n" +
			  "\t<open>1</open>\n" +
			  "\t<atom:author><atom:name>Locus (Android)</atom:name></atom:author>\n" +
			  "\t<Style id=\"misc-sunny.png\">\n" +
			  "\t\t<IconStyle>\n" +
			  "\t\t\t<Icon>\n" +
			  "\t\t\t\t<href>file:////storage/emulated/0/DCIM/FullTestKmzExport01/misc-sunny.png</href>\n" +
			  "\t\t\t</Icon>\n" +
			  "\t\t\t<scale>0.5</scale>\n" +
			  "\t\t\t<hotSpot x=\"0.5\" yunits=\"fraction\" y=\"0\" xunits=\"fraction\"></hotSpot>\n" +
			  "\t\t</IconStyle>\n" +
			  "\t</Style>\n" +
			  "\t<Style id=\"sport-hiking.png\">\n" +
			  "\t\t<IconStyle>\n" +
			  "\t\t\t<Icon>\n" +
			  "\t\t\t\t<href>file:////storage/emulated/0/DCIM/FullTestKmzExport01/sport-hiking.png</href>\n" +
			  "\t\t\t</Icon>\n" +
			  "\t\t\t<hotSpot x=\"0.5\" yunits=\"fraction\" y=\"0\" xunits=\"fraction\"></hotSpot>\n" +
			  "\t\t\t<scale>0</scale>\n" +
			  "\t\t</IconStyle>\n" +
			  "\t\t<LabelStyle>\n" +
			  "\t\t\t<color>00ffffff</color>\n" +
			  "\t\t\t<scale>0</scale>\n" +
			  "\t\t</LabelStyle>\n" +
			  "\t</Style>\n" +
			  "\t<Placemark>\n" +
			  "\t\t<name>Test placemark</name>\n" +
			  "\t\t<description><![CDATA[<!-- desc_gen:start --><table width=\"100%\"></table><!-- desc_gen:end -->]]></description>\n" +
			  "\t\t<gx:TimeStamp><when>2014-11-21T00:27:31Z</when>\n" +
			  "</gx:TimeStamp>\n" +
			  "\t\t<styleUrl>#misc-sunny.png</styleUrl>\n" +
			  "\t\t<Point>\n" +
			  "\t\t\t<coordinates>37.44571,56.126503,200</coordinates>\n" +
			  "\t\t</Point>\n" +
			  "\t</Placemark>\n" +
			  "\t\t<Placemark>\n" +
			  "\t\t<name>Test placemark2</name>\n" +
			  "\t\t<styleUrl>#sport-hiking.png</styleUrl>\n" +
			  "\t</Placemark>\n" +
			  "</Document>\n" +
			  "</kml>";
		multipartFile = new MockMultipartFile("TestPoi.kml", "TestPoi.kml", null, googleKml.getBytes(StandardCharsets.UTF_8));
		multipartDto = new MultipartDto(multipartFile);
		multipartDto.setPointIconSize(percentageSize);
		Document document = XmlTestUtils.getDocument(multipartDto);
		googleEarthHandler = new GoogleEarthHandler(new KmlUtils(document, new XmlDomUtils(document)));
		
		//WHEN
		document = googleEarthHandler.processKml(document, multipartDto);
		String processedKml = kmlHandler.writeTransformedDocument(document, true);
//		System.out.println(processedKml);
		//THEN
		String scale = multipartDto.getPointIconSizeScaled().toString();
		
		assertTrue(XmlTestUtils.containsTagsWithChildren(document, "IconStyle", 2, "scale", scale));
	}
	
	@ParameterizedTest
	@ValueSource(ints = {7, 21, 199})
	public void percentage_PointIconSize_Should_Create_Scales_Into_Existing_IconStyles(int percentageSize)
		  throws IOException, ParserConfigurationException, SAXException {
		//GIVEN with existing <IconStyle>s but without <scale>s into them
		String googleKml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
			  "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:kml=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\"><Document>\n" +
			  "\t<name>FullTestKmzExport01</name>\n" +
			  "\t<open>1</open>\n" +
			  "\t<atom:author><atom:name>Locus (Android)</atom:name></atom:author>\n" +
			  "\t<Style id=\"misc-sunny.png\">\n" +
			  "\t\t<IconStyle>\n" +
			  "\t\t\t<Icon>\n" +
			  "\t\t\t\t<href>file:////storage/emulated/0/DCIM/FullTestKmzExport01/misc-sunny.png</href>\n" +
			  "\t\t\t</Icon>\n" +
			  "\t\t\t<hotSpot x=\"0.5\" yunits=\"fraction\" y=\"0\" xunits=\"fraction\"></hotSpot>\n" +
			  "\t\t</IconStyle>\n" +
			  "\t</Style>\n" +
			  "\t<Style id=\"sport-hiking.png\">\n" +
			  "\t\t<IconStyle>\n" +
			  "\t\t\t<Icon>\n" +
			  "\t\t\t\t<href>file:////storage/emulated/0/DCIM/FullTestKmzExport01/sport-hiking.png</href>\n" +
			  "\t\t\t</Icon>\n" +
			  "\t\t\t<hotSpot x=\"0.5\" yunits=\"fraction\" y=\"0\" xunits=\"fraction\"></hotSpot>\n" +
			  "\t\t</IconStyle>\n" +
			  "\t</Style>\n" +
			  "\t<Placemark>\n" +
			  "\t\t<name>Test placemark</name>\n" +
			  "\t\t<styleUrl>#misc-sunny.png</styleUrl>\n" +
			  "\t\t<Point>\n" +
			  "\t\t</Point>\n" +
			  "\t</Placemark>\n" +
			  "\t\t<Placemark>\n" +
			  "\t\t<name>Test placemark</name>\n" +
			  "\t\t<styleUrl>#sport-hiking.png</styleUrl>\n" +
			  "\t\t<Point>\n" +
			  "\t\t</Point>\n" +
			  "\t</Placemark>\n" +
			  "</Document>\n" +
			  "</kml>";
		multipartFile = new MockMultipartFile("TestPoi.kml", "TestPoi.kml", null, googleKml.getBytes(StandardCharsets.UTF_8));
		multipartDto = new MultipartDto(multipartFile);
		multipartDto.setPointIconSize(percentageSize);
		Document document = XmlTestUtils.getDocument(multipartDto);
		googleEarthHandler = new GoogleEarthHandler(new KmlUtils(document, new XmlDomUtils(document)));

		//WHEN
		document = googleEarthHandler.processKml(document, multipartDto);
//		String processedKml = kmlHandler.writeTransformedDocument(document);
		
		//THEN
		String scale = multipartDto.getPointIconSizeScaled().toString();
		
		assertTrue(XmlTestUtils.containsTagsWithChildren(document, "IconStyle", 2, "scale", scale));
	}
	
	@ParameterizedTest
	@ValueSource(ints = {0, 100})
	public void pointIconOpacity_Should_Update_Only_Normal_Styles(Integer opacity)
		  throws IOException, SAXException, ParserConfigurationException, TransformerException {
		//GIVEN Only Normal Style id=style1 contains IconStyle
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
		} else if (opacity == 100) {
			iconOpacityWithColor = "ffffffff";
		}
		Document document = XmlTestUtils.getDocument(multipartDto);
		googleEarthHandler = new GoogleEarthHandler(new KmlUtils(document, new XmlDomUtils(document)));

		//WHEN
		Document processedDocument = googleEarthHandler.processKml(document, multipartDto);
		
		String resultingKml = kmlHandler.writeTransformedDocument(processedDocument, true);
//		System.out.println(resultingKml);
		
		//THEN
		//Only normal <Style id="style1"> IconStyle color should be with the <IconStyle> and the required <color> text
		assertTrue(XmlTestUtils.containsTagsWithChildren(processedDocument, "IconStyle", 1, "color", iconOpacityWithColor));
	}
	
	@ParameterizedTest
	@ValueSource(ints = {12, 57, 111})
	public void percentage_PointTextSize_Should_Insert_New_Scales_Into_Existing_LabelStyles(int percentageSize)
		  throws IOException, ParserConfigurationException, SAXException {
		//GIVEN kml with existing <LabelStyle/>s but without <scale>s inside
		String googleKml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
			  "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:kml=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\"><Document>\n" +
			  "\t<name>FullTestKmzExport01</name>\n" +
			  "\t<open>1</open>\n" +
			  "\t<atom:author><atom:name>Locus (Android)</atom:name></atom:author>\n" +
			  "\t<Style id=\"misc-sunny.png\">\n" +
			  "\t\t<LabelStyle>\n" +
			  "\t\t\t<color>00ffffff</color>\n" +
			  "\t\t</LabelStyle>\n" +
			  "\t</Style>\n" +
			  "\t<Style id=\"sport-hiking.png\">\n" +
			  "\t\t<IconStyle>\n" +
			  "\t\t\t<Icon>\n" +
			  "\t\t\t\t<href>file:////storage/emulated/0/DCIM/FullTestKmzExport01/sport-hiking.png</href>\n" +
			  "\t\t\t</Icon>\n" +
			  "\t\t\t<hotSpot x=\"0.5\" yunits=\"fraction\" y=\"0\" xunits=\"fraction\"></hotSpot>\n" +
			  "\t\t</IconStyle>\n" +
			  "\t\t<LabelStyle>\n" +
			  "\t\t\t<color>00ffffff</color>\n" +
			  "\t\t</LabelStyle>\n" +
			  "\t</Style>\n" +
			  "\t<Placemark>\n" +
			  "\t\t<name>Test placemark</name>\n" +
			  "\t\t<styleUrl>#misc-sunny.png</styleUrl>\n" +
			  "\t\t<Point>\n" +
			  "\t\t</Point>\n" +
			  "\t</Placemark>\n" +
			  "\t\t<Placemark>\n" +
			  "\t\t<name>Test placemark2</name>\n" +
			  "\t\t<styleUrl>#sport-hiking.png</styleUrl>\n" +
			  "\t\t<Point>\n" +
			  "\t\t</Point>\n" +
			  "\t</Placemark>\n" +
			  "</Document>\n" +
			  "</kml>";
		multipartFile = new MockMultipartFile("TestPoi.kml", "TestPoi.kml", null, googleKml.getBytes(StandardCharsets.UTF_8));
		multipartDto = new MultipartDto(multipartFile);
		multipartDto.setPointTextSize(percentageSize);
		Document document = XmlTestUtils.getDocument(multipartDto);
		googleEarthHandler = new GoogleEarthHandler(new KmlUtils(document, new XmlDomUtils(document)));

		//WHEN
		document = googleEarthHandler.processKml(document, multipartDto);
//		String processedKml = kmlHandler.writeTransformedDocument(document);
		
		//THEN
		String scale = multipartDto.getPointTextSizeScaled().toString();
		
		assertTrue(XmlTestUtils.containsTagsWithChildren(document, "LabelStyle", 2, "scale", scale));
	}
	
	@ParameterizedTest
	@ValueSource(ints = {0, 20, 300})
	public void percentage_PointTextSize_Should_Update_Existing_Scales_Into_Existing_LabelStyles(int percentageSize)
		  throws IOException, ParserConfigurationException, SAXException {
		//GIVEN kml with existing <LabelStyle><scale/></LabelStyle>
		String googleKml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
			  "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:kml=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\"><Document>\n" +
			  "\t<name>FullTestKmzExport01</name>\n" +
			  "\t<open>1</open>\n" +
			  "\t<atom:author><atom:name>Locus (Android)</atom:name></atom:author>\n" +
			  "\t<Style id=\"misc-sunny.png\">\n" +
			  "\t\t<IconStyle>\n" +
			  "\t\t\t<Icon>\n" +
			  "\t\t\t\t<href>file:////storage/emulated/0/DCIM/FullTestKmzExport01/misc-sunny.png</href>\n" +
			  "\t\t\t</Icon>\n" +
			  "\t\t\t<hotSpot x=\"0.5\" yunits=\"fraction\" y=\"0\" xunits=\"fraction\"></hotSpot>\n" +
			  "\t\t</IconStyle>\n" +
			  "\t\t<LabelStyle>\n" +
			  "\t\t\t<color>00ffffff</color>\n" +
			  "\t\t\t<scale>0</scale>\n" +
			  "\t\t</LabelStyle>\n" +
			  "\t</Style>\n" +
			  "\t<Style id=\"sport-hiking.png\">\n" +
			  "\t\t<IconStyle>\n" +
			  "\t\t\t<Icon>\n" +
			  "\t\t\t\t<href>file:////storage/emulated/0/DCIM/FullTestKmzExport01/sport-hiking.png</href>\n" +
			  "\t\t\t</Icon>\n" +
			  "\t\t\t<hotSpot x=\"0.5\" yunits=\"fraction\" y=\"0\" xunits=\"fraction\"></hotSpot>\n" +
			  "\t\t</IconStyle>\n" +
			  "\t\t<LabelStyle>\n" +
			  "\t\t\t<color>00ffffff</color>\n" +
			  "\t\t\t<scale>2.2</scale>\n" +
			  "\t\t</LabelStyle>\n" +
			  "\t</Style>\n" +
			  "\t<Placemark>\n" +
			  "\t\t<name>Test placemark</name>\n" +
			  "\t\t<description></description>\n" +
			  "\t\t<styleUrl>#misc-sunny.png</styleUrl>\n" +
			  "\t\t<Point>\n" +
			  "\t\t</Point>\n" +
			  "\t</Placemark>\n" +
			  "\t\t<Placemark>\n" +
			  "\t\t<name>Test placemark2</name>\n" +
			  "\t\t<description></description>\n" +
			  "\t\t<styleUrl>#sport-hiking.png</styleUrl>\n" +
			  "\t\t<Point>\n" +
			  "\t\t</Point>\n" +
			  "\t</Placemark>\n" +
			  "</Document>\n" +
			  "</kml>";
		multipartFile = new MockMultipartFile("TestPoi.kml", "TestPoi.kml", null, googleKml.getBytes(StandardCharsets.UTF_8));
		multipartDto = new MultipartDto(multipartFile);
		multipartDto.setPointTextSize(percentageSize);
		Document document = XmlTestUtils.getDocument(multipartDto);
		googleEarthHandler = new GoogleEarthHandler(new KmlUtils(document, new XmlDomUtils(document)));

		//WHEN
		document = googleEarthHandler.processKml(document, multipartDto);
//		String processedKml = kmlHandler.writeTransformedDocument(document);
		
		//THEN
		String scale = multipartDto.getPointTextSizeScaled().toString();
		
		assertTrue(XmlTestUtils.containsTagsWithChildren(document, "LabelStyle", 2, "scale", scale));
	}
	
	@ParameterizedTest
	@ValueSource(ints = {0, 20, 300})
	public void percentage_PointTextSize_Should_Create_LabelStyles_With_Scales(int percentageSize)
		  throws IOException, ParserConfigurationException, SAXException {
		//GIVEN kml without <LabelStyle>
		String googleKml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
			  "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:kml=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\"><Document>\n" +
			  "\t<name>FullTestKmzExport01</name>\n" +
			  "\t<open>1</open>\n" +
			  "\t<atom:author><atom:name>Locus (Android)</atom:name></atom:author>\n" +
			  "\t<Style id=\"misc-sunny.png\">\n" +
			  "\t\t<IconStyle>\n" +
			  "\t\t\t<Icon>\n" +
			  "\t\t\t\t<href>file:////storage/emulated/0/DCIM/FullTestKmzExport01/misc-sunny.png</href>\n" +
			  "\t\t\t</Icon>\n" +
			  "\t\t\t<hotSpot x=\"0.5\" yunits=\"fraction\" y=\"0\" xunits=\"fraction\"></hotSpot>\n" +
			  "\t\t</IconStyle>\n" +
			  "\t</Style>\n" +
			  "\t<Style id=\"sport-hiking.png\">\n" +
			  "\t\t<IconStyle>\n" +
			  "\t\t\t<Icon>\n" +
			  "\t\t\t\t<href>file:////storage/emulated/0/DCIM/FullTestKmzExport01/sport-hiking.png</href>\n" +
			  "\t\t\t</Icon>\n" +
			  "\t\t\t<hotSpot x=\"0.5\" yunits=\"fraction\" y=\"0\" xunits=\"fraction\"></hotSpot>\n" +
			  "\t\t</IconStyle>\n" +
			  "\t</Style>\n" +
			  "\t<Placemark>\n" +
			  "\t\t<name>Test placemark</name>\n" +
			  "\t\t<description></description>\n" +
			  "\t\t<gx:TimeStamp></gx:TimeStamp>\n" +
			  "\t\t<styleUrl>#misc-sunny.png</styleUrl>\n" +
			  "\t\t<Point></Point>\n" +
			  "\t</Placemark>\n" +
			  "\t\t<Placemark>\n" +
			  "\t\t<name>Test placemark</name>\n" +
			  "\t\t<description></description>\n" +
			  "\t\t<gx:TimeStamp></gx:TimeStamp>\n" +
			  "\t\t<styleUrl>#sport-hiking.png</styleUrl>\n" +
			  "\t\t<Point></Point>\n" +
			  "\t</Placemark>\n" +
			  "</Document>\n" +
			  "</kml>";
		multipartFile = new MockMultipartFile("TestPoi.kml", "TestPoi.kml", null, googleKml.getBytes(StandardCharsets.UTF_8));
		multipartDto = new MultipartDto(multipartFile);
		multipartDto.setPointTextSize(percentageSize);
		Document document = XmlTestUtils.getDocument(multipartDto);
		googleEarthHandler = new GoogleEarthHandler(new KmlUtils(document, new XmlDomUtils(document)));

		//WHEN
		document = googleEarthHandler.processKml(document, multipartDto);
//		String processedKml = kmlHandler.writeTransformedDocument(document);
		
		//THEN
		String scale = multipartDto.getPointTextSizeScaled().toString();
		
		assertTrue(XmlTestUtils.containsTagsWithChildren(document, "LabelStyle", 2, "scale", scale));
	}
	
	
	/**
	 * Typical RGB incoming from HTML color picker list:
	 * HEX COLOR : #ff0000
	 * HEX COLOR : #000000
	 * HEX COLOR : #ffffff
	 * HEX COLOR : #8e4848
	 * <p>
	 * Typical ARGB {@literal <color/>} from KML:
	 * : 7fffaaff
	 * : a1ff00ff
	 * </p>
	 * * https://developers.google.com/kml/documentation/kmlreference#colorstyle
	 * * Color and opacity (alpha) values are expressed in hexadecimal notation.
	 * * The range of values for any one color is 0 to 255 (00 to ff).
	 * * For alpha, 00 is fully transparent and ff is fully opaque.
	 * * The order of expression is aabbggrr,
	 * * where aa=alpha (00 to ff); bb=blue (00 to ff); gg=green (00 to ff); rr=red (00 to ff).
	 * * For example, if you want to apply a blue color with 50 percent opacity to an overlay, you would specify the following:
	 * * {@literal <color>7fff0000</color>}, where alpha=0x7f, blue=0xff, green=0x00, and red=0x00.
	 */
	@ParameterizedTest
	@ValueSource(strings = {"#000000", "#ff0000", "#ffffff", "#374b5c"})
	public void pointTextColors_Should_Create_LabelStyles_With_Colors(String hexColor)
		  throws IOException, ParserConfigurationException, SAXException {
		//GIVEN kml without <LabelStyle/>s and <color/>s in them
		String googleKml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
			  "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:kml=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\"><Document>\n" +
			  "\t<name>FullTestKmzExport01</name>\n" +
			  "\t<open>1</open>\n" +
			  "\t<atom:author><atom:name>Locus (Android)</atom:name></atom:author>\n" +
			  "\t<Style id=\"misc-sunny.png\">\n" +
			  "\t\t<IconStyle>\n" +
			  "\t\t\t<Icon>\n" +
			  "\t\t\t\t<href>file:////storage/emulated/0/DCIM/FullTestKmzExport01/misc-sunny.png</href>\n" +
			  "\t\t\t</Icon>\n" +
			  "\t\t\t<hotSpot x=\"0.5\" yunits=\"fraction\" y=\"0\" xunits=\"fraction\"></hotSpot>\n" +
			  "\t\t</IconStyle>\n" +
			  "\t</Style>\n" +
			  "\t<Style id=\"sport-hiking.png\">\n" +
			  "\t\t<IconStyle>\n" +
			  "\t\t\t<Icon>\n" +
			  "\t\t\t\t<href>file:////storage/emulated/0/DCIM/FullTestKmzExport01/sport-hiking.png</href>\n" +
			  "\t\t\t</Icon>\n" +
			  "\t\t\t<hotSpot x=\"0.5\" yunits=\"fraction\" y=\"0\" xunits=\"fraction\"></hotSpot>\n" +
			  "\t\t</IconStyle>\n" +
			  "\t</Style>\n" +
			  "\t<Placemark>\n" +
			  "\t\t<name>Test placemark</name>\n" +
			  "\t\t<description></description>\n" +
			  "\t\t<gx:TimeStamp></gx:TimeStamp>\n" +
			  "\t\t<styleUrl>#misc-sunny.png</styleUrl>\n" +
			  "\t\t<Point></Point>\n" +
			  "\t</Placemark>\n" +
			  "</Document>\n" +
			  "\t<Placemark>\n" +
			  "\t\t<name>Test placemark2</name>\n" +
			  "\t\t<description></description>\n" +
			  "\t\t<gx:TimeStamp></gx:TimeStamp>\n" +
			  "\t\t<styleUrl>#sport-hiking.png</styleUrl>\n" +
			  "\t\t<Point></Point>\n" +
			  "\t</Placemark>\n" +
			  "</kml>";
		multipartFile = new MockMultipartFile("TestPoi.kml", "TestPoi.kml", null, googleKml.getBytes(StandardCharsets.UTF_8));
		multipartDto = new MultipartDto(multipartFile);
		multipartDto.setPointTextHexColor(hexColor);
		Document document = XmlTestUtils.getDocument(multipartDto);
		googleEarthHandler = new GoogleEarthHandler(new KmlUtils(document, new XmlDomUtils(document)));

		//WHEN
		document = googleEarthHandler.processKml(document, multipartDto);
//		String processedKml = kmlHandler.writeTransformedDocument(document);
//		System.out.println(processedKml);
		
		//THEN
		String kmlColor = googleEarthHandler.getKmlColor(multipartDto.getPointTextHexColor(), multipartDto.getPointTextOpacity());
//		System.out.println("HEX COLOR : " + hexColor);
//		System.out.println("KML COLOR : " + kmlColor);
		
		assertTrue(XmlTestUtils.containsTagsWithChildren(document, "LabelStyle", 2, "color", kmlColor));
	}
	
	/**
	 * Typical RGB incoming from HTML color picker list:
	 * HEX COLOR : #ff0000
	 * HEX COLOR : #000000
	 * HEX COLOR : #ffffff
	 * HEX COLOR : #8e4848
	 * <p>
	 * Typical ARGB {@literal <color/>} from KML:
	 * : 7fffaaff
	 * : a1ff00ff
	 * <p>
	 * * https://developers.google.com/kml/documentation/kmlreference#colorstyle
	 * * Color and opacity (alpha) values are expressed in hexadecimal notation.
	 * * The range of values for any one color is 0 to 255 (00 to ff).
	 * * For alpha, 00 is fully transparent and ff is fully opaque.
	 * * The order of expression is aabbggrr,
	 * * where aa=alpha (00 to ff); bb=blue (00 to ff); gg=green (00 to ff); rr=red (00 to ff).
	 * * For example, if you want to apply a blue color with 50 percent opacity to an overlay, you would specify the following:
	 * * {@literal <color>7fff0000</color>}, where alpha=0x7f, blue=0xff, green=0x00, and red=0x00.
	 */
	@ParameterizedTest
	@ValueSource(strings = {"#000088", "#ff0000", "#ffffff", "#374b5c"})
	public void pointTextColors_Should_Update_LabelStyles_With_Colors(String hexColor)
		  throws IOException, ParserConfigurationException, SAXException {
		//GIVEN kml with <LabelStyle/>s with AND without <color/>s in them
		String googleKml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:kml=\"http://www.opengis.net/kml/2.2\"><Document>\n" +
			  "\t<name>FullTestKmzExport01</name>\n" +
			  "\t<open>1</open>\n" +
			  "\t<atom:author><atom:name>Locus (Android)</atom:name></atom:author>\n" +
			  "\t<Style id=\"misc-sunny.png\">\n" +
			  "\t\t<IconStyle>\n" +
			  "\t\t\t<Icon>\n" +
			  "\t\t\t\t<href>file:////storage/emulated/0/DCIM/FullTestKmzExport01/misc-sunny.png</href>\n" +
			  "\t\t\t</Icon>\n" +
			  "\t\t\t<hotSpot x=\"0.5\" xunits=\"fraction\" y=\"0\" yunits=\"fraction\"/>\n" +
			  "\t\t</IconStyle>\n" +
			  "\t\t<LabelStyle>\n" +
			  "\t\t\t<color>ff990000</color>\n" +
			  "\t\t</LabelStyle>\n" +
			  "\t</Style>\n" +
			  "\t<Style id=\"sport-hiking.png\">\n" +
			  "\t\t<IconStyle>\n" +
			  "\t\t\t<Icon>\n" +
			  "\t\t\t\t<href>file:////storage/emulated/0/DCIM/FullTestKmzExport01/sport-hiking.png</href>\n" +
			  "\t\t\t</Icon>\n" +
			  "\t\t\t<hotSpot x=\"0.5\" xunits=\"fraction\" y=\"0\" yunits=\"fraction\"/>\n" +
			  "\t\t</IconStyle>\n" +
			  "\t\t<LabelStyle>\n" +
			  "\t\t</LabelStyle>\n" +
			  "\t</Style>\n" +
			  "\t<Placemark>\n" +
			  "\t\t<name>Test placemark</name>\n" +
			  "\t\t<description></description>\n" +
			  "\t\t<gx:TimeStamp><when>2014-11-21T00:27:31Z</when></gx:TimeStamp>\n" +
			  "\t\t<styleUrl>#misc-sunny.png</styleUrl>\n" +
			  "\t\t<Point></Point>\n" +
			  "\t</Placemark>\n" +
			  "\t\t<Placemark>\n" +
			  "\t\t<name>Test placemark2</name>\n" +
			  "\t\t<description></description>\n" +
			  "\t\t<gx:TimeStamp><when>2014-11-21T00:27:31Z</when></gx:TimeStamp>\n" +
			  "\t\t<styleUrl>#sport-hiking.png</styleUrl>\n" +
			  "\t\t<Point></Point>\n" +
			  "\t</Placemark>\n" +
			  "</Document>\n" +
			  "</kml>";
		multipartFile = new MockMultipartFile("TestPoi.kml", "TestPoi.kml", null, googleKml.getBytes(StandardCharsets.UTF_8));
		multipartDto = new MultipartDto(multipartFile);
		multipartDto.setPointTextHexColor(hexColor);
		Document document = XmlTestUtils.getDocument(multipartDto);
		googleEarthHandler = new GoogleEarthHandler(new KmlUtils(document, new XmlDomUtils(document)));

		//WHEN
		document = googleEarthHandler.processKml(document, multipartDto);
//		String processedKml = kmlHandler.writeTransformedDocument(document);
//		System.out.println(processedKml);
		
		//THEN
		String kmlColor = googleEarthHandler.getKmlColor(multipartDto.getPointTextHexColor(), multipartDto.getPointTextOpacity());
//		System.out.println("HEX COLOR : " + hexColor);
//		System.out.println("KML COLOR : " + kmlColor);
		
		assertTrue(XmlTestUtils.containsTagsWithChildren(document, "LabelStyle", 2, "color", kmlColor));
	}
	
	
	/**
	 * Typical RGB incoming from HTML color picker list:
	 * HEX COLOR : #ff0000
	 * HEX COLOR : #000000
	 * HEX COLOR : #ffffff
	 * HEX COLOR : #8e4848
	 * <p>
	 * Typical ARGB {@literal <color/>} from KML:
	 * : 7fffaaff
	 * : a1ff00ff
	 * <p>
	 * * https://developers.google.com/kml/documentation/kmlreference#colorstyle
	 * * Color and opacity (alpha) values are expressed in hexadecimal notation.
	 * * The range of values for any one color is 0 to 255 (00 to ff).
	 * * For alpha, 00 is fully transparent and ff is fully opaque.
	 * * The order of expression is aabbggrr,
	 * * where aa=alpha (00 to ff); bb=blue (00 to ff); gg=green (00 to ff); rr=red (00 to ff).
	 * * For example, if you want to apply a blue color with 50 percent opacity to an overlay, you would specify the following:
	 * * {@literal <color>7fff0000</color>}, where alpha=0x7f, blue=0xff, green=0x00, and red=0x00.
	 */
	@ParameterizedTest
	@ValueSource(strings = {"#112233", "#ff10ab", "#affbfc", "#374b5c"})
	public void incoming_Hex_Input_as_RRGGBB_Colors_Should_Be_Converted_To_Kml_Colors_as_AABBGGRR(String hexColor)
			throws IOException, SAXException, ParserConfigurationException {
		//GIVEN input hex colors from HTML color picker as #rrggbb
		multipartDto = new MultipartDto(new MockMultipartFile("name", new byte[]{}));
		Document document = XmlTestUtils.getMockDocument();
		googleEarthHandler = new GoogleEarthHandler(new KmlUtils(document, new XmlDomUtils(document)));

		//WHEN
		String kmlColor = googleEarthHandler.getKmlColor(hexColor, multipartDto.getPointTextOpacity());
		
		//THEN
//		System.out.println("HEX COLOR : " + hexColor);
//		System.out.println("KML COLOR : " + kmlColor);
		
		assertTrue(kmlColor.contains("ff332211") || kmlColor.contains("ffab10ff") ||
			  kmlColor.contains("fffcfbaf") || kmlColor.contains("ff5c4b37"));
	}
	
	@ParameterizedTest
	@ValueSource(strings = {"#1112233", "#ff10abc", "affbfc", "#374g5c"})
	public void incoming_Incorrect_Hex_Input_Colors_Should_Throw_IllegalArgException(String hexColor)
			throws IOException, SAXException, ParserConfigurationException {
		//GIVEN input hex colors from HTML color picker as #rrggbb
		multipartDto = new MultipartDto(new MockMultipartFile("name", new byte[]{}));
		Document document = XmlTestUtils.getMockDocument();
		googleEarthHandler = new GoogleEarthHandler(new KmlUtils(document, new XmlDomUtils(document)));

		//WHEN
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
			  () -> googleEarthHandler.getKmlColor(hexColor, multipartDto.getPointTextOpacity()));
		
		//THEN
		assertEquals("Color value is not correct! (It has to correspond to '#rrggbb' hex pattern", exception.getMessage());
	}
	
	/**
	 * Convert incoming percentage value 0 - 100% to an integer in base sixteen 00 - FF (0 - 255).
	 * Where 100% = 255 and 1% = 2.55 (Rounding.HALF_UP) accordingly but as two hex digits (e.g.  00, 03, 7F, FF)
	 */
	@ParameterizedTest
	@ValueSource(ints = {0, 1, 2, 50, 73, 99, 100})
	public void percentage_Conversion_Into_Hex_Should_Be_Correct(Integer percent) throws IOException, SAXException, ParserConfigurationException {
		//GIVEN
		// 100% = 255(hex)
		// 1% = 2.55(Rounded.HALF_UP) as two hex digits (e.g. 00, 03, 7F, FF)
		// WHERE: 0 = 00, 1 = 03, 2 = 05, 50 = 127, 99 = FC, 100 = FF
		multipartDto = new MultipartDto(new MockMultipartFile("name", new byte[]{}));
		Document document = XmlTestUtils.getMockDocument();
		googleEarthHandler = new GoogleEarthHandler(new KmlUtils(document, new XmlDomUtils(document)));

		
/*
		BigDecimal hex = new BigDecimal(percent * 2.55).setScale(0, RoundingMode.HALF_UP);
		String hexFormat = String.format("%02x", hex.toBigInteger());
		
		System.out.printf("\nPERCENT : %s", percent);
		System.out.printf("\nHEX PERCENT : %s", hex);
		System.out.printf("\nHEX : %s", hexFormat);
*/
		
		//WHEN
		String hexRepresentation = googleEarthHandler.getHexFromPercentage(percent);
		
		//THEN
		assertTrue(hexRepresentation.equalsIgnoreCase("00") || hexRepresentation.equalsIgnoreCase("03") ||
			  hexRepresentation.equalsIgnoreCase("05") || hexRepresentation.equalsIgnoreCase("7F") ||
			  hexRepresentation.equalsIgnoreCase("BA") || hexRepresentation.equalsIgnoreCase("FC") ||
			  hexRepresentation.equalsIgnoreCase("FF"));
	}
	
	/**
	 * Color and opacity (alpha) values are expressed in hexadecimal notation.
	 * The range of values for any one color is 0 to 255 (00 to ff).
	 * For alpha, 00 is fully transparent and ff is fully opaque.
	 * The order of expression is aabbggrr, where aa=alpha (00 to ff); bb=blue (00 to ff); gg=green (00 to ff); rr=red (00 to ff).
	 * For example, if you want to apply a blue color with 50 percent opacity to an overlay,
	 * you would specify the following: {@literal <color>7fff0000</color>}, where alpha=0x7f, blue=0xff, green=0x00,
	 * and red=0x00.
	 */
	@ParameterizedTest
	@ValueSource(strings = {"#000088", "#ff0000", "#ffffff", "#374b5c"})
	public void pointTextColors_Without_setTextOpacity_Should_Starts_With_Max_Opacity_Value_FF(String hexColor) throws IOException, SAXException, ParserConfigurationException {
		//GIVEN
		multipartFile = new MockMultipartFile("TestPoi.kml", new byte[]{});
		multipartDto = new MultipartDto(multipartFile);
		multipartDto.setPointTextHexColor(hexColor);
		Document document = XmlTestUtils.getMockDocument();
		googleEarthHandler = new GoogleEarthHandler(new KmlUtils(document, new XmlDomUtils(document)));

		//WHEN
		String kmlColorWithOpacity = googleEarthHandler.getKmlColor(multipartDto.getPointTextHexColor(), (Integer) null);
		
		//THEN
		assertTrue(kmlColorWithOpacity.startsWith("ff"));
	}
	
	/**
	 * Color and opacity (alpha) values are expressed in hexadecimal notation.
	 * The range of values for any one color is 0 to 255 (00 to ff).
	 * For alpha, 00 is fully transparent and ff is fully opaque.
	 * The order of expression is aabbggrr, where aa=alpha (00 to ff); bb=blue (00 to ff); gg=green (00 to ff); rr=red (00 to ff).
	 * For example, if you want to apply a blue color with 50 percent opacity to an overlay,
	 * you would specify the following: {@literal <color>7fff0000</color>}, where alpha=0x7f, blue=0xff, green=0x00,
	 * and red=0x00.
	 */
	@ParameterizedTest
	@ValueSource(strings = {"#000088", "#ff0000", "#ffffff", "#374b5c"})
	public void pointTextColors_With_setTextOpacity_Should_Starts_With_Opacity_Value_But_Not_Default_FF(String hexColor) throws IOException, SAXException, ParserConfigurationException {
		//GIVEN
		multipartFile = new MockMultipartFile("TestPoi.kml", new byte[]{});
		multipartDto = new MultipartDto(multipartFile);
		multipartDto.setPointTextHexColor(hexColor);
		multipartDto.setPointTextOpacity((int) (Math.random() * 100 + 1));
		Document document = XmlTestUtils.getMockDocument();
		googleEarthHandler = new GoogleEarthHandler(new KmlUtils(document, new XmlDomUtils(document)));

		//WHEN
		String kmlColorWithOpacity = googleEarthHandler.getKmlColor(multipartDto.getPointTextHexColor(), multipartDto.getPointTextOpacity());
		String separateHexOpacityValue = googleEarthHandler.getHexFromPercentage(multipartDto.getPointTextOpacity());
		
		//THEN
		assertFalse(kmlColorWithOpacity.startsWith("ff"));
		assertTrue(kmlColorWithOpacity.startsWith(separateHexOpacityValue));
	}
	
	@ParameterizedTest
	@ValueSource(strings = {"iconSize", "textSize", "textColor"})
	public void static_Sets_Should_Update_Only_Normal_Style_From_Style_Map(String staticType)
		  throws IOException, ParserConfigurationException, SAXException, TransformerException {
		//GIVEN <StyleMap>'s and <Style>'s with all the <scale>0.8</scale>
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
			  "\t\t\t<styleUrl>#style3</styleUrl>\n" +
			  "\t\t</Pair>\n" +
			  "\t</StyleMap>\n" +
			  "\t<StyleMap id=\"styleMap2\">\n" +
			  "\t\t<Pair>\n" +
			  "\t\t\t<key>normal</key>\n" +
			  "\t\t\t<styleUrl>#style2</styleUrl>\n" +
			  "\t\t</Pair>\n" +
			  "\t\t<Pair>\n" +
			  "\t\t\t<key>highlight</key>\n" +
			  "\t\t\t<styleUrl>#style3</styleUrl>\n" +
			  "\t\t</Pair>\n" +
			  "\t</StyleMap>\n" +
			  "\t<Style id=\"style1\">\n" +
			  "\t\t<IconStyle>\n" +
			  "\t\t\t<scale>0.8</scale>\n" +
			  "\t\t</IconStyle>\n" +
			  "\t\t<LabelStyle>\n" +
			  "\t\t\t<scale>0.8</scale>\n" +
			  "\t\t\t<color>00000000</color>\n" +
			  "\t\t</LabelStyle>\n" +
			  "\t</Style>\n" +
			  "\t<Style id=\"style2\">\n" +
			  "\t\t<IconStyle>\n" +
			  "\t\t\t<scale>0.8</scale>\n" +
			  "\t\t</IconStyle>\n" +
			  "\t\t<LabelStyle>\n" +
			  "\t\t\t<scale>0.8</scale>\n" +
			  "\t\t\t<color>00000000</color>\n" +
			  "\t\t</LabelStyle>\n" +
			  "\t</Style>\n" +
			  "\t<Style id=\"style3\">\n" +
			  "\t\t<IconStyle>\n" +
			  "\t\t\t<scale>0.8</scale>\n" +
			  "\t\t</IconStyle>\n" +
			  "\t\t<LabelStyle>\n" +
			  "\t\t\t<scale>0.8</scale>\n" +
			  "\t\t\t<color>00000000</color>\n" +
			  "\t\t</LabelStyle>\n" +
			  "\t</Style>\n" +
			  "\t<Style id=\"style4\">\n" +
			  "\t\t<IconStyle>\n" +
			  "\t\t\t<scale>0.8</scale>\n" +
			  "\t\t</IconStyle>\n" +
			  "\t\t<LabelStyle>\n" +
			  "\t\t\t<scale>0.8</scale>\n" +
			  "\t\t\t<color>00000000</color>\n" +
			  "\t\t</LabelStyle>\n" +
			  "\t</Style>\n" +
			  "\t<Style id=\"style5\">\n" +
			  "\t\t<IconStyle>\n" +
			  "\t\t\t<scale>0.8</scale>\n" +
			  "\t\t</IconStyle>\n" +
			  "\t\t<LabelStyle>\n" +
			  "\t\t\t<scale>0.8</scale>\n" +
			  "\t\t\t<color>00000000</color>\n" +
			  "\t\t</LabelStyle>\n" +
			  "\t</Style>\n" +
			  "\t<Placemark>\n" +
			  "\t\t\t<name>Test Placemark 1</name>\n" +
			  "\t\t\t<styleUrl>#styleMap1</styleUrl>\n" +
			  "\t\t\t<Point></Point>\n" +
			  "\t</Placemark>\n" +
			  "\t<Placemark>\n" +
			  "\t\t\t<name>Test Placemark 2</name>\n" +
			  "\t\t\t<styleUrl>#styleMap2</styleUrl>\n" +
			  "\t\t\t<Point></Point>\n" +
			  "\t</Placemark>\n" +
			  "\t<Placemark>\n" +
			  "\t\t\t<name>Test Placemark 3</name>\n" +
			  "\t\t\t<styleUrl>#style4</styleUrl>\n" +
			  "\t\t\t<Point></Point>\n" +
			  "\t</Placemark>\n" +
			  "</Document>\n" +
			  "</kml>";
		multipartFile = new MockMultipartFile("TestPoi.kml", "TestPoi.kml", null, googleKml.getBytes(StandardCharsets.UTF_8));
		multipartDto = new MultipartDto(multipartFile);
		
		Integer textAndIconSize = 50;
		String htmlPointsTextColor = "#ffffff";
		
		if (staticType.equals("iconSize")) {
			multipartDto.setPointIconSize(textAndIconSize);
		} else if (staticType.equals("textSize")) {
			multipartDto.setPointTextSize(textAndIconSize);
		} else if (staticType.equals("textColor")) {
			multipartDto.setPointTextHexColor(htmlPointsTextColor);
		}
		Document document = XmlTestUtils.getDocument(multipartDto);
		googleEarthHandler = new GoogleEarthHandler(new KmlUtils(document, new XmlDomUtils(document)));

		//WHEN
		document = googleEarthHandler.processKml(document, multipartDto);
		String processedKml = kmlHandler.writeTransformedDocument(document, true);
//		System.out.println(processedKml);
		
		//THEN
		//Only the "normal" #style1, #style2 from <StyleMap>'s and the referenced from <Placemark> #style4 should be changed (3 pieces)
		//But "highlighted" #style3 from <StyleMap> and unused #style4 should be unchanged (2 pieces)
		if (staticType.equals("iconSize")) {
			String pointIconScale = multipartDto.getPointIconSizeScaled().toString();
			assertTrue(XmlTestUtils.containsTagsWithChildren(document, "IconStyle", 3, "scale", pointIconScale));
			assertTrue(XmlTestUtils.containsTagsWithChildren(document, "IconStyle", 2, "scale", "0.8"));
		} else if (staticType.equals("textSize")) {
			String pointsTextScale = multipartDto.getPointTextSizeScaled().toString();
			assertTrue(XmlTestUtils.containsTagsWithChildren(document, "LabelStyle", 3, "scale", pointsTextScale));
			assertTrue(XmlTestUtils.containsTagsWithChildren(document, "LabelStyle", 2, "scale", "0.8"));
		} else if (staticType.equals("textColor")) {
			String kmlColor = googleEarthHandler.getKmlColor(htmlPointsTextColor, multipartDto.getPointTextOpacity());
			assertTrue(XmlTestUtils.containsTagsWithChildren(document, "LabelStyle", 3, "color", kmlColor));
			assertTrue(XmlTestUtils.containsTagsWithChildren(document, "LabelStyle", 2, "color", "00000000"));
		}
		
	}
	
	@Test
	public void with_Static_Parameters_Placemarks_StyleUrls_To_Styles_Should_Not_Be_Changed()
		  throws IOException, SAXException, ParserConfigurationException, TransformerException {
		//GIVEN just a static multipartDto.setPointTextSize(50);
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
			  "\t<StyleMap id=\"styleMap2\">\n" +
			  "\t\t<Pair>\n" +
			  "\t\t\t<key>normal</key>\n" +
			  "\t\t\t<styleUrl>#style2</styleUrl>\n" +
			  "\t\t</Pair>\n" +
			  "\t\t<Pair>\n" +
			  "\t\t\t<key>highlight</key>\n" +
			  "\t\t\t<styleUrl>#style3</styleUrl>\n" +
			  "\t\t</Pair>\n" +
			  "\t</StyleMap>\n" +
			  "\t<Style id=\"style1\">\n" +
			  "\t\t<IconStyle>\n" +
			  "\t\t\t<Icon>\n" +
			  "\t\t\t\t<href>http://maps.google.com/mapfiles/kml/shapes/poi.png</href>\n" +
			  "\t\t\t</Icon>\n" +
			  "\t\t</IconStyle>\n" +
			  "\t</Style>\n" +
			  "\t<Style id=\"style2\">\n" +
			  "\t\t<IconStyle>\n" +
			  "\t\t\t<Icon>\n" +
			  "\t\t\t\t<href>http://maps.google.com/mapfiles/kml/shapes/poi.png</href>\n" +
			  "\t\t\t</Icon>\n" +
			  "\t\t</IconStyle>\n" +
			  "\t</Style>\n" +
			  "\t\t<Style id=\"style3\">\n" +
			  "\t\t<IconStyle>\n" +
			  "\t\t\t<Icon>\n" +
			  "\t\t\t\t<href>http://maps.google.com/mapfiles/kml/shapes/earthquake.png</href>\n" +
			  "\t\t\t</Icon>\n" +
			  "\t\t</IconStyle>\n" +
			  "\t\t<LabelStyle>\n" +
			  "\t\t\t<scale>0.8</scale>\n" +
			  "\t\t</LabelStyle>\n" +
			  "\t</Style>\n" +
			  "\t\t<Placemark>\n" +
			  "\t\t\t<name>Test Placemark 1</name>\n" +
			  "\t\t\t<styleUrl>#styleMap1</styleUrl>\n" +
			  "\t\t\t<Point>\n" +
			  "\t\t\t\t<coordinates>38.547163,55.88113662000001,133</coordinates>\n" +
			  "\t\t\t</Point>\n" +
			  "\t\t</Placemark>\n" +
			  "\t\t<Placemark>\n" +
			  "\t\t\t<name>Test Placemark 2</name>\n" +
			  "\t\t\t<styleUrl>#styleMap2</styleUrl>\n" +
			  "\t\t\t<Point>\n" +
			  "\t\t\t\t<coordinates>38.54269981,55.88994587,145</coordinates>\n" +
			  "\t\t\t</Point>\n" +
			  "\t\t</Placemark>\n" +
			  "\t\t\t\t<Placemark>\n" +
			  "\t\t\t<name>Test Placemark 3</name>\n" +
			  "\t\t\t<styleUrl>#style3</styleUrl>\n" +
			  "\t\t\t<Point>\n" +
			  "\t\t\t\t<coordinates>38.5409832,55.89456632,143</coordinates>\n" +
			  "\t\t\t</Point>\n" +
			  "\t\t</Placemark>\n" +
			  "</Document>\n" +
			  "</kml>";
		multipartFile = new MockMultipartFile("GoogleEarth.kml", googleKml.getBytes(StandardCharsets.UTF_8));
		multipartDto = new MultipartDto(multipartFile);
		multipartDto.setPointTextSize(50);
		Document document = XmlTestUtils.getDocument(multipartDto);
		googleEarthHandler = new GoogleEarthHandler(new KmlUtils(document, new XmlDomUtils(document)));

		//WHEN
		Document processedDocument = googleEarthHandler.processKml(document, multipartDto);
		
		String resultingKml = kmlHandler.writeTransformedDocument(processedDocument, true);
//		System.out.println(resultingKml);
		
		//THEN
		assertTrue(resultingKml.contains("<styleUrl>#style3</styleUrl>"));
		assertTrue(XmlTestUtils.containsTagWithChild(processedDocument, "Placemark", "styleUrl", "#style3"));
	}
	
	@Disabled
	@Test
	public void complicated_Test()
		  throws IOException, SAXException, ParserConfigurationException, TransformerException {
		//GIVEN just a static multipartDto.setPointTextSize(50);
		String googleEarthKml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
			  "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" +
			  "<Document>\n" +
			  "\t<name>Locus30.06.2020</name>\n" +
			  "\t<atom:author><atom:name>Locus (Android)</atom:name></atom:author>\n" +
			  "\t\t<Style id=\"sport-cyclingsport.png\">\n" +
			  "\t\t<IconStyle>\n" +
			  "\t\t\t<Icon><href>files/sport-cyclingsport.png</href></Icon>\n" +
			  "\t\t\t<hotSpot x=\"0.5\" y=\"0.0\" xunits=\"fraction\" yunits=\"fraction\" />\n" +
			  "\t\t</IconStyle>\n" +
			  "\t</Style>\n" +
			  "<Placemark>\n" +
			  "\t<name>Катушкин</name>\n" +
			  "\t<description><![CDATA[<!-- desc_gen:start -->\n" +
			  "<font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\">\n" +
			  "<!-- desc_user:start -->\n" +
			  "<font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\"><font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\"><font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\"><font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\"><font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\"><font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\"><font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\"><font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\"><font color=\"black\"><table width=\"100%\"><tr><td><table width=\"100%\"></table></td></tr></table></font></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font>\n" +
			  "<!-- desc_user:end -->\n" +
			  "</td></tr><tr><td colspan=\"1\"><hr></td></tr><tr><td><table width=\"100%\"><tr><td align=\"left\" valign=\"center\"><small><b>Создано</b></small></td><td align=\"center\" valign=\"center\">2014-11-21 00:27:30</td></tr>\n" +
			  "</table></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font>\n" +
			  "<!-- desc_gen:end -->]]></description>\n" +
			  "\t<styleUrl>#sport-cyclingsport.png</styleUrl>\n" +
			  "\t<Point>\n" +
			  "\t\t<coordinates>37.7266760,55.8425980,0.00</coordinates>\n" +
			  "\t</Point>\n" +
			  "\t<gx:TimeStamp>\n" +
			  "\t\t<when>2014-11-21T00:27:31Z</when>\n" +
			  "\t</gx:TimeStamp>\n" +
			  "</Placemark>\n" +
			  "<Placemark>\n" +
			  "\t<name>Дорога в сторону Шеремет. шоссе</name>\n" +
			  "\t<description><![CDATA[<!-- desc_gen:start -->\n" +
			  "<font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\">\n" +
			  "<!-- desc_user:start -->\n" +
			  "<font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\"><font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\"><font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\"><font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\"><font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\"><font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\"><font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\"><font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\">2012г\n" +
			  "<font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\"><a href=\"/storage/emulated/0/Locus/data/media/photo/_1348037300055.jpg\" target=\"_blank\"><img src=\"/storage/emulated/0/Locus/data/media/photo/_1348037300055.jpg\" width=\"330px\" align=\"center\"></a>хорошая полевая</td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font>\n" +
			  "<!-- desc_user:end -->\n" +
			  "</td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font>\n" +
			  "<!-- desc_gen:end -->]]></description>\n" +
			  "\t<styleUrl>#sport-cyclingsport.png</styleUrl>\n" +
			  "\t<Point>\n" +
			  "\t\t<coordinates>37.4492730,55.9974100</coordinates>\n" +
			  "\t</Point>\n" +
			  "\t<gx:TimeStamp>\n" +
			  "\t\t<when>2014-11-21T00:27:31Z</when>\n" +
			  "\t</gx:TimeStamp>\n" +
			  "</Placemark>\n" +
			  "<Placemark>\n" +
			  "\t<name>Хорошая грунтовка на северо-запад к Сенежу</name>\n" +
			  "\t<description><![CDATA[<!-- desc_gen:start -->\n" +
			  "<font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\">\n" +
			  "<!-- desc_user:start -->\n" +
			  "<font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\"><font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\"><font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\"><font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\"><font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\"><font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\"><font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\"><font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\"><font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\"><a href=\"/storage/emulated/0/Locus/data/media/photo/_1348050337329.jpg\" target=\"_blank\"><img src=\"/storage/emulated/0/Locus/data/media/photo/_1348050337329.jpg\" width=\"330px\" align=\"center\"></a>видна на ОСМ картах</td></tr><tr><td colspan=\"1\"><hr></td></tr><tr><td><table width=\"100%\"><tr><td align=\"left\" valign=\"center\"><small><b>Высота</b></small></td><td align=\"center\" valign=\"center\">249m</td></tr>\n" +
			  "<tr><td align=\"left\" valign=\"center\"><small><b>Скорость</b></small></td><td align=\"center\" valign=\"center\">4.0km/h</td></tr>\n" +
			  "<tr><td align=\"left\" valign=\"center\"><small><b>Азимут</b></small></td><td align=\"center\" valign=\"center\">189°</td></tr>\n" +
			  "<tr><td align=\"left\" valign=\"center\"><small><b>Точность</b></small></td><td align=\"center\" valign=\"center\">10m</td></tr>\n" +
			  "<tr><td align=\"left\" valign=\"center\"><small><b>Создано</b></small></td><td align=\"center\" valign=\"center\">2012-09-21 11:01:57</td></tr>\n" +
			  "</table></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font></td></tr><tr><td colspan=\"1\"><hr></td></tr><tr><td><table width=\"100%\"><tr><td align=\"left\" valign=\"center\"><small><b>Высота</b></small></td><td align=\"center\" valign=\"center\">236 m</td></tr>\n" +
			  "<tr><td align=\"left\" valign=\"center\"><small><b>Создано</b></small></td><td align=\"center\" valign=\"center\">2014-05-10 16:33:59</td></tr>\n" +
			  "</table></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font></td></tr><tr><td colspan=\"1\"><hr></td></tr><tr><td><table width=\"100%\"><tr><td align=\"left\" valign=\"center\"><small><b>Высота</b></small></td><td align=\"center\" valign=\"center\">236 m</td></tr>\n" +
			  "<tr><td align=\"left\" valign=\"center\"><small><b>Создано</b></small></td><td align=\"center\" valign=\"center\">2014-06-03 14:39:11</td></tr>\n" +
			  "</table></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font></td></tr><tr><td colspan=\"1\"><hr></td></tr><tr><td><table width=\"100%\"><tr><td align=\"left\" valign=\"center\"><small><b>Высота</b></small></td><td align=\"center\" valign=\"center\">236 m</td></tr>\n" +
			  "<tr><td align=\"left\" valign=\"center\"><small><b>Создано</b></small></td><td align=\"center\" valign=\"center\">2014-06-07 17:25:15</td></tr>\n" +
			  "</table></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font></td></tr><tr><td colspan=\"1\"><hr></td></tr><tr><td><table width=\"100%\"><tr><td align=\"left\" valign=\"center\"><small><b>Высота</b></small></td><td align=\"center\" valign=\"center\">236 m</td></tr>\n" +
			  "<tr><td align=\"left\" valign=\"center\"><small><b>Создано</b></small></td><td align=\"center\" valign=\"center\">2014-07-18 17:23:19</td></tr>\n" +
			  "</table></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font></td></tr><tr><td colspan=\"1\"><hr></td></tr><tr><td><table width=\"100%\"><tr><td align=\"left\" valign=\"center\"><small><b>Высота</b></small></td><td align=\"center\" valign=\"center\">236 m</td></tr>\n" +
			  "<tr><td align=\"left\" valign=\"center\"><small><b>Создано</b></small></td><td align=\"center\" valign=\"center\">2014-08-10 13:33:15</td></tr>\n" +
			  "</table></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font></td></tr><tr><td colspan=\"1\"><hr></td></tr><tr><td><table width=\"100%\"><tr><td align=\"left\" valign=\"center\"><small><b>Высота</b></small></td><td align=\"center\" valign=\"center\">236 m</td></tr>\n" +
			  "<tr><td align=\"left\" valign=\"center\"><small><b>Создано</b></small></td><td align=\"center\" valign=\"center\">2014-09-18 16:16:44</td></tr>\n" +
			  "</table></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font>\n" +
			  "<!-- desc_user:end -->\n" +
			  "</td></tr><tr><td colspan=\"1\"><hr></td></tr><tr><td><table width=\"100%\"><tr><td align=\"left\" valign=\"center\"><small><b>Высота</b></small></td><td align=\"center\" valign=\"center\">236 m</td></tr>\n" +
			  "<tr><td align=\"left\" valign=\"center\"><small><b>Создано</b></small></td><td align=\"center\" valign=\"center\">2014-11-21 00:27:30</td></tr>\n" +
			  "</table></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font>\n" +
			  "<!-- desc_gen:end -->]]></description>\n" +
			  "\t<styleUrl>#sport-cyclingsport.png</styleUrl>\n" +
			  "\t<Point>\n" +
			  "\t\t<coordinates>37.1862430,56.1627300,235.55</coordinates>\n" +
			  "\t</Point>\n" +
			  "\t<gx:TimeStamp>\n" +
			  "\t\t<when>2014-11-21T00:27:31Z</when>\n" +
			  "\t</gx:TimeStamp>\n" +
			  "</Placemark>\n" +
			  "<Placemark>\n" +
			  "\t<name>На восток</name>\n" +
			  "\t<description><![CDATA[<!-- desc_gen:start -->\n" +
			  "<font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\">\n" +
			  "<!-- desc_user:start -->\n" +
			  "<font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\"><font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\"><font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\"><font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\"><font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\"><font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\"><font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\"><font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\"><font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\"><a href=\"/storage/emulated/0/Locus/data/media/photo/_1348054187474.jpg\" target=\"_blank\"><img src=\"/storage/emulated/0/Locus/data/media/photo/_1348054187474.jpg\" width=\"330px\" align=\"center\"></a>перекресток грунтовок</td></tr><tr><td colspan=\"1\"><hr></td></tr><tr><td><table width=\"100%\"><tr><td align=\"left\" valign=\"center\"><small><b>Высота</b></small></td><td align=\"center\" valign=\"center\">262m</td></tr>\n" +
			  "<tr><td align=\"left\" valign=\"center\"><small><b>Азимут</b></small></td><td align=\"center\" valign=\"center\">171°</td></tr>\n" +
			  "<tr><td align=\"left\" valign=\"center\"><small><b>Точность</b></small></td><td align=\"center\" valign=\"center\">10m</td></tr>\n" +
			  "<tr><td align=\"left\" valign=\"center\"><small><b>Создано</b></small></td><td align=\"center\" valign=\"center\">2012-09-22 17:01:16</td></tr>\n" +
			  "</table></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font></td></tr><tr><td colspan=\"1\"><hr></td></tr><tr><td><table width=\"100%\"><tr><td align=\"left\" valign=\"center\"><small><b>Высота</b></small></td><td align=\"center\" valign=\"center\">245 m</td></tr>\n" +
			  "<tr><td align=\"left\" valign=\"center\"><small><b>Создано</b></small></td><td align=\"center\" valign=\"center\">2014-05-10 16:33:59</td></tr>\n" +
			  "</table></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font></td></tr><tr><td colspan=\"1\"><hr></td></tr><tr><td><table width=\"100%\"><tr><td align=\"left\" valign=\"center\"><small><b>Высота</b></small></td><td align=\"center\" valign=\"center\">245 m</td></tr>\n" +
			  "<tr><td align=\"left\" valign=\"center\"><small><b>Создано</b></small></td><td align=\"center\" valign=\"center\">2014-06-03 14:39:11</td></tr>\n" +
			  "</table></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font></td></tr><tr><td colspan=\"1\"><hr></td></tr><tr><td><table width=\"100%\"><tr><td align=\"left\" valign=\"center\"><small><b>Высота</b></small></td><td align=\"center\" valign=\"center\">245 m</td></tr>\n" +
			  "<tr><td align=\"left\" valign=\"center\"><small><b>Создано</b></small></td><td align=\"center\" valign=\"center\">2014-06-07 17:25:15</td></tr>\n" +
			  "</table></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font></td></tr><tr><td colspan=\"1\"><hr></td></tr><tr><td><table width=\"100%\"><tr><td align=\"left\" valign=\"center\"><small><b>Высота</b></small></td><td align=\"center\" valign=\"center\">245 m</td></tr>\n" +
			  "<tr><td align=\"left\" valign=\"center\"><small><b>Создано</b></small></td><td align=\"center\" valign=\"center\">2014-07-18 17:23:19</td></tr>\n" +
			  "</table></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font></td></tr><tr><td colspan=\"1\"><hr></td></tr><tr><td><table width=\"100%\"><tr><td align=\"left\" valign=\"center\"><small><b>Высота</b></small></td><td align=\"center\" valign=\"center\">245 m</td></tr>\n" +
			  "<tr><td align=\"left\" valign=\"center\"><small><b>Создано</b></small></td><td align=\"center\" valign=\"center\">2014-08-10 13:33:15</td></tr>\n" +
			  "</table></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font></td></tr><tr><td colspan=\"1\"><hr></td></tr><tr><td><table width=\"100%\"><tr><td align=\"left\" valign=\"center\"><small><b>Высота</b></small></td><td align=\"center\" valign=\"center\">245 m</td></tr>\n" +
			  "<tr><td align=\"left\" valign=\"center\"><small><b>Создано</b></small></td><td align=\"center\" valign=\"center\">2014-09-18 16:16:44</td></tr>\n" +
			  "</table></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font>\n" +
			  "<!-- desc_user:end -->\n" +
			  "</td></tr><tr><td colspan=\"1\"><hr></td></tr><tr><td><table width=\"100%\"><tr><td align=\"left\" valign=\"center\"><small><b>Высота</b></small></td><td align=\"center\" valign=\"center\">245 m</td></tr>\n" +
			  "<tr><td align=\"left\" valign=\"center\"><small><b>Создано</b></small></td><td align=\"center\" valign=\"center\">2014-11-21 00:27:30</td></tr>\n" +
			  "</table></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font>\n" +
			  "<!-- desc_gen:end -->]]></description>\n" +
			  "\t<styleUrl>#sport-cyclingsport.png</styleUrl>\n" +
			  "\t<Point>\n" +
			  "\t\t<coordinates>37.2404120,56.1473070,244.75</coordinates>\n" +
			  "\t</Point>\n" +
			  "\t<gx:TimeStamp>\n" +
			  "\t\t<when>2014-11-21T00:27:31Z</when>\n" +
			  "\t</gx:TimeStamp>\n" +
			  "</Placemark>\n" +
			  "<Placemark>\n" +
			  "\t<name>На север</name>\n" +
			  "\t<description><![CDATA[<!-- desc_gen:start -->\n" +
			  "<font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\">\n" +
			  "<!-- desc_user:start -->\n" +
			  "<font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\"><font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\"><font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\"><font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\"><font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\"><font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\"><font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\"><font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\"><font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\"><a href=\"/storage/emulated/0/Locus/data/media/photo/_1348054239943.jpg\" target=\"_blank\"><img src=\"/storage/emulated/0/Locus/data/media/photo/_1348054239943.jpg\" width=\"330px\" align=\"center\"></a>перекресток грунтовок</td></tr><tr><td colspan=\"1\"><hr></td></tr><tr><td><table width=\"100%\"><tr><td align=\"left\" valign=\"center\"><small><b>Высота</b></small></td><td align=\"center\" valign=\"center\">262m</td></tr>\n" +
			  "<tr><td align=\"left\" valign=\"center\"><small><b>Азимут</b></small></td><td align=\"center\" valign=\"center\">266°</td></tr>\n" +
			  "<tr><td align=\"left\" valign=\"center\"><small><b>Точность</b></small></td><td align=\"center\" valign=\"center\">10m</td></tr>\n" +
			  "<tr><td align=\"left\" valign=\"center\"><small><b>Создано</b></small></td><td align=\"center\" valign=\"center\">2012-09-22 17:01:46</td></tr>\n" +
			  "</table></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font></td></tr><tr><td colspan=\"1\"><hr></td></tr><tr><td><table width=\"100%\"><tr><td align=\"left\" valign=\"center\"><small><b>Высота</b></small></td><td align=\"center\" valign=\"center\">245 m</td></tr>\n" +
			  "<tr><td align=\"left\" valign=\"center\"><small><b>Создано</b></small></td><td align=\"center\" valign=\"center\">2014-05-10 16:33:59</td></tr>\n" +
			  "</table></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font></td></tr><tr><td colspan=\"1\"><hr></td></tr><tr><td><table width=\"100%\"><tr><td align=\"left\" valign=\"center\"><small><b>Высота</b></small></td><td align=\"center\" valign=\"center\">245 m</td></tr>\n" +
			  "<tr><td align=\"left\" valign=\"center\"><small><b>Создано</b></small></td><td align=\"center\" valign=\"center\">2014-06-03 14:39:11</td></tr>\n" +
			  "</table></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font></td></tr><tr><td colspan=\"1\"><hr></td></tr><tr><td><table width=\"100%\"><tr><td align=\"left\" valign=\"center\"><small><b>Высота</b></small></td><td align=\"center\" valign=\"center\">245 m</td></tr>\n" +
			  "<tr><td align=\"left\" valign=\"center\"><small><b>Создано</b></small></td><td align=\"center\" valign=\"center\">2014-06-07 17:25:15</td></tr>\n" +
			  "</table></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font></td></tr><tr><td colspan=\"1\"><hr></td></tr><tr><td><table width=\"100%\"><tr><td align=\"left\" valign=\"center\"><small><b>Высота</b></small></td><td align=\"center\" valign=\"center\">245 m</td></tr>\n" +
			  "<tr><td align=\"left\" valign=\"center\"><small><b>Создано</b></small></td><td align=\"center\" valign=\"center\">2014-07-18 17:23:19</td></tr>\n" +
			  "</table></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font></td></tr><tr><td colspan=\"1\"><hr></td></tr><tr><td><table width=\"100%\"><tr><td align=\"left\" valign=\"center\"><small><b>Высота</b></small></td><td align=\"center\" valign=\"center\">245 m</td></tr>\n" +
			  "<tr><td align=\"left\" valign=\"center\"><small><b>Создано</b></small></td><td align=\"center\" valign=\"center\">2014-08-10 13:33:15</td></tr>\n" +
			  "</table></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font></td></tr><tr><td colspan=\"1\"><hr></td></tr><tr><td><table width=\"100%\"><tr><td align=\"left\" valign=\"center\"><small><b>Высота</b></small></td><td align=\"center\" valign=\"center\">245 m</td></tr>\n" +
			  "<tr><td align=\"left\" valign=\"center\"><small><b>Создано</b></small></td><td align=\"center\" valign=\"center\">2014-09-18 16:16:44</td></tr>\n" +
			  "</table></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font>\n" +
			  "<!-- desc_user:end -->\n" +
			  "</td></tr><tr><td colspan=\"1\"><hr></td></tr><tr><td><table width=\"100%\"><tr><td align=\"left\" valign=\"center\"><small><b>Высота</b></small></td><td align=\"center\" valign=\"center\">245 m</td></tr>\n" +
			  "<tr><td align=\"left\" valign=\"center\"><small><b>Создано</b></small></td><td align=\"center\" valign=\"center\">2014-11-21 00:27:30</td></tr>\n" +
			  "</table></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font>\n" +
			  "<!-- desc_gen:end -->]]></description>\n" +
			  "\t<styleUrl>#sport-cyclingsport.png</styleUrl>\n" +
			  "\t<Point>\n" +
			  "\t\t<coordinates>37.2403750,56.1473270,244.86</coordinates>\n" +
			  "\t</Point>\n" +
			  "\t<gx:TimeStamp>\n" +
			  "\t\t<when>2014-11-21T00:27:31Z</when>\n" +
			  "\t</gx:TimeStamp>\n" +
			  "</Placemark>\n" +
			  "\n" +
			  "\t</Document>\n" +
			  "</kml>";
		multipartFile = new MockMultipartFile("GoogleEarth.kml", googleEarthKml.getBytes(StandardCharsets.UTF_8));
		multipartDto = new MultipartDto(multipartFile);
		multipartDto.setPreviewSize(800);
		multipartDto.setPreviewUnit("pixels");
		multipartDto.setPathType("absolute");
		multipartDto.setPath("D:\\PROGRAM POOL\\Locus\\data-media-photo");
		multipartDto.setClearOutdatedDescriptions(true);
		multipartDto.setPointIconSize(40);
		multipartDto.setPointIconOpacity(70);
		multipartDto.setPointTextSize(0);
		multipartDto.setPointIconSizeDynamic(70);
		multipartDto.setPointIconOpacityDynamic(90);
		multipartDto.setPointTextSizeDynamic(60);
		multipartDto.setPointTextOpacityDynamic(80);
		multipartDto.setPointTextHexColorDynamic("#A5CFE3");
		multipartDto.setDownloadAs(DownloadAs.KML);
		Document document = XmlTestUtils.getDocument(multipartDto);
		googleEarthHandler = new GoogleEarthHandler(new KmlUtils(document, new XmlDomUtils(document)));

		//WHEN
		Document processedDocument = googleEarthHandler.processKml(document, multipartDto);
		
		String resultingKml = kmlHandler.writeTransformedDocument(processedDocument, true);
//		System.out.println(resultingKml);
		
		//THEN
//		assertTrue(resultingKml.contains("<styleUrl>#style3</styleUrl>"));
//		assertTrue(XmlTestUtils.containsParentWithChild(processedDocument, "Placemark", "styleUrl", "#style3"));
	}
}