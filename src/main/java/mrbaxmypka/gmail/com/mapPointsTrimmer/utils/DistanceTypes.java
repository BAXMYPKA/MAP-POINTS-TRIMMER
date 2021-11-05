package mrbaxmypka.gmail.com.mapPointsTrimmer.utils;

import lombok.ToString;

import java.util.StringJoiner;

@ToString
public enum DistanceTypes {

    METERS("meters"),
    KILOMETERS("kilometers"),
    MILES("miles"),
    NAUTICAL_MILES("nautical_miles");

    private final String type;

    DistanceTypes(String type) {
        this.type = type;
    }

    public static DistanceTypes getByValue(String type) {
        for (DistanceTypes pathType : DistanceTypes.values()) {
            if (pathType.type.equalsIgnoreCase(type)) {
                return pathType;
            }
        }
        throw new IllegalArgumentException("No DistanceType found for value = " + type);
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", DistanceTypes.class.getSimpleName() + "[", "]")
                .add("type='" + type + "'")
                .toString();
    }
}
