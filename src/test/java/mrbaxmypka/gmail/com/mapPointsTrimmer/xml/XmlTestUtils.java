package mrbaxmypka.gmail.com.mapPointsTrimmer.xml;

import mrbaxmypka.gmail.com.mapPointsTrimmer.entitiesDto.MultipartDto;
import org.springframework.lang.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

/**
 * @author BAXMYPKA
 */
public class XmlTestUtils {

    private static String GOOGLE_TEST_KML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:kml=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" +
            "<Document>\n" +
            "\t<name>Google Earth Test Poi</name>\n" +
            "\t<StyleMap id=\"styleMap1\">\n" +
            "\t\t<Pair>\n" +
            "\t\t\t<key>normal</key>\n" +
            "\t\t\t<styleUrl>#style1</styleUrl>\n" +
            "\t\t</Pair>\n" +
            "\t\t<Pair>\n" +
            "\t\t\t<key>highlight</key>\n" +
            "\t\t\t<styleUrl>#style2</styleUrl>\n" +
            "\t\t</Pair>\n" +
            "\t</StyleMap>\n" +
            "\t<Style id=\"style1\">\n" +
            "\t\t<LabelStyle>\n" +
            "\t\t\t<scale>0.0</scale>\n" +
            "\t\t\t<color>00000000</color>\n" +
            "\t\t</LabelStyle>\n" +
            "\t</Style>\n" +
            "\t<Style id=\"style2\">\n" +
            "\t\t<LabelStyle>\n" +
            "\t\t\t<scale>0.0</scale>\n" +
            "\t\t\t<color>00000000</color>\n" +
            "\t\t</LabelStyle>\n" +
            "\t</Style>\n" +
            "\t<Placemark>\n" +
            "\t\t\t<name>Test Placemark 1</name>\n" +
            "\t\t\t<styleUrl>#styleMap1</styleUrl>\n" +
            "\t</Placemark>\n" +
            "</Document>\n" +
            "</kml>";


    public static Document getDocument(MultipartDto multipartDto) throws ParserConfigurationException, IOException,
            SAXException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.parse(multipartDto.getMultipartFile().getInputStream());
        document.normalizeDocument();
        return document;
    }

    public static Document getMockDocument() throws ParserConfigurationException, IOException,
            SAXException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.parse(new ByteArrayInputStream(GOOGLE_TEST_KML.getBytes(StandardCharsets.UTF_8)));
        document.normalizeDocument();
        return document;
    }

    /**
     * @param document                The source to be parsed
     * @param requiredParentTagName   Parent tagName to be created or an existing one
     * @param requiredNumberOfParents How many parents with the desired child should be presented within the given document
     * @param requiredChildrenTagName Parent's children tagName that should be presented strictly within parent's tag
     * @param requiredChildrenValue   If not null, what exact text value should those children have.
     *                                Otherwise will be ignored. And only num of parents and children tag name will be considered.
     * @return True if all the conditions are valid
     */
    public static boolean containsTagsWithChildren(
            Document document,
            String requiredParentTagName,
            int requiredNumberOfParents,
            String requiredChildrenTagName,
            @Nullable String requiredChildrenValue) {

        int parentsWithDesiredChildCount = 0;

        NodeList parents = document.getElementsByTagName(requiredParentTagName);
        parents:
        for (int i = 0; i < parents.getLength(); i++) {

            Node parent = parents.item(i);
            NodeList parentChildNodes = parent.getChildNodes();
            for (int j = 0; j < parentChildNodes.getLength(); j++) {
                Node child = parentChildNodes.item(j);
                if (requiredChildrenValue != null) {
                    if (child.getNodeName().equals(requiredChildrenTagName) && child.getTextContent().equals(requiredChildrenValue)) {
                        parentsWithDesiredChildCount += 1;
                        continue parents;
                    }
                } else {
                    if (child.getNodeName().equals(requiredChildrenTagName)) {
                        parentsWithDesiredChildCount += 1;
                        continue parents;
                    }
                }
            }

        }
        return parentsWithDesiredChildCount == requiredNumberOfParents;
    }

    /**
     * @param document
     * @param requiredTagName Parent Nodes to be looked for their children
     * @param requiredChildTagName  A child tag name to found in every Parent
     * @param requiredChildValue    A child text value to be kept in a child Node
     * @return true if any of the Parent contains the Child with the desired value
     */
    public static boolean containsTagWithChild(
            Document document, String requiredTagName, String requiredChildTagName, String requiredChildValue) {

        NodeList parentNodes = document.getElementsByTagName(requiredTagName);
        for (int i = 0; i < parentNodes.getLength(); i++) {
            Node parentNode = parentNodes.item(i);

            NodeList childNodes = parentNode.getChildNodes();
            for (int j = 0; j < childNodes.getLength(); j++) {
                Node child = childNodes.item(j);
                if (child.getNodeName() != null && child.getNodeName().equals(requiredChildTagName) &&
                        child.getTextContent().equals(requiredChildValue)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @param document
     * @param requiredTagName        Parent Nodes to be looked for their children
     * @param requiredTagAttributeName  An attribute name to be found in every appropriate tag
     * @param requiredAttributeValue An attribute text value to be found
     * @return true if any of a desired tag is found and contains the desired value.
     */
    public static boolean containsTagWithAttribute(
            Document document, String requiredTagName, String requiredTagAttributeName, String requiredAttributeValue) {

        NodeList requiredNodes = document.getElementsByTagName(requiredTagName);
        for (int i = 0; i < requiredNodes.getLength(); i++) {
            Node requiredNode = requiredNodes.item(i);

            if (requiredNode.hasAttributes() && requiredNode.getAttributes().getNamedItem(requiredTagAttributeName) != null) {
                Node requiredAttribute = requiredNode.getAttributes().getNamedItem(requiredTagAttributeName);
                if (requiredAttribute.getTextContent().equals(requiredAttributeValue)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static String getAsText(Document document) {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            DOMSource domSource = new DOMSource(document);
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            trimWhitespaces(document); //To delete all the previous whitespaces
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            StringWriter stringWriter = new StringWriter();
            Result result = new StreamResult(stringWriter);
            transformer.transform(domSource, result);
            return clearFix(stringWriter);
        } catch (TransformerException e) {
            e.printStackTrace();
            return "";
        }
    }

    private static void trimWhitespaces(Node node) {
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);
            if (childNode.getNodeType() == Node.TEXT_NODE) {
                childNode.setTextContent(childNode.getTextContent().trim());
            }
            trimWhitespaces(childNode);
        }
    }

    private static String clearFix(StringWriter rawXml) {
        return rawXml.toString().replaceAll("\r\n", "\n").replaceAll("&gt;\t", "");
    }
}
