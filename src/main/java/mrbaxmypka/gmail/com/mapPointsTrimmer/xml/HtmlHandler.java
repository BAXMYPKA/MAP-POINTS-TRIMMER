package mrbaxmypka.gmail.com.mapPointsTrimmer.xml;

import lombok.NoArgsConstructor;
import mrbaxmypka.gmail.com.mapPointsTrimmer.entitiesDto.MultipartDto;
import mrbaxmypka.gmail.com.mapPointsTrimmer.utils.PathTypes;
import mrbaxmypka.gmail.com.mapPointsTrimmer.utils.PreviewSizeUnits;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.jsoup.select.NodeFilter;
import org.jsoup.select.NodeVisitor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * HTML processing class based on {@link org.jsoup.Jsoup} library to parse only CDATA as HTML.
 */
@NoArgsConstructor
@Component
public class HtmlHandler {
	
	/**
	 * @param description  Receives inner text from {@literal <description>...</description>} which in fact is the
	 *                     HTML markup
	 * @param multipartDto To determine all other conditions to be processed on CDATA HTML
	 * @return Fully processed HTML markup to be included in CDATA block.
	 */
	public String processDescriptionText(String description, MultipartDto multipartDto) {
		Element parsedHtmlFragment = Jsoup.parseBodyFragment(description).body();
		
		if (parsedHtmlFragment == null) {
			//No html markup found, cdata is a plain text
			return description;
		} else if (parsedHtmlFragment.childNodeSize() == 1 && parsedHtmlFragment.childNode(0) instanceof TextNode) {
			//The only child is a plain text
			return description;
		}
		//A possible plain text outside html markup
		String plainTextDescription = extractPlainTextDescriptions(parsedHtmlFragment).trim();
		//MUST be the first treatment
		if (multipartDto.isClearOutdatedDescriptions()) {
			clearOutdatedDescriptions(parsedHtmlFragment, multipartDto);
		}
		if (multipartDto.getPath() != null) {
			setPath(parsedHtmlFragment, multipartDto.getPathType(), multipartDto.getPath());
		}
		if (multipartDto.getPreviewSize() != null) {
			setPreviewSize(parsedHtmlFragment, multipartDto);
		}
		addStartEndComments(parsedHtmlFragment);
		// MUST be the last treatment in all the conditions chain
		if (multipartDto.isTrimDescriptions()) {
			return trimDescriptions(parsedHtmlFragment);
		}
		parsedHtmlFragment.prependText(plainTextDescription);
		return parsedHtmlFragment.html();
	}
	
	List<String> getAllImagesFromDescription(String description) {
		Element parsedHtmlFragment = Jsoup.parseBodyFragment(description).body();
		if (parsedHtmlFragment == null) {
			//No html markup found, cdata is a plain text
			return Collections.emptyList();
		}
		Elements imgElements = parsedHtmlFragment.select("img[src]");
		return imgElements.stream().map(img -> img.attr("src")).collect(Collectors.toList());
	}
	
	/**
	 * The given CDATA may contain a plain texts before or after HTML markup.
	 * So here we extract a possible text (as {@link TextNode}) to append it back to the processed HTML
	 *
	 * @return Extracted plain text or empty string.
	 */
	private String extractPlainTextDescriptions(Element parsedHtmlFragment) {
		//A possible User Text Description as TextNode is strictly one of the children.
		return parsedHtmlFragment.childNodes().stream()
			.filter(node -> node instanceof TextNode)
			.map(node -> ((TextNode) node).getWholeText())
			.collect(Collectors.joining("\n"));
	}
	
	/**
	 * Sets new local or remote paths instead old ones.
	 * I.e. old path {@code <a href="files:/_1404638472855.jpg"></>}
	 * can be replaced with {@code <a href="C:/files:/a new path/_1404638472855.jpg"></>}
	 */
	private void setPath(Element parsedHtmlFragment, PathTypes pathType, String path) {
		
		Elements aElements = parsedHtmlFragment.select("a[href]");
		Elements imgElements = parsedHtmlFragment.select("img[src]");
		
		aElements.forEach((a) -> {
			String newPathWithFilename = getNewHrefWithOldFilename(a.attr("href"), pathType, path);
			a.attr("href", newPathWithFilename);
		});
		imgElements.forEach((img) -> {
			String newPathWithFilename = getNewHrefWithOldFilename(img.attr("src"), pathType, path);
			img.attr("src", newPathWithFilename);
		});
	}
	
