package mrbaxmypka.gmail.com.mapPointsTrimmer.entitiesDto;

import lombok.*;
import mrbaxmypka.gmail.com.mapPointsTrimmer.services.GoogleIconsService;
import mrbaxmypka.gmail.com.mapPointsTrimmer.utils.DistanceUnits;
import mrbaxmypka.gmail.com.mapPointsTrimmer.utils.PathTypes;
import mrbaxmypka.gmail.com.mapPointsTrimmer.utils.PreviewSizeUnits;
import mrbaxmypka.gmail.com.mapPointsTrimmer.utils.ThinOutTypes;
import org.springframework.lang.Nullable;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString
@Getter
@Setter
public class MultipartMainDto extends MultipartDto {

    static final long serialVersionUID = 4L;

    private final Integer MAX_ICON_SIZE_PERCENTS = 300;
    private final Integer MAX_TEXT_SIZE_PERCENTS = 300;
    private final Double MAX_SCALE = 3.0;

    //    @NonNull //Lombok required arg for the constructor
    @NotNull(message = "{validation.notNull}")
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
     * {@link Nullable}. If null {@link PathTypes#RELATIVE} will be used.
     */
    @Nullable
    @Setter
    private PathTypes pathType = PathTypes.RELATIVE;

    /**
     * Local or Http path to all the attachments inside HTML descriptions
     * {@link Nullable}. If it is empty, all the paths will be erased.
     */
    @Nullable
    private String path;

    /**
     * Defines units for images size (percentage or pixels);
     * If null, {@link PreviewSizeUnits#PIXELS} will be used
     */
    @Nullable
    private PreviewSizeUnits previewSizeUnit = PreviewSizeUnits.PIXELS;

    /**
     * Attached photos preview size (in width) in pixels
     */
    @Nullable
    @PositiveOrZero(message = "{validation.positiveOrZero}")
    @Max(value = 3000, message = "{validation.maxNumber}")
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
    @PositiveOrZero(message = "{validation.positiveOrZero}")
    @Max(value = 300, message = "{validation.maxNumber}")
    private Integer pointIconSize;

    /**
     * <p>For User's convenience it represents a percentage input as 0 - 100%.</p>
     * <p>(Internally it will be converted into hexadecimal representation as 00 - FF (0 - 255).)</p>
     * <p>This color tag is applied as an overlay to PNG icons, the #FFFFFF is the white color and won't affect the
     * icons color, but the first hex "alpha channel" value will. (E.g. 00FFFFFF will make the icons invisible.)</p>
     * <p>********* FROM THE KML DOCUMENTATION ************************
     * Color and opacity (alpha) values are expressed in hexadecimal notation.
     * The range of values for any one color is 0 to 255 (00 to ff). For alpha, 00 is fully transparent and ff is fully opaque.
     * The order of expression is aabbggrr, where aa=alpha (00 to ff); bb=blue (00 to ff); gg=green (00 to ff); rr=red (00 to ff).
     * For example, if you want to apply a blue color with 50 percent opacity to an overlay, you would specify the following:
     * {@literal <color>7fff0000</color>}, where alpha=0x7f, blue=0xff, green=0x00, and red=0x00
     * </p>
     * Source: https://developers.google.com/kml/documentation/kmlreference#colorstyle
     * **************************************************************
     */
    @Nullable
    @PositiveOrZero(message = "{validation.positiveOrZero}")
    @Max(value = 100, message = "{validation.maxNumber}")
    private Integer pointIconOpacity;

    /**
     * Internally it will be represented as "scale" parameter from 0.0 to 3.0 unit with the step of 0.1.
     * Where 1.0 is the scale of default window font.
     * For User's convenience scale unit is represented as the percentage unit from 0 to 300(%) where
     * 10% is "scale = '0.1'",
     * 90% is "scale = '0.9'" etc.
     */
    @Nullable
    @PositiveOrZero(message = "{validation.positiveOrZero}")
    @Max(value = 300, message = "{validation.maxNumber}")
    private Integer pointTextSize;

    /**
     * Standard HTML hex color value as #ffffff.
     * For using in kml should be converted into kml color by
     * {@link mrbaxmypka.gmail.com.mapPointsTrimmer.xml.GoogleEarthHandler#getKmlColor(String, Integer)}
     * (https://developers.google.com/kml/documentation/kmlreference#colorstyle)
     * Color and opacity (alpha) values are expressed in hexadecimal notation.
     * The range of values for any one color is 0 to 255 (00 to ff). For alpha, 00 is fully transparent and ff is fully opaque.
     * The order of expression is aabbggrr, where aa=alpha (00 to ff); bb=blue (00 to ff); gg=green (00 to ff); rr=red (00 to ff).
     * For example, if you want to apply a blue color with 50 percent opacity to an overlay,
     * you would specify the following: {@literal <color>7fff0000</color>}, where alpha=0x7f, blue=0xff, green=0x00, and red=0x00.
     */
    @Nullable
    private String pointTextHexColor;

    /**
     * <p>For User's convenience it represents a percentage input as 0 - 100%.</p>
     * <p>(Internally it will be converted into hexadecimal representation as 00 - FF (0 - 255).)</p>
     * <p>********* FROM THE KML DOCUMENTATION ************************
     * Color and opacity (alpha) values are expressed in hexadecimal notation.
     * The range of values for any one color is 0 to 255 (00 to ff). For alpha, 00 is fully transparent and ff is fully opaque.
     * The order of expression is aabbggrr, where aa=alpha (00 to ff); bb=blue (00 to ff); gg=green (00 to ff); rr=red (00 to ff).
     * For example, if you want to apply a blue color with 50 percent opacity to an overlay, you would specify the following:
     * {@literal <color>7fff0000</color>}, where alpha=0x7f, blue=0xff, green=0x00, and red=0x00
     * </p>
     * Source: https://developers.google.com/kml/documentation/kmlreference#colorstyle
     * **************************************************************
     */
    @Nullable
    @PositiveOrZero(message = "{validation.positiveOrZero}")
    @Max(value = 100, message = "{validation.maxNumber}")
    private Integer pointTextOpacity;

    /**
     * {@code
     * "Static" means either unmapped <Style> or <key>normal</key> url to <Style>.
     * "Dynamic" means how to display it on mouse over (hovering) from <key>highlight</key> url to <Style>.
     * <StyleMap id="styleMap1">
     * <Pair>
     * ===>>> <key>normal</key> <<<=== STATIC PARAMETER
     * <styleUrl>#style1</styleUrl>
     * </Pair>
     * <Pair>
     * ===>>> <key>highlight</key> <<<=== DYNAMIC PARAMETER
     * <styleUrl>#style3</styleUrl>
     * </Pair>
     * </StyleMap>
     * ===>>> <Style id="style1"> <<<=== STATIC DISPLAY STYLE
     * <IconStyle>
     * <Icon>
     * <href>http://maps.google.com/mapfiles/kml/shapes/poi.png</href>
     * </Icon>
     * </IconStyle>
     * </Style>
     * ===>>> <Style id="style2"> <<<=== ON MOUSE OVER (DYNAMIC) DISPLAY STYLE
     * <IconStyle>
     * <Icon>
     * <href>http://maps.google.com/mapfiles/kml/shapes/poi.png</href>
     * </Icon>
     * </IconStyle>
     * </Style>
     * ===>>> <Style id="style3"> <<<=== UNMAPPED STATIC DISPLAY STYLE
     * <IconStyle>
     * <Icon>
     * <href>http://maps.google.com/mapfiles/kml/shapes/earthquake.png</href>
     * </Icon>
     * </IconStyle>
     * <LabelStyle>
     * <scale>0.8</scale>
     * </LabelStyle>
     * </Style>
     * }
     * Internally it will be represented as "scale" parameter from 0.0 to 3.0 unit with the step of 0.1.
     * Where 1.0 is the scale of default window font.
     * For User's convenience scale unit is represented as the percentage unit from 0 to 300(%) where
     * 10% is "scale = '0.1'",
     * 90% is "scale = '0.9'" etc.
     */
    @Nullable
    @PositiveOrZero(message = "{validation.positiveOrZero}")
    @Max(value = 300, message = "{validation.maxNumber}")
    private Integer pointIconSizeDynamic;

    /**
     * <p>For User's convenience it represents a percentage input as 0 - 100%.</p>
     * <p>(Internally it will be converted into hexadecimal representation as 00 - FF (0 - 255).)</p>
     * <p>This color tag is applied as an overlay to PNG icons, the #FFFFFF is the white color and won't affect the
     * icons color, but the first hex "alpha channel" value will. (E.g. 00FFFFFF will make the icons invisible.)</p>
     * <p>********* FROM THE KML DOCUMENTATION ************************
     * Color and opacity (alpha) values are expressed in hexadecimal notation.
     * The range of values for any one color is 0 to 255 (00 to ff). For alpha, 00 is fully transparent and ff is fully opaque.
     * The order of expression is aabbggrr, where aa=alpha (00 to ff); bb=blue (00 to ff); gg=green (00 to ff); rr=red (00 to ff).
     * For example, if you want to apply a blue color with 50 percent opacity to an overlay, you would specify the following:
     * {@literal <color>7fff0000</color>}, where alpha=0x7f, blue=0xff, green=0x00, and red=0x00
     * </p>
     * Source: https://developers.google.com/kml/documentation/kmlreference#colorstyle
     * **************************************************************
     */
    @Nullable
    @PositiveOrZero(message = "{validation.positiveOrZero}")
    @Max(value = 100, message = "{validation.maxNumber}")
    private Integer pointIconOpacityDynamic;

    /**
     * {@code
     * "Static" means either unmapped <Style> or <key>normal</key> url to <Style>.
     * "Dynamic" means how to display it on mouse over (hovering) from <key>highlight</key> url to <Style>.
     * <StyleMap id="styleMap1">
     * <Pair>
     * ===>>> <key>normal</key> <<<=== STATIC PARAMETER
     * <styleUrl>#style1</styleUrl>
     * </Pair>
     * <Pair>
     * ===>>> <key>highlight</key> <<<=== DYNAMIC PARAMETER
     * <styleUrl>#style3</styleUrl>
     * </Pair>
     * </StyleMap>
     * ===>>> <Style id="style1"> <<<=== STATIC DISPLAY STYLE
     * <IconStyle>
     * <Icon>
     * <href>http://maps.google.com/mapfiles/kml/shapes/poi.png</href>
     * </Icon>
     * </IconStyle>
     * </Style>
     * ===>>> <Style id="style2"> <<<=== ON MOUSE OVER (DYNAMIC) DISPLAY STYLE
     * <IconStyle>
     * <Icon>
     * <href>http://maps.google.com/mapfiles/kml/shapes/poi.png</href>
     * </Icon>
     * </IconStyle>
     * </Style>
     * ===>>> <Style id="style3"> <<<=== UNMAPPED STATIC DISPLAY STYLE
     * <IconStyle>
     * <Icon>
     * <href>http://maps.google.com/mapfiles/kml/shapes/earthquake.png</href>
     * </Icon>
     * </IconStyle>
     * <LabelStyle>
     * <scale>0.8</scale>
     * </LabelStyle>
     * </Style>
     * }
     * Internally it will be represented as "scale" parameter from 0.0 to 3.0 unit with the step of 0.1.
     * Where 1.0 is the scale of default window font.
     * For User's convenience scale unit is represented as the percentage unit from 0 to 300(%) where
     * 10% is "scale = '0.1'",
     * 90% is "scale = '0.9'" etc.
     */
    @Nullable
    @PositiveOrZero(message = "{validation.positiveOrZero}")
    @Max(value = 300, message = "{validation.maxNumber}")
    private Integer pointTextSizeDynamic;

    /**
     * {@code
     * "Static" means either unmapped <Style> or <key>normal</key> url to <Style>.
     * "Dynamic" means how to display it on mouse over (hovering) from <key>highlight</key> url to <Style>.
     * <StyleMap id="styleMap1">
     * <Pair>
     * ===>>> <key>normal</key> <<<=== STATIC PARAMETER
     * <styleUrl>#style1</styleUrl>
     * </Pair>
     * <Pair>
     * ===>>> <key>highlight</key> <<<=== DYNAMIC PARAMETER
     * <styleUrl>#style3</styleUrl>
     * </Pair>
     * </StyleMap>
     * ===>>> <Style id="style1"> <<<=== STATIC DISPLAY STYLE
     * <IconStyle>
     * <Icon>
     * <href>http://maps.google.com/mapfiles/kml/shapes/poi.png</href>
     * </Icon>
     * </IconStyle>
     * </Style>
     * ===>>> <Style id="style2"> <<<=== ON MOUSE OVER (DYNAMIC) DISPLAY STYLE
     * <IconStyle>
     * <Icon>
     * <href>http://maps.google.com/mapfiles/kml/shapes/poi.png</href>
     * </Icon>
     * </IconStyle>
     * </Style>
     * ===>>> <Style id="style3"> <<<=== UNMAPPED STATIC DISPLAY STYLE
     * <IconStyle>
     * <Icon>
     * <href>http://maps.google.com/mapfiles/kml/shapes/earthquake.png</href>
     * </Icon>
     * </IconStyle>
     * <LabelStyle>
     * <scale>0.8</scale>
     * </LabelStyle>
     * </Style>
     * }
     * Standard HTML hex color value as #ffffff.
     * For using in kml should be converted into kml color by {@link mrbaxmypka.gmail.com.mapPointsTrimmer.xml.GoogleEarthHandler#getKmlColor(String, String)}
     * (https://developers.google.com/kml/documentation/kmlreference#colorstyle)
     * Color and opacity (alpha) values are expressed in hexadecimal notation.
     * The range of values for any one color is 0 to 255 (00 to ff). For alpha, 00 is fully transparent and ff is fully opaque.
     * The order of expression is aabbggrr, where aa=alpha (00 to ff); bb=blue (00 to ff); gg=green (00 to ff); rr=red (00 to ff).
     * For example, if you want to apply a blue color with 50 percent opacity to an overlay,
     * you would specify the following: {@literal <color>7fff0000</color>}, where alpha=0x7f, blue=0xff, green=0x00, and red=0x00.
     */
    @Nullable
    private String pointTextHexColorDynamic;

    /**
     * {@code
     * "Static" means either unmapped <Style> or <key>normal</key> url to <Style>.
     * "Dynamic" means how to display it on mouse over (hovering) from <key>highlight</key> url to <Style>.
     * <StyleMap id="styleMap1">
     * <Pair>
     * ===>>> <key>normal</key> <<<=== STATIC PARAMETER
     * <styleUrl>#style1</styleUrl>
     * </Pair>
     * <Pair>
     * ===>>> <key>highlight</key> <<<=== DYNAMIC PARAMETER
     * <styleUrl>#style3</styleUrl>
     * </Pair>
     * </StyleMap>
     * ===>>> <Style id="style1"> <<<=== STATIC DISPLAY STYLE
     * <IconStyle>
     * <Icon>
     * <href>http://maps.google.com/mapfiles/kml/shapes/poi.png</href>
     * </Icon>
     * </IconStyle>
     * </Style>
     * ===>>> <Style id="style2"> <<<=== ON MOUSE OVER (DYNAMIC) DISPLAY STYLE
     * <IconStyle>
     * <Icon>
     * <href>http://maps.google.com/mapfiles/kml/shapes/poi.png</href>
     * </Icon>
     * </IconStyle>
     * </Style>
     * ===>>> <Style id="style3"> <<<=== UNMAPPED STATIC DISPLAY STYLE
     * <IconStyle>
     * <Icon>
     * <href>http://maps.google.com/mapfiles/kml/shapes/earthquake.png</href>
     * </Icon>
     * </IconStyle>
     * <LabelStyle>
     * <scale>0.8</scale>
     * </LabelStyle>
     * </Style>
     * }
     * A percentage value from 0 to 100%
     * <p>For User's convenience it represents a percentage input as 0 - 100%.</p>
     * <p>(Internally it will be converted into hexadecimal representation as 00 - FF (0 - 255).)</p>
     * <p>********* FROM THE KML DOCUMENTATION ************************
     * Color and opacity (alpha) values are expressed in hexadecimal notation.
     * The range of values for any one color is 0 to 255 (00 to ff). For alpha, 00 is fully transparent and ff is fully opaque.
     * The order of expression is aabbggrr, where aa=alpha (00 to ff); bb=blue (00 to ff); gg=green (00 to ff); rr=red (00 to ff).
     * For example, if you want to apply a blue color with 50 percent opacity to an overlay, you would specify the following:
     * {@literal <color>7fff0000</color>}, where alpha=0x7f, blue=0xff, green=0x00, and red=0x00
     * </p>
     * Source: https://developers.google.com/kml/documentation/kmlreference#colorstyle
     * **************************************************************
     */
    @Nullable
    @PositiveOrZero(message = "{validation.positiveOrZero}")
    @Max(value = 100, message = "{validation.maxNumber}")
    private Integer pointTextOpacityDynamic;

    /**
     * For POIs with included photos Locus creates icons on a map as thumbnails.
     * For a better visibility you can change them all to a specific pictogram.
     */
    private boolean replaceLocusIcons = false;

    /**
     * A full names of the existing pictograms in the 'resources/static/pictograms' directory.
     * E.g. 'pictogram1.png', 'pictogram2.PNG' etc.
     * This pictogram is intended to replace all the Locus photo thumbnails in placemarks.
     */
    @Nullable
    private String pictogramName;

    @Nullable
    private boolean thinOutPoints = false;

    /**
     * {@link Nullable}. If null {@link ThinOutTypes#ANY} will be used.
     */
    @Nullable
    private ThinOutTypes thinOutType = ThinOutTypes.ANY;

    @Nullable
    @PositiveOrZero(message = "{validation.positiveOrZero}")
    @Min(value = 1, message = "{validation.minNumber}}")
    @Max(value = 5000, message = "{validation.maxNumber}")
    private Integer thinOutDistance;

    /**
     * Default {@link DistanceUnits#METERS}
     */
    @Nullable
    private DistanceUnits distanceUnit = DistanceUnits.METERS;

    /**
     * If {@link ThinOutTypes#INCLUSIVE} or {@link ThinOutTypes#EXCLUSIVE} it is a list of icons names which Placemarks are
     * to be retained or removed.
     */
    @Nullable
    private List<String> thinOutIconsNames;

    /**
     * To be filled by {@link mrbaxmypka.gmail.com.mapPointsTrimmer.controllers.FilesController#postKml(MultipartMainDto, Locale, HttpSession, HttpServletResponse)}
     * to associate a process (thread) and a temp file with the current User session.
     */
    @Nullable
    private String sessionId;

    /**
     * Google Map icons added by {@link GoogleIconsService#processIconHref(String, MultipartMainDto)}  )}
     * to be added into the resulting zip archive.
     * If a byte array value is null it means the icon with the key name is presented within a given archive from
     * {@link #getMultipartFile()}
     */
    @ToString.Exclude
    private Map<String, byte[]> googleIconsToBeZipped = new HashMap<>();

    /**
     * Filenames which have to be excluded from the resultant .zip (.kmz)
     */
    @ToString.Exclude
    private Set<String> filesToBeExcluded = new HashSet<>(10);

    public MultipartMainDto(MultipartFile multipartFile) {
        super(multipartFile);
        this.multipartFile = multipartFile;
    }

    /**
     * Scale has to be presented as digits divided by a dot and not exceeding the {@link #MAX_SCALE}
     */
    private boolean isScaleCorrect(Double scale) {
        return Double.toString(scale).matches("\\d\\.\\d") && scale.compareTo(MAX_SCALE) <= 0;
    }

    public void setPathType(@Nullable PathTypes pathType) {
        if (pathType != null) {
            this.pathType = pathType;
        }
    }

    /**
     * @param pathType "absolute", "relative", or "web"
     */
    public void setPathType(@Nullable String pathType) {
        this.pathType = PathTypes.getByValue(pathType);
    }

    public void setThinOutType(@Nullable ThinOutTypes thinOutType) {
        if (thinOutType != null) {
            this.thinOutType = thinOutType;
        }
    }

    /**
     * @param thinOutType "all", "inclusive", or "exclusive"
     */
    public void setThinOutType(@Nullable String thinOutType) {
        this.thinOutType = ThinOutTypes.getByValue(thinOutType);
    }

    public void setDistanceUnit(@Nullable DistanceUnits distanceUnit) {
        if (distanceUnit != null) {
            this.distanceUnit = distanceUnit;
        }
    }

    /**
     * @param distanceUnit like "meters", "kilometers", or "miles"
     */
    public void setDistanceUnit(@Nullable String distanceUnit) {
        this.distanceUnit = DistanceUnits.getByValue(distanceUnit);
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

    /**
     * @return Essential scale CSS parameter from 0.0 to 3.0 (max) with the step of 0.1
     */
    public BigDecimal getPointTextSizeScaledDynamic() {
        if (this.pointTextSizeDynamic != null) {
            return new BigDecimal(String.valueOf(this.pointTextSizeDynamic)).divide(BigDecimal.valueOf(100), 1,
                    RoundingMode.DOWN);
        }
        return null;
    }

    /**
     * Internally it will be represented as "scale" parameter from 0.0 to 3.0 unit with the step of 0.1.
     * Where 1.0 is the scale of default window font.
     *
     * @param pointTextSizeScaledDynamic As a pure "scale" CSS parameter from 0.0 to 3.0 with the step of 0.1
     * @throws NumberFormatException If the method receives incompatible data, e.g. 11.1 or 0.12 ect
     */
    public void setPointTextSizeScaledDynamic(@Nullable Double pointTextSizeScaledDynamic) throws NumberFormatException {
        if (pointTextSizeScaledDynamic == null) {
            this.pointTextSizeDynamic = null;
        } else if (!isScaleCorrect(pointTextSizeScaledDynamic)) {
            throw new NumberFormatException("Scale has to be represented as value from 0.0 to 3.0 with the step of 0.1");
        } else {
            BigDecimal bigDecimal = BigDecimal.valueOf(pointTextSizeScaledDynamic).setScale(1, RoundingMode.DOWN);
            this.pointTextSizeDynamic = bigDecimal.multiply(BigDecimal.valueOf(100)).intValue();
        }
    }

    /**
     * @see #getPointIconSizeScaled()
     */
    @Nullable
    @Deprecated
    public Integer getPointIconSize() {
        return this.pointIconSize;
    }

    /**
     * Internally it will be represented as "scale" parameter from 0.0 to 3.0 unit with the step of 0.1.
     * Where 1.0 is the scale of default window font.
     * For User's convenience scale unit is represented as the percentage unit from 0 to 300(%) where
     * 10% is "scale = '0.1'",
     * 90% is "scale = '0.9'" etc.
     */
    public void setPointIconSize(@Nullable Integer pointIconSize) {
        if (pointIconSize != null && pointIconSize > MAX_ICON_SIZE_PERCENTS) {
            throw new NumberFormatException("Point Icon Size cannot exceed 300%!");
        } else {
            this.pointIconSize = pointIconSize;
        }
    }

    /**
     * @see #getPointIconSizeScaled()
     */
    @Nullable
    @Deprecated
    public Integer getPointIconSizeDynamic() {
        return this.pointIconSizeDynamic;
    }

    /**
     * Internally it will be represented as "scale" parameter from 0.0 to 3.0 unit with the step of 0.1.
     * Where 1.0 is the scale of default window font.
     * For User's convenience scale unit is represented as the percentage unit from 0 to 300(%) where
     * 10% is "scale = '0.1'",
     * 90% is "scale = '0.9'" etc.
     */
    public void setPointIconSizeDynamic(@Nullable Integer pointIconSizeDynamic) {
        if (pointIconSizeDynamic != null && pointIconSizeDynamic > MAX_ICON_SIZE_PERCENTS) {
            throw new NumberFormatException("Point Icon Size cannot exceed 300%!");
        } else {
            this.pointIconSizeDynamic = pointIconSizeDynamic;
        }
    }

    /**
     * @see #getPointTextSizeScaled()
     */
    @Nullable
    @Deprecated
    public Integer getPointTextSize() {
        return this.pointTextSize;
    }

    /**
     * Internally it will be represented as "scale" parameter from 0.0 to 3.0 unit with the step of 0.1.
     * Where 1.0 is the scale of default window font.
     * For User's convenience scale unit is represented as the percentage unit from 0 to 300(%) where
     * 10% is "scale = '0.1'",
     * 90% is "scale = '0.9'" etc.
     */
    public void setPointTextSize(@Nullable Integer pointTextSize) {
        if (pointTextSize != null && pointTextSize > MAX_TEXT_SIZE_PERCENTS) {
            throw new NumberFormatException("Point Text Size cannot exceed 300%!");
        } else {
            this.pointTextSize = pointTextSize;
        }
    }

    /**
     * @see #getPointTextSizeScaled()
     */
    @Nullable
    @Deprecated
    public Integer getPointTextSizeDynamic() {
        return this.pointTextSizeDynamic;
    }

    /**
     * Internally it will be represented as "scale" parameter from 0.0 to 3.0 unit with the step of 0.1.
     * Where 1.0 is the scale of default window font.
     * For User's convenience scale unit is represented as the percentage unit from 0 to 300(%) where
     * 10% is "scale = '0.1'",
     * 90% is "scale = '0.9'" etc.
     * {@code
     * "Static" means either unmapped <Style> or <key>normal</key> url to <Style>.
     * "Dynamic" means how to display it on mouse over (hovering) from <key>highlight</key> url to <Style>.
     * <StyleMap id="styleMap1">
     * <Pair>
     * ===>>> <key>normal</key> <<<=== STATIC PARAMETER
     * <styleUrl>#style1</styleUrl>
     * </Pair>
     * <Pair>
     * ===>>> <key>highlight</key> <<<=== DYNAMIC PARAMETER
     * <styleUrl>#style3</styleUrl>
     * </Pair>
     * </StyleMap>
     * ===>>> <Style id="style1"> <<<=== STATIC DISPLAY STYLE
     * <IconStyle>
     * <Icon>
     * <href>http://maps.google.com/mapfiles/kml/shapes/poi.png</href>
     * </Icon>
     * </IconStyle>
     * </Style>
     * ===>>> <Style id="style2"> <<<=== ON MOUSE OVER (DYNAMIC) DISPLAY STYLE
     * <IconStyle>
     * <Icon>
     * <href>http://maps.google.com/mapfiles/kml/shapes/poi.png</href>
     * </Icon>
     * </IconStyle>
     * </Style>
     * ===>>> <Style id="style3"> <<<=== UNMAPPED STATIC DISPLAY STYLE
     * <IconStyle>
     * <Icon>
     * <href>http://maps.google.com/mapfiles/kml/shapes/earthquake.png</href>
     * </Icon>
     * </IconStyle>
     * <LabelStyle>
     * <scale>0.8</scale>
     * </LabelStyle>
     * </Style>
     * }
     * Internally it will be represented as "scale" parameter from 0.0 to 3.0 unit with the step of 0.1.
     * Where 1.0 is the scale of default window font.
     * For User's convenience scale unit is represented as the percentage unit from 0 to 300(%) where
     * 10% is "scale = '0.1'",
     * 90% is "scale = '0.9'" etc.
     */
    public void setPointTextSizeDynamic(@Nullable Integer pointTextSizeDynamic) {
        if (pointTextSizeDynamic != null && pointTextSizeDynamic > MAX_TEXT_SIZE_PERCENTS) {
            throw new NumberFormatException("Point Text Size cannot exceed 300%!");
        } else {
            this.pointTextSizeDynamic = pointTextSizeDynamic;
        }
    }

    /**
     * @return {@link org.springframework.lang.NonNull} Hexidecimal format for color opacity setting from 00 to FF
     */
    public String getPointTextHexOpacity() {
        if (pointTextOpacity < 0 || pointTextOpacity > 100) {
            throw new IllegalArgumentException("Percentage has to be from 0 to 100%!");
        }
        BigDecimal hexPercentage = new BigDecimal(pointTextOpacity * 2.55).setScale(0, RoundingMode.HALF_UP);
        String hexFormat = String.format("%02x", hexPercentage.toBigInteger());
        return hexFormat;
    }

    /**
     * @return {@link org.springframework.lang.NonNull} Hexidecimal format for color opacity setting from 00 to FF
     */
    public String getPointTextHexOpacityDynamic() {
        if (pointTextOpacityDynamic < 0 || pointTextOpacityDynamic > 100) {
            throw new IllegalArgumentException("Percentage has to be from 0 to 100%!");
        }
        BigDecimal hexPercentage = new BigDecimal(pointTextOpacityDynamic * 2.55).setScale(0, RoundingMode.HALF_UP);
        String hexFormat = String.format("%02x", hexPercentage.toBigInteger());
        return hexFormat;
    }

    /**
     * @return {@link org.springframework.lang.NonNull} Hexidecimal format for color opacity setting from 00 to FF
     */
    public String getPointIconHexOpacity() {
        if (pointIconOpacity < 0 || pointIconOpacity > 100) {
            throw new IllegalArgumentException("Percentage has to be from 0 to 100%!");
        }
        BigDecimal hexPercentage = new BigDecimal(pointIconOpacity * 2.55).setScale(0, RoundingMode.HALF_UP);
        String hexFormat = String.format("%02x", hexPercentage.toBigInteger());
        return hexFormat;
    }

    /**
     * @return {@link org.springframework.lang.NonNull} Hexidecimal format for color opacity setting from 00 to FF
     */
    public String getPointIconHexOpacityDynamic() {
        if (pointIconOpacityDynamic < 0 || pointIconOpacityDynamic > 100) {
            throw new IllegalArgumentException("Percentage has to be from 0 to 100%!");
        }
        BigDecimal hexPercentage = new BigDecimal(pointIconOpacityDynamic * 2.55).setScale(0, RoundingMode.HALF_UP);
        String hexFormat = String.format("%02x", hexPercentage.toBigInteger());
        return hexFormat;
    }
}
