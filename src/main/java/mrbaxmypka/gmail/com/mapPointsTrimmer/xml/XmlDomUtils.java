package mrbaxmypka.gmail.com.mapPointsTrimmer.xml;

import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

/**
 * @author BAXMYPKA
 */
@Slf4j
public class XmlDomUtils {
	
	private Document document;
	
	public XmlDomUtils(Document document) {
		this.document = document;
	}
	
	/**
	 * @param parents        A Parents {@link NodeList} to find children of
	 * @param childNodeName  A Child Node tag name to be found
	 * @param recursively    To look through the children of all the children of every Node
	 * @param createIfAbsent If not a such Child Node within the Parent Node found
	 *                       a new one will be created, appended to the Parent and added to the resulting List.
	 * @param asFirstChild   Only in conjunction with "createIfAbsent" parameter.
	 *                       If true a new child Node will be inserted as a first child of the parent.
	 *                       Otherwise it will be appended at the end.
	 * @return A List of Children Nodes or {@link java.util.Collections#EMPTY_LIST} if nothing found.
	 * @throws IllegalArgumentException if 'recursively' and 'createIfAbsent' both == true.
	 */
	public List<Node> getChildNodesFromParents(NodeList parents, String childNodeName, boolean recursively,
		boolean createIfAbsent, boolean asFirstChild)
		throws IllegalArgumentException {
		if (recursively && createIfAbsent) throw new IllegalArgumentException(
			"You can create only direct children within parents! 'recursively' is only for looking through exist nodes!");
		
		List<Node> children = new ArrayList<>();
		
		for (int i = 0; i < parents.getLength(); i++) {
			Node parentNode = parents.item(i);
			//Looking through all the children of all the children for the given parent
			if (recursively) {
				List<Node> allChildNodes = getAllChildrenRecursively(new ArrayList<>(), parentNode);
				allChildNodes.stream()
					.filter(node -> node.getNodeName() != null && node.getNodeName().equals(childNodeName))
					.forEach(children::add);
				continue;
			}
			//Looking through direct children
			NodeList childNodes = parentNode.getChildNodes();
			List<Node> existingChildren = new ArrayList<>();
			for (int j = 0; j < childNodes.getLength(); j++) {
				Node childNode = childNodes.item(j);
				if (childNode.getNodeName() != null && childNode.getNodeName().equals(childNodeName)) {
					existingChildren.add(childNode);
					log.trace("Existing <{}> has been found in <{}>", childNodeName, parentNode.getNodeName());
				}
			}
			if (createIfAbsent && existingChildren.isEmpty()) {
				//Has to create a child if absent and no child is exist
				Element newChildNode = document.createElement(childNodeName);
				if (asFirstChild) {
					parentNode.insertBefore(newChildNode, parentNode.getFirstChild());
				} else {
					parentNode.appendChild(newChildNode);
				}
				children.add(newChildNode);
				log.trace("As 'createIfAbsent'== true and <{}> has't been found in <{}> it was created and appended",
					childNodeName, parentNode.getNodeName());
			} else {
				//Just add the found children (even if they aren't)
				children.addAll(existingChildren);
			}
		}
		log.trace("{} <{}> children have been found for <{}>", children.size(), childNodeName, parents.item(0).getNodeName());
		return children;
	}
	
	/**
	 * @param parents        A Parents {@link NodeList} to find children of
	 * @param childNodeName  A Child Node tag name to be found
	 * @param recursively    To look through the children of all the children of every Node
	 * @param createIfAbsent If not a such Child Node within the Parent Node found
	 *                       a new one will be created, appended to the Parent and added to the resulting List.
	 * @param asFirstChild   Only in conjunction with "createIfAbsent" parameter.
	 *                       If true a new child Node will be inserted as a first child of the parent.
	 *                       Otherwise it will be appended at the end.
	 * @return A List of Children Nodes or {@link java.util.Collections#EMPTY_LIST} if nothing found.
	 * @throws IllegalArgumentException if 'recursively' and 'createIfAbsent' both == true.
	 */
	public List<Node> getChildNodesFromParents(List<Node> parents, String childNodeName, boolean recursively,
		boolean createIfAbsent, boolean asFirstChild)
		throws IllegalArgumentException {
		if (recursively && createIfAbsent) throw new IllegalArgumentException(
			"You can create only direct children within parents! 'recursively' is only for looking through exist nodes!");
		
		List<Node> children = new ArrayList<>();
		
		for (int i = 0; i < parents.size(); i++) {
			Node parentNode = parents.get(i);
			//Looking through all the children of all the children for the given parent
			if (recursively) {
				List<Node> allChildNodes = getAllChildrenRecursively(new ArrayList<>(), parentNode);
				allChildNodes.stream()
					.filter(node -> node.getNodeName() != null && node.getNodeName().equals(childNodeName))
					.forEach(children::add);
				continue;
			}
			//Looking through direct children
			NodeList childNodes = parentNode.getChildNodes();
			List<Node> existingChildren = new ArrayList<>();
			for (int j = 0; j < childNodes.getLength(); j++) {
				Node childNode = childNodes.item(j);
				if (childNode.getNodeName() != null && childNode.getNodeName().equals(childNodeName)) {
					existingChildren.add(childNode);
					log.trace("Existing <{}> has been found in <{}>", childNodeName, parentNode.getNodeName());
				}
			}
			if (createIfAbsent && existingChildren.isEmpty()) {
				//Has to create a child if absent and no child is exist
				Element newChildNode = document.createElement(childNodeName);
				if (asFirstChild) {
					parentNode.insertBefore(newChildNode, parentNode.getFirstChild());
				} else {
					parentNode.appendChild(newChildNode);
				}
				children.add(newChildNode);
				log.trace("As 'createIfAbsent'== true and <{}> has't been found in <{}> it was created and appended",
					childNodeName, parentNode.getNodeName());
			} else {
				//Just add the found children (even if they aren't)
				children.addAll(existingChildren);
			}
		}
		log.trace("{} <{}> children have been found for <{}>", children.size(), childNodeName, parents.get(0).getNodeName());
		return children;
	}
	
