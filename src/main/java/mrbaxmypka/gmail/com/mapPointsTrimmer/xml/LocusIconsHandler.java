package mrbaxmypka.gmail.com.mapPointsTrimmer.xml;

import lombok.extern.slf4j.Slf4j;
import mrbaxmypka.gmail.com.mapPointsTrimmer.entitiesDto.MultipartDto;
import mrbaxmypka.gmail.com.mapPointsTrimmer.services.FileService;
import org.springframework.lang.NonNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Slf4j
class LocusIconsHandler {

    private FileService fileService;
    private KmlUtils kmlUtils;
    private final String PHOTO_THUMBNAIL_NAME = "/Locus/cache/images/";

    public LocusIconsHandler(FileService fileService, KmlUtils kmlUtils) {
        this.fileService = fileService;
        this.kmlUtils = kmlUtils;
    }


    /**
     * 1. {@literal Finds all the <StyleMap>s with "id" references to photo thumbnails}
     * 2. {@literal If a <StyleMap> with a previously created "id" to the current } {@link MultipartDto#getPictogramName()} exists it:
     * 2.1 {@literal Replaces all the <Placemark>s <styleUrl>s with that existing <StyleMap>}
     * 3. {@literal If no <StyleMap> for that} {@link MultipartDto#getPictogramName()} exists it:
     * 3.1 {@literal It gets a first <StyleMap> as an example, clones it and its 'normal' and 'highlight' <Style>s}
     * 3.2 {@literal Creates a new <StyleMap> and its 'normal' and 'highlight' inner <Style>s based on the clone.}
     * 3.3 {@literal Replaces all the <Placemark>s <styleUrl>s with that new cloned <StyleMap>}
     * 4. {@literal Deletes all the <StyleMap>s from the Document with "id" to photo thumbnails. As well as their included "Normal" and "Highlighted" <Style>s}
     * 5. {@literal Finds all the <Style>s with "id" references to photo thumbnails}
     * 6. {@literal If <Style> with 'id' as a such a pictogram name as:} {@link MultipartDto#getPictogramName()} exists it:
     * 6.1 {@literal Replaces all the <Placemark>s <styleUrl>s to thumbnail with that existing <Style>}
     * 7. {@literal Deletes all the <Style>s with thumbnail 'id' attributes}
     * 8. {@literal If no <Style> for that} {@link MultipartDto#getPictogramName()} exists it:
     * 8.1 {@literal It gets a first <Style> as an example and clones it}
     * 8.2 {@literal Creates a new <Style> and new href to icon based on the clone.}
     * 8.3 {@literal Replaces all the <Placemark>s <styleUrl>s with that new cloned <StyleMap>}
     * 9. {@literal Deletes all the <Style>s with thumbnail 'id' attributes}
     *
     * @param documentRoot
     * @param multipartDto
     */
    void replaceLocusIcons(@NonNull Element documentRoot, @NonNull MultipartDto multipartDto) {
        String pictogramName = multipartDto.getPictogramName();
        if (pictogramName == null || pictogramName.isBlank()) {
            log.warn("Pictogram name is null or empty!");
            return;
        }
        if (!fileService.getPictogramsNames().contains(pictogramName)) {
            log.warn("Such a pictogram={} is not presented in 'resources/static/pictograms' directory!", pictogramName);
            return;
        }
        Map<String, Node> replacedStyleMaps = replaceStyleMapsInStyleUrls(documentRoot, pictogramName);
        deleteStyleObjectsFromDocument(replacedStyleMaps);
        //Refresh the current Nodes from Document
        kmlUtils.refreshStyleObjectsMap();
        kmlUtils.refreshStyleUrlsFromPlacemarks();

        Map<String, Node> replacedStyles = replaceStylesInStyleUrls(documentRoot, pictogramName);
        deleteStyleObjectsFromDocument(replacedStyles);
        //Refresh the current Nodes from Document
        kmlUtils.refreshStyleObjectsMap();
        kmlUtils.refreshStyleUrlsFromPlacemarks();
    }

