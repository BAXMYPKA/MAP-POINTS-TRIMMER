package mrbaxmypka.gmail.com.mapPointsTrimmer.xml;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import mrbaxmypka.gmail.com.mapPointsTrimmer.entitiesDto.MultipartMainDto;
import mrbaxmypka.gmail.com.mapPointsTrimmer.services.FileService;
import mrbaxmypka.gmail.com.mapPointsTrimmer.utils.PathTypes;
import mrbaxmypka.gmail.com.mapPointsTrimmer.utils.PreviewSizeUnits;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.jsoup.select.NodeFilter;
import org.jsoup.select.NodeVisitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * HTML processing class based on {@link org.jsoup.Jsoup} library to parse only CDATA as HTML.
 */
@Slf4j
@Component
public class HtmlHandler {

    private final FileService fileService;
    @Getter(AccessLevel.PACKAGE)
    private LocalDateTime descriptionCreationTimestamp = LocalDateTime.now();

    @Autowired
    public HtmlHandler(FileService fileService) {
        this.fileService = fileService;
    }

    /**
     * @param description      Receives inner text from {@literal <description>...</description>} which in fact is the
     *                         HTML markup
     * @param multipartMainDto To determine all other conditions to be processed on CDATA HTML
     * @return Fully processed HTML markup to be included in CDATA block.
     */
    public String processDescriptionText(String description, MultipartMainDto multipartMainDto, LocalDateTime timeStampFromWhen) {
        log.debug("Description and '{}' are received", multipartMainDto);
        Element parsedHtmlFragment = Jsoup.parseBodyFragment(description).body();

        if (parsedHtmlFragment == null) {
            log.debug("HTML parsing has been impossible for this text, will be returned as it is.");
            //No html markup found, cdata is a plain text
            return description;
        } else if (parsedHtmlFragment.childNodeSize() == 1 && parsedHtmlFragment.childNode(0) instanceof TextNode) {
            log.debug("HTML parsing has returned only 0 or 1 Node parsed, so will be returned as it is.");
            //The only child is a plain text
            return description;
        }
        //A possible plain text outside html markup
        String plainTextDescription = extractPlainTextDescriptions(parsedHtmlFragment).trim();
        //MUST be the first treatment
        if (multipartMainDto.isClearOutdatedDescriptions()) { //Locus Map specific
            log.debug("Outdated descriptions will be cleared...");
            clearOutdatedDescriptions(parsedHtmlFragment, multipartMainDto, timeStampFromWhen);
        }
        if (multipartMainDto.getPath() != null) {
            log.debug("The new path for the images will be set...");
            setPath(parsedHtmlFragment, multipartMainDto.getPathType(), multipartMainDto.getPath());
        }
        if (multipartMainDto.getPreviewSize() != null) {
            log.debug("The new preview size for the images will be set...");
            setPreviewSize(parsedHtmlFragment, multipartMainDto, timeStampFromWhen);
        }
        addStartEndComments(parsedHtmlFragment);
        // MUST be the last treatment in all the conditions chain
        if (multipartMainDto.isTrimDescriptions()) {
            log.debug("The description will be cleared...");
            return trimDescriptions(parsedHtmlFragment);
        }
        parsedHtmlFragment.prependText(plainTextDescription);
        log.debug("The fully processed description is being returned...");
        return parsedHtmlFragment.html();
    }

    List<String> getAllImagesFromDescription(String description) {
        log.debug("Getting all the images from a given description text...");
        Element parsedHtmlFragment = Jsoup.parseBodyFragment(description).body();
        if (parsedHtmlFragment == null) {
            log.debug("No parsed HTML has been found, an empty Collection will be returned.");
            //No html markup found, cdata is a plain text
            return Collections.emptyList();
        }
        Elements imgElements = parsedHtmlFragment.select("img[src]");
        log.debug("A Collection of found [src] attributes will be returned...");
        return imgElements.stream().map(img -> img.attr("src")).collect(Collectors.toList());
    }

