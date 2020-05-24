package mrbaxmypka.gmail.com.mapPointsTrimmer.klm;

import lombok.NoArgsConstructor;
import mrbaxmypka.gmail.com.mapPointsTrimmer.entitiesDto.MultipartDto;
import mrbaxmypka.gmail.com.mapPointsTrimmer.utils.PathTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.xml.stream.*;
import javax.xml.stream.events.*;
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
@NoArgsConstructor
@Component
public class XmlHandler {
	
	private XMLEventFactory eventFactory;
	private XMLEventWriter eventWriter;
	private HtmlHandler htmlHandler;
	private List<String> imagesExtensions;
	
	@Autowired
	public XmlHandler(HtmlHandler htmlHandler) {
		this.htmlHandler = htmlHandler;
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
				return htmlHandler.getLocusAttachmentAbsoluteHref(imgSrc, multipartDto);
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
		String processedHtmlCdata = htmlHandler.processCdata(characters.getData(), multipartDto);
		
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
		String newHrefWithOldFilename = htmlHandler.getNewHrefWithOldFilename(
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