	/**
	 * Each existing a[href] contains a full path with the filename as the last text element.
	 * Here we have to replace only the URL and leave the original filename.
	 */
	String getNewHrefWithOldFilename(@Nullable String oldHrefWithFilename, PathTypes pathType, String newHrefWithoutFilename) {
		oldHrefWithFilename = oldHrefWithFilename == null || oldHrefWithFilename.isEmpty() ? "" : oldHrefWithFilename;
		
		//User may want to erase <href>
		if (newHrefWithoutFilename.isBlank()) {
			return "";
		}
		String newHrefWithOldFilename = "";
		
		if (pathType.equals(PathTypes.RELATIVE)) {
			newHrefWithOldFilename = getNewRelativeHref(oldHrefWithFilename, newHrefWithoutFilename);
		} else if (pathType.equals(PathTypes.ABSOLUTE)) {
			newHrefWithOldFilename = getNewAbsoluteHref(oldHrefWithFilename, newHrefWithoutFilename);
		} else if (pathType.equals(PathTypes.WEB)) {
			newHrefWithOldFilename = getNewWebHref(oldHrefWithFilename, newHrefWithoutFilename);
		}
		return newHrefWithOldFilename;
	}
	
	private String getNewRelativeHref(String oldHrefWithFilename, String newHrefWithoutFilename) {
		newHrefWithoutFilename = trimNewHrefWithoutFilename(newHrefWithoutFilename);
		String filename = getFileName(oldHrefWithFilename);
		
		return newHrefWithoutFilename.concat(filename);
	}
	
	/**
	 * Locus Map {@literal <lc:attachment></lc:attachment>} receives only {@link PathTypes#RELATIVE} or
	 * {@link PathTypes#ABSOLUTE} without (!) 'file:///' prefix.
	 *
	 * @return 1) Locus specific absolute type of path like: '/sdcard/Locus/photos/'
	 * 2) Relative type of path (starting with '../', '/' or folder name like 'Locus/photos/'
	 * 3) Empty String if the given href starting with 'www.' or 'http://'
	 */
	String getLocusAttachmentAbsoluteHref(String oldHrefAbsoluteTypeWithFilename, MultipartDto multipartDto) {
		if (oldHrefAbsoluteTypeWithFilename.startsWith("http") || oldHrefAbsoluteTypeWithFilename.startsWith("www")) {
			return "";
		}
		String locusHref = oldHrefAbsoluteTypeWithFilename.trim().replace("file:///", "");
		
		if (PathTypes.ABSOLUTE.equals(multipartDto.getPathType()) && !locusHref.startsWith("/")) {
			locusHref = "/" + locusHref;
		}//Relative type of path can start from "/" or "..." so we ignore that type
		locusHref = locusHref.replaceAll("\\\\", "/").replaceAll("//", "/");
		return locusHref;
	}
	
	private String getNewAbsoluteHref(String oldHrefWithFilename, String newHrefWithoutFilename) {
		newHrefWithoutFilename = trimNewHrefWithoutFilename(newHrefWithoutFilename);
		newHrefWithoutFilename = newHrefWithoutFilename.replaceAll("\\\\", "/");
		String filename = getFileName(oldHrefWithFilename);
		
		return "file:///" + newHrefWithoutFilename + filename;
	}
	
	private String getNewWebHref(String oldHrefWithFilename, String newHrefWithoutFilename) {
		newHrefWithoutFilename = trimNewHrefWithoutFilename(newHrefWithoutFilename);
		String filename = getFileName(oldHrefWithFilename);
		
		return newHrefWithoutFilename + filename;
	}
	
