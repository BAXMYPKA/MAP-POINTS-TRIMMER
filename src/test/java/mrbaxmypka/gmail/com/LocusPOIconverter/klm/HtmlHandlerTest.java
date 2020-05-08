package mrbaxmypka.gmail.com.LocusPOIconverter.klm;

import mrbaxmypka.gmail.com.LocusPOIconverter.entitiesDto.MultipartDto;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.*;

class HtmlHandlerTest {
	
	static HtmlHandler htmlHandler = new HtmlHandler();
	static String html = "<!-- desc_gen:start -->\n" +
		"<font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\">\n" +
		"<!-- desc_user:start -->\n" +
		"<font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\">" +
		"<font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\"><font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\">" +
		"<font color=\"black\"><table width=\"100%\"><tr><td width=\"100%\" align=\"center\"><a href=\"/storage/emulated/0/Locus/data/media/photo/_1404638472855.jpg\" target=\"_blank\"><img src=\"/storage/emulated/0/Locus/data/media/photo/_1404638472855.jpg\" width=\"330px\" align=\"center\" ></a></td></tr><tr><td colspan=\"1\"><hr></td></tr><tr><td><table width=\"100%\"><tr><td align=\"left\" valign=\"center\"><small><b>Высота</b></small></td><td align=\"center\" valign=\"center\">175 m</td></tr>\n" +
		"<tr><td align=\"left\" valign=\"center\"><small><b>Азимут</b></small></td><td align=\"center\" valign=\"center\">160°</td></tr>\n" +
		"<tr><td align=\"left\" valign=\"center\"><small><b>Точность</b></small></td><td align=\"center\" valign=\"center\">3.0 m</td></tr>\n" +
		"<tr><td align=\"left\" valign=\"center\"><small><b>Создано</b></small></td><td align=\"center\" valign=\"center\">2014-07-06 13:20:39</td></tr>\n" +
		"</table></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font></td></tr><tr><td colspan=\"1\"><hr></td></tr><tr><td><table width=\"100%\"><tr><td align=\"left\" valign=\"center\"><small><b>Высота</b></small></td><td align=\"center\" valign=\"center\">175 m</td></tr>\n" +
		"<tr><td align=\"left\" valign=\"center\"><small><b>Создано</b></small></td><td align=\"center\" valign=\"center\">2014-07-18 17:23:20</td></tr>\n" +
		"</table></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font></td></tr><tr><td colspan=\"1\"><hr></td></tr><tr><td><table width=\"100%\"><tr><td align=\"left\" valign=\"center\"><small><b>Высота</b></small></td><td align=\"center\" valign=\"center\">175 m</td></tr>\n" +
		"<tr><td align=\"left\" valign=\"center\"><small><b>Создано</b></small></td><td align=\"center\" valign=\"center\">2014-08-10 13:33:17</td></tr>\n" +
		"</table></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font></td></tr><tr><td colspan=\"1\"><hr></td></tr><tr><td><table width=\"100%\"><tr><td align=\"left\" valign=\"center\"><small><b>Высота</b></small></td><td align=\"center\" valign=\"center\">175 m</td></tr>\n" +
		"<tr><td align=\"left\" valign=\"center\"><small><b>Создано</b></small></td><td align=\"center\" valign=\"center\">2014-09-18 16:16:45</td></tr>\n" +
		"</table></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font>\n" +
		"<!-- desc_user:end -->\n" +
		"</td></tr><tr><td colspan=\"1\"><hr></td></tr><tr><td><table width=\"100%\"><tr><td align=\"left\" valign=\"center\"><small><b>Высота</b></small></td><td align=\"center\" valign=\"center\">175 m</td></tr>\n" +
		"<tr><td align=\"left\" valign=\"center\"><small><b>Создано</b></small></td><td align=\"center\" valign=\"center\">2014-11-21 00:27:31</td></tr>\n" +
		"</table></td></tr><tr><td><table width=\"100%\"></table></td></tr></table></font>\n" +
		"<!-- desc_gen:end -->";
	static MultipartDto multipartDto;
	
	@BeforeAll
	public static void beforeAll() {
		MultipartFile multipartFile = new MockMultipartFile("html", html.getBytes());
		multipartDto = new MultipartDto(multipartFile);
	}
	
	@Test
	public void set_Relative_Path_Should_Set_Relative_Path_Type() {
		//GIVEN
		multipartDto.setSetPath(true);
		multipartDto.setPathType("relative");
		multipartDto.setPath("../myFiles");
		
		//WHEN
		String processedHtml = htmlHandler.processCdata(html, multipartDto);
		
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
		String processedHtml = htmlHandler.processCdata(html, multipartDto);

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
	public void set_Absolute_Windows_Path_With_Whitespaces_Should_Set_Correct_Absolute_Path_Type_URL_Encoded() {
		//GIVEN
		multipartDto.setSetPath(true);
		multipartDto.setPathType("Absolute");
		multipartDto.setPath("D:\\My Folder\\My POI");
		
		//WHEN
		String processedHtml = htmlHandler.processCdata(html, multipartDto);
		
		//THEN
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
		String processedHtml = htmlHandler.processCdata(html, multipartDto);
		
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
	
}