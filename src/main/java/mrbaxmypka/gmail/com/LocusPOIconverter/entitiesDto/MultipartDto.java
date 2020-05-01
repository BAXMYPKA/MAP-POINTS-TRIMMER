package mrbaxmypka.gmail.com.LocusPOIconverter.entitiesDto;

import lombok.*;
import org.springframework.lang.Nullable;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@AllArgsConstructor
@RequiredArgsConstructor
@EqualsAndHashCode(of = {"multipartFile"})
@Getter
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
	 * To trim the whole .kml document
	 */
	private boolean trimXml;
	
	/**
	 * To clear all the unused date stamps after every POIs exporting
	 */
	private boolean clearDescriptions;
	
	private boolean validateXml;
	
	/**
	 * Enables or disables using of {@link #path}
	 */
	private boolean setPath;
	/**
	 * Local or Http path to all the attachments inside HTML descriptions
	 */
	@Nullable
	private String path;
	
	/**
	 * Enables or disables using {@link #previewSize}
	 */
	private boolean setPreviewSize;
	
	/**
	 * Attached photos preview size (in width) in pixels
	 */
	@Nullable
	private Integer previewSize;
}
