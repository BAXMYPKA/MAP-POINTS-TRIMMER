package mrbaxmypka.gmail.com.mapPointsTrimmer.utils;

import lombok.ToString;

import java.util.StringJoiner;

/**
 * {@link #ANY} = All the points will be thin out by distance.
 * {@link #EXCLUSIVE} = All the points except ones with the selected icons will be thin out by distance.
 * {@link #INCLUSIVE} = Only the points with the selected icons will be thin out by distance.
 */
@ToString
public enum ThinOutTypes {

    /**
     * To thin out all the points by distance
     */
    ANY("any"),
    /**
     * To thin out points only with the selected icons
     */
    INCLUSIVE("inclusive"),
    /**
     * To thin out all points except the points with the selected icons
     */
    EXCLUSIVE("exclusive");

    private final String type;

    ThinOutTypes(String type) {
        this.type = type;
    }

    public static ThinOutTypes getByValue(String type) {
        for (ThinOutTypes pathType : ThinOutTypes.values()) {
            if (pathType.type.equalsIgnoreCase(type)) {
                return pathType;
            }
        }
        throw new IllegalArgumentException("No ThinOutType found for value = " + type);
    }

    public String getType() {
        return type;
    }

    public boolean isInclusive() {
        return type.equalsIgnoreCase("inclusive");
    }

    public boolean isExclusive() {
        return type.equalsIgnoreCase("exclusive");
    }

    public boolean isAny() {
        return type.equalsIgnoreCase("any");
    }


    @Override
    public String toString() {
        return new StringJoiner(", ", ThinOutTypes.class.getSimpleName() + "[", "]")
                .add("type='" + type + "'")
                .toString();
    }
}
