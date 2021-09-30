package mrbaxmypka.gmail.com.mapPointsTrimmer.entitiesDto;

import lombok.*;
import mrbaxmypka.gmail.com.mapPointsTrimmer.controllers.FilesController;
import mrbaxmypka.gmail.com.mapPointsTrimmer.services.GoogleIconsService;
import mrbaxmypka.gmail.com.mapPointsTrimmer.services.MultipartMainFileService;
import mrbaxmypka.gmail.com.mapPointsTrimmer.utils.DownloadAs;
import org.springframework.lang.Nullable;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpSession;
import javax.validation.constraints.Max;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
@EqualsAndHashCode
@ToString(onlyExplicitlyIncluded = true)
@Getter
@Setter
public abstract class MultipartDto implements Serializable {

    static final long serialVersionUID = 4L;

    @NonNull //Lombok required arg for the constructor
//    @NotNull(message = "{validation.notNull}")
    private MultipartFile multipartFile;

    @NotNull
    private DownloadAs downloadAs;

    /**
     * To be filled by {@link FilesController#postKml(MultipartDto, Locale, HttpSession)}
     * To be filled by {@link FilesController#postKml(MultipartMainDto, Locale, HttpSession, HttpServletResponse)}
     * to associate a process (thread) and a temp file with the current User session.
     */
    @Nullable
    @ToString.Include
    private String sessionId;

    /**
     * The instant cache for a currently processing .zip file from {@link #multipartFile} (if it is) with images names
     * from it.
     * Can be used to determine the existent icons names.
     */
    @ToString.Exclude
    private final Set<String> imagesNamesFromZip = new HashSet<>();
    /**
     * Google Map icons added by {@link GoogleIconsService#processIconHref(String, MultipartDto)}  )}
     * to be added into the resulting zip archive.
     * If a byte array value is null it means the icon with the key name is presented within a given archive from
     * {@link #getMultipartFile()}
     */
    private Map<String, byte[]> googleIconsToBeZipped = new HashMap<>();

    /**
     * Filenames which have to be excluded from the resultant .zip (.kmz)
     */
    private Set<String> filesToBeExcluded = new HashSet<>(10);

    /**
     * Scale has to be presented as digits divided by a dot and not exceeding the {@link #MAX_SCALE}
     */
    private boolean isScaleCorrect(Double scale) {
        return Double.toString(scale).matches("\\d\\.\\d") && scale.compareTo(MAX_SCALE) <= 0;
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
     * @return Essential scale CSS parameter from 0.0 to 3.0 (max) with the step of 0.1
     */
    public BigDecimal getPointIconSizeScaled() {
        if (this.pointIconSize != null) {
            return new BigDecimal(String.valueOf(this.pointIconSize)).divide(BigDecimal.valueOf(100), 1, RoundingMode.DOWN);
        }
        return null;
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
        } else if (!isScaleCorrect(pointIconSizeScaled)) {
            throw new NumberFormatException("Scale has to be represented as value from 0.0 to 3.0 with the step of 0.1");
        } else {
            BigDecimal bigDecimal = BigDecimal.valueOf(pointIconSizeScaled).setScale(1, RoundingMode.DOWN);
            this.pointIconSize = bigDecimal.multiply(BigDecimal.valueOf(100)).intValue();
        }
    }

    /**
     * @return Essential scale CSS parameter from 0.0 to 3.0 (max) with the step of 0.1
     */
    public BigDecimal getPointIconSizeScaledDynamic() {
        if (this.pointIconSizeDynamic != null) {
            return new BigDecimal(
                    String.valueOf(this.pointIconSizeDynamic)).divide(BigDecimal.valueOf(100), 1, RoundingMode.DOWN);
        }
        return null;
    }

    /**
     * Internally it will be represented as "scale" parameter from 0.0 to 3.0 unit with the step of 0.1.
     * Where 1.0 is the scale of default window font.
     *
     * @param pointIconSizeScaledDynamic As a pure "scale" CSS parameter from 0.0 to 3.0 with the step of 0.1
     * @throws NumberFormatException If the method receives incompatible data, e.g. 11.1 or 0.12 ect
     */
    public void setPointIconSizeScaledDynamic(@Nullable Double pointIconSizeScaledDynamic) throws NumberFormatException {
        if (pointIconSizeScaledDynamic == null) {
            this.pointIconSizeDynamic = null;
        } else if (!isScaleCorrect(pointIconSizeScaledDynamic)) {
            throw new NumberFormatException("Scale has to be represented as value from 0.0 to 3.0 with the step of 0.1");
        } else {
            BigDecimal bigDecimal = BigDecimal.valueOf(pointIconSizeScaledDynamic).setScale(1, RoundingMode.DOWN);
            this.pointIconSizeDynamic = bigDecimal.multiply(BigDecimal.valueOf(100)).intValue();
        }
    }

    /**
     * @return Essential scale CSS parameter from 0.0 to 3.0 (max) with the step of 0.1
     */
    public BigDecimal getPointTextSizeScaled() {
        if (this.pointTextSize != null) {
            return new BigDecimal(String.valueOf(this.pointTextSize)).divide(BigDecimal.valueOf(100), 1, RoundingMode.DOWN);
        }
        return null;
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
        } else if (!isScaleCorrect(pointTextSizeScaled)) {
            throw new NumberFormatException("Scale has to be represented as value from 0.0 to 3.0 with the step of 0.1");
        } else {
            BigDecimal bigDecimal = BigDecimal.valueOf(pointTextSizeScaled).setScale(1, RoundingMode.DOWN);
            this.pointTextSize = bigDecimal.multiply(BigDecimal.valueOf(100)).intValue();
        }
    }

=======
    
//    @NotNull(message = "{validation.notNull}")
    /**
     * To localize any messages for users.
     * Default ENGLISH.
     */
    @ToString.Include
    private Locale locale = Locale.ENGLISH;

    /**
     * The field is only for using within {@link MultipartMainFileService}
     * A .zip(.kmz) or .kml filename in the Temp directory for an according {@link MultipartMainDto}
     */
    private Path tempFile;

    /**
     *  The field is only for using within {@link MultipartMainFileService}
     * A filename of a standalone .xml(.kml) file or a .kml file inside a given .kmz
     */
    private String xmlFilename = "";

    /**
     * The instant cache for a currently processing .zip file from {@link #multipartFile} (if it is) with images names
     * from it.
     * Can be used to determine the existent icons names.
     */
    private final Set<String> imagesNamesFromZip = new HashSet<>();
}
