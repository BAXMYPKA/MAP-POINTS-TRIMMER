package mrbaxmypka.gmail.com.mapPointsTrimmer.services;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import mrbaxmypka.gmail.com.mapPointsTrimmer.entitiesDto.MultipartDto;
import mrbaxmypka.gmail.com.mapPointsTrimmer.utils.DownloadAs;
import mrbaxmypka.gmail.com.mapPointsTrimmer.xml.KmlHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
public class MultipartFileService {

    private final String tempDir = System.getProperty("java.io.tmpdir");
    private final Locale defaultLocale = Locale.ENGLISH;
    private final MessageSource messageSource;
    private final KmlHandler kmlHandler;
    private final FileService fileService;
    /**
     * The temp file which is stored in a system temp directory
     */
    @Getter(value = AccessLevel.PACKAGE)
    private Path tempFile = Paths.get("");
    /**
     * To store the inner kml or gpx filename from archive
     */
    private String xmlFileName = "";

    @Autowired
    public MultipartFileService(KmlHandler kmlHandler, FileService fileService, MessageSource messageSource) {
        this.kmlHandler = kmlHandler;
        this.fileService = fileService;
        this.messageSource = messageSource;
    }

    /**
     * @param multipartDto
     * @param locale
     * @throws IOException To be treated in an ExceptionHandler method or ControllerAdvice level
     */
    public Path processMultipartDto(@NonNull MultipartDto multipartDto, @Nullable Locale locale)
            throws IOException, ParserConfigurationException, SAXException, TransformerException {
        log.info("{} has been received. Locale = {}", multipartDto, locale);

        locale = locale == null ? this.defaultLocale : locale;
        String processedXml;
        //Determine the extension of a given filename
        if (DownloadAs.KML.hasSameExtension(multipartDto.getMultipartFile().getOriginalFilename())) {
            xmlFileName = multipartDto.getMultipartFile().getOriginalFilename();
            processedXml = kmlHandler.processXml(multipartDto.getMultipartFile().getInputStream(), multipartDto);
        } else if (DownloadAs.KMZ.hasSameExtension(multipartDto.getMultipartFile().getOriginalFilename())) {
            InputStream kmlInputStream = getXmlFromZip(multipartDto, DownloadAs.KML, locale);
            processedXml = kmlHandler.processXml(kmlInputStream, multipartDto);
        } else {
            throw new IllegalArgumentException(messageSource.getMessage(
                    "exception.fileExtensionNotSupported",
                    new Object[]{multipartDto.getMultipartFile().getOriginalFilename()},
                    locale));
        }
        log.info("Xml file has been successfully processed.");
        return writeTempFile(processedXml, multipartDto, locale);
    }

    /**
     * @param multipartDto     Which contains .gpz, .kmz or any other ZIP archives
     * @param xmlFileExtension .kml, .gpx or any possible extension of the file to be extracted from the given ZIP
     *                         archive as it must be a single one inside.
     * @param locale           To send a localized error messages.
     * @return {@link InputStream} from the file with the given file extension (.kml as usual).
     * @throws IOException
     */
    private InputStream getXmlFromZip(MultipartDto multipartDto, DownloadAs xmlFileExtension, Locale locale)
            throws IOException {
        log.info("'{}' file is being extracted from the given MultipartDto", xmlFileExtension);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        try (ZipInputStream zis = new ZipInputStream(multipartDto.getMultipartFile().getInputStream())) {
            ZipEntry zipEntry;
            while ((zipEntry = zis.getNextEntry()) != null) {
                if (xmlFileExtension.hasSameExtension(zipEntry.getName())) {
                    //Size may be unknown if entry isn't written to disk yet!
//					byte[] buffer = new byte[(int) zipEntry.getSize()];
// 					zis.readNBytes(buffer, 0, (int) zipEntry.getSize());
                    xmlFileName = zipEntry.getName(); //To store it if .kml has to be returned
                    buffer.writeBytes(zis.readAllBytes());
                    log.info("File '{}' has been extracted from zip and will be returned as InputStream", xmlFileName);
                } else {
                    multipartDto.getImagesNamesFromZip().add(fileService.getFileName(zipEntry.getName()));
                }
            }
        }
        if (buffer.size() > 0) {
            return new ByteArrayInputStream(buffer.toByteArray());
        } else {
            throw new IllegalArgumentException(messageSource.getMessage("exception.noXmlInZipFound",
                    new Object[]{multipartDto.getMultipartFile().getOriginalFilename()}, locale));
        }
    }

