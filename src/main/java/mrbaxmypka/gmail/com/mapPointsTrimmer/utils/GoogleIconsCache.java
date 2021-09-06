package mrbaxmypka.gmail.com.mapPointsTrimmer.utils;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe class for keeping downloaded Google Map icons from http://kml4earth.appspot.com/icons.html.
 */
@Component
public class GoogleIconsCache {

    private final int MAX_CACHED_ICONS = 500;
    private final int ICON_SIZE_LIMIT = 1024 * 15; //15 kilobytes
    /**
     * As an intermediate cache for current set of User's previously downloaded google icons.
     */
    private Map<String, byte[]> googleIconsCache = new ConcurrentHashMap<>();

    public boolean putIcon(String iconName, byte[] iconBytes) {
        if (googleIconsCache.size() >= MAX_CACHED_ICONS ||
                iconBytes == null ||
                iconBytes.length == 0 ||
                iconBytes.length > ICON_SIZE_LIMIT) {
            return false;
        } else {
            googleIconsCache.putIfAbsent(iconName, iconBytes);
            return true;
        }
    }

    public byte[] getIcon(String iconName) {
        return googleIconsCache.get(iconName);
    }

    public boolean containsIconName(String iconName) {
        return googleIconsCache.containsKey(iconName);
    }
}
