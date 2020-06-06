package mrbaxmypka.gmail.com.mapPointsTrimmer.utils;

public enum FileTypes {
	KML, KMZ;
	
	public static FileTypes getValue(String value) {
		for (FileTypes fileTypes : FileTypes.values()) {
			if (fileTypes.name().equalsIgnoreCase(value)) {
				return fileTypes;
			}
		}
		throw new IllegalArgumentException("No such FileTypes for " + value+ " string found!");
	}
}
