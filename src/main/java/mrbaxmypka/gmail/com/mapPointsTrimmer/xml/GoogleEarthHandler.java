package mrbaxmypka.gmail.com.mapPointsTrimmer.xml;

import lombok.NoArgsConstructor;
import mrbaxmypka.gmail.com.mapPointsTrimmer.entitiesDto.MultipartDto;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Component
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
		if (multipartDto.getPointTextColor() != null) {
			setPointTextColor(documentRoot, multipartDto);
		}
		return this.document;
	}
	
	//TODO: to use XPath to select only <Style/>'s with attribute id=""
	
	/**
	 * Applies only to existing <IconStyle> tags.
	 */
	private void setPointsIconsSize(Element documentRoot, MultipartDto multipartDto) {
		String scale = multipartDto.getPointIconSizeScaled().toString();
		NodeList iconStyles = documentRoot.getElementsByTagName("IconStyle");
		List<Node> scales = getIconStylesScales(iconStyles);
		scales.forEach(scaleNode -> scaleNode.setTextContent(scale));
	}
	
	/**
	 * Either applies to existing <LabelStyle> tags or a new ones will be created for every <Style>.
	 * Because <LabelStyle> automatically displays <name> content from <Placemark>s.
	 */
	private void setPointTextSize(Element documentRoot, MultipartDto multipartDto) {
		String scale = multipartDto.getPointTextSizeScaled().toString();
		NodeList styles = documentRoot.getElementsByTagName("Style");
		List<Node> scales = getLabelStylesScales(styles);
		scales.forEach(scaleNode -> scaleNode.setTextContent(scale));
	}
	
	private void setPointTextColor(Element documentRoot, MultipartDto multipartDto) {
		String color = getKmlColor(multipartDto.getPointTextColor(), multipartDto);
		NodeList styles = documentRoot.getElementsByTagName("Style");
		List<Node> colors = getLabelStylesColors(styles);
		colors.forEach(colorNode -> colorNode.setTextContent(color));
	}
	
	/**
	 * @param iconStyles {@link NodeList} from all presented <Style/>'s within xml Document.
	 * @return <scale/> of every <IconStyle/> from every <Style/>
	 */
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
					continue iconStyles;
				}
			}
			//Scale tag not presented, create a new one, append it to parent and add to the resulting List
			Element scale = document.createElement("scale");
			iconStyle.appendChild(scale);
			scales.add(scale);
		}
		return scales;
	}
	
	/**
	 * @param styles {@link NodeList} with all presented <Style/>'s within xml Document.
	 * @return <scale/> of every <LabelStyle/> from every <Style/>
	 */
	private List<Node> getLabelStylesScales(NodeList styles) {
		List<Node> scales = new ArrayList<>();
		List<Node> labelStyles = getLabelStyles(styles);
		
		labelStyles.forEach(labelStyle -> {
			NodeList labelStyleChildNodes = labelStyle.getChildNodes();
			for (int i = 0; i < labelStyleChildNodes.getLength(); i++) {
				Node labelStyleChild = labelStyleChildNodes.item(i);
				if (labelStyleChild.getNodeType() != Node.ELEMENT_NODE || labelStyleChild.getNodeName() == null) continue;
				if (labelStyleChild.getNodeName().equals("scale")) {
					scales.add(labelStyleChild);
					return;
				}
			}
			//<scale> isn't presented
			Node scale = document.createElement("scale");
			labelStyle.appendChild(scale);
			scales.add(scale);
		});
		
		return scales;
	}
	
	/**
	 * @param styles {@link NodeList} with all presented <Style/>'s within xml Document.
	 * @return <color/> of every <LabelStyle/> from every <Style/>
	 */
	private List<Node> getLabelStylesColors(NodeList styles) {
		List<Node> colors = new ArrayList<>();
		List<Node> labelStyles = getLabelStyles(styles);
		
		labelStyles.forEach(labelStyle -> {
			NodeList labelStyleChildNodes = labelStyle.getChildNodes();
			for (int i = 0; i < labelStyleChildNodes.getLength(); i++) {
				Node labelStyleChild = labelStyleChildNodes.item(i);
				if (labelStyleChild.getNodeType() != Node.ELEMENT_NODE || labelStyleChild.getNodeName() == null) continue;
				if (labelStyleChild.getNodeName().equals("color")) {
					colors.add(labelStyleChild);
					return;
				}
			}
			//<color> isn't presented within <LabelStyle/>
			Node color = document.createElement("color");
			labelStyle.appendChild(color);
			colors.add(color);
		});
		
		return colors;
	}
	
	/**
	 * Looks through every <Style/>.
	 * If <Style/> contains <LabelStyle/> it will be added into the resulting List<Node>.
	 * Otherwise <LabelStyle/> will be created, appended as a child to the existing <Style/> and added into the resulting list.
	 *
	 * @return List of <LabelStyle/> from <Style/>
	 */
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
					continue style;
				}
			}
			//<LabelStyle> isn't presented within <Style> parent. Create a new one and append it to <Style> parent
			Node labelStyle = document.createElement("LabelStyle");
			style.appendChild(labelStyle);
			labelStyles.add(labelStyle);
		}
		return labelStyles;
	}
	
	/**
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
		if (!hexColor.matches("^#([0-9a-f]{3}|[0-9a-f]{6})$")) {
			throw new IllegalArgumentException(
				"Color value is not correct! (It has to correspond to '#rrggbb' hex pattern");
		}
		String kmlColor = hexColor.substring(5, 7) + hexColor.substring(3, 5) + hexColor.substring(1, 3);
		
		if (multipartDto.getPointTextOpacity() != null) {
			String opacity = getHexFromPercentage(multipartDto.getPointTextOpacity());
			return opacity + kmlColor;
		} else {
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
		if (percentage < 0 || percentage > 100) {
			throw new IllegalArgumentException("Percentage has to be from 0 to 100%!");
		}
		BigDecimal hexPercentage = new BigDecimal(percentage * 2.55).setScale(0, RoundingMode.HALF_UP);
		return String.format("%02x", hexPercentage.toBigInteger());
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
		String googleMapSpecialUrl = "http://maps.google.com/";
		return !href.startsWith(googleMapSpecialUrl);
	}
	
}