	/**
	 * Extract full filename from a given img[src] or a[href]. E.g. 'files:/image.png' will be returned as 'image.png'
	 *
	 * @param oldHrefWithFilename Href or src to the image
	 * @return The name of the file from the given src or empty string if nothing found.
	 */
	String getFileName(String oldHrefWithFilename) {
		if (!oldHrefWithFilename.contains(".") ||
			(!oldHrefWithFilename.contains("/") && !oldHrefWithFilename.contains("\\"))) return "";
		int lastIndexOFSlash = oldHrefWithFilename.lastIndexOf("/") != -1 ?
			oldHrefWithFilename.lastIndexOf("/") :
			oldHrefWithFilename.lastIndexOf("\\");
		String filename = oldHrefWithFilename.substring(lastIndexOFSlash + 1);
		return filename.isBlank() ? "" : filename;
	}
	
	private String trimNewHrefWithoutFilename(String newHrefWithoutFilename) {
		newHrefWithoutFilename = newHrefWithoutFilename.trim();
		// Each existing a[href] contains a full path with the filename as the last text element.
		// Here we have to replace only the URL and leave the original filename.
		if (!newHrefWithoutFilename.endsWith("/")) {
			// Every new href has to end with '/'
			newHrefWithoutFilename = newHrefWithoutFilename.concat("/");
		}
		newHrefWithoutFilename = newHrefWithoutFilename
			.replaceAll("\\s", "%20")
			.replaceAll("\\\\", "/");
		
		return newHrefWithoutFilename;
	}
	
	/**
	 * 1) Sets preview size in pixels for all <img> Elements.
	 * 2) Checks if the given <img> Elements are presented as links within <a></a>. If not, wrap images with <a></a>
	 * to create links.
	 * 3) Creates a new description within Locus Pro <!-- desc_user:start --> ... <!-- desc_user:end -->
	 * with User's text and images.
	 * For Locus Pro {@code <!-- desc_user:start -->} and {@code <!-- desc_user:end -->} are the markers
	 * for displaying all inner data and text on POI screen (description text, photo, photo data etc).
	 * So when {@link MultipartDto#getPreviewSize()} != null to display it on the screen these comments
	 * have to embrace all the description.
	 */
	private void setPreviewSize(Element parsedHtmlFragment, MultipartDto multipartDto) {
		
		String previewValue = multipartDto.getPreviewSize() + multipartDto.getPreviewSizeUnit().getUnit();
		
		Elements imgElements = parsedHtmlFragment.select("img");
//		Elements imgElements = parsedHtmlFragment.select("img[src]");
		if (imgElements.size() == 0) return;
		
		imgElements.forEach(img -> {
			//Standard attr (given from Locus as usual)
			if (img.hasAttr("width")) {
				img.attr("width", previewValue);
			}
			//GoogleEarth add "max-width" attribute in style
			if (img.hasAttr("style")) {
				setPreviewSizeInStyles(img, multipartDto.getPreviewSize(), multipartDto.getPreviewSizeUnit());
			} else {
				setStyleToElement(img, "width", previewValue);
			}
		});
		//No <img> with [src] attribures
		//Remake imgs into a links if they aren't.
		imgElements.stream()
			.filter(Node::hasParent)
			.filter(element -> !element.parent().tagName().equalsIgnoreCase("a"))
			.forEach(element -> element.replaceWith(getAElementWithInnerImgElement(element)));
		//Finally creates a new User description within <!-- desc_user:start --> ... <!-- desc_user:end -->
		// with User's text and images inside it.
		if (!multipartDto.isClearOutdatedDescriptions()) {
			//All is not clear and need to be placed within UserDescStartEnd comments
			clearOutdatedDescriptions(parsedHtmlFragment, multipartDto);
		}
	}
	
	/**
	 * Sets an additional "style" into that attribute for the given Element,
	 * e.g. <div style="width: 100%; max-width: 120%"></div>.
	 * If no "style" attribute presented it will be added.
	 *
	 * @param element Element
	 * @param key     The "style" attribute key
	 * @param value   The "style" attribute value
	 */
	private void setStyleToElement(Element element, String key, String value) {
		Map<String, String> stylesKeyMap = getStylesKeyMap(element);
		
		stylesKeyMap.put(key.trim(), value.trim());
		
		String newStyles = stylesKeyMap.entrySet().stream()
			.map(entry -> entry.getKey() + ": " + entry.getValue() + ";")
			.collect(Collectors.joining());
		element.attr("style", newStyles);
	}
	
