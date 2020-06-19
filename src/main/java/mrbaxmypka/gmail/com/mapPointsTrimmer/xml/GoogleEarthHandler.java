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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@NoArgsConstructor
@Component
public class GoogleEarthHandler {
	
	private Document document;
	
	//TODO: regenerate docs
	Document processXml(Document document, MultipartDto multipartDto) {
		this.document = document;
		Element documentRoot = document.getDocumentElement();
		log.info("Document and {} are received", multipartDto);
		//If something static will be set we have to delete all the dynamic <StyleMap>'s
		if (multipartDto.getPointIconSize() != null || multipartDto.getPointTextSize() != null ||
			  multipartDto.getPointTextColor() != null) {
			processStyleMaps(documentRoot, multipartDto);
		}
		if (multipartDto.getPointIconSize() != null) {
			setPointsIconsSize(documentRoot, multipartDto);
		}
		if (multipartDto.getPointTextSize() != null) {
			setPointTextSize(documentRoot, multipartDto);
		}
		if (multipartDto.getPointTextColor() != null) {
			setPointTextColor(documentRoot, multipartDto);
		}
		return this.document;
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
	 *     <styleUrl>#styleMap1</styleUrl>
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
	}
	
	/**
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
	 * Replaces <styleUrl>#styleMap</styleUrl> of <Placemark> with <styleUrl>#style1</styleUrl>}
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
			if (styleMapNode.hasAttributes() && styleMapNode.getAttributes().getNamedItem("id") != null) {
				Node idNode = styleMapNode.getAttributes().getNamedItem("id");
				String idAttribute = idNode.getTextContent();
				if (idAttribute.equals(trimmedUrl)) return getKeyNormalStyleUrlFromStyleMap(styleMapNode);
			}
		}
		//No <StyleMap> with such an "id" attribute
		return styleUrl;
	}
	
/*
	private String getKeyNormalStyleUrlFromStyleMap(Node styleMap) {
		NodeList styleMapChildNodes = styleMap.getChildNodes();
		
		String styleUrlToNormal = "";
		
		start:
		for (int i = 0; i < styleMapChildNodes.getLength(); i++) {
			Node styleMapChildNode = styleMapChildNodes.item(i);
			
			if (styleMapChildNode.getNodeName() != null && styleMapChildNode.getNodeName().equals("Pair")) {
				NodeList pairChildNodes = styleMapChildNode.getChildNodes();
				
				for (int j = 0; j < pairChildNodes.getLength(); j++) {
					Node pairChildNode = pairChildNodes.item(j);
					if (pairChildNode.getNodeName() != null && pairChildNode.getNodeName().equals("key")) {
						//<Pair> <key>normal</key> <styleUrl>#exampleUrl</styleUrl> </Pair>
						if (pairChildNode.getTextContent().equalsIgnoreCase("normal")) {
							Node normalStyleUrl =
								getChildNodesFromParent(pairChildNode.getParentNode(), "styleUrl", null, false)
									.get(0);
							styleUrlToNormal = normalStyleUrl.getTextContent();
							break start;
						}
					}
				}
			}
		}
		return styleUrlToNormal;
	}
*/
	
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
	
	//TODO: to use XPath to select only <Style/>'s with attribute id=""
	
	/**
	 * Applies only to existing <IconStyle> tags.
	 */
	private void setPointsIconsSize(Element documentRoot, MultipartDto multipartDto) {
		log.info("Setting the icons size...");
		String scale = multipartDto.getPointIconSizeScaled().toString();
		NodeList iconStyles = documentRoot.getElementsByTagName("IconStyle");
		List<Node> scales = getChildNodesFromParents(iconStyles, "scale", false, true);
		scales.forEach(scaleNode -> scaleNode.setTextContent(scale));
		log.info("Icons size has been set.");
	}
	
	/**
	 * Either applies to existing <LabelStyle> tags or a new ones will be created for every <Style>.
	 * Because <LabelStyle> automatically displays <name> content from <Placemark>s.
	 */
	private void setPointTextSize(Element documentRoot, MultipartDto multipartDto) {
		log.info("Setting the points names size...");
		String scale = multipartDto.getPointTextSizeScaled().toString();
		NodeList styles = documentRoot.getElementsByTagName("Style");
		List<Node> scales = getLabelStylesScales(styles);
		scales.forEach(scaleNode -> scaleNode.setTextContent(scale));
		log.info("Points names size has been set.");
	}
	
	private void setPointTextColor(Element documentRoot, MultipartDto multipartDto) {
		log.info("Setting the names text color...");
		String color = getKmlColor(multipartDto.getPointTextColor(), multipartDto);
		NodeList styles = documentRoot.getElementsByTagName("Style");
		List<Node> colors = getLabelStylesColors(styles);
		colors.forEach(colorNode -> colorNode.setTextContent(color));
		log.info("Names color has been set.");
	}
	
