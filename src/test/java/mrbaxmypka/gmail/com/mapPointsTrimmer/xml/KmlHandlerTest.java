package mrbaxmypka.gmail.com.mapPointsTrimmer.xml;

import mrbaxmypka.gmail.com.mapPointsTrimmer.entitiesDto.MultipartMainDto;
import mrbaxmypka.gmail.com.mapPointsTrimmer.services.FileService;
import mrbaxmypka.gmail.com.mapPointsTrimmer.services.GoogleIconsService;
import mrbaxmypka.gmail.com.mapPointsTrimmer.utils.GoogleIconsCache;
import mrbaxmypka.gmail.com.mapPointsTrimmer.utils.PreviewSizeUnits;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.MessageSource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class KmlHandlerTest {
	
	private static InputStream inputStream;
	private static MultipartMainDto multipartMainDto;
	private static MultipartFile multipartFile;
	private static MessageSource messageSource;
	private static Resource resource;
	private static ResourceLoader resourceLoader;
	private static FileService fileService;
	private static GoogleIconsCache googleIconsCache;
	private static HtmlHandler htmlHandler;
	private static GoogleIconsService googleIconsService;
	private KmlHandler kmlHandler;
	private static final String CLASSPATH_TO_DIRECTORY = "classpath:static/pictograms";
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
	
	@BeforeEach
	public void beforeEach() throws IOException {
		messageSource = Mockito.mock(MessageSource.class);
		resourceLoader = Mockito.mock(ResourceLoader.class);
		resource = Mockito.mock(Resource.class);
		Mockito.when(resourceLoader.getResource(CLASSPATH_TO_DIRECTORY)).thenReturn(resource);
		Mockito.when(resource.getInputStream()).thenReturn(new ByteArrayInputStream("Pictogram1.png".getBytes(StandardCharsets.UTF_8)));
		fileService = new FileService(messageSource);
		
		htmlHandler = new HtmlHandler(fileService);
		googleIconsCache = new GoogleIconsCache();
		googleIconsService = new GoogleIconsService(googleIconsCache);
		kmlHandler = new KmlHandler(htmlHandler, googleIconsService, fileService);
	}
	
	@Test
	public void setPath_Should_Replace_All_Href_Tags_Content_In_Xml_Body()
			throws IOException, XMLStreamException, ParserConfigurationException, SAXException, TransformerException, ClassNotFoundException, InterruptedException {
		//GIVEN
		String newPath = "C:\\MyPoi\\MyPoiImages";
		multipartFile = new MockMultipartFile(
			"LocusTestPoi.kml", "LocusTestPoi.kml", null, locusKml.getBytes(StandardCharsets.UTF_8));
		multipartMainDto = new MultipartMainDto(multipartFile);
		multipartMainDto.setPath(newPath);
		multipartMainDto.setPathType("absolute");
		
		//WHEN
		String processedKml = kmlHandler.processXml(multipartMainDto.getMultipartFile().getInputStream(), multipartMainDto);
		
		//THEN
		assertTrue(processedKml.contains("<href>file:///C:/MyPoi/MyPoiImages/file-sdcardLocuscacheimages1571471453728.png</href>"));
		assertTrue(processedKml.contains("<href>file:///C:/MyPoi/MyPoiImages/file-sdcardLocuscacheimages1589191676952.png</href>"));
		
		assertFalse(processedKml.contains("<href>files/file-sdcardLocuscacheimages1571471453728.png</href>"));
		assertFalse(processedKml.contains("<href>files/file-sdcardLocuscacheimages1589191676952.png</href>"));
	}
	
	/**
	 * This test used to be actual up to {@link mrbaxmypka.gmail.com.mapPointsTrimmer.MapPointsTrimmerApplication} v3.0.
	 * The following versions try to download those type of icons.
	 * =========================================================
	 * Google Earth has some special http links to icons which it treats as local.
	 * So those types of {@literal <href></href>} have not to be changed.
	 */
	@Disabled
	@Test
	public void setPath_Should_Replace_All_Href_Tags_Content_In_Xml_Body_Except_Special_GoogleEarth_Web_Links()
			throws IOException, TransformerException, SAXException, ParserConfigurationException, InterruptedException {
		//GIVEN
		multipartFile = new MockMultipartFile(
			"TestPoi.kml", "TestPoi.kml", null, locusKml.getBytes(StandardCharsets.UTF_8));
		multipartMainDto = new MultipartMainDto(multipartFile);
		multipartMainDto.setPath("C:\\MyPoi\\MyPoiImages");
		multipartMainDto.setPathType("absolute");
		
		//WHEN
		String processedKml = kmlHandler.processXml(multipartMainDto.getMultipartFile().getInputStream(), multipartMainDto);
		
		//THEN
		//Special GoogleEarth icons paths should be preserved
		assertTrue(processedKml.contains("<href>http://maps.google.com/mapfiles/kml/shapes/motorcycling.png</href>"));
	}
	
	@Test
	public void set_Paths_With_Whitespaces_Should_Set_Correct_Absolute_Path_Type_URL_Encoded()
			throws IOException, XMLStreamException, TransformerException, SAXException, ParserConfigurationException, ClassNotFoundException, InterruptedException {
		//GIVEN
		String pathWithWhitespaces = "D:\\My Folder\\My POI";
		multipartFile = new MockMultipartFile(
			"LocusTestPois.kml", "LocusTestPois.kml", null, locusKml.getBytes(StandardCharsets.UTF_8));
		multipartMainDto = new MultipartMainDto(multipartFile);
		multipartMainDto.setPath(pathWithWhitespaces);
		multipartMainDto.setPathType("absolute");
		
		//WHEN
		String processedHtml = kmlHandler.processXml(multipartMainDto.getMultipartFile().getInputStream(), multipartMainDto);
		
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
			throws IOException, XMLStreamException, TransformerException, SAXException, ParserConfigurationException, InterruptedException {
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
			"\t Test user description<!-- desc_user:end --> \n" +
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
		multipartMainDto = new MultipartMainDto(multipartFile);
		multipartMainDto.setTrimXml(true);
		
		//WHEN
		String processedKml = kmlHandler.processXml(multipartMainDto.getMultipartFile().getInputStream(), multipartMainDto);
//		System.out.println(processedKml);
		
		//THEN xml tags are without whitespaces but CDATA starts and ends with them
		assertAll(
			() -> assertTrue(processedKml.contains(
				"<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"no\"?><kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\"><Document><name>Test POIs from Locus</name><atom:author><atom:name>Locus (Android)</atom:name></atom:author><Placemark><name>A road fork</name><description>")),
			() -> assertTrue(processedKml.contains(
				"</description><styleUrl>#misc-sunny.png</styleUrl><Point><coordinates>37.558700,55.919883,0.00</coordinates></Point><gx:TimeStamp><when>2014-11-21T00:27:31Z</when></gx:TimeStamp></Placemark></Document></kml>")));
		
		Pattern newLinePattern = Pattern.compile("\n", Pattern.MULTILINE);
		Matcher newLineMatcher = newLinePattern.matcher(processedKml);
		//The resulting string should contain at least 5 line breaks (as <description> has them)
		assertTrue(newLineMatcher.results().count() >= 5);
		
		assertFalse(processedKml.contains(
			"<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
				"<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\"\n" +
				"xmlns:atom=\"http://www.w3.org/2005/Atom\">\n"));
	}
	
	@Test
	public void locusAsAttachment_Should_Create_ExtendedData_For_Attachments_With_Src_From_Description()
			throws IOException, ParserConfigurationException, SAXException, TransformerException, InterruptedException {
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
			"\t Test user description<!-- desc_user:end --> \n" +
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
		multipartMainDto = new MultipartMainDto(multipartFile);
		multipartMainDto.setAsAttachmentInLocus(true);
		
		//WHEN
		String processedKml = kmlHandler.processXml(multipartMainDto.getMultipartFile().getInputStream(), multipartMainDto);

		//THEN <ExtendedData> ans <lc:attachment xmlns:lc="http://www.locusmap.eu"> has to be created
		assertAll(
			() -> assertTrue(processedKml.contains(
				"<ExtendedData>")),
			() -> assertTrue(processedKml.contains(
				"<lc:attachment xmlns:lc=\"http://www.locusmap.eu\">files/_1318431492316.jpg</lc:attachment>")),
			() -> assertTrue(processedKml.contains(
				"</ExtendedData>"))
		);
	}
	
	@Test
	public void locusAsAttachment_Should_Just_Replace_Existing_Src_From_Description()
			throws IOException, ParserConfigurationException, SAXException, XMLStreamException, TransformerException, InterruptedException {
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
			"\t Test user description<!-- desc_user:end --> \n" +
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
		multipartMainDto = new MultipartMainDto(multipartFile);
		multipartMainDto.setAsAttachmentInLocus(true);
		
		//WHEN
		String processedKml = kmlHandler.processXml(multipartMainDto.getMultipartFile().getInputStream(), multipartMainDto);

		//THEN <lc:attachments> text has to be replaced from description one
		assertTrue(processedKml.contains("<ExtendedData xmlns:lc=\"http://www.locusmap.eu\""));//Just a check
		assertTrue(processedKml.contains("<lc:attachment>C:/MyPOI/_1318431492316.jpg</lc:attachment>"));
		assertFalse(processedKml.contains("<lc:attachment>files/_1318431492316.jpg</lc:attachment>"));
	}
	
	@Test
	public void locusAsAttachment_Should_Add_More_Attachments_Src_From_Description()
			throws IOException, ParserConfigurationException, SAXException, XMLStreamException, TransformerException, InterruptedException {
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
		multipartMainDto = new MultipartMainDto(multipartFile);
		multipartMainDto.setAsAttachmentInLocus(true);
		
		//WHEN
		String processedKml = kmlHandler.processXml(multipartMainDto.getMultipartFile().getInputStream(), multipartMainDto);

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
			throws IOException, XMLStreamException, TransformerException, SAXException, ParserConfigurationException, InterruptedException {
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
		multipartMainDto = new MultipartMainDto(multipartFile);
		multipartMainDto.setAsAttachmentInLocus(true);
		multipartMainDto.setPreviewSize(640);
		multipartMainDto.setPreviewSizeUnit(PreviewSizeUnits.PIXELS);
		
		//WHEN
		String processedKml = kmlHandler.processXml(multipartMainDto.getMultipartFile().getInputStream(), multipartMainDto);

		//THEN <ExtendedData> has to be filled with new <lc:attachment>'s with src to images from description
		assertAll(
			() -> assertTrue(processedKml.contains("<ExtendedData xmlns:lc=\"http://www.locusmap.eu\">")),
			() -> assertTrue(processedKml.contains("<lc:attachment>files/p__20200511_130333.jpg</lc:attachment>")),
			() -> assertTrue(processedKml.contains("<lc:attachment>files/p__20200511_125332.jpg</lc:attachment>"))
		);
	}
	
	@Test
	public void locusAsAttachment_Should_Contain_Only_Relative_And_Absolute_Type_Of_Href_When_PathTypes_Undefined()
			throws IOException, XMLStreamException, TransformerException, SAXException, ParserConfigurationException, InterruptedException {
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
		multipartMainDto = new MultipartMainDto(multipartFile);
		multipartMainDto.setAsAttachmentInLocus(true);
		
		//WHEN
		String processedKml = kmlHandler.processXml(multipartMainDto.getMultipartFile().getInputStream(), multipartMainDto);
		
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
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////// GOOGLE EARTH TESTS ////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////
	
}