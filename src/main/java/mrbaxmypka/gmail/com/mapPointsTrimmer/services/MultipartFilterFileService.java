package mrbaxmypka.gmail.com.mapPointsTrimmer.services;

import lombok.extern.slf4j.Slf4j;
import mrbaxmypka.gmail.com.mapPointsTrimmer.entitiesDto.MultipartFilterDto;
import mrbaxmypka.gmail.com.mapPointsTrimmer.entitiesDto.MultipartMainDto;
import mrbaxmypka.gmail.com.mapPointsTrimmer.xml.KmlHandler;
import mrbaxmypka.gmail.com.mapPointsTrimmer.xml.XmlHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
public class MultipartFilterFileService extends MultipartMainFileService {

    private XmlHandler xmlHandler;

    @Autowired
    public MultipartFilterFileService(
            KmlHandler kmlHandler, FileService fileService, MessageSource messageSource, XmlHandler xmlHandler) {
        super(kmlHandler, fileService, messageSource);
        this.xmlHandler = xmlHandler;
    }

    public Path processMultipartFilterDto(MultipartFilterDto multipartFilterDto, Locale locale)
            throws IOException, ParserConfigurationException, SAXException, TransformerException {
        String zipFilename = getFileService().getFileName(multipartFilterDto.getMultipartZipFile().getOriginalFilename());
        String xmlFilename = getFileService().getFileName(multipartFilterDto.getMultipartXmlFile().getOriginalFilename());
        checkCorrectZipFilename(zipFilename, locale);
        checkCorrectXmlFilename(xmlFilename, locale);
        if (getFileService().getExtension(xmlFilename).equals("kmz")) {
            //TODO: to treat
            return null;
        } else if (getFileService().getExtension(xmlFilename).equals("txt")) {
            //
        } else if (getFileService().getExtension(xmlFilename).equals("kml") ||
                getFileService().getExtension(xmlFilename).equals("xml")) {
            Document document = xmlHandler.getDocument(multipartFilterDto.getMultipartFile().getInputStream());
            String documentAsString = xmlHandler.getAsString(document);
            setImagesNamesFromZip(multipartFilterDto);
            return processTempZip(documentAsString, multipartFilterDto, locale);
        }
        return null;
    }

    private void checkCorrectZipFilename(String zipFilename, Locale locale) throws IllegalArgumentException {
        checkNullEmptyFilename(zipFilename, locale);
        if (getFileService().getZipExtensions().contains(getFileService().getExtension(zipFilename))) {
            throw new IllegalArgumentException(getMessageSource().getMessage(
                    "exception.filenameNotSupported", new Object[]{zipFilename}, locale));
        }
    }

    private void checkCorrectXmlFilename(String xmlFilename, Locale locale) {
        checkNullEmptyFilename(xmlFilename, locale);
        if (getFileService().getAllowedXmlExtensions().contains(getFileService().getExtension(xmlFilename))) {
            throw new IllegalArgumentException(getMessageSource().getMessage(
                    "exception.filenameNotSupported", new Object[]{xmlFilename}, locale));
        }
    }

    private void checkNullEmptyFilename(String filename, Locale locale) throws IllegalArgumentException {
        if (filename == null || filename.isBlank()) {
            throw new IllegalArgumentException(getMessageSource().getMessage(
                    "exception.nullFilename", null, locale));
        } else if (getFileService().getExtension(filename).isBlank()) {
            throw new IllegalArgumentException(getMessageSource().getMessage(
                    "exception.fileExtensionNotSupported", new Object[]{filename}, locale));
        }
    }

    /**
     * Looks through all the given .zip(.kmz) file for images and adds them into the instant cache in {@link MultipartFilterDto#getImagesNamesFromZip()}
     *
     * @param multipartFilterDto Which contains .gpz, .kmz or any other ZIP archives
     * @throws IOException If something wrong with the given .zip file
     */
    private void setImagesNamesFromZip(MultipartFilterDto multipartFilterDto)
            throws IOException {
        try (ZipInputStream zis = new ZipInputStream(multipartFilterDto.getMultipartZipFile().getInputStream())) {
            ZipEntry zipEntry;
            while ((zipEntry = zis.getNextEntry()) != null) {
                addImageNameFromZip(zipEntry, multipartFilterDto);
            }
        }
        log.info("All images files are being processed from the given MultipartFilterDto");
    }

