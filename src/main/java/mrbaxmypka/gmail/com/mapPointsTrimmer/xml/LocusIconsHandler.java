package mrbaxmypka.gmail.com.mapPointsTrimmer.xml;

import lombok.extern.slf4j.Slf4j;
import mrbaxmypka.gmail.com.mapPointsTrimmer.entitiesDto.MultipartMainDto;
import mrbaxmypka.gmail.com.mapPointsTrimmer.services.FileService;
import mrbaxmypka.gmail.com.mapPointsTrimmer.utils.DownloadAs;
import org.springframework.lang.NonNull;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Slf4j
class LocusIconsHandler {
	
	private FileService fileService;
	private KmlUtils kmlUtils;
	
	public LocusIconsHandler(FileService fileService, KmlUtils kmlUtils) {
		this.fileService = fileService;
		this.kmlUtils = kmlUtils;
	}
	
	/**
	 * 1. {@literal Finds all the <StyleMap>s with "id" references to photo thumbnails}
	 * 2. {@literal If a <StyleMap> with a previously created "id" to the current } {@link MultipartMainDto#getPictogramName()} exists it:
	 * 2.1 {@literal Replaces all the <Placemark>s <styleUrl>s with that existing <StyleMap>}
	 * 3. {@literal If no <StyleMap> for that} {@link MultipartMainDto#getPictogramName()} exists it:
	 * 3.1 {@literal It gets a first <StyleMap> as an example, clones it and its 'normal' and 'highlight' <Style>s}
	 * 3.2 {@literal Creates a new <StyleMap> and its 'normal' and 'highlight' inner <Style>s based on the clone.}
	 * 3.3 {@literal Replaces all the <Placemark>s <styleUrl>s with that new cloned <StyleMap>}
	 * 4. {@literal Deletes all the <StyleMap>s from the Document with "id" to photo thumbnails.
	 * As well as their included "Normal" and "Highlighted" <Style>s}
	 * 5. {@literal Finds all the <Style>s with "id" references to photo thumbnails}
	 * 6. {@literal If <Style> with 'id' as a such a pictogram name as:} {@link MultipartMainDto#getPictogramName()} exists it:
	 * 6.1 {@literal Replaces all the <Placemark>s <styleUrl>s to thumbnail with that existing <Style>}
	 * 7. {@literal Deletes all the <Style>s with thumbnail 'id' attributes}
	 * 8. {@literal If no <Style> for that} {@link MultipartMainDto#getPictogramName()} exists it:
	 * 8.1 {@literal It gets a first <Style> as an example and clones it}
	 * 8.2 {@literal Creates a new <Style> and new href to icon based on the clone.}
	 * 8.3 {@literal Replaces all the <Placemark>s <styleUrl>s with that new cloned <StyleMap>}
	 * 9. {@literal Deletes all the <Style>s with thumbnail 'id' attributes}
	 * 10. If {@link MultipartMainDto#getDownloadAs()} == {@link DownloadAs#KMZ} all the previously deleted photo icons should be excluded from the resultant .zip(.kmz) file.
	 *
	 * @param multipartMainDto
	 */
	void replaceLocusIcons(@NonNull MultipartMainDto multipartMainDto) {
		String pictogramName = multipartMainDto.getPictogramName();
		if (pictogramName == null || pictogramName.isBlank()) {
			log.warn("Pictogram name is null or empty!");
			return;
		}
		if (!fileService.getPictogramsNames().contains(pictogramName)) {
			log.warn("Such a pictogram={} is not presented in 'resources/static/pictograms' directory!", pictogramName);
			return;
		}
		Map<String, Node> replacedStyleMaps = replaceStyleMapsInStyleUrls(pictogramName);
		//After deleting from the Document photo icons should not be included into the resultant .zip(.kmz)
		excludeFilesFromZip(replacedStyleMaps, multipartMainDto);
		deleteStyleObjectsFromDocument(replacedStyleMaps);
		//Refresh the current Nodes from Document
		kmlUtils.refreshStyleObjectsMap();
		kmlUtils.refreshStyleUrlsFromPlacemarks();
		
		Map<String, Node> replacedStyles = replaceStylesInStyleUrls(pictogramName);
		//After deleting from the Document photo icons should not be included into the resultant .zip(.kmz)
		excludeFilesFromZip(replacedStyles, multipartMainDto);
		deleteStyleObjectsFromDocument(replacedStyles);
		//Refresh the current Nodes from Document
		kmlUtils.refreshStyleObjectsMap();
		kmlUtils.refreshStyleUrlsFromPlacemarks();
	}
	
