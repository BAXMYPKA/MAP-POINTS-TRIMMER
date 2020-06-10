package mrbaxmypka.gmail.com.mapPointsTrimmer.utils;

import lombok.ToString;

@ToString
public enum PreviewSizeUnits {
	PIXELS("px"),
	PERCENTAGE("%");
	
	private final String unit;
	
	PreviewSizeUnits(String unit) {
		this.unit = unit;
	}
	
	/**
	 * @param unit In any lettercase: "pIxels" or "Percentage"
	 * @return corresponding {@link PreviewSizeUnits}
	 * @throws IllegalArgumentException When no match found
	 */
	public static PreviewSizeUnits getByTypeName(String unit) throws IllegalArgumentException {
		for (PreviewSizeUnits units : PreviewSizeUnits.values()) {
			if (units.name().equalsIgnoreCase(unit)) {
				return units;
			}
		}
		throw new IllegalArgumentException("No PreviewSizeUnit for " + unit + " found!");
	}
	
	/**
	 * @return '%' or 'px' exact units
	 */
	public String getUnit() {
		return this.unit;
	}
}
