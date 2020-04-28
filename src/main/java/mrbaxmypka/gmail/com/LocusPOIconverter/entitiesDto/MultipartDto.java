package mrbaxmypka.gmail.com.LocusPOIconverter.entitiesDto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;

@AllArgsConstructor
@RequiredArgsConstructor
@Getter
public class MultipartDto {
	
	@NonNull //Lombok
	@NotNull
	private MultipartFile multipartFile;
	
	private boolean trimDescriptions;
	
	private boolean trimXml;
	
	private boolean validateXml;
	
	private boolean setPath;
	
	private String path;
	
	private boolean setPreviewSize;
	
	private Integer previewSize;
}
