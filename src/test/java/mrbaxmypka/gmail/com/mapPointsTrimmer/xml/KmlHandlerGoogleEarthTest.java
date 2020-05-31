package mrbaxmypka.gmail.com.mapPointsTrimmer.xml;

import mrbaxmypka.gmail.com.mapPointsTrimmer.entitiesDto.MultipartDto;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class KmlHandlerGoogleEarthTest {
	
	private static MultipartDto multipartDto;
	private static MultipartFile multipartFile;
	private HtmlHandler htmlHandler = new HtmlHandler();
	private KmlHandler kmlHandler = new KmlHandler(htmlHandler);
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
	
	@Test
	public void header_Should_Be_Added_With_Lc_Locusmap_Namespace_Prefix_If_Contain_Lc_Attachments_Without_Namespace_In_ExtendedData_And_Attachment()
		throws IOException, ParserConfigurationException, SAXException, TransformerException {
		//GIVEN kml with <ExtendedData> without namespace BUT with <lc:attachment>
		String locusKml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:kml=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" +
			"<Document>\n" +
			"\t<name>FullTestKmzExport01</name>\n" +
			"\t<open>1</open>\n" +
			"\t<atom:author><atom:name>Locus (Android)</atom:name></atom:author>" +
			"<Placemark>\n" +
			"\t\t<name>2018-09-07 12:38:41</name>\n" +
			"\t\t<description><![CDATA[<!-- desc_gen:start -->\n" +
			"<div> <!-- desc_user:start --> \n" +
			" <table width=\"100%\" style=\"color:black\"> \n" +
			"  <tbody> \n" +
			"   <tr> \n" +
			"    <td align=\"center\"><a href=\"file:///C:/Users/%username%/Documents/TEST%20POI/FullTestKmzExport01/p__20180907_123842.jpg\" target=\"_blank\">" +
			"<img src=\"files/p__20180907_123842.jpg\" width=\"640px\" align=\"right\" style=\"border:3px white solid;\"></a></td> \n" +
			"   </tr> \n" +
			"   <tr> \n" +
			"    <td colspan=\"2\"> \n" +
			"     <hr></td> \n" +
			"   </tr> \n" +
			"   <tr> \n" +
			"    <td align=\"left\" valign=\"center\"><small><b>Высота</b></small></td> \n" +
			"    <td align=\"center\" valign=\"center\">146 m</td> \n" +
			"   </tr> \n" +
			"   <tr> \n" +
			"    <td align=\"left\" valign=\"center\"><small><b>Создано</b></small></td> \n" +
			"    <td align=\"center\" valign=\"center\">2018-09-07 12:39:01</td> \n" +
			"   </tr> \n" +
			"   <tr> \n" +
			"    <td colspan=\"2\"> \n" +
			"     <hr></td> \n" +
			"   </tr> \n" +
			"  </tbody> \n" +
			" </table><!-- desc_user:end --> \n" +
			"</div><!-- desc_gen:end -->]]></description>\n" +
			"\t\t<gx:TimeStamp><when>2018-09-07T12:38:41Z</when>\n" +
			"</gx:TimeStamp>\n" +
			"\t\t<styleUrl>#sport-cyclingsport.png1</styleUrl>\n" +
			"\t\t<ExtendedData>\n" +
			"\t\t\t<lc:attachment>files/p__20180907_123842.jpg</lc:attachment>\n" +
			"\t\t</ExtendedData>\n" +
			"\t\t<Point>\n" +
			"\t\t\t<coordinates>38.783625,55.964572,146</coordinates>\n" +
			"\t\t</Point>\n" +
			"\t</Placemark>" +
			"</Document>\n" +
			"</kml>";
		multipartFile = new MockMultipartFile("TestPoi.kml", "TestPoi.kml", null, locusKml.getBytes(StandardCharsets.UTF_8));
		multipartDto = new MultipartDto(multipartFile);
		multipartDto.setAsAttachmentInLocus(true);
		
		//WHEN
		String processedKml = kmlHandler.processXml(multipartDto);
		
		//THEN <kml (...) xmlns:lc="http://www.locusmap.eu"> namespace in the header has to be created
		assertAll(
			() -> assertTrue(processedKml.contains(
				"<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:kml=\"http://www.opengis.net/kml/2.2\"" +
					" xmlns:lc=\"http://www.locusmap.eu\">")),
			() -> assertTrue(processedKml.contains(
				"<ExtendedData>\n" +
					"\t\t\t<lc:attachment>files/p__20180907_123842.jpg</lc:attachment>\n" +
					"\t\t</ExtendedData>"))
		);
	}
	
}
