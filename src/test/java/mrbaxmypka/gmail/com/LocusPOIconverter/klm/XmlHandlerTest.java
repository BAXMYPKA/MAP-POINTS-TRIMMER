package mrbaxmypka.gmail.com.LocusPOIconverter.klm;

import mrbaxmypka.gmail.com.LocusPOIconverter.entitiesDto.MultipartDto;
import mrbaxmypka.gmail.com.LocusPOIconverter.utils.PathTypes;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerException;
import java.io.*;
import java.nio.charset.StandardCharsets;

class XmlHandlerTest {
	
	private static InputStream inputStream;
	private static MultipartDto multipartDto;
	private static MultipartFile multipartFile;
	private HtmlHandler htmlHandler = new HtmlHandler();
	private XmlHandler xmlHandler = new XmlHandler(htmlHandler);
	private static String locusKml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
		  "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\"\n" +
		  "xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" +
		  "<Document>\n" +
		  "\t<name>Locus17.04.2020</name>\n" +
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
		  "<font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\">\n" +
		  "<!-- desc_user:start -->\n" +
		  "<font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\"><font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\"><font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\"><font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\"><font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\"><font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\"><font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\"><font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\"><font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\"><a href=\"/storage/emulated/0/Locus/data/media/photo/Точка_1318431492316.jpg\" target=\"_blank\"><img src=\"/storage/emulated/0/Locus/data/media/photo/Точка_1318431492316.jpg\" width=\"330px\" align=\"center\"></a>Test place name</td></tr><tr><td colspan=\"1\"><hr></td></tr><tr><td><table width=\"100%\"><tr><td align=\"left\" valign=\"center\"><small><b>Высота</b></small></td><td align=\"center\" valign=\"center\">203m</td></tr>\n" +
		  "<tr><td align=\"left\" valign=\"center\"><small><b>Азимут</b></small></td><td align=\"center\" valign=\"center\">332°</td></tr>\n" +
		  "<tr><td align=\"left\" valign=\"center\"><small><b>Точность</b></small></td><td align=\"center\" valign=\"center\">10m</td></tr>\n" +
		  "<tr><td align=\"left\" valign=\"center\"><small><b>Создано</b></small></td><td align=\"center\" valign=\"center\">2012-09-18 18:46:14</td></tr>\n" +
		  "</table></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font>\n" +
		  "Test user description" +
		  "<!-- desc_user:end -->\n" +
		  "</td></tr><tr><td colspan=\"1\"><hr></td></tr><tr><td><table width=\"100%\"><tr><td align=\"left\" valign=\"center\"><small><b>Создано</b></small></td><td align=\"center\" valign=\"center\">2014-11-21 00:27:30</td></tr>\n" +
		  "</table></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font>\n" +
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
	
	private static String googleEarthKml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
		"<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\"\n" +
		"\t xmlns:kml=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\"\n" +
		"\t xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.google.com/kml/ext/2.2 \">\n" +
		"\t<Document>\n" +
		"\t\t<name>Мои метки 19.08.2019.kmz</name>\n" +
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
		"\t\t\t<name>Мои метки</name>\n" +
		"\t\t\t<open>1</open>\n" +
		"\t\t\t<Style>\n" +
		"\t\t\t\t<ListStyle>\n" +
		"\t\t\t\t\t<listItemType>check</listItemType>\n" +
		"\t\t\t\t\t<ItemIcon>\n" +
		"\t\t\t\t\t\t<state>open</state>\n" +
		"\t\t\t\t\t\t<href>C:/Users/Sersus/Programs/КАРТОГРАФИЯ/Locus/data-media-photo/mysavedplaces_open.png</href>\n" +
		"\t\t\t\t\t</ItemIcon>\n" +
		"\t\t\t\t\t<ItemIcon>\n" +
		"\t\t\t\t\t\t<state>closed</state>\n" +
		"\t\t\t\t\t\t<href>C:/Users/Sersus/Programs/КАРТОГРАФИЯ/Locus/data-media-photo/mysavedplaces_closed.png\n" +
		"\t\t\t\t\t\t</href>\n" +
		"\t\t\t\t\t</ItemIcon>\n" +
		"\t\t\t\t\t<bgColor>00ffffff</bgColor>\n" +
		"\t\t\t\t\t<maxSnippetLines>2</maxSnippetLines>\n" +
		"\t\t\t\t</ListStyle>\n" +
		"\t\t\t</Style>\n" +
		"\t\t\t<Document>\n" +
		"\t\t\t\t<name>ИзбранноеИзЛокус19.08.2019</name>\n" +
		"\t\t\t\t<atom:author>\n" +
		"\t\t\t\t\t<atom:name>Locus (Android)</atom:name>\n" +
		"\t\t\t\t</atom:author>\n" +
		"\t\t\t\t<Style id=\"misc-sunny.png\">\n" +
		"\t\t\t\t\t<IconStyle>\n" +
		"\t\t\t\t\t\t<color>99ffffff</color>\n" +
		"\t\t\t\t\t\t<scale>0.5</scale>\n" +
		"\t\t\t\t\t\t<Icon>\n" +
		"\t\t\t\t\t\t\t<href>C:/Users/Sersus/Programs/КАРТОГРАФИЯ/Locus/data-media-photo/misc-sunny.png</href>\n" +
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
		"\t\t\t\t\t\t\t<href>C:/Users/Sersus/Programs/КАРТОГРАФИЯ/Locus/data-media-photo/misc-sunny.png</href>\n" +
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
		"\t\t\t\t</StyleMap>\n"+
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
		"\t\t\t</Folder>\n"+
		"\t</Document>\n" +
		"</kml>\n";
	
