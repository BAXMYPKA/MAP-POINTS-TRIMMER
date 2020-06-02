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

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GoogleEarthHandlerTest {
	
	private MultipartDto multipartDto;
	private MultipartFile multipartFile;
	private GoogleEarthHandler googleEarthHandler = new GoogleEarthHandler();
	private KmlHandler kmlHandler = new KmlHandler(new HtmlHandler(), googleEarthHandler);
	
	@ParameterizedTest
	@ValueSource(ints = {12, 57, 111})
	public void percentage_PointIconSize_Should_Insert_Scale_To_IconStyle(int percentageSize)
		  throws IOException, ParserConfigurationException, SAXException, TransformerException {
		//GIVEN kml with <ExtendedData> without namespace BUT with <lc:attachment>
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
			  "\t\t<name>Test placemart</name>\n" +
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
		
		//THEN
		String scale = multipartDto.getPointIconSizeScaled().toString();
		
		assertTrue(containsElement(document, "IconStyle", 2, "scale", scale));
	}
	
	
	private Document getDocument(MultipartDto multipartDto) throws ParserConfigurationException, IOException, SAXException {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document document = documentBuilder.parse(multipartDto.getMultipartFile().getInputStream());
		document.normalizeDocument();
		return document;
	}
	
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