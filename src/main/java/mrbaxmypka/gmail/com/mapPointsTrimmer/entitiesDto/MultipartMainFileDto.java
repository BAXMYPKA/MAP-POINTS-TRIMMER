package mrbaxmypka.gmail.com.mapPointsTrimmer.entitiesDto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mrbaxmypka.gmail.com.mapPointsTrimmer.services.MultipartFileService;

import java.nio.file.Path;

/**
 * Keeps all the necessary information for using within {@link MultipartFileService}.
 */
@NoArgsConstructor
@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
public class MultipartMainFileDto {

    public MultipartMainFileDto(MultipartMainDto multipartMainDto, Path tempFile) {
        this.multipartMainDto = multipartMainDto;
        this.tempFile = tempFile;
    }

    public MultipartMainFileDto(String xmlFilename) {
        this.xmlFilename = xmlFilename;
    }

    public MultipartMainFileDto(MultipartMainDto multipartMainDto) {
        this.multipartMainDto = multipartMainDto;
    }

    /**
     * The main object from a User
     */
    private MultipartMainDto multipartMainDto;
    /**
     * A .zip(.kmz) or .kml filename in the Temp directory for an according {@link MultipartMainDto}
     */
    private Path tempFile;
    /**
     * A filename of a standalone .xml(.kml) file or a .kml file inside a given .kmz
     */
    private String xmlFilename = "";
}
