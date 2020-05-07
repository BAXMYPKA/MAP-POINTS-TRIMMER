package mrbaxmypka.gmail.com.LocusPOIconverter.klm;

import mrbaxmypka.gmail.com.LocusPOIconverter.entitiesDto.MultipartDto;
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
import java.util.regex.Pattern;

class XmlHandlerTest {
	
	private static InputStream inputStream;
	private static MultipartDto multipartDto;
	private static MultipartFile multipartFile;
	private HtmlHandler htmlHandler = new HtmlHandler();
	private XmlHandler xmlHandler = new XmlHandler(htmlHandler);
	
	@Test
	@Disabled
	public void valid_LocusPOI_Kml_Should_Be_Validated() throws IOException {
		//GIVEN Only to validate xml
		inputStream = new FileInputStream("src/test/java/resources/LocusTestPois.kml");
		multipartFile = new MockMultipartFile(
			"LocusTestPois.kml", "LocusTestPois.kml", null, inputStream);
		multipartDto = new MultipartDto(
			multipartFile, false, false, false, true, false, null, false, null);
		
		//WHEN
		
		Assertions.assertDoesNotThrow(() -> xmlHandler.processKml(multipartDto));
	}
	
	@Test
	public void setPath_In_Description_Cdata_Should_Replace_All_Href_And_Src()
		throws IOException, ParserConfigurationException, SAXException, XMLStreamException, TransformerException {
		//GIVEN
		
		String newPath = "files:/a new path/";
		inputStream = new FileInputStream("src/test/java/resources/LocusTestPois.kml");
		multipartFile = new MockMultipartFile(
			"LocusTestPois.kml", "LocusTestPois.kml", null, inputStream);
		multipartDto = new MultipartDto(
			multipartFile, false, false, false, false, true, newPath, false, null);
		
		//WHEN
		
		String processedKml = xmlHandler.processKml(multipartDto);
		
		//THEN Check all the new href and src
		
		Assertions.assertFalse(processedKml.contains("href=\"/storage/emulated/0/Locus/data/media/photo/"));
		Assertions.assertFalse(processedKml.contains("src=\"/storage/emulated/0/Locus/data/media/photo/"));
		
		Assertions.assertTrue(processedKml.contains("href=\"files:/a new path/"));
		Assertions.assertTrue(processedKml.contains("src=\"files:/a new path/"));
	}
	
	@Test
	public void setPath_In_Description_Cdata_Should_Preserve_All_Filenames_In_Href_And_Src()
		throws IOException, ParserConfigurationException, SAXException, XMLStreamException, TransformerException {
		//GIVEN
		
		String newPath = "files:/a new path/";
		inputStream = new FileInputStream("src/test/java/resources/LocusTestPois.kml");
		multipartFile = new MockMultipartFile(
			"LocusTestPois.kml", "LocusTestPois.kml", null, inputStream);
		multipartDto = new MultipartDto(
			multipartFile, false, false, false, false, true, newPath, false, null);
		
		//WHEN
		
		String processedKml = xmlHandler.processKml(multipartDto);
		
		//THEN Check a couple of new filenames
		
		Assertions.assertFalse(processedKml.contains("href=\"/storage/emulated/0/Locus/data/media/photo/p__20200409_150847.jpg\""));
		Assertions.assertFalse(processedKml.contains("src=\"/storage/emulated/0/Locus/data/media/photo/p__20200409_150847.jpg\""));
		Assertions.assertFalse(processedKml.contains("href=\"/storage/emulated/0/Locus/data/media/photo/p__20180523_123601.jpg\""));
		Assertions.assertFalse(processedKml.contains("src=\"/storage/emulated/0/Locus/data/media/photo/p__20180523_123601.jpg\""));
		
		Assertions.assertTrue(processedKml.contains("href=\"files:/a new path/p__20200409_150847.jpg\""));
		Assertions.assertTrue(processedKml.contains("img src=\"files:/a new path/p__20200409_150847.jpg\""));
		Assertions.assertTrue(processedKml.contains("href=\"files:/a new path/p__20180523_123601.jpg\""));
		Assertions.assertTrue(processedKml.contains("img src=\"files:/a new path/p__20180523_123601.jpg\""));
	}
	
