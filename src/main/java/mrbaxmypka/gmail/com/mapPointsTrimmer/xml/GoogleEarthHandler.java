package mrbaxmypka.gmail.com.mapPointsTrimmer.xml;

import lombok.extern.slf4j.Slf4j;
import mrbaxmypka.gmail.com.mapPointsTrimmer.entitiesDto.MultipartMainDto;
import org.springframework.lang.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Slf4j
public class GoogleEarthHandler {

    private Document document;
    private XmlDomUtils xmlDomUtils;
    private KmlUtils kmlUtils;

    public GoogleEarthHandler(KmlUtils kmlUtils) {
        this.kmlUtils = kmlUtils;
    }

    Document processKml(Document document, MultipartMainDto multipartMainDto) {
        this.document = document;
        xmlDomUtils = new XmlDomUtils(document);
        log.info("Document and {} are received", multipartMainDto);

        if (multipartMainDto.getPointIconSize() != null) {
            setPointsIconsSize(multipartMainDto);
        }
        if (multipartMainDto.getPointIconOpacity() != null) {
            setPointsIconsOpacity(multipartMainDto);
        }
        if (multipartMainDto.getPointTextSize() != null) {
            setPointsTextSize(multipartMainDto);
        }
        if (multipartMainDto.getPointTextHexColor() != null) {
            setPointsTextColor(multipartMainDto);
        }
        //If something dynamic is set we create and assign <StyleMap>'s if absent
        if (multipartMainDto.getPointIconSizeDynamic() != null ||
                multipartMainDto.getPointTextSizeDynamic() != null ||
                multipartMainDto.getPointTextHexColorDynamic() != null) {
            createStyleMaps();
        }
        if (multipartMainDto.getPointIconSizeDynamic() != null) {
            setPointsIconsSizeDynamic(multipartMainDto);
        }
        if (multipartMainDto.getPointIconOpacityDynamic() != null) {
            setPointsIconsOpacityDynamic(multipartMainDto);
        }
        if (multipartMainDto.getPointTextSizeDynamic() != null) {
            setPointsTextSizeDynamic(multipartMainDto);
        }
        if (multipartMainDto.getPointTextHexColorDynamic() != null) {
            setPointsTextColorDynamic(multipartMainDto);
        }
        return this.document;
    }

    private void setPointsIconsSize(MultipartMainDto multipartMainDto) {
        log.info("Setting the icons size...");

        String scale = multipartMainDto.getPointIconSizeScaled().toString();

        kmlUtils.getStyleUrlsFromPlacemarks().forEach(styleUrl -> {
            Node styleObject = kmlUtils.getStyleObject(styleUrl);
            if (styleObject.getNodeName().equals("Style")) {
                Node iconStyleScaleNode = getIconStyleScaleNodeFromStyle(styleObject);
                iconStyleScaleNode.setTextContent(scale);
            } else if (styleObject.getNodeName().equals("StyleMap")) {
                kmlUtils.getNormalStyleNode(styleObject).ifPresent(normalStyleNode -> {
                    Node iconStyleScaleNode = getIconStyleScaleNodeFromStyle(normalStyleNode);
                    iconStyleScaleNode.setTextContent(scale);
                });
            }
        });
        log.info("Icons size has been set.");
    }

    /**
     * HTML hexadecimal color with max value "#FFFFFF" will be prefixed with 'kml color' hexadecimal value
     * from 00 to FF as "00FFFFFF" or "FFFFFFFF" etc.
     */
    private void setPointsIconsOpacity(MultipartMainDto multipartMainDto) {
        log.info("Setting the icons opacity...");

        String opacityColor = getKmlColor("#ffffff", multipartMainDto.getPointIconHexOpacity());

        kmlUtils.getStyleUrlsFromPlacemarks().forEach(styleUrl -> {
            Node styleObject = kmlUtils.getStyleObject(styleUrl);
            if (styleObject.getNodeName().equals("Style")) {
                getIconsStyleColorNodeFromStyle(styleObject);
                Node iconStyleColorNode = getIconsStyleColorNodeFromStyle(styleObject);
                iconStyleColorNode.setTextContent(opacityColor);
            } else if (styleObject.getNodeName().equals("StyleMap")) {
                kmlUtils.getNormalStyleNode(styleObject).ifPresent(normalStyleNode -> {
                    Node iconStyleColorNode = getIconsStyleColorNodeFromStyle(normalStyleNode);
                    iconStyleColorNode.setTextContent(opacityColor);
                });
            }
        });
        log.info("Icons opacity has been set.");
    }

