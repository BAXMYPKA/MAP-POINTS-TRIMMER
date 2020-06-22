package mrbaxmypka.gmail.com.mapPointsTrimmer.xml;

import mrbaxmypka.gmail.com.mapPointsTrimmer.entitiesDto.MultipartDto;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * https://developers.google.com/kml/documentation/kmlreference
 */
class GoogleEarthHandlerTest {
	
	private MultipartDto multipartDto;
	private MultipartFile multipartFile;
	private GoogleEarthHandler googleEarthHandler = new GoogleEarthHandler();
	private KmlHandler kmlHandler = new KmlHandler(new HtmlHandler(), googleEarthHandler);
	private String googleEarthKml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
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
		"\t\t\t<scale>0.8</scale>\n" +
		"\t\t\t<Icon>\n" +
		"\t\t\t\t<href>http://maps.google.com/mapfiles/kml/shapes/poi.png</href>\n" +
		"\t\t\t</Icon>\n" +
		"\t\t</IconStyle>\n" +
		"\t</Style>\n" +
		"\t<Style id=\"style2\">\n" +
		"\t\t<IconStyle>\n" +
		"\t\t\t<scale>0.8</scale>\n" +
		"\t\t\t<Icon>\n" +
		"\t\t\t\t<href>http://maps.google.com/mapfiles/kml/shapes/poi.png</href>\n" +
		"\t\t\t</Icon>\n" +
		"\t\t\t<hotSpot x=\"0.5\" y=\"0\" xunits=\"fraction\" yunits=\"fraction\"/>\n" +
		"\t\t</IconStyle>\n" +
		"\t</Style>\n" +
		"\t<Style id=\"style3\">\n" +
		"\t\t<IconStyle>\n" +
		"\t\t\t<scale>0.6</scale>\n" +
		"\t\t\t<Icon>\n" +
		"\t\t\t\t<href>http://maps.google.com/mapfiles/kml/shapes/earthquake.png</href>\n" +
		"\t\t\t</Icon>\n" +
		"\t\t</IconStyle>\n" +
		"\t</Style>\n" +
		"\t<Style id=\"style4\">\n" +
		"\t\t<IconStyle>\n" +
		"\t\t\t<scale>0.709091</scale>\n" +
		"\t\t\t<Icon>\n" +
		"\t\t\t\t<href>http://maps.google.com/mapfiles/kml/shapes/earthquake.png</href>\n" +
		"\t\t\t</Icon>\n" +
		"\t\t\t<hotSpot x=\"0.5\" y=\"0\" xunits=\"fraction\" yunits=\"fraction\"/>\n" +
		"\t\t</IconStyle>\n" +
		"\t\t<LabelStyle>\n" +
		"\t\t\t<scale>0.8</scale>\n" +
		"\t\t</LabelStyle>\n" +
		"\t</Style>\n" +
		"\t<Folder>\n" +
		"\t\t<name>Folder with POI</name>\n" +
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
		"\t\t<Placemark>\n" +
		"\t\t\t<name>Test Placemark 3</name>\n" +
		"\t\t\t<styleUrl>#style3</styleUrl>\n" +
		"\t\t\t<Point>\n" +
		"\t\t\t\t<coordinates>38.5409832,55.89456632,143</coordinates>\n" +
		"\t\t\t</Point>\n" +
		"\t\t</Placemark>\n" +
		"\t\t<Placemark>\n" +
		"\t\t\t<name>Test Placemark 4</name>\n" +
		"\t\t\t<styleUrl>#style4</styleUrl>\n" +
		"\t\t\t<Point>\n" +
		"\t\t\t\t<coordinates>38.53305459,55.91967435,136</coordinates>\n" +
		"\t\t\t</Point>\n" +
		"\t\t</Placemark>\n" +
		"\t</Folder>\n" +
		"</Document>\n" +
		"</kml>";
	
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
		Document document = getDocument(multipartDto);
		
		//WHEN
		document = googleEarthHandler.processXml(document, multipartDto);
		String processedKml = kmlHandler.writeTransformedDocument(document);
//		System.out.println(processedKml);
		//THEN
		String scale = multipartDto.getPointIconSizeScaled().toString();
		
		assertTrue(containsElement(document, "IconStyle", 2, "scale", scale));
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
		Document document = getDocument(multipartDto);
		
