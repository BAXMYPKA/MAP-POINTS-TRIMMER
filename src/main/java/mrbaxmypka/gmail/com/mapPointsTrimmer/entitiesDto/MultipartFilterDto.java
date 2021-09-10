package mrbaxmypka.gmail.com.mapPointsTrimmer.entitiesDto;

import lombok.*;
import org.springframework.lang.Nullable;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = true, of = {"multipartZipFile"})
@ToString(onlyExplicitlyIncluded = true)
@Getter
@Setter
public class MultipartFilterDto extends MultipartDto {

    static final long serialVersionUID = 4L;

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
    @ToString.Include
    private String sessionId;

    /**
     * Filenames which have to be excluded from the resultant .zip (.kmz)
     */
    private Set<String> filesToBeExcluded = new HashSet<>(10);
}
