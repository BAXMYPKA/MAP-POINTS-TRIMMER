package mrbaxmypka.gmail.com.mapPointsTrimmer.xml;

import mrbaxmypka.gmail.com.mapPointsTrimmer.entitiesDto.MultipartDto;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

public class GoogleEarthHandler {
	
	private Document document;
	
	Document processXml(Document document, MultipartDto multipartDto) {
		this.document = document;
		Element documentRoot = document.getDocumentElement();
		
		if (multipartDto.getPointIconSize() != null) {
			setPointsIconsSize(documentRoot, multipartDto);
		}
		if (multipartDto.getPointTextSize() != null) {
			setPointTextSize(documentRoot, multipartDto);
		}
		return this.document;
	}
	
	private void setPointsIconsSize(Element documentRoot, MultipartDto multipartDto) {
		Float scale = multipartDto.getPointIconSize().floatValue();
		NodeList iconStyles = documentRoot.getElementsByTagName("IconStyle");
		List<Node> scales = getIconStylesScales(iconStyles);
		scales.forEach(scaleNode -> scaleNode.setTextContent(scale.toString()));
	}
	
	private void setPointTextSize(Element documentRoot, MultipartDto multipartDto) {
		Float scale = multipartDto.getPointIconSize().floatValue();
		NodeList styles = documentRoot.getElementsByTagName("Style");
		List<Node> scales = getLabelStylesScales(styles);
		scales.forEach(scaleNode -> scaleNode.setTextContent(scale.toString()));
	}
	
	private List<Node> getIconStylesScales(NodeList iconStyles) {
		List<Node> scales = new ArrayList<>();
		//Look through every <IconStyle>
		for (int i = 0; i < iconStyles.getLength(); i++) {
			Node iconStyle = iconStyles.item(i);
			
			NodeList iconStyleChildNodes = iconStyle.getChildNodes();
			Node scale = null;
			//Look through every Style direct children
			for (int j = 0; j < iconStyleChildNodes.getLength(); j++) {
				if (iconStyleChildNodes.item(j).getNodeType() != Node.ELEMENT_NODE) continue;
				//Scale tag is presented, just insert a new value
				if (iconStyleChildNodes.item(j).getLocalName().equals("scale")) {
					scale = iconStyleChildNodes.item(j);
					scales.add(scale);
					break;
				}
			}
			//Scale tag not presented, we have to create and append a new one
			if (scale == null) {
				Element newScale = document.createElement("scale");
				iconStyle.appendChild(newScale);
				scales.add(newScale);
			}
		}
		return scales;
	}
	
	private List<Node> getLabelStylesScales(NodeList styles) {
		List<Node> scales = new ArrayList<>();
		//Look through every <Style>
		for (int i = 0; i < styles.getLength(); i++) {
			Node style = styles.item(i);
			
			Node labelStyle = null;
			Node scale = null;
			NodeList styleChildNodes = style.getChildNodes();
			
			//Look through <Style> children
			for (int j = 0; j < styleChildNodes.getLength(); j++) {
				if (styleChildNodes.item(j).getNodeType() != Node.ELEMENT_NODE) continue;
				if (styleChildNodes.item(j).getLocalName().equals("LabelStyle")) {
					labelStyle = styleChildNodes.item(j);
					
					NodeList labelStyleChildNodes = labelStyle.getChildNodes();
					//Look through <LabelStyle> children
					for (int k = 0; k < labelStyleChildNodes.getLength(); k++) {
						if (labelStyleChildNodes.item(k).getNodeType() != Node.ELEMENT_NODE) continue;
						//Look through for <LabelStyle> -> <scale>
						if (labelStyleChildNodes.item(k).getLocalName().equals("scale")) {
							scale = labelStyleChildNodes.item(k);
							//<scale> is presented within parent's <LabelStyle> and just has to be added
							scales.add(scale);
							break;
						}
					}
					//<scale> isn't presented, create a new one
					if (scale == null) {
						scale = document.createElement("scale");
						labelStyle.appendChild(scale);
						scales.add(scale);
					}
				}
			}
			//<LabelStyle> isn't presented within <Style> parent. Create a new one, append a new <scale> to it
			// and append all them to <Style> parent before returning
			if (labelStyle == null) {
				labelStyle = document.createElement("LabelStyle");
				scale = document.createElement("scale");
				labelStyle.appendChild(scale);
				style.appendChild(labelStyle);
				scales.add(scale);
			}
		}
		return scales;
	}
}
