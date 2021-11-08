package mrbaxmypka.gmail.com.mapPointsTrimmer.xml;

import lombok.Getter;
import mrbaxmypka.gmail.com.mapPointsTrimmer.entitiesDto.MultipartMainDto;
import mrbaxmypka.gmail.com.mapPointsTrimmer.entitiesDto.PlacemarkNodeDto;
import mrbaxmypka.gmail.com.mapPointsTrimmer.services.FileService;
import mrbaxmypka.gmail.com.mapPointsTrimmer.utils.ThinOutTypes;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Thins out .kml points by a given distance.
 * If two points are placed to each other closer than a {@link MultipartMainDto#getThinOutDistance()}
 * the second point will be deleted.
 * If a .KMZ file given AND also a .KMZ is supposed to be returned
 * the close spaced points will be deleted with their icons and photos.
 */
@Getter
public class ThinOutKmlPointsHandler extends ThinOutPointsHandler {

    private KmlUtils kmlUtils;
    private FileService fileService;
    private HtmlHandler htmlHandler;
    private List<PlacemarkNodeDto> placemarkNodeDtoList;

    public ThinOutKmlPointsHandler(KmlUtils kmlUtils, FileService fileService, HtmlHandler htmlHandler) {
        this.kmlUtils = kmlUtils;
        this.fileService = fileService;
        this.htmlHandler = htmlHandler;
    }

    void thinOutPoints(Document kmlDocument, MultipartMainDto multipartMainDto) {
        if (multipartMainDto.getThinOutDistance() == null ||
                multipartMainDto.getThinOutDistance() < 1 ||
                multipartMainDto.getThinOutDistance() > 5000) {
            throw new IllegalArgumentException("The minimum or maximum distance is wrong!");
        }

        placemarkNodeDtoList = new ArrayList<>(10);

        NodeList placemarkNodes = kmlDocument.getElementsByTagName("Placemark");
        for (int i = 0; i < placemarkNodes.getLength(); i++) {
            Node placemark = placemarkNodes.item(i);
            PlacemarkNodeDto placemarkNodeDto = new PlacemarkNodeDto(placemark);
            setCoordinates(placemarkNodeDto);
            setIconsNames(placemarkNodeDto);
            setImagesNames(placemarkNodeDto);
            placemarkNodeDtoList.add(placemarkNodeDto);
        }

        if (multipartMainDto.getThinOutType().equals(ThinOutTypes.ANY)) {
            //
        } else if (multipartMainDto.getThinOutType().equals(ThinOutTypes.EXCLUSIVE)) {
            //
        } else if (multipartMainDto.getThinOutType().equals(ThinOutTypes.INCLUSIVE)) {
            thinOutInclusive(kmlDocument, multipartMainDto);
        }
    }

    void thinOutInclusive(Document kmlDocument, MultipartMainDto multipartMainDto) throws IllegalArgumentException {
        if (multipartMainDto.getThinOutIconsNames() == null || multipartMainDto.getThinOutIconsNames().isEmpty()) {
            throw new IllegalArgumentException("When inclusive thinning out you have to select one or more icons!");
        }
        remove(kmlDocument, multipartMainDto);
    }

    void thinOutExclusive(Document kmlDocument, MultipartMainDto multipartMainDto) {

    }

    void thinOutAll(Document kmlDocument, MultipartMainDto multipartMainDto) {

    }

    /**
     * It compares every Placemark from the original list with the cloned comparable copy.
     * If two closely spaced Placemark are found through the iteration
     * it deletes the oldest ones from the original list/
     *
     * @param multipartMainDto
     */
    private void remove(Document kmlDocument, MultipartMainDto multipartMainDto) {
        List<PlacemarkNodeDto> clonedPlacemarksDto = new ArrayList<>(placemarkNodeDtoList);
        placemarkNodeDtoList.removeIf(placemarkNodeDto -> {
            if (canBeDeleted(placemarkNodeDto, clonedPlacemarksDto, multipartMainDto)) {
                clonedPlacemarksDto.remove(placemarkNodeDto);
                kmlDocument.removeChild(placemarkNodeDto.getPlacemarkNode());
                return true;
            } else {
                return false;
            }
        });
        //TODO: to delete images from kmz
    }

    private boolean canBeDeleted(
            PlacemarkNodeDto toBeDeleted, List<PlacemarkNodeDto> compareList, MultipartMainDto multipartMainDto) {

        return compareList.stream().anyMatch(placemarkNodeDto -> {
            double distance = getDistance(toBeDeleted.getLatitude(), toBeDeleted.getLongitude(),
                    placemarkNodeDto.getLatitude(), placemarkNodeDto.getLongitude(),
                    multipartMainDto.getDistanceUnit());
            if (distance < multipartMainDto.getThinOutDistance() &&
                    ThinOutTypes.INCLUSIVE.equals(multipartMainDto.getThinOutType()) &&
                    multipartMainDto.getThinOutIconsNames().contains(toBeDeleted.getIconName())) {
                //Has to be deleted inclusively
                return true;
            } else if (distance < multipartMainDto.getThinOutDistance() &&
                    ThinOutTypes.EXCLUSIVE.equals(multipartMainDto.getThinOutType()) &&
                    multipartMainDto.getThinOutIconsNames().contains(toBeDeleted.getIconName())) {
                //The Placemark with that icon name doesn't allowed for deletion
                return false;
            } else {
                //Any Placemark with any icon name is allowed for deletion
                return true;
            }
        });
    }

    /**
     * Sets {@link PlacemarkNodeDto#setAltitude(Integer)},
     * {@link PlacemarkNodeDto#setLongitude(double)},
     * {@link PlacemarkNodeDto#setLatitude(double)}
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

        placemarkNodeDto.setLongitude(Double.parseDouble(coordinates[0]));
        placemarkNodeDto.setLatitude(Double.parseDouble(coordinates[1]));
        if (coordinates[3] != null) {
            placemarkNodeDto.setAltitude(Integer.parseInt(coordinates[2]));
        }
    }

    /**
     * Sets icons names from a Style or a Normal Style from a StyleMap.
     */
    private void setIconsNames(PlacemarkNodeDto placemarkNodeDto) {
        Node styleUrlNode = kmlUtils.getStyleUrlNodeFromPlacemark(placemarkNodeDto.getPlacemarkNode());
        Node styleObject = kmlUtils.getStyleObject(styleUrlNode.getTextContent());
        if (styleObject.getNodeName().equals("Style")) {
            String hrefToIcon = kmlUtils.getIconHrefNodeFromStyle(styleObject).getTextContent();
            placemarkNodeDto.setIconName(fileService.getFileName(hrefToIcon));
        } else if (styleObject.getNodeName().equals("StyleMap")) {
            kmlUtils.getNormalStyleNodeFromStyleMap(styleObject).ifPresent(normalStyleNode -> {
                String styleUrl = kmlUtils.getIconHrefNodeFromStyle(normalStyleNode).getTextContent();
                placemarkNodeDto.getImageNames().add(fileService.getFileName(styleUrl));
            });
//            kmlUtils.getHighlightStyleNodeFromStyleMap(styleObject).ifPresent(highlightStyleNode -> {
//                String styleUrl = kmlUtils.getIconHrefNodeFromStyle(highlightStyleNode).getTextContent();
//                placemarkNodeDto.getImageNames().add(fileService.getFileName(styleUrl));
//            });
        }
    }

    private void setImagesNames(PlacemarkNodeDto placemarkNodeDto) {
        Node descriptionNode = kmlUtils.getDescriptionNodeFromPlacemark(placemarkNodeDto.getPlacemarkNode());
        htmlHandler.getAllImagesFromDescription(descriptionNode.getTextContent()).forEach(src -> {
            placemarkNodeDto.getImageNames().add(fileService.getFileName(src));
        });
    }

}