	/**
	 * Sets the new preview size into inline "Style" attributes as "width", "max-width"
	 *
	 * @param imgElement  With obligatory "style" attribute
	 * @param previewSize Ready to use value, eg. "640px".
	 */
	private void setPreviewSizeInStyles(Element imgElement, int previewSize, PreviewSizeUnits sizeUnit) {
		String styleKeyValue = previewSize + sizeUnit.getUnit();
		Map<String, String> stylesKeyMap = getStylesKeyMap(imgElement);
		
		if (stylesKeyMap.containsKey("max-width")) {
			stylesKeyMap.put("max-width", styleKeyValue);
		} else {
			stylesKeyMap.put("width", styleKeyValue);
		}
		String newStyles = stylesKeyMap.entrySet().stream()
			.map(entry -> entry.getKey() + ":" + entry.getValue() + ";")
			.collect(Collectors.joining());
		imgElement.attr("style", newStyles);
	}
	
	/**
	 * @return Map of Element's "style" attribute as key-value pair. E.g. 'style="width: 200px; max-width: 250px"
	 * return "style" attributes as {@link HashMap} with key="width", value="200px" etc.
	 * Or empty {@link HashMap} if no "style' attribute presented.
	 */
	private Map<String, String> getStylesKeyMap(Element element) {
		Map<String, String> stylesKeyMap = new LinkedHashMap<>();
		String[] keys = element.attr("style").split(":|;");
		//attr() may return "" and split() will also return same as a single array value
		if (keys.length > 1) {
			for (int i = 0; i < keys.length; i += 2) {
				stylesKeyMap.put(keys[i].trim(), keys[i + 1].trim());
			}
		}
		return stylesKeyMap;
	}
	
	/**
	 * @return Trimmed String inline without redundant whitespaces and line breaks.
	 */
	private String trimDescriptions(Element parsedHtmlFragment) {
		//Deletes 2 or more whitespaces in a row
		return parsedHtmlFragment.html()
			.replaceAll("\\s{2,}", "").replaceAll("\\n", "").trim();
	}
	
	/**
	 * Removes all the unnecessary HTML nodes and data duplicates.
	 * MUST be the last method in a chain.
	 */
	private void clearOutdatedDescriptions(Element parsedHtmlFragment, MultipartDto multipartDto) {
		
		deleteImagesDuplicates(parsedHtmlFragment);
		
		Elements imgElements = parsedHtmlFragment.select("img[src]");
		String userDescriptionText = getUserDescriptionText(parsedHtmlFragment).trim();
		Element newHtmlDescription = createNewHtmlDescription(userDescriptionText, multipartDto);
		
		if (!imgElements.isEmpty()) {
			
			imgElements.forEach(imgElement -> {
				if (imgElement.hasAttr("align")) imgElement.attr("align", "center");
				if (imgElement.hasAttr("style")) setStyleToElement(imgElement, "border", "1px white solid");
			});
			
			Element tr = new Element("tr");
			Element tdWithImg = new Element("td")
				.attr("align", "center").attr("colspan", "2");
			tdWithImg.insertChildren(0, getAElementsWithInnerImgElement(imgElements));
			tr.appendChild(tdWithImg);
			newHtmlDescription.select("tbody").first().appendChild(tr);
//			newHtmlDescription.select("tbody").first().appendChild(getTableRowWithSeparator());
		}
		Elements tableRowWithMinDateTime = getTableRowsWithMinDateTime(parsedHtmlFragment.getAllElements());
		if (!tableRowWithMinDateTime.isEmpty()) {
			tableRowWithMinDateTime.forEach(tr -> newHtmlDescription.select("tbody").first().appendChild(tr));
			newHtmlDescription.select("tbody").first().appendChild(getTableRowWithSeparator());
		}
		clearEmptyTables(newHtmlDescription);
		parsedHtmlFragment.html(newHtmlDescription.outerHtml());
	}
	
