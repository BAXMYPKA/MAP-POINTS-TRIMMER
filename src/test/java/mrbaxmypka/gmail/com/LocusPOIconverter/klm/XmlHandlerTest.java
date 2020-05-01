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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
		
		//Is contains new strings
		Assertions.assertFalse(Pattern.matches(".*\\n.*", processedKml));
		//Is contains 2 or more whitespaces in a row
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
	
}