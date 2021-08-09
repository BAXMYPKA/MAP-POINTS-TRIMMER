package mrbaxmypka.gmail.com.mapPointsTrimmer.controllers;

import lombok.extern.slf4j.Slf4j;
import mrbaxmypka.gmail.com.mapPointsTrimmer.entitiesDto.MultipartDto;
import mrbaxmypka.gmail.com.mapPointsTrimmer.services.MultipartFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.xml.sax.SAXException;

import javax.validation.Valid;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

@Slf4j
@RestController
public class FilesController extends AbstractController {

    @Autowired
    private MultipartFileService multipartFileService;
    private Path tempFile;

    /**
     * @param file   Can receive .kml or .kmz files only
     * @param locale For defining a User language
     * @return
     */
    @PostMapping(path = "/poi",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<FileSystemResource> postKml(
            @Valid @ModelAttribute MultipartDto file, Locale locale)
            throws IOException, SAXException, ParserConfigurationException, TransformerException, ExecutionException, InterruptedException {
        log.info("{} file has been received as: {}.", MultipartDto.class.getSimpleName(), file);
        System.out.println("CONTROLLER THREAD = " + Thread.currentThread().getName() + " ID = " + Thread.currentThread().getId());
        tempFile = multipartFileService.processMultipartDto(file, locale);
        log.info("Temp file={}", tempFile);
        FileSystemResource resource = new FileSystemResource(tempFile);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + getAsciiEncodedFilename(tempFile) + "\"; filename*=UTF-8''" + getAsciiEncodedFilename(tempFile))
                .body(resource);
    }

    /**
     * When close tab, close browser or refresh the page the browser sends a 'navigator.sendBeacon('/stop')' POST signal
     * to stop the processing.
     */
    @PostMapping(path = "/stop")
    @ResponseBody
    public void postStopBeacon() {
        log.info("A refresh or close tab event has been received to stop the processing!");
    }

    private String getAsciiEncodedFilename(Path pathToFile) {
        try {
            URI encodedFilename = new URI(null, null, pathToFile.getFileName().toString(), null);
            return encodedFilename.toASCIIString();
        } catch (URISyntaxException e) {
            log.info(e.getMessage(), e);
            return pathToFile.getFileName().toString().replaceAll("\\s", "");
        }
    }
}
