package mrbaxmypka.gmail.com.mapPointsTrimmer.services;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

@Slf4j
@NoArgsConstructor
@Component
public class GoogleIconsService {
	
	private final String GOOGLE_URL = "http://maps.google.com/";
	@Autowired
	private MultipartFileService multipartFileService;
	
	/**
	 * Google Earth has special href to icons it internally redirects to it local image store.
	 * If the given href starts with {@link #GOOGLE_URL} this method will try do download the icon.
	 *
	 * @param href Href or src to an icon to be evaluated
	 * @return If the given href don't start with {@link #GOOGLE_URL} the initial href will be returned.
	 * If the given user zip archive contains the icon name from the given href, the icon name without url will be returned.
	 * Otherwise this method will try to download the icon and return the downloaded icon filename of the initial url if failed.
	 */
	public String processIconHref(String href) {
		log.trace("Href to evaluate as GoogleMap special = '{}'", href);
		if (href.startsWith(GOOGLE_URL)) {
			return getDownloadedHref(href);
		} else {
			return href;
		}
	}
	
	/**
	 * @param googleUrl An {@link URL} which starts with {@link #GOOGLE_URL}
	 * @return If a current zip archive from User contain an icon filename from the given URL that filename will be returned.
	 * Otherwise it will try to download the icon. If success, the filename of the downloaded icon will be returned,
	 * if isn't (e.g. no internet connection) it return the initial URL.
	 */
	private String getDownloadedHref(String googleUrl) {
		return multipartFileService.getIconsNamesFromZip().stream()
			.filter(s -> s.equals(getIconFilename(googleUrl)))
			.findFirst()
			.orElse(downloadIcon(googleUrl));
	}
	
	/**
	 * @param googleHref A full URL to an icon
	 * @return Only the icon filename without Internet address.
	 */
	private String getIconFilename(String googleHref) {
		if (googleHref.startsWith(GOOGLE_URL)) {
			return googleHref.substring(googleHref.lastIndexOf("/"));
		} else {
			return googleHref;
		}
	}
	
	/**
	 * If download success it will put an icon filename and bytes array into {@link MultipartFileService#getDownloadedGoogleIcons()}
	 *
	 * @return A newly downloaded icon filename or the initial url if failed.
	 */
	private String downloadIcon(String googleUrl) {
		try {
			URLConnection urlConnection = new URL(googleUrl).openConnection();
			urlConnection.setConnectTimeout(1000);
			urlConnection.setReadTimeout(1000);
			InputStream is = urlConnection.getInputStream();
			byte[] bytes = is.readAllBytes();
			multipartFileService.getDownloadedGoogleIcons().put(getIconFilename(googleUrl), bytes);
		} catch (IOException e) {
			log.trace(e.getMessage(), e);
			return googleUrl;
		}
		return getIconFilename(googleUrl);
	}
}
