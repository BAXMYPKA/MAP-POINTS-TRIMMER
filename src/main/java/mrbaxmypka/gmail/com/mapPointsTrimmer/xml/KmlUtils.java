package mrbaxmypka.gmail.com.mapPointsTrimmer.xml;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class KmlUtils {

    private Document document;
    private XmlDomUtils xmlDomUtils;
    /**
     * {@literal All the <Style>'s and <StyleMap>'s Nodes from the Document's by their "id" attributes
     * as the real objects which can be edited of even deleted.}
     * key=id
     * value={@link Node}
     */
    @Getter(AccessLevel.PACKAGE)
    private Map<String, Node> styleObjectsMap;
    /**
     * {@literal All the <Placemark> <styleUrl/> </Placemark>s from the given {@link Document}} WITHOUT '#' PREFIX.
     */
    @Getter(AccessLevel.PACKAGE)
    private List<String> styleUrlsFromPlacemarks;
    @Getter(AccessLevel.PACKAGE)
    private final String STYLEMAP_ID_ATTRIBUTE_PREFIX = "styleMapOf:";
    @Getter(AccessLevel.PACKAGE)
    private final String HIGHLIGHT_STYLE_ID_ATTRIBUTE_PREFIX = "highlightOf:";
    @Getter(AccessLevel.PACKAGE)
    private final String KML_FILES_DEFAULT_DIRECTORY = "files/";

    public KmlUtils(@NonNull Document document, XmlDomUtils xmlDomUtils) {
        this.document = document;
        this.xmlDomUtils = xmlDomUtils;
        refreshStyleObjectsMap();
        refreshStyleUrlsFromPlacemarks();
    }

    /**
     * Refreshing method.
     * {@code Puts aLL the <Style/>'s and <StyleMap/>'s Nodes from the Document by their "id" attribute.}
     */
    void refreshStyleObjectsMap() {
        styleObjectsMap = new HashMap<>();
        NodeList styleMapNodes = document.getElementsByTagName("StyleMap");
        NodeList styleNodes = document.getElementsByTagName("Style");
        for (int i = 0; i < styleMapNodes.getLength(); i++) {
            Node styleMapNode = styleMapNodes.item(i);
            styleObjectsMap.put(styleMapNode.getAttributes().getNamedItem("id").getTextContent(), styleMapNode);
        }
        for (int i = 0; i < styleNodes.getLength(); i++) {
            Node styleNode = styleNodes.item(i);
            if (styleNode.getAttributes() != null && styleNode.getAttributes().getNamedItem("id") != null) {
                //<Style> can be without "id" as a container for <ListStyle>
                styleObjectsMap.put(styleNode.getAttributes().getNamedItem("id").getTextContent(), styleNode);
            }
        }
        log.trace("Style objects map's set with the size={}", styleObjectsMap.size());
    }

    /**
     * Refreshing method.
     * {@code Puts aLL the <Style/>'s and <StyleMap/>'s Nodes from the Document by their "id" attribute.}
     */
    void refreshStyleUrlsFromPlacemarks() {
        styleUrlsFromPlacemarks =
                xmlDomUtils.getChildNodesFromParents(document.getElementsByTagName("Placemark"), "styleUrl", false, false
                        , false)
                        .stream()
                        .map(styleUrlNode -> styleUrlNode.getTextContent().substring(1))
                        .collect(Collectors.toList());
        log.trace("The List<String> of StyleUrls from Placemarks has been set with size={}", styleUrlsFromPlacemarks.size());
    }

    /**
     * @param styleUrl {@literal To be found in all <Placemark/>s.} Doesn't matter if it starts with '#' or not.
     * @return List of concrete {@literal <Placemark/>s} objects from the {@link Document} {@literal  with such a <styleUrl/>}
     */
    List<Node> getPlacemarksByStyleUrl(String styleUrl) {
        styleUrl = styleUrl.startsWith("#") ? styleUrl : "#" + styleUrl;
        NodeList placemarks = document.getElementsByTagName("Placemark");

        List<Node> foundPlacemarks = new ArrayList<>();

        if (placemarks.getLength() <= 0) return foundPlacemarks;

        for (int i = 0; i < placemarks.getLength(); i++) {
            Node placemarkNode = placemarks.item(i);
            //Looking through direct children
            NodeList childNodes = placemarkNode.getChildNodes();
            for (int j = 0; j < childNodes.getLength(); j++) {
                Node childNode = childNodes.item(j);
                if (childNode.getNodeName() != null && childNode.getNodeName().equals("styleUrl")) {
                    if (childNode.getTextContent() != null && childNode.getTextContent().equals(styleUrl)) {
                        foundPlacemarks.add(placemarkNode);
                        break;
                    }
                }
            }

        }
        log.trace("The NodeList of Placemarks has been set with size={}", foundPlacemarks.size());
        return foundPlacemarks;
    }

    void setStyleUrlToPlacemarks(List<Node> placemarks, String styleUrl) {
        final String correctedStyleUrl = styleUrl.startsWith("#") ? styleUrl : "#" + styleUrl;
        placemarks.forEach(placemarkNode -> {
            xmlDomUtils.getChildNodesFromParent(placemarkNode, "styleUrl", null, false, true, false)
                    .forEach(styleUrlNode -> {
                        styleUrlNode.setTextContent(styleUrl);
                    });
        });
    }

    void setStyleUrlToPlacemark(Node placemark, String styleUrl) {
        final String correctStyleUrl = styleUrl.startsWith("#") ? styleUrl : "#" + styleUrl;
        xmlDomUtils.getChildNodesFromParent(placemark, "styleUrl", null, false, true, false)
                .forEach(styleUrlNode -> {
                    styleUrlNode.setTextContent(correctStyleUrl);
                });
    }

    /**
     * {@code
     * <StyleMap id="styleMap1">
     * <Pair>
     * <key>normal</key>
     * <styleUrl>#style1</styleUrl>
     * </Pair>
     * <Pair>
     * <key>highlight</key>
     * <styleUrl>#style-1-cloned</styleUrl>
     * </Pair>
     * </StyleMap>
     * <Style id="style1">
     * <IconStyle>
     * <scale>0.8</scale>
     * <Icon>
     * <href>http://maps.google.com/mapfiles/kml/shapes/poi.png</href>
     * </Icon>
     * <hotSpot x="0.5" y="0" xunits="fraction" yunits="fraction"/>
     * </IconStyle>
     * <LabelStyle>
     * <scale>0.7</scale>
     * </LabelStyle>
     * </Style>
     * <Style id="style-1-cloned">
     * <IconStyle>
     * <scale>0.8</scale>
     * <Icon>
     * <href>http://maps.google.com/mapfiles/kml/shapes/poi.png</href>
     * </Icon>
     * <hotSpot x="0.5" y="0" xunits="fraction" yunits="fraction"/>
     * </IconStyle>
     * <LabelStyle>
     * <scale>0.7</scale>
     * </LabelStyle>
     * </Style>
     * }
     *
     * @return A {@link Node} inserted into the {@link Document}.
     */
    Node createInsertedStyleMapNode(Node styleNode) {
        String idAttribute = STYLEMAP_ID_ATTRIBUTE_PREFIX + styleNode.getAttributes().getNamedItem("id").getTextContent();

        if (getStyleObjectsMap().containsKey(idAttribute)) {
            //Not to create duplicates
            return getStyleObjectsMap().get(idAttribute);
        }
        Element styleMapNode = document.createElement("StyleMap");
        styleMapNode.setAttribute("id", idAttribute);

        Element pairNormalNode = document.createElement("Pair");
        Element keyNormalNode = document.createElement("key");
        keyNormalNode.setTextContent("normal");
        Element styleUrlNormal = document.createElement("styleUrl");
        styleUrlNormal.setTextContent("#" + styleNode.getAttributes().getNamedItem("id").getTextContent());
        pairNormalNode.appendChild(keyNormalNode);
        pairNormalNode.appendChild(styleUrlNormal);
        styleMapNode.appendChild(pairNormalNode);

        Node highlightStyleNode = createHighlightStyleNode(styleNode);

        Element pairHighlightNode = document.createElement("Pair");
        Element keyHighlightNode = document.createElement("key");
        keyHighlightNode.setTextContent("highlight");
        Element styleUrlHighlightNode = document.createElement("styleUrl");
        styleUrlHighlightNode.setTextContent("#" + highlightStyleNode.getAttributes().getNamedItem("id").getTextContent());
        pairHighlightNode.appendChild(keyHighlightNode);
        pairHighlightNode.appendChild(styleUrlHighlightNode);
        styleMapNode.appendChild(pairHighlightNode);

        insertIntoDocument(highlightStyleNode);
        insertIntoDocument(styleMapNode);

        return styleMapNode;
    }

    private Node createHighlightStyleNode(Node styleNode) {
        Element styleHighlightNode = (Element) styleNode.cloneNode(true);
        styleHighlightNode.setAttribute(
                "id", HIGHLIGHT_STYLE_ID_ATTRIBUTE_PREFIX + styleNode.getAttributes().getNamedItem("id").getTextContent());
        return styleHighlightNode;
    }

    /**
     * {@literal
     * A Kml Document must contain a single header as a <Document/> Node.
     * The new created <StyleMap/>'s have to be inserted either before elder ones or the a first child of the
     * <Document/>
     * }
     *
     * @return A given "styleMapNode" parameter as the inserted into Document one.
     */
    Node insertIntoDocument(Node styleMapNode) {
        Node documentNode = document.getElementsByTagName("Document").item(0);
        Node insertBeforeNode = null;
        //<Document/>
        NodeList childNodes = documentNode.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);
            if (childNode.getNodeName() != null &&
                    (childNode.getNodeName().equals("Style") || childNode.getNodeName().equals("StyleMap"))) {
                insertBeforeNode = childNode;
                break;
            }
        }
        insertBeforeNode = insertBeforeNode != null ? insertBeforeNode : childNodes.item(0).getFirstChild();
        documentNode.insertBefore(styleMapNode, insertBeforeNode);
        return styleMapNode;
    }

    /**
     * @param styleUrl {@literal <StyleMap>s or <Placemark>s <styleUrl> to the <Style> Node} with or without the first '#' sign.
     * @return {@link Node} {@literal <Style> or <StyleMap>} as StyleObject from {@link KmlUtils#getStyleObjectsMap()}.
     * If "styleUrl" is null returns a new {@link Node} with "Default" tagName.
     */
    Node getStyleObject(String styleUrl) {
        if (styleUrl.startsWith("#")) {
            styleUrl = styleUrl.substring(1);
        }
        Node styleNode = getStyleObjectsMap().getOrDefault(styleUrl, document.createElement("Default"));
        if (styleNode.getNodeName().contentEquals("Default")) {
            log.warn("The Document is incorrect because <styleUrl>#{}</styleUrl> points to non-existent <Style> with no such id", styleUrl);
        }
        return styleNode;
    }

    /**
     * {@code
     * <StyleMap id="styleMap1">
     * <Pair>
     * <key>normal</key>
     * ==>> <styleUrl>#style1</styleUrl> <<==
     * </Pair>
     * <Pair>
     * <key>highlight</key>
     * <styleUrl>#style2</styleUrl>
     * </Pair>
     * </StyleMap>
     * ===>>> <Style id="style1"> <<<===
     * <IconStyle>
     * <scale>0.8</scale>
     * <Icon>
     * <href>http://maps.google.com/mapfiles/kml/shapes/poi.png</href>
     * </Icon>
     * <hotSpot x="0.5" y="0" xunits="fraction" yunits="fraction"/>
     * </IconStyle>
     * <LabelStyle>
     * <scale>0.7</scale>
     * </LabelStyle>
     * </Style>
     * <Style id="style2">
     * <IconStyle>
     * <scale>0.8</scale>
     * <Icon>
     * <href>http://maps.google.com/mapfiles/kml/shapes/poi.png</href>
     * </Icon>
     * <hotSpot x="0.5" y="0" xunits="fraction" yunits="fraction"/>
     * </IconStyle>
     * <LabelStyle>
     * <scale>0.7</scale>
     * </LabelStyle>
     * </Style>
     * }
     *
     * @return {@literal <Style id="id"/> or Optional.empty() if the "normal" <Style/> from <Pair/> isn't presented or
     * has no "id" attribute and cannot be directly used for icons as in the following e.g.:
     * <Style>
     * <ListStyle>
     * <listItemType>check</listItemType>
     * <bgColor>00ffffff</bgColor>
     * <maxSnippetLines>2</maxSnippetLines>
     * </ListStyle>
     * </Style>
     * }
     */
    Optional<Node> getNormalStyleNode(Node styleMap) {
        return xmlDomUtils.getChildNodesFromParent(styleMap, "Pair", null, false, false, false)
                .stream()
                .filter(pairNode -> !xmlDomUtils.getChildNodesFromParent(pairNode, "key", "normal", false, false, false).isEmpty())
                .findFirst()
                .map(normalPairNode -> xmlDomUtils.getChildNodesFromParent(normalPairNode, "styleUrl", null, false, false, false).get(0))
                .map(normalStyleUrl -> getStyleObject(normalStyleUrl.getTextContent().substring(1)));
    }

    /**
     * {@code
     * <StyleMap id="styleMap1">
     * <Pair>
     * <key>normal</key>
     * ==>> <styleUrl>#style1</styleUrl> <<==
     * </Pair>
     * <Pair>
     * <key>highlight</key>
     * <styleUrl>#style2</styleUrl>
     * </Pair>
     * </StyleMap>
     * ===>>> <Style id="style1"> <<<===
     * <IconStyle>
     * <scale>0.8</scale>
     * <Icon>
     * <href>http://maps.google.com/mapfiles/kml/shapes/poi.png</href>
     * </Icon>
     * <hotSpot x="0.5" y="0" xunits="fraction" yunits="fraction"/>
     * </IconStyle>
     * <LabelStyle>
     * <scale>0.7</scale>
     * </LabelStyle>
     * </Style>
     * <Style id="style2">
     * <IconStyle>
     * <scale>0.8</scale>
     * <Icon>
     * <href>http://maps.google.com/mapfiles/kml/shapes/poi.png</href>
     * </Icon>
     * <hotSpot x="0.5" y="0" xunits="fraction" yunits="fraction"/>
     * </IconStyle>
     * <LabelStyle>
     * <scale>0.7</scale>
     * </LabelStyle>
     * </Style>
     * }
     *
     * @return {@literal <Style id="id"/> or Optional.empty() if the "normal" <Style/> from <Pair/> isn't presented or
     * has no "id" attribute and cannot be directly used for icons as in the following e.g.:
     * <Style>
     * <ListStyle>
     * <listItemType>check</listItemType>
     * <bgColor>00ffffff</bgColor>
     * <maxSnippetLines>2</maxSnippetLines>
     * </ListStyle>
     * </Style>
     * }
     */
    Optional<Node> getHighlightStyleNode(Node styleMap) {
        return xmlDomUtils.getChildNodesFromParent(styleMap, "Pair", null, false, false, false)
                .stream()
                .filter(pairNode -> !xmlDomUtils.getChildNodesFromParent(pairNode, "key", "highlight", false, false, false).isEmpty())
                .findFirst()
                .map(highlightPairNode -> xmlDomUtils.getChildNodesFromParent(highlightPairNode, "styleUrl", null, false, false, false).get(0))
                .map(highlightStyleUrl -> getStyleObject(highlightStyleUrl.getTextContent().substring(1)));
    }

    /**
     * {@literal
     * <Style id="generic_n40">
     * ===>> <IconStyle> <<===
     * <scale>0.8</scale>
     * <Icon>
     * <href>http://maps.google.com/mapfiles/kml/shapes/poi.png</href>
     * </Icon>
     * <hotSpot x="0.5" y="0" xunits="fraction" yunits="fraction"/>
     * </IconStyle>
     * <LabelStyle>
     * <scale>0.7</scale>
     * </LabelStyle>
     * </Style>}
     *
     * @return Returns the existing 'IconStyle' node or created and inserted new one
     */
    Node getIconStyleNode(Node styleNode) {
        return xmlDomUtils.getChildNodesFromParent(styleNode, "IconStyle", null, false, true, false).get(0);
    }

    /**
     * {@literal
     * <Style id="generic_n40">
     * <IconStyle>
     * <scale>0.8</scale>
     * ===> <Icon> <===
     * <href>http://maps.google.com/mapfiles/kml/shapes/poi.png</href>
     * </Icon>
     * <hotSpot x="0.5" y="0" xunits="fraction" yunits="fraction"/>
     * </IconStyle>
     * <LabelStyle>
     * <scale>0.7</scale>
     * </LabelStyle>
     * </Style>}
     *
     * @return Returns the existing 'Icon' node or a new one created with its parent ('IconStyle')
     */
    Node getIconNode(Node styleNode) {
        Node iconStyleNode = getIconStyleNode(styleNode);
        return xmlDomUtils.getChildNodesFromParent(iconStyleNode, "Icon", null, false, true, false).get(0);
    }

    /**
     * {@literal
     * <Style id="generic_n40">
     * <IconStyle>
     * <scale>0.8</scale>
     * <Icon>
     * ===>> <href>http://maps.google.com/mapfiles/kml/shapes/poi.png</href> <<===
     * </Icon>
     * <hotSpot x="0.5" y="0" xunits="fraction" yunits="fraction"/>
     * </IconStyle>
     * <LabelStyle>
     * <scale>0.7</scale>
     * </LabelStyle>
     * </Style>}
     *
     * @return Returns the existing 'href' node or a new one created with all its parents ('IconStyle' -> 'Icon' -> 'href')
     */
    Node getIconHrefNode(Node styleNode) {
        Node iconNode = getIconNode(styleNode);
        return xmlDomUtils.getChildNodesFromParent(iconNode, "href", null, false, true, false).get(0);
    }

    /**
     * {@literal
     * <StyleMap id="styleMapOf:file:///1602925147891">
     * <Pair>
     * <key>normal</key>
     * ===>> <styleUrl>#file:///1602925147891</styleUrl> <<===
     * </Pair>
     * <Pair>
     * <key>highlight</key>
     * <styleUrl>#highlightOf:file:///1602925147891</styleUrl>
     * </Pair>
     * </StyleMap>
     * }
     *
     * @param styleMap {@link Node} {@literal <StyleMal/>}
     * @return {@link Node} {@literal <Pair> <key>normal</key> <styleUrl/> Node}
     * @throws IllegalArgumentException If no StyleUrl found (the Document is incorrect).
     */
    Node getStyleUrlNodeFromNormalStylePair(Node styleMap) {
        List<Node> pairs = xmlDomUtils.getChildNodesFromParent(styleMap, "Pair", null, false, false, false);
        Node normalStyleUrl = null;
        start:
        for (Node pairNode : pairs) {
            NodeList pairChildNodes = pairNode.getChildNodes();
            for (int j = 0; j < pairChildNodes.getLength(); j++) {
                Node pairChildNode = pairChildNodes.item(j);
                if (pairChildNode.getNodeName() != null
                        && pairChildNode.getNodeName().equals("key")
                        && pairChildNode.getTextContent().equals("normal")) {

                    NodeList normalPairNodeChildren = pairNode.getChildNodes();
                    for (int i = 0; i < normalPairNodeChildren.getLength(); i++) {
                        Node normalPairNodeChild = normalPairNodeChildren.item(i);
                        if (normalPairNodeChild.getNodeName() != null &&
                                normalPairNodeChild.getNodeName().equals("styleUrl")) {
                            normalStyleUrl = normalPairNodeChild;
                            break start;
                        }
                    }
                }
            }
        }
        if (normalStyleUrl == null) {
            throw new IllegalArgumentException("The xml (.kml) Document contains incorrect structure! It's StyleMap with id="
                    + styleMap.getAttributes().getNamedItem("id").getTextContent() + " doesn't contain a normal Style url!");
        }
        return normalStyleUrl;
    }

    /**
     * {@literal
     * <StyleMap id="styleMapOf:file:///1602925147891">
     * <Pair>
     * <key>normal</key>
     * <styleUrl>#file:///1602925147891</styleUrl>
     * </Pair>
     * <Pair>
     * <key>highlight</key>
     * ===>> <styleUrl>#highlightOf:file:///1602925147891</styleUrl> <<===
     * </Pair>
     * </StyleMap>
     * }
     *
     * @param styleMap {@link Node} {@literal <StyleMal/>}
     * @return {@link Node} {@literal <Pair> <key>highlight</key> <styleUrl/> Node}
     * @throws IllegalArgumentException If no StyleUrl found (the Document is incorrect).
     */
    Node getStyleUrlNodeFromHighlightedStylePair(Node styleMap) {
        List<Node> pairs = xmlDomUtils.getChildNodesFromParent(styleMap, "Pair", null, false, false, false);
        Node highlightStyleUrl = null;
        start:
        for (Node pairNode : pairs) {
            NodeList pairChildNodes = pairNode.getChildNodes();
            for (int j = 0; j < pairChildNodes.getLength(); j++) {
                Node pairChildNode = pairChildNodes.item(j);
                if (pairChildNode.getNodeName() != null
                        && pairChildNode.getNodeName().equals("key")
                        && pairChildNode.getTextContent().equals("highlight")) {

                    NodeList highlightPairNodeChildren = pairNode.getChildNodes();
                    for (int i = 0; i < highlightPairNodeChildren.getLength(); i++) {
                        Node highlightPairNodeChild = highlightPairNodeChildren.item(i);
                        if (highlightPairNodeChild.getNodeName() != null &&
                                highlightPairNodeChild.getNodeName().equals("styleUrl")) {
                            highlightStyleUrl = highlightPairNodeChild;
                            break start;
                        }
                    }
                }
            }
        }
        if (highlightStyleUrl == null) {
            throw new IllegalArgumentException("The xml (.kml) Document contains incorrect structure! It's StyleMap with id="
                    + styleMap.getAttributes().getNamedItem("id").getTextContent() + " doesn't contain the highlight Style url!");
        }
        return highlightStyleUrl;
    }

    Node getCoordinatesNode(Node placemark) {
        Node point = xmlDomUtils.getChildNodesFromParent(placemark, "Point", null, false, true, false).get(0);
        return xmlDomUtils.getChildNodesFromParent(point, "coordinates", null, false, true, false).get(0);
    }

    Node getDescriptionNode(Node point) {
        return xmlDomUtils.getChildNodesFromParent(point, "description", null, false, true, false).get(0);
    }

    Node getPointNode(Node placemark) {
        return xmlDomUtils.getChildNodesFromParent(placemark, "description", null, false, true, false).get(0);
    }

    Node getStyleUrlNode(Node placemark) {
        return xmlDomUtils.getChildNodesFromParent(placemark, "styleUrl", null, false, true, false).get(0);
    }

    Node getGxTimeStampNode(Node point) {
        return xmlDomUtils.getChildNodesFromParent(point, "gx:TimeStamp", null, false, true, false).get(0);
    }

    Node getWhenNode(Node gxTimeStamp) {
        return xmlDomUtils.getChildNodesFromParent(gxTimeStamp, "when", null, false, true, false).get(0);
    }

    /**
     * @param placemark
     * @return A {@link List<Node>} of "lc:attachment" from "ExtendedData" or an {@link Collections#EMPTY_LIST} if nothing found.
     */
    List<Node> getLocusAttachmentsNodes(Node placemark) {
        List<Node> extendedDatas = xmlDomUtils.getChildNodesFromParent(placemark, "ExtendedData", null, false, false, false);
        if (!extendedDatas.isEmpty()) {
            return xmlDomUtils.getChildNodesFromParent(extendedDatas.get(0), "lc:attachment", null, false, false, false);
        } else {
            return extendedDatas;
        }
    }

}
