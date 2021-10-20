package mrbaxmypka.gmail.com.mapPointsTrimmer.services;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * @author BAXMYPKA
 */
@Slf4j
@Component
public class FileService {

    @Autowired
    private final MessageSource messageSource;

    /**
     * All possible images files extensions in lower case.
     */
    @Getter(AccessLevel.PUBLIC)
    private final List<String> allowedImagesExtensions;

    /**
     * All possible images files extensions in lower case.
     */
    @Getter(AccessLevel.PUBLIC)
    private final List<String> allowedZipExtensions;

    /**
     * Allowed file extensions to be loaded as xml files from index.html 'xmlFile' form.
     * .kmz files also allowed as inner .kml file will be extracted and processed.
     */
    @Getter(AccessLevel.PUBLIC)
    private final List<String> allowedXmlExtensions;

    /**
     * Collects a list of pictograms names ONLY as '.png' OR '.PNG' files from the 'resources/static/pictograms' directory.
     * <p>
     * {@literal ArrayList<String> pictogramNames} or an empty Array if nothing found.
     */
    @Getter(AccessLevel.PUBLIC)
    private final ArrayList<String> pictogramsNames = new ArrayList<>(40);

    /**
     * MUST be set AFTER {@link #setPictogramNames()}
     * Collects a Map collection of only pictograms names (.png files) and they relative paths
     * from the 'resources/static/pictograms' directory.
     * Important!
     * For Thymeleaf view by default the path = "pictograms/image.png" because Thymeleaf gets them from its own relative path.
     * But further in the {@link MultipartMainFileService} we have to add the "static/" prefix (e.g. "static/pictograms/image.png")
     * as the serverside searches for resources by its Classloader from the root of the "resources" directory.
     * <p>
     * <p>
     * {@link HashMap} pictogramNames where
     * key = pictogram name
     * value = full pictogram path
     * e.g.: key=pictureName.png value=pictograms/pictureName.png
     * or an empty Map if nothing found.
     */
    @Getter(AccessLevel.PUBLIC)
    private final Map<String, String> pictogramsNamesPaths = new HashMap<>(40);

    @Value("${logging.file.name}")
    private String pathToLogFile;

    private final String PICTOGRAMS_PATH = "pictograms/";

    private String stackTrace;

    private final String IMG_PATH_WITH_FILENAME_REGEX = "[\\P{L}.\\S]{1,254}\\.[a-zA-Z1-9]{2,5}";

    @Autowired
    public FileService(MessageSource messageSource) {
        this.messageSource = messageSource;
        setPictogramNames();
        setPictogramsNamesPaths();
        allowedImagesExtensions = new ArrayList<>(5);
        allowedImagesExtensions.addAll(Arrays.asList(
                ".jpg", ".jpeg", ".png", ".bmp", ".tif", ".tiff", ".gif", ".raw", ".psd", ".xcf", "cdr"));
        allowedZipExtensions = new ArrayList<>(2);
        allowedZipExtensions.addAll(Arrays.asList("zip", "kmz", "jar", "gz"));
        allowedXmlExtensions = new ArrayList<>(5);
        allowedXmlExtensions.addAll(Arrays.asList("kmz", "kml", "xml", "txt"));
    }


