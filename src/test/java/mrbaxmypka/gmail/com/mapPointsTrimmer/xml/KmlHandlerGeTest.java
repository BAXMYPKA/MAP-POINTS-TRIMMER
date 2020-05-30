package mrbaxmypka.gmail.com.mapPointsTrimmer.xml;

import mrbaxmypka.gmail.com.mapPointsTrimmer.entitiesDto.MultipartDto;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class KmlHandlerGeTest {
	
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
	public void header_Should_Be_Added_With_Lc_Locusmap_eu_Namespace_Prefix_If_Contain_Lc_Attachments()
		throws IOException, XMLStreamException {
		//GIVEN
		String googleKml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
			"<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" +
			"<Document>\n" +
			"\t<name>Test POIs from GoogleEarth</name>\n" +
			"\t<atom:author><atom:name>Locus (Android)</atom:name></atom:author>\n" +
			"<Placemark>\n" +
			"\t<name>A road fork</name>\n" +
			"\t<description>Test user description</description>\n" +
			"\t<styleUrl>#misc-sunny.png</styleUrl>\n" +
			"\t<ExtendedData>\n" +
			"\t\t<lc:attachment>files/p__20200409_150847.jpg</lc:attachment>\n" +
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
		multipartFile = new MockMultipartFile("GoogleEarth.kml", "GoogleEarth.kml", null, googleKml.getBytes(StandardCharsets.UTF_8));
		multipartDto = new MultipartDto(multipartFile);
		multipartDto.setSetPreviewSize(true);
		multipartDto.setPreviewSize(640);
		
		//WHEN
		String processedKml = kmlHandler.processKml(multipartDto);
		
		//THEN
		
/*
		assertAll(
			() -> assertTrue(processedKml.contains(
				"xmlns:lc=\"http://www.locusmap.eu\">")),
			() -> assertTrue(processedKml.contains("<kml xmlns=\"http://www.opengis.net/kml/2.2\" " +
				"xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\" xmlns:lc=\"http://www.locusmap.eu\">")),
			() -> assertTrue(processedKml.contains(
				"<lc:attachment>files/p__20200409_150847.jpg</lc:attachment>")),
			() -> assertTrue(processedKml.contains("</ExtendedData>"))
		
		);
*/
	}
	
}
