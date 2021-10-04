package mrbaxmypka.gmail.com.mapPointsTrimmer.xml;

import lombok.extern.slf4j.Slf4j;
import mrbaxmypka.gmail.com.mapPointsTrimmer.entitiesDto.MultipartMainDto;
import mrbaxmypka.gmail.com.mapPointsTrimmer.services.FileService;
import mrbaxmypka.gmail.com.mapPointsTrimmer.services.GoogleIconsService;
import mrbaxmypka.gmail.com.mapPointsTrimmer.utils.PathTypes;
import org.springframework.stereotype.Component;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.InputStream;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Kml processing class based on the StAX xml-library.
 */
@Slf4j
@Component
public class KmlHandler extends XmlHandler {

    public KmlHandler(HtmlHandler htmlHandler, GoogleIconsService googleIconsService, FileService fileService) {
        super(htmlHandler, googleIconsService, fileService);
    }

    /**
     * All the additional information for a User (preview size, outdated descriptions etc) are placed inside the
     * CDATA[[]] as an HTML markup.
     * So the main goal for this method is the extracting CDATA and pass it to the HTML parser.
     */
    public String processXml(InputStream kmlInputStream, MultipartMainDto multipartMainDto)
            throws IOException, ParserConfigurationException, SAXException, TransformerException, InterruptedException {
        log.info("The given KML is being processed...");

        Document document = getDocument(kmlInputStream);
        XmlDomUtils xmlDomUtils = new XmlDomUtils(document);
        KmlUtils kmlUtils = new KmlUtils(document, xmlDomUtils);
        LocusMapHandler locusMapHandler = new LocusMapHandler(getFileService(), xmlDomUtils, kmlUtils, getHtmlHandler());
        //It was HERE as the first method
        //        locusMapHandler.processKml(document, multipartMainDto);
/*
        //Processing Google Earth specific options
        log.info("Kml Google options are being processed...");
        GoogleEarthHandler googleEarthHandler = new GoogleEarthHandler(kmlUtils);
        googleEarthHandler.processKml(document, multipartMainDto);
*/
        log.info("Setting the new path to images...");
        processHref(document, multipartMainDto);

        //Processing the further text options regarding to inner CDATA or plain text from <description>s
        log.info("Descriptions from KML are being processed...");
        processDescriptionsTexts(document, multipartMainDto);

        locusMapHandler.processKml(document, multipartMainDto);

        //Processing Google Earth specific options
        log.info("Kml Google options are being processed...");
        GoogleEarthHandler googleEarthHandler = new GoogleEarthHandler(kmlUtils);
        googleEarthHandler.processKml(document, multipartMainDto);

        if (multipartMainDto.isTrimXml()) {
            log.info("KML is being trimmed...");
            trimWhitespaces(document);
        }
        log.info("The given KML has been processed.");
        return writeTransformedDocument(document, !multipartMainDto.isTrimXml());
    }

