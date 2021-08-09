package mrbaxmypka.gmail.com.mapPointsTrimmer.entitiesDto;

import lombok.*;

import java.nio.file.Path;

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

    private MultipartDto multipartDto;
    private Path tempFile;
    private String xmlFilename = "";
}
