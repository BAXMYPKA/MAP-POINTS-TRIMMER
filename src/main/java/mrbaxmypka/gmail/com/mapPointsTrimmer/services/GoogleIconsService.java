package mrbaxmypka.gmail.com.mapPointsTrimmer.services;

import lombok.extern.slf4j.Slf4j;
import mrbaxmypka.gmail.com.mapPointsTrimmer.entitiesDto.MultipartDto;
import mrbaxmypka.gmail.com.mapPointsTrimmer.utils.DownloadAs;
import mrbaxmypka.gmail.com.mapPointsTrimmer.utils.GoogleIconsCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Google Earth has special hrefs to icons as "http://maps.google.com/" which internally redirect to its local image store.
 * Thus those types of hrefs aren't retrieved from the Internet within Google Earth BUT will require the Internet from
 * any other application.
 * So not to use Internet next time, if the given href starts with {@link #MAPS_GOOGLE_URL} this class will try do download the icon
 * or replace this href with the previously downloaded icon (if any).
 */
@Slf4j
@Component
public class GoogleIconsService {

    private final String MAPS_GOOGLE_URL = "http://maps.google.com/";
    private final GoogleIconsCache googleIconsCache;

    @Autowired
    public GoogleIconsService(GoogleIconsCache googleIconsCache) {
        this.googleIconsCache = googleIconsCache;
    }

    /**
     * Google Earth has special href to icons it internally redirects to it local image store.
     * If the given {@link MultipartDto#getDownloadAs()} == {@link DownloadAs#KMZ}
     * and the given href starts with {@link #MAPS_GOOGLE_URL} this method will try do download the icon.
     *
     * @param href         Href or src to an icon to be evaluated
     * @param multipartDto To examine its current icons and images cache {@link MultipartDto#getImagesNamesFromZip()} (if any)
     *                     whether such an icon is already present among archive.
     * @return If the given href don't start with {@link #MAPS_GOOGLE_URL} the initial href will be returned.
     * If the given user zip archive contains the icon name from the given href, the icon name without url will be returned.
     * Otherwise this method will try to download the icon and return the downloaded icon filename of the initial url if failed.
     */
    public String processIconHref(String href, MultipartDto multipartDto) {
        log.trace("Href to evaluate as GoogleMap special = '{}'", href == null ? "null" : href);
        if (href == null) return "";
        if (href.startsWith(MAPS_GOOGLE_URL)) {
            String downloadedHref = getResolvedHref(href, multipartDto);
            if (downloadedHref.startsWith(MAPS_GOOGLE_URL)) {
                //Failed to download the icon, return the initial filename
                return href;
            } else if (!googleIconsCache.containsIconName(downloadedHref)) {
                //The icon with the name is already presented in a current zip and hasn't been downloaded
                return downloadedHref;
            } else {
                //Icon hasn't been presented in a current zip and has been downloaded to GoogleIconsCache as a new one
                multipartDto.getGoogleIconsToBeZipped().put(downloadedHref, googleIconsCache.getIcon(downloadedHref));
                return downloadedHref;
            }
        } else {
            return href;
        }
    }

    /**
     * @param googleUrl An {@link URL} which starts with {@link #MAPS_GOOGLE_URL}
     * @return If a current zip archive from User contain an icon filename from the given URL that filename will be returned.
     * Otherwise it will try to download the icon. If success, the filename of the downloaded icon will be returned,
     * if isn't (e.g. no internet connection) it return the initial Google URL.
     */
    private String getResolvedHref(String googleUrl, MultipartDto multipartDto) {
        return multipartDto.getImagesNamesFromZip().stream()
                .filter(imageFromZip -> imageFromZip.equals(getIconFilename(googleUrl)))
                .findFirst() //Returns a previously downloaded filename presented in the given .kmz
                .orElseGet(() -> {
                    if (DownloadAs.KML.equals(multipartDto.getDownloadAs())) {
                        return googleUrl; //No need to download
                    } else {
                        return downloadIcon(googleUrl); //To be included into the resultant .kmz
                    }
                });
    }

    /**
     * @param googleHref A full URL to an icon
     * @return Only the icon filename without Internet address.
     */
    private String getIconFilename(String googleHref) {
        if (googleHref.startsWith(MAPS_GOOGLE_URL)) {
            return googleHref.substring(googleHref.lastIndexOf("/") + 1);
        } else {
            return googleHref;
        }
    }

    /**
     * If download success it will put an icon filename and bytes array into {@link GoogleIconsCache#putIcon(String, byte[])}
     *
     * @return A newly downloaded icon filename or the initial url if failed.
     */
    private String downloadIcon(String googleUrl) {
        if (googleIconsCache.containsIconName(getIconFilename(googleUrl))) {
            return getIconFilename(googleUrl);
        } else {
            try {
                URLConnection urlConnection = new URL(googleUrl).openConnection();
                urlConnection.setConnectTimeout(2000);
                urlConnection.setReadTimeout(1500);
                InputStream is = urlConnection.getInputStream();
                byte[] iconBytes = is.readAllBytes();
                if (googleIconsCache.putIcon(getIconFilename(googleUrl), iconBytes)) {
                    return getIconFilename(googleUrl);
                } else {
                    return googleUrl;
                }
            } catch (IOException e) {
                log.debug(e.getMessage(), e);
                return googleUrl;
            }
        }
    }
}
