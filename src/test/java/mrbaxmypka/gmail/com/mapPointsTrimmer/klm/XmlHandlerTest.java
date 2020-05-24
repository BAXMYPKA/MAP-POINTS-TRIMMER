package mrbaxmypka.gmail.com.mapPointsTrimmer.klm;

import mrbaxmypka.gmail.com.mapPointsTrimmer.entitiesDto.MultipartDto;
import mrbaxmypka.gmail.com.mapPointsTrimmer.utils.PreviewSizeUnits;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class XmlHandlerTest {
	
	private static InputStream inputStream;
	private static MultipartDto multipartDto;
	private static MultipartFile multipartFile;
	private static String locusKml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
		"<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\"\n" +
		"xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" +
		"<Document>\n" +
		"\t<name>Test POIs from Locus</name>\n" +
		"\t<atom:author><atom:name>Locus (Android)</atom:name></atom:author>\n" +
		"\t<Style id=\"file:///sdcard/Locus/cache/images/1571471453728\">\n" +
		"\t\t<IconStyle>\n" +
		"\t\t\t<Icon><href>files/file-sdcardLocuscacheimages1571471453728.png</href></Icon>\n" +
		"\t\t\t<hotSpot x=\"0.5\" y=\"0.0\" xunits=\"fraction\" yunits=\"fraction\" />\n" +
		"\t\t</IconStyle>\n" +
		"\t</Style>\n" +
		"\t<Style id=\"generic_n44\">\n" +
		"\t\t<IconStyle>\n" +
		"\t\t\t<scale>0.8</scale>\n" +
		"\t\t\t<Icon>\n" +
		"\t\t\t\t<href>http://maps.google.com/mapfiles/kml/shapes/motorcycling.png</href>\n" +
		"\t\t\t</Icon>\n" +
		"\t\t\t<hotSpot x=\"0.5\" y=\"0\" xunits=\"fraction\" yunits=\"fraction\"/>\n" +
		"\t\t</IconStyle>\n" +
		"\t\t<LabelStyle>\n" +
		"\t\t\t<scale>0.7</scale>\n" +
		"\t\t</LabelStyle>\n" +
		"\t\t<BalloonStyle>\n" +
		"\t\t\t<text>$[description]</text>\n" +
		"\t\t</BalloonStyle>\n" +
		"\t</Style>\n" +
		"\t<Style id=\"file:///sdcard/Locus/cache/images/15891916769520\">\n" +
		"\t\t<IconStyle>\n" +
		"\t\t\t<Icon>\n" +
		"\t\t\t\t<href>files/file-sdcardLocuscacheimages1589191676952.png</href>\n" +
		"\t\t\t</Icon>\n" +
		"\t\t\t<hotSpot x=\"0.5\" y=\"0\" xunits=\"fraction\" yunits=\"fraction\"/>\n" +
		"\t\t</IconStyle>\n" +
		"\t</Style>\n" +
		"<Placemark>\n" +
		"\t<name>A road fork</name>\n" +
		"\t<description><![CDATA[<!-- desc_gen:start -->\n" +
		"<table width=\"100%\">\n" +
		" <tbody>\n" +
		" <!-- desc_user:start -->\n" +
		" <font color=\"black\">\n" +
		"   <tr>\n" +
		"\t<td width=\"100%\" align=\"center\"><a href=\"files/_1318431492316.jpg\" target=\"_blank\"><img src=\"files/_1318431492316.jpg\" width=\"330px\" align=\"center\"></a>Test place name</td>\n" +
		"   </tr>\n" +
		"\t <table width=\"100%\">\n" +
		"\t  <tbody>\n" +
		"\t   <tr>\n" +
		"\t\t<td align=\"left\" valign=\"center\"><small><b>Высота</b></small></td>\n" +
		"\t\t<td align=\"center\" valign=\"center\">203m</td>\n" +
		"\t   </tr> \n" +
		"\t   <tr>\n" +
		"\t\t<td align=\"left\" valign=\"center\"><small><b>Азимут</b></small></td>\n" +
		"\t\t<td align=\"center\" valign=\"center\">332°</td>\n" +
		"\t   </tr> \n" +
		"\t   <tr>\n" +
		"\t\t<td align=\"left\" valign=\"center\"><small><b>Точность</b></small></td>\n" +
		"\t\t<td align=\"center\" valign=\"center\">10m</td>\n" +
		"\t   </tr> \n" +
		"\t   <tr>\n" +
		"\t\t<td align=\"left\" valign=\"center\"><small><b>Создано</b></small></td>\n" +
		"\t\t<td align=\"center\" valign=\"center\">2012-09-18 18:46:14</td>\n" +
		"\t   </tr> \n" +
		"\t  </tbody>\n" +
		"\t </table></font> \n" +
		"\t Test user description<!-- desc_user:end --> >\n" +
		" </tbody>\n" +
		"</table>" +
		"<!-- desc_gen:end -->]]></description>\n" +
		"\t<styleUrl>#misc-sunny.png</styleUrl>\n" +
		"\t<ExtendedData xmlns:lc=\"http://www.locusmap.eu\">\n" +
		"\t\t<lc:attachment>files/_1318431492316.jpg</lc:attachment>\n" +
		"\t</ExtendedData>\n" +
		"\t<Point>\n" +
		"\t\t<coordinates>37.558700,55.919883,0.00</coordinates>\n" +
		"\t</Point>\n" +
		"\t<gx:TimeStamp>\n" +
		"\t\t<when>2014-11-21T00:27:31Z</when>\n" +
		"\t</gx:TimeStamp>\n" +
		"</Placemark>\n" +
		"</Document>\n" +
		"</kml>\n";
	private static String googleEarthKml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
		"<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\"\n" +
		"\t xmlns:kml=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\"\n" +
		"\t xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.google.com/kml/ext/2.2 \">\n" +
		"\t<Document>\n" +
		"\t\t<name>Test POIs from Google Earth.kmz</name>\n" +
		"\t\t<Style id=\"track001102\">\n" +
		"\t\t\t<LineStyle>\n" +
		"\t\t\t\t<color>c80000ff</color>\n" +
		"\t\t\t\t<width>2.3</width>\n" +
		"\t\t\t</LineStyle>\n" +
		"\t\t</Style>\n" +
		"\t\t<StyleMap id=\"msn_police\">\n" +
		"\t\t\t<Pair>\n" +
		"\t\t\t\t<key>normal</key>\n" +
		"\t\t\t\t<styleUrl>#sn_police2</styleUrl>\n" +
		"\t\t\t</Pair>\n" +
		"\t\t\t<Pair>\n" +
		"\t\t\t\t<key>highlight</key>\n" +
		"\t\t\t\t<styleUrl>#sh_police20</styleUrl>\n" +
		"\t\t\t</Pair>\n" +
		"\t\t</StyleMap>\n" +
		"\t\t<Folder>\n" +
		"\t\t\t<name>My Placemarks</name>\n" +
		"\t\t\t<open>1</open>\n" +
		"\t\t\t<Style>\n" +
		"\t\t\t\t<ListStyle>\n" +
		"\t\t\t\t\t<listItemType>check</listItemType>\n" +
		"\t\t\t\t\t<ItemIcon>\n" +
		"\t\t\t\t\t\t<state>open</state>\n" +
		"\t\t\t\t\t\t<href>C:/Users/%username%/Programs/Locus/data-media-photo/mysavedplaces_open.png</href>\n" +
		"\t\t\t\t\t</ItemIcon>\n" +
		"\t\t\t\t\t<ItemIcon>\n" +
		"\t\t\t\t\t\t<state>closed</state>\n" +
		"\t\t\t\t\t\t<href>C:/Users/%username%/Programs/Locus/data-media-photo/mysavedplaces_closed.png\n" +
		"\t\t\t\t\t\t</href>\n" +
		"\t\t\t\t\t</ItemIcon>\n" +
		"\t\t\t\t\t<bgColor>00ffffff</bgColor>\n" +
		"\t\t\t\t\t<maxSnippetLines>2</maxSnippetLines>\n" +
		"\t\t\t\t</ListStyle>\n" +
		"\t\t\t</Style>\n" +
		"\t\t\t<Document>\n" +
		"\t\t\t\t<name>Test POIs from Locus</name>\n" +
		"\t\t\t\t<atom:author>\n" +
		"\t\t\t\t\t<atom:name>Locus (Android)</atom:name>\n" +
		"\t\t\t\t</atom:author>\n" +
		"\t\t\t\t<Style id=\"misc-sunny.png\">\n" +
		"\t\t\t\t\t<IconStyle>\n" +
		"\t\t\t\t\t\t<color>99ffffff</color>\n" +
		"\t\t\t\t\t\t<scale>0.5</scale>\n" +
		"\t\t\t\t\t\t<Icon>\n" +
		"\t\t\t\t\t\t\t<href>C:/Users/%username%/Programs/Locus/data-media-photo/misc-sunny.png</href>\n" +
		"\t\t\t\t\t\t</Icon>\n" +
		"\t\t\t\t\t\t<hotSpot x=\"0.5\" y=\"0\" xunits=\"fraction\" yunits=\"fraction\"/>\n" +
		"\t\t\t\t\t</IconStyle>\n" +
		"\t\t\t\t\t<LabelStyle>\n" +
		"\t\t\t\t\t\t<color>00ffffff</color>\n" +
		"\t\t\t\t\t\t<scale>0</scale>\n" +
		"\t\t\t\t\t</LabelStyle>\n" +
		"\t\t\t\t</Style>\n" +
		"\t\t\t\t<Style id=\"misc-sunny.png0\">\n" +
		"\t\t\t\t\t<IconStyle>\n" +
		"\t\t\t\t\t\t<color>99ffffff</color>\n" +
		"\t\t\t\t\t\t<scale>0.5</scale>\n" +
		"\t\t\t\t\t\t<Icon>\n" +
		"\t\t\t\t\t\t\t<href>C:/Users/%username%/Programs/Locus/data-media-photo/misc-sunny.png</href>\n" +
		"\t\t\t\t\t\t</Icon>\n" +
		"\t\t\t\t\t\t<hotSpot x=\"0.5\" y=\"0\" xunits=\"fraction\" yunits=\"fraction\"/>\n" +
		"\t\t\t\t\t</IconStyle>\n" +
		"\t\t\t\t\t<LabelStyle>\n" +
		"\t\t\t\t\t\t<color>00ffffff</color>\n" +
		"\t\t\t\t\t\t<scale>0</scale>\n" +
		"\t\t\t\t\t</LabelStyle>\n" +
		"\t\t\t\t</Style>\n" +
		"\t\t\t\t<StyleMap id=\"misc-sunny.png1\">\n" +
		"\t\t\t\t\t<Pair>\n" +
		"\t\t\t\t\t\t<key>normal</key>\n" +
		"\t\t\t\t\t\t<styleUrl>#misc-sunny.png0</styleUrl>\n" +
		"\t\t\t\t\t</Pair>\n" +
		"\t\t\t\t\t<Pair>\n" +
		"\t\t\t\t\t\t<key>highlight</key>\n" +
		"\t\t\t\t\t\t<styleUrl>#misc-sunny.png</styleUrl>\n" +
		"\t\t\t\t\t</Pair>\n" +
		"\t\t\t\t</StyleMap>\n" +
		"\t\t\t<Folder>\n" +
		"\t\t\t\t<name>My POIs</name>\n" +
		"\t\t\t\t<Placemark>\n" +
		"\t\t\t\t\t<name>The POI without image</name>\n" +
		"\t\t\t\t\t<open>1</open>\n" +
		"\t\t\t\t\t\t<description>GE description without CDATA</description>\n" +
		"\t\t\t\t\t<styleUrl>#generic41</styleUrl>\n" +
		"\t\t\t\t\t<Point>\n" +
		"\t\t\t\t\t\t<coordinates>38.547163,55.88113662000001,133</coordinates>\n" +
		"\t\t\t\t\t</Point>\n" +
		"\t\t\t\t</Placemark>\n" +
		"\t\t\t\t<Placemark>\n" +
		"\t\t\t\t\t<name>The POI with image</name>\n" +
		"\t\t\t\t\t<open>1</open>\n" +
		"\t\t\t\t\t\t<description><img style=\"width:500px;border:3px white solid;\" src=\"files/p__20200511_130745.jpg\"></description>\n" +
		"\t\t\t\t\t<styleUrl>#generic42</styleUrl>\n" +
		"\t\t\t\t\t<Point>\n" +
		"\t\t\t\t\t\t<coordinates>38.54269981,55.88994587,145</coordinates>\n" +
		"\t\t\t\t\t</Point>\n" +
		"\t\t\t\t</Placemark>\n" +
		"\t\t\t</Folder>\n" +
		"\t</Document>\n" +
		"</kml>\n";
	private HtmlHandler htmlHandler = new HtmlHandler();
	private XmlHandler xmlHandler = new XmlHandler(htmlHandler);
	
	@Test
	public void setPath_Should_Replace_All_Href_Tags_Content_In_Xml_Body()
		throws IOException, ParserConfigurationException, SAXException, XMLStreamException {
		//GIVEN
		String newPath = "C:\\MyPoi\\MyPoiImages";
		multipartFile = new MockMultipartFile(
			"LocusTestPoi.kml", "LocusTestPoi.kml", null, locusKml.getBytes(StandardCharsets.UTF_8));
		multipartDto = new MultipartDto(multipartFile);
		multipartDto.setSetPath(true);
		multipartDto.setPath(newPath);
		multipartDto.setPathType("absolute");
		
		//WHEN
		String processedKml = xmlHandler.processKml(multipartDto);
		
		//THEN
		assertTrue(processedKml.contains("<href>file:///C:/MyPoi/MyPoiImages/file-sdcardLocuscacheimages1571471453728.png</href>"));
		assertTrue(processedKml.contains("<href>file:///C:/MyPoi/MyPoiImages/file-sdcardLocuscacheimages1589191676952.png</href>"));
		
		assertFalse(processedKml.contains("<href>files/file-sdcardLocuscacheimages1571471453728.png</href>"));
		assertFalse(processedKml.contains("<href>files/file-sdcardLocuscacheimages1589191676952.png</href>"));
	}
	
	/**
	 * Google Earth has some special http links to icons which it treats as local.
	 * So those types of <href></href> have not to be changed.
	 */
	@Test
	public void setPath_Should_Replace_All_Href_Tags_Content_In_Xml_Body_Except_Special_GoogleEarth_Web_Links()
		throws IOException, ParserConfigurationException, SAXException, XMLStreamException {
		//GIVEN
		multipartFile = new MockMultipartFile(
			"TestPoi.kml", "TestPoi.kml", null, locusKml.getBytes(StandardCharsets.UTF_8));
		multipartDto = new MultipartDto(multipartFile);
		multipartDto.setSetPath(true);
		multipartDto.setPath("C:\\MyPoi\\MyPoiImages");
		multipartDto.setPathType("absolute");
		
		//WHEN
		String processedKml = xmlHandler.processKml(multipartDto);
		
		//THEN
		//Special GoogleEarth icons paths should be preserved
		assertTrue(processedKml.contains("<href>http://maps.google.com/mapfiles/kml/shapes/motorcycling.png</href>"));
	}
	
	@Test
	public void set_Paths_With_Whitespaces_Should_Set_Correct_Absolute_Path_Type_URL_Encoded()
		throws IOException, ParserConfigurationException, SAXException, XMLStreamException {
		//GIVEN
		String pathWithWhitespaces = "D:\\My Folder\\My POI";
		multipartFile = new MockMultipartFile(
			"LocusTestPois.kml", "LocusTestPois.kml", null, locusKml.getBytes(StandardCharsets.UTF_8));
		multipartDto = new MultipartDto(multipartFile);
		multipartDto.setSetPath(true);
		multipartDto.setPath(pathWithWhitespaces);
		multipartDto.setPathType("absolute");
		
		//WHEN
		String processedHtml = xmlHandler.processKml(multipartDto);
		
		//THEN All whitespaces should be replaced with URL '%20' sign
		assertAll(
			() -> assertTrue(processedHtml.contains(
				"<href>file:///D:/My%20Folder/My%20POI/file-sdcardLocuscacheimages1571471453728.png</href>")),
			() -> assertTrue(processedHtml.contains(
				"<href>file:///D:/My%20Folder/My%20POI/file-sdcardLocuscacheimages1589191676952.png</href>"))
		);
		
		assertAll(
			() -> assertFalse(processedHtml.contains(
				"<href>files/file-sdcardLocuscacheimages1571471453728.png</href>")),
			() -> assertFalse(processedHtml.contains(
				"<href>files/file-sdcardLocuscacheimages1589191676952.png</href>"))
		);
	}
	
	@Test
	public void trimXml_Only_Should_Return_Only_Xml_Without_LineBreaks()
		throws IOException, ParserConfigurationException, SAXException, XMLStreamException {
		//GIVEN
		String locusKml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
			"<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\"\n" +
			"xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" +
			"<Document>\n" +
			"\t<name>Test POIs from Locus</name>\n" +
			"\t<atom:author><atom:name>Locus (Android)</atom:name></atom:author>\n" +
			"<Placemark>\n" +
			"\t<name>A road fork</name>\n" +
			"\t<description><![CDATA[<!-- desc_gen:start -->\n" +
			"<table width=\"100%\">\n" +
			" <tbody>\n" +
			" <!-- desc_user:start -->\n" +
			" <font color=\"black\">\n" +
			"   <tr>\n" +
			"\t<td width=\"100%\" align=\"center\">" +
			"<a href=\"file:///C:/MyPOI/_1318431492316.jpg\" target=\"_blank\">" +
			"<img src=\"file:///C:/MyPOI/_1318431492316.jpg\" width=\"330px\" align=\"center\">" +
			"</a>Test place name</td>\n</tr>\n" +
			"\t Test user description<!-- desc_user:end --> >\n" +
			" </tbody>\n" +
			"</table>" +
			"<!-- desc_gen:end -->]]></description>\n" +
			"\t<styleUrl>#misc-sunny.png</styleUrl>\n" +
			"\t<Point>\n" +
			"\t\t<coordinates>37.558700,55.919883,0.00</coordinates>\n" +
			"\t</Point>\n" +
			"\t<gx:TimeStamp>\n" +
			"\t\t<when>2014-11-21T00:27:31Z</when>\n" +
			"\t</gx:TimeStamp>\n" +
			"</Placemark>\n" +
			"</Document>\n" +
			"</kml>\n";
		inputStream = new ByteArrayInputStream(locusKml.getBytes(StandardCharsets.UTF_8));
		multipartFile = new MockMultipartFile(
			"LocusTestPois.kml", "LocusTestPois.kml", null, inputStream);
		multipartDto = new MultipartDto(multipartFile);
		multipartDto.setTrimXml(true);
		
		//WHEN
		String processedKml = xmlHandler.processKml(multipartDto);
		
		//THEN xml tags are without whitespaces but CDATA starts and ends with them
		
		assertAll(
			() -> assertTrue(processedKml.contains(
				"<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"no\"?><kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\"><Document><name>Test POIs from Locus</name><atom:author><atom:name>Locus (Android)</atom:name></atom:author><Placemark><name>A road fork</name><description>")),
			() -> assertTrue(processedKml.contains(
				"</description><styleUrl>#misc-sunny.png</styleUrl><Point><coordinates>37.558700,55.919883,0.00</coordinates></Point><gx:TimeStamp><when>2014-11-21T00:27:31Z</when></gx:TimeStamp></Placemark></Document></kml>")),
			() -> assertTrue(processedKml.contains(
				"![CDATA[\n" +
					"<!-- desc_gen:start --> <font color=\"black\"> </font><font color=\"black\"> Test user description<!-- desc_user:end --> &gt; </font>\n" +
					"<table width=\"100%\"> \n" +
					" <tbody> <!-- desc_user:start --> \n" +
					"  <tr> \n" +
					"   <td width=\"100%\" align=\"center\"><a href=\"file:///C:/MyPOI/_1318431492316.jpg\" target=\"_blank\"><img src=\"file:///C:/MyPOI/_1318431492316.jpg\" width=\"330px\" align=\"center\"></a>Test place name</td> \n" +
					"  </tr>\n" +
					" </tbody> \n" +
					"</table><!-- desc_gen:end -->"))
		);
		assertFalse(processedKml.contains(
			"<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
				"<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\"\n" +
				"xmlns:atom=\"http://www.w3.org/2005/Atom\">\n"));
	}
	
	@Test
	public void locusAsAttachment_Should_Create_ExtendedData_lcAttachment_With_Src_From_Description()
		throws IOException, ParserConfigurationException, SAXException, XMLStreamException {
		//GIVEN kml without <ExtendedData>
		String locusKml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
			"<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\"\n" +
			"xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" +
			"<Document>\n" +
			"\t<name>Test POIs from Locus</name>\n" +
			"\t<atom:author><atom:name>Locus (Android)</atom:name></atom:author>\n" +
			"<Placemark>\n" +
			"\t<name>A road fork</name>\n" +
			"\t<description><![CDATA[<!-- desc_gen:start -->\n" +
			"<table width=\"100%\">\n" +
			" <tbody>\n" +
			" <!-- desc_user:start -->\n" +
			" <font color=\"black\">\n" +
			"   <tr>\n" +
			"\t<td width=\"100%\" align=\"center\">" +
			"<a href=\"files/_1318431492316.jpg\" target=\"_blank\">" +
			"<img src=\"files//_1318431492316.jpg\" width=\"330px\" align=\"center\">" +
			"</a>Test place name</td>\n</tr>\n" +
			"\t Test user description<!-- desc_user:end --> >\n" +
			" </tbody>\n" +
			"</table>" +
			"<!-- desc_gen:end -->]]></description>\n" +
			"\t<styleUrl>#misc-sunny.png</styleUrl>\n" +
			"\t<Point>\n" +
			"\t\t<coordinates>37.558700,55.919883,0.00</coordinates>\n" +
			"\t</Point>\n" +
			"\t<gx:TimeStamp>\n" +
			"\t\t<when>2014-11-21T00:27:31Z</when>\n" +
			"\t</gx:TimeStamp>\n" +
			"</Placemark>\n" +
			"</Document>\n" +
			"</kml>\n";
		multipartFile = new MockMultipartFile("TestPoi.kml", "TestPoi.kml", null, locusKml.getBytes(StandardCharsets.UTF_8));
		multipartDto = new MultipartDto(multipartFile);
		multipartDto.setAsAttachmentInLocus(true);
		
		//WHEN
		String processedKml = xmlHandler.processKml(multipartDto);

		//THEN <ExtendedData xmlns:lc="http://www.locusmap.eu"> has to be created with <lc:attachment>
		assertAll(
			() -> assertTrue(processedKml.contains(
				"<ExtendedData xmlns:lc=\"http://www.locusmap.eu\">")),
			() -> assertTrue(processedKml.contains(
				"<lc:attachment>files/_1318431492316.jpg</lc:attachment>")),
			() -> assertTrue(processedKml.contains(
				"</ExtendedData>"))
		);
	}
	
	@Test
	public void locusAsAttachment_Should_Just_Replace_Existing_Src_From_Description()
		throws IOException, ParserConfigurationException, SAXException, XMLStreamException {
		//GIVEN with existing <ExtendedData>
		String locusKml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
			"<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\"\n" +
			"xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" +
			"<Document>\n" +
			"\t<name>Test POIs from Locus</name>\n" +
			"\t<atom:author><atom:name>Locus (Android)</atom:name></atom:author>\n" +
			"<Placemark>\n" +
			"\t<name>A road fork</name>\n" +
			"\t<description><![CDATA[<!-- desc_gen:start -->\n" +
			"<table width=\"100%\">\n" +
			" <tbody>\n" +
			" <!-- desc_user:start -->\n" +
			" <font color=\"black\">\n" +
			"   <tr>\n" +
			"\t<td width=\"100%\" align=\"center\">" +
			"<a href=\"file:///C:/MyPOI/_1318431492316.jpg\" target=\"_blank\">" +
			"<img src=\"file:///C:/MyPOI/_1318431492316.jpg\" width=\"330px\" align=\"center\">" +
			"</a>Test place name</td>\n</tr>\n" +
			"\t Test user description<!-- desc_user:end --> >\n" +
			" </tbody>\n" +
			"</table>" +
			"<!-- desc_gen:end -->]]></description>\n" +
			"\t<styleUrl>#misc-sunny.png</styleUrl>\n" +
			"\t<ExtendedData xmlns:lc=\"http://www.locusmap.eu\">\n" +
			"\t\t<lc:attachment>files/_1318431492316.jpg</lc:attachment>\n" +
			"\t</ExtendedData>\n" +
			"\t<Point>\n" +
			"\t\t<coordinates>37.558700,55.919883,0.00</coordinates>\n" +
			"\t</Point>\n" +
			"\t<gx:TimeStamp>\n" +
			"\t\t<when>2014-11-21T00:27:31Z</when>\n" +
			"\t</gx:TimeStamp>\n" +
			"</Placemark>\n" +
			"</Document>\n" +
			"</kml>\n";
		multipartFile = new MockMultipartFile("TestPoi.kml", "TestPoi.kml", null, locusKml.getBytes(StandardCharsets.UTF_8));
		multipartDto = new MultipartDto(multipartFile);
		multipartDto.setAsAttachmentInLocus(true);
		
		//WHEN
		String processedKml = xmlHandler.processKml(multipartDto);
		
		//THEN <lc:attachments> text has to be replaced from description one
		assertTrue(processedKml.contains("<ExtendedData xmlns:lc=\"http://www.locusmap.eu\""));//Just a check
		assertTrue(processedKml.contains("<lc:attachment>C:/MyPOI/_1318431492316.jpg</lc:attachment>"));
		assertFalse(processedKml.contains("<lc:attachment>files/_1318431492316.jpg</lc:attachment>"));
	}
	
	@Test
	public void locusAsAttachment_Should_Add_More_Attachments_Src_From_Description()
		throws IOException, ParserConfigurationException, SAXException, XMLStreamException {
		//GIVEN with existing <ExtendedData> and additional images in description
		String locusKml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
			"<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\"\n" +
			"xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" +
			"<Document>\n" +
			"\t<name>Test POIs from Locus</name>\n" +
			"\t<atom:author><atom:name>Locus (Android)</atom:name></atom:author>\n" +
			"<Placemark>\n" +
			"\t<name>A road fork</name>\n" +
			"\t<description><![CDATA[<!-- desc_gen:start -->\n" +
			"<table width=\"100%\">\n" +
			" <tbody>\n" +
			" <!-- desc_user:start -->\n" +
			" <font color=\"black\">\n" +
			"   <tr>\n" +
			"\t<td width=\"100%\" align=\"center\">" +
			"<a href=\"file:///C:/MyPOI/_1318431492316.jpg\" target=\"_blank\">" +
			"<img src=\"file:///C:/MyPOI/_1318431492316.jpg\" width=\"330px\" align=\"center\"></a>" +
			"<a href=\"files/p__20180523_123601.jpg\" target=\"_blank\">" +
			"<img src=\"files/p__20180523_123601.jpg\" width=\"60px\" align=\"right\" style=\"border: 3px white solid;\"></a>\n" +
			"\t<a href=\"files/p__20200409_150847.jpg\" target=\"_blank\">" +
			"<img src=\"files/p__20200409_150847.jpg\" width=\"60px\" align=\"right\" style=\"border: 3px white solid;\"></a>" +
			"Test place name</td>\n</tr>\n" +
			"\t Test user description<!-- desc_user:end --> >\n" +
			" </tbody>\n" +
			"</table>" +
			"<!-- desc_gen:end -->]]></description>\n" +
			"\t<styleUrl>#misc-sunny.png</styleUrl>\n" +
			"\t<ExtendedData xmlns:lc=\"http://www.locusmap.eu\">\n" +
			"\t\t<lc:attachment>files/_1318431492316.jpg</lc:attachment>\n" +
			"\t</ExtendedData>\n" +
			"\t<Point>\n" +
			"\t\t<coordinates>37.558700,55.919883,0.00</coordinates>\n" +
			"\t</Point>\n" +
			"\t<gx:TimeStamp>\n" +
			"\t\t<when>2014-11-21T00:27:31Z</when>\n" +
			"\t</gx:TimeStamp>\n" +
			"</Placemark>\n" +
			"</Document>\n" +
			"</kml>\n";
		multipartFile = new MockMultipartFile("TestPoi.kml", "TestPoi.kml", null, locusKml.getBytes(StandardCharsets.UTF_8));
		multipartDto = new MultipartDto(multipartFile);
		multipartDto.setAsAttachmentInLocus(true);
		
		//WHEN
		String processedKml = xmlHandler.processKml(multipartDto);
		
		//THEN <ExtendedData> has to be filled with new <lc:attachment>'s with src to images from description
		assertFalse(processedKml.contains("<lc:attachment>files/_1318431492316.jpg</lc:attachment>"));
		
		assertAll(
			() -> assertTrue(processedKml.contains(
				"<ExtendedData xmlns:lc=\"http://www.locusmap.eu\"")),
			() -> assertTrue(processedKml.contains(
				"<lc:attachment>C:/MyPOI/_1318431492316.jpg</lc:attachment>")),
			() -> assertTrue(processedKml.contains(
				"<lc:attachment>files/p__20180523_123601.jpg</lc:attachment>")),
			() -> assertTrue(processedKml.contains(
				"<lc:attachment>files/p__20200409_150847.jpg</lc:attachment>")),
			() -> assertTrue(processedKml.contains("</ExtendedData>"))
		
		);
	}
	
	@Test
	public void locusAsAttachment_With_2_Placemarks_Should_Add_More_Attachments_Src_From_Description_To_Each_Without_Dublicates()
		throws IOException, ParserConfigurationException, SAXException, XMLStreamException {
		//GIVEN For 2 Placemarks in a row with existing <ExtendedData> and additional images in description
		String locus = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
			"<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" +
			"<Document>\n" +
			"\t<name>FullTestKmzExport01</name>\n" +
			"\t<atom:author><atom:name>Locus (Android)</atom:name></atom:author>" +
			"<Placemark>\n" +
			"\t<name>2020-05-11 13:03:33</name>\n" +
			"\t<description><![CDATA[<!-- desc_gen:start -->\n" +
			"<font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\"><a href=\"files/p__20200511_130333.jpg\" target=\"_blank\"><img src=\"files/p__20200511_130333.jpg\" width=\"60px\" align=\"right\" style=\"border: 3px white solid;\"></a><br /><br /></td></tr><tr><td colspan=\"1\"><hr></td></tr><tr><td><table width=\"100%\"><tr><td align=\"left\" valign=\"center\"><small><b>Высота</b></small></td><td align=\"center\" valign=\"center\">166 m</td></tr>\n" +
			"<tr><td align=\"left\" valign=\"center\"><small><b>Скорость</b></small></td><td align=\"center\" valign=\"center\">12,6 km/h</td></tr>\n" +
			"<tr><td align=\"left\" valign=\"center\"><small><b>Азимут</b></small></td><td align=\"center\" valign=\"center\">327 °</td></tr>\n" +
			"<tr><td align=\"left\" valign=\"center\"><small><b>Точность</b></small></td><td align=\"center\" valign=\"center\">10 m</td></tr>\n" +
			"<tr><td align=\"left\" valign=\"center\"><small><b>Создано</b></small></td><td align=\"center\" valign=\"center\">2020-05-11 13:03:45</td></tr>\n" +
			"</table></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font>\n" +
			"<!-- desc_gen:end -->]]></description>\n" +
			"\t<styleUrl>#file:///sdcard/Locus/cache/images/1589191424250</styleUrl>\n" +
			"\t<ExtendedData xmlns:lc=\"http://www.locusmap.eu\">\n" +
			"\t\t<lc:attachment>files/p__20200511_130333.jpg</lc:attachment>\n" +
			"\t</ExtendedData>\n" +
			">\t<Point>\n" +
			"\t\t<coordinates>37.786342,56.039063,166.00</coordinates>\n" +
			"\t</Point>\n" +
			"\t<gx:TimeStamp>\n" +
			"\t\t<when>2020-05-11T13:03:33Z</when>\n" +
			"\t</gx:TimeStamp>\n" +
			"</Placemark>\n" +
			"<Placemark>\n" +
			"\t<name>2020-05-11 12:53:32</name>\n" +
			"\t<description><![CDATA[<!-- desc_gen:start -->\n" +
			"<font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\"><a href=\"files/p__20200511_125332.jpg\" target=\"_blank\"><img src=\"files/p__20200511_125332.jpg\" width=\"60px\" align=\"right\" style=\"border: 3px white solid;\"></a><br /><br /></td></tr><tr><td colspan=\"1\"><hr></td></tr><tr><td><table width=\"100%\"><tr><td align=\"left\" valign=\"center\"><small><b>Высота</b></small></td><td align=\"center\" valign=\"center\">159 m</td></tr>\n" +
			"<tr><td align=\"left\" valign=\"center\"><small><b>Скорость</b></small></td><td align=\"center\" valign=\"center\">1,8 km/h</td></tr>\n" +
			"<tr><td align=\"left\" valign=\"center\"><small><b>Азимут</b></small></td><td align=\"center\" valign=\"center\">236 °</td></tr>\n" +
			"<tr><td align=\"left\" valign=\"center\"><small><b>Точность</b></small></td><td align=\"center\" valign=\"center\">10 m</td></tr>\n" +
			"<tr><td align=\"left\" valign=\"center\"><small><b>Создано</b></small></td><td align=\"center\" valign=\"center\">2020-05-11 12:53:50</td></tr>\n" +
			"</table></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font>\n" +
			"<!-- desc_gen:end -->]]></description>\n" +
			"\t<styleUrl>#misc-sunny.png</styleUrl>\n" +
			"\t<ExtendedData xmlns:lc=\"http://www.locusmap.eu\">\n" +
			"\t\t<lc:attachment>files/p__20200511_125332.jpg</lc:attachment>\n" +
			"\t</ExtendedData>\n" +
			">\t<Point>\n" +
			"\t\t<coordinates>37.809237,56.039861,159.00</coordinates>\n" +
			"\t</Point>\n" +
			"\t<gx:TimeStamp>\n" +
			"\t\t<when>2020-05-11T12:53:32Z</when>\n" +
			"\t</gx:TimeStamp>\n" +
			"</Placemark>\n" +
			"</Document>\n" +
			"</kml>\n";
		multipartFile = new MockMultipartFile("TestPoi.kml", "TestPoi.kml", null, locus.getBytes(StandardCharsets.UTF_8));
		multipartDto = new MultipartDto(multipartFile);
		multipartDto.setAsAttachmentInLocus(true);
		multipartDto.setSetPreviewSize(true);
		multipartDto.setPreviewSize(640);
		multipartDto.setPreviewSizeUnit(PreviewSizeUnits.PIXELS);
		
		//WHEN
		String processedKml = xmlHandler.processKml(multipartDto);
		
		//THEN <ExtendedData> has to be filled with new <lc:attachment>'s with src to images from description
		assertAll(
			() -> assertTrue(processedKml.contains(
				"<ExtendedData xmlns:lc=\"http://www.locusmap.eu\">\n" +
					"\t\t<lc:attachment>files/p__20200511_130333.jpg</lc:attachment>\n" +
					"\t</ExtendedData>")),
			() -> assertTrue(processedKml.contains(
				"<ExtendedData xmlns:lc=\"http://www.locusmap.eu\">\n" +
					"\t\t<lc:attachment>files/p__20200511_125332.jpg</lc:attachment>\n" +
					"\t</ExtendedData>"))
		);
	}
	
	@Test
	public void locusAsAttachment_Should_Contain_Only_Relative_And_Absolute_Type_Of_Href_When_PathTypes_Undefined()
		throws IOException, ParserConfigurationException, SAXException, XMLStreamException {
		//GIVEN with existing <ExtendedData> and additional images in description with various types of path
		String locusKml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
			"<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\"\n" +
			"xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" +
			"<Document>\n" +
			"\t<name>Test POIs from Locus</name>\n" +
			"\t<atom:author><atom:name>Locus (Android)</atom:name></atom:author>\n" +
			"<Placemark>\n" +
			"\t<name>A road fork</name>\n" +
			"\t<description><![CDATA[<!-- desc_gen:start -->\n" +
			"<table width=\"100%\">\n" +
			" <tbody>\n" +
			" <!-- desc_user:start -->\n" +
			" <font color=\"black\">\n" +
			"   <tr>\n" +
			"\t<td width=\"100%\" align=\"center\">" +
			"<a href=\"http://my-site/images/_1318431492333.jpg\" target=\"_blank\">" +
			"<img src=\"http://my-site/images/_1318431492333.jpg\" width=\"330px\" align=\"center\"></a>" +
			"<a href=\"file:///C:/MyPOI/_1318431492316.jpg\" target=\"_blank\">" +
			"<img src=\"file:///C:/MyPOI/_1318431492316.jpg\" width=\"330px\" align=\"center\"></a>" +
			"<a href=\"files/p__20180523_123601.jpg\" target=\"_blank\">" +
			"<img src=\"files/p__20180523_123601.jpg\" width=\"60px\" align=\"right\" style=\"border: 3px white solid;\"></a>\n" +
			"\t<a href=\"../POI/files/p__20200409_150847.jpg\" target=\"_blank\">" +
			"<img src=\"../POI/files/p__20200409_150847.jpg\" width=\"60px\" align=\"right\" style=\"border: 3px " +
			"white solid;\"></a>" +
			"Test place name</td>\n</tr>\n" +
			"\t Test user description<!-- desc_user:end --> >\n" +
			" </tbody>\n" +
			"</table>" +
			"<!-- desc_gen:end -->]]></description>\n" +
			"\t<styleUrl>#misc-sunny.png</styleUrl>\n" +
			"\t<ExtendedData xmlns:lc=\"http://www.locusmap.eu\">\n" +
			"\t\t<lc:attachment>files/_1318431492316.jpg</lc:attachment>\n" +
			"\t</ExtendedData>\n" +
			"\t<Point>\n" +
			"\t\t<coordinates>37.558700,55.919883,0.00</coordinates>\n" +
			"\t</Point>\n" +
			"\t<gx:TimeStamp>\n" +
			"\t\t<when>2014-11-21T00:27:31Z</when>\n" +
			"\t</gx:TimeStamp>\n" +
			"</Placemark>\n" +
			"</Document>\n" +
			"</kml>\n";
		multipartFile = new MockMultipartFile("TestPoi.kml", "TestPoi.kml", null, locusKml.getBytes(StandardCharsets.UTF_8));
		multipartDto = new MultipartDto(multipartFile);
		multipartDto.setAsAttachmentInLocus(true);
		
		//WHEN
		String processedKml = xmlHandler.processKml(multipartDto);

		//THEN <ExtendedData> has to be filled with new <lc:attachment>'s with src to images from description
		assertAll(
			() -> assertTrue(processedKml.contains(
				"<lc:attachment>C:/MyPOI/_1318431492316.jpg</lc:attachment>")),
			() -> assertTrue(processedKml.contains(
				"<lc:attachment>files/p__20180523_123601.jpg</lc:attachment>")),
			() -> assertTrue(processedKml.contains(
				"<lc:attachment>../POI/files/p__20200409_150847.jpg</lc:attachment>"))
		);
		
		assertAll(
			() -> assertFalse(processedKml.contains(
				"<lc:attachment>http://my-site/images/_1318431492333.jpg</lc:attachment>")),
			() -> assertFalse(processedKml.contains(
				"<lc:attachment>file:///C:/MyPOI/_1318431492316.jpg</lc:attachment>")),
			() -> assertFalse(processedKml.contains(
				"<lc:attachment>/C:/MyPOI/_1318431492316.jpg</lc:attachment>"))
		);
			}
	
	/**
	 * Locus Map points with correct tags as
	 * <ExtendedData xmlns:lc="http://www.locusmap.eu"><lc:attachment>...</lc:attachment></ExtendedData>
	 * after importing to Google Earth and then exporting from Google Earth back loose xmlns:lc="http://www.locusmap.eu"
	 * namespace.
	 * We have to return it back.
	 */
	@Test
	public void header_Should_Be_Added_With_Lc_Locusmap_eu_Namespace_Prefix_If_Contain_Lc_Attachments()
		throws IOException, ParserConfigurationException, SAXException, XMLStreamException {
		//GIVEN with existing <ExtendedData> parent without namespace for "lc" prefix and <lc:attachment> child
		String googleKml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
			"<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" +
			"<Document>\n" +
			"\t<name>Test POIs from Locus</name>\n" +
			"\t<atom:author><atom:name>Locus (Android)</atom:name></atom:author>\n" +
			"<Placemark>\n" +
			"\t<name>A road fork</name>\n" +
			"\t<description><![CDATA[<!-- desc_gen:start -->\n" +
			"<table width=\"100%\">\n" +
			" <tbody>\n" +
			" <!-- desc_user:start -->\n" +
			" <font color=\"black\">\n" +
			"   <tr>\n" +
			"\t<td width=\"100%\" align=\"center\">" +
			"\t<a href=\"files/p__20200409_150847.jpg\" target=\"_blank\">" +
			"<img src=\"files/p__20200409_150847.jpg\" width=\"60px\" align=\"right\" style=\"border: 3px white solid;\"></a>" +
			"Test place name</td>\n</tr>\n" +
			"\t Test user description<!-- desc_user:end --> >\n" +
			" </tbody>\n" +
			"</table>" +
			"<!-- desc_gen:end -->]]></description>\n" +
			"\t<styleUrl>#misc-sunny.png</styleUrl>\n" +
			"" +
			"\t<ExtendedData>\n" +
			"\t\t<lc:attachment>files/p__20200409_150847.jpg</lc:attachment>\n" +
			"\t</ExtendedData>\n" +
			"" +
			"\t<Point>\n" +
			"\t\t<coordinates>37.558700,55.919883,0.00</coordinates>\n" +
			"\t</Point>\n" +
			"\t<gx:TimeStamp>\n" +
			"\t\t<when>2014-11-21T00:27:31Z</when>\n" +
			"\t</gx:TimeStamp>\n" +
			"</Placemark>\n" +
			"</Document>\n" +
			"</kml>\n";
		multipartFile = new MockMultipartFile("TestPoi.kml", "TestPoi.kml", null, googleKml.getBytes(StandardCharsets.UTF_8));
		multipartDto = new MultipartDto(multipartFile);
		multipartDto.setSetPreviewSize(true);
		multipartDto.setPreviewSize(640);
		
		//WHEN
		String processedKml = xmlHandler.processKml(multipartDto);
		
		//THEN <ExtendedData> has to contain "lc" prefix with "locusmap.eu" namespace to support <lc:attachment>
		// children tags
		
		assertAll(
			() -> assertTrue(processedKml.contains(
				"xmlns:lc=\"http://www.locusmap.eu\">")),
			() -> assertTrue(processedKml.contains("<kml xmlns=\"http://www.opengis.net/kml/2.2\" " +
				"xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\" xmlns:lc=\"http://www.locusmap.eu\">")),
			() -> assertTrue(processedKml.contains(
				"<lc:attachment>files/p__20200409_150847.jpg</lc:attachment>")),
			() -> assertTrue(processedKml.contains("</ExtendedData>"))
		
		);
	}
	
}