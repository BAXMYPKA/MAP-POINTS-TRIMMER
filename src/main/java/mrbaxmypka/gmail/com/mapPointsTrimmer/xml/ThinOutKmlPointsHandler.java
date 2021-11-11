package mrbaxmypka.gmail.com.mapPointsTrimmer.xml;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import mrbaxmypka.gmail.com.mapPointsTrimmer.entitiesDto.MultipartMainDto;
import mrbaxmypka.gmail.com.mapPointsTrimmer.entitiesDto.PlacemarkNodeDto;
import mrbaxmypka.gmail.com.mapPointsTrimmer.services.FileService;
import mrbaxmypka.gmail.com.mapPointsTrimmer.utils.DateTimeParser;
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
        checkThinOutType(multipartMainDto);
        removePlacemarks(kmlDocument, multipartMainDto);
    }

    private void checkThinOutType(MultipartMainDto multipartMainDto) throws IllegalArgumentException {
        if ((multipartMainDto.getThinOutType().isInclusive() || multipartMainDto.getThinOutType().isExclusive()) &&
                (multipartMainDto.getThinOutIconsNames() == null || multipartMainDto.getThinOutIconsNames().isEmpty())) {
            throw new IllegalArgumentException(
                    "When inclusive or exclusive type of thinning out you have to select one or more icons!");
        }
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
                removePlacemarkImagesFromKmz(placemarkNodeDto, multipartMainDto, clonedPlacemarksDto);
                //Remove from the main Document
                kmlDocument.removeChild(placemarkNodeDto.getPlacemarkNode());
                return true;
            } else {
                return false;
            }
        });
    }

    /**
     * Finds the first Placemark by the closest distance and determines the possibility.
     *
     * @param placemarkToBeDeleted
     * @param compareList
     * @param multipartMainDto
     * @return
     */
    private boolean canBeDeleted(
            PlacemarkNodeDto placemarkToBeDeleted, List<PlacemarkNodeDto> compareList, MultipartMainDto multipartMainDto) {

        if (!canBeDeletedByIcon(placemarkToBeDeleted, multipartMainDto)) {
            return false;
        }

        return compareList.stream().anyMatch(placemarkToBeCompared -> {
            double distance = getHaversineDistance(placemarkToBeDeleted.getLatitude(), placemarkToBeDeleted.getLongitude(),
                    placemarkToBeCompared.getLatitude(), placemarkToBeCompared.getLongitude(),
                    placemarkToBeDeleted.getAltitude(), placemarkToBeCompared.getAltitude(),
                    multipartMainDto.getDistanceUnit());
            boolean canByDeletedByDistance = distance < multipartMainDto.getThinOutDistance();
            boolean canBeDeletedByDate = canBeDeletedByDate(placemarkToBeDeleted, placemarkToBeCompared);

            if (canBeDeletedByDate && canByDeletedByDistance) {
                //Has to be deleted or inclusively either any Placemark with any icon name is allowed for deletion
                return true;
            } else {
                return false;
            }
        });
    }

    /**
     * @return True if the Placemark icon name is placed in the Inclusive list
     * either not included in the Exclusive list or Placemarks with any icon are allowed for deletion.
     * Otherwise returns false.
     */
    private boolean canBeDeletedByIcon(PlacemarkNodeDto placemarkNodeDto, MultipartMainDto multipartMainDto) {
        if (multipartMainDto.getThinOutType().isExclusive() &&
                multipartMainDto.getThinOutIconsNames().contains(placemarkNodeDto.getIconName())) {
            //The Placemark to be deleted with that icon name isn't allowed for deletion
            return false;
        } else if (multipartMainDto.getThinOutType().isInclusive() &&
                !multipartMainDto.getThinOutIconsNames().contains(placemarkNodeDto.getIconName())) {
            //This Placemark to be deleted icon is not included in the allowed for deletion list
            return false;
        } else {
            //This Placemark icon is allowed to be deleted
            return true;
        }
    }

    /**
     * Determines the oldest TimeStamp.
     *
     * @param placemarkToBeDeleted  If it is has the oldest TimeStamp it has to be deleted.
     * @param placemarkToBeCompared It it is has the oldest TimeStamp the 'placemarkToBeDeleted' has not to be deleted
     * @return If the 'placemarkToBeDeleted' is older than 'placemarkToBeCompared' or has to gx:TimeStamp at all returns true.
     * Otherwise false.
     */
    private boolean canBeDeletedByDate(PlacemarkNodeDto placemarkToBeDeleted, PlacemarkNodeDto placemarkToBeCompared) {
        Node whenToBeDeleted = kmlUtils.getWhenNode(kmlUtils.getGxTimeStampNode(placemarkToBeDeleted.getPlacemarkNode()));
        LocalDateTime dateTimeToBeDeleted = dateTimeParser.parseWhenLocalDateTime(whenToBeDeleted.getTextContent());

        Node whenToBeCompared = kmlUtils.getWhenNode(kmlUtils.getGxTimeStampNode(placemarkToBeCompared.getPlacemarkNode()));
        LocalDateTime dateTimeToBeCompared = dateTimeParser.parseWhenLocalDateTime(whenToBeCompared.getTextContent());

        if (dateTimeToBeDeleted.isEqual(LocalDateTime.MIN)) {
            //Both Placemarks don't have valid timeStamps. Any of them can be deleted
            return true;
        } else if (dateTimeToBeCompared.isEqual(LocalDateTime.MIN)) {
            //The only current Placemark to be deleted has the TimeStamp so has to be retained.
            return false;
        }

        return dateTimeToBeDeleted.isBefore(dateTimeToBeCompared);
    }

    /**
     * Adds possible images from the Placemark's description to the remove from the KMZ list
     *
     * @param placemarkNodeDto To extract images names from.
     * @param multipartMainDto To add images names to its {@link MultipartMainDto#getFilesToBeExcluded()} exclusion list.
     */
    private void removePlacemarkImagesFromKmz(PlacemarkNodeDto placemarkNodeDto,
                                              MultipartMainDto multipartMainDto,
                                              List<PlacemarkNodeDto> clonedPlacemarkNodeDtoList) {
        Node descriptionNode = kmlUtils.getDescriptionNode(placemarkNodeDto.getPlacemarkNode());
        multipartMainDto.getFilesToBeExcluded().addAll(
                htmlHandler.getAllImagesFromDescription(descriptionNode.getTextContent())
                        .stream()
                        .map(src -> fileService.getFileName(src)).collect(Collectors.toList()));

        if (locusIconsHandler.isLocusPhotoIconThumbnail(placemarkNodeDto.getIconName())) {
            //Has to be excluded from the resulting kmz as a unique Locus photo thumbnail icon
            multipartMainDto.getFilesToBeExcluded().add(placemarkNodeDto.getIconName());
        } else if (clonedPlacemarkNodeDtoList.stream().noneMatch(placemarkNodeDtoToBeCompared ->
                !placemarkNodeDto.getPlacemarkNode().isSameNode(placemarkNodeDtoToBeCompared.getPlacemarkNode()) &&
                        placemarkNodeDto.getIconName().equals(placemarkNodeDtoToBeCompared.getIconName()))) {
            //The icon of the Placemark is a unique one and not used among other Placemarks as an icon. Has to be deleted
            multipartMainDto.getFilesToBeExcluded().add(placemarkNodeDto.getIconName());
        }
    }

    /**
     * Sets {@link PlacemarkNodeDto#setAltitude(double)},
     * {@link PlacemarkNodeDto#setLongitude(double)},
     * {@link PlacemarkNodeDto#setLatitude(double)}
     * The "coordinates" Node is a required node.
     * {@literal {@literal Either <coordinates>-119.779550,33.829268,0</coordinates>
     * or <coordinates>-122.000,37.002,127.00</coordinates> or <coordinates>-122.000,37.002</coordinates>}
     * A single tuple consisting of floating point values for longitude, latitude, and altitude (in that order).
     * Longitude and latitude values are in degrees, where
     * longitude ≥ −180 and <= 180
     * latitude ≥ −90 and ≤ 90
     * altitude values (optional) are in meters above sea level.
     * If altitude isn't presented it will be set as 0.0 double value.
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
            placemarkNodeDto.setAltitude(Double.parseDouble(coordinates[2]));
        } else {
            placemarkNodeDto.setAltitude(0.0);
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