    /**
     * Either applies to existing <LabelStyle> tags or a new ones will be created for every <Style>.
     * Because <LabelStyle> automatically displays <name> content from <Placemark>s.
     */
    private void setPointsTextSize(MultipartMainDto multipartMainDto) {
        log.info("Setting the points names size...");
        String scale = multipartMainDto.getPointTextSizeScaled().toString();

        kmlUtils.getStyleUrlsFromPlacemarks().forEach(styleUrl -> {
            Node styleObject = kmlUtils.getStyleObject(styleUrl);
            if (styleObject.getNodeName().equals("Style")) {
                Node labelStyleScaleNode = getLabelStyleScaleNodeFromStyle(styleObject);
                labelStyleScaleNode.setTextContent(scale);
            } else if (styleObject.getNodeName().equals("StyleMap")) {
                kmlUtils.getNormalStyleNode(styleObject).ifPresent(normalStyleNode -> {
                    Node labelStyleScaleNode = getLabelStyleScaleNodeFromStyle(normalStyleNode);
                    labelStyleScaleNode.setTextContent(scale);
                });
            }
        });
        log.info("Points names size has been set.");
    }

    private void setPointsTextColor(MultipartMainDto multipartMainDto) {
        log.info("Setting the names text color...");
        String kmlColor = getKmlColor(multipartMainDto.getPointTextHexColor(), multipartMainDto.getPointTextOpacity());

        kmlUtils.getStyleUrlsFromPlacemarks().forEach(styleUrl -> {
            Node styleObject = kmlUtils.getStyleObject(styleUrl);
            if (styleObject.getNodeName().equals("Style")) {
                Node labelStyleColorNode = getLabelStyleColorNodeFromStyle(styleObject);
                labelStyleColorNode.setTextContent(kmlColor);
            } else if (styleObject.getNodeName().equals("StyleMap")) {
                kmlUtils.getNormalStyleNode(styleObject).ifPresent(normalStyleNode -> {
                    Node labelStyleColorNode = getLabelStyleColorNodeFromStyle(normalStyleNode);
                    labelStyleColorNode.setTextContent(kmlColor);
                });
            }
        });
        log.info("Names color has been set.");
    }

    /**
     * {@literal
     * <Style id="generic_n40">
     * <IconStyle>
     * ===>>> <scale>0.8</scale> <<<===
     * <Icon>
     * <href>http://maps.google.com/mapfiles/kml/shapes/poi.png</href>
     * </Icon>
     * <hotSpot x="0.5" y="0" xunits="fraction" yunits="fraction"/>
     * </IconStyle>
     * <LabelStyle>
     * <scale>0.7</scale>
     * </LabelStyle>
     * </Style>}
     */
    private Node getIconStyleScaleNodeFromStyle(Node styleNode) {
        Node iconStyleNode = xmlDomUtils.getChildNodesFromParent(styleNode, "IconStyle", null, false, true, false).get(0);
        return xmlDomUtils.getChildNodesFromParent(iconStyleNode, "scale", null, false, true, false).get(0);
    }

    /**
     * {@literal
     * <Style id="generic_n40">
     * <IconStyle>
     * ===>>> <scale>0.8</scale> <<<===
     * <Icon>
     * <href>http://maps.google.com/mapfiles/kml/shapes/poi.png</href>
     * </Icon>
     * <hotSpot x="0.5" y="0" xunits="fraction" yunits="fraction"/>
     * </IconStyle>
     * <LabelStyle>
     * <scale>0.7</scale>
     * </LabelStyle>
     * </Style>}
     */
    private Node getIconsStyleColorNodeFromStyle(Node styleNode) {
        Node iconStyleNode = xmlDomUtils.getChildNodesFromParent(styleNode, "IconStyle", null, false, true, false).get(0);
        return xmlDomUtils.getChildNodesFromParent(iconStyleNode, "color", null, false, true, false).get(0);
    }

    /**
     * {@literal
     * <Style id="generic_n40">
     * <IconStyle>
     * <scale>0.8</scale>
     * <Icon>
     * <href>http://maps.google.com/mapfiles/kml/shapes/poi.png</href>
     * </Icon>
     * <hotSpot x="0.5" y="0" xunits="fraction" yunits="fraction"/>
     * </IconStyle>
     * <LabelStyle>
     * ===>>> <scale>0.7</scale> <<<===
     * </LabelStyle>
     * </Style>}
     */
    private Node getLabelStyleScaleNodeFromStyle(Node styleNode) {
        Node labelStyleNode = xmlDomUtils.getChildNodesFromParent(styleNode, "LabelStyle", null, false, true, false).get(0);
        return xmlDomUtils.getChildNodesFromParent(labelStyleNode, "scale", null, false, true, false).get(0);
    }

