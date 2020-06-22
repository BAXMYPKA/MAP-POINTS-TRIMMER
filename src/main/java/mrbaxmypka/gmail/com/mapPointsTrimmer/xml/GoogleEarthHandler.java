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
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@NoArgsConstructor
@Component
public class GoogleEarthHandler {
	
	private Document document;
	/**
	 * {@literal <Style>'s and <StyleMap>'s Nodes by their "id" attributes}
	 */
	private Map<String, Node> styleObjects;
	private List<String> styleUrlsFromPlacemarks;
	
	//TODO: regenerate docs
	Document processXml(Document document, MultipartDto multipartDto) {
		this.document = document;
		Element documentRoot = document.getDocumentElement();
		log.info("Document and {} are received", multipartDto);
		
		setStyleObjects();
		setStyleUrlsFromPlacemarks();
		
		if (multipartDto.getPointIconSize() != null) {
			setPointsIconsSize(multipartDto);
		}
		if (multipartDto.getPointTextSize() != null) {
			setPointTextSize(multipartDto);
		}
		if (multipartDto.getPointTextColor() != null) {
			setPointTextColor(multipartDto);
		}
		//If something dynamic is set we create <StyleMap>'s, additional <Style>'s
		// and <Pair>'s with <key>'s 'normal' and 'highlighted'.
		if (multipartDto.getPointIconSizeDynamic() != null || multipartDto.getPointTextSizeDynamic() != null ||
			multipartDto.getPointTextColorDynamic() != null) {
			createStyleMaps();
		}
		if (multipartDto.getPointIconSizeDynamic() != null) {
			//
		}

//		deleteUnusedStyles();
		return this.document;
	}
	
	private void setStyleObjects() {
		styleObjects = new HashMap<>();
		NodeList styleMapNodes = document.getElementsByTagName("StyleMap");
		NodeList styleNodes = document.getElementsByTagName("Style");
		for (int i = 0; i < styleMapNodes.getLength(); i++) {
			Node styleMapNode = styleMapNodes.item(i);
			styleObjects.put(styleMapNode.getAttributes().getNamedItem("id").getTextContent(), styleMapNode);
		}
		for (int i = 0; i < styleNodes.getLength(); i++) {
			Node styleNode = styleNodes.item(i);
			styleObjects.put(styleNode.getAttributes().getNamedItem("id").getTextContent(), styleNode);
		}
	}
	
	private void setStyleUrlsFromPlacemarks() {
		this.styleUrlsFromPlacemarks =
			getChildNodesFromParents(document.getElementsByTagName("Placemark"), "styleUrl", false, false)
				.stream()
				.map(styleUrlNode -> styleUrlNode.getTextContent().substring(1))
				.collect(Collectors.toList());
	}
	
	/**
	 * {@literal    <StyleMap id="styleMap1">
	 * <Pair>
	 * <key>normal</key>
	 * <styleUrl>#style1</styleUrl>
	 * </Pair>
	 * <Pair>
	 * <key>highlight</key>
	 * <styleUrl>#style2</styleUrl>
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
	 * </Style>}
	 * <Placemark>
	 * <styleUrl>#styleMap1</styleUrl>
	 * </Placemark>
	 * Within all the <Placemark><styleUrl/></Placemark> replaces all references to <StyleMap/> with <Style/>
	 * 1) Checks all the "styleUrl"'s from "Placemark"'s whether they are pointing to "StyleMap"
	 * 2) If true, replace "styleUrl" with "key" "normal" "styleUrl"
	 * 3) Deletes "StyleMap"'s from DOM.
	 */
	private void processStyleMaps(Element documentRoot, MultipartDto multipartDto) {
		NodeList styleMapNodes = documentRoot.getElementsByTagName("StyleMap");
		
		replacePlacemarksStyleUrl();
		
		//Delete all the <StyleMap>'s from Document
		for (int i = styleMapNodes.getLength() - 1; i >= 0; i--) {
			Node styleMapNode = styleMapNodes.item(i);
			styleMapNode.getParentNode().removeChild(styleMapNode);
		}
		//Delete unused <Style>'s from Document
		deleteUnusedStyles();
	}
	