	@Test
	public void setPreviewSize_Should_Set_All_Img_Widths_Attributes()
		throws IOException, ParserConfigurationException, SAXException, XMLStreamException, TransformerException {
		//GIVEN
		
		inputStream = new FileInputStream("src/test/java/resources/LocusTestPois.kml");
		multipartFile = new MockMultipartFile(
			"LocusTestPois.kml", "LocusTestPois.kml", null, inputStream);
		multipartDto = new MultipartDto(
			multipartFile, false, false, false, false, true, null, true, 800);
		
		//WHEN
		
		String processedKml = xmlHandler.processKml(multipartDto);
		
		//THEN
		
		Assertions.assertFalse(processedKml.contains("width=\"330px\""));
		Assertions.assertFalse(processedKml.contains("width=\"60px\""));
		
		Assertions.assertTrue(processedKml.contains("width=\"800px\""));
	}
	
	@Test
	public void setPreviewSize_With_Null_Integer_Should_Set_All_Img_Widths_Attributes_To_0()
		throws IOException, ParserConfigurationException, SAXException, XMLStreamException, TransformerException {
		//GIVEN
		
		inputStream = new FileInputStream("src/test/java/resources/LocusTestPois.kml");
		multipartFile = new MockMultipartFile(
			"LocusTestPois.kml", "LocusTestPois.kml", null, inputStream);
		multipartDto = new MultipartDto(
			multipartFile, false, false, false, false, false, null, true, null);
		
		//WHEN
		
		String processedKml = xmlHandler.processKml(multipartDto);
		
		//THEN
		
		Assertions.assertFalse(processedKml.contains("width=\"330px\""));
		Assertions.assertFalse(processedKml.contains("width=\"60px\""));
		
		Assertions.assertTrue(processedKml.contains("width=\"0px\""));
	}
	
	@Test
	public void setTrimDescriptions_Should_Trim_All_Whitespaces_And_Write_Only_Descriptions_Inline()
		throws IOException, ParserConfigurationException, SAXException, XMLStreamException, TransformerException {
		//GIVEN
		
		inputStream = new FileInputStream("src/test/java/resources/LocusTestPois.kml");
		multipartFile = new MockMultipartFile(
			"LocusTestPois.kml", "LocusTestPois.kml", null, inputStream);
		multipartDto = new MultipartDto(
			multipartFile, true, false, false, false, false, null, true, null);
		
		//WHEN
		
		String processedKml = xmlHandler.processKml(multipartDto);
		
		//THEN
		
		//Doesn't contain new strings
		Assertions.assertFalse(Pattern.matches(".*\\n.*", processedKml));
		//Doesn't contain 2 or more whitespaces in a row
		Assertions.assertFalse(Pattern.matches(".*\\s{2,}.*", processedKml));
	}
	
	@Test
	public void clearDescriptions_Should_Return_Single_Table_With_Earliest_DateTimes_And_Full_Descriptions()
		throws IOException, ParserConfigurationException, SAXException, XMLStreamException, TransformerException {
		//GIVEN
		
		inputStream = new FileInputStream("src/test/java/resources/LocusTestPois.kml");
		multipartFile = new MockMultipartFile(
			"LocusTestPois.kml", "LocusTestPois.kml", null, inputStream);
		multipartDto = new MultipartDto(
			multipartFile, false, false, true, false, false, null, false, null);
		
		//WHEN
		
		String processedKml = xmlHandler.processKml(multipartDto);
		
		//THEN
		Assertions.assertFalse(processedKml.contains("2014-07-18 17:23:20"));
		Assertions.assertFalse(processedKml.contains("2014-09-18 16:16:44"));
		Assertions.assertFalse(processedKml.contains("2014-11-21 00:27:31"));
		
		Assertions.assertTrue(processedKml.contains(
			"2014-05-10 16:33:59"));
		Assertions.assertTrue(processedKml.contains(
			"<td align=\"center\" valign=\"center\">177 m</td>"));
		Assertions.assertTrue(processedKml.contains(
			"<td align=\"center\" valign=\"center\">120°</td>"));
		Assertions.assertTrue(processedKml.contains(
			"<td align=\"center\" valign=\"center\">222 m</td>"));
	}
	