		//WHEN
		document = googleEarthHandler.processXml(document, multipartDto);
//		String processedKml = kmlHandler.writeTransformedDocument(document);
		
		//THEN
		String scale = multipartDto.getPointIconSizeScaled().toString();
		
		assertTrue(containsElement(document, "IconStyle", 2, "scale", scale));
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
		Document document = getDocument(multipartDto);
		
		//WHEN
		document = googleEarthHandler.processXml(document, multipartDto);
//		String processedKml = kmlHandler.writeTransformedDocument(document);
		
		//THEN
		String scale = multipartDto.getPointTextSizeScaled().toString();
		
		assertTrue(containsElement(document, "LabelStyle", 2, "scale", scale));
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
		Document document = getDocument(multipartDto);
		
		//WHEN
		document = googleEarthHandler.processXml(document, multipartDto);
//		String processedKml = kmlHandler.writeTransformedDocument(document);
		
		//THEN
		String scale = multipartDto.getPointTextSizeScaled().toString();
		
		assertTrue(containsElement(document, "LabelStyle", 2, "scale", scale));
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
		Document document = getDocument(multipartDto);
		
		//WHEN
		document = googleEarthHandler.processXml(document, multipartDto);
//		String processedKml = kmlHandler.writeTransformedDocument(document);
		
		//THEN
		String scale = multipartDto.getPointTextSizeScaled().toString();
		
		assertTrue(containsElement(document, "LabelStyle", 2, "scale", scale));
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
		multipartDto.setPointTextColor(hexColor);
		Document document = getDocument(multipartDto);
		
		//WHEN
		document = googleEarthHandler.processXml(document, multipartDto);
//		String processedKml = kmlHandler.writeTransformedDocument(document);
//		System.out.println(processedKml);
		
		//THEN
		String kmlColor = googleEarthHandler.getKmlColor(multipartDto.getPointTextColor(), multipartDto);
//		System.out.println("HEX COLOR : " + hexColor);
//		System.out.println("KML COLOR : " + kmlColor);
		
		assertTrue(containsElement(document, "LabelStyle", 2, "color", kmlColor));
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
		multipartDto.setPointTextColor(hexColor);
		Document document = getDocument(multipartDto);
		
		//WHEN
		document = googleEarthHandler.processXml(document, multipartDto);
//		String processedKml = kmlHandler.writeTransformedDocument(document);
//		System.out.println(processedKml);
		
		//THEN
		String kmlColor = googleEarthHandler.getKmlColor(multipartDto.getPointTextColor(), multipartDto);
//		System.out.println("HEX COLOR : " + hexColor);
//		System.out.println("KML COLOR : " + kmlColor);
		
		assertTrue(containsElement(document, "LabelStyle", 2, "color", kmlColor));
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
	public void incoming_Hex_Input_as_RRGGBB_Colors_Should_Be_Converted_To_Kml_Colors_as_AABBGGRR(String hexColor) {
		//GIVEN input hex colors from HTML color picker as #rrggbb
		multipartDto = new MultipartDto(new MockMultipartFile("name", new byte[]{}));
		
		//WHEN
		String kmlColor = googleEarthHandler.getKmlColor(hexColor, multipartDto);
		
		//THEN
//		System.out.println("HEX COLOR : " + hexColor);
//		System.out.println("KML COLOR : " + kmlColor);
		
		assertTrue(kmlColor.contains("ff332211") || kmlColor.contains("ffab10ff") ||
			kmlColor.contains("fffcfbaf") || kmlColor.contains("ff5c4b37"));
	}
	
	@ParameterizedTest
	@ValueSource(strings = {"#1112233", "#ff10abc", "affbfc", "#374g5c"})
	public void incoming_Incorrect_Hex_Input_Colors_Should_Throw_IllegalArgException(String hexColor) {
		//GIVEN input hex colors from HTML color picker as #rrggbb
		multipartDto = new MultipartDto(new MockMultipartFile("name", new byte[]{}));
		
		//WHEN
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
			() -> googleEarthHandler.getKmlColor(hexColor, multipartDto));
		