	/**
	 * {@literal All the <styleUrl>s pointers in <Placemark>s with the <Style>
	 * (with existing or newly created one) where the 'id' attribute is corresponding to }{@link MultipartMainDto#getPictogramName()}
	 * {@literal  and <IconHref> is updated as well.}
	 * Its better to execute this method after replacing StyleMaps in {@link #replaceStyleMapsInStyleUrls(String)}.
	 *
	 * @param pictogramName A replacing filename of the new pictogram.png
	 * @return {@literal Map<String ('id'),Node(Style)> where <Style>s Nodes can by safely deleted from the }{@link Document}
	 */
	private Map<String, Node> replaceStylesInStyleUrls(String pictogramName) {
		Map<String, Node> stylesWithThumbnails = kmlUtils.getStyleObjectsMap().entrySet().stream()
				.filter(entry -> entry.getValue().getNodeName().equalsIgnoreCase("Style"))
				.filter(entry -> isLocusPhotoIconThumbnail(entry.getValue()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		//There is no <StyleMap>s with the photo thumbnails
		if (stylesWithThumbnails.isEmpty()) return stylesWithThumbnails;
		
		if (!replacePlacemarksStyleUrlsWithExistingPicStyle(pictogramName, stylesWithThumbnails)) {
			replacePlacemarksStyleUrlsWithNewPicStyle(pictogramName, stylesWithThumbnails);
		}
		return stylesWithThumbnails;
	}
	
	/**
	 * @param pictogramName        {@link MultipartMainDto#getPictogramName()}.
	 *                             Also it is a standard styleUrl to an existing pictogram as just a full pictogram name
	 * @param stylesWithThumbnails {@literal Filtered <StyleMap>s whith 'id's indicating the photo thumbnail}.
	 *                             key=Style id
	 *                             value=Style Node
	 * @return True if the current {@link Document} contains {@literal a previously created <Style> with such a pictogram name
	 * and all the <Placemark>s <styleUrl>s have been replaced with it.
	 * False if no previously created <Style> exists and no replacement has been done.}
	 */
	private boolean replacePlacemarksStyleUrlsWithExistingPicStyle(String pictogramName, Map<String, Node> stylesWithThumbnails) {
		AtomicBoolean isReplacedWithExisting = new AtomicBoolean(false);
		//If KML contains the previously created <Style> with this pictogram name.
		//All the <Placemark>s <styleUrl/>s with a photo thumbnails will be redirected to it
		kmlUtils.getStyleObjectsMap().values().stream()
				.filter(styleObjectNode -> styleObjectNode.getNodeName().equals("Style"))
				.filter(styleNode -> styleNode.getAttributes().getNamedItem("id").getTextContent().equals(pictogramName))
				.findFirst()
				.ifPresent(existingStyleNode -> {
					replacePlacemarksStyleUrls(stylesWithThumbnails, pictogramName);
					isReplacedWithExisting.set(true);
				});
		return isReplacedWithExisting.get();
	}
	
	/**
	 * Creates a new cloned 'Style' based on any existing 'Style'. With a new 'id' as just a pictogram name attribute
	 * and a new 'href'
	 *
	 * @param pictogramName
	 * @param stylesWithThumbnails
	 */
	private void replacePlacemarksStyleUrlsWithNewPicStyle(String pictogramName, Map<String, Node> stylesWithThumbnails) {
		//No previously created Style with this pictogramName. Have to create a new one
		//Based on a template settings from any existing <Style> as a clone. And replace Placemarks styleUrls
		Node clonedStyle = cloneStyleObject(stylesWithThumbnails.values());
		Node updatedStyle = updateStyle(clonedStyle, pictogramName);
		Node insertedStyle = kmlUtils.insertIntoDocument(updatedStyle);
		replacePlacemarksStyleUrls(stylesWithThumbnails, insertedStyle.getAttributes().getNamedItem("id").getTextContent());
	}
	
	/**
	 * @param pictogramName To be included as an 'id' attribute
	 * @return {@literal An updated clone of the <Style> with all the included <IconStyle>s }
	 */
	private Node updateStyle(Node clonedStyle, String pictogramName) {
		clonedStyle.getAttributes().getNamedItem("id").setTextContent(pictogramName);
		Node clonedStyleIconHrefNode = kmlUtils.getIconHrefNode(clonedStyle);
		
		String originalPathWithFilename = kmlUtils.getIconHrefNode(clonedStyle).getTextContent();
		String originalPath = fileService.getPath(originalPathWithFilename);
		
		if (originalPath.isBlank()) { //No previous path found
			clonedStyleIconHrefNode.setTextContent(kmlUtils.getKML_FILES_DEFAULT_DIRECTORY() + pictogramName);
			log.warn("No path from a previous 'Icon href' found! The default path to a pictogram is applied!" +
					"Style Node from cloned Style has been updated.");
		} else {
			clonedStyleIconHrefNode.setTextContent(originalPath + pictogramName);
			log.trace("Style Node from cloned Style has been updated.");
		}
		return clonedStyle;
	}
	
	/**
	 * {@literal All the <styleUrl>s pointers in <Placemark>s with the <StyleMaps>
	 * (with existing or newly created one) where the 'id' attribute is corresponding to }{@link MultipartMainDto#getPictogramName()}
	 * {@literal  and <IconHref>s in 'normal' and 'highlight' inner <Style>s are updated as well.}
	 * Its better to execute this method before replacing Styles.
	 *
	 * @param pictogramName
	 * @return {@literal Map<String,Node(StyleMap)> where <StyleMap>s Nodes can by safely deleted from the }{@link Document}
	 */
	private Map<String, Node> replaceStyleMapsInStyleUrls(String pictogramName) {
		Map<String, Node> styleMapsWithThumbnails = kmlUtils.getStyleObjectsMap().entrySet().stream()
				.filter(entry -> entry.getValue().getNodeName().equalsIgnoreCase("StyleMap"))
				.filter(entry -> isLocusPhotoIconThumbnail(entry.getValue()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		//There is no <StyleMap>s with the photo thumbnails
		if (styleMapsWithThumbnails.isEmpty()) return styleMapsWithThumbnails;
		
		if (!replacePlacemarksStyleUrlsWithExistingPicStyleMap(pictogramName, styleMapsWithThumbnails)) {
			replacePlacemarksStyleUrlsWithNewPictogramStyleMap(pictogramName, styleMapsWithThumbnails);
		}
		return styleMapsWithThumbnails;
	}
	
	/**
	 * @param pictogramName           {@link MultipartMainDto#getPictogramName()}
	 * @param styleMapsWithThumbnails {@literal Filtered <StyleMap>s whith 'id's indicating the photo thumbnail}
	 * @return True if the current {@link Document} contains {@literal a previously created <StyleMap> with such a pictogram name
	 * and all the <Placemark>s <styleUrl>s have been replaced with it.
	 * False if no previously created <StyleMap> exists and no replacement has been done.}
	 */
	private boolean replacePlacemarksStyleUrlsWithExistingPicStyleMap(String pictogramName, Map<String, Node> styleMapsWithThumbnails) {
		AtomicBoolean isReplacedWithExisting = new AtomicBoolean(false);
		//Standard styleUrl to an existing pictogram as "styleMapOf:"+pictogram name
		String existingStyleUrl = kmlUtils.getSTYLEMAP_ID_ATTRIBUTE_PREFIX() + pictogramName;
		//If KML contains the previously created <MapStyle> with this pictogram name.
		//All the <Placemark>s <styleUrl/>s with a photo thumbnails will be redirected to it
		kmlUtils.getStyleObjectsMap().values().stream()
				.filter(styleObjectNode -> styleObjectNode.getNodeName().equals("StyleMap"))
				.filter(styleMapNode -> styleMapNode.getAttributes().getNamedItem("id").getTextContent().equals(existingStyleUrl))
				.findFirst()
				.ifPresent(existingStyleMapNode -> {
					replacePlacemarksStyleUrls(styleMapsWithThumbnails, existingStyleUrl);
					isReplacedWithExisting.set(true);
				});
		return isReplacedWithExisting.get();
	}
	
	/**
	 * {@literal Creates a new <StyleMap> with inner 'normal' and 'highlight' <Style>s as a clone of existing one.
	 * Updates them ids and hrefs. Insert in into the }{@link Document} {@literal and finally replaces all the <style<Url>s
	 * in <Placemark>s with this new <StyleMap>.}
	 *
	 * @param pictogramName
	 * @param styleMapsWithThumbnails
	 */
	private void replacePlacemarksStyleUrlsWithNewPictogramStyleMap(String pictogramName, Map<String, Node> styleMapsWithThumbnails) {
		//Standard <StyleMap> 'id' attribute to an existing pictogram as "styleMapOf:"+pictogram name
		String pictogramStyleMapId = kmlUtils.getSTYLEMAP_ID_ATTRIBUTE_PREFIX() + pictogramName;
		//No previously created StyleMap with this pictogramName. Have to create a new one with the new <Style>s
		//Based on a template settings from any existing <StyleMap> as a clone. And replace Placemarks styleUrls
		Node clonedStyleMap = cloneStyleObject(styleMapsWithThumbnails.values());
		Node updatedStyleMap = updateStyleMap(clonedStyleMap, pictogramName);
		kmlUtils.insertIntoDocument(updatedStyleMap);
		replacePlacemarksStyleUrls(styleMapsWithThumbnails, pictogramStyleMapId);
	}
	
	/**
	 * {@literal Replaces existing <styleUrl>s from <Placemakr>s with a given new 'newStyleUrl' within the Document.}
	 *
	 * @param styleObjectsWithThumbnails {@literal <StyleMap>s or <Style>s with ONLY the photo thumbnail 'id' attribute.
	 *                                   All the <Placemark>s referencing to those 'id's will be collected by it.}
	 * @param newStyleUrl                {@literal It will replace all <Placemark>s <styleUrl>s.}
	 */
	private void replacePlacemarksStyleUrls(Map<String, Node> styleObjectsWithThumbnails, String newStyleUrl) {
		//<Placemark>s referring to <StyleMap>s or <Style>s with photo thumbnails
		List<String> thumbnailsStyleUrlsFromPlacemarks = kmlUtils.getStyleUrlsFromPlacemarks();
		thumbnailsStyleUrlsFromPlacemarks.retainAll(styleObjectsWithThumbnails.keySet());
		
		thumbnailsStyleUrlsFromPlacemarks.forEach(thumbnailsStyleUrls -> {
			kmlUtils.getPlacemarksByStyleUrl(thumbnailsStyleUrls)
					.forEach(placemarkNode -> kmlUtils.setStyleUrlToPlacemark(placemarkNode, newStyleUrl));
		});
	}
	
	/**
	 * @return Just a deeply cloned example for using {@literal an old <Style> or <StyleMap> settings for the new one}
	 */
	private Node cloneStyleObject(@NonNull Collection<Node> styleObjectsWithThumbnails) {
		Node styleObjectExample = styleObjectsWithThumbnails.iterator().next();
		return styleObjectExample.cloneNode(true);
	}
	
	/**
	 * @param pictogramName
	 * @return {@literal An updated clone of the <StyleMap> with all the included Style <Pair>s (normal and highlight) as well}
	 */
	private Node updateStyleMap(Node clonedStyleMap, String pictogramName) {
		updateNormalStyleNode(clonedStyleMap, pictogramName);
		updateHighlightStyleNode(clonedStyleMap, pictogramName);
		updateClonedStyleMap(clonedStyleMap, pictogramName);
		return clonedStyleMap;
	}
	
	/**
	 * {@literal Finds 'normal' <Style> from a given cloned <StyleMap> and turn it into the correct Style with a pictogram}
	 *
	 * @param clonedStyleMapNode {@literal A clone of <StyleMap> whose normal and highlight <Pair>s are points to a photo <Style>}
	 * @param pictogramName
	 * @return {@literal The corrected <Style> with a pictogram to be used as a 'normal' on in a <Pair> of a given 'clonedStyleMapNode'}
	 */
	private Node updateNormalStyleNode(Node clonedStyleMapNode, String pictogramName) {
		Node clonedNormalStyleNode = kmlUtils.getNormalStyleNode(clonedStyleMapNode)
				.orElseThrow(() -> new IllegalArgumentException(
						"A not valid xml (kml) as there is no 'NormalStyle' from 'StyleMap' within a Document!"))
				.cloneNode(true);
		clonedNormalStyleNode.getAttributes().getNamedItem("id").setTextContent(pictogramName);
		Node clonedNormalStyleIconHrefNode = kmlUtils.getIconHrefNode(clonedNormalStyleNode);
		String originalPathWithFilename = clonedNormalStyleIconHrefNode.getTextContent();
		String originalPath = fileService.getPath(originalPathWithFilename);
		if (originalPath.isBlank()) { //No previous path found
			clonedNormalStyleIconHrefNode.setTextContent(kmlUtils.getKML_FILES_DEFAULT_DIRECTORY() + pictogramName);
			log.warn("No path from a previous 'Icon href' found! The default path to a pictogram is applied!" +
					"Normal Style Node from cloned StyleMap has been updated.");
		} else {
			clonedNormalStyleIconHrefNode.setTextContent(originalPath + pictogramName);
			log.trace("Normal Style Node from cloned StyleMap has been updated.");
		}
		kmlUtils.insertIntoDocument(clonedNormalStyleNode);
		log.trace("Normal Style Node from cloned StyleMap has been cloned, updated and inserted into the Document.");
		return clonedNormalStyleNode;
	}
	
	private Node updateHighlightStyleNode(Node clonedStyleMapNode, String pictogramName) {
		Node clonedHighStyleNode = kmlUtils.getHighlightStyleNode(clonedStyleMapNode)
				.orElseThrow(() -> new IllegalArgumentException(
						"A not valid xml (kml) document! There is no 'HighlightStyle' inside 'StyleMap'!"))
				.cloneNode(true);
		clonedHighStyleNode.getAttributes().getNamedItem("id")
				.setTextContent(kmlUtils.getHIGHLIGHT_STYLE_ID_ATTRIBUTE_PREFIX() + pictogramName);
		Node clonedHighStyleIconHrefNode = kmlUtils.getIconHrefNode(clonedHighStyleNode);
		String originalPathWithFilename = clonedHighStyleIconHrefNode.getTextContent();
		String originalPath = fileService.getPath(originalPathWithFilename);
		if (originalPath.isBlank()) { //No previous path found
			clonedHighStyleIconHrefNode.setTextContent(kmlUtils.getKML_FILES_DEFAULT_DIRECTORY() + pictogramName);
			log.warn("No path from a previous 'Icon href' found! The default path to a pictogram is applied!" +
					"Highlight Style Node from cloned StyleMap has been updated.");
		} else {
			clonedHighStyleIconHrefNode.setTextContent(originalPath + pictogramName);
			log.trace("Highlight Style Node from cloned StyleMap has been updated.");
		}
		kmlUtils.insertIntoDocument(clonedHighStyleNode);
		log.trace("Highlight Style Node from cloned StyleMap has been cloned, updated and inserted into the Document.");
		return clonedHighStyleNode;
	}
	
	private void updateClonedStyleMap(Node clonedStyleMapNode, String pictogramName) {
		clonedStyleMapNode.getAttributes().getNamedItem("id")
				.setTextContent(kmlUtils.getSTYLEMAP_ID_ATTRIBUTE_PREFIX() + pictogramName);
		
		kmlUtils.getStyleUrlNodeFromNormalStylePair(clonedStyleMapNode).setTextContent("#" + pictogramName);
		kmlUtils.getStyleUrlNodeFromHighlightedStylePair(clonedStyleMapNode).setTextContent(
				"#" + kmlUtils.getHIGHLIGHT_STYLE_ID_ATTRIBUTE_PREFIX() + pictogramName);
	}
	
	/**
	 * When all the Placemarks now referring to the existing StyleMap of Style with a previously created id to pictogramName
	 * We can safely delete StyleMaps and their Styles (or just Styles) with icon thumbnails
	 *
	 * @param styleObjectsWithThumbnails Where {@link Node} is StyleMap or Style with 'id' attribute references to a photo thumbnail
	 */
	private void deleteStyleObjectsFromDocument(Map<String, Node> styleObjectsWithThumbnails) {
		styleObjectsWithThumbnails.values().forEach(styleObject -> {
			if (styleObject.getNodeName().equalsIgnoreCase("StyleMap")) {
				kmlUtils.getNormalStyleNode(styleObject)
						.ifPresent(normalStyle -> normalStyle.getParentNode().removeChild(normalStyle));
				kmlUtils.getHighlightStyleNode(styleObject)
						.ifPresent(highlightStyle -> highlightStyle.getParentNode().removeChild(highlightStyle));
			}
			styleObject.getParentNode().removeChild(styleObject);
		});
	}
	
	/**
	 * Adds .png photo icons names into the {@link MultipartMainDto#getFilesToBeExcluded()}.
	 *
	 * @param replacedStyleObjects {@literal <Style>s or <StyleMap>'s "normal" <Style>s which <href>s to photo icons are already deleted from the Document.
	 *                             So the .png themselves should be deleted from a resultant .zip (.kmz) file.}
	 */
	private void excludeFilesFromZip(Map<String, Node> replacedStyleObjects, MultipartMainDto multipartMainDto) {
		if (!DownloadAs.KMZ.equals(multipartMainDto.getDownloadAs())) return;
		replacedStyleObjects.values()
				.forEach(styleObject -> {
					if (styleObject.getNodeName().equals("Style")) { //It's the <Style> Node
						String href = kmlUtils.getIconHrefNode(styleObject).getTextContent();
						String fileName = fileService.getFileName(href);
						multipartMainDto.getFilesToBeExcluded().add(fileName);
					} else { //It's the <StyleMap> Node
						Node normalStyle = kmlUtils.getNormalStyleNode(styleObject).orElseThrow(
								() -> new IllegalArgumentException("StyleMap object has to contain normal Style!"));
						String href = kmlUtils.getIconHrefNode(normalStyle).getTextContent();
						String fileName = fileService.getFileName(href);
						multipartMainDto.getFilesToBeExcluded().add(fileName);
					}
				});
	}
	
	/**
	 * In my case full names of the vast majority of icons with photos are like 'file:///sdcard/Locus/cache/images/123456789.png',
	 * and the 'Style id='' attribute is the full path above except the extension, 'file:///sdcard/Locus/cache/images/123456789'.
	 * 1) {@link #isLocusPhotoId(String)} So the basic match of 'Style id=""' has to start with 'file:///', contain a Locus-autogenerated part of a full path like '/Locus/cache/images/'
	 * and end with a filename with a minimum of 4 signs.
	 * 1.1) {@link #isLocusPhotoId(String)} This app generates a special prefix for 'StyleMap id=styleMapOf:file///[...]'. So the next math has to start with 'styleMapOf:file:///[...]'
	 * Also some versions of Locus Map generate icons with only digit names started with a hyphen like "-1245953029.png" and with appropriate 'Style id="-1245953029"',
	 * where digits number is from 7 to 10.
	 * 2) {@link #isLocusPhotoIconHref(String)} So the next specific match is better to check only by 'href' where href has to start with any path
	 * then a hyphen "-" and then only from 7 to 10 digits and end with ".png" in low or high case.
	 * Like "files/-1245953029.png" or "C:/MyFiles/photos/-1245953029.PNG".
	 * 2.1) {@link #isLocusPhotoIconHref(String)} An additional check for 'href' to contain  [any_path][any_prefix]locuscacheimages[any_filename_postfix][.png]. It must return true for the photo icon.
	 * And the final versions of Locus Map generates just a digital filename for a .png icon from 7 to 10 digits as "1801528832.png".
	 * 4) The last specific match also has to be checked by a 'href' full path which has to start with any path ended with '/'and 7 to 10 digits filename with '.png' extension, e.g. "files/12345678.png"
	 *
	 * @return 'True' if the given StyleObject icon is an individual photo icon thumbnail. Otherwise 'false' is returned.
	 */
	boolean isLocusPhotoIconThumbnail(Node styleObject) {
		if (!styleObject.hasAttributes() || styleObject.getAttributes().getNamedItem("id") == null) {
			return false;
		}
		String id = styleObject.getAttributes().getNamedItem("id").getTextContent();
		if (isLocusPhotoId(id)) {
			return true;
		} else {
			Node style = getStyle(styleObject);
			String href = kmlUtils.getIconHrefNode(style).getTextContent();
			return isLocusPhotoIconHref(href);
		}
	}
	
	boolean isLocusPhotoIconThumbnail(String iconName) {
		return isLocusPhotoIconThumbnail(iconName);
	}
	
	private Node getStyle(Node stylObject) {
		if (stylObject.getNodeName().equals("Style")) {
			return stylObject;
		} else {
			return kmlUtils.getNormalStyleNode(stylObject).orElseThrow(() -> new IllegalArgumentException(
					"A not valid xml(kml)! A StyleMap has to contain a 'normal' Style!"));
		}
	}
	
	private boolean isLocusPhotoId(String id) {
		id = id != null ? id : "";
		return id.matches("^file:///[\\w]*/Locus/cache/images/[\\w\\.]{4,}") ||
				id.matches("styleMapOf:file:///[\\w]*/Locus/cache/images/[\\w\\.]{4,}");
	}
	
	private boolean isLocusPhotoIconHref(String href) {
		href = href != null ? href : "";
		return href.toLowerCase().matches("^[\\S]*locuscacheimages[\\d]{7,}\\.png") ||
				href.toLowerCase().matches("^[\\S]+/-\\d{7,10}\\.png") ||
				href.toLowerCase().matches("^[\\S]+/\\d{7,10}\\.png");
	}
}