	/**
	 * Replaces links to StyleMaps with links to Styles with key "normal" into all Placemarks.
	 * Given:
	 * {@literal
	 * <StyleMap id="styleMap">
	 * <Pair>
	 * <key>normal</key>
	 * <styleUrl>#style1</styleUrl>
	 * </Pair>
	 * <Pair>
	 * <key>highlight</key>
	 * <styleUrl>#style2</styleUrl>
	 * </Pair>
	 * </StyleMap><Placemark>
	 * <name>Test Placemark 2</name>
	 * <styleUrl>#styleMap</styleUrl>
	 * </Placemark>
	 * This method
	 * Replaces <styleUrl>#styleMap</styleUrl> of <Placemark> with "normal" <styleUrl>#style1</styleUrl>}
	 */
	private void replacePlacemarksStyleUrl() {
		NodeList placemarkNodes = document.getElementsByTagName("Placemark");
		//<styleUrl>'s from <Placemark>'s
		List<Node> placemarksStyleUrls =
			getChildNodesFromParents(placemarkNodes, "styleUrl", false, false);
		for (int i = 0; i < placemarksStyleUrls.size(); i++) {
			Node placemarkStyleUrlNode = placemarksStyleUrls.get(i);
			String currentUrl = placemarkStyleUrlNode.getTextContent(); //#exampleUrl
			String urlToStyle = getStyleUrlToNormalStyleOfStyleMap(currentUrl);
			placemarkStyleUrlNode.setTextContent(urlToStyle);
		}
	}
	
	/**
	 * {@literal    <StyleMap id="m_ylw-pushpin12">
	 * <Pair>
	 * <key>normal</key>
	 * <styleUrl>#s_ylw-pushpin40</styleUrl>
	 * </Pair>
	 * <Pair>
	 * <key>highlight</key>
	 * <styleUrl>#s_ylw-pushpin_hl11</styleUrl>
	 * </Pair>
	 * </StyleMap>
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
	 * </LabelStyle>
	 * </Style>}
	 * If a given 'styleUrl' reference to {@literal <StyleMap>} it will be replaced with
	 * {@literal <styleUrl> with <key> 'normal'}.
	 * Otherwise the initial parameter will be returned
	 *
	 * @param styleUrl {@literal e.g. "#exampleUrl" to <StyleMap id="styleUrl"> or <Style id="styleUrl">}
	 * @return If a given 'styleUrl' reference to {@literal <StyleMap> it will be replaced with
	 * <styleUrl> with <key> 'normal'} and returned.
	 * Otherwise the initial parameter will be returned
	 */
	private String getStyleUrlToNormalStyleOfStyleMap(String styleUrl) {
		NodeList styleMapNodes = document.getElementsByTagName("StyleMap");
		String trimmedUrl = styleUrl.startsWith("#") ? styleUrl.substring(1) : styleUrl;
		
		for (int i = 0; i < styleMapNodes.getLength(); i++) {
			Node styleMapNode = styleMapNodes.item(i);
			Node idNode = styleMapNode.getAttributes().getNamedItem("id");
			String idAttribute = idNode.getTextContent();
			if (idAttribute.equals(trimmedUrl)) return getKeyNormalStyleUrlFromStyleMap(styleMapNode);
		}
		//No <StyleMap> with such an "id" attribute
		return styleUrl;
	}
	
	private String getKeyNormalStyleUrlFromStyleMap(Node styleMap) {
		List<Node> pairNodes = getChildNodesFromParent(styleMap, "Pair", null, false, false);
		//<Pair> <key>normal</key> <styleUrl>#exampleUrl</styleUrl> </Pair>
		Node pairWithKeyNormal = pairNodes.stream()
			.filter(pairNode ->
				!getChildNodesFromParent(pairNode, "key", "normal", false, false).isEmpty())
			.collect(Collectors.toList()).get(0);
		
		return getChildNodesFromParent(pairWithKeyNormal, "styleUrl", null, false, false)
			.get(0).getTextContent();
	}
	
