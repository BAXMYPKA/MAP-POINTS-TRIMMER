package mrbaxmypka.gmail.com.mapPointsTrimmer.xml;

import mrbaxmypka.gmail.com.mapPointsTrimmer.entitiesDto.MultipartDto;
import mrbaxmypka.gmail.com.mapPointsTrimmer.utils.PathTypes;
import org.springframework.stereotype.Component;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Kml processing class based on the StAX xml-library.
 */
@Component
public class KmlHandler extends XmlHandler {
	
	private Document document;
	
	public KmlHandler(HtmlHandler htmlHandler, GoogleEarthHandler googleEarthHandler) {
		super(htmlHandler, googleEarthHandler);
	}
	
	/**
	 * All the additional information for a User (preview size, outdated descriptions etc) are placed inside the
	 * CDATA[[]] as an HTML markup.
	 * So the main goal for this method is the extracting CDATA and pass it to the HTML parser.
	 */
	public String processXml(InputStream kmlInputStream, MultipartDto multipartDto)
		throws IOException, ParserConfigurationException, SAXException, TransformerException {
		
		document = getDocument(kmlInputStream);
		Element documentRoot = document.getDocumentElement();
		//Processing Google Earth specific options
		GoogleEarthHandler googleEarthHandler = new GoogleEarthHandler();
		googleEarthHandler.processXml(document, multipartDto);
		
		if (multipartDto.getPath() != null) {
			processHref(documentRoot, multipartDto);
		}
		//Processing the further text options regarding to inner CDATA or plain text from <description>s
		processDescriptionsTexts(documentRoot, multipartDto);
		
		if (multipartDto.isAsAttachmentInLocus()) {
			processLocusAttachments(documentRoot, multipartDto);
		}
		if (multipartDto.isTrimXml()) {
			trimWhitespaces(documentRoot);
		}
		
		return writeTransformedDocument(document);
	}
	
