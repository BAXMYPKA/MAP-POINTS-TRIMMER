package mrbaxmypka.gmail.com.mapPointsTrimmer.utils;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ToString
public enum DownloadAs {
	KML, KMZ;
	
	/**
	 * Case insensitive method.
	 *
	 * @param value "kMl", "KMZ" string etc to be determined as a possible {@link DownloadAs}
	 * @return Enum as the {@link DownloadAs} which is corresponds to a given string value.
	 */
	public static DownloadAs getValue(String value) {
		for (DownloadAs downloadAs : DownloadAs.values()) {
			if (downloadAs.name().equalsIgnoreCase(value)) {
				return downloadAs;
			}
		}
		throw new IllegalArgumentException("No such FileTypes for " + value + " string found!");
	}
	
	/**
	 * Case insensitive method.
	 *
	 * @param filename The name of the file to extract its extension and compare with the current {@link DownloadAs}
	 * @return true if the given 'Filename' has same extension, otherwise 'false'.
	 */
	public boolean hasSameExtension(String filename) {
		try {
			String originalExtension = filename.substring(filename.lastIndexOf(".")).toLowerCase();
			return ".".concat(this.name()).toLowerCase().equals(originalExtension);
		} catch (Exception e) {
			log.info("Filename {} is incorrect!", filename == null ? "null" : filename);
//			e.printStackTrace();
			return false;
		}
	}
	
	
	/**
	 * @return ".kml", ".kmz" etc
	 */
	public String getExtension() {
		return "." + this.name().toLowerCase();
	}
}
