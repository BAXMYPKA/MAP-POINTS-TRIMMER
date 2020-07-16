package mrbaxmypka.gmail.com.mapPointsTrimmer.xml;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mrbaxmypka.gmail.com.mapPointsTrimmer.entitiesDto.MultipartDto;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
//@NoArgsConstructor
//@Component
public class GoogleEarthHandler {
	
	private Document document;
	/**
	 * {@literal <Style>'s and <StyleMap>'s Nodes by their "id" attributes}
	 */
	private Map<String, Node> styleObjectsMap;
	private List<String> styleUrlsFromPlacemarks;
	private XmlDomUtils xmlDomUtils;
	
	Document processXml(Document document, MultipartDto multipartDto) {
		this.document = document;
		xmlDomUtils = new XmlDomUtils(document);
		log.info("Document and {} are received", multipartDto);
		
		setStyleObjectsMap();
		setStyleUrlsFromPlacemarks();
		
		if (multipartDto.getPointIconSize() != null) {
			setPointsIconsSize(multipartDto);
		}
		if (multipartDto.getPointIconOpacity() != null) {
			setPointsIconsOpacity(multipartDto);
		}
		if (multipartDto.getPointTextSize() != null) {
			setPointsTextSize(multipartDto);
		}
		if (multipartDto.getPointTextHexColor() != null) {
			setPointsTextColor(multipartDto);
		}
		//If something dynamic is set we create and assign <StyleMap>'s if absent
		if (multipartDto.getPointIconSizeDynamic() != null ||
			multipartDto.getPointTextSizeDynamic() != null ||
			multipartDto.getPointTextHexColorDynamic() != null) {
			createStyleMaps();
		}
		if (multipartDto.getPointIconSizeDynamic() != null) {
			setPointsIconsSizeDynamic(multipartDto);
		}
		if (multipartDto.getPointIconOpacityDynamic() != null) {
			setPointsIconsOpacityDynamic(multipartDto);
		}
		if (multipartDto.getPointTextSizeDynamic() != null) {
			setPointsTextSizeDynamic(multipartDto);
		}
		if (multipartDto.getPointTextHexColorDynamic() != null) {
			setPointsTextColorDynamic(multipartDto);
		}
		return this.document;
	}
	
	/**
	 * {@code Puts aLL the <Style/>'s and <StyleMap/>'s Nodes from the Document by their "id" attribute.}
	 */
	private void setStyleObjectsMap() {
		styleObjectsMap = new HashMap<>();
		NodeList styleMapNodes = document.getElementsByTagName("StyleMap");
		NodeList styleNodes = document.getElementsByTagName("Style");
		for (int i = 0; i < styleMapNodes.getLength(); i++) {
			Node styleMapNode = styleMapNodes.item(i);
			styleObjectsMap.put(styleMapNode.getAttributes().getNamedItem("id").getTextContent(), styleMapNode);
		}
		for (int i = 0; i < styleNodes.getLength(); i++) {
			Node styleNode = styleNodes.item(i);
			styleObjectsMap.put(styleNode.getAttributes().getNamedItem("id").getTextContent(), styleNode);
		}
		log.trace("Style objects map's set with the size={}", styleObjectsMap.size());
	}
	
	private void setStyleUrlsFromPlacemarks() {
		styleUrlsFromPlacemarks =
			xmlDomUtils.getChildNodesFromParents(document.getElementsByTagName("Placemark"), "styleUrl", false, false
				, false)
				.stream()
				.map(styleUrlNode -> styleUrlNode.getTextContent().substring(1))
				.collect(Collectors.toList());
		log.trace("The List<String> of StyleUrls from Placemarks has been set with size={}", styleUrlsFromPlacemarks.size());
	}
	
