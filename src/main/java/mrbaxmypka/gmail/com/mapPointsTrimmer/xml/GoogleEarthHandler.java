package mrbaxmypka.gmail.com.mapPointsTrimmer.xml;

import mrbaxmypka.gmail.com.mapPointsTrimmer.entitiesDto.MultipartDto;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class GoogleEarthHandler {
	
	private Document document;
	
	Element processXml(Document document, MultipartDto multipartDto) {
		this.document = document;
		Element documentRoot = document.getDocumentElement();
		
		if (multipartDto.getPointIconSize() != null) {
			setPointsIconsSize(documentRoot, multipartDto);
		}
		if (multipartDto.getPointTextSize() != null) {
			setPointTextSize(documentRoot, multipartDto);
		}
		
		return null;
	}
	
	private void setPointsIconsSize(Element documentRoot, MultipartDto multipartDto) {
		Float scale = multipartDto.getPointIconSize().floatValue();
		
		NodeList iconStyles = documentRoot.getElementsByTagName("IconStyle");
		
		//Look through every <IconStyle>
		for (int i = 0; i < iconStyles.getLength(); i++) {
			Node iconStyle = iconStyles.item(i);
			
			NodeList iconStyleChildNodes = iconStyle.getChildNodes();
			Node scaleNode = null;
			//Look through every Style direct children
			for (int j = 0; j < iconStyleChildNodes.getLength(); j++) {
				if (iconStyleChildNodes.item(j).getNodeType() != Node.ELEMENT_NODE) continue;
				//Scale tag is presented, just insert a new value
				if (iconStyleChildNodes.item(j).getLocalName().equals("scale")) {
					scaleNode = iconStyleChildNodes.item(j);
					scaleNode.setTextContent(scale.toString());
					break;
				}
			}
			//Scale tag not presented, we have to create and append a new one
			if (scaleNode == null) {
				scaleNode = document.createElement("scale");
				scaleNode.setTextContent(scale.toString());
				iconStyle.appendChild(scaleNode);
			}
		}
	}
	
	private void setPointTextSize(Element documentRoot, MultipartDto multipartDto) {
		Float scale = multipartDto.getPointIconSize().floatValue();
		
		NodeList styles = documentRoot.getElementsByTagName("Style");
		
		//Look through every <Style>
		for (int i = 0; i < styles.getLength(); i++) {
			Node style = styles.item(i);
			
			NodeList styleChildNodes = style.getChildNodes();
			Node labelStyle = null;
			//Look through <LabelStyle>'s
			for (int j = 0; j < styleChildNodes.getLength(); j++) {
				if (styleChildNodes.item(j).getNodeType() != Node.ELEMENT_NODE) continue;
				//LabelStyle tag is presented, just insert a new one
				if (styleChildNodes.item(j).getLocalName().equals("scale")) {
					labelStyle = styleChildNodes.item(j);
					labelStyle.setTextContent(scale.toString());
					break;
				}
			}
			//Scale tag not presented, we have to create and append a new one
			if (labelStyle == null) {
				labelStyle = document.createElement("scale");
				labelStyle.setTextContent(scale.toString());
				style.appendChild(labelStyle);
			}
		}
	}
	
}