	/**
	 * {@literal If any <styleUrl/> from <Placemark/>'s points to <Style id="styleId"/> those <StyleUrl/>'s have to
	 * be deleted}
	 */
	private void deleteUnusedStyles() {
		List<Node> placemarksStyleUrlNodes = getChildNodesFromParents(
			document.getElementsByTagName("Placemark"), "styleUrl", false, false);
		List<String> urlsToStyles = placemarksStyleUrlNodes.stream()
			.map(node -> node.getTextContent().substring(1)) // To remove starting '#' sign
			.collect(Collectors.toList());
		
		NodeList styleNodes = document.getElementsByTagName("Style");
		for (int i = 0; i < styleNodes.getLength(); i++) {
			Node styleNode = styleNodes.item(i);
			if (styleNode.getAttributes() != null && styleNode.getAttributes().getNamedItem("id") != null) {
				String idAttributeString = styleNode.getAttributes().getNamedItem("id").getTextContent();
				if (!urlsToStyles.contains(idAttributeString)) {
					styleNode.getParentNode().removeChild(styleNode);
				}
			}
		}
	}
	
	//TODO: to use XPath to select only <Style/>'s with attribute id=""
	
	private void setPointsIconsSize(MultipartDto multipartDto) {
		log.info("Setting the icons size...");
		
		String scale = multipartDto.getPointIconSizeScaled().toString();
		
		styleUrlsFromPlacemarks.forEach(styleUrl -> {
			Node styleObject = styleObjects.get(styleUrl);
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
	 * Either applies to existing <LabelStyle> tags or a new ones will be created for every <Style>.
	 * Because <LabelStyle> automatically displays <name> content from <Placemark>s.
	 */
	private void setPointTextSize(MultipartDto multipartDto) {
		log.info("Setting the points names size...");
		String scale = multipartDto.getPointTextSizeScaled().toString();
		
		styleUrlsFromPlacemarks.forEach(styleUrl -> {
			Node styleObject = styleObjects.get(styleUrl);
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
	
	private void setPointTextColor(MultipartDto multipartDto) {
		log.info("Setting the names text color...");
		String kmlColor = getKmlColor(multipartDto.getPointTextColor(), multipartDto);
		
		styleUrlsFromPlacemarks.forEach(styleUrl -> {
			Node styleObject = styleObjects.get(styleUrl);
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
	
	private Node getIconStyleScaleNodeFromStyle(Node styleNode) {
		Node iconStyleNode = getChildNodesFromParent(styleNode, "IconStyle", null, false, true).get(0);
		return getChildNodesFromParent(iconStyleNode, "scale", null, false, true).get(0);
	}
	
	private Node getLabelStyleScaleNodeFromStyle(Node styleNode) {
		Node labelStyleNode = getChildNodesFromParent(styleNode, "LabelStyle", null, false, true).get(0);
		return getChildNodesFromParent(labelStyleNode, "scale", null, false, true).get(0);
	}
	
	private Node getLabelStyleColorNodeFromStyle(Node styleNode) {
		Node labelStyleNode = getChildNodesFromParent(styleNode, "LabelStyle", null, false, true).get(0);
		return getChildNodesFromParent(labelStyleNode, "color", null, false, true).get(0);
	}
	
	private Node getNormalStyleNodeFromStyleMap(Node styleMap) {
		return getChildNodesFromParent(styleMap, "Pair", null, false, false)
			.stream()
			.filter(pairNode -> !getChildNodesFromParent(pairNode, "key", "normal", false, false).isEmpty())
			.findFirst()
			.map(normalPairNode -> getChildNodesFromParent(normalPairNode, "styleUrl", null, false, false).get(0))
			.map(normalStyleUrl -> styleObjects.get(normalStyleUrl.getTextContent().substring(1)))
			.get();
	}
	
/*
	private Node getNormalStyleNodeFromStyleMap(Node styleMap) {
		Optional<Node> first = getChildNodesFromParent(styleMap, "Pair", null, false, false)
			.stream()
			.filter(pairNode -> !getChildNodesFromParent(pairNode, "key", "normal", false, false).isEmpty())
			.findFirst();
		Optional<Node> styleUrl = first
			.map(normalPairNode -> getChildNodesFromParent(normalPairNode, "styleUrl", null, false, false).get(0));
		Optional<String> node = styleUrl.map(normalStyleUrl -> normalStyleUrl.getTextContent());
		
		return styleObjects.get(node.get());
	}
*/
	
	/**
	 * @param styleNodes {@link NodeList} with all presented <Style/>'s within xml Document.
	 * @return <scale/> of every <LabelStyle/> from every <Style/>
	 */
	private List<Node> getLabelStylesScales(NodeList styleNodes) {
		List<Node> labelStylesScaleNodes = new ArrayList<>();
		
		List<Node> labelStyles = getChildNodesFromParents(styleNodes, "LabelStyle", false, true);
		labelStyles.forEach(labelStyleNode -> {
			Node scaleNode = getChildNodesFromParent(labelStyleNode, "scale", null, false, true).get(0);
			labelStylesScaleNodes.add(scaleNode);
		});
		log.debug("All <scale>'s within <LabelStyle>'s tags have been found or created.");
		return labelStylesScaleNodes;
	}
	
	/**
	 * @param styleNodes {@link NodeList} with all presented <Style/>'s within xml Document.
	 * @return <color/> of every <LabelStyle/> from every <Style/>
	 */
	private List<Node> getLabelStylesColors(NodeList styleNodes) {
		List<Node> labelStylesColorNodes = new ArrayList<>();
		
		List<Node> labelStyleNodes = getChildNodesFromParents(styleNodes, "LabelStyle", false, true);
		labelStyleNodes.forEach(labelStyleNode -> {
			Node color = getChildNodesFromParent(labelStyleNode, "color", null, false, true).get(0);
			labelStylesColorNodes.add(color);
		});
		log.debug("All <color>'s within <LabelStyle>'s tags have been found or created.");
		return labelStylesColorNodes;
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
	 * {@literal <color>7fff0000</color>}, where alpha=0x7f, blue=0xff, green=0x00, and red=0x00.
	 * <p>
	 * * Typical RGB incoming from HTML color picker list (as #rrggbb):
	 * * HEX COLOR : #ff0000
	 * * HEX COLOR : #000000
	 * * HEX COLOR : #ffffff
	 * * HEX COLOR : #8e4848
	 *
	 * @param hexColor "#rrggbb" (reg, green, blue) natural input from HTML color picker in hex
	 * @return kml specific color with opacity (alpha-channel) as "aabbggrr" (alpha, blue, green,red)
	 * witch is corresponds to KML specification.
	 */
	protected String getKmlColor(String hexColor, MultipartDto multipartDto) throws IllegalArgumentException {
		log.debug("Got '{}' hex color input", hexColor);
		if (!hexColor.matches("^#([0-9a-f]{3}|[0-9a-f]{6})$")) {
			throw new IllegalArgumentException(
				"Color value is not correct! (It has to correspond to '#rrggbb' hex pattern");
		}
		String kmlColor = hexColor.substring(5, 7) + hexColor.substring(3, 5) + hexColor.substring(1, 3);
		log.debug("Hex color has been converted into '{}' KML color", kmlColor);
		if (multipartDto.getPointTextOpacity() != null) {
			String opacity = getHexFromPercentage(multipartDto.getPointTextOpacity());
			log.info("KML color with opacity will be returned as '{}'", opacity + kmlColor);
			return opacity + kmlColor;
		} else {
			log.info("KML color will be returned as '{}'", "ff" + kmlColor);
			return "ff" + kmlColor;
		}
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
	
	private void createStyleMaps() {
		Element documentElement = document.getDocumentElement();
		List<Node> styleNodes = getChildNodesFromParent(documentElement, "Style", null, false, false);
		styleNodes.forEach(styleNode -> {
			Node highlightStyleNode = createHighlightStyle(styleNode);
			Node styleMap = createStyleMapNode(styleNode, highlightStyleNode);
		});
		
		Node name = getChildNodesFromParent(documentElement, "name", null, false, false).get(0);
		name.insertBefore(null, name);
	}
	
	private Node createHighlightStyle(Node styleNode) {
		Element styleHighlightNode = (Element) styleNode.cloneNode(true);
		styleHighlightNode.setAttribute("id", "highlight:" + styleNode.getAttributes().getNamedItem("id").toString());
		return styleHighlightNode;
	}
	
	/**
	 * {@literal
	 * <StyleMap id="styleMap1">
	 * <Pair>
	 * <key>normal</key>
	 * <styleUrl>#style1</styleUrl>
	 * </Pair>
	 * <Pair>
	 * <key>highlight</key>
	 * <styleUrl>#style2</styleUrl>
	 * </Pair>
	 * </StyleMap>
	 * }
	 */
	private Node createStyleMapNode(Node styleNode, Node styleHighlightNode) {
		Element styleMapNode = document.createElement("StyleMap");
		styleMapNode.setAttribute("id", "styleMap:" + styleNode.getAttributes().getNamedItem("id").toString());
		Element pairNormalNode = document.createElement("Pair");
		Element keyNormalNode = document.createElement("key");
		keyNormalNode.setTextContent("normal");
		Element styleUrlNormal = document.createElement("styleUrl");
		styleUrlNormal.setTextContent("#" + styleNode.getAttributes().getNamedItem("id").getTextContent());
		pairNormalNode.appendChild(keyNormalNode);
		pairNormalNode.appendChild(styleUrlNormal);
		styleMapNode.appendChild(pairNormalNode);
		
		Element pairHighlightNode = document.createElement("Pair");
		Element keyHighlightNode = document.createElement("key");
		keyHighlightNode.setTextContent("highlight");
		Element styleUrlHighlight = document.createElement("styleUrl");
//		styleUrlHighlight.setTextContent("#" + styleNode.getAttributes().getNamedItem("id").getTextContent());
		pairHighlightNode.appendChild(keyHighlightNode);
		pairHighlightNode.appendChild(styleUrlHighlight);
		styleMapNode.appendChild(pairHighlightNode);
		
		Node nameNode = getChildNodesFromParent(document.getDocumentElement(), "name", null, false, false).get(0);
		document.insertBefore(styleMapNode, nameNode);
		return styleMapNode;
	}
	
	/**
	 * Some programs as Google Earth has special href they internally redirect to their local image store.
	 * It is not recommended to change those type of hrefs.
	 * Evaluates a given "href" or "src" not to start with "http://maps.google.com" as those types of hrefs are
	 * Google Earth Pro specific and cannot be replaced with the new links.
	 *
	 * @param href Href or src to be evaluated
	 * @return If the given href is Google Earth specific and cannot be replaced with a new href or not.
	 */
	//TODO: to download special GE images
	boolean isImageHrefChangeable(String href) {
		log.trace("Href to evaluate as GoogleMap special = '{}'", href);
		String googleMapSpecialUrl = "http://maps.google.com/";
		return !href.startsWith(googleMapSpecialUrl);
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////// COMMON METHODS ///////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * @param parents        A Parents {@link NodeList} to find children of
	 * @param childNodeName  A Child Node tag name to be found
	 * @param recursively    To look through the children of all the children of every Node
	 * @param createIfAbsent If not a such Child Node within the Parent Node found
	 *                       a new one will be created, appended to the Parent and added to the resulting List.
	 * @return A List of Children Nodes or {@link java.util.Collections#EMPTY_LIST} if nothing found.
	 * @throws IllegalArgumentException if 'recursively' and 'createIfAbsent' both == true.
	 */
	List<Node> getChildNodesFromParents(NodeList parents, String childNodeName, boolean recursively, boolean createIfAbsent)
		throws IllegalArgumentException {
		if (recursively && createIfAbsent) throw new IllegalArgumentException(
			"You can create only direct children within parents! 'recursively' is only for looking through exist nodes!");
		
		List<Node> children = new ArrayList<>();
		
		for (int i = 0; i < parents.getLength(); i++) {
			Node parentNode = parents.item(i);
			//Looking through all the children of all the children for the given parent
			if (recursively) {
				List<Node> allChildNodes = getAllChildrenRecursively(new ArrayList<>(), parentNode);
				allChildNodes.stream()
					.filter(node -> node.getNodeName() != null && node.getNodeName().equals(childNodeName))
					.forEach(children::add);
				continue;
			}
			//Looking through direct children
			NodeList childNodes = parentNode.getChildNodes();
			List<Node> existingChildren = new ArrayList<>();
			for (int j = 0; j < childNodes.getLength(); j++) {
				Node childNode = childNodes.item(j);
				if (childNode.getNodeName() != null && childNode.getNodeName().equals(childNodeName)) {
					existingChildren.add(childNode);
					log.trace("Existing <{}> has been found in <{}>", childNodeName, parentNode.getNodeName());
				}
			}
			if (createIfAbsent && existingChildren.isEmpty()) {
				//Has to create a child if absent and no child is exist
				Element newChildNode = document.createElement(childNodeName);
				parentNode.appendChild(newChildNode);
				children.add(newChildNode);
				log.trace("As 'createIfAbsent'== true and <{}> has't been found in <{}> it was created and appended",
					childNodeName, parentNode.getNodeName());
			} else {
				//Just add the found children (even if they aren't)
				children.addAll(existingChildren);
			}
		}
		log.trace("{} <{}> children have been found for <{}>", children.size(), childNodeName, parents.item(0).getNodeName());
		return children;
	}
	
	/**
	 * @param parents        A Parents {@link NodeList} to find children of
	 * @param childNodeName  A Child Node tag name to be found
	 * @param recursively    To look through the children of all the children of every Node
	 * @param createIfAbsent If not a such Child Node within the Parent Node found
	 *                       a new one will be created, appended to the Parent and added to the resulting List.
	 * @return A List of Children Nodes or {@link java.util.Collections#EMPTY_LIST} if nothing found.
	 * @throws IllegalArgumentException if 'recursively' and 'createIfAbsent' both == true.
	 */
	List<Node> getChildNodesFromParents(List<Node> parents, String childNodeName, boolean recursively, boolean createIfAbsent)
		throws IllegalArgumentException {
		if (recursively && createIfAbsent) throw new IllegalArgumentException(
			"You can create only direct children within parents! 'recursively' is only for looking through exist nodes!");
		
		List<Node> children = new ArrayList<>();
		
		for (int i = 0; i < parents.size(); i++) {
			Node parentNode = parents.get(i);
			//Looking through all the children of all the children for the given parent
			if (recursively) {
				List<Node> allChildNodes = getAllChildrenRecursively(new ArrayList<>(), parentNode);
				allChildNodes.stream()
					.filter(node -> node.getNodeName() != null && node.getNodeName().equals(childNodeName))
					.forEach(children::add);
				continue;
			}
			//Looking through direct children
			NodeList childNodes = parentNode.getChildNodes();
			List<Node> existingChildren = new ArrayList<>();
			for (int j = 0; j < childNodes.getLength(); j++) {
				Node childNode = childNodes.item(j);
				if (childNode.getNodeName() != null && childNode.getNodeName().equals(childNodeName)) {
					existingChildren.add(childNode);
					log.trace("Existing <{}> has been found in <{}>", childNodeName, parentNode.getNodeName());
				}
			}
			if (createIfAbsent && existingChildren.isEmpty()) {
				//Has to create a child if absent and no child is exist
				Element newChildNode = document.createElement(childNodeName);
				parentNode.appendChild(newChildNode);
				children.add(newChildNode);
				log.trace("As 'createIfAbsent'== true and <{}> has't been found in <{}> it was created and appended",
					childNodeName, parentNode.getNodeName());
			} else {
				//Just add the found children (even if they aren't)
				children.addAll(existingChildren);
			}
		}
		log.trace("{} <{}> children have been found for <{}>", children.size(), childNodeName, parents.get(0).getNodeName());
		return children;
	}
	
	/**
	 * @param parent         A Parent Node to find child of
	 * @param childNodeName  A Child Node tag name to be found
	 * @param childNodeValue {@link Nullable} If not null only the Child Node with equal text content will be returned
	 * @param recursively    To look through the children of all the children of every Node
	 * @param createIfAbsent If not a such Child Node within the Parent Node found
	 *                       a new one will be created, appended to the Parent and added to the resulting List.
	 * @return A List of Children Nodes or {@link java.util.Collections#EMPTY_LIST} if nothing found.
	 * @throws IllegalArgumentException if 'recursively' and 'createIfAbsent' both == true.
	 */
	List<Node> getChildNodesFromParent(
		Node parent, String childNodeName, @Nullable String childNodeValue, boolean recursively, boolean createIfAbsent)
		throws IllegalArgumentException {
		
		if (recursively && createIfAbsent) throw new IllegalArgumentException(
			"You can create only direct children within parents! 'recursively' is only for looking through exist nodes!");
		
		List<Node> childrenNodesToBeReturned = new ArrayList<>();
		
		NodeList childNodes = parent.getChildNodes();
		List<Node> existingChildren = new ArrayList<>();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node childNode = childNodes.item(i);
			
			if (recursively) {
				List<Node> allChildren = getAllChildrenRecursively(new ArrayList<>(), childNode);
				allChildren.stream()
					.filter(node -> node.getNodeName() != null && node.getNodeName().equals(childNodeName))
					.forEach(existingChildren::add);
				continue;
			}
			if (childNode.getNodeName() != null && childNode.getNodeName().equals(childNodeName)) {
				if (childNodeValue == null) {
					existingChildren.add(childNode);
				} else if (childNode.getTextContent() != null && childNode.getTextContent().equals(childNodeValue)) {
					existingChildren.add(childNode);
				}
			}
		}
		if (createIfAbsent && existingChildren.isEmpty()) {
			//Has to create a child if absent and no child is exist
			Element newChildNode = document.createElement(childNodeName);
			parent.appendChild(newChildNode);
			childrenNodesToBeReturned.add(newChildNode);
			log.trace("As 'createIfAbsent'== true and <{}> has't been found in <{}> it was created and appended",
				childNodeName, parent.getNodeName());
		} else {
			//Just add the found children (even if they aren't)
			childrenNodesToBeReturned.addAll(existingChildren);
		}
		log.trace("{} <{}> children have been found for <{}>",
			childrenNodesToBeReturned.size(), childNodeName, parent.getNodeName());
		return childrenNodesToBeReturned;
	}
	
	private List<Node> getAllChildrenRecursively(List<Node> nodeList, Node parentNode) {
		if (parentNode.hasChildNodes()) {
			NodeList childNodes = parentNode.getChildNodes();
			for (int i = 0; i < childNodes.getLength(); i++) {
				Node childNode = childNodes.item(i);
				getAllChildrenRecursively(nodeList, childNode);
			}
		} else {
			nodeList.add(parentNode);
		}
		return nodeList;
	}
}