    /**
     * {@literal All the <styleUrl>s pointers in <Placemark>s with the <Style>
     * (with existing or newly created one) where the 'id' attribute is corresponding to }{@link MultipartDto#getPictogramName()}
     * {@literal  and <IconHref> is updated as well.}
     * Its better to execute this method after replacing StyleMaps in {@link #replaceStyleMapsInStyleUrls(Element, String)}.
     *
     * @param documentRoot
     * @param pictogramName
     * @return {@literal Map<String,Node(Style)> where <Style>s Nodes can by safely deleted from the }{@link Document}
     */
    private Map<String, Node> replaceStylesInStyleUrls(Element documentRoot, String pictogramName) {
        Map<String, Node> stylesWithThumbnails = kmlUtils.getStyleObjectsMap().entrySet().stream()
                .filter(entry -> entry.getValue().getNodeName().equalsIgnoreCase("Style"))
                .filter(entry -> entry.getKey().contains(PHOTO_THUMBNAIL_NAME))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        //There is no <StyleMap>s with the photo thumbnails
        if (stylesWithThumbnails.isEmpty()) return stylesWithThumbnails;

        if (!replacePlacemarksStyleUrlsWithExistingPicStyle(pictogramName, stylesWithThumbnails)) {
            replacePlacemarksStyleUrlsWithNewPicStyle(pictogramName, stylesWithThumbnails);
        }
        return stylesWithThumbnails;
    }

    /**
     * @param pictogramName        {@link MultipartDto#getPictogramName()}.
     *                             Also it is a standard styleUrl to an existing pictogram as just a full pictogram name
     * @param stylesWithThumbnails {@literal Filtered <StyleMap>s whith 'id's indicating the photo thumbnail}.
     *                             key=Style id
     *                             value=Style Node
     * @return True if the current {@link Document} contains {@literal a previously created <Style> with such a pictogram name
     * and all the <Placemark>s <styleUrl>s have been replaced with it.
     * False if no previously created <Style> exists and no replacement has been done.}
     */
    private boolean replacePlacemarksStyleUrlsWithExistingPicStyle(String pictogramName, Map<String, Node> stylesWithThumbnails) {
        AtomicBoolean isReplacedWithExisting = new AtomicBoolean(false);
        //If KML contains the previously created <Style> with this pictogram name.
        //All the <Placemark>s <styleUrl/>s with a photo thumbnails will be redirected to it
        kmlUtils.getStyleObjectsMap().values().stream()
                .filter(styleObjectNode -> styleObjectNode.getNodeName().equals("Style"))
                .filter(styleNode -> styleNode.getAttributes().getNamedItem("id").getTextContent().equals(pictogramName))
                .findFirst()
                .ifPresent(existingStyleNode -> {
                    replacePlacemarksStyleUrls(stylesWithThumbnails, pictogramName);
                    isReplacedWithExisting.set(true);
                });
        return isReplacedWithExisting.get();
    }

    /**
     * Creates a new cloned 'Style' based on any existing 'Style'. With a new 'id' as just a pictogram name attribute
     * and a new 'href'
     *
     * @param pictogramName
     * @param stylesWithThumbnails
     */
    private void replacePlacemarksStyleUrlsWithNewPicStyle(String pictogramName, Map<String, Node> stylesWithThumbnails) {
        //No previously created Style with this pictogramName. Have to create a new one
        //Based on a template settings from any existing <Style> as a clone. And replace Placemarks styleUrls
        Node clonedStyle = cloneStyleObject(stylesWithThumbnails.values());
        Node updatedStyle = updateStyle(clonedStyle, pictogramName);
        Node insertedStyle = kmlUtils.insertIntoDocument(updatedStyle);
        replacePlacemarksStyleUrls(stylesWithThumbnails, insertedStyle.getAttributes().getNamedItem("id").getTextContent());
    }