	private void setPointsIconsSize(MultipartDto multipartDto) {
		log.info("Setting the icons size...");
		
		String scale = multipartDto.getPointIconSizeScaled().toString();
		
		styleUrlsFromPlacemarks.forEach(styleUrl -> {
			Node styleObject = styleObjectsMap.get(styleUrl);
			if (styleObject.getNodeName().equals("Style")) {
				Node iconStyleScaleNode = getIconStyleScaleNodeFromStyle(styleObject);
				iconStyleScaleNode.setTextContent(scale);
			} else if (styleObject.getNodeName().equals("StyleMap")) {
				Node normalStyleNode = getNormalStyleNodeFromStyleMap(styleObject);
				Node iconStyleScaleNode = getIconStyleScaleNodeFromStyle(normalStyleNode);
				iconStyleScaleNode.setTextContent(scale);
			}
		});
		log.info("Icons size has been set.");
	}
	
	/**
	 * HTML hexadecimal color with max value "#FFFFFF" will be prefixed with 'kml color' hexadecimal value
	 * from 00 to FF as "00FFFFFF" or "FFFFFFFF" etc.
	 */
	private void setPointsIconsOpacity(MultipartDto multipartDto) {
		log.info("Setting the icons opacity...");
		
		String opacityColor = getKmlColor("#ffffff", multipartDto.getPointIconHexOpacity());
		
		styleUrlsFromPlacemarks.forEach(styleUrl -> {
			Node styleObject = styleObjectsMap.get(styleUrl);
			if (styleObject.getNodeName().equals("Style")) {
				getIconsStyleColorNodeFromStyle(styleObject);
				Node iconStyleColorNode = getIconsStyleColorNodeFromStyle(styleObject);
				iconStyleColorNode.setTextContent(opacityColor);
			} else if (styleObject.getNodeName().equals("StyleMap")) {
				Node normalStyleNode = getNormalStyleNodeFromStyleMap(styleObject);
				Node iconStyleColorNode = getIconsStyleColorNodeFromStyle(normalStyleNode);
				iconStyleColorNode.setTextContent(opacityColor);
			}
		});
		log.info("Icons opacity has been set.");
	}
	
	/**
	 * Either applies to existing <LabelStyle> tags or a new ones will be created for every <Style>.
	 * Because <LabelStyle> automatically displays <name> content from <Placemark>s.
	 */
	private void setPointsTextSize(MultipartDto multipartDto) {
		log.info("Setting the points names size...");
		String scale = multipartDto.getPointTextSizeScaled().toString();
		
		styleUrlsFromPlacemarks.forEach(styleUrl -> {
			Node styleObject = styleObjectsMap.get(styleUrl);
			if (styleObject.getNodeName().equals("Style")) {
				Node labelStyleScaleNode = getLabelStyleScaleNodeFromStyle(styleObject);
				labelStyleScaleNode.setTextContent(scale);
			} else if (styleObject.getNodeName().equals("StyleMap")) {
				Node normalStyleNode = getNormalStyleNodeFromStyleMap(styleObject);
				Node labelStyleScaleNode = getLabelStyleScaleNodeFromStyle(normalStyleNode);
				labelStyleScaleNode.setTextContent(scale);
			}
		});
		log.info("Points names size has been set.");
	}
	
