package mrbaxmypka.gmail.com.mapPointsTrimmer.utils;

import lombok.ToString;

import java.util.StringJoiner;

@ToString
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
	
	private final String type;
	
	PathTypes(String type) {
		this.type = type;
	}
	
	public static PathTypes getByValue(String type) {
		for (PathTypes pathType : PathTypes.values()) {
			if (pathType.type.equalsIgnoreCase(type)) {
				return pathType;
			}
		}
		throw new IllegalArgumentException("No PathType found for value = " + type);
	}
	
	public String getType() {
		return type;
	}
	
	@Override
	public String toString() {
		return new StringJoiner(", ", PathTypes.class.getSimpleName() + "[", "]")
			.add("type='" + type + "'")
			.toString();
	}
}