    /**
     * @param pictogramName To be included as an 'id' attribute
     * @return {@literal An updated clone of the <Style> with all the included <IconStyle>s }
     */
    private Node updateStyle(Node clonedStyle, String pictogramName) {
        clonedStyle.getAttributes().getNamedItem("id").setTextContent(pictogramName);
        Node clonedStyleIconHrefNode = kmlUtils.getIconHrefNodeFromStyle(clonedStyle);

        String originalPathWithFilename = kmlUtils.getIconHrefNodeFromStyle(clonedStyle).getTextContent();
        String originalPath = fileService.getPath(originalPathWithFilename);

        if (originalPath.isBlank()) { //No previous path found
            clonedStyleIconHrefNode.setTextContent(kmlUtils.getKML_FILES_DEFAULT_DIRECTORY() + pictogramName);
            log.warn("No path from a previous 'Icon href' found! The default path to a pictogram is applied!" +
                    "Style Node from cloned Style has been updated.");
            return clonedStyle;
        } else {
            clonedStyleIconHrefNode.setTextContent(originalPath + pictogramName);
            log.trace("Style Node from cloned Style has been updated.");
            return clonedStyle;
        }
    }

    /**
     * {@literal All the <styleUrl>s pointers in <Placemark>s with the <StyleMaps>
     * (with existing or newly created one) where the 'id' attribute is corresponding to }{@link MultipartDto#getPictogramName()}
     * {@literal  and <IconHref>s in 'normal' and 'highlight' inner <Style>s are updated as well.}
     * Its better to execute this method before replacing Styles.
     *
     * @param documentRoot
     * @param pictogramName
     * @return {@literal Map<String,Node(StyleMap)> where <StyleMap>s Nodes can by safely deleted from the }{@link Document}
     */
    private Map<String, Node> replaceStyleMapsInStyleUrls(Element documentRoot, String pictogramName) {
        Map<String, Node> styleMapsWithThumbnails = kmlUtils.getStyleObjectsMap().entrySet().stream()
                .filter(entry -> entry.getValue().getNodeName().equalsIgnoreCase("StyleMap"))
                .filter(entry -> entry.getKey().contains(PHOTO_THUMBNAIL_NAME))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        //There is no <StyleMap>s with the photo thumbnails
        if (styleMapsWithThumbnails.isEmpty()) return styleMapsWithThumbnails;

        if (!replacePlacemarksStyleUrlsWithExistingPicStyleMap(pictogramName, styleMapsWithThumbnails)) {
            replacePlacemarksStyleUrlsWithNewPicStyleMap(pictogramName, styleMapsWithThumbnails);
        }
        return styleMapsWithThumbnails;
    }

    /**
     * @param pictogramName           {@link MultipartDto#getPictogramName()}
     * @param styleMapsWithThumbnails {@literal Filtered <StyleMap>s whith 'id's indicating the photo thumbnail}
     * @return True if the current {@link Document} contains {@literal a previously created <StyleMap> with such a pictogram name
     * and all the <Placemark>s <styleUrl>s have been replaced with it.
     * False if no previously created <StyleMap> exists and no replacement has been done.}
     */
    private boolean replacePlacemarksStyleUrlsWithExistingPicStyleMap(String pictogramName, Map<String, Node> styleMapsWithThumbnails) {
        AtomicBoolean isReplacedWithExisting = new AtomicBoolean(false);
        //Standard styleUrl to an existing pictogram as "styleMapOf:"+pictogram name
        String existingStyleUrl = kmlUtils.getSTYLEMAP_ID_ATTRIBUTE_PREFIX() + pictogramName;
        //If KML contains the previously created <MapStyle> with this pictogram name.
        //All the <Placemark>s <styleUrl/>s with a photo thumbnails will be redirected to it
        kmlUtils.getStyleObjectsMap().values().stream()
                .filter(styleObjectNode -> styleObjectNode.getNodeName().equals("StyleMap"))
                .filter(styleMapNode -> styleMapNode.getAttributes().getNamedItem("id").getTextContent().equals(existingStyleUrl))
                .findFirst()
                .ifPresent(existingStyleMapNode -> {
                    replacePlacemarksStyleUrls(styleMapsWithThumbnails, existingStyleUrl);
                    isReplacedWithExisting.set(true);
                });
        return isReplacedWithExisting.get();
    }