	@Test
	public void trimXml_Only_Should_Return_Only_Xml_Without_LineBreaks()
		throws IOException, ParserConfigurationException, SAXException, XMLStreamException, TransformerException {
		//GIVEN
		
		String testKml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
			"<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\"\n" +
			"xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" +
			"<Document>\n" +
			"\t<name>Избранное_Locus17.04.2020</name>\n" +
			"\t<atom:author><atom:name>Locus (Android)</atom:name></atom:author>\n" +
			"\t<Style id=\"file:///sdcard/Locus/cache/images/1571471453728\">\n" +
			"\t\t<IconStyle>\n" +
			"\t\t\t<Icon><href>files/file-sdcardLocuscacheimages1571471453728.png</href></Icon>\n" +
			"\t\t\t<hotSpot x=\"0.5\" y=\"0.0\" xunits=\"fraction\" yunits=\"fraction\" />\n" +
			"\t\t</IconStyle>\n" +
			"\t</Style>\n" +
			"<Placemark>\n" +
			"\t<name>Грунтовая развилка</name>\n" +
			"\t<description><![CDATA[<!-- desc_gen:start -->\n" +
			"<font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\">\n" +
			"<!-- desc_user:start -->\n" +
			"<font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\"><font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\"><font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\"><font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\"><font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\"><font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\"><font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\"><font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\"><font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\"><a href=\"/storage/emulated/0/Locus/data/media/photo/Точка_1318431492316.jpg\" target=\"_blank\"><img src=\"/storage/emulated/0/Locus/data/media/photo/Точка_1318431492316.jpg\" width=\"330px\" align=\"center\"></a>Архангельское-Тюриково</td></tr><tr><td colspan=\"1\"><hr></td></tr><tr><td><table width=\"100%\"><tr><td align=\"left\" valign=\"center\"><small><b>Высота</b></small></td><td align=\"center\" valign=\"center\">203m</td></tr>\n" +
			"<tr><td align=\"left\" valign=\"center\"><small><b>Азимут</b></small></td><td align=\"center\" valign=\"center\">332°</td></tr>\n" +
			"<tr><td align=\"left\" valign=\"center\"><small><b>Точность</b></small></td><td align=\"center\" valign=\"center\">10m</td></tr>\n" +
			"<tr><td align=\"left\" valign=\"center\"><small><b>Создано</b></small></td><td align=\"center\" valign=\"center\">2012-09-18 18:46:14</td></tr>\n" +
			"</table></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font>\n" +
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
			"<Placemark>\n" +
			"\t<name>2020-04-09 15:08:47</name>\n" +
			"\t<description><![CDATA[<!-- desc_gen:start -->\n" +
			"<font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\"><a href=\"files/p__20200409_150847.jpg\" target=\"_blank\"><img src=\"files/p__20200409_150847.jpg\" width=\"60px\" align=\"right\" style=\"border: 3px white solid;\"></a><br /><br /></td></tr><tr><td colspan=\"1\"><hr></td></tr><tr><td><table width=\"100%\"><tr><td align=\"left\" valign=\"center\"><small><b>Высота</b></small></td><td align=\"center\" valign=\"center\">228 m</td></tr>\n" +
			"<tr><td align=\"left\" valign=\"center\"><small><b>Создано</b></small></td><td align=\"center\" valign=\"center\">2020-04-09 15:09:15</td></tr>\n" +
			"</table></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font>\n" +
			"<!-- desc_gen:end -->]]></description>\n" +
			"\t<styleUrl>#file:///sdcard/Locus/cache/images/1586434138689</styleUrl>\n" +
			"\t<ExtendedData xmlns:lc=\"http://www.locusmap.eu\">\n" +
			"\t\t<lc:attachment>files/p__20200409_150847.jpg</lc:attachment>\n" +
			"\t</ExtendedData>\n" +
			">\t<Point>\n" +
			"\t\t<coordinates>38.216408,56.266199,228.00</coordinates>\n" +
			"\t</Point>\n" +
			"\t<gx:TimeStamp>\n" +
			"\t\t<when>2020-04-09T15:08:47Z</when>\n" +
			"\t</gx:TimeStamp>\n" +
			"</Placemark>\n" +
			"</Document>\n" +
			"</kml>\n";
		
		inputStream = new ByteArrayInputStream(testKml.getBytes(StandardCharsets.UTF_8));
		multipartFile = new MockMultipartFile(
			"LocusTestPois.kml", "LocusTestPois.kml", null, inputStream);
		multipartDto = new MultipartDto(
			multipartFile, false, true, false, false, false, null, false, null);
		
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
	
	
	@Test
	public void all_Conditions_Enabled_Should_Return_Valid_Cdata()
		throws IOException, ParserConfigurationException, SAXException, XMLStreamException, TransformerException {
		//GIVEN
		
		String newPath = "files:/a new path/";
		
		inputStream = new FileInputStream("src/test/java/resources/LocusTestPois.kml");
		multipartFile = new MockMultipartFile(
			"LocusTestPois.kml", "LocusTestPois.kml", null, inputStream);
		multipartDto = new MultipartDto(
			multipartFile, true, true, true, false, true, newPath, true, 800);
		
		//WHEN
		
		String processedKml = xmlHandler.processKml(multipartDto);
		
		//THEN
		
		Assertions.assertAll(
			() -> Assertions.assertFalse(processedKml.contains("2014-07-18 17:23:20")),
			() -> Assertions.assertFalse(processedKml.contains("2014-09-18 16:16:44")),
			() -> Assertions.assertFalse(processedKml.contains("2014-11-21 00:27:31")),
			
			() -> Assertions.assertTrue(processedKml.contains("<td align=\"center\" valign=\"center\">120°</td>")),
			() -> Assertions.assertTrue(processedKml.contains("<td align=\"center\" valign=\"center\">222 m</td>"))
		
		);
		
		Assertions.assertAll(
			() -> Assertions.assertTrue(processedKml
				.contains("href=\"files:/a new path/p__20200409_150847.jpg\"")),
			() -> Assertions.assertTrue(processedKml
				.contains("img src=\"files:/a new path/p__20200409_150847.jpg\"")),
			() -> Assertions.assertTrue(processedKml
				.contains("href=\"files:/a new path/p__20180523_123601.jpg\"")),
			() -> Assertions.assertTrue(processedKml
				.contains("img src=\"files:/a new path/p__20180523_123601.jpg\"")),
			
			() -> Assertions.assertFalse(processedKml
				.contains("href=\"/storage/emulated/0/Locus/data/media/photo/p__20200409_150847.jpg\"")),
			() -> Assertions.assertFalse(processedKml
				.contains("src=\"/storage/emulated/0/Locus/data/media/photo/p__20200409_150847.jpg\"")),
			() -> Assertions.assertFalse(processedKml
				.contains("href=\"/storage/emulated/0/Locus/data/media/photo/p__20180523_123601.jpg\"")),
			() -> Assertions.assertFalse(processedKml
				.contains("src=\"/storage/emulated/0/Locus/data/media/photo/p__20180523_123601.jpg\""))
		);
		
		Assertions.assertAll(
			() -> Assertions.assertFalse(processedKml.contains("width=\"330px\"")),
			() -> Assertions.assertFalse(processedKml.contains("width=\"60px\"")),
			
			() -> Assertions.assertTrue(processedKml.contains("width=\"800px\""))
		);
		
		//To check the whole document to be inline
/*
		Pattern patternMultiWhitespaces = Pattern.compile(".*\\s{2,}", Pattern.MULTILINE);
		Pattern patternNewString = Pattern.compile(".*\\n.*", Pattern.MULTILINE);
		
		Assertions.assertAll(
			() -> Assertions.assertFalse(patternMultiWhitespaces.matcher(processedKml).find()),
			() -> Assertions.assertFalse(patternNewString.matcher(processedKml).find())
		);
*/
	}
}