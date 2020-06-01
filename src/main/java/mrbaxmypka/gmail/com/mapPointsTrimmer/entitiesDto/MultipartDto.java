package mrbaxmypka.gmail.com.mapPointsTrimmer.entitiesDto;

import lombok.*;
import mrbaxmypka.gmail.com.mapPointsTrimmer.utils.PathTypes;
import mrbaxmypka.gmail.com.mapPointsTrimmer.utils.PreviewSizeUnits;
import org.springframework.lang.Nullable;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

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
	
	@Nullable
	@Digits(integer = 3, fraction = 0)
	@PositiveOrZero
	@Max(300)
	private BigDecimal pointIconSize;
	
	@Nullable
	@Digits(integer = 3, fraction = 0)
	@PositiveOrZero
	@Max(300)
	private BigDecimal pointTextSize;
	
	public void setPathType(@Nullable String pathType) {
		this.pathType = PathTypes.getByValue(pathType);
	}
	
	/**
	 * @param previewUnit Full text as "pixels" or "percentage" in any letter case
	 *                    OR unit as "px" or "%"
	 * @throws IllegalArgumentException If no match found.
	 */
	public void setPreviewUnit(@Nullable String previewUnit) throws IllegalArgumentException {
		for (PreviewSizeUnits unit : PreviewSizeUnits.values()) {
			if (unit.getUnit().equalsIgnoreCase(previewUnit)) {
				this.previewSizeUnit = unit;
				return;
			}
		}
		this.previewSizeUnit = PreviewSizeUnits.getByTypeName(previewUnit);
	}
	
	public void setPointIconSize(@Nullable BigDecimal pointIconSize) {
		if (pointIconSize != null) {
			pointIconSize = pointIconSize.setScale(1, RoundingMode.HALF_DOWN);
		}
		this.pointIconSize = pointIconSize;
	}
	
	public void setPointTextSize(@Nullable BigDecimal pointTextSize) {
		if (pointTextSize != null) {
			pointTextSize = pointTextSize.setScale(1, RoundingMode.HALF_DOWN);
		}
		this.pointTextSize = pointTextSize;
		
	}
	
	public void setPointIconSize(@Nullable Double pointIconSize) {
		if (pointIconSize != null) {
			this.pointIconSize = BigDecimal.valueOf(pointIconSize);
		} else {
			this.pointIconSize = null;
		}
	}
	
	public void setPointTextSize(@Nullable Double pointTextSize) {
		if (pointTextSize != null) {
			this.pointTextSize = new BigDecimal(pointTextSize, MathContext.DECIMAL128).setScale(1, RoundingMode.HALF_EVEN);
		} else {
			this.pointTextSize = null;
		}
	}
	
	public void setPointIconSize(@Nullable Integer pointIconSize) {
		if (pointIconSize == null) {
			this.pointIconSize = null;
		} else {
			this.pointIconSize = BigDecimal.valueOf(pointIconSize / 10, 1);
		}
	}
	
	public void setPointTextSize(@Nullable Integer pointTextSize) {
		if (pointTextSize == null) {
			this.pointTextSize = null;
		} else {
			this.pointTextSize = BigDecimal.valueOf(pointTextSize / 10, 1);
		}
	}
	
}