	/**
	 * @param parent         A Parent Node to find child of
	 * @param childNodeName  A Child Node tag name to be found
	 * @param childNodeValue {@link Nullable} If not null only the Child Node with equal text content will be returned
	 * @param recursively    To look through the children of all the children of every Node
	 * @param createIfAbsent If not a such Child Node within the Parent Node found
	 *                       a new one will be created, appended to the Parent and added to the resulting List.
	 * @param asFirstChild   Only in conjunction with "createIfAbsent" parameter.
	 *                       If true a new child Node will be inserted as a first child of the parent.
	 *                       Otherwise it will be appended at the end.
	 * @return A List of Children Nodes or {@link java.util.Collections#EMPTY_LIST} if nothing found.
	 * @throws IllegalArgumentException if 'recursively' and 'createIfAbsent' both == true.
	 */
	public List<Node> getChildNodesFromParent(
		Node parent, String childNodeName, @Nullable String childNodeValue, boolean recursively, boolean createIfAbsent, boolean asFirstChild)
		throws IllegalArgumentException {
		
		if (recursively && createIfAbsent) throw new IllegalArgumentException(
			"You can create only direct children within parents! 'recursively' is only for looking through exist nodes!");
		
		List<Node> childrenNodesToBeReturned = new ArrayList<>();
		
		NodeList childNodes = parent.getChildNodes();
		List<Node> existingChildren = new ArrayList<>();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node childNode = childNodes.item(i);
			
			if (recursively) {
				List<Node> allChildren = getAllChildrenRecursively(new ArrayList<>(), childNode);
				allChildren.stream()
					.filter(node -> node.getNodeName() != null && node.getNodeName().equals(childNodeName))
					.forEach(existingChildren::add);
				continue;
			}
			if (childNode.getNodeName() != null && childNode.getNodeName().equals(childNodeName)) {
				if (childNodeValue == null) {
					existingChildren.add(childNode);
				} else if (childNode.getTextContent() != null && childNode.getTextContent().equals(childNodeValue)) {
					existingChildren.add(childNode);
				}
			}
		}
		if (createIfAbsent && existingChildren.isEmpty()) {
			//Has to create a child if absent and no child is exist
			Element newChildNode = document.createElement(childNodeName);
			if (asFirstChild) {
				parent.insertBefore(newChildNode, parent.getFirstChild());
			} else {
				parent.appendChild(newChildNode);
			}
			childrenNodesToBeReturned.add(newChildNode);
			log.trace("As 'createIfAbsent'== true and <{}> has't been found in <{}> it was created and appended",
				childNodeName, parent.getNodeName());
		} else {
			//Just add the found children (even if they aren't)
			childrenNodesToBeReturned.addAll(existingChildren);
		}
		log.trace("{} <{}> children have been found for <{}>",
			childrenNodesToBeReturned.size(), childNodeName, parent.getNodeName());
		return childrenNodesToBeReturned;
	}
	
	public List<Node> getAllChildrenRecursively(List<Node> nodeList, Node parentNode) {
		if (parentNode.hasChildNodes()) {
			NodeList childNodes = parentNode.getChildNodes();
			for (int i = 0; i < childNodes.getLength(); i++) {
				Node childNode = childNodes.item(i);
				getAllChildrenRecursively(nodeList, childNode);
			}
		} else {
			nodeList.add(parentNode);
		}
		return nodeList;
	}
}