    /**
     * The given CDATA may contain a plain texts before or after HTML markup.
     * So here we extract a possible text (as {@link TextNode}) to append it back to the processed HTML
     *
     * @return Extracted plain text or empty string.
     */
    private String extractPlainTextDescriptions(Element parsedHtmlFragment) {
        log.trace("Extracting plain text as the part of the given description...");
        //A possible User Text Description as TextNode is strictly one of the children.
        return parsedHtmlFragment.childNodes().stream()
                .filter(node -> node instanceof TextNode)
                .map(node -> ((TextNode) node).getWholeText())
                .collect(Collectors.joining("\n"));
    }

    /**
     * 1) Sets new local or remote paths instead old ones.
     * I.e. old path {@code <a href="files:/_1404638472855.jpg"></>}
     * can be replaced with {@code <a href="C:/files:/a new path/_1404638472855.jpg"></>}
     * 2) Deletes images duplicates
     */
    private void setPath(Element parsedHtmlFragment, PathTypes pathType, String path) {
        log.trace("Setting the new path to images...");
        Elements aElements = parsedHtmlFragment.select("a[href]");
        Elements imgElements = parsedHtmlFragment.select("img[src]");

        deleteImagesDuplicates(parsedHtmlFragment);

        aElements.forEach((a) -> {
            String newPathWithFilename = getNewHrefWithOldFilename(a.attr("href"), pathType, path);
            a.attr("href", newPathWithFilename);
        });
        imgElements.forEach((img) -> {
            String newPathWithFilename = getNewHrefWithOldFilename(img.attr("src"), pathType, path);
            img.attr("src", newPathWithFilename);
        });
        log.trace("New path has been set.");
    }

    /**
     * Each existing a[href] contains a full path with the filename as the last text element.
     * Here we have to replace only the URL and leave the original filename.
     */
    String getNewHrefWithOldFilename(@Nullable String oldHrefWithFilename, PathTypes pathType, String newHrefWithoutFilename) {
        oldHrefWithFilename = oldHrefWithFilename == null || oldHrefWithFilename.isEmpty() ? "" : oldHrefWithFilename;
        log.trace("Old href with the filename is {}", oldHrefWithFilename);
        //User may want to erase <href>
        if (newHrefWithoutFilename == null || newHrefWithoutFilename.isBlank()) {
            log.trace("New href is null or blank");
            return "";
        }
        String newHrefWithOldFilename = "";

        if (pathType.equals(PathTypes.RELATIVE)) {
            log.trace("Getting relative type of the new href...");
            newHrefWithOldFilename = getNewRelativeHref(oldHrefWithFilename, newHrefWithoutFilename);
        } else if (pathType.equals(PathTypes.ABSOLUTE)) {
            log.trace("Getting absolute type of the new href...");
            newHrefWithOldFilename = getNewAbsoluteHref(oldHrefWithFilename, newHrefWithoutFilename);
        } else if (pathType.equals(PathTypes.WEB)) {
            log.trace("Getting web type of the new href...");
            newHrefWithOldFilename = getNewWebHref(oldHrefWithFilename, newHrefWithoutFilename);
        }
        log.debug("A new href with the old name will be returned as '{}'", newHrefWithOldFilename);
        return newHrefWithOldFilename;
    }

    private String getNewRelativeHref(String oldHrefWithFilename, String newHrefWithoutFilename) {
        newHrefWithoutFilename = trimNewHrefWithoutFilename(newHrefWithoutFilename);
        String filename = fileService.getFileName(oldHrefWithFilename);
        log.trace("The new relative href will be returned as '{}'", newHrefWithoutFilename.concat(filename));
        return newHrefWithoutFilename.concat(filename);
    }

