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
import java.util.concurrent.*;

@Slf4j
@RestController
public class FilesController extends AbstractController {

    @Autowired
    private MultipartFileService multipartFileService;

    private ExecutorService executorService;

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
        tempFile = getPathFromNewThread(file, locale);
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

        //TODO: to delete the following string
        System.out.println("\"A refresh or close tab event has been received to stop the processing!\"");

        if (executorService != null) {

            System.out.println("SHUTTING DOWN THE PROCESS");

            executorService.shutdown();
            multipartFileService.deleteTempFile();
            log.info("The file processing has being shutting down!");
            try {
                if (!executorService.awaitTermination(1000, TimeUnit.MILLISECONDS)) {
                    log.info("The file processing has shut down!");
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                log.info("The file processing has been completely shut down!");
                executorService.shutdownNow();
            }
        }
    }

    private Path getPathFromNewThread(MultipartDto file, Locale locale) throws ExecutionException {
        int processors = Runtime.getRuntime().availableProcessors();
        if (processors > 2) {
            processors = processors - 1;
        }
        System.out.println("PROCESSORS = " + processors);
        Callable<Path> tempCallablePath = () -> multipartFileService.processMultipartDto(file, locale);
        executorService = Executors.newFixedThreadPool(processors);
        Future<Path> futureSubmit = executorService.submit(tempCallablePath);
        try {
            tempFile = futureSubmit.get();
        } catch (CancellationException | InterruptedException ce) {
            log.info("The file processing has been cancelled!", ce);
        }
        return tempFile;
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