	private void setPointsTextColor(MultipartDto multipartDto) {
		log.info("Setting the names text color...");
		String kmlColor = getKmlColor(multipartDto.getPointTextHexColor(), multipartDto.getPointTextOpacity());
		
		styleUrlsFromPlacemarks.forEach(styleUrl -> {
			Node styleObject = styleObjectsMap.get(styleUrl);
			if (styleObject.getNodeName().equals("Style")) {
				Node labelStyleColorNode = getLabelStyleColorNodeFromStyle(styleObject);
				labelStyleColorNode.setTextContent(kmlColor);
			} else if (styleObject.getNodeName().equals("StyleMap")) {
				Node normalStyleNode = getNormalStyleNodeFromStyleMap(styleObject);
				Node labelStyleColorNode = getLabelStyleColorNodeFromStyle(normalStyleNode);
				labelStyleColorNode.setTextContent(kmlColor);
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
	 */
	private Node getNormalStyleNodeFromStyleMap(Node styleMap) {
		return xmlDomUtils.getChildNodesFromParent(styleMap, "Pair", null, false, false, false)
			.stream()
			.filter(pairNode -> !xmlDomUtils.getChildNodesFromParent(pairNode, "key", "normal", false, false, false).isEmpty())
			.findFirst()
			.map(normalPairNode -> xmlDomUtils.getChildNodesFromParent(normalPairNode, "styleUrl", null, false, false, false).get(0))
			.map(normalStyleUrl -> styleObjectsMap.get(normalStyleUrl.getTextContent().substring(1)))
			.get();
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
	 */
	private Node getHighlightStyleNodeFromStyleMap(Node styleMap) {
		return xmlDomUtils.getChildNodesFromParent(styleMap, "Pair", null, false, false, false)
			.stream()
			.filter(pairNode -> !xmlDomUtils.getChildNodesFromParent(pairNode, "key", "highlight", false, false, false).isEmpty())
			.findFirst()
			.map(highlightPairNode -> xmlDomUtils.getChildNodesFromParent(highlightPairNode, "styleUrl", null, false, false, false).get(0))
			.map(highlightStyleUrl -> styleObjectsMap.get(highlightStyleUrl.getTextContent().substring(1)))
			.get();
	}
	
	/**
	 * {@code All the <Placemark><styleUrl/></Placemark> have to reference to <StyleMap/>'s instead of <Style/>'s}
	 */
	private void createStyleMaps() {
		List<Node> placemarksStyleUrlNodes =
			xmlDomUtils.getChildNodesFromParents(document.getElementsByTagName("Placemark"), "styleUrl", false, false, false);
		placemarksStyleUrlNodes.stream()
			.filter(styleUrlNode -> styleObjectsMap.get(styleUrlNode.getTextContent().substring(1)) != null)
			.forEach(styleUrlNode -> {
				Node styleObjectNode = styleObjectsMap.get(styleUrlNode.getTextContent().substring(1));
				if (styleObjectNode.getNodeName().equals("Style")) {
					//Replace styleUrl to <Style/> with <StyleMap/>
					Node styleMapNode = createStyleMapNode(styleObjectNode);
					styleUrlNode.setTextContent("#" + styleMapNode.getAttributes().getNamedItem("id").getTextContent());
					styleObjectsMap.put(styleMapNode.getAttributes().getNamedItem("id").getTextContent(), styleMapNode);
				}
			});
		//Refresh existing collections
		setStyleObjectsMap();
		setStyleUrlsFromPlacemarks();
		log.info("<StyleMap>'s have been created for all the <Placemark><styleUrl/></Placemark>. ({} StyleObjects)",
			styleObjectsMap.size());
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
	 */
	private Node createStyleMapNode(Node styleNode) {
		String idAttribute = "styleMapOf:" + styleNode.getAttributes().getNamedItem("id").getTextContent();
		
		if (styleObjectsMap.containsKey(idAttribute)) {
			//Not to create duplicates
			return styleObjectsMap.get(idAttribute);
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
		
		insertIntoDocumentNode(highlightStyleNode);
		insertIntoDocumentNode(styleMapNode);
		
		return styleMapNode;
	}
	
	private Node createHighlightStyleNode(Node styleNode) {
		Element styleHighlightNode = (Element) styleNode.cloneNode(true);
		styleHighlightNode.setAttribute("id", "highlightOf:" + styleNode.getAttributes().getNamedItem("id").getTextContent());
		return styleHighlightNode;
	}
	
	private void setPointsIconsSizeDynamic(MultipartDto multipartDto) {
		log.info("Setting the points icons dynamic size...");
		NodeList styleMapNodes = document.getElementsByTagName("StyleMap");
		for (int i = 0; i < styleMapNodes.getLength(); i++) {
			Node styleMapNode = styleMapNodes.item(i);
			Node highlightStyleNode = getHighlightStyleNodeFromStyleMap(styleMapNode);
			Node iconStyleNode = xmlDomUtils.getChildNodesFromParent(highlightStyleNode, "IconStyle", null, false, true, true).get(0);
			Node scaleNode = xmlDomUtils.getChildNodesFromParent(iconStyleNode, "scale", null, false, true, true).get(0);
			scaleNode.setTextContent(multipartDto.getPointIconSizeScaledDynamic().toString());
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
	private void setPointsIconsOpacityDynamic(MultipartDto multipartDto) {
		log.info("Setting the dynamic icons opacity...");
		
		String opacityColor = getKmlColor("#ffffff", multipartDto.getPointIconHexOpacityDynamic());
		
		NodeList styleMapNodes = document.getElementsByTagName("StyleMap");
		for (int i = 0; i < styleMapNodes.getLength(); i++) {
			Node styleMapNode = styleMapNodes.item(i);
			Node highlightStyleNode = getHighlightStyleNodeFromStyleMap(styleMapNode);
			Node iconStyleNode = xmlDomUtils.getChildNodesFromParent(highlightStyleNode, "IconStyle", null, false, true, true).get(0);
			Node colorNode = xmlDomUtils.getChildNodesFromParent(iconStyleNode, "color", null, false, true, true).get(0);
			colorNode.setTextContent(opacityColor);
		}
		log.info("Icons dynamic opacity has been set.");
	}
	
	
	private void setPointsTextSizeDynamic(MultipartDto multipartDto) {
		log.info("Setting the points text dynamic size...");
		NodeList styleMapNodes = document.getElementsByTagName("StyleMap");
		for (int i = 0; i < styleMapNodes.getLength(); i++) {
			Node styleMapNode = styleMapNodes.item(i);
			Node highlightStyleNode = getHighlightStyleNodeFromStyleMap(styleMapNode);
			Node labelStyleNode = xmlDomUtils.getChildNodesFromParent(highlightStyleNode, "LabelStyle", null, false, true, true).get(0);
			Node scaleNode = xmlDomUtils.getChildNodesFromParent(labelStyleNode, "scale", null, false, true, true).get(0);
			scaleNode.setTextContent(multipartDto.getPointTextSizeScaledDynamic().toString());
		}
		log.info("All the points text dynamic size has been set.");
	}
	
	private void setPointsTextColorDynamic(MultipartDto multipartDto) {
		log.info("Setting the names text dynamic color...");
		String kmlColor = getKmlColor(multipartDto.getPointTextHexColorDynamic(), multipartDto.getPointTextOpacityDynamic());
		NodeList styleMapNodes = document.getElementsByTagName("StyleMap");
		for (int i = 0; i < styleMapNodes.getLength(); i++) {
			Node styleMapNode = styleMapNodes.item(i);
			Node highlightStyleNode = getHighlightStyleNodeFromStyleMap(styleMapNode);
			Node labelStyleNode = xmlDomUtils.getChildNodesFromParent(highlightStyleNode, "LabelStyle", null, false, true, true).get(0);
			Node colorNode = xmlDomUtils.getChildNodesFromParent(labelStyleNode, "color", null, false, true, true).get(0);
			colorNode.setTextContent(kmlColor);
		}
		log.info("All the points text dynamic color has been set.");
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
	private Node insertIntoDocumentNode(Node styleMapNode) {
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
	 *                 {@link MultipartDto#getPointTextHexColor()} or {@link MultipartDto#getPointTextHexColorDynamic()}
	 * @param opacity  Percentage value from 0 to 100%.
	 *                 {@link MultipartDto#getPointTextOpacity()} or {@link MultipartDto#getPointTextOpacityDynamic()}
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
	 *                   {@link MultipartDto#getPointTextHexColor()} or {@link MultipartDto#getPointTextHexColorDynamic()}
	 * @param hexOpacity Hexadecimal value from 00 to FF.
	 *                   {@link MultipartDto#getPointTextHexOpacity()} or {@link MultipartDto#getPointTextHexOpacityDynamic()}
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
