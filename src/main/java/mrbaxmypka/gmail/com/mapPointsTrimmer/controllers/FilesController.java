package mrbaxmypka.gmail.com.mapPointsTrimmer.controllers;

import lombok.extern.slf4j.Slf4j;
import mrbaxmypka.gmail.com.mapPointsTrimmer.entitiesDto.MultipartFilterDto;
import mrbaxmypka.gmail.com.mapPointsTrimmer.entitiesDto.MultipartMainDto;
import mrbaxmypka.gmail.com.mapPointsTrimmer.services.MultipartFilterFileService;
import mrbaxmypka.gmail.com.mapPointsTrimmer.services.MultipartMainFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xml.sax.SAXException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

@Slf4j
@RestController
public class FilesController extends AbstractController {

    @Autowired
    private MultipartMainFileService multipartMainFileService;
    @Autowired
    private MultipartFilterFileService multipartFilterFileService;

    /**
     * @param multipartFileDto Can receive .kml or .kmz files only
     * @param locale           For defining a User language
     * @return The resulting processed file as the binary body into the response.
     */
/*
    @PostMapping(path = "/poi",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<FileSystemResource> postKml(
            @Valid @ModelAttribute MultipartMainDto multipartFileDto, Locale locale, HttpSession httpSession)
            throws IOException, SAXException, ParserConfigurationException, TransformerException, InterruptedException {
        log.info("{} file has been received as: {}.", MultipartMainDto.class.getSimpleName(), multipartFileDto);
        multipartFileDto.setSessionId(httpSession.getId());
        multipartFileDto.setLocale(locale);
        Path tempFile = multipartMainFileService.processMultipartMainDto(multipartFileDto);
        log.info("Temp file={}", tempFile);
        FileSystemResource resource = new FileSystemResource(tempFile);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + getAsciiEncodedFilename(tempFile) + "\"; filename*=UTF-8''" + getAsciiEncodedFilename(tempFile))
                .body(resource);
    }
*/

/*
    @PostMapping(path = "/filter",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<FileSystemResource> postZip(
            @Valid @ModelAttribute MultipartFilterDto multipartFileDto, Locale locale, HttpSession httpSession, Model model)
            throws IOException, SAXException, ParserConfigurationException, TransformerException {
        log.info("{} file has been received as: {}.", MultipartFilterDto.class.getSimpleName(), multipartFileDto);
        multipartFileDto.setSessionId(httpSession.getId());
        multipartFileDto.setLocale(locale);
        Path tempFile = multipartFilterFileService.processMultipartFilterDto(multipartFileDto);

        log.info("Temp file={}", tempFile);
        FileSystemResource resource = new FileSystemResource(tempFile);
        ResponseEntity<FileSystemResource> body = ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + getAsciiEncodedFilename(tempFile) + "\"; filename*=UTF-8''" + getAsciiEncodedFilename(tempFile))
                .body(resource);
        return body;
    }
*/


    /**
     * From the referenced Spring Boot document:
     * "... if the method handles the response itself
     * (by writing the response content directly, declaring an argument of type ServletResponse / HttpServletResponse for that purpose
     * not declaring a response argument in the handler method signature)"
     * If the method writes to the servletResponse directly.
     * In this case, there is nothing for spring to do; a return value of void tells spring "I got this" and it does nothing with the response.
     *
     * @param multipartFileDto Can receive .kml or .kmz files only
     * @param locale           For defining a User language
     * @return The resulting processed file as the binary body into the response.
     */
    @PostMapping(path = "/poi",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public void postKml(
            @Valid @ModelAttribute MultipartMainDto multipartFileDto,
            Locale locale,
            HttpSession httpSession,
            HttpServletResponse response)
            throws IOException, SAXException, ParserConfigurationException, TransformerException, InterruptedException {
        log.info("{} file has been received as: {}.", MultipartMainDto.class.getSimpleName(), multipartFileDto);

        multipartFileDto.setSessionId(httpSession.getId());
        multipartFileDto.setLocale(locale);

        Path tempFile = multipartMainFileService.processMultipartMainDto(multipartFileDto);
        log.info("Temp file={}", tempFile);

        response.setHeader(
                "Content-Disposition",
                "attachment; filename=\"" + getAsciiEncodedFilename(tempFile) + "\"; filename*=UTF-8''" + getAsciiEncodedFilename(tempFile));

        ServletOutputStream outputStream = response.getOutputStream();
        InputStream inputStream = Files.newInputStream(tempFile);
        outputStream.write(inputStream.readAllBytes());

        outputStream.close();
        inputStream.close();

        multipartMainFileService.deleteTempFile(httpSession.getId());
    }

    /**
     * From the referenced Spring Boot document:
     * "... if the method handles the response itself
     * (by writing the response content directly, declaring an argument of type ServletResponse / HttpServletResponse for that purpose
     * not declaring a response argument in the handler method signature)"
     * If the method writes to the servletResponse directly.
     * In this case, there is nothing for spring to do; a return value of void tells spring "I got this" and it does nothing with the response.
     *
     * @param multipartFileDto
     * @param locale
     * @param httpSession
     * @param response
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws TransformerException
     */
    @PostMapping(path = "/filter",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public void postZip(
            @Valid @ModelAttribute MultipartFilterDto multipartFileDto,
            Locale locale,
            HttpSession httpSession,
            HttpServletResponse response)
            throws IOException, SAXException, ParserConfigurationException, TransformerException {
        log.info("{} file has been received as: {}.", MultipartFilterDto.class.getSimpleName(), multipartFileDto);
        multipartFileDto.setSessionId(httpSession.getId());
        multipartFileDto.setLocale(locale);
        Path tempFile = multipartFilterFileService.processMultipartFilterDto(multipartFileDto);

        log.info("Temp file={}", tempFile);

        response.setHeader("Content-Disposition", "attachment; filename=\"" + getAsciiEncodedFilename(tempFile) + "\"; filename*=UTF-8''" + getAsciiEncodedFilename(tempFile));
        response.setContentType("application/zip");
        ServletOutputStream outputStream = response.getOutputStream();
        InputStream inputStream = Files.newInputStream(tempFile);
        outputStream.write(inputStream.readAllBytes());

        outputStream.close();
        inputStream.close();

        multipartFilterFileService.deleteTempFile(httpSession.getId());
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
