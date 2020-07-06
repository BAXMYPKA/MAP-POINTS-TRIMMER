package mrbaxmypka.gmail.com.mapPointsTrimmer.utils;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class GoogleIconsCache {
	
	private final int MAX_CACHED_ICONS = 300;
	private final int ICON_SIZE_LIMIT = 1024 * 15; //15 kilobytes
	/**
	 * As an intermediate cache for current set of User's previously downloaded google icons.
	 * MUST be cleared after every processing of .kmz.
	 */
	private Map<String, byte[]> googleIconsCache = new ConcurrentHashMap<>();
	
	public boolean putIcon(String iconName, byte[] iconBytes) {
		if (googleIconsCache.size() < MAX_CACHED_ICONS || iconBytes.length == 0 || iconBytes.length > ICON_SIZE_LIMIT) {
			return false;
		} else {
			googleIconsCache.put(iconName, iconBytes);
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
