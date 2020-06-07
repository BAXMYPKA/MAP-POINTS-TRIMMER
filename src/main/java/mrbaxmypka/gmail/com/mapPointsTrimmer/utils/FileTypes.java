package mrbaxmypka.gmail.com.mapPointsTrimmer.utils;

public enum FileTypes {
	KML, KMZ;
	
	/**
	 * Case insensitive method.
	 * @param value "kMl", "KMZ" string etc to be determined as a possible {@link FileTypes}
	 * @return Enum as the {@link FileTypes} which is corresponds to a given string value.
	 */
	public static FileTypes getValue(String value) {
		for (FileTypes fileTypes : FileTypes.values()) {
			if (fileTypes.name().equalsIgnoreCase(value)) {
				return fileTypes;
			}
		}
		throw new IllegalArgumentException("No such FileTypes for " + value+ " string found!");
	}
	
	/**
	 * Case insensitive method.
	 * @param filename The name of the file to extract its extension and compare with the current {@link FileTypes}
	 * @return true if the given 'Filename' has same extension, otherwise 'false'.
	 */
	public boolean isSameExtension(String filename) {
		try {
			String originalExtension = filename.substring(filename.lastIndexOf(".")).toLowerCase();
			return ".".concat(this.name()).toLowerCase().equals(originalExtension);
		} catch (Exception e) {
//			e.printStackTrace();
			return false;
		}
	}
}
