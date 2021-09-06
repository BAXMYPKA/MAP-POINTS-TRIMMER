package mrbaxmypka.gmail.com.mapPointsTrimmer.entitiesDto;

import lombok.*;
import mrbaxmypka.gmail.com.mapPointsTrimmer.services.GoogleIconsService;
import org.springframework.lang.Nullable;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.*;

@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
@EqualsAndHashCode(of = {"multipartXmlFile"})
@ToString
@Getter
@Setter
public class MultipartFilterDto implements Serializable {

    static final long serialVersionUID = 3L;

    @NonNull //Lombok required arg for the constructor
    @NotNull(message = "{validation.notNull}")
    private MultipartFile multipartXmlFile;

    @NonNull //Lombok required arg for the constructor
    @NotNull(message = "{validation.notNull}")
    private MultipartFile multipartZipFile;

    /**
     * To be filled by {@link mrbaxmypka.gmail.com.mapPointsTrimmer.controllers.FilesController#postKml(MultipartMainDto, Locale, HttpSession)}
     * to associate a process (thread) and a temp file with the current User session.
     */
    @Nullable
    private String sessionId;

    /**
     * The instant cache for a currently processing .zip file from {@link #multipartXmlFile} (if it is) with images names
     * from it.
     * Can be used to determine the existent icons names.
     */
    private final Set<String> imagesNamesFromZip = new HashSet<>();

    /**
     * Google Map icons added by {@link GoogleIconsService#processIconHref(String, MultipartMainDto)}  )}
     * to be added into the resulting zip archive.
     * If a byte array value is null it means the icon with the key name is presented within a given archive from
     * {@link #getMultipartXmlFile()}
     */
    private Map<String, byte[]> googleIconsToBeZipped = new HashMap<>();

    /**
     * Filenames which have to be excluded from the resultant .zip (.kmz)
     */
    private Set<String> filesToBeExcluded = new HashSet<>(10);
}