    /**
     * https://stackoverflow.com/a/43836969/11592202
     * Relocates all the internal files from a given .kmz or .gpz from a User to the temp directory as a new kmz
     * with replacing an old inner .kml or .gpx with the new one.
     *
     * @param processedXml A processed xml String to be written to the resulting file.
     * @param multipartDto
     * @param locale       To return a locale-oriented messages
     * @return {@link Path} to a temp file to be returned to a User.
     * @throws IOException With the localized message to be returned to a User if the temp file writing is failed.
     */
    private void processTempZip(String processedXml, MultipartDto multipartDto, Locale locale)
            throws IOException {
        //The name of the zip file
        String zipFilename = getZipFilename(multipartDto);

        //It is important to save tempFile as the field to get an opportunity to delete it case of app crashing
        tempFile = Paths.get(tempDir.concat(zipFilename));
        ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(tempFile));
        if (DownloadAs.KMZ.equals(multipartDto.getDownloadAs()) &&
                DownloadAs.KMZ.hasSameExtension(multipartDto.getMultipartFile().getOriginalFilename())) {
            //If a User set "Download as .kmz" option and also has uploaded a .kmz file
            //Copy original images from the MultipartFile .kmz to the new temp .kmz
            processExistingZip(zos, processedXml, multipartDto);
        } else if (DownloadAs.KMZ.equals(multipartDto.getDownloadAs()) &&
                !DownloadAs.KMZ.hasSameExtension(multipartDto.getMultipartFile().getOriginalFilename())) {
            //If a User set "Download as .kmz" option but has uploaded a .kml file
            //Create a new .kmz file (where we have to write downloaded Google icons if any)
            createNewZip(zos, processedXml, multipartDto);
        }
/*
        //Write downloaded icons if there are
        String imagesFolderName = DownloadAs.KMZ.equals(multipartDto.getDownloadAs()) ? "files/" : "files/";
        for (Map.Entry<String, byte[]> iconEntry : multipartDto.getGoogleIconsToBeZipped().entrySet()) {
            ZipEntry zipEntry = new ZipEntry(imagesFolderName + iconEntry.getKey());
            zos.putNextEntry(zipEntry);
            zos.write(iconEntry.getValue());
            zos.closeEntry();
        }
        log.info("{} downloaded icons have been added to the resulting zip", multipartDto.getGoogleIconsToBeZipped().size());
*/
        writeDownloadedGoogleIcons(zos, multipartDto);
        writeLocusPictogram(zos, multipartDto);
        zos.close();
        log.info("Temp zip file {} has been written to the temp dir", tempFile);
    }

    private void writeDownloadedGoogleIcons(ZipOutputStream zos, MultipartDto multipartDto) throws IOException {
        //Write downloaded icons if there are
        String imagesFolderName = DownloadAs.KMZ.equals(multipartDto.getDownloadAs()) ? "files/" : "files/";
        for (Map.Entry<String, byte[]> iconEntry : multipartDto.getGoogleIconsToBeZipped().entrySet()) {
            ZipEntry zipEntry = new ZipEntry(imagesFolderName + iconEntry.getKey());
            zos.putNextEntry(zipEntry);
            zos.write(iconEntry.getValue());
            zos.closeEntry();
        }
        log.info("{} downloaded icons have been added to the resulting zip", multipartDto.getGoogleIconsToBeZipped().size());
    }

    private void writeLocusPictogram(ZipOutputStream zos, MultipartDto multipartDto) throws IOException {
        if (!multipartDto.isReplaceLocusIcons() ||
                multipartDto.getImagesNamesFromZip().contains(multipartDto.getPictogramName())) return;
        //Write Locus pictograms if there are
        String imagesFolderName = DownloadAs.KMZ.equals(multipartDto.getDownloadAs()) ? "files/" : "files/";
        String pictogramFullPath = fileService.getPictogramsNamesMap().get(multipartDto.getPictogramName());
//        byte[] pictogram = Objects.requireNonNull(this.getClass().getClassLoader().getResourceAsStream(pictogramFullPath))
//                .readAllBytes();
        byte[] pictogram = this.getClass().getClassLoader().getResourceAsStream(pictogramFullPath).readAllBytes();
        ZipEntry zipEntry = new ZipEntry(imagesFolderName + multipartDto.getPictogramName());
            zos.putNextEntry(zipEntry);
            zos.write(pictogram);
            zos.closeEntry();
        log.info("{} pictogram icon have been added to the resulting zip", multipartDto.getPictogramName());
    }

    private void processExistingZip(ZipOutputStream zos, String processedXml, MultipartDto multipartDto) throws IOException {
        log.info("To be downloaded as the {} zip file {} is being prepared to be written to the temp dir",
                multipartDto.getDownloadAs().getExtension(), tempFile);

        DownloadAs xmlFileExtension = DownloadAs.KMZ.equals(multipartDto.getDownloadAs()) ? DownloadAs.KML : DownloadAs.KML;
        //Copy original images from the MultipartFile to the temp zip
        ZipInputStream zis = new ZipInputStream(multipartDto.getMultipartFile().getInputStream());
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
            } else if (multipartDto.getFilesToBeExcluded().contains(fileService.getFileName(zipOutEntry.getName()))) {
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

    private void createNewZip(ZipOutputStream zos, String processedXml, MultipartDto multipartDto) throws IOException {
        log.info("To be downloaded as the {} file {} is being prepared to be written to the temp dir",
                multipartDto.getDownloadAs().getExtension(), tempFile);
        DownloadAs zipFileExtension = multipartDto.getDownloadAs();
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

    private String getZipFilename(MultipartDto multipartDto) {
        int extensionDotIndex = multipartDto.getMultipartFile().getOriginalFilename().lastIndexOf(".");
        //Cut off the extension
        String zipFilename = extensionDotIndex == -1 ?
                multipartDto.getMultipartFile().getOriginalFilename() :
                multipartDto.getMultipartFile().getOriginalFilename().substring(0, extensionDotIndex);
        if (DownloadAs.KMZ.equals(multipartDto.getDownloadAs())) {
            //Add the .kmz extension
            zipFilename = zipFilename + DownloadAs.KMZ.getExtension();
        }
        return zipFilename;
    }

    private Path writeTempFile(String processedXml, MultipartDto multipartDto, Locale locale) throws IOException {
//		String originalFilename = multipartDto.getMultipartFile().getOriginalFilename();
        if (DownloadAs.KML.equals(multipartDto.getDownloadAs())) {
            //Write .kml file
            log.info("Temp file will be written to the temp directory as '{}' before returning as it is.", xmlFileName);
            tempFile = Paths.get(tempDir.concat(xmlFileName));
            BufferedWriter bufferedWriter = Files.newBufferedWriter(tempFile, StandardCharsets.UTF_8);
            bufferedWriter.write(processedXml);
            bufferedWriter.close();
            log.info("Temp file has been written as '{}'", tempFile);
        } else if (DownloadAs.KMZ.equals(multipartDto.getDownloadAs())) {
            //Write .kmz file
            log.info("Temp file will be written as KMZ");
            processTempZip(processedXml, multipartDto, locale);
        }
        return tempFile;
    }

    public void deleteTempFile() {
        try {
            Files.deleteIfExists(tempFile);
            log.info("Temp file={} has been deleted", tempFile.toString());
        } catch (IOException e) {
            log.info("Deleting temp file has caused an exception:\n", e);
        }
    }
}