    /**
     * {@literal
     * <Style id="generic_n40">
     * <IconStyle>
     * <scale>0.8</scale>
     * <Icon>
     * <href>http://maps.google.com/mapfiles/kml/shapes/poi.png</href>
     * </Icon>
     * <hotSpot x="0.5" y="0" xunits="fraction" yunits="fraction"/>
     * </IconStyle>
     * <LabelStyle>
     * <scale>0.7</scale>
     * ===>>> <color>00000000</color> <<<===
     * </LabelStyle>
     * </Style>}
     */
    private Node getLabelStyleColorNodeFromStyle(Node styleNode) {
        Node labelStyleNode = xmlDomUtils.getChildNodesFromParent(styleNode, "LabelStyle", null, false, true, false).get(0);
        return xmlDomUtils.getChildNodesFromParent(labelStyleNode, "color", null, false, true, false).get(0);
    }


    /**
     * {@code All the <Placemark><styleUrl/></Placemark> have to reference to <StyleMap/>'s instead of <Style/>'s}
     */
    private void createStyleMaps() {
        List<Node> placemarksStyleUrlNodes =
                xmlDomUtils.getChildNodesFromParents(document.getElementsByTagName("Placemark"), "styleUrl", false, false, false);
        placemarksStyleUrlNodes.stream()
                .filter(styleUrlNode -> kmlUtils.getStyleObject(styleUrlNode.getTextContent().substring(1)) != null)
                .forEach(styleUrlNode -> {
                    Node styleObjectNode = kmlUtils.getStyleObject(styleUrlNode.getTextContent().substring(1));
                    if (styleObjectNode.getNodeName().equals("Style")) {
                        //Replace styleUrl to <Style/> with <StyleMap/>
                        Node styleMapNode = kmlUtils.createInsertedStyleMapNode(styleObjectNode);
                        styleUrlNode.setTextContent("#" + styleMapNode.getAttributes().getNamedItem("id").getTextContent());
                        kmlUtils.getStyleObjectsMap().put(styleMapNode.getAttributes().getNamedItem("id").getTextContent(), styleMapNode);
                    }
                });
        //Refresh existing collections
        kmlUtils.refreshStyleObjectsMap();
        kmlUtils.refreshStyleUrlsFromPlacemarks();
        log.info("<StyleMap>'s have been created for all the <Placemark><styleUrl/></Placemark>. ({} StyleObjects)",
                kmlUtils.getStyleObjectsMap().size());
    }

    private Node createHighlightStyleNode(Node styleNode) {
        Element styleHighlightNode = (Element) styleNode.cloneNode(true);
        styleHighlightNode.setAttribute("id", kmlUtils.getHIGHLIGHT_STYLE_ID_ATTRIBUTE_PREFIX() + styleNode.getAttributes().getNamedItem("id").getTextContent());
        return styleHighlightNode;
    }

    private void setPointsIconsSizeDynamic(MultipartMainDto multipartMainDto) {
        log.info("Setting the points icons dynamic size...");
        NodeList styleMapNodes = document.getElementsByTagName("StyleMap");
        for (int i = 0; i < styleMapNodes.getLength(); i++) {
            Node styleMapNode = styleMapNodes.item(i);
            kmlUtils.getHighlightStyleNode(styleMapNode).ifPresent(highlightStyleNode -> {
                Node iconStyleNode = xmlDomUtils.getChildNodesFromParent(highlightStyleNode, "IconStyle", null, false, true, true).get(0);
                Node scaleNode = xmlDomUtils.getChildNodesFromParent(iconStyleNode, "scale", null, false, true, true).get(0);
                scaleNode.setTextContent(multipartMainDto.getPointIconSizeScaledDynamic().toString());
            });
        }
        log.info("All the points icons dynamic size has been set.");
    }