    /**
     * Asynchronous method.
     * 1) Waits for 2 seconds to all the log lines to be flushed on disk and try to read the temp log file
     * 1.1) If failed (output stream for the file from other thread may be not closed) waits again for 1.5s.
     * If the reading is completely failed (output stream may be not closed) returns the message of the last exception.
     *
     * @param locale To localize a message to the end User.
     * @return The full log messages from the temp log file or the failure message if that file couldn't be read.
     */
    public String getStackTraceFromLogFile(Locale locale) {
        Path logFilePath = Paths.get(pathToLogFile);
        this.stackTrace = "";

        Callable<String> callLogFile = () -> {
            try {
                stackTrace = String.join("<br>\n", Files.readAllLines(logFilePath, StandardCharsets.UTF_8));
            } catch (IOException e) {
                Thread.sleep(1500);
                stackTrace = Files.readString(logFilePath);
            }
            return stackTrace;
        };

        FutureTask<String> log = new FutureTask<>(callLogFile);
        new Thread(log).start();

        try {
            return log.get(2, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {

            stackTrace = messageSource.getMessage(
                    "exceptions.logFileReadFailure(1)", new Object[]{e.getMessage()}, locale);
        }
        return stackTrace;
    }

    public void deleteLogFile() {
        Path logFilePath = Paths.get(pathToLogFile);
        try {
            Files.deleteIfExists(logFilePath);
            log.debug("Log file={} has been deleted.", pathToLogFile);
        } catch (IOException e) {
            log.debug("Deleting the temp log file failed and has caused an exception:\n", e);
        }
    }

    private void setPictogramNames() {
        try {
            URI pictogramsUri = Objects.requireNonNull(this.getClass().getClassLoader().getResource("static/pictograms"))
                    .toURI();
            Path pictogramsPath;
            if (pictogramsUri.getScheme().equals("jar")) {
                log.info("Searching for pictograms inside the jar...");
                FileSystem fileSystem = FileSystems.newFileSystem(pictogramsUri, Collections.<String, Object>emptyMap());
                pictogramsPath = fileSystem.getPath("../BOOT-INF/classes/static/pictograms/");
            } else {
                log.info("Searching for pictograms inside the target classes...");
                pictogramsPath = Paths.get(pictogramsUri);
            }
            log.info("Path to pictograms: " + pictogramsPath.toString());
            Files.walk(pictogramsPath, 1).forEach(pictogramPath -> {
                String pictogram = pictogramPath.getFileName().toString();
                if (getExtension(pictogram).toLowerCase().endsWith("png")) {
                    pictogramsNames.add(pictogram);
                }
            });
            log.info("{} Pictograms names have been collected.", pictogramsNames.size());
        } catch (URISyntaxException | IOException exception) {
            log.error(exception.getMessage(), exception);
        }
        //This will list all the files inside the jar
/*
        try {
            CodeSource src = this.getClass().getProtectionDomain().getCodeSource();
            if (src != null) {
                URL jar = src.getLocation();
                ZipInputStream zip = new ZipInputStream(jar.openStream());
                while (true) {
                    ZipEntry e = zip.getNextEntry();
                    if (e == null)
                        break;
                    String name = e.getName();
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }

*/
    }


    private void setPictogramsNamesPaths() {
        pictogramsNames.forEach(pictogram -> pictogramsNamesPaths.put(pictogram, PICTOGRAMS_PATH + pictogram));
        log.info("{} Pictograms names with full paths have been collected.", pictogramsNamesPaths.size());
    }

    /**
     * Extract the exact filename from a given path or http link as img[src] or a[href].
     * E.g. 'files:/image.png' or 'C:\images\image.jpg' will be returned as 'image.png', and 'image.jpg' will be returned as it.
     * Regexp: "[\P{L}.\S]{1,254}\.[a-zA-Z1-9]{2,5}"
     *
     * @param pathWithFilename Href or src to the image. E.g. "file:///D:/MyFolder/MyPOI/picture.jpg" or "files/picture.png"
     * @return The name of the file from the given src (e.g. "picture.jpg") or empty string if nothing found
     * or the given path is not a valid path.
     */
    public String getFileName(String pathWithFilename) {
        if (pathWithFilename == null) {
            throw new IllegalArgumentException(messageSource.getMessage("exception.nullFilename", null, Locale.ENGLISH));
        }
        if (!pathWithFilename.matches(IMG_PATH_WITH_FILENAME_REGEX)) {
            return "";
        }
        //If index of '/' or '\' return -1 the 'pathWithFilename' consist of only the filename without a path
        int lastIndexOFSlash = pathWithFilename.lastIndexOf("/") != -1 ?
                pathWithFilename.lastIndexOf("/") :
                pathWithFilename.lastIndexOf("\\");
        String filename = pathWithFilename.substring(lastIndexOFSlash + 1);
        log.trace("Filename as '{}' will be returned", filename);
        return filename.isBlank() ? "" : filename;
    }

    /**
     * Extract the exact path from a given path with a filename or http link as img[src] or a[href].
     * E.g. 'files:/image.png' or 'C:\images\image.jpg' will be returned as 'files:/' or 'C:\images\'.
     * Regexp: "[\P{L}.\S]{1,254}\.[a-zA-Z1-9]{2,5}"
     *
     * @param pathWithFilename Href or src to the image ('files:/image.png' or 'C:\images\image.jpg' etc)
     * @return The only path without the filename ('files:/' or 'C:\images\' etc) or an empty String if no path found.
     */
    public String getPath(String pathWithFilename) {
        if (!pathWithFilename.matches(IMG_PATH_WITH_FILENAME_REGEX)) {
            return "";
        }
        //If index of '/' or '\' return -1 the 'pathWithFilename' consist of only the filename without a path
        int lastIndexOFSlash = pathWithFilename.lastIndexOf("/") != -1 ? pathWithFilename.lastIndexOf("/") :
                pathWithFilename.lastIndexOf("\\") != -1 ? pathWithFilename.lastIndexOf("\\") : 0;
        if (lastIndexOFSlash == 0) {
            log.trace("A path has not been found. An empty String will be returned.");
            return "";
        }
        String path = pathWithFilename.substring(0, lastIndexOFSlash + 1);
        log.trace("Path as '{}' will be returned", path);
        return path.isBlank() ? "" : path;
    }

    /**
     * @param filename The file extension to be derived from.
     * @return The filename extension in lowercase without a dot (e.g. "image.img" will be returned as "img")
     * OR an empty String if no extension found.
     */
    public String getExtension(@NonNull String filename) {
        if (!filename.contains(".")) {
            //The filename doesn't contain an extension
            log.debug("The given filename = {} doesn't contain the extension!", filename);
            return "";
        }
        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        if (extension.isBlank()) {
            return "";
        } else {
            return extension;
        }
    }
}
