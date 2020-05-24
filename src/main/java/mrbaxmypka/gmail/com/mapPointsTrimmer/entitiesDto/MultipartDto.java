package mrbaxmypka.gmail.com.mapPointsTrimmer.entitiesDto;

import lombok.*;
import mrbaxmypka.gmail.com.mapPointsTrimmer.utils.PathTypes;
import mrbaxmypka.gmail.com.mapPointsTrimmer.utils.PreviewSizeUnits;
import org.springframework.lang.Nullable;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
@EqualsAndHashCode(of = {"multipartFile"})
@Getter
@Setter
public class MultipartDto implements Serializable {
	
	static final long serialVersionUID = 3L;
	
	@NonNull //Lombok required arg for the constructor
	@NotNull
	private MultipartFile multipartFile;
	
	/**
	 * To trim whitespaces only in presented CDATA[[]] user descriptions
	 */
	private boolean trimDescriptions;
	
	/**
	 * To trim the whole .kml document to a single line without breaks and whitespaces
	 */
	private boolean trimXml;
	
	/**
	 * To clear all the unused date stamps after every POIs exporting
	 */
	private boolean clearOutdatedDescriptions;
	
	private boolean validateXml;
	
	/**
	 * Enables or disables using of {@link #path}
	 */
	private boolean setPath;
	
	@Nullable
	private PathTypes pathType;
	
	/**
	 * Local or Http path to all the attachments inside HTML descriptions
	 */
	@Nullable
	private String path;
	
	/**
	 * Enables or disables using {@link #previewSize}
	 */
	@Nullable
	private boolean setPreviewSize;
	
	/**
	 * Defines units for images size (percentage or pixels);
	 * If null, {@link PreviewSizeUnits#PIXELS} will be used
	 */
	private PreviewSizeUnits previewSizeUnit;
	
	/**
	 * Attached photos preview size (in width) in pixels
	 */
	@Nullable
	private Integer previewSize;
	
	/**
	 * A given images will be displayed in "Attachments" tab exclusively in Locus Pro
	 */
	private boolean asAttachmentInLocus;
	
	public void setPathType(@Nullable String pathType) {
		this.pathType = PathTypes.getByValue(pathType);
	}
	
	public void setPreviewUnit(@Nullable String previewUnit) {
		this.previewSizeUnit = PreviewSizeUnits.getByTypeName(previewUnit);
	}
}
