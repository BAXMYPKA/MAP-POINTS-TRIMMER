package mrbaxmypka.gmail.com.mapPointsTrimmer.xml;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import mrbaxmypka.gmail.com.mapPointsTrimmer.entitiesDto.MultipartMainDto;
import mrbaxmypka.gmail.com.mapPointsTrimmer.entitiesDto.PlacemarkNodeDto;
import mrbaxmypka.gmail.com.mapPointsTrimmer.services.FileService;
import mrbaxmypka.gmail.com.mapPointsTrimmer.utils.DateTimeParser;
import mrbaxmypka.gmail.com.mapPointsTrimmer.utils.ThinOutTypes;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Thins out .kml points by a given distance.
 * If two points are placed to each other closer than a {@link MultipartMainDto#getThinOutDistance()}
 * the second point will be deleted.
 * If a .KMZ file given AND also a .KMZ is supposed to be returned
 * the close spaced points will be deleted with their icons and photos.
 */
@Slf4j
@Getter
public class ThinOutKmlPointsHandler extends ThinOutPointsHandler {

    private KmlUtils kmlUtils;
    private FileService fileService;
    private HtmlHandler htmlHandler;
    private LocusIconsHandler locusIconsHandler;
    private List<PlacemarkNodeDto> placemarkNodeDtoList;
    private DateTimeParser dateTimeParser;

    public ThinOutKmlPointsHandler(KmlUtils kmlUtils, FileService fileService, HtmlHandler htmlHandler) {
        this.kmlUtils = kmlUtils;
        this.fileService = fileService;
        this.htmlHandler = htmlHandler;
        this.locusIconsHandler = new LocusIconsHandler(fileService, kmlUtils);
        this.dateTimeParser = new DateTimeParser();

    }

    void thinOutPoints(Document kmlDocument, MultipartMainDto multipartMainDto) {
        if (multipartMainDto.getThinOutDistance() == null ||
                multipartMainDto.getThinOutDistance() < 1 ||
                multipartMainDto.getThinOutDistance() > 5000) {
            throw new IllegalArgumentException("The minimum or maximum distance is wrong!");
        }
        setPlacemarkNodeDtoList(kmlDocument);

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
        removePlacemarks(kmlDocument, multipartMainDto);
    }

    /**
     * It compares every Placemark from the original list with the cloned comparable copy.
     * If two closely spaced Placemark are found through the iteration
     * it deletes the oldest ones from the original list/
     *
     * @param multipartMainDto
     */
    private void removePlacemarks(Document kmlDocument, MultipartMainDto multipartMainDto) {
        List<PlacemarkNodeDto> clonedPlacemarksDto = new ArrayList<>(placemarkNodeDtoList);
        placemarkNodeDtoList.removeIf(placemarkNodeDto -> {
            if (canBeDeleted(placemarkNodeDto, clonedPlacemarksDto, multipartMainDto)) {
                //Also remove this placemarkDto from the cloned List not to compare it twice in the future
                clonedPlacemarksDto.remove(placemarkNodeDto);
                removePlacemarkImagesFromKmz(placemarkNodeDto, multipartMainDto);
                //Remove from the main Document
                kmlDocument.removeChild(placemarkNodeDto.getPlacemarkNode());
                return true;
            } else {
                return false;
            }
        });
    }

    private boolean canBeDeleted(
            PlacemarkNodeDto placemarkToBeDeleted, List<PlacemarkNodeDto> compareList, MultipartMainDto multipartMainDto) {

        //TODO: to compare by date

        return compareList.stream().anyMatch(placemarkToBeCompared -> {
            double distance = getDistance(placemarkToBeDeleted.getLatitude(), placemarkToBeDeleted.getLongitude(),
                    placemarkToBeCompared.getLatitude(), placemarkToBeCompared.getLongitude(),
                    multipartMainDto.getDistanceUnit());
            if (distance < multipartMainDto.getThinOutDistance() &&
                    ThinOutTypes.INCLUSIVE.equals(multipartMainDto.getThinOutType()) &&
                    multipartMainDto.getThinOutIconsNames().contains(placemarkToBeDeleted.getIconName())) {
                //Has to be deleted inclusively
                return true;
            } else if (distance < multipartMainDto.getThinOutDistance() &&
                    ThinOutTypes.EXCLUSIVE.equals(multipartMainDto.getThinOutType()) &&
                    multipartMainDto.getThinOutIconsNames().contains(placemarkToBeDeleted.getIconName())) {
                //The Placemark with that icon name doesn't allowed for deletion
                return false;
            } else {
                //Any Placemark with any icon name is allowed for deletion
                return true;
            }
        });
    }

    private boolean canBeDeletedByDate(PlacemarkNodeDto placemarkToBeDeleted, PlacemarkNodeDto placemarkToBeCompared) {
        Node whenToBeDeleted = kmlUtils.getWhenNode(kmlUtils.getGxTimeStampNode(placemarkToBeDeleted.getPlacemarkNode()));
        LocalDateTime dateTimeToBeDeleted = dateTimeParser.parseWhenLocalDateTime(whenToBeDeleted.getTextContent());

        Node whenToBeCompared = kmlUtils.getWhenNode(kmlUtils.getGxTimeStampNode(placemarkToBeCompared.getPlacemarkNode()));
        LocalDateTime dateTimeToBeCompared = dateTimeParser.parseWhenLocalDateTime(whenToBeCompared.getTextContent());

        if (dateTimeToBeDeleted.isEqual(LocalDateTime.MIN) && dateTimeToBeCompared.isEqual(LocalDateTime.MIN)) {
            //Both Placemarks don't have valid timeStamps. Any of them can be deleted
            return true;
        }

        return false;
    }

    /**
     * Adds possible images from the Placemark's description to the remove from the KMZ list
     *
     * @param placemarkNodeDto To extract images names from.
     * @param multipartMainDto To add images names to its {@link MultipartMainDto#getFilesToBeExcluded()} exclusion list.
     */
    private void removePlacemarkImagesFromKmz(PlacemarkNodeDto placemarkNodeDto, MultipartMainDto multipartMainDto) {
        Node descriptionNode = kmlUtils.getDescriptionNode(placemarkNodeDto.getPlacemarkNode());
        List<String> imagesNames = htmlHandler.getAllImagesFromDescription(descriptionNode.getTextContent()).stream()
                .map(src -> fileService.getFileName(src)).collect(Collectors.toList());

        //TODO: 1) to check if the icon name is the Locus photo pictogram to be deleted 2) if the icon name is used elsewhere, if not - delete
        if (locusIconsHandler.isLocusPhotoIconThumbnail(placemarkNodeDto.getIconName())) {
            //TODO: to continue 1)
        }

        multipartMainDto.getFilesToBeExcluded().addAll(imagesNames);
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
        Node coordinatesNode = kmlUtils.getCoordinatesNode(placemarkNodeDto.getPlacemarkNode());
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
        Node styleUrlNode = kmlUtils.getStyleUrlNode(placemarkNodeDto.getPlacemarkNode());
        Node styleObject = kmlUtils.getStyleObject(styleUrlNode.getTextContent());
        if (styleObject.getNodeName().equals("Style")) {
            String hrefToIcon = kmlUtils.getIconHrefNode(styleObject).getTextContent();
            placemarkNodeDto.setIconName(fileService.getFileName(hrefToIcon));
        } else if (styleObject.getNodeName().equals("StyleMap")) {
            kmlUtils.getNormalStyleNode(styleObject).ifPresent(normalStyleNode -> {
                String styleUrl = kmlUtils.getIconHrefNode(normalStyleNode).getTextContent();
                placemarkNodeDto.getImageNames().add(fileService.getFileName(styleUrl));
            });
        }
    }

    private void setImagesNames(PlacemarkNodeDto placemarkNodeDto) {
        Node descriptionNode = kmlUtils.getDescriptionNode(placemarkNodeDto.getPlacemarkNode());
        htmlHandler.getAllImagesFromDescription(descriptionNode.getTextContent()).forEach(src -> {
            placemarkNodeDto.getImageNames().add(fileService.getFileName(src));
        });
    }

    private void setPlacemarkNodeDtoList(Document kmlDocument) {
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
    }
}
