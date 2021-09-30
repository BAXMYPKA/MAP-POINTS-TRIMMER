package mrbaxmypka.gmail.com.mapPointsTrimmer.services;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import mrbaxmypka.gmail.com.mapPointsTrimmer.entitiesDto.MultipartDto;
import mrbaxmypka.gmail.com.mapPointsTrimmer.entitiesDto.MultipartMainDto;
import mrbaxmypka.gmail.com.mapPointsTrimmer.utils.DownloadAs;
import mrbaxmypka.gmail.com.mapPointsTrimmer.xml.KmlHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
@Getter(AccessLevel.PROTECTED)
public class MultipartMainFileService {

    private final String TEMP_DIR = System.getProperty("java.io.tmpdir");
    private final Locale DEFAULT_LOCALE = Locale.ENGLISH;
    private final String PICTOGRAMS_FULL_PATH_PREFIX = "static/";
    private final MessageSource messageSource;
    private final KmlHandler kmlHandler;
    private final FileService fileService;
    @Getter(AccessLevel.PACKAGE)
    private final Set<MultipartDto> tempFiles = new HashSet<>(2);

    @Autowired
    public MultipartMainFileService(KmlHandler kmlHandler, FileService fileService, MessageSource messageSource) {
        this.kmlHandler = kmlHandler;
        this.fileService = fileService;
        this.messageSource = messageSource;
    }

    /**
     * @param multipartMainDto
     * @return The resulting {@link Path} in a temp directory with the processed file for a User.
     * @throws IOException To be treated in an ExceptionHandler method or ControllerAdvice level
     */
    public Path processMultipartMainDto(@NonNull MultipartMainDto multipartMainDto)
            throws IOException, ParserConfigurationException, SAXException, TransformerException, InterruptedException {
        log.info("{} has been received. Locale = {}", multipartMainDto, multipartMainDto.getLocale());
        tempFiles.add(multipartMainDto);
        String processedXml;
        //Determine the extension of a given filename
        if (DownloadAs.KML.hasSameExtension(multipartMainDto.getMultipartFile().getOriginalFilename())) {
            String fileName = fileService.getFileName(multipartMainDto.getMultipartFile().getOriginalFilename());
            multipartMainDto.setXmlFilename(fileName);
            processedXml = kmlHandler.processXml(multipartMainDto.getMultipartFile().getInputStream(), multipartMainDto);
        } else if (DownloadAs.KMZ.hasSameExtension(multipartMainDto.getMultipartFile().getOriginalFilename())) {
            InputStream kmlInputStream = getXmlFromZip(multipartMainDto, DownloadAs.KML, multipartMainDto.getLocale());
            processedXml = kmlHandler.processXml(kmlInputStream, multipartMainDto);
        } else {
            throw new IllegalArgumentException(messageSource.getMessage(
                    "exception.fileExtensionNotSupported",
                    new Object[]{multipartMainDto.getMultipartFile().getOriginalFilename()},
                    multipartMainDto.getLocale()));
        }
        log.info("Xml file has been successfully processed.");
        return writeTempFile(processedXml, multipartMainDto, multipartMainDto.getLocale());
    }

    /**
     * 1) Looks through all the .zip(.kmz)
     * 2) Searches for .xml (.kml) file inside the given .zip (.kml) to return it in the end
     * 3) Adds all found images files to the instant cache in {@link MultipartMainDto#getImagesNamesFromZip()}
     *
     * @param multipartMainDto Which contains .gpz, .kmz or any other ZIP archives
     * @param xmlFileExtension .kml, .gpx or any possible extension of the file to be extracted from the given ZIP
     *                         archive as it must be a single one inside.
     * @param locale           To send a localized error messages.
     * @return {@link InputStream} from the file with the given file extension (.kml as usual).
     * @throws IOException If something wrong with the given .zip file
     */
    private InputStream getXmlFromZip(MultipartMainDto multipartMainDto, DownloadAs xmlFileExtension, Locale locale)
            throws IOException {
        log.info("'{}' file is being extracted from the given MultipartDto", xmlFileExtension);
        ByteArrayOutputStream xmlBuffer = new ByteArrayOutputStream();

        try (ZipInputStream zis = new ZipInputStream(multipartMainDto.getMultipartFile().getInputStream())) {
            ZipEntry zipEntry;
            while ((zipEntry = zis.getNextEntry()) != null) {
                if (xmlFileExtension.hasSameExtension(zipEntry.getName())) {
                    //Size may be unknown if entry isn't written to disk yet!
//					byte[] buffer = new byte[(int) zipEntry.getSize()];
// 					zis.readNBytes(buffer, 0, (int) zipEntry.getSize());
                    multipartMainDto.setXmlFilename(zipEntry.getName()); //To store it if .kml has to be returned
                    xmlBuffer.writeBytes(zis.readAllBytes());
                    log.info("File '{}' has been extracted from zip and will be returned as InputStream", multipartMainDto.getXmlFilename());
                } else {
                    addImageNameFromZip(zipEntry, multipartMainDto);
                }
            }
        }
        if (xmlBuffer.size() > 0) {
            return new ByteArrayInputStream(xmlBuffer.toByteArray());
        } else {
            throw new IllegalArgumentException(messageSource.getMessage("exception.noXmlInZipFound",
                    new Object[]{multipartMainDto.getMultipartFile().getOriginalFilename()}, locale));
        }
    }