    /**
     * {@literal Locus Map <lc:attachment></lc:attachment>} receives only {@link PathTypes#RELATIVE} or
     * {@link PathTypes#ABSOLUTE} without (!) 'file:///' prefix.
     *
     * @param imageSrc Image name with the full path to it
     * @return
     */
    boolean isLocusAbsoluteHref(String imageSrc) {
        if (imageSrc.startsWith("http") || imageSrc.startsWith("www")) {
            log.debug("Locus doesn't accept web types of href for <attachment>, blank string will be returned");
            return false;
        } else if (imageSrc.startsWith("file:///")) {
            return false;
        } else if (imageSrc.startsWith("/") || imageSrc.startsWith("...") || imageSrc.startsWith("files/")) {
            return true;
        } else {
            //The given path starts with a folder name or a filename
            return true;
        }
    }

    /**
     * {@literal Locus Map <lc:attachment></lc:attachment>} receives only {@link PathTypes#RELATIVE} or
     * {@link PathTypes#ABSOLUTE} without (!) 'file:///' prefix.
     *
     * @return 1) Locus specific absolute type of path like: '/sdcard/Locus/photos/'
     * 2) Relative type of path (starting with '../', '/' or folder name like 'Locus/photos/'
     * 3) Empty String if the given href starting with 'www.' or 'http://'
     */
    String getLocusAttachmentAbsoluteHref(String oldHrefAbsoluteTypeWithFilename, MultipartMainDto multipartMainDto) {
        if (oldHrefAbsoluteTypeWithFilename.startsWith("http") || oldHrefAbsoluteTypeWithFilename.startsWith("www")) {
            log.debug("Locus doesn't accept web types of href for <attachment>, blank string will be returned");
            return "";
        }
        String locusHref = oldHrefAbsoluteTypeWithFilename.trim().replace("file:///", "");

        if (PathTypes.ABSOLUTE.equals(multipartMainDto.getPathType()) && !locusHref.startsWith("/")) {
            locusHref = "/" + locusHref;
        }//Relative type of path can start from "/" or "..." so we ignore that type
        locusHref = locusHref.replaceAll("\\\\", "/").replaceAll("//", "/");
        log.debug("Locus attachment absolute type href will be returned as '{}'", locusHref);
        return locusHref;
    }

    private String getNewAbsoluteHref(String oldHrefWithFilename, String newHrefWithoutFilename) {
        newHrefWithoutFilename = trimNewHrefWithoutFilename(newHrefWithoutFilename);
        newHrefWithoutFilename = newHrefWithoutFilename.startsWith("/") ? newHrefWithoutFilename.substring(1) : newHrefWithoutFilename;
        String filename = fileService.getFileName(oldHrefWithFilename);
        String newAbsoluteHref = "file:///" + newHrefWithoutFilename + filename;
        log.trace("Absolute type href will be returned as '{}'", newAbsoluteHref);
        return newAbsoluteHref;
    }

    private String getNewWebHref(String oldHrefWithFilename, String newHrefWithoutFilename) {
        newHrefWithoutFilename = trimNewHrefWithoutFilename(newHrefWithoutFilename);
        String filename = fileService.getFileName(oldHrefWithFilename);
        String newWebHref = newHrefWithoutFilename + filename;
        log.trace("Web type href will be returned as '{}'", newWebHref);
        return newWebHref;
    }

    private String trimNewHrefWithoutFilename(String newHrefWithoutFilename) {
        newHrefWithoutFilename = newHrefWithoutFilename.trim();
        newHrefWithoutFilename = newHrefWithoutFilename
                .replaceAll("\\s", "%20")
                .replaceAll("\\\\", "/");
        // Each existing a[href] contains a full path with the filename as the last text element.
        // Here we have to replace only the URL and leave the original filename.
        if (!newHrefWithoutFilename.endsWith("/")) {
            // Every new href has to end with '/'
            newHrefWithoutFilename = newHrefWithoutFilename.concat("/");
        }
        log.trace("Trimmed href without filename will be returned as '{}'", newHrefWithoutFilename);
        return newHrefWithoutFilename;
    }