	private void trimWhitespaces(Node node) {
		NodeList childNodes = node.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node childNode = childNodes.item(i);
			if (childNode.getNodeType() == Node.TEXT_NODE) {
				childNode.setTextContent(childNode.getTextContent().trim());
			}
			trimWhitespaces(childNode);
		}
	}
	
	/**
	 * Every old href tag contains path to file and a filename. So here we derive an existing filename
	 * and append it to the new path.
	 *
	 * @param documentRoot RootElement with all the child Nodes from Document
	 */
	private void processHref(Element documentRoot, MultipartDto multipartDto) {
		NodeList hrefs = documentRoot.getElementsByTagName("href");
		for (int i = 0; i < hrefs.getLength(); i++) {
			Node node = hrefs.item(i);
			String oldHrefWithFilename = node.getTextContent();
			if (googleEarthHandler.isImageHrefChangeable(oldHrefWithFilename)) {
				String newHrefWithOldFilename = getHtmlHandler().getNewHrefWithOldFilename(
					oldHrefWithFilename, multipartDto.getPathType(), multipartDto.getPath());
				node.setTextContent(newHrefWithOldFilename);
			}
		}
	}
	
	/**
	 * The first temporary condition checks {@code '\\s*>\\s*'} regexp as Locus may spread those signs occasionally
	 * (especially after {@code <ExtendedData> tag}). So
	 */
	private void processDescriptionsTexts(Element documentRoot, MultipartDto multipartDto) {
		NodeList descriptions = documentRoot.getElementsByTagName("description");
		for (int i = 0; i < descriptions.getLength(); i++) {
			
			Node descriptionNode = descriptions.item(i);
			String textContent = descriptions.item(i).getTextContent();
			
			if (textContent == null || textContent.isBlank()) {
				descriptionNode.setTextContent("");
			} else {
				//Obtain an inner CDATA text to treat as HTML elements or plain text
				String processedHtmlCdata = htmlHandler.processDescriptionText(textContent, multipartDto);
				processedHtmlCdata = prettyPrintCdataXml(processedHtmlCdata, multipartDto);
				CDATASection cdataSection = document.createCDATASection(processedHtmlCdata);
				descriptionNode.setTextContent("");
				descriptionNode.appendChild(cdataSection);
			}
		}
	}
	
	/**
	 * 1) Compares the given List of src to images from user description
	 * and an existing <lc:attacmhent></lc:attacmhent> from <ExtendedData></ExtendedData> of Locus xml.
	 * 2) Overwrites src in attachments if they have the same filenames as those from User description
	 * 3) If description contains more src to images than the existing <ExtendedData></ExtendedData> has,
	 * it add additional <lc:attacmhent></lc:attacmhent> elements to the <ExtendedData></ExtendedData> parent.
	 *
	 * @return A new {@link LinkedList<XMLEvent>} with modified or new <lc:attachments></lc:attachments>.
	 * Or the old unmodified List if no changes were done.</ExtendedData>
	 */
	private void processLocusAttachments(Element documentRoot, MultipartDto multipartDto) {
		NodeList placemarks = documentRoot.getElementsByTagName("Placemark");
		//Iterate though every <Placemark>
		for (int i = 0; i < placemarks.getLength(); i++) {
			Node placemark = placemarks.item(i);
			NodeList placemarksChildNodes = placemark.getChildNodes();
			
			String descriptionText = "";//If any as a plain text
			List<String> imgSrcFromDescription = new LinkedList<>();//Existing img files in <description>
			List<Node> attachments = new LinkedList<>();//Existing <attachment> nodes in <ExtendedData>
			
			//Derive <description> and <ExtendedData> from Placemark
			for (int j = 0; j < placemarksChildNodes.getLength(); j++) {
				Node placemarkChild = placemarksChildNodes.item(j);
				if (placemarkChild.getNodeType() != Node.ELEMENT_NODE || placemarkChild.getLocalName() == null) continue;
				if (placemarkChild.getLocalName().equals("description")) {
					descriptionText = placemarkChild.getTextContent();
					imgSrcFromDescription = htmlHandler.getAllImagesFromDescription(descriptionText);
				} else if (placemarkChild.getLocalName().equals("ExtendedData")) {
					
					//<ExtendedData> is the parent for every <attachment>
					NodeList extendedDataChildNodes = placemarksChildNodes.item(j).getChildNodes();
					for (int k = 0; k < extendedDataChildNodes.getLength(); k++) {
						Node extendedDataChild = extendedDataChildNodes.item(k);
						if (extendedDataChild.getNodeType() != Node.ELEMENT_NODE ||
							extendedDataChild.getLocalName() == null) continue;
						if (extendedDataChild.getLocalName().equals("attachment")) {
							attachments.add(extendedDataChild);
						}
					}
				}
			}
			processImagesFromDescription(imgSrcFromDescription, attachments, placemark, multipartDto);
		}
		
	}
	
	/**
	 * 1) Compares the given List of images src from user description to the existing <lc:attacmhent></lc:attacmhent>
	 * from <ExtendedData></ExtendedData> from Locus xml.
	 * 1.1) If no <lc:attachment> presented, it will create a new <ExtendedData> and all the existing images src will
	 * be appended to if as the new <lc:attachment>.
	 * 2) Overwrites attachments data with data from srs if they have the same filenames as those from User description
	 * 2.1) Deletes those src from images list
	 * 3) If description contains more images src than the existing <lc:attachments>s,
	 * it will add additional <lc:attacmhent> elements to the <ExtendedData> parent.
	 *
	 * @param imgSrcFromDescription A List of src to images from User Description
	 * @param attachmentNodes       (Linked)List of <attachment></attachment> {@link Node} within <ExtendedData></ExtendedData>
	 *                              parent, e.g.:
	 *                              <ExtendedData>
	 *                              <lc:attachment></lc:attachment>
	 *                              <lc:attachment></lc:attachment>
	 *                              </ExtendedData>
	 *                              to operate on (replace, remove, add).
	 *                              In the end all those ExtendedData will be written in the end of Placemark tag, e.g.
	 *                              <Placemark>
	 *                              <>...</>
	 *                              <ExtendedData>...</ExtendedData>
	 *                              </Placemark>
	 * @param placemark             A Parent {@link Node} for adding a new <ExtendedData></ExtendedData> if the last one is not
	 *                              presented.
	 */
	private void processImagesFromDescription(
		List<String> imgSrcFromDescription, List<Node> attachmentNodes, Node placemark, MultipartDto multipartDto) {
		//No images from description to insert as attachments
		if (imgSrcFromDescription.isEmpty()) {
			return;
			//WEB paths are not supported as attachments
		} else if (multipartDto.getPathType() != null && multipartDto.getPathType().equals(PathTypes.WEB)) {
			return;
		}
		//Turn all imgSrc into Locus specific paths
		final List<String> locusAttachmentsHref = getLocusSpecificAttachmentsHref(imgSrcFromDescription, multipartDto);
		
		//Iterate through existing <ExtendedData> elements
		if (!attachmentNodes.isEmpty()) {
			//addLcPrefixForLocusmapNamespace(attachmentNodes);
			attachmentNodes = attachmentNodes.stream()
				.filter(attachment -> attachment.getPrefix().equals("lc"))
				.peek(attachment -> {
					String attachmentFilename = htmlHandler.getFileName(attachment.getTextContent());
					//If <attachment> has same name as any img from description, this attachment is being set same
					// src="" from src List
					Iterator<String> iterator = locusAttachmentsHref.iterator();
					while (iterator.hasNext()) {
						String imgSrc = iterator.next();
						if (htmlHandler.getFileName(imgSrc).equals(attachmentFilename)) {
							
							attachment.setTextContent(imgSrc);
							//And that src is removing to determine if description has more src than <ExtendedData>
							//has <lc:attachment>'s
							iterator.remove();
							break;
						}
					}
				})
				.collect(Collectors.toList());
			//If not all the images from Description are attached we create and add new <lc:attachment>'s
			if (!locusAttachmentsHref.isEmpty()) {
				List<Element> newAttachments = getImagesSrcAsLcAttachments(locusAttachmentsHref);
				Node parentExtendedData = attachmentNodes.get(0).getParentNode();
				newAttachments.forEach(parentExtendedData::appendChild);
			}
			//<ExtendedData> isn't presented within the <Placemark>
		} else {
			//Create a new <ExtendedData> parent with new <lc:attachment> children from images src from description
			List<Element> imagesSrcAsLcAttachments = getImagesSrcAsLcAttachments(locusAttachmentsHref);
			Node newExtendedData = getNewExtendedData(imagesSrcAsLcAttachments);
			placemark.appendChild(newExtendedData);
		}
	}
	
	private List<String> getLocusSpecificAttachmentsHref(List<String> imagesToAttach, MultipartDto multipartDto) {
		imagesToAttach = imagesToAttach.stream()
			.map(imgSrc -> {
				//Locus lc:attachments accepts only RELATIVE type of path
				//So remake path to Locus specific absolute path without "file:///"
				return getHtmlHandler().getLocusAttachmentAbsoluteHref(imgSrc, multipartDto);
			})
			.filter(imgSrc -> !imgSrc.isBlank())
			.collect(Collectors.toList());
		
		return imagesToAttach;
	}
	
	/**
	 * @param imagesToAttach Src to images from User description
	 * @return {@link XMLEvent#CHARACTERS} as <lc:attachment>src/to/image.img</lc:attachment>
	 * to be added to <ExtendedData></ExtendedData> to Locus Map xml.
	 */
	private List<Element> getImagesSrcAsLcAttachments(List<String> imagesToAttach) {
		List<Element> lcAttachments = new ArrayList<>();
		imagesToAttach.forEach(img -> {
			Element attachment = document.createElementNS("http://www.locusmap.eu", "lc:attachment");
			attachment.setTextContent(img);
			lcAttachments.add(attachment);
		});
		return lcAttachments;
	}
	
	/**
	 * @param elementsToBeInside All {@link XMLEvent}'s to be written inside <ExtendedData></ExtendedData> with
	 *                           Locus namespace.
	 * @return {@link LinkedList<XMLEvent>} with XMLEvents inside to be written into existing document as:
	 * <ExtendedData xmlns:lc="http://www.locusmap.eu">... xmlEventsToBeInside ...</ExtendedData>
	 */
	private Node getNewExtendedData(List<Element> elementsToBeInside) {
		Element extendedData = document.createElement("ExtendedData");
//		Element extendedData = document.createElementNS("http://www.locusmap.eu", "ExtendedData");
		elementsToBeInside.forEach(extendedData::appendChild);
		return extendedData;
	}
	
	/**
	 * Just for pretty printing and mark out CDATA descriptions.
	 * If the whole XML document is inline except CDATA so as to emphasize that CDATA among XML this method add start
	 * and end lineBreaks.
	 */
	private String prettyPrintCdataXml(String processedHtmlCdata, MultipartDto multipartDto) {
		if (multipartDto.isTrimXml() && !multipartDto.isTrimDescriptions()) {
			processedHtmlCdata = "\n" + processedHtmlCdata.concat("\n");
		}
		return processedHtmlCdata;
	}
}