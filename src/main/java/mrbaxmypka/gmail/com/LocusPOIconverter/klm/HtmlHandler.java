package mrbaxmypka.gmail.com.LocusPOIconverter.klm;

import lombok.NoArgsConstructor;
import mrbaxmypka.gmail.com.LocusPOIconverter.entitiesDto.MultipartDto;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * HTML processing class based on {@link org.jsoup.Jsoup} library to parse CDATA as HTML.
 */
@NoArgsConstructor
@Component
public class HtmlHandler {
	
	/**
	 * @param htmlCdata    Only {@link String} with inner HTML markup
	 * @param multipartDto To determine all other conditions to be processed on CDATA HTML
	 * @return Fully processed HTML markup to be included in CDATA block.
	 */
	public String processCdata(String htmlCdata, MultipartDto multipartDto) {
		//Gets HTML <body> then all the included children and then the first one as a children's parent container
		Element parsedHtmlFragment = Jsoup.parseBodyFragment(htmlCdata).body().children().first();
		
		if (multipartDto.isClearDescriptions()) { //Should be the first treatment
			clearDescriptions(parsedHtmlFragment);
		}
		if (multipartDto.isSetPath()) {
			setPath(parsedHtmlFragment, multipartDto.getPath());
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
	private void setPath(Element parsedHtmlFragment, String path) {
		Elements aElements = parsedHtmlFragment.select("a[href]");
		Elements imgElements = parsedHtmlFragment.select("img[src]");
		
		final String href = path == null ? "" : path;
		
		aElements.stream()
			.filter(a -> !a.attr("href").startsWith("www.") && !a.attr("href").startsWith("http:"))
			.forEach((a) -> {
				String newPathWithFilename = getNewHrefWithOldFilename(a.attr("href"), href);
				a.attr("href", newPathWithFilename);
			});
		imgElements.stream()
			.filter(img -> !img.attr("src").startsWith("www.") && !img.attr("src").startsWith("http:"))
			.forEach((img) -> {
				String newPathWithFilename = getNewHrefWithOldFilename(img.attr("src"), href);
				img.attr("src", newPathWithFilename);
			});
	}
	
	/**
	 * Each existing a[href] contains a full path with the filename as the last text element.
	 * Here we have to replace only the URL and leave the original filename.
	 */
	private String getNewHrefWithOldFilename(String oldHrefWithFilename, String newHrefWithoutFilename) {
//		Each existing a[href] contains a full path with the filename as the last text element.
//		Here we have to replace only the URL and leave the original filename.
		if (!newHrefWithoutFilename.endsWith("/")) {
//			Every new href has to end with '/'
			newHrefWithoutFilename = newHrefWithoutFilename.concat("/");
		}
		int lastIndexOFSlash = oldHrefWithFilename.lastIndexOf("/");
		String filename = oldHrefWithFilename.substring(lastIndexOFSlash + 1, oldHrefWithFilename.length());
		
		return newHrefWithoutFilename.concat(filename);
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
	private void clearDescriptions(Element parsedHtmlFragment) {
		Elements aElements = parsedHtmlFragment.select("a[href]"); //Those <a> also include <img> or whatever else
		
		Element newHtmlDescription = createNewHtmlDescription();
		
		if (!aElements.isEmpty()) {
			//These are the descriptions set of a photos or another included attachments as table rows
			String parentText = aElements.first().parent().ownText() != null ? aElements.first().parent().ownText() : "";
			
			Element tr = new Element("tr");
			Element td = new Element("td").appendText(parentText)
				.attr("width", "100%").attr("align", "center");
			td.insertChildren(0, aElements);
			tr.appendChild(td);
			newHtmlDescription.select("tbody").first().appendChild(tr);
			newHtmlDescription.select("tbody").first().appendChild(getTableRowWithSeparator());
		}
		Elements tableRowWithDescAndDateTime = getTableRowsWithDescAndDateTime(parsedHtmlFragment.getAllElements());
		tableRowWithDescAndDateTime.forEach(tr -> newHtmlDescription.select("tbody").first().appendChild(tr));
		newHtmlDescription.select("tbody").first().appendChild(getTableRowWithSeparator());
		
		parsedHtmlFragment.html(newHtmlDescription.outerHtml());
	}
	
	/**
	 * @return Just a new table with tbody for a future description
	 */
	private Element createNewHtmlDescription() {
		Element table = new Element("table")
			.attr("width", "100%").attr("style", "color:black");
		table.appendChild(new Element("tbody"));
		return table;
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
		Element td = new Element("td").attr("colspan", "1");
		td.appendChild(new Element("hr"));
		tr.appendChild(td);
		return tr; //Returns just <tr> with <td> with <hr> inside as a table rows separator
	}
	
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
