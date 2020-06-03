package mrbaxmypka.gmail.com.mapPointsTrimmer.xml;

import mrbaxmypka.gmail.com.mapPointsTrimmer.entitiesDto.MultipartDto;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.data.auditing.IsNewAwareAuditingHandler;
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
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * https://developers.google.com/kml/documentation/kmlreference
 */
class GoogleEarthHandlerTest {
	
	private MultipartDto multipartDto;
	private MultipartFile multipartFile;
	private GoogleEarthHandler googleEarthHandler = new GoogleEarthHandler();
	private KmlHandler kmlHandler = new KmlHandler(new HtmlHandler(), googleEarthHandler);
	
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
			"\t\t<description><![CDATA[<!-- desc_gen:start -->\n" +
			" <table width=\"100%\"> \n" +
			" </table><!-- desc_gen:end -->]]></description>\n" +
			"\t\t<gx:TimeStamp><when>2014-11-21T00:27:31Z</when>\n" +
			"</gx:TimeStamp>\n" +
			"\t\t<styleUrl>#transport-steamtrain.png</styleUrl>\n" +
			"\t\t<Point>\n" +
			"\t\t\t<coordinates>37.44571,56.126503,200</coordinates>\n" +
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
	@ValueSource(ints = {7, 21, 199})
	public void percentage_PointIconSize_Should_Create_Scales_Into_Existing_IconStyles(int percentageSize)
		throws IOException, ParserConfigurationException, SAXException, TransformerException {
		//GIVEN with existing <IconStyle>s but without <scale>s into them
		String googleKml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
			"<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:kml=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\">" +
			"<Document>\n" +
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
			"\t\t<description><![CDATA[<!-- desc_gen:start -->\n" +
			" <table width=\"100%\"> \n" +
			" </table><!-- desc_gen:end -->]]></description>\n" +
			"\t\t<gx:TimeStamp><when>2014-11-21T00:27:31Z</when>\n" +
			"</gx:TimeStamp>\n" +
			"\t\t<styleUrl>#transport-steamtrain.png</styleUrl>\n" +
			"\t\t<Point>\n" +
			"\t\t\t<coordinates>37.44571,56.126503,200</coordinates>\n" +
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
			"\t\t<description><![CDATA[<!-- desc_gen:start -->\n" +
			" <table width=\"100%\"> \n" +
			" </table><!-- desc_gen:end -->]]></description>\n" +
			"\t\t<gx:TimeStamp><when>2014-11-21T00:27:31Z</when>\n" +
			"</gx:TimeStamp>\n" +
			"\t\t<styleUrl>#transport-steamtrain.png</styleUrl>\n" +
			"\t\t<Point>\n" +
			"\t\t\t<coordinates>37.44571,56.126503,200</coordinates>\n" +
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
			"\t\t<description><![CDATA[<!-- desc_gen:start -->\n" +
			" <table width=\"100%\"> \n" +
			" </table><!-- desc_gen:end -->]]></description>\n" +
			"\t\t<gx:TimeStamp><when>2014-11-21T00:27:31Z</when>\n" +
			"</gx:TimeStamp>\n" +
			"\t\t<styleUrl>#transport-steamtrain.png</styleUrl>\n" +
			"\t\t<Point>\n" +
			"\t\t\t<coordinates>37.44571,56.126503,200</coordinates>\n" +
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
			"\t\t<description><![CDATA[<!-- desc_gen:start -->\n" +
			" <table width=\"100%\"> \n" +
			" </table><!-- desc_gen:end -->]]></description>\n" +
			"\t\t<gx:TimeStamp><when>2014-11-21T00:27:31Z</when>\n" +
			"</gx:TimeStamp>\n" +
			"\t\t<styleUrl>#transport-steamtrain.png</styleUrl>\n" +
			"\t\t<Point>\n" +
			"\t\t\t<coordinates>37.44571,56.126503,200</coordinates>\n" +
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
	
	
	/**
	 * Typical RGB incoming from HTML color picker list:
	 * HEX COLOR : #ff0000
	 * HEX COLOR : #000000
	 * HEX COLOR : #ffffff
	 * HEX COLOR : #8e4848
	 * <p>
	 * Typical ARGB <color/> from KML:
	 * : 7fffaaff
	 * : a1ff00ff
	 * <p>
	 * * https://developers.google.com/kml/documentation/kmlreference#colorstyle
	 * * <color>
	 * * Color and opacity (alpha) values are expressed in hexadecimal notation.
	 * * The range of values for any one color is 0 to 255 (00 to ff).
	 * * For alpha, 00 is fully transparent and ff is fully opaque.
	 * * The order of expression is aabbggrr,
	 * * where aa=alpha (00 to ff); bb=blue (00 to ff); gg=green (00 to ff); rr=red (00 to ff).
	 * * For example, if you want to apply a blue color with 50 percent opacity to an overlay, you would specify the following:
	 * * <color>7fff0000</color>, where alpha=0x7f, blue=0xff, green=0x00, and red=0x00.
	 */
	@ParameterizedTest
	@ValueSource(strings = {"#000000", "#ff0000", "#ffffff", "#374b5c"})
	public void pointTextColors_Should_Create_LabelStyles_With_Colors(String hexColor)
		throws IOException, ParserConfigurationException, SAXException, TransformerException {
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
			"\t\t<description><![CDATA[<!-- desc_gen:start -->\n" +
			" <table width=\"100%\"> \n" +
			" </table><!-- desc_gen:end -->]]></description>\n" +
			"\t\t<gx:TimeStamp><when>2014-11-21T00:27:31Z</when>\n" +
			"</gx:TimeStamp>\n" +
			"\t\t<styleUrl>#transport-steamtrain.png</styleUrl>\n" +
			"\t\t<Point>\n" +
			"\t\t\t<coordinates>37.44571,56.126503,200</coordinates>\n" +
			"\t\t</Point>\n" +
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
		String kmlColor = googleEarthHandler.getKmlColor(multipartDto.getPointTextColor());
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
	 * Typical ARGB <color/> from KML:
	 * : 7fffaaff
	 * : a1ff00ff
	 * <p>
	 * * https://developers.google.com/kml/documentation/kmlreference#colorstyle
	 * * <color>
	 * * Color and opacity (alpha) values are expressed in hexadecimal notation.
	 * * The range of values for any one color is 0 to 255 (00 to ff).
	 * * For alpha, 00 is fully transparent and ff is fully opaque.
	 * * The order of expression is aabbggrr,
	 * * where aa=alpha (00 to ff); bb=blue (00 to ff); gg=green (00 to ff); rr=red (00 to ff).
	 * * For example, if you want to apply a blue color with 50 percent opacity to an overlay, you would specify the following:
	 * * <color>7fff0000</color>, where alpha=0x7f, blue=0xff, green=0x00, and red=0x00.
	 */
	@ParameterizedTest
	@ValueSource(strings = {"#000088", "#ff0000", "#ffffff", "#374b5c"})
	public void pointTextColors_Should_Update_LabelStyles_With_Colors(String hexColor)
		throws IOException, ParserConfigurationException, SAXException, TransformerException {
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
			"\t\t<description><![CDATA[<!-- desc_gen:start -->\n" +
			" <table width=\"100%\"> \n" +
			" </table><!-- desc_gen:end -->]]></description>\n" +
			"\t\t<gx:TimeStamp><when>2014-11-21T00:27:31Z</when>\n" +
			"</gx:TimeStamp>\n" +
			"\t\t<styleUrl>#transport-steamtrain.png</styleUrl>\n" +
			"\t\t<Point>\n" +
			"\t\t\t<coordinates>37.44571,56.126503,200</coordinates>\n" +
			"\t\t</Point>\n" +
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
		String kmlColor = googleEarthHandler.getKmlColor(multipartDto.getPointTextColor());
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
	 * Typical ARGB <color/> from KML:
	 * : 7fffaaff
	 * : a1ff00ff
	 * <p>
	 * * https://developers.google.com/kml/documentation/kmlreference#colorstyle
	 * * <color>
	 * * Color and opacity (alpha) values are expressed in hexadecimal notation.
	 * * The range of values for any one color is 0 to 255 (00 to ff).
	 * * For alpha, 00 is fully transparent and ff is fully opaque.
	 * * The order of expression is aabbggrr,
	 * * where aa=alpha (00 to ff); bb=blue (00 to ff); gg=green (00 to ff); rr=red (00 to ff).
	 * * For example, if you want to apply a blue color with 50 percent opacity to an overlay, you would specify the following:
	 * * <color>7fff0000</color>, where alpha=0x7f, blue=0xff, green=0x00, and red=0x00.
	 */
	@ParameterizedTest
	@ValueSource(strings = {"#112233", "#ff10ab", "#affbfc", "#374b5c"})
	public void incoming_Hex_Input_as_RRGGBB_Colors_Should_Be_Converted_To_Kml_Colors_as_AABBGGRR(String hexColor) {
		//GIVEN input hex colors from HTML color picker as #rrggbb
		
		//WHEN
		String kmlColor = googleEarthHandler.getKmlColor(hexColor);
		
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
		
		//WHEN
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
			() -> googleEarthHandler.getKmlColor(hexColor));
		
		//THEN
		assertEquals("Color value is not correct! (It has to correspond to '#rrggbb' hex pattern", exception.getMessage());
	}
	
	private Document getDocument(MultipartDto multipartDto) throws ParserConfigurationException, IOException, SAXException {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document document = documentBuilder.parse(multipartDto.getMultipartFile().getInputStream());
		document.normalizeDocument();
		return document;
	}
	
	/**
	 * @param document               To be parsed
	 * @param parentTagName          Parent tagName to be created or an existing one
	 * @param howManyParents         How many parents should be presented within the given document
	 * @param desiredChildrenTagName Parent's children tagName that should be presented strictly within parent's tag
	 * @param desiredChildrenValue   What exact text value should those children have.
	 * @return True if all the conditions are valid
	 */
	private boolean containsElement(
		Document document, String parentTagName, int howManyParents, String desiredChildrenTagName, String desiredChildrenValue) {
		int parentsCount;
		int childrenCount = 0;
		boolean isValueEquals = false;
		
		NodeList parents = document.getElementsByTagName(parentTagName);
		parentsCount = parents.getLength();
		for (int i = 0; i < parents.getLength(); i++) {
			
			Node parent = parents.item(i);
			NodeList parentChildNodes = parent.getChildNodes();
			for (int j = 0; j < parentChildNodes.getLength(); j++) {
				Node children = parentChildNodes.item(j);
				if (children.getNodeName().equals(desiredChildrenTagName)) {
					childrenCount += 1;
					isValueEquals = children.getTextContent().equals(desiredChildrenValue);
				}
			}
			
		}
		return howManyParents == parentsCount && childrenCount == parentsCount && isValueEquals;
	}
}