    /**
     * https://stackoverflow.com/a/43836969/11592202
     * Relocates all the internal files from a given .kmz or .gpz from a User to the temp directory as a new kmz
     * with replacing an old inner .kml or .gpx with the new one.
     *
     * @param processedXml       A processed xml String to be written to the resulting file.
     * @param multipartFilterDto
     * @param locale             To return a locale-oriented messages
     * @return {@link Path} to a temp file to be returned to a User.
     * @throws IOException With the localized message to be returned to a User if the temp file writing is failed.
     */
    private Path processTempZip(String processedXml, MultipartFilterDto multipartFilterDto, Locale locale)
            throws IOException {
        //The name of the zip file
        String zipFilename = getZipFilename(multipartFilterDto);

        //It is important to save tempFile as the field to get an opportunity to delete it case of app crashing
        Path tempFile = Paths.get(getTEMP_DIR().concat(zipFilename));
        multipartFilterDto.setTempFile(tempFile);

        ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(tempFile));

        //Copy original images from the MultipartFile .zip to the new temp .zip
        processExistingZip(zos, processedXml, multipartFilterDto, tempFile);
        zos.close();
        log.info("Temp zip file {} has been written to the temp dir", tempFile);
        return multipartFilterDto.getTempFile();
    }

    private void processExistingZip(
            ZipOutputStream zos, String processedXml, MultipartFilterDto multipartFilterDto, Path tempFile)
            throws IOException {
        //Copy original images from the MultipartFile to the temp zip
        ZipInputStream zis = new ZipInputStream(multipartFilterDto.getMultipartZipFile().getInputStream());
        ZipEntry zipInEntry;
        while ((zipInEntry = zis.getNextEntry()) != null) {
            ZipEntry zipOutEntry = new ZipEntry(zipInEntry.getName());
            zipOutEntry.setComment(zipInEntry.getComment());
            zipOutEntry.setExtra(zipInEntry.getExtra());

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            String imageFileName = getFileService().getFileName(zipOutEntry.getName());

            if (multipartFilterDto.getFilesToBeExcluded().contains(imageFileName)) {
                //If a file has not to be included in the resultant .zip just skip it
                continue;
            } else if (!processedXml.contains(imageFileName)) {
                //This image is redundant as the user's xml doesn't contain it, just skip this image from .zip
                continue;
            } else {
                zos.putNextEntry(zipOutEntry);
                buffer.writeBytes(zis.readAllBytes());
            }
            zos.write(buffer.toByteArray());
            zos.closeEntry();
        }
        log.info("Images from the User's zip have been added to the {}", tempFile);
    }

/*
    private void createNewZip(ZipOutputStream zos, String processedXml, MultipartMainDto multipartMainDto, Path tempFile) throws IOException {
        log.info("To be downloaded as the {} file {} is being prepared to be written to the temp dir",
                multipartMainDto.getDownloadAs().getExtension(), tempFile);
        DownloadAs zipFileExtension = multipartMainDto.getDownloadAs();
        if (DownloadAs.KMZ.equals(zipFileExtension)) {
            //Create a new zip
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            zos.putNextEntry(new ZipEntry("doc.kml"));
            buffer.writeBytes(processedXml.getBytes(StandardCharsets.UTF_8));
            zos.write(buffer.toByteArray());
            zos.closeEntry();
        }
        log.info("The new zip has been created as {} with the added xml root file", tempFile);
    }
*/

    /**
     * As some old versions of Edge browser (at least Microsoft EdgeHTML 17 +) attach {@link MultipartFile#getOriginalFilename()}
     * as the full path to the file (e.g. "C:\temp\Points.kmz" instead of just "Points.kmz"), getting just the filename
     * is a bit more complicated.
     *
     * @param multipartFilterDto A filename to be extracted from.
     * @return The filename of a .zip(.kmz) archive from a given {@link MultipartMainDto}
     */
    private String getZipFilename(MultipartFilterDto multipartFilterDto) {
        String zipFilename = Objects.requireNonNullElse(
                multipartFilterDto.getMultipartZipFile().getOriginalFilename(), "default.zip");
        if (zipFilename.contains("/") || zipFilename.contains("\\")) {
            //MultipartFile contains a full path with the filename
            int slashIndex = zipFilename.lastIndexOf("/") != -1 ?
                    zipFilename.lastIndexOf("/") :
                    zipFilename.lastIndexOf("\\");
            zipFilename = zipFilename.substring(slashIndex + 1);
        }
        return zipFilename;
    }
}
