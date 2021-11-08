package mrbaxmypka.gmail.com.mapPointsTrimmer.xml;

import mrbaxmypka.gmail.com.mapPointsTrimmer.entitiesDto.MultipartMainDto;
import mrbaxmypka.gmail.com.mapPointsTrimmer.entitiesDto.PlacemarkNodeDto;
import mrbaxmypka.gmail.com.mapPointsTrimmer.services.FileService;
import mrbaxmypka.gmail.com.mapPointsTrimmer.utils.ThinOutTypes;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Arrays;

/**
 * Thins out .kml points by a given distance.
 * If two points are placed to each other closer than a {@link MultipartMainDto#getThinOutDistance()}
 * the second point will be deleted.
 * If a .KMZ file given AND also a .KMZ is supposed to be returned
 * the close spaced points will be deleted with their icons and photos.
 */
public class ThinOutKmlPointsHandler extends ThinOutPointsHandler {

    private KmlUtils kmlUtils;
    private FileService fileService;

    public ThinOutKmlPointsHandler(KmlUtils kmlUtils, FileService fileService) {
        this.kmlUtils = kmlUtils;
        this.fileService = fileService;
    }

    void thinOutPoints(Document kmlDocument, MultipartMainDto multipartMainDto) {
        if (multipartMainDto.getThinOutType().equals(ThinOutTypes.ANY)) {
            //
        } else if (multipartMainDto.getThinOutType().equals(ThinOutTypes.EXCLUSIVE)) {
            //
        } else if (multipartMainDto.getThinOutType().equals(ThinOutTypes.INCLUSIVE)) {
            //
        }
    }

    void thinOutInclusive(Document kmlDocument, MultipartMainDto multipartMainDto) throws IllegalArgumentException {
        if (multipartMainDto.getThinOutIcons() == null || multipartMainDto.getThinOutIcons().isEmpty()) {
            throw new IllegalArgumentException("When inclusive thinning out you have to select one or more icons!");
        }
        NodeList placemarkNodes = kmlDocument.getElementsByTagName("Placemark");
        for (int i = 0; i < placemarkNodes.getLength(); i++) {
            Node placemark = placemarkNodes.item(i);
            PlacemarkNodeDto placemarkNodeDto = new PlacemarkNodeDto(placemark);
            setCoordinates(placemarkNodeDto);
        }
    }

    private void inclusiveCompare(NodeList placemarkNodes, Node comparePlacemark) {
        Node coordinatesNode = kmlUtils.getCoordinatesNodeFromPlacemark(comparePlacemark);

        for (int i = 0; i < placemarkNodes.getLength(); i++) {
            Node placemark = placemarkNodes.item(i);
            if (placemark.isSameNode(comparePlacemark)) {
                continue;
            }

        }
    }

    /**
     * Sets {@link PlacemarkNodeDto#setAltitude(Integer)},
     * {@link PlacemarkNodeDto#setLongitude(float)},
     * {@link PlacemarkNodeDto#setLatitude(float)}
     * The "coordinates" Node is a required node.
     * {@literal <coordinates>-119.779550,33.829268,0</coordinates> or <coordinates>-122.000,37.002</coordinates>}
     * A single tuple consisting of floating point values for longitude, latitude, and altitude (in that order).
     * Longitude and latitude values are in degrees, where
     * longitude ≥ −180 and <= 180
     * latitude ≥ −90 and ≤ 90
     * altitude values (optional) are in meters above sea level
     * Do not include spaces between the three values that describe a coordinate.
     *
     * @param placemarkNodeDto
     */
    private void setCoordinates(PlacemarkNodeDto placemarkNodeDto) {
        Node coordinatesNode = kmlUtils.getCoordinatesNodeFromPlacemark(placemarkNodeDto.getPlacemarkNode());
        String[] coordinates = coordinatesNode.getTextContent().split(",");

        placemarkNodeDto.setLongitude(Float.parseFloat(coordinates[0]));
        placemarkNodeDto.setLatitude(Float.parseFloat(coordinates[1]));

        if (coordinates[3] != null) {
            placemarkNodeDto.setAltitude(Integer.parseInt(coordinates[2]));
        }
    }

    private void setIconName(PlacemarkNodeDto placemarkNodeDto) {
        Node styleUrlNode = kmlUtils.getStyleUrlNodeFromPlacemark(placemarkNodeDto.getPlacemarkNode());
        Node styleObject = kmlUtils.getStyleObject(styleUrlNode.getTextContent());
        if (styleObject.getNodeName().equals("Style")) {
            String hrefToIcon = kmlUtils.getIconHrefNodeFromStyle(styleObject).getTextContent();
            placemarkNodeDto.setIconName(fileService.getFileName(hrefToIcon));
        }
    }

    void thinOutExclusive(Document kmlDocument, MultipartMainDto multipartMainDto) {

    }

    void thinOutAll(Document kmlDocument, MultipartMainDto multipartMainDto) {

    }
}