	/**
	 * @param imgElement To be copied and inserted into <a></a>
	 * @return New <a></a> Element with the copy of given <img></img> Element
	 */
	private Element getAElementWithInnerImgElement(Element imgElement) {
		String src = imgElement.attr("src");
		Element newImgElement = new Element(imgElement.tagName());
		newImgElement.attributes().addAll(imgElement.attributes());
		return new Element("a").attr("href", src).attr("target", "_blank")
			.appendChild(newImgElement);
	}
	
	private Elements getAElementsWithInnerImgElement(Elements imgElements) {
		return imgElements.stream()
			.map(imgElement -> {
				String src = imgElement.attr("src");
				//As we will return new <a> Element we need to remove the old one from the DOM
				if (imgElement.hasParent()) {
					Element parent = imgElement.parent();
					if (parent.tagName().equals("a") || parent.tagName().equals("td")) parent.remove();
				}
				return new Element("a").attr("href", src).attr("target", "_blank")
					.appendChild(imgElement);
			})
			.collect(Collectors.toCollection(Elements::new));
	}
	
	/**
	 * For Locus Pro {@code <!-- desc_user:start -->} and {@code <!-- desc_user:end -->} are the markers
	 * for displaying all inner data and text on POI screen (description text, photo, photo data etc).
	 * So when {@link MultipartDto#getPreviewSize()}  != null} to display it on the screen these comments
	 * have to embrace all the description.
	 * Otherwise only description text will be visible.
	 *
	 * @return A {@code <div></div>} Element with a new table with tbody embraced with "desc_user" comments
	 * or just a new table with tbody for a data if {@link MultipartDto#getPreviewSize()} not set.
	 */
	private Element createNewHtmlDescription(String userDescription, MultipartDto multipartDto) {
		Element table = new Element("table")
			.attr("width", "100%").attr("style", "color:black");
		table.appendChild(new Element("tbody"));
		//'setPath' option for photos and any user description texts in Locus have to be within special comments
		if (multipartDto.getPreviewSize() != null || !userDescription.isBlank()) {
			String descUserStart = " desc_user:start ";
			String descUserEnd = " desc_user:end ";
			Element divElement = new Element("div").appendChild(new Comment(descUserStart));
			if (!userDescription.isBlank()) divElement.appendText(userDescription);
			return divElement.appendChild(table).appendChild(new Comment(descUserEnd));
		}
		return table;
	}
	
	
	/**
	 * In Locus a simple text with User's descriptions is placed between
	 * {@code <!-- desc_user:start -->} and {@code <!-- desc_user:end -->} xml comments (if any).
	 * In older versions it may contain HTML elements, so this method is intended to derive only text
	 * among all elements between these comments.
	 *
	 * @return Derived text between xml tags or empty String if nothing found.
	 */
	private String getUserDescriptionText(Element parsedHtmlFragment) {
		//This Comment has Parent with all the children for extracting text with User's descriptions
		final Comment[] commentNode = new Comment[1];//To be modified from within anonymous class or lambda
		parsedHtmlFragment.traverse(new NodeVisitor() {
			@Override
			public void head(Node node, int i) {
				if (node instanceof Comment && ((Comment) node).getData().contains("desc_user:start")) {
					commentNode[0] = (Comment) node;
				}
			}
			
			@Override
			public void tail(Node node, int i) {
			}
		});
		
		if (commentNode[0] == null) return "";
		
		List<Node> nodesWithinUserDescComments = commentNode[0].parent().childNodes();
		
		StringBuilder textUserDescription = new StringBuilder();
		
		for (int i = 0; i < nodesWithinUserDescComments.size(); i++) {
			if (nodesWithinUserDescComments.get(i) instanceof Comment &&
				((Comment) nodesWithinUserDescComments.get(i)).getData().contains("desc_user:start")) {
				//From here we iterate over further Elements...
				for (int j = i; j < nodesWithinUserDescComments.size(); j++) {
					if (nodesWithinUserDescComments.get(j) instanceof TextNode &&
						!((TextNode) nodesWithinUserDescComments.get(j)).getWholeText().isBlank()) {
						//Write out all non-blank text data
						textUserDescription.append(((TextNode) nodesWithinUserDescComments.get(j)).getWholeText());
						continue;
					}
					//... Until find the end marker
					if (nodesWithinUserDescComments.get(j) instanceof Comment &&
						((Comment) nodesWithinUserDescComments.get(j)).getData().contains("desc_user:end")) {
						return textUserDescription.toString();
					}
				}
			}
		}
		return "";
	}
	
