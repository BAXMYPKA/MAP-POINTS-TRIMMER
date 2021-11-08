package mrbaxmypka.gmail.com.mapPointsTrimmer.utils;

import lombok.ToString;

import java.util.StringJoiner;

@ToString
public enum DistanceUnits {

    METERS("meters"),
    KILOMETERS("kilometers"),
    MILES("miles"),
    YARDS("yards"),
    NAUTICAL_MILES("nautical_miles");

    private final String unit;

    DistanceUnits(String unit) {
        this.unit = unit;
    }

    public static DistanceUnits getByValue(String unit) {
        for (DistanceUnits distUnit : DistanceUnits.values()) {
            if (distUnit.unit.equalsIgnoreCase(unit)) {
                return distUnit;
            }
        }
        throw new IllegalArgumentException("No DistanceUnit found for value = " + unit);
    }

    public String getUnit() {
        return unit;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", DistanceUnits.class.getSimpleName() + "[", "]")
                .add("unit='" + unit + "'")
                .toString();
    }
}
