package mrbaxmypka.gmail.com.mapPointsTrimmer.xml;

import mrbaxmypka.gmail.com.mapPointsTrimmer.entitiesDto.MultipartDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class HtmlHandlerTest {
	
	static HtmlHandler htmlHandler = new HtmlHandler();
	static String html = "<!-- desc_gen:start -->\n" +
		"<font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\">\n" +
		"<!-- desc_user:start -->\n" +
		"User description within comments\n" +
		"<font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\">" +
		"<font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\"><font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\">" +
		"<font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\">" +
		"<a href=\"/storage/emulated/0/Locus/data/media/photo/_1404638472855.jpg\" target=\"_blank\"><img src=\"/storage/emulated/0/Locus/data/media/photo/_1404638472855.jpg\" width=\"330px\" align=\"center\" >User description within 'a' tag</a>" +
		"</td></tr><tr><td colspan=\"1\"><hr></td></tr><tr><td><table width=\"100%\"><tr><td align=\"left\" valign=\"center\"><small><b>Высота</b></small></td><td align=\"center\" valign=\"center\">175 m</td></tr>\n" +
		"<tr><td align=\"left\" valign=\"center\"><small><b>Азимут</b></small></td><td align=\"center\" valign=\"center\">160°</td></tr>\n" +
		"<tr><td align=\"left\" valign=\"center\"><small><b>Точность</b></small></td><td align=\"center\" valign=\"center\">3.0 m</td></tr>\n" +
		"<tr><td align=\"left\" valign=\"center\"><small><b>Создано</b></small></td><td align=\"center\" valign=\"center\">2014-07-06 13:20:39</td></tr>\n" +
		"</table></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font></td></tr><tr><td colspan=\"1\"><hr></td>" +
		"</tr><tr><td><table width=\"100%\"><tr><td align=\"left\" valign=\"center\"><small><b>Высота</b></small></td><td align=\"center\" valign=\"center\">175 m</td></tr>\n" +
		"<tr><td align=\"left\" valign=\"center\"><small><b>Создано</b></small></td><td align=\"center\" valign=\"center\">2014-07-18 17:23:20</td></tr>\n" +
		"</table></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font></td></tr><tr><td colspan=\"1\"><hr></td></tr>" +
		"<tr><td><table width=\"100%\"><tr><td align=\"left\" valign=\"center\"><small><b>Высота</b></small></td><td align=\"center\" valign=\"center\">175 m</td></tr>\n" +
		"<tr><td align=\"left\" valign=\"center\"><small><b>Создано</b></small></td><td align=\"center\" valign=\"center\">2014-08-10 13:33:17</td></tr>\n" +
		"</table></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font></td></tr><tr><td colspan=\"1\"><hr></td></tr>" +
		"<tr><td><table width=\"100%\"><tr><td align=\"left\" valign=\"center\"><small><b>Высота</b></small></td><td align=\"center\" valign=\"center\">175 m</td></tr>\n" +
		"<tr><td align=\"left\" valign=\"center\"><small><b>Создано</b></small></td><td align=\"center\" valign=\"center\">2014-09-18 16:16:45</td></tr>\n" +
		"</table></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font>\n" +
		"<!-- desc_user:end -->\n" +
		"</td></tr><tr><td colspan=\"1\"><hr></td></tr><tr><td><table width=\"100%\"><tr><td align=\"left\" valign=\"center\"><small><b>Высота</b></small></td><td align=\"center\" valign=\"center\">175 m</td></tr>\n" +
		"<tr><td align=\"left\" valign=\"center\"><small><b>Создано</b></small></td><td align=\"center\" valign=\"center\">2014-11-21 00:27:31</td></tr>\n" +
		"</table></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font>\n" +
		"<!-- desc_gen:end -->";
	static MultipartDto multipartDto;
	
	@BeforeEach
	public void beforeEach() {
		MultipartFile multipartFile = new MockMultipartFile("html", html.getBytes(StandardCharsets.UTF_8));
		multipartDto = new MultipartDto(multipartFile);
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////// LOCUS PRO TESTS ////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////
	
	@Test
	public void any_Modification_Should_Enclose_Description_With_DescGenStart_DescGenEnd_With_Them_Initially() {
		//GIVEN
		String twoImgsWithStyles = "<!-- desc_gen:start -->\n" +
			"<font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\">" +
			"<img style=\"width:500px;border:3px white solid;\" src=\"files/p__20200511_130745.jpg\">" +
			"<img src=\"files/p__20180514_153338.jpg\" width=\"60px\" align=\"right\" style=\"border: 3px white solid; color: black; max-width: 300%\"> " +
			"<br /><br /></td></tr><tr><td colspan=\"1\"><hr></td></tr><tr><td><table width=\"100%\"><tr><td align=\"left\" valign=\"center\"><small><b>Высота</b></small></td><td align=\"center\" valign=\"center\">169 m</td></tr>\n" +
			"<tr><td align=\"left\" valign=\"center\"><small><b>Азимут</b></small></td><td align=\"center\" valign=\"center\">147 °</td></tr>\n" +
			"<tr><td align=\"left\" valign=\"center\"><small><b>Точность</b></small></td><td align=\"center\" valign=\"center\">3 m</td></tr>\n" +
			"<tr><td align=\"left\" valign=\"center\"><small><b>Создано</b></small></td><td align=\"center\" valign=\"center\">2018-05-14 15:28:41</td></tr>\n" +
			"</table></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font>\n" +
			"<!-- desc_gen:end -->";
		MultipartFile multipartFile = new MockMultipartFile("html", twoImgsWithStyles.getBytes(StandardCharsets.UTF_8));
		multipartDto = new MultipartDto(multipartFile);
		multipartDto.setSetPreviewSize(true);
		multipartDto.setPreviewSize(750);
		
		//WHEN
		String processedHtml = htmlHandler.processDescriptionText(twoImgsWithStyles, multipartDto);
		
		//THEN
		assertAll(
			() -> assertTrue(processedHtml.startsWith("<!-- desc_gen:start -->")),
			() -> assertTrue(processedHtml.endsWith("<!-- desc_gen:end -->"))
		);
	}
	
	@Test
	public void any_Modification_Should_Enclose_Description_With_DescGenStart_DescGenEnd_Without_Them_Initially() {
		//GIVEN
		String twoImgsWithStyles = "<font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\">" +
			"<img style=\"width:500px;border:3px white solid;\" src=\"files/p__20200511_130745.jpg\">" +
			"<img src=\"files/p__20180514_153338.jpg\" width=\"60px\" align=\"right\" style=\"border: 3px white solid; color: black; max-width: 300%\"> " +
			"<br /><br /></td></tr><tr><td colspan=\"1\"><hr></td></tr><tr><td><table width=\"100%\"><tr><td align=\"left\" valign=\"center\"><small><b>Высота</b></small></td><td align=\"center\" valign=\"center\">169 m</td></tr>\n" +
			"<tr><td align=\"left\" valign=\"center\"><small><b>Азимут</b></small></td><td align=\"center\" valign=\"center\">147 °</td></tr>\n" +
			"<tr><td align=\"left\" valign=\"center\"><small><b>Точность</b></small></td><td align=\"center\" valign=\"center\">3 m</td></tr>\n" +
			"<tr><td align=\"left\" valign=\"center\"><small><b>Создано</b></small></td><td align=\"center\" valign=\"center\">2018-05-14 15:28:41</td></tr>\n" +
			"</table></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font>";
		MultipartFile multipartFile = new MockMultipartFile("html", twoImgsWithStyles.getBytes(StandardCharsets.UTF_8));
		multipartDto = new MultipartDto(multipartFile);
		multipartDto.setSetPreviewSize(true);
		multipartDto.setPreviewSize(750);
		
		//WHEN
		String processedHtml = htmlHandler.processDescriptionText(twoImgsWithStyles, multipartDto);
		
		//THEN
		assertAll(
			() -> assertTrue(processedHtml.startsWith("<!-- desc_gen:start -->")),
			() -> assertTrue(processedHtml.endsWith("<!-- desc_gen:end -->"))
		);
	}
	
	@Test
	public void setPreviewSize_Should_Embrace_Images_And_Data_Within_DescUserStart_And_DescUserEnd_Comments_Without_Them_Initially() {
		//GIVEN CDATA with image but without user descriptions within <!-- desc_user:start --> and <!-- desc_user:end -->
		String withoutDescUserComments = "<!-- desc_gen:start -->\n" +
			"<font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\">" +
			"<a href=\"files/p__20180514_153338.jpg\" target=\"_blank\"><img src=\"files/p__20180514_153338.jpg\" width=\"60px\" align=\"right\" style=\"border: 3px white solid;\"></a>" +
			"<br /><br /></td></tr><tr><td colspan=\"1\"><hr></td></tr><tr><td><table width=\"100%\"><tr><td align=\"left\" valign=\"center\"><small><b>Высота</b></small></td><td align=\"center\" valign=\"center\">169 m</td></tr>\n" +
			"<tr><td align=\"left\" valign=\"center\"><small><b>Азимут</b></small></td><td align=\"center\" valign=\"center\">147 °</td></tr>\n" +
			"<tr><td align=\"left\" valign=\"center\"><small><b>Точность</b></small></td><td align=\"center\" valign=\"center\">3 m</td></tr>\n" +
			"<tr><td align=\"left\" valign=\"center\"><small><b>Создано</b></small></td><td align=\"center\" valign=\"center\">2018-05-14 15:28:41</td></tr>\n" +
			"</table></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font>\n" +
			"<!-- desc_gen:end -->";
		MultipartFile multipartFile = new MockMultipartFile(
			"html", withoutDescUserComments.getBytes(StandardCharsets.UTF_8));
		multipartDto = new MultipartDto(multipartFile);
		multipartDto.setSetPreviewSize(true);
		multipartDto.setPreviewSize(400);
		
		//WHEN
		String processedHtml = htmlHandler.processDescriptionText(withoutDescUserComments, multipartDto);
		
		//THEN
		assertAll(
			() -> assertTrue(processedHtml.contains("<!-- desc_user:start -->")),
			() -> assertTrue(processedHtml.contains("<!-- desc_user:end -->"))
		);
	}
	
	@Test
	public void setPreviewSize_Should_Embrace_All_Users_Texts_Within_DescUserStart_And_DescUserEnd_Comments() {
		//GIVEN CDATA with image and user description within <!-- desc_user:start --> and <!-- desc_user:end -->
		String withDescUserCommentsAndText = "<!-- desc_gen:start -->\n" +
			"<font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\"><img src=\"files/p__20200511_131742.jpg\" width=\"60px\" align=\"right\" style=\"border: 3px white solid;\"><br /><br />\n" +
			"</td></tr><tr><td colspan=\"1\"><hr></td></tr><tr><td><table width=\"100%\"><tr><td align=\"left\" valign=\"center\"><small><b>Высота</b></small></td><td align=\"center\" valign=\"center\">165 m</td></tr>\n" +
			"<tr><td align=\"left\" valign=\"center\"><small><b>Создано</b></small></td><td align=\"center\" valign=\"center\">2020-05-12 08:50:23</td></tr>\n" +
			"</table></td></tr><tr><td><table width=\"100%\"></table></td></tr></table>\n" +
			"<!-- desc_user:start -->\n" +
			"Test user description\n" +
			"<!-- desc_user:end -->\n" +
			"</font>\n" +
			"<!-- desc_gen:end -->";
		MultipartFile multipartFile = new MockMultipartFile(
			"html", withDescUserCommentsAndText.getBytes(StandardCharsets.UTF_8));
		multipartDto = new MultipartDto(multipartFile);
		multipartDto.setSetPreviewSize(true);
		multipartDto.setPreviewSize(400);
		
		//WHEN
		String processedHtml = htmlHandler.processDescriptionText(withDescUserCommentsAndText, multipartDto);
		
		//THEN
		assertAll(
			() -> assertTrue(processedHtml.contains("<!-- desc_user:start -->")),
			() -> assertTrue(processedHtml.contains("<!-- desc_user:end -->"))
		);
		
		assertTrue(processedHtml.contains(" <!-- desc_user:start -->Test user description"));
	}
	
	@Test
	public void setPreviewSize_Should_Embrace_All_Users_Texts_Within_DescUserStart_And_DescUserEnd_Comments_For_Complicated_Cdata() {
		//GIVEN CDATA with image and old style user description within <!-- desc_user:start --> and <!-- desc_user:end -->
		String oldStyleCdata = "<!-- desc_gen:start -->\n" +
			"<font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\">\n" +
			"<!-- desc_user:start -->\n" +
			"<font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\"><font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\"><font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\"><font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\"><font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\"><font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\"><font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\"><font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\"><font color=\"black\"><table width=\"100%\"><tr><td><table width=\"100%\"></table></td></tr></table></font></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font></td></tr><tr><td colspan=\"1\"><hr></td></tr><tr><td><table width=\"100%\"><tr><td align=\"left\" valign=\"center\"><small><b>Высота</b></small></td><td align=\"center\" valign=\"center\">230 m</td></tr>\n" +
			"<tr><td align=\"left\" valign=\"center\"><small><b>Создано</b></small></td><td align=\"center\" valign=\"center\">2014-05-10 16:33:59</td></tr>\n" +
			"</table></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font></td></tr><tr><td colspan=\"1\"><hr></td></tr><tr><td><table width=\"100%\"><tr><td align=\"left\" valign=\"center\"><small><b>Высота</b></small></td><td align=\"center\" valign=\"center\">230 m</td></tr>\n" +
			"<tr><td align=\"left\" valign=\"center\"><small><b>Создано</b></small></td><td align=\"center\" valign=\"center\">2014-06-03 14:39:11</td></tr>\n" +
			"</table></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font></td></tr><tr><td colspan=\"1\"><hr></td></tr><tr><td><table width=\"100%\"><tr><td align=\"left\" valign=\"center\"><small><b>Высота</b></small></td><td align=\"center\" valign=\"center\">230 m</td></tr>\n" +
			"<tr><td align=\"left\" valign=\"center\"><small><b>Создано</b></small></td><td align=\"center\" valign=\"center\">2014-06-07 17:25:15</td></tr>\n" +
			"</table></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font></td></tr><tr><td colspan=\"1\"><hr></td></tr><tr><td><table width=\"100%\"><tr><td align=\"left\" valign=\"center\"><small><b>Высота</b></small></td><td align=\"center\" valign=\"center\">230 m</td></tr>\n" +
			"<tr><td align=\"left\" valign=\"center\"><small><b>Создано</b></small></td><td align=\"center\" valign=\"center\">2014-07-18 17:23:19</td></tr>\n" +
			"</table></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font></td></tr><tr><td colspan=\"1\"><hr></td></tr><tr><td><table width=\"100%\"><tr><td align=\"left\" valign=\"center\"><small><b>Высота</b></small></td><td align=\"center\" valign=\"center\">230 m</td></tr>\n" +
			"<tr><td align=\"left\" valign=\"center\"><small><b>Создано</b></small></td><td align=\"center\" valign=\"center\">2014-08-10 13:33:15</td></tr>\n" +
			"</table></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font></td></tr><tr><td colspan=\"1\"><hr></td></tr><tr><td><table width=\"100%\"><tr><td align=\"left\" valign=\"center\"><small><b>Высота</b></small></td><td align=\"center\" valign=\"center\">230 m</td></tr>\n" +
			"<tr><td align=\"left\" valign=\"center\"><small><b>Создано</b></small></td><td align=\"center\" valign=\"center\">2014-09-18 16:16:44</td></tr>\n" +
			"</table></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font>\n" +
			"Test user description" +
			"<!-- desc_user:end -->\n" +
			"</td></tr><tr><td colspan=\"1\"><hr></td></tr><tr><td><table width=\"100%\"><tr><td align=\"left\" valign=\"center\"><small><b>Высота</b></small></td><td align=\"center\" valign=\"center\">230 m</td></tr>\n" +
			"<tr><td align=\"left\" valign=\"center\"><small><b>Создано</b></small></td><td align=\"center\" valign=\"center\">2014-11-21 00:27:30</td></tr>\n" +
			"</table></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font>\n" +
			"<!-- desc_gen:end -->";
		MultipartFile multipartFile = new MockMultipartFile("html", oldStyleCdata.getBytes(StandardCharsets.UTF_8));
		multipartDto = new MultipartDto(multipartFile);
		multipartDto.setSetPreviewSize(true);
		multipartDto.setPreviewSize(400);
		
		//WHEN
		String processedHtml = htmlHandler.processDescriptionText(oldStyleCdata, multipartDto);
		
		//THEN
		assertAll(
			() -> assertTrue(processedHtml.contains("<!-- desc_user:start -->")),
			() -> assertTrue(processedHtml.contains("<!-- desc_user:end -->"))
		);
		
		assertTrue(processedHtml.contains(" Test user description<!-- desc_user:end -->"));
	}
	
	@Test
	public void setPath_and_ClearDescription_for_Old_Style_Placemarks_Should_Embrace_Images_And_Data_Within_DescUserStart_And_DescUserEnd_Comments_With_them_Initially() {
		//GIVEN CDATA with image and user descriptions within <!-- desc_user:start --> and <!-- desc_user:end -->
		multipartDto.setSetPath(true);
		multipartDto.setPathType("relative");
		multipartDto.setPath("../myFiles");
		
		
		//WHEN
		String processedHtml = htmlHandler.processDescriptionText(html, multipartDto);
		
		//THEN
		assertAll(
			() -> assertTrue(processedHtml.contains("<!-- desc_user:start -->")),
			() -> assertTrue(processedHtml.contains("<!-- desc_user:end -->"))
		);
		assertAll(
			() -> assertTrue(processedHtml.contains("<!-- desc_user:start --> User description within comments")),
			() -> assertTrue(processedHtml.contains("User description within 'a' tag"))
		);
		
		assertAll(
			() -> assertFalse(processedHtml.contains(
				"<a href=\"/storage/emulated/0/Locus/data/media/photo/_1404638472855.jpg")),
			() -> assertFalse(processedHtml.contains(
				"<img src=\"/storage/emulated/0/Locus/data/media/photo/_1404638472855.jpg"))
		);
		
	}
	
	@Test
	public void setPreviewSize_for_Imgs_With_Inline_Styles_Should_Set_New_Widths() {
		//GIVEN CDATA with 2 imgs with inline styles to be replaces with new width
		String twoImgsWithStyles = "<!-- desc_gen:start -->\n" +
			"<font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\">" +
			"<img style=\"width:500px;border:3px white solid;\" src=\"files/p__20200511_130745.jpg\">" +
			"<img src=\"files/p__20180514_153338.jpg\" width=\"60px\" align=\"right\" style=\"border: 3px white solid; color: black; max-width: 300%\"> " +
			"<br /><br /></td></tr><tr><td colspan=\"1\"><hr></td></tr><tr><td><table width=\"100%\"><tr><td align=\"left\" valign=\"center\"><small><b>Высота</b></small></td><td align=\"center\" valign=\"center\">169 m</td></tr>\n" +
			"<tr><td align=\"left\" valign=\"center\"><small><b>Азимут</b></small></td><td align=\"center\" valign=\"center\">147 °</td></tr>\n" +
			"<tr><td align=\"left\" valign=\"center\"><small><b>Точность</b></small></td><td align=\"center\" valign=\"center\">3 m</td></tr>\n" +
			"<tr><td align=\"left\" valign=\"center\"><small><b>Создано</b></small></td><td align=\"center\" valign=\"center\">2018-05-14 15:28:41</td></tr>\n" +
			"</table></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font>\n" +
			"<!-- desc_gen:end -->";
		MultipartFile multipartFile = new MockMultipartFile("html", twoImgsWithStyles.getBytes(StandardCharsets.UTF_8));
		multipartDto = new MultipartDto(multipartFile);
		multipartDto.setSetPreviewSize(true);
		multipartDto.setPreviewSize(750);
		
		//WHEN
		String processedHtml = htmlHandler.processDescriptionText(twoImgsWithStyles, multipartDto);
		
		//THEN
		assertAll(
			() -> assertTrue(processedHtml.contains("style=\"width: 750px;")),
			() -> assertTrue(processedHtml.contains("max-width: 750px"))
		);
		
		assertAll(
			() -> assertFalse(processedHtml.contains("width:500px")),
			() -> assertFalse(processedHtml.contains("max-width: 300%")),
			() -> assertFalse(processedHtml.contains("max-width:300%"))
		);
		
	}
	
	@Test
	public void setPreviewSize_With_Null_Integer_Should_Set_All_Img_Widths_Attributes_To_0() {
		//GIVEN
		multipartDto.setSetPreviewSize(true);
		multipartDto.setPreviewSize(null);
		
		//WHEN
		
		String processedKml = htmlHandler.processDescriptionText(html, multipartDto);
		
		//THEN
		
		assertFalse(processedKml.contains("width=\"330px\""));
		assertTrue(processedKml.contains("width=\"0px\""));
	}
	
	@Test
	public void setPreviewSize_Should_Return_Cleared_Single_Table_With_Earliest_DateTime_And_Full_Descriptions() {
		//"Set preview size" option forces clearing outdated descriptions and rearrange the html structure
		//so that to place the images within <!-- desc_user:start\end --> comments
		//as Locus shows inner content only within them.
		
		//GIVEN
		//The real placemark named "Плотина"
		String outdatedDescription = "<!-- desc_gen:start -->\n" +
			"<font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\">\n" +
			"<!-- desc_user:start -->\n" +
			"<font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\"><font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\"><font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\"><font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\"><font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\"><font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\"><font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\"><font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\"><a href=\"/storage/emulated/0/Locus/data/media/photo/_1370227813151.jpg\" target=\"_blank\"><img src=\"/storage/emulated/0/Locus/data/media/photo/_1370227813151.jpg\" width=\"330px\" align=\"center\"></a></td></tr><tr><td colspan=\"1\"><hr></td></tr><tr><td><table width=\"100%\"><tr><td align=\"left\" valign=\"center\"><small><b>Высота</b></small></td><td align=\"center\" valign=\"center\">177 m</td></tr>\n" +
			"<tr><td align=\"left\" valign=\"center\"><small><b>Азимут</b></small></td><td align=\"center\" valign=\"center\">170°</td></tr>\n" +
			"<tr><td align=\"left\" valign=\"center\"><small><b>Точность</b></small></td><td align=\"center\" valign=\"center\">3.0 m</td></tr>\n" +
			"<tr><td align=\"left\" valign=\"center\"><small><b>Создано</b></small></td><td align=\"center\" valign=\"center\">2013-06-05 08:27:14</td></tr>\n" +
			"</table></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font></td></tr><tr><td colspan=\"1\"><hr></td></tr><tr><td><table width=\"100%\"><tr><td align=\"left\" valign=\"center\"><small><b>Высота</b></small></td><td align=\"center\" valign=\"center\">177 m</td></tr>\n" +
			"<tr><td align=\"left\" valign=\"center\"><small><b>Создано</b></small></td><td align=\"center\" valign=\"center\">2014-04-28 15:35:07</td></tr>\n" +
			"</table></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font></td></tr><tr><td colspan=\"1\"><hr></td></tr><tr><td><table width=\"100%\"><tr><td align=\"left\" valign=\"center\"><small><b>Высота</b></small></td><td align=\"center\" valign=\"center\">177 m</td></tr>\n" +
			"<tr><td align=\"left\" valign=\"center\"><small><b>Создано</b></small></td><td align=\"center\" valign=\"center\">2014-05-10 16:33:59</td></tr>\n" +
			"</table></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font></td></tr><tr><td colspan=\"1\"><hr></td></tr><tr><td><table width=\"100%\"><tr><td align=\"left\" valign=\"center\"><small><b>Высота</b></small></td><td align=\"center\" valign=\"center\">177 m</td></tr>\n" +
			"<tr><td align=\"left\" valign=\"center\"><small><b>Создано</b></small></td><td align=\"center\" valign=\"center\">2014-06-03 14:39:11</td></tr>\n" +
			"</table></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font></td></tr><tr><td colspan=\"1\"><hr></td></tr><tr><td><table width=\"100%\"><tr><td align=\"left\" valign=\"center\"><small><b>Высота</b></small></td><td align=\"center\" valign=\"center\">177 m</td></tr>\n" +
			"<tr><td align=\"left\" valign=\"center\"><small><b>Создано</b></small></td><td align=\"center\" valign=\"center\">2014-06-07 17:25:15</td></tr>\n" +
			"</table></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font></td></tr><tr><td colspan=\"1\"><hr></td></tr><tr><td><table width=\"100%\"><tr><td align=\"left\" valign=\"center\"><small><b>Высота</b></small></td><td align=\"center\" valign=\"center\">177 m</td></tr>\n" +
			"<tr><td align=\"left\" valign=\"center\"><small><b>Создано</b></small></td><td align=\"center\" valign=\"center\">2014-07-18 17:23:19</td></tr>\n" +
			"</table></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font></td></tr><tr><td colspan=\"1\"><hr></td></tr><tr><td><table width=\"100%\"><tr><td align=\"left\" valign=\"center\"><small><b>Высота</b></small></td><td align=\"center\" valign=\"center\">177 m</td></tr>\n" +
			"<tr><td align=\"left\" valign=\"center\"><small><b>Создано</b></small></td><td align=\"center\" valign=\"center\">2014-08-10 13:33:16</td></tr>\n" +
			"</table></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font></td></tr><tr><td colspan=\"1\"><hr></td></tr><tr><td><table width=\"100%\"><tr><td align=\"left\" valign=\"center\"><small><b>Высота</b></small></td><td align=\"center\" valign=\"center\">177 m</td></tr>\n" +
			"<tr><td align=\"left\" valign=\"center\"><small><b>Создано</b></small></td><td align=\"center\" valign=\"center\">2014-09-18 16:16:45</td></tr>\n" +
			"</table></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font>\n" +
			"<!-- desc_user:end -->\n" +
			"</td></tr><tr><td colspan=\"1\"><hr></td></tr><tr><td><table width=\"100%\"><tr><td align=\"left\" valign=\"center\"><small><b>Высота</b></small></td><td align=\"center\" valign=\"center\">177 m</td></tr>\n" +
			"<tr><td align=\"left\" valign=\"center\"><small><b>Создано</b></small></td><td align=\"center\" valign=\"center\">2014-11-21 00:27:30</td></tr>\n" +
			"</table></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font>\n" +
			"<!-- desc_gen:end -->";
		MultipartFile multipartFile = new MockMultipartFile("html", outdatedDescription.getBytes(StandardCharsets.UTF_8));
		multipartDto = new MultipartDto(multipartFile);
		multipartDto.setSetPreviewSize(true);
		multipartDto.setPreviewSize(750);
		
		//WHEN
		String processedHtml = htmlHandler.processDescriptionText(outdatedDescription, multipartDto);
		
		//THEN contains only the earliest date of creation
		assertAll(
			() -> assertTrue(processedHtml.contains("<!-- desc_user:start -->")),
			() -> assertTrue(processedHtml.contains("<!-- desc_user:end -->")),
			() -> assertTrue(processedHtml.contains("2013-06-05 08:27:14"))
		);
		
		assertAll(
			() -> assertFalse(processedHtml.contains("2014-05-10 16:33:59")),
			() -> assertFalse(processedHtml.contains("2014-06-03 14:39:11")),
			() -> assertFalse(processedHtml.contains("2014-09-18 16:16:45")),
			() -> assertFalse(processedHtml.contains("2014-11-21 00:27:30"))
		);
		//Description points have to be inserted without dublicates
		Pattern patternAltitude = Pattern.compile("177 m", Pattern.MULTILINE);
		Pattern patternAzimuth = Pattern.compile("170°", Pattern.MULTILINE);
		Pattern patternPrecision = Pattern.compile("3.0 m", Pattern.MULTILINE);
		Matcher matcherAlt = patternAltitude.matcher(processedHtml);
		Matcher matcherAzm = patternAzimuth.matcher(processedHtml);
		Matcher matcherPre = patternPrecision.matcher(processedHtml);
		
		assertAll(
			() -> assertEquals(1, matcherAlt.results().count()),
			() -> assertEquals(1, matcherAzm.results().count()),
			() -> assertEquals(1, matcherPre.results().count())
		);
	}
	
	@Test
	public void setPreviewSize_In_Percentage_Should_Set_Percentage_Units() {
		//GIVEN
		String imgInPixels = "<!-- desc_gen:start -->\n" +
			"<font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\">" +
			"<img src=\"files/p__20180514_153338.jpg\" width=\"60px\" align=\"right\" style=\"border: 3px white solid; color: black; max-width: 300%\"> " +
			"<br /><br /></td></tr><tr><td colspan=\"1\"><hr></td></tr><tr><td><table width=\"100%\"><tr><td align=\"left\" valign=\"center\"><small><b>Высота</b></small></td><td align=\"center\" valign=\"center\">169 m</td></tr>\n" +
			"<tr><td align=\"left\" valign=\"center\"><small><b>Азимут</b></small></td><td align=\"center\" valign=\"center\">147 °</td></tr>\n" +
			"<tr><td align=\"left\" valign=\"center\"><small><b>Точность</b></small></td><td align=\"center\" valign=\"center\">3 m</td></tr>\n" +
			"<tr><td align=\"left\" valign=\"center\"><small><b>Создано</b></small></td><td align=\"center\" valign=\"center\">2018-05-14 15:28:41</td></tr>\n" +
			"</table></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font>\n" +
			"<!-- desc_gen:end -->";
		MultipartFile multipartFile = new MockMultipartFile("html", imgInPixels.getBytes(StandardCharsets.UTF_8));
		multipartDto = new MultipartDto(multipartFile);
		multipartDto.setSetPreviewSize(true);
		multipartDto.setPreviewSize(90);
		multipartDto.setPreviewUnit("%");
		
		//WHEN
		String processedHtml = htmlHandler.processDescriptionText(imgInPixels, multipartDto);
		
		//THEN
		assertAll(
			() -> assertTrue(processedHtml.contains(" width=\"90%\" ")),
			() -> assertTrue(processedHtml.contains("max-width: 90%;\""))
		);
		
		assertAll(
			() -> assertFalse(processedHtml.contains("width:60px")),
			() -> assertFalse(processedHtml.contains("max-width: 300%"))
		);
		
	}
	
	@Test
	public void setPreviewSize_With_ClearOutdatedDescriptions_Should_Not_Return_Fanthom_Empty_A_Elements() {
		//GIVEN
		String imgInPixels = "<!-- desc_gen:start -->\n" +
			"<div> <!-- desc_user:start --> \n" +
			" <table width=\"100%\" style=\"color:black\"> \n" +
			"  <tbody> \n" +
			"   <tr> \n" +
			"    <td align=\"center\" colspan=\"2\"><a href=\"file:///D:/TEMP/TRIMMER%20TESTS/01FullTestKmzExportFromLocus/p__20200511_130333.jpg\" target=\"_blank\"><img src=\"files/p__20200511_130333.jpg\" width=\"800px\" align=\"center\" style=\"border: 3px white solid;\"></a></td> \n" +
			"    <td></td> \n" +
			"   </tr> \n" +
			"   <tr> \n" +
			"    <td colspan=\"2\"> \n" +
			"     <hr></td> \n" +
			"   </tr> \n" +
			"   <tr> \n" +
			"    <td align=\"left\" valign=\"center\"><small><b>Высота</b></small></td> \n" +
			"    <td align=\"center\" valign=\"center\">166 m</td> \n" +
			"   </tr> \n" +
			"   <tr> \n" +
			"    <td align=\"left\" valign=\"center\"><small><b>Скорость</b></small></td> \n" +
			"    <td align=\"center\" valign=\"center\">12,6 km/h</td> \n" +
			"   </tr> \n" +
			"   <tr> \n" +
			"    <td align=\"left\" valign=\"center\"><small><b>Азимут</b></small></td> \n" +
			"    <td align=\"center\" valign=\"center\">327 °</td> \n" +
			"   </tr> \n" +
			"   <tr> \n" +
			"    <td align=\"left\" valign=\"center\"><small><b>Точность</b></small></td> \n" +
			"    <td align=\"center\" valign=\"center\">10 m</td> \n" +
			"   </tr> \n" +
			"   <tr> \n" +
			"    <td align=\"left\" valign=\"center\"><small><b>Создано</b></small></td> \n" +
			"    <td align=\"center\" valign=\"center\">2020-05-11 13:03:45</td> \n" +
			"   </tr> \n" +
			"   <tr> \n" +
			"    <td colspan=\"2\"> \n" +
			"     <hr></td> \n" +
			"   </tr> \n" +
			"  </tbody> \n" +
			" </table><!-- desc_user:end --> \n" +
			"</div><!-- desc_gen:end -->";
		MultipartFile multipartFile = new MockMultipartFile("html", imgInPixels.getBytes(StandardCharsets.UTF_8));
		multipartDto = new MultipartDto(multipartFile);
		multipartDto.setSetPreviewSize(true);
		multipartDto.setPreviewSize(100);
		multipartDto.setPreviewUnit("%");
		multipartDto.setSetPath(true);
		multipartDto.setPath("/storage/");
		multipartDto.setAsAttachmentInLocus(true);
		multipartDto.setClearOutdatedDescriptions(true);
		
		//WHEN
		String processedHtml = htmlHandler.processDescriptionText(imgInPixels, multipartDto);
		
		//THEN
		assertFalse(processedHtml.contains("<a href=\"/storage/p__20200511_130333.jpg\" target=\"_blank\"></a>"));
	}
	
	@Test
	public void set_Relative_Path_Should_Set_Relative_Path_Type() {
		//GIVEN
		multipartDto.setSetPath(true);
		multipartDto.setPathType("relative");
		multipartDto.setPath("../myFiles");
		
		//WHEN
		String processedHtml = htmlHandler.processDescriptionText(html, multipartDto);
		
		//THEN
		assertAll(
			() -> assertTrue(processedHtml.contains("<a href=\"../myFiles/_1404638472855.jpg")),
			() -> assertTrue(processedHtml.contains("<img src=\"../myFiles/_1404638472855.jpg"))
		);
		
		assertAll(
			() -> assertFalse(processedHtml.contains(
				"<a href=\"/storage/emulated/0/Locus/data/media/photo/_1404638472855.jpg")),
			() -> assertFalse(processedHtml.contains(
				"<img src=\"/storage/emulated/0/Locus/data/media/photo/_1404638472855.jpg"))
		);
	}
	
	@Test
	public void set_Absolute_Windows_Path_Without_Whitespaces_Should_Set_Correct_Absolute_Path_Type() {
		//GIVEN
		multipartDto.setSetPath(true);
		multipartDto.setPathType("Absolute");
		multipartDto.setPath("D:\\MyFolder\\MyPOI");
		
		//WHEN
		String processedHtml = htmlHandler.processDescriptionText(html, multipartDto);
		
		//THEN
		assertAll(
			() -> assertTrue(processedHtml.contains("<a href=\"file:///D:/MyFolder/MyPOI/_1404638472855.jpg")),
			() -> assertTrue(processedHtml.contains("<img src=\"file:///D:/MyFolder/MyPOI/_1404638472855.jpg"))
		);
		
		assertAll(
			() -> assertFalse(processedHtml.contains(
				"<a href=\"/storage/emulated/0/Locus/data/media/photo/_1404638472855.jpg")),
			() -> assertFalse(processedHtml.contains(
				"<img src=\"/storage/emulated/0/Locus/data/media/photo/_1404638472855.jpg"))
		);
	}
	
	@Test
	public void set_Paths_With_Whitespaces_Should_Set_Correct_Absolute_Path_Type_URL_Encoded() {
		//GIVEN
		String pathWithWhitespaces = "D:\\My Folder\\My POI";
		multipartDto.setSetPath(true);
		multipartDto.setPathType("Absolute");
		multipartDto.setPath(pathWithWhitespaces);
		
		//WHEN
		String processedHtml = htmlHandler.processDescriptionText(html, multipartDto);
		
		//THEN All whitespaces should be replaced with URL '%20' sign
		assertAll(
			() -> assertTrue(processedHtml.contains("<a href=\"file:///D:/My%20Folder/My%20POI/_1404638472855.jpg")),
			() -> assertTrue(processedHtml.contains("<img src=\"file:///D:/My%20Folder/My%20POI/_1404638472855.jpg"))
		);
		
		assertAll(
			() -> assertFalse(processedHtml.contains(
				"<a href=\"/storage/emulated/0/Locus/data/media/photo/_1404638472855.jpg")),
			() -> assertFalse(processedHtml.contains(
				"<img src=\"/storage/emulated/0/Locus/data/media/photo/_1404638472855.jpg"))
		);
	}
	
	@Test
	public void set_Web_Path_Should_Set_Correct_HTTP_Path_Type() {
		//GIVEN
		multipartDto.setSetPath(true);
		multipartDto.setPathType("wEb");
		multipartDto.setPath("http://www.mysite.com/my images");
		
		//WHEN
		String processedHtml = htmlHandler.processDescriptionText(html, multipartDto);
		
		//THEN
		assertAll(
			() -> assertTrue(processedHtml.contains("<a href=\"http://www.mysite.com/my%20images/_1404638472855.jpg")),
			() -> assertTrue(processedHtml.contains("<img src=\"http://www.mysite.com/my%20images/_1404638472855.jpg"))
		);
		
		assertAll(
			() -> assertFalse(processedHtml.contains(
				"<a href=\"/storage/emulated/0/Locus/data/media/photo/_1404638472855.jpg")),
			() -> assertFalse(processedHtml.contains(
				"<img src=\"/storage/emulated/0/Locus/data/media/photo/_1404638472855.jpg"))
		);
	}
	
	
	@Test
	public void setPath_Should_Set_Correct_Wrong_Backslashes() {
		//GIVEN
		String descriptionCdata = "<!-- desc_gen:start -->\n" +
			"<font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\"><img src=\"files/p__20200511_131742.jpg\" width=\"60px\" align=\"right\" style=\"border: 3px white solid;\"><br /><br />\n" +
			"</td></tr><tr><td colspan=\"1\"><hr></td></tr><tr><td><table width=\"100%\"><tr><td align=\"left\" valign=\"center\"><small><b>Высота</b></small></td><td align=\"center\" valign=\"center\">165 m</td></tr>\n" +
			"<tr><td align=\"left\" valign=\"center\"><small><b>Создано</b></small></td><td align=\"center\" valign=\"center\">2020-05-12 08:50:23</td></tr>\n" +
			"</table></td></tr><tr><td><table width=\"100%\"></table></td></tr></table>\n" +
			"<!-- desc_user:start -->\n" +
			"Test user description\n" +
			"<!-- desc_user:end -->\n" +
			"</font>\n" +
			"<!-- desc_gen:end -->";
		MultipartFile multipartFile = new MockMultipartFile("html", descriptionCdata.getBytes(StandardCharsets.UTF_8));
		multipartDto = new MultipartDto(multipartFile);
		multipartDto.setSetPreviewSize(true);
		multipartDto.setPreviewSize(400);
		multipartDto.setSetPath(true);
		multipartDto.setPathType("relAtive");
		//Wrong backslash
		multipartDto.setPath("..My maps\\my folder");
		
		//WHEN
		String processedHtml = htmlHandler.processDescriptionText(descriptionCdata, multipartDto);
		
		//THEN
		assertAll(
			() -> assertTrue(processedHtml.contains("<a href=\"..My%20maps/my%20folder/p__20200511_131742.jpg\"")),
			() -> assertTrue(processedHtml.contains("<img src=\"..My%20maps/my%20folder/p__20200511_131742.jpg\""))
		);
		
		assertAll(
			() -> assertFalse(processedHtml.contains(
				"<img src=\"..My%20maps\\my%20folder/p__20200511_131742.jpg")),
			() -> assertFalse(processedHtml.contains(
				"<img src=\"files/p__20200511_131742.jpg"))
		);
	}
	
	@Test
	public void setTrimDescriptions_Should_Trim_All_Whitespaces_And_Write_Only_Descriptions_Inline() {
		//GIVEN
		multipartDto.setTrimDescriptions(true);
		
		//WHEN
		String processedHtml = htmlHandler.processDescriptionText(html, multipartDto);
		
		//THEN
		//Doesn't contain new strings
		assertFalse(Pattern.matches(".*\\n.*", processedHtml));
		//Doesn't contain 2 or more whitespaces in a row
		assertFalse(Pattern.matches(".*\\s{2,}.*", processedHtml));
	}
	
	@Test
	public void clearDescriptions_Should_Return_Single_Table_With_Earliest_DateTimes_And_Full_Descriptions() {
		//GIVEN
		multipartDto.setClearOutdatedDescriptions(true);
		
		//WHEN
		String processedHtml = htmlHandler.processDescriptionText(html, multipartDto);
		
		//THEN contains only the earliest date of creation
		assertAll(
			() -> assertFalse(processedHtml.contains("2014-07-18 17:23:20")),
			() -> assertFalse(processedHtml.contains("2014-08-10 13:33:17")),
			() -> assertFalse(processedHtml.contains("2014-09-18 16:16:45")),
			() -> assertFalse(processedHtml.contains("2014-11-21 00:27:31"))
		);
		
		assertTrue(processedHtml.contains("2014-07-06 13:20:39"));
		
		assertTrue(processedHtml.contains("<td align=\"center\" valign=\"center\">175 m</td>"));
		
		assertAll(
			() -> assertTrue(processedHtml.contains("<!-- desc_user:start -->")),
			() -> assertTrue(processedHtml.contains("<!-- desc_user:end -->"))
		);
		assertTrue(processedHtml.contains("<!-- desc_user:start -->User description within comments"));
	}
	
	@Test
	public void cdata_As_Plain_Description_Text_And_Html_Markup_Should_Return_Correct_Description_Before_Html() {
		//GIVEN Plain text prepended before HTML markup
		String plainTextDescription = "" +
			"<div>" +
			"<!-- desc_user:start -->Inside user startEnd<!-- desc_user:end -->" +
			"<a href=\"file:\"><img src=\"file:\"></img>Inside a text</a>" +
			"</div>" +
			"Plain text description";
		MultipartFile multipartFile = new MockMultipartFile("text", plainTextDescription.getBytes(StandardCharsets.UTF_8));
		multipartDto = new MultipartDto(multipartFile);
		multipartDto.setSetPath(true);
		multipartDto.setSetPreviewSize(true);
		multipartDto.setClearOutdatedDescriptions(true);
		
		//WHEN
		String processedHtml = htmlHandler.processDescriptionText(plainTextDescription, multipartDto);
		
		//THEN
		assertAll(
			() -> assertTrue(processedHtml.startsWith("Plain text description")),
			() -> assertTrue(processedHtml.contains(
				"<!-- desc_user:start -->Inside user startEnd"))
		);
	}
	
	@Test
	public void reimported_Points_From_Locus_Should_Have_Been_Eliminated() {
		//GIVEN
		String imgInPixels = "<!-- desc_gen:start -->\n" +
			"<font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\"><a href=\"files/p__20200511_130333.jpg\" target=\"_blank\"><img src=\"files/p__20200511_130333.jpg\" width=\"60px\" align=\"right\" style=\"border: 3px white solid;\"></a><br /><br />\n" +
			"<!-- desc_user:start -->\n" +
			"<table width=\"100%\" style=\"color:black\"> \n" +
			"  <tbody> \n" +
			"   <tr> \n" +
			"    <td align=\"center\" colspan=\"2\"><a href=\"file:////storage/emulated/0/DCIM/FullTestKmzExport01/p__20200511_130333.jpg\" target=\"_blank\"><img src=\"file:////storage/emulated/0/DCIM/FullTestKmzExport01/p__20200511_130333.jpg\" width=\"100%\" align=\"center\" style=\"border:3px white solid;width:100%;\"></a></td> \n" +
			"   </tr> \n" +
			"   <tr> \n" +
			"    <td colspan=\"2\"> \n" +
			"     <hr></td> \n" +
			"   </tr> \n" +
			"   <tr> \n" +
			"    <td align=\"left\" valign=\"center\"><small><b>Высота</b></small></td> \n" +
			"    <td align=\"center\" valign=\"center\">166 m</td> \n" +
			"   </tr> \n" +
			"   <tr> \n" +
			"    <td align=\"left\" valign=\"center\"><small><b>Скорость</b></small></td> \n" +
			"    <td align=\"center\" valign=\"center\">12,6 km/h</td> \n" +
			"   </tr> \n" +
			"   <tr> \n" +
			"    <td align=\"left\" valign=\"center\"><small><b>Азимут</b></small></td> \n" +
			"    <td align=\"center\" valign=\"center\">327 °</td> \n" +
			"   </tr> \n" +
			"   <tr> \n" +
			"    <td align=\"left\" valign=\"center\"><small><b>Точность</b></small></td> \n" +
			"    <td align=\"center\" valign=\"center\">10 m</td> \n" +
			"   </tr> \n" +
			"   <tr> \n" +
			"    <td align=\"left\" valign=\"center\"><small><b>Создано</b></small></td> \n" +
			"    <td align=\"center\" valign=\"center\">2020-05-11 13:03:45</td> \n" +
			"   </tr> \n" +
			"   <tr> \n" +
			"    <td colspan=\"2\"> \n" +
			"     <hr></td> \n" +
			"   </tr> \n" +
			"   <tr> \n" +
			"    <td colspan=\"2\"> \n" +
			"     <hr></td> \n" +
			"   </tr> \n" +
			"  </tbody> \n" +
			" </table>\n" +
			"<!-- desc_user:end -->\n" +
			"</td></tr><tr><td colspan=\"1\"><hr></td></tr><tr><td><table width=\"100%\"><tr><td align=\"left\" valign=\"center\"><small><b>Altitude</b></small></td><td align=\"center\" valign=\"center\">166 m</td></tr>\n" +
			"<tr><td align=\"left\" valign=\"center\"><small><b>Created</b></small></td><td align=\"center\" valign=\"center\">2020-05-26 12:26:45</td></tr>\n" +
			"</table></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font>\n" +
			"<!-- desc_gen:end -->";
		MultipartFile multipartFile = new MockMultipartFile("html", imgInPixels.getBytes(StandardCharsets.UTF_8));
		multipartDto = new MultipartDto(multipartFile);
		multipartDto.setSetPreviewSize(true);
		multipartDto.setPreviewSize(800);
		multipartDto.setPreviewUnit("px");
		multipartDto.setSetPath(true);
		multipartDto.setPath("/storage/");
		multipartDto.setClearOutdatedDescriptions(true);
		
		//WHEN
		String processedHtml = htmlHandler.processDescriptionText(imgInPixels, multipartDto);
		
		//THEN
		Pattern image = Pattern.compile("src=\"/storage/p__20200511_130333.jpg\"", Pattern.MULTILINE);
		Pattern href = Pattern.compile("href=\"/storage/p__20200511_130333.jpg\"", Pattern.MULTILINE);
		Matcher matcherImage = image.matcher(processedHtml);
		Matcher matcherHref = href.matcher(processedHtml);
		
		assertAll(
			() -> assertEquals(1, matcherImage.results().count()),
			() -> assertEquals(1, matcherHref.results().count())
		
		);

//		assertFalse(processedHtml.contains("<a href=\"/storage/p__20200511_130333.jpg\" target=\"_blank\"></a>"));
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////// GOOGLE EARTH TESTS /////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////
	
	@Test
	public void cdata_As_Plain_Description_Text_Should_Return_Plain_Text() {
		//GIVEN Plain text from <description>...</description>
		String plainTextDescription = "Plain text description";
		MultipartFile multipartFile = new MockMultipartFile("text", plainTextDescription.getBytes(StandardCharsets.UTF_8));
		multipartDto = new MultipartDto(multipartFile);
		multipartDto.setSetPath(true);
		multipartDto.setSetPreviewSize(true);
		multipartDto.setPreviewSize(750);
		
		//WHEN
		String processedHtml = htmlHandler.processDescriptionText(plainTextDescription, multipartDto);
		
		//THEN
		assertTrue(processedHtml.contentEquals("Plain text description"));
	}
	
	/**
	 * Google Earth images for POI may contain only <img></img>. So we need to set them into <a><img></img></a>
	 */
	@Test
	public void setPreviewSize_for_Pure_Img_Should_Be_Transformed_Into_Img_Within_A_Links() {
		//GIVEN CDATA with 2 imgs with inline styles to be replaces with new width
		String twoPureImgs = "<!-- desc_gen:start -->\n" +
			"<table><tr><td>" +
			"<img style=\"width:500px;border:3px white solid;\" src=\"files/p__20200511_130745.jpg\">" +
			"<img src=\"files/p__20180514_153338.jpg\" width=\"60px\" align=\"right\" style=\"border: 3px white solid; color: black; max-width: 300%\">" +
			"</td></tr></table>\n" +
			"<!-- desc_gen:end -->";
		MultipartFile multipartFile = new MockMultipartFile("html", twoPureImgs.getBytes(StandardCharsets.UTF_8));
		multipartDto = new MultipartDto(multipartFile);
		multipartDto.setSetPreviewSize(true);
		multipartDto.setPreviewSize(750);
		
		//WHEN
		String processedHtml = htmlHandler.processDescriptionText(twoPureImgs, multipartDto);
		
		//THEN
		assertAll(
			() -> assertTrue(processedHtml.contains("<a href=\"files/p__20200511_130745.jpg\" target=\"_blank\"><img ")),
			() -> assertTrue(processedHtml.contains("<a href=\"files/p__20180514_153338.jpg\" target=\"_blank\"><img ")),
			() -> assertTrue(processedHtml.contains("></a>"))
		);
	}
	
	@Test
	public void description_As_Plain_Description_Text_Should_Return_Correct_Description() {
		//GIVEN Plain text prepended before HTML markup
		String plainTextDescription = "Plain text description";
		MultipartFile multipartFile = new MockMultipartFile("text", plainTextDescription.getBytes(StandardCharsets.UTF_8));
		multipartDto = new MultipartDto(multipartFile);
		multipartDto.setSetPath(true);
		multipartDto.setSetPreviewSize(true);
		
		//WHEN
		String processedHtml = htmlHandler.processDescriptionText(plainTextDescription, multipartDto);

		//THEN
		assertEquals("Plain text description", processedHtml);
	}
	
}