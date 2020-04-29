package mrbaxmypka.gmail.com.LocusPOIconverter.entitiesDto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;

@AllArgsConstructor
@RequiredArgsConstructor
@EqualsAndHashCode(of = {"multipartFile"})
@Getter
public class MultipartDto {
	
	@NonNull //Lombok
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
	
	private boolean clearDescriptions;
	
	private boolean validateXml;
	
	private boolean setPath;
	
	private String path;
	
	private boolean setPreviewSize;
	
	private Integer previewSize;
}
