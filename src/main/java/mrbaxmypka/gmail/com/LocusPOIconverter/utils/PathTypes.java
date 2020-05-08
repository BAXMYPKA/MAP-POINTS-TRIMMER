package mrbaxmypka.gmail.com.LocusPOIconverter.utils;

import java.util.StringJoiner;

public enum PathTypes {
	
	/**
	 * For the absolute local paths like "C:\Documents\MyPOI\"
	 */
	ABSOLUTE("absolute"),
	/**
	 * For the local paths to folders near to file with POIs, like "files/" or "../files/"
	 */
	RELATIVE("relative"),
	/**
	 * For HTTP paths through the Internet
	 */
	WEB("web");
	
	private String type;
	
	PathTypes(String type) {
		this.type = type;
	}
	
	public String getType() {
		return type;
	}
	
	public static PathTypes getByValue(String type) {
		for (PathTypes pathType : PathTypes.values()) {
			if (pathType.type.equalsIgnoreCase(type)) {
				return pathType;
			}
		}
		throw new IllegalArgumentException("No PathType found for value = " + type);
	}
	
	@Override
	public String toString() {
		return new StringJoiner(", ", PathTypes.class.getSimpleName() + "[", "]")
			.add("type='" + type + "'")
			.toString();
	}
}