    /**
     * Hexadecimal value from 00 to FF as "00FFFFFF" or "FFFFFFFF" etc.
     * <p>This color tag is applied as an overlay to PNG icons, the #FFFFFF is the white color and won't affect the
     * icons color, but the first hex "alpha channel" value will. (E.g. 00FFFFFF will make the icons invisible.)</p>
     * <p>********* FROM THE KML DOCUMENTATION ************************
     * Color and opacity (alpha) values are expressed in hexadecimal notation.
     * The range of values for any one color is 0 to 255 (00 to ff). For alpha, 00 is fully transparent and ff is fully opaque.
     * The order of expression is aabbggrr, where aa=alpha (00 to ff); bb=blue (00 to ff); gg=green (00 to ff); rr=red (00 to ff).
     * For example, if you want to apply a blue color with 50 percent opacity to an overlay, you would specify the following:
     * {@literal <color>7fff0000</color>}, where alpha=0x7f, blue=0xff, green=0x00, and red=0x00
     * </p>
     * Source: https://developers.google.com/kml/documentation/kmlreference#colorstyle
     * **************************************************************
     */
    private void setPointsIconsOpacityDynamic(MultipartMainDto multipartMainDto) {
        log.info("Setting the dynamic icons opacity...");

        String opacityColor = getKmlColor("#ffffff", multipartMainDto.getPointIconHexOpacityDynamic());

        NodeList styleMapNodes = document.getElementsByTagName("StyleMap");
        for (int i = 0; i < styleMapNodes.getLength(); i++) {
            Node styleMapNode = styleMapNodes.item(i);
            kmlUtils.getHighlightStyleNode(styleMapNode).ifPresent(highlightStyleNode -> {
                Node iconStyleNode = xmlDomUtils.getChildNodesFromParent(highlightStyleNode, "IconStyle", null, false, true, true).get(0);
                Node colorNode = xmlDomUtils.getChildNodesFromParent(iconStyleNode, "color", null, false, true, true).get(0);
                colorNode.setTextContent(opacityColor);
            });
        }
        log.info("Icons dynamic opacity has been set.");
    }


    private void setPointsTextSizeDynamic(MultipartMainDto multipartMainDto) {
        log.info("Setting the points text dynamic size...");
        NodeList styleMapNodes = document.getElementsByTagName("StyleMap");
        for (int i = 0; i < styleMapNodes.getLength(); i++) {
            Node styleMapNode = styleMapNodes.item(i);
            kmlUtils.getHighlightStyleNode(styleMapNode).ifPresent(highlightStyleNode -> {
                Node labelStyleNode = xmlDomUtils.getChildNodesFromParent(highlightStyleNode, "LabelStyle", null, false, true, true).get(0);
                Node scaleNode = xmlDomUtils.getChildNodesFromParent(labelStyleNode, "scale", null, false, true, true).get(0);
                scaleNode.setTextContent(multipartMainDto.getPointTextSizeScaledDynamic().toString());
            });
        }
        log.info("All the points text dynamic size has been set.");
    }

    private void setPointsTextColorDynamic(MultipartMainDto multipartMainDto) {
        log.info("Setting the names text dynamic color...");
        String kmlColor = getKmlColor(multipartMainDto.getPointTextHexColorDynamic(), multipartMainDto.getPointTextOpacityDynamic());
        NodeList styleMapNodes = document.getElementsByTagName("StyleMap");
        for (int i = 0; i < styleMapNodes.getLength(); i++) {
            Node styleMapNode = styleMapNodes.item(i);
            kmlUtils.getHighlightStyleNode(styleMapNode).ifPresent(highlightStyleNode -> {
                Node labelStyleNode = xmlDomUtils.getChildNodesFromParent(highlightStyleNode, "LabelStyle", null, false, true, true).get(0);
                Node colorNode = xmlDomUtils.getChildNodesFromParent(labelStyleNode, "color", null, false, true, true).get(0);
                colorNode.setTextContent(kmlColor);
            });
        }
        log.info("All the points text dynamic color has been set.");
    }

    /**
     * Converts standard HEX color from HTML User input into KML color standard.
     * ====================================================================
     * https://developers.google.com/kml/documentation/kmlreference#colorstyle
     * Color and opacity (alpha) values are expressed in hexadecimal notation.
     * The range of values for any one color is 0 to 255 (00 to ff).
     * For alpha, 00 is fully transparent and ff is fully opaque.
     * The order of expression is aabbggrr,
     * where aa=alpha (00 to ff); bb=blue (00 to ff); gg=green (00 to ff); rr=red (00 to ff).
     * For example, if you want to apply a blue color with 50 percent opacity to an overlay, you would specify the following:
     * {@code <color>7fff0000</color>}, where alpha=0x7f, blue=0xff, green=0x00, and red=0x00.
     * <p>
     * * Typical RGB incoming from HTML color picker list (as #rrggbb):
     * * HEX COLOR : #ff0000
     * * HEX COLOR : #000000
     * * HEX COLOR : #ffffff
     * * HEX COLOR : #8e4848
     *
     * @param hexColor "#rrggbb" (reg, green, blue) natural input from HTML color picker in hex
     *                 {@link MultipartMainDto#getPointTextHexColor()} or {@link MultipartMainDto#getPointTextHexColorDynamic()}
     * @param opacity  Percentage value from 0 to 100%.
     *                 {@link MultipartMainDto#getPointTextOpacity()} or {@link MultipartMainDto#getPointTextOpacityDynamic()}
     *                 If null, the max "ff" value will be set.
     * @return kml specific color with opacity (alpha-channel) as "aabbggrr" (alpha, blue, green,red)
     * witch is corresponds to KML specification.
     */
    public String getKmlColor(String hexColor, @Nullable Integer opacity) throws IllegalArgumentException {
        log.info("Got '{}' hex color input with {} opacity", hexColor, opacity != null ? opacity : "null");
        if (!hexColor.matches("^#([0-9a-fA-F]{3}|[0-9a-fA-F]{6})$")) {
            throw new IllegalArgumentException(
                    "Color value is not correct! (It has to correspond to '#rrggbb' hex pattern");
        }
        String kmlColor = hexColor.substring(5, 7) + hexColor.substring(3, 5) + hexColor.substring(1, 3);
        String kmlOpacity = opacity != null ? getHexFromPercentage(opacity) : "ff";
        log.info("KML color with opacity will be returned as '{}'", kmlOpacity + kmlColor);
        return kmlOpacity + kmlColor;
    }

