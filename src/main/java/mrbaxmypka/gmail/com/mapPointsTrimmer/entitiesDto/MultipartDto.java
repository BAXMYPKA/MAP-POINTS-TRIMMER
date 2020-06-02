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
	
	/**
	 * Internally it will be represented as "scale" parameter from 0.0 to 3.0 unit with the step of 0.1.
	 * Where 1.0 is the scale of default window font.
	 * For User's convenience scale unit is represented as the percentage unit from 0 to 300(%) where
	 * 10% is "scale = '0.1'",
	 * 90% is "scale = '0.9'" etc.
	 */
	@Nullable
	@PositiveOrZero
	@Max(300)
	private Integer pointIconSize;
	
	/**
	 * Internally it will be represented as "scale" parameter from 0.0 to 3.0 unit with the step of 0.1.
	 * Where 1.0 is the scale of default window font.
	 * For User's convenience scale unit is represented as the percentage unit from 0 to 300(%) where
	 * 10% is "scale = '0.1'",
	 * 90% is "scale = '0.9'" etc.
	 */
	@Nullable
	@PositiveOrZero
	@Max(300)
	private Integer pointTextSize;
	
	private boolean isScaleCorrect(Double scale) {
		return Double.toString(scale).matches("\\d\\.\\d") && scale.compareTo(3.0) > 0;
	}
	
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
	
	/**
	 * Internally it will be represented as "scale" parameter from 0.0 to 3.0 unit with the step of 0.1.
	 * Where 1.0 is the scale of default window font.
	 * For User's convenience scale unit is represented as the percentage unit from 0 to 300(%) where
	 * 10% is "scale = '0.1'",
	 * 90% is "scale = '0.9'" etc.
	 */
	public void setPointIconSize(@Nullable Integer pointIconSize) {
		if (pointIconSize != null && pointIconSize > 300) {
			throw new NumberFormatException("Point Icon Size cannot exceed 300%!");
		} else {
			this.pointIconSize = pointIconSize;
		}
	}
	
	/**
	 * Internally it will be represented as "scale" parameter from 0.0 to 3.0 unit with the step of 0.1.
	 * Where 1.0 is the scale of default window font.
	 * For User's convenience scale unit is represented as the percentage unit from 0 to 300(%) where
	 * 10% is "scale = '0.1'",
	 * 90% is "scale = '0.9'" etc.
	 */
	public void setPointTextSize(@Nullable Integer pointTextSize) {
		if (pointTextSize != null && pointTextSize > 300) {
			throw new NumberFormatException("Point Text Size cannot exceed 300%!");
		} else {
			this.pointTextSize = pointTextSize;
		}
	}
	
	/**
	 * Internally it will be represented as "scale" parameter from 0.0 to 3.0 unit with the step of 0.1.
	 * Where 1.0 is the scale of default window font.
	 *
	 * @param pointIconSizeScaled As a pure "scale" CSS parameter from 0.0 to 3.0 with the step of 0.1
	 * @throws NumberFormatException If the method receives incompatible data, e.g. 11.1 or 0.12 ect
	 */
	public void setPointIconSizeScaled(@Nullable Double pointIconSizeScaled) throws NumberFormatException {
		if (pointIconSizeScaled == null) {
			this.pointIconSize = null;
		} else if (isScaleCorrect(pointIconSizeScaled)) {
			throw new NumberFormatException("Scale has to be represented as value from 0.0 to 3.0 with the step of 0.1");
		} else {
			BigDecimal bigDecimal = BigDecimal.valueOf(pointIconSizeScaled).setScale(1, RoundingMode.DOWN);
			this.pointIconSize = bigDecimal.multiply(BigDecimal.valueOf(100)).intValue();
		}
	}
	
	/**
	 * Internally it will be represented as "scale" parameter from 0.0 to 3.0 unit with the step of 0.1.
	 * Where 1.0 is the scale of default window font.
	 *
	 * @param pointTextSizeScaled As a pure "scale" CSS parameter from 0.0 to 3.0 with the step of 0.1
	 * @throws NumberFormatException If the method receives incompatible data, e.g. 11.1 or 0.12 ect
	 */
	public void setPointTextSizeScaled(@Nullable Double pointTextSizeScaled) throws NumberFormatException {
		if (pointTextSizeScaled == null) {
			this.pointTextSize = null;
		} else if (isScaleCorrect(pointTextSizeScaled)) {
			throw new NumberFormatException("Scale has to be represented as value from 0.0 to 3.0 with the step of 0.1");
		} else {
			BigDecimal bigDecimal = BigDecimal.valueOf(pointTextSizeScaled).setScale(1, RoundingMode.DOWN);
			this.pointTextSize = bigDecimal.multiply(BigDecimal.valueOf(100)).intValue();
			
		}
	}
	
	/**
	 * @return Essential scale CSS parameter from 0.0 to 3.0 (max) with the step of 0.1
	 */
	public BigDecimal getPointIconSizeScaled() {
		if (this.getPointIconSize() != null) {
			BigDecimal pointIconSizeScaled = new BigDecimal(String.valueOf(this.pointIconSize));
			return new BigDecimal(String.valueOf(this.pointIconSize)).divide(BigDecimal.valueOf(100), 1, RoundingMode.DOWN);
		}
		return null;
	}
	
	/**
	 * @return Essential scale CSS parameter from 0.0 to 3.0 (max) with the step of 0.1
	 */
	public BigDecimal getPointTextSizeScaled() {
		if (this.getPointIconSize() != null) {
			return new BigDecimal(String.valueOf(this.pointTextSize)).divide(BigDecimal.valueOf(100), 1, RoundingMode.DOWN);
		}
		return null;
		
	}
	
	/**
	 * @see #getPointIconSizeScaled()
	 */
	@Nullable
	@Deprecated
	public Integer getPointIconSize() {
		return pointIconSize;
	}
	
	/**
	 * @see #getPointTextSizeScaled()
	 */
	@Nullable
	@Deprecated
	public Integer getPointTextSize() {
		return pointTextSize;
	}
}
