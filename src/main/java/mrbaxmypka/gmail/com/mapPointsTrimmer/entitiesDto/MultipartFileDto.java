package mrbaxmypka.gmail.com.mapPointsTrimmer.entitiesDto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.nio.file.Path;

/**
 * Keeps all the necessary information for using within {@link mrbaxmypka.gmail.com.mapPointsTrimmer.services.MultipartFileService}.
 */
@NoArgsConstructor
@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
public class MultipartFileDto {

    public MultipartFileDto(MultipartDto multipartDto, Path tempFile) {
        this.multipartDto = multipartDto;
        this.tempFile = tempFile;
    }

    public MultipartFileDto(String xmlFilename) {
        this.xmlFilename = xmlFilename;
    }

    public MultipartFileDto(MultipartDto multipartDto) {
        this.multipartDto = multipartDto;
    }

    /**
     * The main object from a User
     */
    private MultipartDto multipartDto;
    /**
     * A .zip(.kmz) or .kml filename in the Temp directory for an according {@link MultipartDto}
     */
    private Path tempFile;
    /**
     * A filename of a standalone .xml(.kml) file or a .kml file inside a given .kmz
     */
    private String xmlFilename = "";
}
