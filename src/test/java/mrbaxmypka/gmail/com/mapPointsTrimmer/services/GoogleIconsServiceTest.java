package mrbaxmypka.gmail.com.mapPointsTrimmer.services;

import mrbaxmypka.gmail.com.mapPointsTrimmer.entitiesDto.MultipartDto;
import mrbaxmypka.gmail.com.mapPointsTrimmer.utils.DownloadAs;
import mrbaxmypka.gmail.com.mapPointsTrimmer.utils.GoogleIconsCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.context.MessageSource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class GoogleIconsServiceTest {
	
	private MultipartDto multipartDto;
	private GoogleIconsService googleIconsService;
	private GoogleIconsCache googleIconsCache;
	private MessageSource messageSource;
	private ResourceLoader resourceLoader;
	private FileService fileService;
	private Resource resource;
	private final String CLASSPATH_TO_DIRECTORY = "classpath:static/pictograms";
	private final String EXISTING_GOOGLE_ICON = "http://maps.google.com/mapfiles/kml/shapes/cabs.png";
	private final String NOT_EXISTING_GOOGLE_ICON = "http://maps.google.com/mapfiles/kml/shapes/parks.png";
	
	
	@BeforeEach
	public void beforeEach() throws IOException {
		messageSource = Mockito.mock(MessageSource.class);
		resourceLoader = Mockito.mock(ResourceLoader.class);
		resource = Mockito.mock(Resource.class);
		Mockito.when(resourceLoader.getResource(CLASSPATH_TO_DIRECTORY)).thenReturn(resource);
		Mockito.when(resource.getInputStream()).thenReturn(new ByteArrayInputStream("Pictogram1.png".getBytes(StandardCharsets.UTF_8)));
		fileService = new FileService(messageSource, resourceLoader);
		multipartDto = new MultipartDto(new MockMultipartFile("Test.kml", "Test.kml", null, new byte[]{}));
		multipartDto.setDownloadAs(DownloadAs.KMZ);
		
		googleIconsCache = new GoogleIconsCache();
		googleIconsService = new GoogleIconsService(googleIconsCache);
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
	public void existing_Maps_Google_Icon_In_Kmz_Should_Not_Be_Downloaded() {
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
		String cabsIconHref = googleIconsService.processIconHref(EXISTING_GOOGLE_ICON, this.multipartDto);
		
		//THEN
		//The existing icon filename should be returned
		assertEquals("cabs.png", cabsIconHref);
		//ImagesNamesFromZip should be involved to retrieve the existing icon name
		Mockito.verify(multipartDto, Mockito.atLeastOnce()).getImagesNamesFromZip();
		//The icon should not be downloaded and put into the google icons cache
		assertFalse(googleIconsCache.containsIconName("cabs.png"));
	}
	
	@Test
	public void not_Existing_Maps_Google_Icon_In_Kmz_Should_Be_Downloaded_And_Cached() {
		//GIVEN
		
		//WHEN
		String iconHref = googleIconsService.processIconHref(NOT_EXISTING_GOOGLE_ICON, multipartDto);
		
		//THEN
		assertEquals("parks.png", iconHref);
		//Google icons should be downloaded and cached
		assertTrue(googleIconsCache.containsIconName("parks.png"));
		assertNotNull(googleIconsCache.getIcon("parks.png"));
		assertTrue(googleIconsCache.getIcon("parks.png").length > 1024);
		assertEquals(1, multipartDto.getGoogleIconsToBeZipped().size());
	}

	@Test
	public void not_Existing_Maps_Google_Icon_In_Kmz_Should_Not_Be_Downloaded_And_Cached_When_DowloadAsKml() {
		//GIVEN
		multipartDto.setDownloadAs(DownloadAs.KML);

		//WHEN
		String iconHref = googleIconsService.processIconHref(NOT_EXISTING_GOOGLE_ICON, multipartDto);

		//THEN
		assertEquals(NOT_EXISTING_GOOGLE_ICON, iconHref);
		//Google icons should not be downloaded and cached
		assertFalse(googleIconsCache.containsIconName("parks.png"));
		assertEquals(0, multipartDto.getGoogleIconsToBeZipped().size());
	}

}