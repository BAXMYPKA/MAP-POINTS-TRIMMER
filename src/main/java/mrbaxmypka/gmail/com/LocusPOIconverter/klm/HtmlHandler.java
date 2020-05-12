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
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * HTML processing class based on {@link org.jsoup.Jsoup} library to parse CDATA as HTML.
 */
@NoArgsConstructor
@Component
public class HtmlHandler {
	
	/**
	 * @param htmlCdata    Receives inner text from CDATA which in fact is the HTML markup
	 * @param multipartDto To determine all other conditions to be processed on CDATA HTML
	 * @return Fully processed HTML markup to be included in CDATA block.
	 */
	public String processCdata(String htmlCdata, MultipartDto multipartDto) {
		//Gets HTML <body> then all the included children and then the first one as a children's parent container
		Element parsedHtmlFragment = Jsoup.parseBodyFragment(htmlCdata).body().children().first();
		
		if (multipartDto.isClearOutdatedDescriptions()) { //Should be the first treatment
			clearOutdatedDescriptions(parsedHtmlFragment, multipartDto);
		}
		if (multipartDto.isSetPath()) {
			setPath(parsedHtmlFragment, multipartDto.getPathType(), multipartDto.getPath());
		}
		if (multipartDto.isSetPreviewSize()) {
			Integer previewSize = multipartDto.getPreviewSize() == null ? 0 : multipartDto.getPreviewSize();
			setPreviewSize(parsedHtmlFragment, previewSize);
		}
		addStartEndComments(parsedHtmlFragment);
		
		// MUST be the last treatment in all the conditions chain
		if (multipartDto.isTrimDescriptions()) {
			return trimDescriptions(parsedHtmlFragment);
		}
		return parsedHtmlFragment.html();
	}
	
	
	/**
	 * Sets new local or remote paths instead old ones.
	 * I.e. old path {@code <a href="files:/_1404638472855.jpg"></>}
	 * can be replaced with {@code <a href="C:/files:/a new path/_1404638472855.jpg"></>}
	 */
	private void setPath(Element parsedHtmlFragment, PathTypes pathType, String path) {
		Elements aElements = parsedHtmlFragment.select("a[href]");
		Elements imgElements = parsedHtmlFragment.select("img[src]");
		
		final String href = path == null ? "" : path;
		
		aElements.stream()
			  .filter(a -> !a.attr("href").startsWith("www.") && !a.attr("href").startsWith("http:"))
			  .forEach((a) -> {
				  String newPathWithFilename = getNewHrefWithOldFilename(a.attr("href"), pathType, href);
				  a.attr("href", newPathWithFilename);
			  });
		imgElements.stream()
			  .filter(img -> !img.attr("src").startsWith("www.") && !img.attr("src").startsWith("http:"))
			  .forEach((img) -> {
				  String newPathWithFilename = getNewHrefWithOldFilename(img.attr("src"), pathType, href);
				  img.attr("src", newPathWithFilename);
			  });
	}
	
