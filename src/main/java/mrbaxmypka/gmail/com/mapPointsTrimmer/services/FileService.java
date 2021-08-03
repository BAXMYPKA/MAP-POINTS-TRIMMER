package mrbaxmypka.gmail.com.mapPointsTrimmer.services;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * @author BAXMYPKA
 */
@Slf4j
@Component
public class FileService {
	
	@Autowired
	private MessageSource messageSource;
	
	final Resource pictogramsResource;

	/**
	 * All possible images files extensions in lower case.
	 */
	@Getter(AccessLevel.PUBLIC)
	private List<String> imagesExtensions;

	/**
	 * Collects a list of pictograms names ONLY as '.png' OR '.PNG' files from the 'resources/static/pictograms' directory.
	 * <p>
	 * {@literal ArrayList<String> pictogramNames} or an empty Array if nothing found.
	 */
	@Getter(AccessLevel.PUBLIC)
	private ArrayList<String> pictogramsNames;
    
    /**
	 * MUST be set AFTER {@link #setPictogramNames()}
     * Collects a Map collection of only pictograms names (.png files) and they relative paths
     * from the 'resources/static/pictograms' directory.
     *
     * {@link HashMap} pictogramNames where
     * key = pictogram name
     * value = full pictogram path
     * e.g.: key=pictureName.png value=pictograms/pictureName.png
     * or an empty Map if nothing found.
     */
    @Getter(AccessLevel.PUBLIC)
    private Map<String, String> pictogramsNamesPaths;
	
	@Value("${logging.file.name}")
	private String pathToLogFile;
	
	private final String PICTOGRAMS_PATH = "pictograms/";
	
	private String stackTrace;
	
	@Autowired
	public FileService(MessageSource messageSource, ResourceLoader resourceLoader) {
		this.messageSource = messageSource;
		pictogramsResource = resourceLoader.getResource("classpath:static/pictograms");
		setPictogramNames();
		setPictogramsNamesPaths();
		imagesExtensions = new ArrayList<>(5);
		imagesExtensions.addAll(Arrays.asList(
				".jpg", ".jpeg", ".png", ".bmp", ".tif", ".tiff", ".gif", ".raw", ".psd", ".xcf", "cdr"));
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
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(pictogramsResource.getInputStream()))) {
			pictogramsNames = reader.lines().collect(Collectors.toCollection(ArrayList::new));
			pictogramsNames.removeIf(s -> !s.toLowerCase().endsWith(".png")); //Delete all non-.png files
			log.info("{} Pictograms names have been collected.", pictogramsNames.size());
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			pictogramsNames = new ArrayList<>(0);
		}
		
	}
	
	private void setPictogramsNamesPaths() {
		pictogramsNamesPaths = new HashMap<>(pictogramsNames.size());
		pictogramsNames.forEach(pictogram -> pictogramsNamesPaths.put(pictogram, PICTOGRAMS_PATH + pictogram));
		log.info("{} Pictograms names with full paths have been collected.", pictogramsNamesPaths.size());
	}
	
	/**
	 * Extract the exact filename from a given path or http link as img[src] or a[href].
	 * E.g. 'files:/image.png' or 'C:\images\image.jpg' will be returned as 'image.png', and 'image.jpg' will be returned as it.
	 *
	 * @param pathWithFilename Href or src to the image. E.g. "file:///D:/MyFolder/MyPOI/picture.jpg" or "files/picture.png"
	 * @return The name of the file from the given src (e.g. "picture.jpg") or empty string if nothing found
	 * or the given path is not a valid path.
	 */
	public String getFileName(String pathWithFilename) {
		if (!pathWithFilename.matches("[.\\S]{1,100}\\.[a-zA-Z1-9]{3,5}")) return "";
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
	 *
	 * @param pathWithFilename Href or src to the image ('files:/image.png' or 'C:\images\image.jpg' etc)
	 * @return The only path without the filename ('files:/' or 'C:\images\' etc) or an empty String if no path found.
	 */
	public String getPath(String pathWithFilename) {
		if (!pathWithFilename.matches("[.\\S]{1,100}\\.[a-zA-Z1-9]{3,5}")) {
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
}
