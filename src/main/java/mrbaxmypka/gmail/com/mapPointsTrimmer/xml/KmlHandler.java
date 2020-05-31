package mrbaxmypka.gmail.com.mapPointsTrimmer.xml;

import mrbaxmypka.gmail.com.mapPointsTrimmer.entitiesDto.MultipartDto;
import mrbaxmypka.gmail.com.mapPointsTrimmer.utils.PathTypes;
import org.springframework.stereotype.Component;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.*;
import javax.xml.stream.events.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Kml processing class based on the StAX xml-library.
 */
@Component
public class KmlHandler extends Xml {
	
	private XMLEventFactory eventFactory;
	private XMLEventWriter eventWriter;
	private Document document;
	
	public KmlHandler(HtmlHandler htmlHandler) {
		super(htmlHandler);
	}
	
	
	/**
	 * All the additional information for a User (preview size, outdated descriptions etc) are placed inside the
	 * CDATA[[]] as an HTML markup.
	 * So the main goal for this method is the extracting CDATA and pass it to the HTML parser.
	 */
	public String processXml(MultipartDto multipartDto)
		throws IOException, ParserConfigurationException, SAXException, TransformerException {
		//To skip the whole processing if nothing is set
		if (!multipartDto.isSetPath() &&
			!multipartDto.isTrimDescriptions() &&
			!multipartDto.isSetPreviewSize() &&
			!multipartDto.isClearOutdatedDescriptions() &&
			!multipartDto.isTrimXml() &&
			!multipartDto.isAsAttachmentInLocus()) {
			return new String(multipartDto.getMultipartFile().getBytes());
		}
		document = getDocument(multipartDto.getMultipartFile().getInputStream());
		Element documentRoot = document.getDocumentElement();
		
		if (multipartDto.isSetPath()) {
			processHref_2(documentRoot, multipartDto);
		}
		processDescriptionsText_2(documentRoot, multipartDto);
		
		if (multipartDto.isAsAttachmentInLocus()) {
			processLocusAttachments_2(documentRoot, multipartDto);
		}
		if (multipartDto.isTrimXml()) {
			trimWhitespaces(documentRoot);
		}
		return writeTransformedDocument(document);
	}
	
/*
	private String writeTransformedDocument(Document document, MultipartDto multipartDto) throws TransformerException {
		if (multipartDto.isTrimXml()) {
			Element documentElement = document.getDocumentElement();
			trimWhitespaces(documentElement);
		}
		
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		DOMSource domSource = new DOMSource(document);
		Transformer transformer = transformerFactory.newTransformer();
		StringWriter stringWriter = new StringWriter();
		Result result = new StreamResult(stringWriter);
		transformer.transform(domSource, result);
		return stringWriter.toString();
	}
*/
	
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
	private void processHref_2(Element documentRoot, MultipartDto multipartDto) {
		NodeList hrefs = documentRoot.getElementsByTagName("href");
		for (int i = 0; i < hrefs.getLength(); i++) {
			Node node = hrefs.item(i);
			String oldHrefWithFilename = node.getTextContent();
			if (isChangeable_2(oldHrefWithFilename)) {
				String newHrefWithOldFilename = getHtmlHandler().getNewHrefWithOldFilename(
					oldHrefWithFilename, multipartDto.getPathType(), multipartDto.getPath());
				node.setTextContent(newHrefWithOldFilename);
			}
		}
	}
	
	//TODO: to fix ">" after ExtendedData .matches("\\s*>\\s*")
	