    /**
     * 1) Sets preview size in pixels for all <img> Elements.
     * 2) Checks if the given <img> Elements are presented as links within <a></a>. If not, wrap images with <a></a>
     * to create links.
     * 3) Creates a new description within Locus Pro <!-- desc_user:start --> ... <!-- desc_user:end -->
     * with User's text and images.
     * For Locus Pro {@code <!-- desc_user:start -->} and {@code <!-- desc_user:end -->} are the markers
     * for displaying all inner data and text on POI screen (description text, photo, photo data etc).
     * So when {@link MultipartMainDto#getPreviewSize()} != null to display it on the screen these comments
     * have to embrace all the description.
     */
    private void setPreviewSize(Element parsedHtmlFragment, MultipartMainDto multipartMainDto, LocalDateTime timeStampWhen) {

        String previewValue = multipartMainDto.getPreviewSize() + multipartMainDto.getPreviewSizeUnit().getUnit();

        Elements imgElements = parsedHtmlFragment.select("img");
//		Elements imgElements = parsedHtmlFragment.select("img[src]");
        if (imgElements.size() == 0) {
            log.debug("No 'img' Elements found within description HTML");
            return;
        }

        imgElements.forEach(img -> {
            //Standard attr (given from Locus as usual)
            if (img.hasAttr("width")) {
                log.trace("Width '{}' will be reset into existing 'width' attribute", previewValue);
                img.attr("width", previewValue);
            }
            //GoogleEarth add "max-width" attribute in style
            if (img.hasAttr("style")) {
                log.trace("Width '{}' will be reset within existing 'style' attribute", previewValue);
                setPreviewSizeInStyles(img, multipartMainDto.getPreviewSize(), multipartMainDto.getPreviewSizeUnit());
            } else {
                log.trace("New attribute 'width={}' will be set into 'img'", previewValue);
                setStyleToElement(img, "width", previewValue);
            }
        });
        //No <img> with [src] attributes
        //Remake imgs into a links if they aren't.
        log.debug("Turning <img>'s into <a><img></a> if they aren't...");
        imgElements.stream()
                .filter(Node::hasParent)
                .filter(element -> !element.parent().tagName().equalsIgnoreCase("a"))
                .forEach(element -> element.replaceWith(getAElementWithInnerImgElement(element)));
        //Finally creates a new User description within <!-- desc_user:start --> ... <!-- desc_user:end -->
        // with User's text and images inside it.
        if (!multipartMainDto.isClearOutdatedDescriptions()) { //Locus Map specific
            log.trace("Clearing outdated description as this option isn't presented in MultipartDto...");
            //All is not clear and need to be placed within UserDescStartEnd comments
            clearOutdatedDescriptions(parsedHtmlFragment, multipartMainDto, timeStampWhen);
        }
    }

