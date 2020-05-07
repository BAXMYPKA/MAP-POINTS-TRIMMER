package mrbaxmypka.gmail.com.LocusPOIconverter.entitiesDto;

import lombok.*;
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
	private boolean clearDescriptions;
	
	private boolean validateXml;
	
	/**
	 * Enables or disables using of {@link #path}
	 */
	private boolean setPath;
	
	//TODO: to complete path types
	private Enum pathType;
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
