package mrbaxmypka.gmail.com.mapPointsTrimmer.xml;

import lombok.extern.slf4j.Slf4j;
import mrbaxmypka.gmail.com.mapPointsTrimmer.entitiesDto.MultipartDto;
import mrbaxmypka.gmail.com.mapPointsTrimmer.services.FileService;
import mrbaxmypka.gmail.com.mapPointsTrimmer.services.GoogleIconsService;
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
@Slf4j
@Component
public class KmlHandler extends XmlHandler {
	
	private Document document;
	
	public KmlHandler(HtmlHandler htmlHandler, GoogleIconsService googleIconsService, FileService fileService, LocusMapHandler locusMapHandler) {
		super(htmlHandler, googleIconsService, fileService, locusMapHandler);
	}
	
	/**
	 * All the additional information for a User (preview size, outdated descriptions etc) are placed inside the
	 * CDATA[[]] as an HTML markup.
	 * So the main goal for this method is the extracting CDATA and pass it to the HTML parser.
	 */
	public String processXml(InputStream kmlInputStream, MultipartDto multipartDto)
			throws IOException, ParserConfigurationException, SAXException, TransformerException {
		log.info("The given KML is being processed...");
		document = getDocument(kmlInputStream);
		Element documentRoot = document.getDocumentElement();
		//Processing Google Earth specific options
		GoogleEarthHandler googleEarthHandler = new GoogleEarthHandler();
		googleEarthHandler.processXml(document, multipartDto);

		getLocusMapHandler().processXml(document, multipartDto);

		log.info("Setting the new path to images...");
		processHref(documentRoot, multipartDto);

		log.info("Descriptions from KML are being processed...");
		//Processing the further text options regarding to inner CDATA or plain text from <description>s
		processDescriptionsTexts(documentRoot, multipartDto);
		
/*
		if (multipartDto.isAsAttachmentInLocus()) {
			log.info("Images are being attached for Locus...");
			processLocusAttachments(documentRoot, multipartDto);
		}
*/
		if (multipartDto.isTrimXml()) {
			log.info("KML is being trimmed...");
			trimWhitespaces(documentRoot);
		}
		log.info("The KML has been processed");
		return writeTransformedDocument(document, !multipartDto.isTrimXml());
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
			String currentIconHrefWithFilename = node.getTextContent();
			String processedIconHrefWithFilename = getGoogleIconsService().processIconHref(currentIconHrefWithFilename, multipartDto);
			if (multipartDto.getPath() != null) {
				//Full processing with a new href to image
				String newHrefWithOldFilename = getHtmlHandler().getNewHrefWithOldFilename(
						processedIconHrefWithFilename, multipartDto.getPathType(), multipartDto.getPath());
				node.setTextContent(newHrefWithOldFilename);
				log.trace("<href> text has been replaced with '{}'", newHrefWithOldFilename);
			} else if (!currentIconHrefWithFilename.equals(processedIconHrefWithFilename)) {
				//User hasn't defined a custom path to images
				//Existing href was a GoogleMaps special url and has been downloaded locally
				//It has to be zipped into the default folder
				String newHrefWithOldFilename = getHtmlHandler().getNewHrefWithOldFilename(
						processedIconHrefWithFilename, PathTypes.RELATIVE, "files/");
				node.setTextContent(newHrefWithOldFilename);
				log.trace("<href> text has been replaced with '{}'", newHrefWithOldFilename);
			}
		}
		log.info("All <href>'s within the given Document have been replaced with the new ones.");
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
				log.trace("Description has been set as blank string");
			} else {
				//Obtain an inner CDATA text to treat as HTML elements or plain text
				String processedHtmlCdata = getHtmlHandler().processDescriptionText(textContent, multipartDto);
				processedHtmlCdata = prettyPrintCdataXml(processedHtmlCdata, multipartDto);
				CDATASection cdataSection = document.createCDATASection(processedHtmlCdata);
				descriptionNode.setTextContent("");
				descriptionNode.appendChild(cdataSection);
				log.trace("Description has been processed and set");
			}
		}
		log.info("All the <description> have been processed.");
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
/*
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
				if (placemarkChild.getNodeType() != Node.ELEMENT_NODE || placemarkChild.getLocalName() == null)
					continue;
				if (placemarkChild.getLocalName().equals("description")) {
					descriptionText = placemarkChild.getTextContent();
					imgSrcFromDescription = getHtmlHandler().getAllImagesFromDescription(descriptionText);
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
		log.info("All <attachment>'s for Locus has been processed and added.");
	}
*/

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
/*
	private void processImagesFromDescription(
			List<String> imgSrcFromDescription, List<Node> attachmentNodes, Node placemark, MultipartDto multipartDto) {
		log.trace("'{}' images for '{}' attachments are being processed", imgSrcFromDescription.size(), attachmentNodes.size());
		//No images from description to insert as attachments
		if (imgSrcFromDescription.isEmpty()) {
			return;
			//WEB paths are not supported as attachments
		} else if (multipartDto.getPathType() != null && multipartDto.getPathType().equals(PathTypes.WEB)) {
			log.trace("Web type isn't supported for Locus <attachment>");
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
						String attachmentFilename = getFileService().getFileName(attachment.getTextContent());
						//If <attachment> has same name as any img from description, this attachment is being set same
						// src="" from src List
						Iterator<String> iterator = locusAttachmentsHref.iterator();
						while (iterator.hasNext()) {
							String imgSrc = iterator.next();
							if (getFileService().getFileName(imgSrc).equals(attachmentFilename)) {
								
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
				log.trace("A new <attachment>'s have been added for the images from the <description>");
			}
			//<ExtendedData> isn't presented within the <Placemark>
		} else {
			//Create a new <ExtendedData> parent with new <lc:attachment> children from images src from description
			List<Element> imagesSrcAsLcAttachments = getImagesSrcAsLcAttachments(locusAttachmentsHref);
			Node newExtendedData = getNewExtendedData(imagesSrcAsLcAttachments);
			placemark.appendChild(newExtendedData);
			log.trace("A new <ExtendedData> with <attachment>'s has been added for the images from the <description>");
		}
	}
*/

