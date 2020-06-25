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
import java.io.IOException;

/**
 * @author BAXMYPKA
 */
public class XmlTestUtils {
	
	public static Document getDocument(MultipartDto multipartDto) throws ParserConfigurationException, IOException,
		SAXException {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document document = documentBuilder.parse(multipartDto.getMultipartFile().getInputStream());
		document.normalizeDocument();
		return document;
	}
	
	/**
	 * @param document                The source to be parsed
	 * @param requiredParentTagName   Parent tagName to be created or an existing one
	 * @param requiredNumberOfParents How many parents with the desired child should be presented within the given document
	 * @param requiredChildrenTagName Parent's children tagName that should be presented strictly within parent's tag
	 * @param requiredChildrenValue   If not null, what exact text value should those children have.
	 *                                Otherwise will be ignored. And only num of parents and children tag name will be consedered.
	 * @return True if all the conditions are valid
	 */
	public static boolean containsParentsWithChildren(
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
	 * @param requiredParentTagName Parent Nodes to be looked for their children
	 * @param requiredChildTagName  A child tag name to found in every Parent
	 * @param requiredChildValue    A child text value to be kept in a child Node
	 * @return true if any of the Parent contains the Child with the desired value
	 */
	public static boolean containsParentWithChild(
		Document document, String requiredParentTagName, String requiredChildTagName, String requiredChildValue) {
		
		NodeList parentNodes = document.getElementsByTagName(requiredParentTagName);
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
}