    /**
     * Sets an additional "style" into that attribute for the given Element,
     * e.g. <div style="width: 100%; max-width: 120%"></div>.
     * If no "style" attribute presented it will be added.
     *
     * @param element Element
     * @param key     The "style" attribute key
     * @param value   The "style" attribute value
     */
    private void setStyleToElement(Element element, String key, String value) {
        Map<String, String> stylesKeyMap = getStylesKeyMap(element);
        log.trace("Setting additional attributes into existing 'style' as key={}, value={}", key, value);
        stylesKeyMap.put(key.trim(), value.trim());

        String newStyles = stylesKeyMap.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue() + ";")
                .collect(Collectors.joining());
        element.attr("style", newStyles);
    }

    /**
     * Sets the new preview size into inline "Style" attributes as "width", "max-width"
     *
     * @param imgElement  With obligatory "style" attribute
     * @param previewSize Ready to use value, eg. "640px".
     */
    private void setPreviewSizeInStyles(Element imgElement, int previewSize, PreviewSizeUnits sizeUnit) {
        String styleKeyValue = previewSize + sizeUnit.getUnit();
        Map<String, String> stylesKeyMap = getStylesKeyMap(imgElement);
        log.trace("Setting additional attributes into existing 'style' with previewSize={}, sizeUnit={}",
                previewSize, sizeUnit);
        if (stylesKeyMap.containsKey("max-width")) {
            stylesKeyMap.put("max-width", styleKeyValue);
        } else {
            stylesKeyMap.put("width", styleKeyValue);
        }
        String newStyles = stylesKeyMap.entrySet().stream()
                .map(entry -> entry.getKey() + ":" + entry.getValue() + ";")
                .collect(Collectors.joining());
        imgElement.attr("style", newStyles);
    }

    /**
     * @return Map of Element's "style" attribute as key-value pair. E.g. 'style="width: 200px; max-width: 250px"
     * return "style" attributes as {@link HashMap} with key="width", value="200px" etc.
     * Or empty {@link HashMap} if no "style' attribute presented.
     */
    private Map<String, String> getStylesKeyMap(Element element) {
        Map<String, String> stylesKeyMap = new LinkedHashMap<>();
        String[] keys = element.attr("style").split(":|;");
        //attr() may return "" and split() will also return same as a single array value
        if (keys.length > 1) {
            for (int i = 0; i < keys.length; i += 2) {
                stylesKeyMap.put(keys[i].trim(), keys[i + 1].trim());
            }
        }
        log.trace("Attribute 'style' keys and values will be returned as '{}'", stylesKeyMap);
        return stylesKeyMap;
    }

    /**
     * @return Trimmed String inline without redundant whitespaces and line breaks.
     */
    private String trimDescriptions(Element parsedHtmlFragment) {
        log.debug("Description is being trimmed...");
        //Deletes 2 or more whitespaces in a row
        return parsedHtmlFragment.html()
                .replaceAll("\\s{2,}", "").replaceAll("\\n", "").trim();
    }

    /**
     * Removes all the unnecessary HTML nodes and data duplicates.
     * Also extracts the earliest creation timestamp within table's tr-td with the according td's from the given Description.
     * MUST be the last method in a chain.
     */
    private void clearOutdatedDescriptions(
            Element parsedHtmlFragment, MultipartMainDto multipartMainDto, LocalDateTime timeStampFromWhen) {
        log.debug("The description is being cleared...");

        Elements imgElements = parsedHtmlFragment.select("img[src]");
        String userDescriptionText = getUserDescriptionText(parsedHtmlFragment).trim();
        Element newHtmlDescription = createNewHtmlDescription(userDescriptionText, multipartMainDto);

        if (!imgElements.isEmpty()) {

            imgElements.forEach(imgElement -> {
                if (imgElement.hasAttr("align")) imgElement.attr("align", "center");
                if (imgElement.hasAttr("style")) setStyleToElement(imgElement, "border", "1px white solid");
            });

            Element tr = new Element("tr");
            Element tdWithImg = new Element("td")
                    .attr("align", "center").attr("colspan", "2");
            tdWithImg.insertChildren(0, getAElementsWithInnerImgElement(imgElements));
            tr.appendChild(tdWithImg);
            newHtmlDescription.select("tbody").first().appendChild(tr);
        }
        Elements tableRowsWithMinDateTime = getTableRowsWithMinDateTime(parsedHtmlFragment.getAllElements(), timeStampFromWhen);
        if (!tableRowsWithMinDateTime.isEmpty()) {
            tableRowsWithMinDateTime.forEach(tr -> newHtmlDescription.select("tbody").first().appendChild(tr));
        }
        clearEmptyTables(newHtmlDescription);
        parsedHtmlFragment.html(newHtmlDescription.outerHtml());
        log.debug("The description has been cleared.");
    }

    /**
     * @param imgElement To be copied and inserted into <a></a>
     * @return New <a></a> Element with the copy of given <img></img> Element
     */
    private Element getAElementWithInnerImgElement(Element imgElement) {
        log.trace("Getting all <a> elements which contains <img> elements inside...");
        String src = imgElement.attr("src");
        Element newImgElement = new Element(imgElement.tagName());
        newImgElement.attributes().addAll(imgElement.attributes());
        return new Element("a").attr("href", src).attr("target", "_blank")
                .appendChild(newImgElement);
    }

    private Elements getAElementsWithInnerImgElement(Elements imgElements) {
        log.trace("Getting all <a> elements which contains <img> elements inside...");
        return imgElements.stream()
                .map(imgElement -> {
                    String src = imgElement.attr("src");
                    //As we will return new <a> Element we need to remove the old one from the DOM
                    if (imgElement.hasParent()) {
                        Element parent = imgElement.parent();
                        if (parent.tagName().equals("a") || parent.tagName().equals("td")) parent.remove();
                    }
                    return new Element("a").attr("href", src).attr("target", "_blank")
                            .appendChild(imgElement);
                })
                .collect(Collectors.toCollection(Elements::new));
    }

    /**
     * For Locus Pro {@code <!-- desc_user:start -->} and {@code <!-- desc_user:end -->} are the markers
     * for displaying all inner data and text on POI screen (description text, photo, photo data etc).
     * So when {@link MultipartMainDto#getPreviewSize()}  != null} to display it on the screen these comments
     * have to embrace all the description.
     * Otherwise only description text will be visible.
     *
     * @return A {@code <div></div>} Element with a new table with tbody embraced with "desc_user" comments
     * or just a new table with tbody for a data if {@link MultipartMainDto#getPreviewSize()} not set.
     */
    private Element createNewHtmlDescription(String userDescription, MultipartMainDto multipartMainDto) {
        Element table = new Element("table")
                .attr("width", "100%").attr("style", "color:black");
        table.appendChild(new Element("tbody"));
        //'setPath' option for photos and any user description texts in Locus have to be within special comments
        if (multipartMainDto.getPreviewSize() != null || !userDescription.isBlank()) {
            String descUserStart = " desc_user:start ";
            String descUserEnd = " desc_user:end ";
            Element divElement = new Element("div").appendChild(new Comment(descUserStart));
            if (!userDescription.isBlank()) divElement.appendText(userDescription);
            return divElement.appendChild(table).appendChild(new Comment(descUserEnd));
        }
        log.trace("New HTML description has been created");
        return table;
    }


    /**
     * In Locus a simple text with User's descriptions is placed between
     * {@code <!-- desc_user:start -->} and {@code <!-- desc_user:end -->} xml comments (if any).
     * In older versions it may contain HTML elements, so this method is intended to derive only text
     * among all elements between these comments.
     *
     * @return Derived text between xml tags or empty String if nothing found.
     */
    private String getUserDescriptionText(Element parsedHtmlFragment) {
        log.debug("Getting a User description plain text from the given HTML...");
        //This Comment has Parent with all the children for extracting text with User's descriptions
        final Comment[] commentNode = new Comment[1];//To be modified from within anonymous class or lambda
        parsedHtmlFragment.traverse(new NodeVisitor() {
            @Override
            public void head(Node node, int i) {
                if (node instanceof Comment && ((Comment) node).getData().contains("desc_user:start")) {
                    commentNode[0] = (Comment) node;
                }
            }

            @Override
            public void tail(Node node, int i) {
            }
        });

        if (commentNode[0] == null) return "";

        List<Node> nodesWithinUserDescComments = commentNode[0].parent().childNodes();

        StringBuilder textUserDescription = new StringBuilder();

        for (int i = 0; i < nodesWithinUserDescComments.size(); i++) {
            if (nodesWithinUserDescComments.get(i) instanceof Comment &&
                    ((Comment) nodesWithinUserDescComments.get(i)).getData().contains("desc_user:start")) {
                //From here we iterate over further Elements...
                for (int j = i; j < nodesWithinUserDescComments.size(); j++) {
                    if (nodesWithinUserDescComments.get(j) instanceof TextNode &&
                            !((TextNode) nodesWithinUserDescComments.get(j)).getWholeText().isBlank()) {
                        //Write out all non-blank text data
                        textUserDescription.append(((TextNode) nodesWithinUserDescComments.get(j)).getWholeText());
                        continue;
                    }
                    //... Until find the end marker
                    if (nodesWithinUserDescComments.get(j) instanceof Comment &&
                            ((Comment) nodesWithinUserDescComments.get(j)).getData().contains("desc_user:end")) {
                        log.debug("Description has been found within 'desc_user:start-end' Locus Map comments");
                        return textUserDescription.toString();
                    }
                }
            }
        }
        log.debug("Description hasn't been found, empty string will be returned");
        return "";
    }

    /**
     * 1) Filters td table Elements with DateTime,
     * 2) gets the earliest and retain only that one,
     * 3) Compares the previous with the given {@link LocalDateTime} from the {@literal <gx:TimeStamp><when>} Node from the parent Placemark
     * 3.1) Set the earliest DateTime into td Element
     * 4) gets td's Parent Node with all the table rows which contain the whole description of a POI.
     * 4.1) If no td's with the data were found, returns the empty Element.
     * {@literal While descriptions contain DateTimeFormat as ""yyyy-MM-dd HH:mm:ss", the Placemark's timestamp contains it as:
     * <Placemark>
     * <gx:TimeStamp>
     * <when>2014-11-21T00:27:31Z</when>
     * </gx:TimeStamp>
     * </Placemark}
     * The format "yyyy-MM-dd'T'HH:mm:ss'Z'" can only be parsed as {@link java.time.OffsetDateTime}
     * or
     * {@link java.time.Instant#parse(CharSequence)}, e.g. Instant instant = Instant.parse( "2018-01-23T01:23:45.123456789Z" )
     *
     * @return {@code new Elements("<tr>")} with the whole POI description for the earliest DateTime
     * or empty {@link Elements} collection (.size() == 0)
     */
    private Elements getTableRowsWithMinDateTime(Elements htmlElements, LocalDateTime timeStampFromWhen) {
        log.trace("Getting a table row with the minimum DataTime within the given HTML...");
        Elements tdElementsWithDescription = htmlElements.select("td");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        Element tdElementWithMinimumDateTime = tdElementsWithDescription.stream()
                .filter(Element::hasText)
                .filter(e -> {
                    //Filters only timestamps which can be parsed anyway
                    try {
                        LocalDateTime.parse(e.text(), dateTimeFormatter);
                        return true;
                    } catch (DateTimeParseException ex) {
                        try {
                            LocalDateTime.parse(e.text(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                            return true;
                        } catch (DateTimeParseException exception) {
                            return false;
                        }
                    }
                })
                .min((e1, e2) -> {
                    //Gets minimum from existing dates into the Description
                    LocalDateTime dateTime1 = LocalDateTime.now();
                    LocalDateTime dateTime2 = LocalDateTime.now();
                    try {
                        dateTime1 =
                                LocalDateTime.parse(e1.text(), dateTimeFormatter);
                        dateTime2 =
                                LocalDateTime.parse(e2.text(), dateTimeFormatter);
                    } catch (DateTimeParseException e) {
                        dateTime1 =
                                LocalDateTime.parse(e1.text(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                        dateTime2 =
                                LocalDateTime.parse(e2.text(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                    }
                    return dateTime1.compareTo(dateTime2);
                })
                .map(element -> {
                    //The minimum date from the Description is being compared with <when> timestamp
                    LocalDateTime descriptionMinDate;
                    try {
                        descriptionMinDate = LocalDateTime.parse(element.text(), dateTimeFormatter);
                    } catch (DateTimeParseException e) {
                        descriptionMinDate = LocalDateTime.parse(element.text(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                    }
                    if (descriptionMinDate.isAfter(timeStampFromWhen)) {
                        //The earliest timestamp now is from <when> tag as the minimum one
                        element.text(timeStampFromWhen.format(dateTimeFormatter));
                        descriptionCreationTimestamp = timeStampFromWhen;
                    } else {
                        //The earliest timestamp is in the Description. Stay unchanged.
                        descriptionCreationTimestamp = descriptionMinDate;
                    }
                    return element;
                })
                .orElse(new Element("empty"));
        if (tdElementWithMinimumDateTime.hasParent()) {
            log.trace("The minimum DateTime found and the table with those data will be returned");
            //<tr> is the first parent, <tbody> or <table> is the second which contains all the <tr> with descriptions
            return tdElementWithMinimumDateTime.parent().parent().children();
        } else if (!tdElementsWithDescription.isEmpty()) {
            log.trace("No DateTime data has been found, the initial <tr>s with <td>s will be returned");
            //<tr> is the first parent, <tbody> or <table> is the second which contains all the <tr> with descriptions
            return tdElementsWithDescription.first().parent().parent().children();
        } else {
            log.trace("No DateTime data and no <td> with dat have been found, a new empty HTML will be returned");
            //No <td> elements found, return empty tag
            return new Elements();
        }
    }

    /**
     * @return Just a tr with td with a hr )))
     * @deprecated
     */
    private Element getTableRowWithSeparator() {
        Element tr = new Element("tr");
        Element td = new Element("td").attr("colspan", "2");
        td.appendChild(new Element("hr"));
        tr.appendChild(td);
        log.trace("A new table row with <hr> will be returned");
        return tr; //Returns just <tr> with <td> with <hr> inside as a table rows separator
    }

    /**
     * {@literal If every <td></td> within their <tr></tr> is empty or just contain a single <hr> element, those "tr" elements will be deleted from the DOM}
     */
    private void clearEmptyTables(Element table) {
        Elements tableRows = table.select("tr");
        tableRows.forEach(tr -> {
            Elements tdElements = tr.select("td");
            if (tdElements.stream().allMatch(td -> td.children().isEmpty())) {
                //Removes <td> with no inner elements
                tr.remove();
            } else if (tdElements.stream().allMatch(td ->
                    (long) td.getAllElements().size() == 2 &&
                            ((td.getElementsByTag("hr") != null && td.hasAttr("colspan"))))) {
                //There is an old deprecated table row with just an attribute "colspan='2'" and a <hr> divider
                tr.remove();
            }
        });
        log.debug("Empty <td> and <tr> are eliminated from the given table");
    }

    /**
     * Searches for <img> tags with same filenames in their "src" attributes and deletes them from DOM.
     * If they're have parent nodes as <a href=""></a> - they will be deleted too.
     */
    private void deleteImagesDuplicates(Element parsedHtmlFragment) {
        Elements imgElements = parsedHtmlFragment.select("img[src]");

        Set<String> fileNames = new HashSet<>(3);

        imgElements.forEach(img -> {
            String fileName = fileService.getFileName(img.attr("src"));
            if (fileNames.contains(fileName)) {
                if (img.hasParent() && img.parent().tagName().equals("a")) {
                    img.parent().remove();
                } else {
                    img.remove();
                }
            }
            fileNames.add(fileName);
        });
        log.debug("Duplicated images have been eliminated from the description.");
    }

    /**
     * Safe comments that make sense for only Locus Pro. Other programs will ignore it.
     */
    private void addStartEndComments(Element parsedHtmlFragment) {
        Comment desc_gen_start = new Comment(" desc_gen:start ");
        Comment desc_gen_end = new Comment(" desc_gen:end ");

        parsedHtmlFragment.filter(new NodeFilter() {
            @Override
            public FilterResult head(Node node, int depth) {
                if (node instanceof Comment &&
                        (((Comment) node).getData().contains("desc_gen:start") ||
                                ((Comment) node).getData().contains("desc_gen:end"))) {
                    return FilterResult.REMOVE;
                }
                return FilterResult.CONTINUE;
            }

            @Override
            public FilterResult tail(Node node, int depth) {
                return FilterResult.CONTINUE;
            }
        });
        parsedHtmlFragment.prependChild(desc_gen_start);
        parsedHtmlFragment.appendChild(desc_gen_end);
        log.debug("{} and {} have been added around user description", desc_gen_start, desc_gen_end);
    }
}