	/**
	 * The first temporary condition checks {@code '\\s*>\\s*'} regexp as Locus may spread those signs occasionally
	 * (especially after {@code <ExtendedData> tag}). So
	 */
	private void processDescriptionsText_2(Element documentRoot, MultipartDto multipartDto) {
		NodeList descriptions = documentRoot.getElementsByTagName("description");
		for (int i = 0; i < descriptions.getLength(); i++) {
			
			Node descriptionNode = descriptions.item(i);
			String textContent = descriptions.item(i).getTextContent();
			
			if (textContent == null || textContent.isBlank()) {
				descriptionNode.setTextContent("");
			} else {
				//Obtain an inner CDATA text to treat as HTML elements or plain text
				String processedHtmlCdata = htmlHandler.processDescriptionText(textContent, multipartDto);
//				processedHtmlCdata = prettyPrintCdataXml(processedHtmlCdata, multipartDto);
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
	private void processLocusAttachments_2(Element documentRoot, MultipartDto multipartDto) {
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
				if (placemarksChildNodes.item(j).getNodeType() != Node.ELEMENT_NODE) continue;
				if (placemarksChildNodes.item(j).getLocalName().equals("description")) {
					descriptionText = placemarksChildNodes.item(j).getTextContent();
					imgSrcFromDescription = htmlHandler.getAllImagesFromDescription(descriptionText);
				} else if (placemarksChildNodes.item(j).getLocalName().equals("ExtendedData")) {
					
					//<ExtendedData> is the parent for every <attachment>
					NodeList extendedDataChildNodes = placemarksChildNodes.item(j).getChildNodes();
					for (int k = 0; k < extendedDataChildNodes.getLength(); k++) {
						if (extendedDataChildNodes.item(k).getNodeType() != Node.ELEMENT_NODE) continue;
						if (extendedDataChildNodes.item(k).getLocalName().equals("attachment")) {
							attachments.add(extendedDataChildNodes.item(k));
						}
					}
				}
			}
			processImagesFromDescription_2(imgSrcFromDescription, attachments, placemark, multipartDto);
		}
		
	}
	
	/**
	 * 1) Compares the given List of src to images from user description
	 * and an existing <lc:attacmhent></lc:attacmhent> from <ExtendedData></ExtendedData> of Locus xml.
	 * 2) Overwrites src in attachments if they have the same filenames as those from User description
	 * 3) If description contains more src to images than the existing <ExtendedData></ExtendedData> has,
	 * it add additional <lc:attacmhent></lc:attacmhent> elements to the <ExtendedData></ExtendedData> parent.
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
	private void processImagesFromDescription_2(
		List<String> imgSrcFromDescription, List<Node> attachmentNodes, Node placemark, MultipartDto multipartDto) {
		//No images from description to insert as attachments
		if (imgSrcFromDescription.isEmpty()) {
			return;
			//WEB paths are not supported as attachments
		} else if (multipartDto.getPathType() != null && multipartDto.getPathType().equals(PathTypes.WEB)) {
			return;
		}
		//Turn all imgSrc into Locus specific paths
		final List<String> locusAttachmentsHref = getLocusSpecificAttachmentsHref_2(imgSrcFromDescription, multipartDto);
		
		//Iterate through existing <ExtendedData> elements
		if (!attachmentNodes.isEmpty()) {
			//addLcPrefixForLocusmapNamespace_2(attachmentNodes);
			attachmentNodes = attachmentNodes.stream()
				.filter(attachment -> attachment.getPrefix().equals("lc"))
				.peek(attachment -> {
					String attachmentFilename = htmlHandler.getFileName(attachment.getTextContent());
					
					Iterator<String> iterator = locusAttachmentsHref.iterator();
					while (iterator.hasNext()) {
						String imgSrc = iterator.next();
						if (htmlHandler.getFileName(imgSrc).equals(attachmentFilename)) {
							
							attachment.setTextContent(imgSrc);
							iterator.remove();
							break;
						}
					}
				})
				.collect(Collectors.toList());
			//If not all the images from Description are attached we create and add new <lc:attachment>'s
			if (!locusAttachmentsHref.isEmpty()) {
				List<Element> newAttachments = getImagesSrcAsLcAttachments_2(locusAttachmentsHref);
				Node parentExtendedData = attachmentNodes.get(0).getParentNode();
				newAttachments.forEach(parentExtendedData::appendChild);
			}
			//<ExtendedData> isn't presented within the <Placemark>
		} else {
			//Create a new <ExtendedData> parent with new <lc:attachment> children from images src from description
			List<Element> imagesSrcAsLcAttachments = getImagesSrcAsLcAttachments_2(locusAttachmentsHref);
			Node newExtendedData = getNewExtendedData_2(imagesSrcAsLcAttachments);
			placemark.appendChild(newExtendedData);
		}
	}
	
	/**
	 * Checks if the <ExtendedData></ExtendedData> StartElement has the "http://www.locusmap.eu" namespace
	 * with the "lc:" prefix for specific Locus Map xml elements.
	 * If doesn't it will add that namespace as the additional {@link Namespace} {@link XMLEvent} that will be written
	 * by {@link XMLEventWriter} right after <ExtendedData> {@link StartElement}
	 */
	private void addLcPrefixForLocusmapNamespace_2(List<Node> attachmentNodes) {
		for (int i = 0; i < attachmentNodes.size(); i++) {
			Node attachmentNode = attachmentNodes.get(i);
			if (attachmentNode.getPrefix() == null || !attachmentNode.getPrefix().equals("lc")) {
				attachmentNode.setPrefix("lc");
			}
		}
		Node extendedData = attachmentNodes.get(0).getParentNode();
		if (extendedData.getNamespaceURI() == null || !extendedData.getNamespaceURI().equals("http://www.locusmap.eu")) {
			Element extendedDataWithNamespace = document.createElement(extendedData.getNodeName());
			extendedDataWithNamespace.setAttributeNS("http://www.locusmap.eu", "xmlns", "lc");
			attachmentNodes.forEach(extendedDataWithNamespace::appendChild);
			document.replaceChild(extendedDataWithNamespace, extendedData);
		}
		
	}
	
	private List<String> getLocusSpecificAttachmentsHref_2(List<String> imagesToAttach, MultipartDto multipartDto) {
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
	private List<Element> getImagesSrcAsLcAttachments_2(List<String> imagesToAttach) {
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
	private Node getNewExtendedData_2(List<Element> elementsToBeInside) {
		Element extendedData = document.createElement("ExtendedData");
//		Element extendedData = document.createElementNS("http://www.locusmap.eu", "ExtendedData");
		elementsToBeInside.forEach(extendedData::appendChild);
		return extendedData;
	}
	
	/**
	 * Some programs as Google Earth has special href they internally redirect to their local image store.
	 * It is not recommended to change those type of hrefs.
	 *
	 * @param oldHrefWithFilename The existing <href>path to image</href> to be verified.
	 * @return 'True' if existing href is not recommended to change. Otherwise 'false'.
	 */
	private boolean isChangeable_2(String oldHrefWithFilename) {
		String googleMapSpecialUrl = "http://maps.google.com/";
		return !oldHrefWithFilename.startsWith(googleMapSpecialUrl);
	}
	
	/**
	 * All the additional information for a User (preview size, outdated descriptions etc) are placed inside the
	 * CDATA[[]] as an HTML markup.
	 * So the main goal for this method is the extracting CDATA and pass it to the HTML parser.
	 */
	public String processKml(MultipartDto multipartDto) throws XMLStreamException, IOException {
		
		InputStream kmlInputStream = stupidNamespaceFixMethod(multipartDto.getMultipartFile().getInputStream());
		
		StringWriter stringWriter = new StringWriter();
		
		XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		XMLEventReader eventReader = inputFactory.createXMLEventReader(kmlInputStream);
//		XMLEventReader eventReader = inputFactory.createXMLEventReader(multipartDto.getMultipartFile().getInputStream());
		XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
		
		eventFactory = XMLEventFactory.newInstance();
		eventWriter = outputFactory.createXMLEventWriter(stringWriter);
		
		List<String> imagesFromDescription = new ArrayList<>();
		LinkedList<XMLEvent> extendedDataEvents = new LinkedList<>();
		
		//To skip the whole processing if nothing is set
		if (!multipartDto.isSetPath() &&
			!multipartDto.isTrimDescriptions() &&
			!multipartDto.isSetPreviewSize() &&
			!multipartDto.isClearOutdatedDescriptions() &&
			!multipartDto.isTrimXml() &&
			!multipartDto.isAsAttachmentInLocus()) {
			return new String(multipartDto.getMultipartFile().getBytes());
		}
		while (eventReader.hasNext()) {
			XMLEvent event = eventReader.nextEvent();
			
			switch (event.getEventType()) {
				case XMLEvent.START_ELEMENT:
					StartElement startElement = event.asStartElement();
					if (multipartDto.isSetPath()) {
						if (startElement.getName().getLocalPart().equals("href")) {
							writeXmlEvent(multipartDto, event);
							XMLEvent newHrefTextEvent = processHref(eventReader.nextEvent(), multipartDto);
							writeXmlEvent(multipartDto, newHrefTextEvent);
							break;
						}
					}
					if (startElement.getName().getLocalPart().equals("description")) {
						writeXmlEvent(multipartDto, event);
						XMLEvent descriptionTextEvent =
							processDescriptionText(eventReader.nextEvent().asCharacters(), multipartDto);
						if (multipartDto.isAsAttachmentInLocus()) {
							imagesFromDescription.clear(); //To prepare for the images from a new description
							imagesFromDescription.addAll(
								htmlHandler.getAllImagesFromDescription(descriptionTextEvent.asCharacters().getData()));
						}
						writeXmlEvent(multipartDto, descriptionTextEvent);
						break;
					}
					if (multipartDto.isAsAttachmentInLocus() && startElement.getName().getLocalPart().equals("ExtendedData")) {
						extendedDataEvents.clear();//To start collection new <ExtendedData> inner xml events
						while (eventReader.hasNext()) {
							extendedDataEvents.add(event);
							event = eventReader.nextEvent();
							if (event.isEndElement() && event.asEndElement().getName().getLocalPart().equals("ExtendedData")) {
								extendedDataEvents.add(event);
								eventReader.nextEvent();
								break;
							}
						}
						break;
					}
					writeXmlEvent(multipartDto, event);
					break;
				
				case XMLEvent.END_ELEMENT:
					EndElement endElement = event.asEndElement();
					if (multipartDto.isAsAttachmentInLocus() && endElement.getName().getLocalPart().equals("Placemark")) {
						List<XMLEvent> extendedData = processExtendedData(
							imagesFromDescription, extendedDataEvents, multipartDto);
						writeXmlEvent(multipartDto, extendedData.toArray(XMLEvent[]::new));
						extendedDataEvents.clear();//To prepare for a new <ExtendedData> events
					}
					writeXmlEvent(multipartDto, event);
					break;
				
				default:
					writeXmlEvent(multipartDto, event);
			}
		}
		return stringWriter.toString();
	}
	
	private InputStream stupidNamespaceFixMethod(InputStream kmlInputStream) throws IOException {
		//A really stupid quickfix for GoogleEarth reproducing <ExtendedData> without Locus specific prefix "lc"
		//with corresponding namespace for the following <lc:attachments> tags
		String kml = new String(kmlInputStream.readAllBytes(), StandardCharsets.UTF_8);
		if (kml.contains("<lc:attachment>") && !kml.contains("<ExtendedData xmlns:lc=\"http://www.locusmap.eu\">")) {
			int firstHeaderIndex = kml.indexOf("<kml");
			int lastHeaderIndex = kml.indexOf(">", firstHeaderIndex);
			String header = kml.substring(firstHeaderIndex, lastHeaderIndex + 1);
			if (!header.contains("xmlns:lc=\"http://www.locusmap.eu\"")) {
				String newHeader = header.replace(">", " xmlns:lc=\"http://www.locusmap.eu\">\n");
				kml = kml.replace(header, newHeader);
			}
		}
		return new ByteArrayInputStream(kml.getBytes(StandardCharsets.UTF_8));
	}
	
	private void writeXmlEvent(MultipartDto multipartDto, XMLEvent... event) throws XMLStreamException {
		for (XMLEvent ev : event) {
			if (multipartDto.isTrimXml() && ev.isCharacters() && ev.asCharacters().isWhiteSpace()) {
				continue;
			}
			eventWriter.add(ev);
		}
	}
	
	private List<XMLEvent> processExtendedData(
		List<String> imgSrcFromDescription, List<XMLEvent> extendedDataEvents, MultipartDto multipartDto) {
		//If no need to add attachments it checks <ExtendedData> correct namespace for <lc:attachments> prefixes
		if (!multipartDto.isAsAttachmentInLocus()) {
			extendedDataEvents.stream().filter(event -> event.isStartElement() &&
				event.asStartElement().getName().getLocalPart().equals("attachments") &&
				event.asStartElement().getName().getPrefix().equals("lc"))
				.findAny()
				.ifPresent(event -> addLcPrefixForLocusmapNamespace(extendedDataEvents));
			return extendedDataEvents;
		} else {
			return processLocusAttachments(imgSrcFromDescription, extendedDataEvents, multipartDto);
		}
	}
	
	/**
	 * 1) Compares the given List of src to images from user description
	 * and an existing <lc:attacmhent></lc:attacmhent> from <ExtendedData></ExtendedData> of Locus xml.
	 * 2) Overwrites src in attachments if they have the same filenames as those from User description
	 * 3) If description contains more src to images than the existing <ExtendedData></ExtendedData> has,
	 * it add additional <lc:attacmhent></lc:attacmhent> elements to the <ExtendedData></ExtendedData> parent.
	 *
	 * @param imgSrcFromDescription A List of src to images from User Description
	 * @param extendedDataEvents    (Linked)List of {@link XMLEvent} within <ExtendedData></ExtendedData> parent, e.g.:
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
	 * @return A new {@link LinkedList<XMLEvent>} with modified or new <lc:attachments></lc:attachments>.
	 * Or the old unmodified List if no changes were done.</ExtendedData>
	 */
	private List<XMLEvent> processLocusAttachments(
		List<String> imgSrcFromDescription, List<XMLEvent> extendedDataEvents, MultipartDto multipartDto) {
		
		if (imgSrcFromDescription.isEmpty()) {
			//No images from description to insert as attachments
			return extendedDataEvents;
		} else if (multipartDto.getPathType() != null && multipartDto.getPathType().equals(PathTypes.WEB)) {
			//WEB paths are not supported as attachments
			return extendedDataEvents;
		}
		//Turn all imgSrc into Locus specific paths
		final List<String> locusAttachmentsHref = getLocusSpecificAttachmentsHref(imgSrcFromDescription, multipartDto);
		
		//Iterate through existing <ExtendedData> elements
		if (!extendedDataEvents.isEmpty()) {
			addLcPrefixForLocusmapNamespace(extendedDataEvents);
			extendedDataEvents = extendedDataEvents.stream()
				.map(extendedEvent -> {
					if (!extendedEvent.isCharacters() || extendedEvent.asCharacters().isWhiteSpace()) {
						return extendedEvent;
					}
					String eventFilename = htmlHandler.getFileName(extendedEvent.asCharacters().getData());
					
					Iterator<String> iterator = locusAttachmentsHref.iterator();
					while (iterator.hasNext()) {
						String imgSrc = iterator.next();
						if (htmlHandler.getFileName(imgSrc).equals(eventFilename)) {
							
							extendedEvent = eventFactory.createCharacters(imgSrc);
							iterator.remove();
							break;
							
						}
					}
					return extendedEvent;
				})
				.collect(Collectors.toList());
			//If not all the images from Description are attached we create and add new <lc:attachment>'s
			if (!locusAttachmentsHref.isEmpty()) {
				List<XMLEvent> xmlEventList = getImagesSrcAsLcAttachments(locusAttachmentsHref);
				extendedDataEvents.addAll(1, xmlEventList);
			}
			return extendedDataEvents;
		} else { //<ExtendedData> isn't presented within the <Placemark>
			//Create a new <ExtendedData> parent with new <lc:attachment> children from images src from description
			return getNewExtendedData(getImagesSrcAsLcAttachments(locusAttachmentsHref));
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
	private List<XMLEvent> getImagesSrcAsLcAttachments(List<String> imagesToAttach) {
		List<XMLEvent> lcAttachments = new ArrayList<>();
		imagesToAttach.forEach(img -> {
			lcAttachments.add(
				eventFactory.createCharacters("\n")); //Just for prettyPrinting
			lcAttachments.add(
				eventFactory.createStartElement("lc", "http://www.locusmap.eu", "attachment"));
			lcAttachments
				.add(eventFactory.createCharacters(img));
			lcAttachments
				.add(eventFactory.createEndElement("lc", "http://www.locusmap.eu", "attachment"));
		});
		return lcAttachments;
	}
	
	/**
	 * @param xmlEventsToBeInside All {@link XMLEvent}'s to be written inside <ExtendedData></ExtendedData> with
	 *                            Locus namespace.
	 * @return {@link LinkedList<XMLEvent>} with XMLEvents inside to be written into existing document as:
	 * <ExtendedData xmlns:lc="http://www.locusmap.eu">... xmlEventsToBeInside ...</ExtendedData>
	 */
	private List<XMLEvent> getNewExtendedData(List<XMLEvent> xmlEventsToBeInside) {
		XMLEvent extendedDataStart = eventFactory.createStartElement(
			"", "http://www.locusmap.eu", "ExtendedData");
		XMLEvent extendedDataEnd = eventFactory.createEndElement(
			"", "", "ExtendedData");
		List<XMLEvent> extendedData = new LinkedList<>(Arrays.asList(extendedDataStart, getLcLocusNamespace(), extendedDataEnd));
		extendedData.addAll(2, xmlEventsToBeInside);
		return extendedData;
	}
	
	/**
	 * Checks if the <ExtendedData></ExtendedData> StartElement has the "http://www.locusmap.eu" namespace
	 * with the "lc:" prefix for specific Locus Map xml elements.
	 * If doesn't it will add that namespace as the additional {@link Namespace} {@link XMLEvent} that will be written
	 * by {@link XMLEventWriter} right after <ExtendedData> {@link StartElement}
	 */
	private void addLcPrefixForLocusmapNamespace(List<XMLEvent> extendedDataEvents) {
		for (int i = 0; i < extendedDataEvents.size(); i++) {
			XMLEvent event = extendedDataEvents.get(i);
			if (event.isStartElement() && event.asStartElement().getName().getLocalPart().equals("ExtendedData")) {
				
				while (event.asStartElement().getNamespaces().hasNext()) {
					Namespace namespace = event.asStartElement().getNamespaces().next();
					if (namespace.getNamespaceURI().equals("http://www.locusmap.eu") && namespace.getPrefix().equals("lc")) {
						return;
					}
				}
				
				extendedDataEvents.add(++i, getLcLocusNamespace());
				return;
			}
		}
	}
	
	private Namespace getLcLocusNamespace() {
		return eventFactory.createNamespace("lc", "http://www.locusmap.eu");
	}
	
	/**
	 * The first temporary condition checks {@code '\\s*>\\s*'} regexp as Locus may spread those signs occasionally
	 * (especially after {@code <ExtendedData> tag}). So
	 */
	private XMLEvent processDescriptionText(Characters characters, MultipartDto multipartDto) {
		
		if (characters.getData().matches("\\s*>\\s*")) {
			return eventFactory.createIgnorableSpace("");
		}
		
		if (characters.isWhiteSpace() || characters.getData().matches("\\s*>\\s*")) {
			return multipartDto.isTrimXml() ?
				eventFactory.createIgnorableSpace("") :
				eventFactory.createCharacters(characters.getData());
		}
		//Obtain an inner CDATA text to treat as HTML elements or plain text
		String processedHtmlCdata = htmlHandler.processDescriptionText(characters.getData(), multipartDto);
		
		processedHtmlCdata = prettyPrintCdataXml(processedHtmlCdata, multipartDto);
		
		return eventFactory.createCData(processedHtmlCdata);
	}
	
	/**
	 * Every old href tag contains path to file and a filename. So here we derive an existing filename
	 * and append it to the new path.
	 *
	 * @param textEventHrefWithFilename {@link XMLEvent} as string within <href></href> tags
	 * @return A new href with the old filename.
	 */
	private XMLEvent processHref(XMLEvent textEventHrefWithFilename, MultipartDto multipartDto) {
		String oldHrefWithFilename = textEventHrefWithFilename.asCharacters().getData();
		if (!isChangeable(oldHrefWithFilename)) {
			return eventFactory.createCharacters(oldHrefWithFilename);
		}
		String newHrefWithOldFilename = getHtmlHandler().getNewHrefWithOldFilename(
			oldHrefWithFilename, multipartDto.getPathType(), multipartDto.getPath());
		return eventFactory.createCharacters(newHrefWithOldFilename);
	}
	
	/**
	 * Some programs as Google Earth has special href they internally redirect to their local image store.
	 * It is not recommended to change those type of hrefs.
	 *
	 * @param oldHrefWithFilename The existing <href>path to image</href> to be verified.
	 * @return 'True' if existing href is not recommended to change. Otherwise 'false'.
	 */
	private boolean isChangeable(String oldHrefWithFilename) {
		String googleMapSpecialUrl = "http://maps.google.com/";
		return !oldHrefWithFilename.startsWith(googleMapSpecialUrl);
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
