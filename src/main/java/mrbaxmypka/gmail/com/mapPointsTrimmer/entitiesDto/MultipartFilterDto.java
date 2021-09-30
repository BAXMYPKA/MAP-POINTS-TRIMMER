package mrbaxmypka.gmail.com.mapPointsTrimmer.entitiesDto;

import lombok.*;
import mrbaxmypka.gmail.com.mapPointsTrimmer.controllers.FilesController;
import mrbaxmypka.gmail.com.mapPointsTrimmer.services.MultipartFilterFileService;
import org.springframework.lang.Nullable;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.constraints.NotNull;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
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
	
	@Nullable
	private MultipartFile multipartFile;
	
	@NonNull //Lombok required arg for the constructor
	@NotNull(message = "{validation.notNull}")
	private MultipartFile multipartXmlFile;
	
	@NonNull //Lombok required arg for the constructor
	@NotNull(message = "{validation.notNull}")
	private MultipartFile multipartZipFile;
	
	/**
	 * The field is only for using within {@link MultipartFilterFileService}
	 * A filename of a standalone .xml(.kml) file or a .kml file inside a given .kmz
	 */
	private String xmlFilename = "";
	
	/**
	 * To be filled by {@link FilesController#postKml(MultipartMainDto, Locale, HttpSession, HttpServletResponse)}
	 * to associate a process (thread) and a temp file with the current User session.
	 */
	@Nullable
	@ToString.Include
	private String sessionId;
	
	@Nullable
	private Charset charset;
	
	/**
	 * Filenames which have to be excluded from the resultant .zip (.kmz)
	 */
	private Set<String> filesToBeExcluded = new HashSet<>(10);
	
	public void setCharset(String zipNamesCharset) {
		if (zipNamesCharset == null || zipNamesCharset.isBlank()) {
			charset = StandardCharsets.UTF_8;
		} else {
			charset = Charset.forName(zipNamesCharset);
		}
	}
	
	public void setCharset(Charset zipNamesCharset) {
		if (zipNamesCharset == null) {
			charset = StandardCharsets.UTF_8;
		} else {
			charset = zipNamesCharset;
		}
	}
}