    /**
     * {@literal Creates a new <StyleMap> with inner 'normal' and 'highlight' <Style>s as a clone of existing one.
     * Updates them ids and hrefs. Insert in into the }{@link Document} {@literal and finally replaces all the <style<Url>s
     *  in <Placemark>s with this new <StyleMap>.}
     * @param pictogramName
     * @param styleMapsWithThumbnails
     */
    private void replacePlacemarksStyleUrlsWithNewPicStyleMap(String pictogramName, Map<String, Node> styleMapsWithThumbnails) {
        //Standard styleUrl to an existing pictogram as "styleMapOf:"+pictogram name
        String existingStyleUrl = kmlUtils.getSTYLEMAP_ID_ATTRIBUTE_PREFIX() + pictogramName;
        //No previously created StyleMap with this pictogramName. Have to create a new one with the new <Style>s
        //Based on a template settings from any existing <StyleMap> as a clone. And replace Placemarks styleUrls
        Node clonedStyleMap = cloneStyleObject(styleMapsWithThumbnails.values());
        Node updatedStyleMap = updateStyleMap(clonedStyleMap, pictogramName);
        kmlUtils.insertIntoDocument(updatedStyleMap);
        replacePlacemarksStyleUrls(styleMapsWithThumbnails, existingStyleUrl);
    }

    /**
     * {@literal Replaces existing <styleUrl>s from <Placemakr>s with a given new 'newStyleUrl' within the Document.}
     *
     * @param styleObjectsWithThumbnails {@literal <StyleMap>s or <Style>s with ONLY the photo thumbnail 'id' attribute.
     *                                   All the <Placemark>s referencing to those 'id's will be collected by it.}
     * @param newStyleUrl                {@literal It will replace all <Placemark>s <styleUrl>s.}
     */
    private void replacePlacemarksStyleUrls(Map<String, Node> styleObjectsWithThumbnails, String newStyleUrl) {
        //<Placemark>s referring to <StyleMap>s or <Style>s with photo thumbnails
        List<String> thumbnailsStyleUrlsFromPlacemarks = kmlUtils.getStyleUrlsFromPlacemarks();
        thumbnailsStyleUrlsFromPlacemarks.retainAll(styleObjectsWithThumbnails.keySet());

        thumbnailsStyleUrlsFromPlacemarks.forEach(thumbnailsStyleUrls -> {
            kmlUtils.getPlacemarksByStyleUrl(thumbnailsStyleUrls)
                    .forEach(placemarkNode -> kmlUtils.setStyleUrlToPlacemark(placemarkNode, newStyleUrl));
        });
    }

    /**
     * @return Just a deeply cloned example for using {@literal an old <Style> or <StyleMap> settings for the new one}
     */
    private Node cloneStyleObject(@NonNull Collection<Node> styleObjectsWithThumbnails) {
        Node styleObjectExample = styleObjectsWithThumbnails.iterator().next();
        return styleObjectExample.cloneNode(true);
    }

    /**
     * @param pictogramName
     * @return {@literal An updated clone of the <StyleMap> with all the included <Styles> (normal and highlight) as well}
     */
    private Node updateStyleMap(Node clonedStyleMap, String pictogramName) {
        updateNormalStyleNode(clonedStyleMap, pictogramName);
        updateHighlightStyleNode(clonedStyleMap, pictogramName);
        updateClonedStyleMap(clonedStyleMap, pictogramName);
        return clonedStyleMap;
    }

