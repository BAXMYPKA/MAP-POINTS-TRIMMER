package mrbaxmypka.gmail.com.mapPointsTrimmer.services;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

@Slf4j
@NoArgsConstructor
@Component
public class GoogleIconsService {
	
	private final int ICON_SIZE_LIMIT = 1024 * 15; //15 kilobytes
	private final String GOOGLE_URL = "http://maps.google.com/";
	@Getter
	private Set<String> iconsNamesFromZip = new HashSet<>();
	private final int MAX_CACHED_ICONS = 300;
	/**
	 * As an intermediate cache for current set of User's previously downloaded google icons.
	 * MUST be cleared after every processing of .kmz.
	 */
	@Getter
//	@Setter(AccessLevel.PUBLIC)
	private Map<String, byte[]> downloadedGoogleIcons = new LinkedHashMap<>();
	
	
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
		return getIconsNamesFromZip().stream()
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
	 * If download success it will put an icon filename and bytes array into {@link #getDownloadedGoogleIcons()}
	 *
	 * @return A newly downloaded icon filename or the initial url if failed.
	 */
	private String downloadIcon(String googleUrl) {
		try {
			URLConnection urlConnection = new URL(googleUrl).openConnection();
			urlConnection.setConnectTimeout(2000);
			urlConnection.setReadTimeout(2000);
			InputStream is = urlConnection.getInputStream();
			byte[] bytes = is.readAllBytes();
			if (bytes.length > ICON_SIZE_LIMIT) {
				return googleUrl;
			}
			getDownloadedGoogleIcons().put(getIconFilename(googleUrl), bytes);
		} catch (IOException e) {
			log.trace(e.getMessage(), e);
			return googleUrl;
		}
		return getIconFilename(googleUrl);
	}
	
	public void clearGoogleIconsCache() {
		//TODO: to be tested
		//Clear cache
		if (downloadedGoogleIcons.size() > MAX_CACHED_ICONS) {
			Iterator<Map.Entry<String, byte[]>> iterator = downloadedGoogleIcons.entrySet().iterator();
			while (downloadedGoogleIcons.size() > MAX_CACHED_ICONS && iterator.hasNext()) {
				iterator.next();
				iterator.remove();
			}
		}
//		iconsNamesFromZip.clear();
	}
}
