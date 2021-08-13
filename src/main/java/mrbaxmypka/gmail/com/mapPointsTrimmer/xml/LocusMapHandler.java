package mrbaxmypka.gmail.com.mapPointsTrimmer.xml;

import lombok.extern.slf4j.Slf4j;
import mrbaxmypka.gmail.com.mapPointsTrimmer.entitiesDto.MultipartDto;
import mrbaxmypka.gmail.com.mapPointsTrimmer.services.FileService;
import mrbaxmypka.gmail.com.mapPointsTrimmer.utils.PathTypes;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.stream.events.XMLEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class LocusMapHandler {

    private FileService fileService;
    private XmlDomUtils xmlDomUtils;
    private KmlUtils kmlUtils;
    private HtmlHandler htmlHandler;
    //TODO: to eliminate this???
    private Document document;

    public LocusMapHandler(FileService fileService,
                           XmlDomUtils xmlDomUtils,
                           KmlUtils kmlUtils,
                           HtmlHandler htmlHandler) {
        this.fileService = fileService;
        this.xmlDomUtils = xmlDomUtils;
        this.kmlUtils = kmlUtils;
        this.htmlHandler = htmlHandler;
    }

    Document processKml(Document document, MultipartDto multipartDto) {
        this.document = document;
        this.xmlDomUtils = new XmlDomUtils(document);

        Element documentRoot = document.getDocumentElement();
        if (multipartDto.isAsAttachmentInLocus()) {
            log.info("Images are being attached for Locus...");
            processLocusAttachments(documentRoot, multipartDto);
        }
        if (multipartDto.isReplaceLocusIcons()) {
            log.info("Photo icons are being replaced in Locus...");
            LocusIconsHandler locusIconsHandler = new LocusIconsHandler(fileService, kmlUtils);
            locusIconsHandler.replaceLocusIcons(multipartDto);
        }

        return this.document;
    }

    /**
     * 1) Compares the given List of src to images from user description
     * and an existing <lc:attacmhent></lc:attacmhent> from <ExtendedData></ExtendedData> of Locus xml.
     * 2) Overwrites src in attachments if they have the same filenames as those from User description
     * 3) If description contains more src to images than the existing <ExtendedData></ExtendedData> has,
     * it add additional <lc:attacmhent></lc:attacmhent> elements to the <ExtendedData></ExtendedData> parent.
     *
     * @return A new {@link LinkedList < XMLEvent >} with modified or new <lc:attachments></lc:attachments>.
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
                if (placemarkChild.getNodeType() != Node.ELEMENT_NODE || placemarkChild.getLocalName() == null)
                    continue;
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
        log.info("All <attachment>'s for Locus has been processed and added.");
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
                        String attachmentFilename = fileService.getFileName(attachment.getTextContent());
                        //If <attachment> has same name as any img from description, this attachment is being set same
                        // src="" from src List
                        Iterator<String> iterator = locusAttachmentsHref.iterator();
                        while (iterator.hasNext()) {
                            String imgSrc = iterator.next();
                            if (fileService.getFileName(imgSrc).equals(attachmentFilename)) {

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

    private List<String> getLocusSpecificAttachmentsHref(List<String> imagesToAttach, MultipartDto multipartDto) {
        imagesToAttach = imagesToAttach.stream()
                .map(imgSrc -> {
                    //Locus lc:attachments accepts only RELATIVE type of path
                    //So remake path to Locus specific absolute path without "file:///"
                    return htmlHandler.getLocusAttachmentAbsoluteHref(imgSrc, multipartDto);
                })
                .filter(imgSrc -> !imgSrc.isBlank())
                .collect(Collectors.toList());
        log.trace("{} processed 'src' for Locus <attachment> will be returned", imagesToAttach.size());
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
        log.trace("{} <attachment>'s with the processed 'src' for images will be returned", lcAttachments.size());
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
        log.trace("A new <ExtendedData> will be returned");
        return extendedData;
    }
}