    private Node updateNormalStyleNode(Node clonedStyleMapNode, String pictogramName) {
        Node clonedNormalStyleNode = kmlUtils.getNormalStyleNodeFromStyleMap(clonedStyleMapNode)
                .orElseThrow(() -> new IllegalArgumentException(
                        "A not valid xml (kml) document! There is no 'NormalStyle' inside 'StyleMap'!"));
        clonedNormalStyleNode.getAttributes().getNamedItem("id").setTextContent(pictogramName);
        Node clonedNormalStyleIconHrefNode = kmlUtils.getIconHrefNodeFromStyle(clonedNormalStyleNode);
        String originalPathWithFilename = kmlUtils.getIconHrefNodeFromStyle(clonedNormalStyleIconHrefNode).getTextContent();
        String originalPath = fileService.getPath(originalPathWithFilename);
        if (originalPath.isBlank()) { //No previous path found
            clonedNormalStyleIconHrefNode.setTextContent(kmlUtils.getKML_FILES_DEFAULT_DIRECTORY() + pictogramName);
            log.warn("No path from a previous 'Icon href' found! The default path to a pictogram is applied!" +
                    "Normal Style Node from cloned StyleMap has been updated.");
            return clonedNormalStyleNode;
        } else {
            clonedNormalStyleIconHrefNode.setTextContent(originalPath + pictogramName);
            log.trace("Normal Style Node from cloned StyleMap has been updated.");
            return clonedNormalStyleNode;
        }
    }

    private Node updateHighlightStyleNode(Node clonedStyleMapNode, String pictogramName) {
        Node clonedHighStyleNode = kmlUtils.getHighlightStyleNodeFromStyleMap(clonedStyleMapNode)
                .orElseThrow(() -> new IllegalArgumentException(
                        "A not valid xml (kml) document! There is no 'HighlightStyle' inside 'StyleMap'!"));
        clonedHighStyleNode.getAttributes().getNamedItem("id")
                .setTextContent(kmlUtils.getHIGHLIGHT_STYLE_ID_ATTRIBUTE_PREFIX() + pictogramName);
        Node highStyleIconHrefNode = kmlUtils.getIconHrefNodeFromStyle(clonedHighStyleNode);
        String originalPathWithFilename = kmlUtils.getIconHrefNodeFromStyle(highStyleIconHrefNode).getTextContent();
        String originalPath = fileService.getPath(originalPathWithFilename);
        if (originalPath.isBlank()) { //No previous path found
            highStyleIconHrefNode.setTextContent(kmlUtils.getKML_FILES_DEFAULT_DIRECTORY() + pictogramName);
            log.warn("No path from a previous 'Icon href' found! The default path to a pictogram is applied!" +
                    "Highlight Style Node from cloned StyleMap has been updated.");
            return clonedHighStyleNode;
        } else {
            highStyleIconHrefNode.setTextContent(originalPath + pictogramName);
            log.trace("Highlight Style Node from cloned StyleMap has been updated.");
            return clonedHighStyleNode;
        }
    }

    private void updateClonedStyleMap(Node clonedStyleMapNode, String pictogramName) {
        clonedStyleMapNode.getAttributes().getNamedItem("id")
                .setTextContent(kmlUtils.getSTYLEMAP_ID_ATTRIBUTE_PREFIX() + pictogramName);
        kmlUtils.getStyleUrlNodeFromNormalStylePair(clonedStyleMapNode).setTextContent("#" + pictogramName);
        kmlUtils.getStyleUrlNodeFromHighlightedStylePair(clonedStyleMapNode).setTextContent(
                "#" + kmlUtils.getHIGHLIGHT_STYLE_ID_ATTRIBUTE_PREFIX() + pictogramName);
    }

    /**
     * When all the Placemarks now referring to the existing StyleMap of Style with a previously created id to pictogramName
     * We can safely delete StyleMaps and their Styles (or just Styles) with icon thumbnails
     *
     * @param styleObjectsWithThumbnails Where {@link Node} is StyleMap or Style with 'id' attribute references to a photo thumbnail
     */
    private void deleteStyleObjectsFromDocument(Map<String, Node> styleObjectsWithThumbnails) {
        styleObjectsWithThumbnails.values().forEach(styleObject -> {
            if (styleObject.getNodeName().equalsIgnoreCase("StyleMap")) {
                kmlUtils.getNormalStyleNodeFromStyleMap(styleObject)
                        .ifPresent(normalStyle -> normalStyle.getParentNode().removeChild(normalStyle));
                kmlUtils.getHighlightStyleNodeFromStyleMap(styleObject)
                        .ifPresent(highlightStyle -> highlightStyle.getParentNode().removeChild(highlightStyle));
            }
            styleObject.getParentNode().removeChild(styleObject);
        });
    }

}