	/**
	 * Filters td Elements with DateTime, gets the earliest, gets its Parent Node with all the table rows which contain
	 * the whole description of a POI.
	 *
	 * @return {@code new Elements("<tr>")} with the whole POI description for the earliest DateTime
	 * or empty {@link Elements} collection (.size() == 0)
	 */
	private Elements getTableRowsWithMinDateTime(Elements htmlElements) {
		Elements tdElementsWithDescription = htmlElements.select("td");
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		Element tdElementWithMinimumDateTime = tdElementsWithDescription.stream()
			.filter(Element::hasText)
			.filter(e -> {
				try {
					LocalDateTime.parse(e.text(), dateTimeFormatter);
					return true;
				} catch (DateTimeParseException ex) {
					return false;
				}
			})
			.min((e1, e2) -> {
				LocalDateTime dateTime1 =
					LocalDateTime.parse(e1.text(), dateTimeFormatter);
				LocalDateTime dateTime2 =
					LocalDateTime.parse(e2.text(), dateTimeFormatter);
				return dateTime1.compareTo(dateTime2);
			})
			.orElse(new Element("empty"));
		if (tdElementWithMinimumDateTime.hasParent()) {
			//<tr> is the first parent, <tbody> or <table> is the second which contains all the <tr> with descriptions
			return tdElementWithMinimumDateTime.parent().parent().children();
		} else {
			//No dateTime's found, return empty tag
			return new Elements();
		}
	}
	
	/**
	 * @return Just a tr with td with a hr )))
	 */
	private Element getTableRowWithSeparator() {
		Element tr = new Element("tr");
		Element td = new Element("td").attr("colspan", "2");
		td.appendChild(new Element("hr"));
		tr.appendChild(td);
		return tr; //Returns just <tr> with <td> with <hr> inside as a table rows separator
	}
	
	/**
	 * If every <td></td> within their <tr></tr> is empty those "tr" elements will be deleted from the DOM
	 */
	private void clearEmptyTables(Element table) {
		Elements tableRows = table.select("tr");
		tableRows.forEach(tr -> {
			Elements tdElements = tr.select("td");
			if (tdElements.stream().allMatch(td -> td.children().isEmpty())) tr.remove();
		});
	}
	
	/**
	 * Searches for <img> tags with same filenames in their "src" attributes and deletes them from DOM.
	 * If they're have parent nodes as <a href=""></a> - they will be deleted too.
	 */
	private void deleteImagesDuplicates(Element parsedHtmlFragment) {
		Elements imgElements = parsedHtmlFragment.select("img[src]");
		
		Set<String> fileNames = new HashSet<>(3);
		imgElements.forEach(img -> {
			String fileName = getFileName(img.attr("src"));
			if (fileNames.contains(fileName)) {
				if (img.hasParent() && img.parent().tagName().equals("a")) {
					img.parent().remove();
				} else {
					img.remove();
				}
			}
			fileNames.add(fileName);
		});
	}
	
	/**
	 * Safe comments that make sense for only Locus Pro. Other programs will ignore it.
	 */
	private void addStartEndComments(Element parsedHtmlFragment) {
		Comment desc_gen_start = new Comment(" desc_gen:start ");
		Comment desc_gen_end = new Comment(" desc_gen:end ");
		
		parsedHtmlFragment.filter(new NodeFilter() {
			@Override
			public FilterResult head(Node node, int depth) {
				if (node instanceof Comment &&
					(((Comment) node).getData().contains("desc_gen:start") ||
						((Comment) node).getData().contains("desc_gen:end"))) {
					return FilterResult.REMOVE;
				}
				return FilterResult.CONTINUE;
			}
			
			@Override
			public FilterResult tail(Node node, int depth) {
				return FilterResult.CONTINUE;
			}
		});
		parsedHtmlFragment.prependChild(desc_gen_start);
		parsedHtmlFragment.appendChild(desc_gen_end);
	}
}
