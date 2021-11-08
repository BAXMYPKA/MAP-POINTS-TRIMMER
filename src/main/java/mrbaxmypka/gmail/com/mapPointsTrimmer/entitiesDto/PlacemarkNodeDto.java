package mrbaxmypka.gmail.com.mapPointsTrimmer.entitiesDto;

import lombok.Getter;
import lombok.Setter;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
public class PlacemarkNodeDto {

    private Node placemarkNode;

    private double longitude;

    private double latitude;

    private Integer altitude;

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