		//THEN
		assertEquals("Color value is not correct! (It has to correspond to '#rrggbb' hex pattern", exception.getMessage());
	}
	
	/**
	 * Convert incoming percentage value 0 - 100% to an integer in base sixteen 00 - FF (0 - 255).
	 * Where 100% = 255 and 1% = 2.55 (Rounding.HALF_UP) accordingly but as two hex digits (e.g.  00, 03, 7F, FF)
	 */
	@ParameterizedTest
	@ValueSource(ints = {0, 1, 2, 50, 73, 99, 100})
	public void percentage_Conversion_Into_Hex_Should_Be_Correct(Integer percent) {
		//GIVEN
		// 100% = 255(hex)
		// 1% = 2.55(Rounded.HALF_UP) as two hex digits (e.g. 00, 03, 7F, FF)
		// WHERE: 0 = 00, 1 = 03, 2 = 05, 50 = 127, 99 = FC, 100 = FF
		
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
	public void pointTextColors_Without_setTextOpacity_Should_Starts_With_Max_Opacity_Value_FF(String hexColor) {
		//GIVEN
		multipartFile = new MockMultipartFile("TestPoi.kml", new byte[]{});
		multipartDto = new MultipartDto(multipartFile);
		multipartDto.setPointTextColor(hexColor);
		
		//WHEN
		String kmlColorWithOpacity = googleEarthHandler.getKmlColor(multipartDto.getPointTextColor(), multipartDto);
		
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
	public void pointTextColors_With_setTextOpacity_Should_Starts_With_Opacity_Value_But_Not_Default_FF(String hexColor) {
		//GIVEN
		multipartFile = new MockMultipartFile("TestPoi.kml", new byte[]{});
		multipartDto = new MultipartDto(multipartFile);
		multipartDto.setPointTextColor(hexColor);
		multipartDto.setPointTextOpacity((int) (Math.random() * 100 + 1));
		
		//WHEN
		String kmlColorWithOpacity = googleEarthHandler.getKmlColor(multipartDto.getPointTextColor(), multipartDto);
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
			multipartDto.setPointTextColor(htmlPointsTextColor);
		}
		Document document = getDocument(multipartDto);
		
		//WHEN
		document = googleEarthHandler.processXml(document, multipartDto);
		String processedKml = kmlHandler.writeTransformedDocument(document);
//		System.out.println(processedKml);
		
		//THEN
		//Only the "normal" #style1, #style2 from <StyleMap>'s and the referenced from <Placemark> #style4 should be changed (3 pieces)
		//But "highlighted" #style3 from <StyleMap> and unused #style4 should be unchanged (2 pieces)
		if (staticType.equals("iconSize")) {
			String pointIconScale = multipartDto.getPointIconSizeScaled().toString();
			assertTrue(containsElement(document, "IconStyle", 3, "scale", pointIconScale));
			assertTrue(containsElement(document, "IconStyle", 2, "scale", "0.8"));
		} else if (staticType.equals("textSize")) {
			String pointsTextScale = multipartDto.getPointTextSizeScaled().toString();
			assertTrue(containsElement(document, "LabelStyle", 3, "scale", pointsTextScale));
			assertTrue(containsElement(document, "LabelStyle", 2, "scale", "0.8"));
		} else if (staticType.equals("textColor")) {
			String kmlColor = googleEarthHandler.getKmlColor(htmlPointsTextColor, multipartDto);
			assertTrue(containsElement(document, "LabelStyle", 3, "color", kmlColor));
			assertTrue(containsElement(document, "LabelStyle", 2, "color", "00000000"));
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
		
		//WHEN
		Document processedDocument = googleEarthHandler.processXml(getDocument(multipartDto), multipartDto);
		
		String resultingKml = kmlHandler.writeTransformedDocument(processedDocument);
//		System.out.println(resultingKml);
		
		//THEN
		assertTrue(resultingKml.contains("<styleUrl>#style3</styleUrl>"));
		assertTrue(containsParentWithChild(
			  processedDocument, "Placemark", "styleUrl", "#style3"));
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////// DYNAMIC STYLES TESTS /////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Test
	public void any_Dynamic_Set_Should_Create_StyleMaps()
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
			  "\t\t\t<Icon></Icon>\n" +
			  "\t\t</IconStyle>\n" +
			  "\t</Style>\n" +
			  "\t<Style id=\"style2\">\n" +
			  "\t\t<IconStyle>\n" +
			  "\t\t\t<Icon></Icon>\n" +
			  "\t\t</IconStyle>\n" +
			  "\t</Style>\n" +
			  "\t\t<Style id=\"style3\">\n" +
			  "\t\t<IconStyle>\n" +
			  "\t\t\t<Icon></Icon>\n" +
			  "\t\t</IconStyle>\n" +
			  "\t\t<LabelStyle>\n" +
			  "\t\t\t<scale>0.8</scale>\n" +
			  "\t\t</LabelStyle>\n" +
			  "\t</Style>\n" +
			  "\t\t<Style id=\"style4\">\n" +
			  "\t\t<IconStyle>\n" +
			  "\t\t\t<Icon></Icon>\n" +
			  "\t\t</IconStyle>\n" +
			  "\t</Style>\n" +
			  "\t\t<Placemark>\n" +
			  "\t\t\t<name>Test Placemark 1</name>\n" +
			  "\t\t\t<styleUrl>#styleMap1</styleUrl>\n" +
			  "\t\t\t<Point></Point>\n" +
			  "\t\t</Placemark>\n" +
			  "\t\t<Placemark>\n" +
			  "\t\t\t<name>Test Placemark 2</name>\n" +
			  "\t\t\t<styleUrl>#styleMap2</styleUrl>\n" +
			  "\t\t\t<Point></Point>\n" +
			  "\t\t</Placemark>\n" +
			  "\t\t<Placemark>\n" +
			  "\t\t\t<name>Test Placemark 2</name>\n" +
			  "\t\t\t<styleUrl>#style1</styleUrl>\n" +
			  "\t\t\t<Point></Point>\n" +
			  "\t\t</Placemark>\n" +
			  "\t\t\t\t<Placemark>\n" +
			  "\t\t\t<name>Test Placemark 3</name>\n" +
			  "\t\t\t<styleUrl>#style4</styleUrl>\n" +
			  "\t\t\t<Point></Point>\n" +
			  "\t\t</Placemark>\n" +
			  "</Document>\n" +
			  "</kml>";
		multipartFile = new MockMultipartFile("GoogleEarth.kml", googleKml.getBytes(StandardCharsets.UTF_8));
		multipartDto = new MultipartDto(multipartFile);
		multipartDto.setPointIconSizeDynamic(50);
		
		//WHEN
		Document processedDocument = googleEarthHandler.processXml(getDocument(multipartDto), multipartDto);
		
		String resultingKml = kmlHandler.writeTransformedDocument(processedDocument);
		System.out.println(resultingKml);
		
		//THEN
		
	}
	
	@Test
	public void any_Dynamic_Set_Should_Replace_All_Placemarks_StyleUrl_References_to_Styles_With_StyleMaps()
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
			"</Document>\n" +
			"</kml>";
		multipartFile = new MockMultipartFile("GoogleEarth.kml", googleKml.getBytes(StandardCharsets.UTF_8));
		multipartDto = new MultipartDto(multipartFile);
		multipartDto.setPointTextSize(50);
		
		//WHEN
		Document processedDocument = googleEarthHandler.processXml(getDocument(multipartDto), multipartDto);
		
		String resultingKml = kmlHandler.writeTransformedDocument(processedDocument);
		
		//THEN
		assertFalse(resultingKml.contains("<StyleMap"));
	}
	
	@Disabled
	@Test
	public void with_Static_Parameters_Placemarks_StyleUrls_To_StyleMaps_Should_Be_Replaced_With_Key_Normal_Style_Url()
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
			"</Document>\n" +
			"</kml>";
		multipartFile = new MockMultipartFile("GoogleEarth.kml", googleKml.getBytes(StandardCharsets.UTF_8));
		multipartDto = new MultipartDto(multipartFile);
		multipartDto.setPointTextSize(50);
		
		//WHEN
		Document processedDocument = googleEarthHandler.processXml(getDocument(multipartDto), multipartDto);
		
		String resultingKml = kmlHandler.writeTransformedDocument(processedDocument);
//		System.out.println(resultingKml);
		
		//THEN
		assertAll(
			() -> assertFalse(resultingKml.contains("<styleUrl>#styleMap1</styleUrl>")),
			() -> assertFalse(resultingKml.contains("<styleUrl>#styleMap2</styleUrl>"))
		);
		assertAll(
			() -> assertTrue(resultingKml.contains("<styleUrl>#style1</styleUrl>")),
			() -> assertTrue(resultingKml.contains("<styleUrl>#style2</styleUrl>"))
		);
	}
	
	
	@Disabled
	@Test
	public void with_Dynamic_Parameters_Unused_Styles_Should_Be_Deleted()
		throws IOException, SAXException, ParserConfigurationException, TransformerException {
		//GIVEN <Style id="style3"> is the <highlight> of <StyleMap id=styleMap1">. Unused and should be deleted
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
			"\t\t\t<styleUrl>#style2</styleUrl>\n" +
			"\t\t\t<Point>\n" +
			"\t\t\t\t<coordinates>38.54269981,55.88994587,145</coordinates>\n" +
			"\t\t\t</Point>\n" +
			"\t\t</Placemark>\n" +
			"\t\t\t<Placemark>\n" +
			"\t\t\t<name>Test Placemark 3</name>\n" +
			"\t\t\t<styleUrl>#style2</styleUrl>\n" +
			"\t\t\t<Point>\n" +
			"\t\t\t\t<coordinates>38.5409832,55.89456632,143</coordinates>\n" +
			"\t\t\t</Point>\n" +
			"\t\t</Placemark>\n" +
			"</Document>\n" +
			"</kml>";
		multipartFile = new MockMultipartFile("GoogleEarth.kml", googleKml.getBytes(StandardCharsets.UTF_8));
		multipartDto = new MultipartDto(multipartFile);
		multipartDto.setPointTextSize(50);
		
		//WHEN
		Document processedDocument = googleEarthHandler.processXml(getDocument(multipartDto), multipartDto);
		
		String resultingKml = kmlHandler.writeTransformedDocument(processedDocument);
//		System.out.println(resultingKml);
		
		//THEN
		assertFalse(resultingKml.contains("<styleUrl>#style3</styleUrl>"));
		assertFalse(containsParentWithChild(
			processedDocument, "Placemark", "styleUrl", "#style3"));
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////// UTILITY METHODS ///////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	private Document getDocument(MultipartDto multipartDto) throws ParserConfigurationException, IOException, SAXException {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document document = documentBuilder.parse(multipartDto.getMultipartFile().getInputStream());
		document.normalizeDocument();
		return document;
	}
	
	/**
	 * @param document                     The source to be parsed
	 * @param parentTagName                Parent tagName to be created or an existing one
	 * @param numOfParentsWithDesiredChild How many parents with the desired child should be presented within the given document
	 * @param desiredChildrenTagName       Parent's children tagName that should be presented strictly within parent's tag
	 * @param desiredChildrenValue         What exact text value should those children have.
	 * @return True if all the conditions are valid
	 */
	private boolean containsElement(
		Document document, String parentTagName, int numOfParentsWithDesiredChild, String desiredChildrenTagName, String desiredChildrenValue) {
		int parentsWithDesiredChildCount = 0;
		
		NodeList parents = document.getElementsByTagName(parentTagName);
		for (int i = 0; i < parents.getLength(); i++) {
			
			Node parent = parents.item(i);
			NodeList parentChildNodes = parent.getChildNodes();
			for (int j = 0; j < parentChildNodes.getLength(); j++) {
				Node child = parentChildNodes.item(j);
				if (child.getNodeName().equals(desiredChildrenTagName) && child.getTextContent().equals(desiredChildrenValue)) {
					parentsWithDesiredChildCount += 1;
				}
			}
			
		}
		return parentsWithDesiredChildCount == numOfParentsWithDesiredChild;
	}
	
	/**
	 * @param document
	 * @param parentTagName       Parent Nodes to be looked for their children
	 * @param desiredChildTagName A child tag name to found in every Parent
	 * @param desiredChildValue   A child text value to be kept in a child Node
	 * @return true if any of the Parent contains the Child with the desired value
	 */
	private boolean containsParentWithChild(
		Document document, String parentTagName, String desiredChildTagName, String desiredChildValue) {
		
		NodeList parentNodes = document.getElementsByTagName(parentTagName);
		for (int i = 0; i < parentNodes.getLength(); i++) {
			Node parentNode = parentNodes.item(i);
			
			NodeList childNodes = parentNode.getChildNodes();
			for (int j = 0; j < childNodes.getLength(); j++) {
				Node child = childNodes.item(j);
				if (child.getNodeName() != null && child.getNodeName().equals(desiredChildTagName) &&
					child.getTextContent().equals(desiredChildValue)) {
					return true;
				}
			}
		}
		return false;
	}
}