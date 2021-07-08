package mrbaxmypka.gmail.com.mapPointsTrimmer.xml;

import mrbaxmypka.gmail.com.mapPointsTrimmer.entitiesDto.MultipartDto;
import mrbaxmypka.gmail.com.mapPointsTrimmer.services.FileService;
import mrbaxmypka.gmail.com.mapPointsTrimmer.services.GoogleIconsService;
import mrbaxmypka.gmail.com.mapPointsTrimmer.utils.GoogleIconsCache;
import org.junit.jupiter.api.Test;
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

class XmlHandlerTest {
	
	private FileService fileService = new FileService();
	private GoogleIconsCache googleIconsCache = new GoogleIconsCache();
	private GoogleIconsService googleIconsService = new GoogleIconsService(googleIconsCache);
	private HtmlHandler htmlHandler = new HtmlHandler(fileService);
	private LocusMapHandler locusMapHandler;
	private XmlHandler xmlHandler = new KmlHandler(htmlHandler, googleIconsService, fileService, locusMapHandler);
	
	@Test
	public void kml_header_Should_Be_Added_With_Lc_Locusmap_Namespace_Prefix_If_Contain_Lc_Attachments_Without_Namespace_In_ExtendedData_And_Attachment()
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
		MultipartFile multipartFile = new MockMultipartFile("TestPoi.kml", "TestPoi.kml", null, locusKml.getBytes(StandardCharsets.UTF_8));
		MultipartDto multipartDto = new MultipartDto(multipartFile);
		multipartDto.setAsAttachmentInLocus(true);
		
		//WHEN
		String processedKml = xmlHandler.processXml(multipartDto.getMultipartFile().getInputStream(), multipartDto);
//		System.out.println(processedKml);
		
		//THEN <kml (...) xmlns:lc="http://www.locusmap.eu"> namespace in the header has to be created
		assertAll(
				() -> assertTrue(processedKml.contains(
						"<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:kml=\"http://www.opengis.net/kml/2.2\"" +
								" xmlns:lc=\"http://www.locusmap.eu\">")),
				() -> assertTrue(processedKml.contains("<lc:attachment>files/p__20180907_123842.jpg</lc:attachment>"))
		);
	}
	
	@Test
	public void kml_header_Should_Be_Added_With_Xsi_Namespace_Prefix_If_Contain_Inner_Document_Tag_With_Xsi_SchemaLocation_Namespace()
			throws IOException, TransformerException, SAXException, ParserConfigurationException {
		//GIVEN .kml without bounded "xsi" and "lc" attributes for two potential SAXParseExceptions
		String googleKmlForTwoExceptions = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:kml=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" +
				"\t<Document>\n" +
				"\t\t<Style>\n" +
				"\t\t\t<ListStyle>\n" +
				"\t\t\t\t<listItemType>check</listItemType>\n" +
				"\t\t\t\t<bgColor>00ffffff</bgColor>\n" +
				"\t\t\t\t<maxSnippetLines>2</maxSnippetLines>\n" +
				"\t\t\t</ListStyle>\n" +
				"\t\t</Style>\n" +
				"\t\t<name>Test double SAXParseException</name>\n" +
				"\t\t<Document xsi:schemaLocation=\"http://www.opengis.net/kml/2.2 http://schemas.opengis.net/kml/2.2.0/ogckml22.xsd\">\n" +
				"\t\t\t<name>Хотчинская петля (Талдом-Савелово)(зап,грунт, чужой)</name>\n" +
				"\t\t</Document>\n" +
				"\t\t<Placemark>\n" +
				"\t\t\t<name>2015-04-10 14:27:17</name>\n" +
				"\t\t\t<description/>\n" +
				"\t\t\t<styleUrl>#styleMap1</styleUrl>\n" +
				"\t\t\t<ExtendedData>\n" +
				"\t\t\t\t<lc:attachment>files/_1428665237633.jpg</lc:attachment>\n" +
				"\t\t\t</ExtendedData>\n" +
				"\t\t</Placemark>\n" +
				"\t</Document>\n" +
				"</kml>\n";
		MultipartFile multipartFile = new MockMultipartFile("TestPoi.kml", "TestPoi.kml", null, googleKmlForTwoExceptions.getBytes(StandardCharsets.UTF_8));
		MultipartDto multipartDto = new MultipartDto(multipartFile);
		multipartDto.setAsAttachmentInLocus(true);
		multipartDto.setPointIconSize(300);
		
		//WHEN
		
		//THEN
		//No SAXParseExceptions should be thrown
		//And <kml (...) xmlns:lc="http://www.locusmap.eu" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"> namespaces in the header have to be created
		assertDoesNotThrow(
				() -> {
					Document document =
							xmlHandler.getDocument(new ByteArrayInputStream(googleKmlForTwoExceptions.getBytes(StandardCharsets.UTF_8)));
					String processedKml = xmlHandler.writeTransformedDocument(document, true);
//					System.out.println(processedKml);
					assertTrue(processedKml.contains(
							"kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:kml=\"http://www.opengis.net/kml/2.2\"" +
									" xmlns:lc=\"http://www.locusmap.eu\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""));
				}
		);
	}
}