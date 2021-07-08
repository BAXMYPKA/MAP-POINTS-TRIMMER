package mrbaxmypka.gmail.com.mapPointsTrimmer.xml;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class KmlUtils {

    private Document document;
    private XmlDomUtils xmlDomUtils;
    /**
     * {@literal All the <Style>s and <StyleMap>s Nodes from the Document's start by their "id" attributes}
	 * key=id
	 * value={@link Node}
     */
    @Getter(AccessLevel.PACKAGE)
    private Map<String, Node> styleObjectsMap;
    @Getter(AccessLevel.PACKAGE)
    private List<String> styleUrlsFromPlacemarks;

    public KmlUtils(Document document, XmlDomUtils xmlDomUtils) {
        this.document = document;
        this.xmlDomUtils = xmlDomUtils;
        setStyleObjectsMap();
        setStyleUrlsFromPlacemarks();
    }

    /**
     * {@code Puts aLL the <Style/>'s and <StyleMap/>'s Nodes from the Document by their "id" attribute.}
     */
	void setStyleObjectsMap() {
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

	void setStyleUrlsFromPlacemarks() {
		styleUrlsFromPlacemarks =
				xmlDomUtils.getChildNodesFromParents(document.getElementsByTagName("Placemark"), "styleUrl", false, false
						, false)
						.stream()
						.map(styleUrlNode -> styleUrlNode.getTextContent().substring(1))
						.collect(Collectors.toList());
		log.trace("The List<String> of StyleUrls from Placemarks has been set with size={}", styleUrlsFromPlacemarks.size());
	}

	/**
	 * @param id                A special id or a full name of the pictogram
	 * @param pictogramFullName {@link Nullable} If a previous 'id' parameter is not represented a full name, this has to be used
	 * @return A {@link Node} NOT inserted into the {@link Document}. Use {@link #insertIntoDocument(Node)} for this
	 */
	Node createNotInsertedStyleNode(String id, @Nullable String pictogramFullName) {
		Element styleNode = document.createElement("Style");
		styleNode.setAttribute("id", "styleOf" + id);
		Element iconStyleNode = document.createElement("IconStyle");
		Element iconNode = document.createElement("Icon");
		Element hrefNode = document.createElement("href");
		hrefNode.setTextContent(pictogramFullName != null ? pictogramFullName : "files/" + id);

		iconNode.appendChild(hrefNode);
		iconStyleNode.appendChild(iconNode);
		styleNode.appendChild(iconStyleNode);

		return styleNode;
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
	 * @return A {@link Node} inserted into the {@link Document}.
	 */
	Node createInsertedStyleMapNode(Node styleNode) {
		String idAttribute = "styleMapOf:" + styleNode.getAttributes().getNamedItem("id").getTextContent();

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
		styleHighlightNode.setAttribute("id", "highlightOf:" + styleNode.getAttributes().getNamedItem("id").getTextContent());
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

}