	/**
	 * @param iconStyles {@link NodeList} from all presented <Style/>'s within xml Document.
	 * @return <scale/> of every <IconStyle/> from every <Style/>
	 */
/*
	private List<Node> getIconStylesScales(NodeList iconStyles) {
		List<Node> scales = new ArrayList<>();
		//Look through every <IconStyle>
		iconStyles:
		for (int i = 0; i < iconStyles.getLength(); i++) {
			Node iconStyle = iconStyles.item(i);
			
			NodeList iconStyleChildNodes = iconStyle.getChildNodes();
			for (int j = 0; j < iconStyleChildNodes.getLength(); j++) {
				Node iconStyleChild = iconStyleChildNodes.item(j);
				if (iconStyleChild.getNodeType() != Node.ELEMENT_NODE || iconStyleChild.getNodeName() == null) continue;
				//Scale tag is presented, just insert a new value
				if (iconStyleChild.getNodeName().equals("scale")) {
					scales.add(iconStyleChild);
					log.trace("Existing <scale> has been found in <IconStyle>");
					continue iconStyles;
				}
			}
			//Scale tag not presented, create a new one, append it to parent and add to the resulting List
			Element scale = document.createElement("scale");
			iconStyle.appendChild(scale);
			scales.add(scale);
			log.trace("The new <scale> has been added into <IconStyle>");
		}
		log.debug("All <scale>'s within <IconStyle>'s tags have been found or created");
		return scales;
	}
*/
	
	/**
	 * @param iconStyles {@link NodeList} from all presented <Style/>'s within xml Document.
	 * @return <scale/> of every <IconStyle/> from every <Style/>
	 */
/*
	private List<Node> getIconStylesScales(NodeList iconStyles) {
		return getChildNodesFromParents(iconStyles, "scale", false, true);
	}
*/
	
	/**
	 * @param styles {@link NodeList} with all presented <Style/>'s within xml Document.
	 * @return <scale/> of every <LabelStyle/> from every <Style/>
	 */
/*
	private List<Node> getLabelStylesScales(NodeList styles) {
		List<Node> scales = new ArrayList<>();
		List<Node> labelStyles = getLabelStyles(styles);
		
		labelStyles.forEach(labelStyle -> {
			NodeList labelStyleChildNodes = labelStyle.getChildNodes();
			for (int i = 0; i < labelStyleChildNodes.getLength(); i++) {
				Node labelStyleChild = labelStyleChildNodes.item(i);
				if (labelStyleChild.getNodeType() != Node.ELEMENT_NODE || labelStyleChild.getNodeName() == null)
					continue;
				if (labelStyleChild.getNodeName().equals("scale")) {
					scales.add(labelStyleChild);
					log.trace("Existing <scale> has been found in <LabelStyle>");
					return;
				}
			}
			//<scale> isn't presented
			Node scale = document.createElement("scale");
			labelStyle.appendChild(scale);
			scales.add(scale);
			log.trace("The new <scale> has been added into <LabelStyle>");
		});
		log.debug("All <scale>'s within <LabelStyle>'s tags have been found or created.");
		return scales;
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
	 * @param styles {@link NodeList} with all presented <Style/>'s within xml Document.
	 * @return <color/> of every <LabelStyle/> from every <Style/>
	 */
/*
	private List<Node> getLabelStylesColors(NodeList styles) {
		List<Node> colors = new ArrayList<>();
		List<Node> labelStyles = getLabelStyles(styles);
		
		labelStyles.forEach(labelStyle -> {
			NodeList labelStyleChildNodes = labelStyle.getChildNodes();
			for (int i = 0; i < labelStyleChildNodes.getLength(); i++) {
				Node labelStyleChild = labelStyleChildNodes.item(i);
				if (labelStyleChild.getNodeType() != Node.ELEMENT_NODE || labelStyleChild.getNodeName() == null)
					continue;
				if (labelStyleChild.getNodeName().equals("color")) {
					colors.add(labelStyleChild);
					log.trace("Existing <color> has been found in <LabelStyle>");
					return;
				}
			}
			//<color> isn't presented within <LabelStyle/>
			Node color = document.createElement("color");
			labelStyle.appendChild(color);
			colors.add(color);
			log.trace("The new <color> has been added into <LabelStyle>");
		});
		log.debug("All <color>'s within <LabelStyle>'s tags have been found or created.");
		return colors;
	}
*/
	
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
	 * Looks through every <Style/>.
	 * If <Style/> contains <LabelStyle/> it will be added into the resulting List<Node>.
	 * Otherwise <LabelStyle/> will be created, appended as a child to the existing <Style/> and added into the resulting list.
	 *
	 * @return List of <LabelStyle/> from <Style/>
	 */
/*
	private List<Node> getLabelStyles(NodeList styles) {
		List<Node> labelStyles = new ArrayList<>();
		style:
		for (int i = 0; i < styles.getLength(); i++) {
			Node style = styles.item(i);
			
			NodeList styleChildNodes = style.getChildNodes();
			for (int j = 0; j < styleChildNodes.getLength(); j++) {
				Node styleChild = styleChildNodes.item(j);
				if (styleChild.getNodeType() != Node.ELEMENT_NODE) continue;
				if (styleChild.getNodeName().equals("LabelStyle")) {
					labelStyles.add(styleChild);
					log.trace("Existing <LabelStyle> has been found.");
					continue style;
				}
			}
			//<LabelStyle> isn't presented within <Style> parent. Create a new one and append it to <Style> parent
			Node labelStyle = document.createElement("LabelStyle");
			style.appendChild(labelStyle);
			labelStyles.add(labelStyle);
			log.trace("The new <LabelStyle> has been created and added.");
		}
		log.debug("All <LabelStyle>'s within Documents have been found or created.");
		return labelStyles;
	}
*/
	
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