	/**
	 * Each existing a[href] contains a full path with the filename as the last text element.
	 * Here we have to replace only the URL and leave the original filename.
	 */
	private String getNewHrefWithOldFilename(
		  String oldHrefWithFilename, PathTypes pathType, String newHrefWithoutFilename) {
		
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
	
	private String getFileName(String oldHrefWithFilename) {
		int lastIndexOFSlash = oldHrefWithFilename.lastIndexOf("/") != -1 ?
			  oldHrefWithFilename.lastIndexOf("/") :
			  oldHrefWithFilename.lastIndexOf("\\");
		return oldHrefWithFilename.substring(lastIndexOFSlash + 1);
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
	
	private void setPreviewSize(Element parsedHtmlFragment, Integer previewSize) {
		Elements imgElements = parsedHtmlFragment.select("img[width]");
		imgElements.forEach(img -> img.attr("width", previewSize.toString() + "px"));
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
//		Elements aElements = parsedHtmlFragment.select("a[href]"); //Those <a> also include <img> or whatever else
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

/*
		if (!aElements.isEmpty()) {
			//These are the descriptions set of a photos or another included attachments as table rows
			String parentText = aElements.first().parent().ownText() != null ? aElements.first().parent().ownText() : "";
			
			Element tr = new Element("tr");
			Element td = new Element("td").appendText(parentText)
//				.attr("width", "100%")
				.attr("align", "center");
			td.insertChildren(0, aElements);
			tr.appendChild(td);
			newHtmlDescription.select("tbody").first().appendChild(tr);
			newHtmlDescription.select("tbody").first().appendChild(getTableRowWithSeparator());
		}
*/
		Elements tableRowWithDescAndDateTime = getTableRowsWithDescAndDateTime(parsedHtmlFragment.getAllElements());
		tableRowWithDescAndDateTime.forEach(tr -> newHtmlDescription.select("tbody").first().appendChild(tr));
		newHtmlDescription.select("tbody").first().appendChild(getTableRowWithSeparator());
		
		parsedHtmlFragment.html(newHtmlDescription.outerHtml());
	}
	
	private Elements getAElementsWithInnerImgElement(Elements imgElements) {
		return imgElements.stream()
			  .map(element -> {
				  String src = element.attr("src");
				  return new Element("a").appendChild(element)
						.attr("href", src).attr("target", "_blank");
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
	 * @return A {@code <div></div>} Element with a new table with tbody embraced with comments
	 * or just a new table with tbody for a data.
	 */
	private Element createNewHtmlDescription(String userDescription, MultipartDto multipartDto) {
		Element table = new Element("table")
			  .attr("width", "100%").attr("style", "color:black");
		table.appendChild(new Element("tbody"));
		
		if (multipartDto.isSetPath() || !userDescription.isBlank()) {
			String descUserStart = "desc_user:start";
			String descUserEnd = "desc_user:end";
			Element divElement = new Element("div");
			if (!userDescription.isBlank()) divElement.appendText(userDescription);
			return divElement
				  .appendChild(new Comment(descUserStart)).appendChild(table).appendChild(new Comment(descUserEnd));
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
		List<Node> nodes = parsedHtmlFragment.childNodes(); //Nodes are only for convenient finding for Comments elements
		StringBuilder textUserDescription = new StringBuilder("");
		for (int i = 0; i < nodes.size(); i++) {
			if (nodes.get(i) instanceof Comment && ((Comment) nodes.get(i)).getData().contains("desc_user:start")) {
				//From here we iterate over further Elements...
				for (int j = i; j < nodes.size(); j++) {
					if (nodes.get(j) instanceof TextNode && !((TextNode) nodes.get(j)).getWholeText().isBlank()) {
						//Write out all non-blank text data
						textUserDescription.append(((TextNode) nodes.get(j)).getWholeText());
						continue;
					}
					//... Until find the end marker
					if (nodes.get(j) instanceof Comment && ((Comment) nodes.get(i)).getData().contains("desc_user:end")) {
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
	 * @return {@code new Elements("<tr>")} with the whole POI description for the earliest DateTime.
	 */
	private Elements getTableRowsWithDescAndDateTime(Elements htmlElements) {
		Elements tdElementsWithDescription = htmlElements.select("td[align],[valign]");
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
			  .orElse(new Element("td").text(LocalDateTime.now().format(dateTimeFormatter)));
		
		if (tdElementWithMinimumDateTime.hasParent()) {
			//<tr> is the first parent, <tbody> or <table> is the second which contains all the <tr> with descriptions
			return tdElementWithMinimumDateTime.parent().parent().children();
		} else {
			return new Elements(new Element("tr").appendChild(tdElementWithMinimumDateTime));
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
		Comment desc_gen_start = new Comment("desc_gen:start");
		Comment desc_gen_end = new Comment("desc_gen:end");
		
		if (!parsedHtmlFragment.html().startsWith(desc_gen_start.getData())) {
			parsedHtmlFragment.prependChild(new Comment("desc_gen:start"));
		}
		if (!parsedHtmlFragment.html().endsWith(desc_gen_end.getData())) {
			parsedHtmlFragment.appendChild(new Comment("desc_gen:end"));
		}
	}
}
