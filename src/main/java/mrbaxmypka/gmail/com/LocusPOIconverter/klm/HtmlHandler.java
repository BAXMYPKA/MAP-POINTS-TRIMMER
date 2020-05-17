package mrbaxmypka.gmail.com.LocusPOIconverter.klm;

import lombok.NoArgsConstructor;
import mrbaxmypka.gmail.com.LocusPOIconverter.entitiesDto.MultipartDto;
import mrbaxmypka.gmail.com.LocusPOIconverter.utils.PathTypes;
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
import org.thymeleaf.expression.Lists;

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
	 * @param description  Receives inner text from <description>...</description> which in fact is the HTML markup
	 * @param multipartDto To determine all other conditions to be processed on CDATA HTML
	 * @return Fully processed HTML markup to be included in CDATA block.
	 */
	public String processCdata(String description, MultipartDto multipartDto) {
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
		//Should be the first treatment
		if (multipartDto.isClearOutdatedDescriptions()) {
			clearOutdatedDescriptions(parsedHtmlFragment, multipartDto);
		}
		if (multipartDto.isSetPath()) {
			setPath(parsedHtmlFragment, multipartDto.getPathType(), multipartDto.getPath());
		}
		if (multipartDto.isSetPreviewSize()) {
			Integer previewSize = multipartDto.getPreviewSize() == null ? 0 : multipartDto.getPreviewSize();
			setPreviewSize(parsedHtmlFragment, previewSize, multipartDto);
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
		/*
		parsedHtmlFragment.parent().filter(new NodeFilter() {
			@Override
			public FilterResult head(Node node, int depth) {
				if (node instanceof TextNode && !((TextNode) node).getWholeText().isBlank()) {
					plainTextDescription.append(((TextNode) node).getWholeText());
					return FilterResult.CONTINUE;
				}
				return FilterResult.CONTINUE;
			}
			
			@Override
			public FilterResult tail(Node node, int depth) {
				return FilterResult.CONTINUE;
			}
		});
*/
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
	private void setPath(Element parsedHtmlFragment, @Nullable PathTypes pathType, @Nullable String path) {
		
		Elements aElements = parsedHtmlFragment.select("a[href]");
		Elements imgElements = parsedHtmlFragment.select("img[src]");
		
		final PathTypes type = pathType == null ? PathTypes.RELATIVE : pathType;
		final String hrefPath = path == null ? "" : path;
		
		aElements.forEach((a) -> {
			String newPathWithFilename = getNewHrefWithOldFilename(a.attr("href"), type, hrefPath);
			a.attr("href", newPathWithFilename);
		});
		imgElements.forEach((img) -> {
			String newPathWithFilename = getNewHrefWithOldFilename(img.attr("src"), type, hrefPath);
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
		if (pathType == null) {
			throw new IllegalArgumentException("PathTypes cannot be null!");
		}
		String newHrefWithOldFilename;
		
		if (pathType.equals(PathTypes.RELATIVE)) {
			newHrefWithOldFilename = getNewRelativeHref(oldHrefWithFilename, newHrefWithoutFilename);
		} else if (pathType.equals(PathTypes.ABSOLUTE)) {
			newHrefWithOldFilename = getNewAbsoluteHref(oldHrefWithFilename, newHrefWithoutFilename);
		} else if (pathType.equals(PathTypes.WEB)) {
			newHrefWithOldFilename = getNewWebHref(oldHrefWithFilename, newHrefWithoutFilename);
		} else {
			throw new IllegalArgumentException("The PathTypes.getType cannot be recognized!");
		}
		return newHrefWithOldFilename;
	}
	
	private String getNewRelativeHref(String oldHrefWithFilename, String newHrefWithoutFilename) {
		newHrefWithoutFilename = trimNewHrefWithoutFilename(newHrefWithoutFilename);
		String filename = getFileName(oldHrefWithFilename);
		
		return newHrefWithoutFilename.concat(filename);
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
	 * @param oldHrefWithFilename Href or src to the image
	 * @return The name of the file from the given src or empty string if nothing found.
	 */
	String getFileName(String oldHrefWithFilename) {
		if (!oldHrefWithFilename.contains(".") ||
			(!oldHrefWithFilename.contains("/") && !oldHrefWithFilename.contains("\\"))) return "";
		int lastIndexOFSlash = oldHrefWithFilename.lastIndexOf("/") != -1 ?
			  oldHrefWithFilename.lastIndexOf("/") :
			  oldHrefWithFilename.lastIndexOf("\\");
		String filename =  oldHrefWithFilename.substring(lastIndexOFSlash + 1);
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
		newHrefWithoutFilename = newHrefWithoutFilename.replaceAll("\\s", "%20");
		
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
	 * So when {@link MultipartDto#isSetPreviewSize() = true} to display it on the screen these comments
	 * have to embrace all the description.
	 */
	private void setPreviewSize(Element parsedHtmlFragment, Integer previewSize, MultipartDto multipartDto) {
		Elements imgElements = parsedHtmlFragment.select("img[src]");
		if (imgElements.size() == 0) return;
		
		String previewSizeAttr = previewSize.toString() + "px";
		imgElements.forEach(img -> {
			if (img.hasAttr("width")) {
				img.attr("width", previewSizeAttr);
			}
			if (img.hasAttr("style")) {
				setPreviewSizeInStyles(img, previewSizeAttr);
			}
		});
		//No <img> with [src] attribures
		//Remake imgs into a links if they aren't.
		imgElements.stream()
			  .filter(element -> element.hasParent())
			  .filter(element -> !element.parent().tagName().equalsIgnoreCase("a"))
			  .forEach(element -> element.replaceWith(getAElementWithInnerImgElement(element)));
		//Finally creates a new User description within <!-- desc_user:start --> ... <!-- desc_user:end -->
		// with User's text and images inside it.
		clearOutdatedDescriptions(parsedHtmlFragment, multipartDto);
	}
	
	/**
	 * Sets the new preview size into inline "Style" attributes as "width", "max-width"
	 *
	 * @param imgElementWithStyles With obligatory "style" attribute
	 * @param previewSize          Ready to use value, eg. "640px".
	 */
	private void setPreviewSizeInStyles(Element imgElementWithStyles, String previewSize) {
		Map<String, String> stylesKeyMap = new LinkedHashMap<>();
		String[] keys = imgElementWithStyles.attr("style").split(":|;");
		
		if (keys.length > 0) {
			for (int i = 0; i < keys.length; i += 2) {
				stylesKeyMap.put(keys[i].trim(), keys[i + 1].trim());
			}
		}
		stylesKeyMap.entrySet().forEach(entry -> {
			if (entry.getKey().equalsIgnoreCase("width")) {
				entry.setValue(previewSize);
			}
			if (entry.getKey().equalsIgnoreCase("max-width")) {
				entry.setValue(previewSize);
			}
		});
		String newStyles = stylesKeyMap.entrySet().stream()
			  .map(entry -> entry.getKey() + ":" + entry.getValue() + ";")
			  .collect(Collectors.joining());
		imgElementWithStyles.attr("style", newStyles);
	}
	
	/**
	 * @return Trimmed String inline without redundant whitespaces and line breaks.
	 */
	private String trimDescriptions(Element parsedHtmlFragment) {
		//Deletes 2 or more whitespaces in a row
		String trimmedString = parsedHtmlFragment.html()
			  .replaceAll("\\s{2,}", "").replaceAll("\\n", "").trim();
		return trimmedString;
	}
	
	/**
	 * Removes all the unnecessary HTML nodes and data duplicates.
	 * MUST be the last method in a chain.
	 */
	private void clearOutdatedDescriptions(Element parsedHtmlFragment, MultipartDto multipartDto) {
		Elements imgElements = parsedHtmlFragment.select("img[src]");
		String userDescriptionText = getUserDescriptionText(parsedHtmlFragment).trim();
		Element newHtmlDescription = createNewHtmlDescription(userDescriptionText, multipartDto);
		
		if (!imgElements.isEmpty()) {
			Element tr = new Element("tr");
			Element td = new Element("td").attr("align", "center");
			td.insertChildren(0, getAElementsWithInnerImgElement(imgElements));
			tr.appendChild(td);
			newHtmlDescription.select("tbody").first().appendChild(tr);
			newHtmlDescription.select("tbody").first().appendChild(getTableRowWithSeparator());
		}
		
		Elements tableRowWithDescAndDateTime = getTableRowsWithDescAndDateTime(parsedHtmlFragment.getAllElements());
		if (!tableRowWithDescAndDateTime.isEmpty()) {
			tableRowWithDescAndDateTime.forEach(tr -> newHtmlDescription.select("tbody").first().appendChild(tr));
			newHtmlDescription.select("tbody").first().appendChild(getTableRowWithSeparator());
		}
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
				  return new Element("a").attr("href", src).attr("target", "_blank")
						.appendChild(imgElement);
			  })
			  .collect(Collectors.toCollection(Elements::new));
	}
	
	/**
	 * For Locus Pro {@code <!-- desc_user:start -->} and {@code <!-- desc_user:end -->} are the markers
	 * for displaying all inner data and text on POI screen (description text, photo, photo data etc).
	 * So when {@link MultipartDto#isSetPreviewSize() = true} to display it on the screen these comments
	 * have to embrace all the description.
	 * Otherwise only description text will be visible.
	 *
	 * @return A {@code <div></div>} Element with a new table with tbody embraced with "desc_user" comments
	 * or just a new table with tbody for a data if {@link MultipartDto#isSetPreviewSize()} not set.
	 */
	private Element createNewHtmlDescription(String userDescription, MultipartDto multipartDto) {
		Element table = new Element("table")
			  .attr("width", "100%").attr("style", "color:black");
		table.appendChild(new Element("tbody"));
		//'setPath' option for photos and any user description texts in Locus have to be within special comments
		if (multipartDto.isSetPreviewSize() || !userDescription.isBlank()) {
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
/*
		StringBuilder textUserDescription = new StringBuilder("");
		Elements allElements = parsedHtmlFragment.getAllElements();
		for (int i = 0; i < allElements.size(); i++){
			if (allElements.get(i).ownText().contains("desc_user:start")) {
				//From here we iterate over further Elements...
				for (int j = i; j < allElements.size(); j++) {
					if (allElements.get(j).hasText()) {
						//Write out all non-blank text data
						textUserDescription.append(allElements.get(j).ownText());
						continue;
					}
					//... Until find the end marker
					if (allElements.get(j).ownText().contains("desc_user:end")) {
						return textUserDescription.toString();
					}
				}
			}
			
		}
*/
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
		
		StringBuilder textUserDescription = new StringBuilder("");
		
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
	private Elements getTableRowsWithDescAndDateTime(Elements htmlElements) {
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
