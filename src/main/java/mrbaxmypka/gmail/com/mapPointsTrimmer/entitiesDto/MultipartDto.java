package mrbaxmypka.gmail.com.mapPointsTrimmer.entitiesDto;

import lombok.*;
import mrbaxmypka.gmail.com.mapPointsTrimmer.services.MultipartMainFileService;
import mrbaxmypka.gmail.com.mapPointsTrimmer.utils.DownloadAs;
import org.springframework.lang.Nullable;
import org.springframework.web.multipart.MultipartFile;

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
@EqualsAndHashCode(of = {"multipartFile"})
@ToString(onlyExplicitlyIncluded = true)
@Getter
@Setter
public abstract class MultipartDto implements Serializable {

    static final long serialVersionUID = 4L;

    @NonNull //Lombok required arg for the constructor
    @NotNull(message = "{validation.notNull}")
    private MultipartFile multipartFile;

    @NotNull
    private DownloadAs downloadAs;

    /**
     * To be filled by {@link mrbaxmypka.gmail.com.mapPointsTrimmer.controllers.FilesController#postKml(MultipartMainDto, Locale, HttpSession)}
     * to associate a process (thread) and a temp file with the current User session.
     */
    @Nullable
    @ToString.Include
    private String sessionId;

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