    protected void addImageNameFromZip(ZipEntry zipEntry, MultipartDto multipartDto) {
        String filename = fileService.getFileName(zipEntry.getName());
        if (!filename.contains(".")) return; //The filename doesn't contain an extension
        String extension = filename.substring(filename.lastIndexOf(".")).toLowerCase();
        if (fileService.getAllowedImagesExtensions().contains(extension)) {
            multipartDto.getImagesNamesFromZip().add(filename);
        }
    }

    /**
     * https://stackoverflow.com/a/43836969/11592202
     * Relocates all the internal files from a given .kmz or .gpz from a User to the temp directory as a new kmz
     * with replacing an old inner .kml or .gpx with the new one.
     *
     * @param processedXml     A processed xml String to be written to the resulting file.
     * @param multipartMainDto
     * @param locale           To return a locale-oriented messages
     * @return {@link Path} to a temp file to be returned to a User.
     * @throws IOException With the localized message to be returned to a User if the temp file writing is failed.
     */
    private void processTempZip(String processedXml, MultipartMainDto multipartMainDto, Locale locale)
            throws IOException {
        //The name of the zip file
        String zipFilename = getZipFilename(multipartMainDto);

        //It is important to save tempFile as the field to get an opportunity to delete it case of app crashing
        Path tempFile = Paths.get(TEMP_DIR.concat(zipFilename));
        multipartMainDto.setTempFile(tempFile);

        ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(tempFile));
        if (DownloadAs.KMZ.equals(multipartMainDto.getDownloadAs()) &&
                DownloadAs.KMZ.hasSameExtension(multipartMainDto.getMultipartFile().getOriginalFilename())) {
            //If a User set "Download as .kmz" option and also has uploaded a .kmz file
            //Copy original images from the MultipartFile .kmz to the new temp .kmz
            processExistingZip(zos, processedXml, multipartMainDto, tempFile);
        } else if (DownloadAs.KMZ.equals(multipartMainDto.getDownloadAs()) &&
                !DownloadAs.KMZ.hasSameExtension(multipartMainDto.getMultipartFile().getOriginalFilename())) {
            //If a User set "Download as .kmz" option but has uploaded a .kml file
            //Create a new .kmz file (where we have to write downloaded Google icons if any)
            createNewZip(zos, processedXml, multipartMainDto, tempFile);
        }
        writeDownloadedGoogleIcons(zos, multipartMainDto);
        writeLocusPictogram(zos, multipartMainDto);
        zos.close();
        log.info("Temp zip file {} has been written to the temp dir", tempFile);
    }

    private void writeDownloadedGoogleIcons(ZipOutputStream zos, MultipartMainDto multipartMainDto) throws IOException {
        //Write downloaded icons if there are
        String imagesFolderName = DownloadAs.KMZ.equals(multipartMainDto.getDownloadAs()) ? "files/" : "files/";
        for (Map.Entry<String, byte[]> iconEntry : multipartMainDto.getGoogleIconsToBeZipped().entrySet()) {
            ZipEntry zipEntry = new ZipEntry(imagesFolderName + iconEntry.getKey());
            zos.putNextEntry(zipEntry);
            zos.write(iconEntry.getValue());
            zos.closeEntry();
        }
        log.info("{} downloaded icons have been added to the resulting zip", multipartMainDto.getGoogleIconsToBeZipped().size());
    }

    private void writeLocusPictogram(ZipOutputStream zos, MultipartMainDto multipartMainDto) throws IOException {
        if (!multipartMainDto.isReplaceLocusIcons() ||
                multipartMainDto.getImagesNamesFromZip().contains(multipartMainDto.getPictogramName())) return;
        //Write Locus pictograms if there are
        String imagesFolderName = DownloadAs.KMZ.equals(multipartMainDto.getDownloadAs()) ? "files/" : "files/";
        String pictogramFullPath = PICTOGRAMS_FULL_PATH_PREFIX +
                fileService.getPictogramsNamesPaths().get(multipartMainDto.getPictogramName());
        byte[] pictogram = Objects.requireNonNull(this.getClass().getClassLoader().getResourceAsStream(pictogramFullPath))
                .readAllBytes();
        ZipEntry zipEntry = new ZipEntry(imagesFolderName + multipartMainDto.getPictogramName());
        zos.putNextEntry(zipEntry);
        zos.write(pictogram);
        zos.closeEntry();
        log.info("{} pictogram icon have been added to the resulting zip", multipartMainDto.getPictogramName());
    }

    private void processExistingZip(ZipOutputStream zos, String processedXml, MultipartMainDto multipartMainDto, Path tempFile) throws IOException {
        log.info("To be downloaded as the {} zip file {} is being prepared to be written to the temp dir",
                multipartMainDto.getDownloadAs().getExtension(), tempFile);

        DownloadAs xmlFileExtension = DownloadAs.KMZ.equals(multipartMainDto.getDownloadAs()) ? DownloadAs.KML : DownloadAs.KML;
        //Copy original images from the MultipartFile to the temp zip
        ZipInputStream zis = new ZipInputStream(multipartMainDto.getMultipartFile().getInputStream());
        ZipEntry zipInEntry;
        while ((zipInEntry = zis.getNextEntry()) != null) {
            ZipEntry zipOutEntry = new ZipEntry(zipInEntry.getName());
            zipOutEntry.setComment(zipInEntry.getComment());
            zipOutEntry.setExtra(zipInEntry.getExtra());

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            if (xmlFileExtension.hasSameExtension(zipInEntry.getName())) {
                //Replace existing .xml (.kml) file with the new one
                zos.putNextEntry(zipOutEntry);
                buffer.writeBytes(processedXml.getBytes(StandardCharsets.UTF_8));
            } else if (multipartMainDto.getFilesToBeExcluded().contains(fileService.getFileName(zipOutEntry.getName()))) {
                //If a file has not to be included in the resultant .zip just skip it
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

    /**
     * As some old versions of Edge browser (at least Microsoft EdgeHTML 17 +) attach {@link MultipartFile#getOriginalFilename()}
     * as the full path to the file (e.g. "C:\temp\Points.kmz" instead of just "Points.kmz"), getting just the filename
     * is a bit more complicated.
     *
     * @param multipartMainDto A filename to be extracted from.
     * @return The filename of a .zip(.kmz) archive from a given {@link MultipartMainDto}
     */
    private String getZipFilename(MultipartMainDto multipartMainDto) {
        String filename = Objects.requireNonNullElse(multipartMainDto.getMultipartFile().getOriginalFilename(), "default.kmz");
        if (filename.contains("/") || filename.contains("\\")) {
            //MultipartFile contains a full path with the filename
            int slashIndex = filename.lastIndexOf("/") != -1 ?
                    filename.lastIndexOf("/") :
                    filename.lastIndexOf("\\");
            filename = filename.substring(slashIndex + 1);
        }
        int extensionDotIndex = filename.lastIndexOf(".");
        //Cut off the extension
        String zipFilename = extensionDotIndex == -1 ?
                filename :
                filename.substring(0, extensionDotIndex);
        if (DownloadAs.KMZ.equals(multipartMainDto.getDownloadAs())) {
            //Add the .kmz extension
            zipFilename = zipFilename + DownloadAs.KMZ.getExtension();
        }
        return zipFilename;
    }

    private Path writeTempFile(String processedXml, MultipartMainDto multipartMainDto, Locale locale)
            throws IOException {
        if (DownloadAs.KML.equals(multipartMainDto.getDownloadAs())) {
            //Write .kml file
            Path tempFile = Paths.get(TEMP_DIR.concat(multipartMainDto.getXmlFilename()));
            multipartMainDto.setTempFile(tempFile);
            log.info("Temp file will be written to the temp directory as '{}' before returning as it is.", multipartMainDto.getTempFile());
            BufferedWriter bufferedWriter = Files.newBufferedWriter(tempFile, StandardCharsets.UTF_8);
            bufferedWriter.write(processedXml);
            bufferedWriter.close();
            log.info("Temp file has been written as '{}'", tempFile);
        } else if (DownloadAs.KMZ.equals(multipartMainDto.getDownloadAs())) {
            //Write .kmz file
            log.info("Temp file will be written as KMZ");
            processTempZip(processedXml, multipartMainDto, locale);
        }
        return multipartMainDto.getTempFile();
    }

    /**
     * Deletes all the temp files for the end-users in the server temp directory.
     */
    public void deleteTempFiles() {
        if (tempFiles.isEmpty()) return;

        tempFiles.removeIf(multipartDto -> {
            if (multipartDto.getTempFile() != null) {
                try {
                    Files.deleteIfExists(multipartDto.getTempFile());
                    log.info("Temp file={} has been deleted", multipartDto.getTempFile().toString());
                } catch (IOException e) {
                    log.info("Deleting temp file has caused an exception:\n", e);
                    return true;
                }
            }
            return true;
        });
        log.info("All temp files have been deleted!");
    }

    /**
     * @param sessionId Deletes the specific temp file by its sessionId from the server temp directory.
     */
    public void deleteTempFile(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) return;

        tempFiles.removeIf(multipartDto -> {
            if (multipartDto.getSessionId() != null && multipartDto.getSessionId().equals(sessionId)) {
                try {
                    Files.deleteIfExists(multipartDto.getTempFile());
                } catch (IOException e) {
                    log.info("Deleting temp file has caused an exception:\n", e);
                }
                log.info("Temp file={} has been deleted", multipartDto.getTempFile().toString());
                return true;
            } else {
                return false;
            }
        });
    }
}
