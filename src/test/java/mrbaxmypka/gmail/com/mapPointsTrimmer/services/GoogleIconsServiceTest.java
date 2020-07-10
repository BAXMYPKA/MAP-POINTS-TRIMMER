package mrbaxmypka.gmail.com.mapPointsTrimmer.services;

import mrbaxmypka.gmail.com.mapPointsTrimmer.utils.GoogleIconsCache;
import mrbaxmypka.gmail.com.mapPointsTrimmer.xml.KmlHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.verification.VerificationMode;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class GoogleIconsServiceTest {
	
	private static GoogleIconsService googleIconsService;
	@Mock
	private static MultipartFileService mockMultipartFileService;
	private static GoogleIconsCache googleIconsCache;
	private Path testKmz = Paths.get("/src/test/java/resources/TestKmz.kmz");
	
	@BeforeEach
	public void beforeEach() {
		googleIconsCache = new GoogleIconsCache();
		mockMultipartFileService = Mockito.mock(MultipartFileService.class);
		googleIconsService = new GoogleIconsService(mockMultipartFileService, googleIconsCache);
	}
	
	@ParameterizedTest
	@ValueSource(strings = {"files/bus.png", "http://google.com/mapfiles/kml/shapes/bus.png"})
	public void not_Maps_Google_Url_Should_Be_Returned_Same(String notMapsGoogleUrl) {
		//GIVEN
		
		//WHEN
		String iconHref = googleIconsService.processIconHref(notMapsGoogleUrl);
		
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
		//GIVEN "cabs.png" as the existing in the user .kmz archive
		Set<String> imagesNamesFromZip = new HashSet<>();
		imagesNamesFromZip.add("cabs.png");
		Mockito.when(mockMultipartFileService.getImagesNamesFromZip()).thenReturn(imagesNamesFromZip);
		googleIconsService = Mockito.spy(new GoogleIconsService(mockMultipartFileService, googleIconsCache));
		
		//WHEN
		String cabsIconHref = googleIconsService.processIconHref("http://maps.google.com/mapfiles/kml/shapes/cabs.png");
		
		//THEN
		//The existing icon filename should be returned
		assertEquals("cabs.png", cabsIconHref);
		//ImagesNamesFromZip should be involved to retrieve the existing icon name
		Mockito.verify(mockMultipartFileService, Mockito.times(1)).getImagesNamesFromZip();
		//The icon should not be downloaded and put into the google icons cache
		assertFalse(googleIconsCache.containsIconName("cabs.png"));
	}
	
	@Test
	public void not_Existent_Maps_Google_Icon_Should_Be_Downloaded_And_Cached() {
		//GIVEN
		Mockito.when(mockMultipartFileService.getImagesNamesFromZip()).thenReturn(new HashSet<String>());
		
		//WHEN
		String iconHref = googleIconsService.processIconHref("http://maps.google.com/mapfiles/kml/shapes/parks.png");
		
		//THEN
		assertEquals("parks.png", iconHref);
		//Google icons should be downloaded and cached
		assertTrue(googleIconsCache.containsIconName("parks.png"));
	}
	
	//TODO: check timeouts for downloading
}