/*
	private List<String> getLocusSpecificAttachmentsHref(List<String> imagesToAttach, MultipartDto multipartDto) {
		imagesToAttach = imagesToAttach.stream()
				.map(imgSrc -> {
					//Locus lc:attachments accepts only RELATIVE type of path
					//So remake path to Locus specific absolute path without "file:///"
					return getHtmlHandler().getLocusAttachmentAbsoluteHref(imgSrc, multipartDto);
				})
				.filter(imgSrc -> !imgSrc.isBlank())
				.collect(Collectors.toList());
		log.trace("{} processed 'src' for Locus <attachment> will be returned", imagesToAttach.size());
		return imagesToAttach;
	}
*/

	/**
	 * @param imagesToAttach Src to images from User description
	 * @return {@link XMLEvent#CHARACTERS} as <lc:attachment>src/to/image.img</lc:attachment>
	 * to be added to <ExtendedData></ExtendedData> to Locus Map xml.
	 */
/*
	private List<Element> getImagesSrcAsLcAttachments(List<String> imagesToAttach) {
		List<Element> lcAttachments = new ArrayList<>();
		imagesToAttach.forEach(img -> {
			Element attachment = document.createElementNS("http://www.locusmap.eu", "lc:attachment");
			attachment.setTextContent(img);
			lcAttachments.add(attachment);
		});
		log.trace("{} <attachment>'s with the processed 'src' for images will be returned", lcAttachments.size());
		return lcAttachments;
	}
*/

	/**
	 * @param elementsToBeInside All {@link XMLEvent}'s to be written inside <ExtendedData></ExtendedData> with
	 *                           Locus namespace.
	 * @return {@link LinkedList<XMLEvent>} with XMLEvents inside to be written into existing document as:
	 * <ExtendedData xmlns:lc="http://www.locusmap.eu">... xmlEventsToBeInside ...</ExtendedData>
	 */
/*
	private Node getNewExtendedData(List<Element> elementsToBeInside) {
		Element extendedData = document.createElement("ExtendedData");
//		Element extendedData = document.createElementNS("http://www.locusmap.eu", "ExtendedData");
		elementsToBeInside.forEach(extendedData::appendChild);
		log.trace("A new <ExtendedData> will be returned");
		return extendedData;
	}
*/

	/**
	 * Just for pretty printing and mark out CDATA descriptions.
	 * If the whole XML document is inline except CDATA so as to emphasize that CDATA among XML this method add start
	 * and end lineBreaks.
	 */
	private String prettyPrintCdataXml(String processedHtmlCdata, MultipartDto multipartDto) {
		if (multipartDto.isTrimXml() && !multipartDto.isTrimDescriptions()) {
			processedHtmlCdata = "\n" + processedHtmlCdata.concat("\n");
		}
		log.trace("CDATA from the <description> will be return as the prettyPrinted one");
		return processedHtmlCdata;
	}
}