    /**
     * Every old href tag contains path to file and a filename. So here we derive an existing filename
     * and append it to the new path.
     *
     * @param document {@link Document} with all the child Nodes
     */
    private void processHref(Document document, MultipartMainDto multipartMainDto) {
        NodeList hrefs = document.getElementsByTagName("href");
        for (int i = 0; i < hrefs.getLength(); i++) {
            Node node = hrefs.item(i);
            String currentIconHrefWithFilename = node.getTextContent();
            String processedIconHrefWithFilename = getGoogleIconsService().processIconHref(currentIconHrefWithFilename, multipartMainDto);
            if (multipartMainDto.getPath() != null) {
                //Full processing with a new href to image
                String newHrefWithOldFilename = getHtmlHandler().getNewHrefWithOldFilename(
                        processedIconHrefWithFilename, multipartMainDto.getPathType(), multipartMainDto.getPath());
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
    private void processDescriptionsTexts(Document document, MultipartMainDto multipartMainDto) {
//		NodeList descriptions = documentRoot.getElementsByTagName("description");
        NodeList descriptions = document.getElementsByTagName("description");
        for (int i = 0; i < descriptions.getLength(); i++) {

            Node descriptionNode = descriptions.item(i);
            String textContent = descriptions.item(i).getTextContent();

            if (textContent == null || textContent.isBlank()) {
                descriptionNode.setTextContent("");
                log.trace("Description has been set as blank string");
            } else {
                LocalDateTime timeStampFromPlacemark = null;
                if (multipartMainDto.isClearOutdatedDescriptions()) {
                    //Get the <gx:TimeStamp> <when>2014-11-21T00:27:36Z</when>	</gx:TimeStamp> "when" timestamp
                    //To compare with the possible one into the <description>
                    timeStampFromPlacemark = getTimeStampFromPlacemark(descriptionNode.getParentNode(), document);
                }

                //Obtain an inner CDATA text to treat as HTML elements or plain text
                String processedHtmlCdata = getHtmlHandler().processDescriptionText(textContent, multipartMainDto, timeStampFromPlacemark);
                processedHtmlCdata = prettyPrintCdataXml(processedHtmlCdata, multipartMainDto);
                CDATASection cdataSection = document.createCDATASection(processedHtmlCdata);
                descriptionNode.setTextContent("");
                descriptionNode.appendChild(cdataSection);
                log.trace("Description has been processed and set");
            }
        }
        log.info("All the <description> have been processed.");
    }

    private LocalDateTime getTimeStampFromPlacemark(Node placemarkNode, Document document) {
        LocalDateTime dateTime = null;
        Node when = null;
        NodeList childNodes = placemarkNode.getChildNodes();
        try {
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node childrenNode = childNodes.item(i);
                if ("gx:TimeStamp".equalsIgnoreCase(childrenNode.getNodeName())) {
                    when = getWhenFromTimeStamp(childrenNode, document);
                    dateTime = LocalDateTime.parse(when.getTextContent().trim(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                    break;
                }
            }
        } catch (DateTimeException e) {
            log.info(e.getMessage(), e);
            dateTime = LocalDateTime.parse(when.getTextContent().trim(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } finally {
            return dateTime != null ? dateTime : LocalDateTime.now();
        }
    }

    /**
     * @param timeStampNode
     * @param document
     * @return The extracted Node or a newly created, filled with the LocalDateTime.now() and appended to the parent Node one.
     */
    private Node getWhenFromTimeStamp(Node timeStampNode, Document document) {
        Node when = null;
        NodeList childNodes = timeStampNode.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childrenNode = childNodes.item(i);
            if ("when".equalsIgnoreCase(childrenNode.getNodeName())) {
                when = childrenNode;
            }
        }
        if (when == null) {
            when = document.createElement("when");
            when.setTextContent(LocalDateTime.now().toString());
            timeStampNode.appendChild(when);
        } else if (when.getTextContent() == null || when.getTextContent().isBlank()) {
            when.setTextContent(LocalDateTime.now().toString());
        }
        return when;
    }

    /**
     * Just for pretty printing and mark out CDATA descriptions.
     * If the whole XML document is inline except CDATA so as to emphasize that CDATA among XML this method add start
     * and end lineBreaks.
     */
    private String prettyPrintCdataXml(String processedHtmlCdata, MultipartMainDto multipartMainDto) {
        if (multipartMainDto.isTrimXml() && !multipartMainDto.isTrimDescriptions()) {
            processedHtmlCdata = "\n" + processedHtmlCdata.concat("\n");
        }
        log.trace("CDATA from the <description> will be return as the prettyPrinted one");
        return processedHtmlCdata;
    }

    /**
     * By just looking through the .xml(.kml) as a {@link String} adds files from the {@link MultipartMainDto#getImagesNamesFromZip()}
     * into the {@link MultipartMainDto#getFilesToBeExcluded()} if a given String doesn't contain it.
     * Removes
     *
     * @param kml
     * @param multipartMainDto
     */
    private void removeUnusedFiles(String kml, MultipartMainDto multipartMainDto) {
        multipartMainDto.getImagesNamesFromZip().forEach(imageName -> {
            if (!kml.contains(imageName)) {
                multipartMainDto.getFilesToBeExcluded().add(imageName);
            }
        });
    }
}