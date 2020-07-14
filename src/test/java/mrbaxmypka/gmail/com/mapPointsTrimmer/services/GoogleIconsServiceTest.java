package mrbaxmypka.gmail.com.mapPointsTrimmer.services;

import mrbaxmypka.gmail.com.mapPointsTrimmer.entitiesDto.MultipartDto;
import mrbaxmypka.gmail.com.mapPointsTrimmer.utils.DownloadAs;
import mrbaxmypka.gmail.com.mapPointsTrimmer.utils.GoogleIconsCache;
import mrbaxmypka.gmail.com.mapPointsTrimmer.xml.GoogleEarthHandler;
import mrbaxmypka.gmail.com.mapPointsTrimmer.xml.HtmlHandler;
import mrbaxmypka.gmail.com.mapPointsTrimmer.xml.KmlHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class GoogleIconsServiceTest {
	
	private MultipartDto multipartDto;
	private GoogleIconsService googleIconsService;
	private MultipartFileService mockMultipartFileService;
	private GoogleIconsCache googleIconsCache;
	private Path testKmz = Paths.get("src/test/java/resources/TestKmz.kmz");
	
	@BeforeEach
	public void beforeEach() {
		googleIconsCache = new GoogleIconsCache();
		mockMultipartFileService = Mockito.mock(MultipartFileService.class);
		googleIconsService = new GoogleIconsService(googleIconsCache);
		multipartDto = new MultipartDto(new MockMultipartFile("Test.kml", "Test.kml", null, new byte[]{}));
	}
	
	@ParameterizedTest
	@ValueSource(strings = {"files/bus.png", "http://google.com/mapfiles/kml/shapes/bus.png"})
	public void not_Maps_Google_Url_Should_Be_Returned_Same(String notMapsGoogleUrl) {
		//GIVEN
		
		//WHEN
		String iconHref = googleIconsService.processIconHref(notMapsGoogleUrl, multipartDto);
		
		//THEN
		assertEquals(iconHref, notMapsGoogleUrl);
		//Google icons cache should not contain that icon
		assertFalse(googleIconsCache.containsIconName(notMapsGoogleUrl));
	}
	
	/**
	 * Current "TestKmz.kmz" contains href to the http://maps.google.com/mapfiles/kml/shapes/cabs.png icon with the
	 * previously downloaded one as "cabs.png"
	 */
	@Test
	public void existent_Maps_Google_Icon_In_Kmz_Should_Not_Be_Downloaded() {
		//GIVEN
		multipartDto = Mockito.spy(
				new MultipartDto(
						new MockMultipartFile("Test.kml", "Test.kml", null, new byte[]{})));
		// "cabs.png" as the existing in the user .kmz archive
		Set<String> imagesNamesFromZip = new HashSet<>();
		imagesNamesFromZip.add("cabs.png");
		multipartDto.getImagesNamesFromZip().addAll(imagesNamesFromZip);
		googleIconsService = Mockito.spy(new GoogleIconsService(googleIconsCache));
		
		//WHEN
		String cabsIconHref = googleIconsService.processIconHref("http://maps.google.com/mapfiles/kml/shapes/cabs.png", this.multipartDto);
		
		//THEN
		//The existing icon filename should be returned
		assertEquals("cabs.png", cabsIconHref);
		//ImagesNamesFromZip should be involved to retrieve the existing icon name
		Mockito.verify(multipartDto, Mockito.atLeastOnce()).getImagesNamesFromZip();
		//The icon should not be downloaded and put into the google icons cache
		assertFalse(googleIconsCache.containsIconName("cabs.png"));
	}
	
	@Test
	public void not_Existent_Maps_Google_Icon_In_Kmz_Should_Be_Downloaded_And_Cached() {
		//GIVEN
		
		//WHEN
		String iconHref = googleIconsService.processIconHref("http://maps.google.com/mapfiles/kml/shapes/parks.png", multipartDto);
		
		//THEN
		assertEquals("parks.png", iconHref);
		//Google icons should be downloaded and cached
		assertTrue(googleIconsCache.containsIconName("parks.png"));
		assertNotNull(googleIconsCache.getIcon("parks.png"));
		assertTrue(googleIconsCache.getIcon("parks.png").length > 1024);
		assertEquals(1, multipartDto.getGoogleIconsToBeZipped().size());
	}
}