	@Test
	@Disabled
	public void valid_LocusPOI_Kml_Should_Be_Validated() throws IOException {
		//GIVEN Only to validate xml
		inputStream = new FileInputStream("src/test/java/resources/LocusTestPois.kml");
		multipartFile = new MockMultipartFile(
			  "LocusTestPois.kml", "LocusTestPois.kml", null, inputStream);
		multipartDto = new MultipartDto(
			  multipartFile, false, false, false, true, false, null, null, false, null, false);
		
		//WHEN
		
		Assertions.assertDoesNotThrow(() -> xmlHandler.processKml(multipartDto));
	}
	
	@Test
	@Disabled("Should be rewritten to set paths in main XML body")
	public void setPath_In_Description_Cdata_Should_Replace_All_Href_And_Src()
		  throws IOException, ParserConfigurationException, SAXException, XMLStreamException, TransformerException {
		//GIVEN
		
		String newPath = "files:/a new path/";
		inputStream = new FileInputStream("src/test/java/resources/LocusTestPois.kml");
		multipartFile = new MockMultipartFile(
			  "LocusTestPois.kml", "LocusTestPois.kml", null, inputStream);
		multipartDto = new MultipartDto(
			  multipartFile, false, false, false, false, true, PathTypes.RELATIVE, newPath, false, null, false);
		
		//WHEN
		
		String processedKml = xmlHandler.processKml(multipartDto);
		
		//THEN Check all the new href and src
		
		Assertions.assertFalse(processedKml.contains("href=\"/storage/emulated/0/Locus/data/media/photo/"));
		Assertions.assertFalse(processedKml.contains("src=\"/storage/emulated/0/Locus/data/media/photo/"));
		
		Assertions.assertTrue(processedKml.contains("href=\"files:/a new path/"));
		Assertions.assertTrue(processedKml.contains("src=\"files:/a new path/"));
	}
	
	@Test
	public void trimXml_Only_Should_Return_Only_Xml_Without_LineBreaks()
		  throws IOException, ParserConfigurationException, SAXException, XMLStreamException, TransformerException {
		//GIVEN
		
		inputStream = new ByteArrayInputStream(locusKml.getBytes(StandardCharsets.UTF_8));
		multipartFile = new MockMultipartFile(
			  "LocusTestPois.kml", "LocusTestPois.kml", null, inputStream);
		multipartDto = new MultipartDto(
			  multipartFile, false, true, false, false, false, null, null, false, null, false);
		
		//WHEN
		
		String processedKml = xmlHandler.processKml(multipartDto);
		
		//THEN xml tags are without whitespaces but CDATA starts and ends with them
		
		Assertions.assertAll(
			  () -> Assertions.assertTrue(processedKml.contains("<description><![CDATA[\n")),
			  () -> Assertions.assertTrue(processedKml.contains("\n<!--desc_gen:start-->")),
			  () -> Assertions.assertTrue(processedKml.contains("<!--desc_gen:end-->\n"))
		);
		
		Assertions.assertAll(
			  () -> Assertions.assertFalse(processedKml.contains("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n")),
			  () -> Assertions.assertFalse(processedKml.contains("<Document>\n")),
			  () -> Assertions.assertFalse(processedKml.contains("\t<name>Избранное_Locus17.04.2020</name>\n"))
		);
	}
}