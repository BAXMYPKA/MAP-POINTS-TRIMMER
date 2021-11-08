package mrbaxmypka.gmail.com.mapPointsTrimmer.entitiesDto;

import lombok.Getter;
import lombok.Setter;
import org.w3c.dom.Node;

import java.util.List;

@Getter
@Setter
public class PlacemarkNodeDto {

    private Node placemarkNode;

    private float longitude;

    private float latitude;

    private Integer altitude;

    /**
     * The name of the icon for this Placemark
     */
    private String iconName;

    /**
     * Images names that Placemark could contain in it's Description.
     */
    private List<String> imageNames;

    public PlacemarkNodeDto(Node placemarkNode) {
        this.placemarkNode = placemarkNode;
    }
}
