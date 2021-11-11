package mrbaxmypka.gmail.com.mapPointsTrimmer.entitiesDto;

import lombok.Getter;
import lombok.Setter;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class PlacemarkNodeDto {

    private Node placemarkNode;

    /**
     * The "coordinates" Node is a required node.
     * {@literal Either <coordinates>-119.779550,33.829268,0</coordinates>
     * or <coordinates>-122.000,37.002,127.00</coordinates> or <coordinates>-122.000,37.002</coordinates>}
     * A single tuple consisting of floating point values for longitude, latitude, and altitude (in that order).
     * Longitude and latitude values are in degrees, where
     * longitude ≥ −180 and <= 180
     * latitude ≥ −90 and ≤ 90
     * altitude values (optional) are in meters above sea level.
     */
    private double longitude;

    /**
     * The "coordinates" Node is a required node.
     * {@literal Either <coordinates>-119.779550,33.829268,0</coordinates>
     * or <coordinates>-122.000,37.002,127.00</coordinates> or <coordinates>-122.000,37.002</coordinates>}
     * A single tuple consisting of floating point values for longitude, latitude, and altitude (in that order).
     * Longitude and latitude values are in degrees, where
     * longitude ≥ −180 and <= 180
     * latitude ≥ −90 and ≤ 90
     * altitude values (optional) are in meters above sea level.
     */
    private double latitude;

    /**
     * The "coordinates" Node is a required node.
     * {@literal Either <coordinates>-119.779550,33.829268,0</coordinates>
     * or <coordinates>-122.000,37.002,127.00</coordinates> or <coordinates>-122.000,37.002</coordinates>}
     * A single tuple consisting of floating point values for longitude, latitude, and altitude (in that order).
     * Longitude and latitude values are in degrees, where
     * longitude ≥ −180 and <= 180
     * latitude ≥ −90 and ≤ 90
     * altitude values (optional) are in meters above sea level.
     */
    private double altitude;

    /**
     * The name of an icon for this Placemark (those may be from its Style or NormalStyle from StyleMap).
     * Have to be deleted from the resulting KML file if it is either a Locus Pro specific photo pictogram
     * or a unique one only for this Placemark.
     */
    private String iconName;

    /**
     * Images names that Placemark could contain in it's Description.
     */
    private List<String> imageNames = new ArrayList<>(1);

    public PlacemarkNodeDto(Node placemarkNode) {
        this.placemarkNode = placemarkNode;
    }
}