    /**
     * Converts standard HEX color from HTML User input into KML color standard.
     * ====================================================================
     * https://developers.google.com/kml/documentation/kmlreference#colorstyle
     * Color and opacity (alpha) values are expressed in hexadecimal notation.
     * The range of values for any one color is 0 to 255 (00 to ff).
     * For alpha, 00 is fully transparent and ff is fully opaque.
     * The order of expression is aabbggrr,
     * where aa=alpha (00 to ff); bb=blue (00 to ff); gg=green (00 to ff); rr=red (00 to ff).
     * For example, if you want to apply a blue color with 50 percent opacity to an overlay, you would specify the following:
     * {@code <color>7fff0000</color>}, where alpha=0x7f, blue=0xff, green=0x00, and red=0x00.
     * <p>
     * * Typical RGB incoming from HTML color picker list (as #rrggbb):
     * * HEX COLOR : #ff0000
     * * HEX COLOR : #000000
     * * HEX COLOR : #ffffff
     * * HEX COLOR : #8e4848
     *
     * @param hexColor   "#rrggbb" (reg, green, blue) natural input from HTML color picker in hex
     *                   {@link MultipartMainDto#getPointTextHexColor()} or {@link MultipartMainDto#getPointTextHexColorDynamic()}
     * @param hexOpacity Hexadecimal value from 00 to FF.
     *                   {@link MultipartMainDto#getPointTextHexOpacity()} or {@link MultipartMainDto#getPointTextHexOpacityDynamic()}
     *                   If null, the max "ff" value will be set.
     * @return kml specific color with opacity (alpha-channel) as "aabbggrr" (alpha, blue, green,red)
     * witch is corresponds to KML specification.
     */
    public String getKmlColor(String hexColor, String hexOpacity) throws IllegalArgumentException {
        log.info("Got '{}' hex color input with {} opacity", hexColor, hexOpacity != null ? hexOpacity : "null");
        if (!hexColor.matches("^#([0-9a-fA-F]{3}|[0-9a-fA-F]{6})$")) {
            throw new IllegalArgumentException(
                    "Color value is not correct! (It has to correspond to '#rrggbb' hex pattern");
        }
        String kmlColor = hexColor.substring(5, 7) + hexColor.substring(3, 5) + hexColor.substring(1, 3);
        log.info("KML color with opacity will be returned as '{}'", hexOpacity + kmlColor);
        return hexOpacity + kmlColor;
    }

    /**
     * Convert incoming percentage value 0 - 100% to an integer in base sixteen 00 - FF (0 - 255).
     * Where 100% = 255 and 1% = 2.55 (Rounding.HALF_UP) accordingly but as two hex digits (e.g.  00, 03, 7F, FF)
     *
     * @param percentage 0 - 100%
     * @return Hexadecimal representation from 00 to FF as two hex digits.
     * @throws IllegalArgumentException if a given percentage below 0 or above 100%
     */
    String getHexFromPercentage(Integer percentage) throws IllegalArgumentException {
        log.debug("Got percentage as {}", percentage);
        if (percentage < 0 || percentage > 100) {
            throw new IllegalArgumentException("Percentage has to be from 0 to 100%!");
        }
        BigDecimal hexPercentage = new BigDecimal(percentage * 2.55).setScale(0, RoundingMode.HALF_UP);
        String hexFormat = String.format("%02x", hexPercentage.toBigInteger());
        log.info("Hex format '{}' will be returned", hexFormat);
        return hexFormat;
    }

}
