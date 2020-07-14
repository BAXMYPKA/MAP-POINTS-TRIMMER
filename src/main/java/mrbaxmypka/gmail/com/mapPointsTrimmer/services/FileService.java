package mrbaxmypka.gmail.com.mapPointsTrimmer.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.concurrent.*;

/**
 * @author BAXMYPKA
 */
@Slf4j
@Component
public class FileService {
	
	@Autowired
	private MessageSource messageSource;
	@Value("${logging.file.name}")
	private String pathToLogFile;
	private String stackTrace;
	
	
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
				"exceptions.logFileReadFailure(1)",	new Object[]{e.getMessage()}, locale);
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
	
	/**
	 * Extract the exact filename from a given path or http link as img[src] or a[href].
	 * E.g. 'files:/image.png' or 'C:\images\image.jpg' will be returned as 'image.png', and 'image.jpg' will be returned as it.
	 *
	 * @param pathWithFilename Href or src to the image
	 * @return The name of the file from the given src or empty string if nothing found.
	 */
	public String getFileName(String pathWithFilename) {
/*
		if (!oldHrefWithFilename.contains(".") ||
			(!oldHrefWithFilename.contains("/") && !oldHrefWithFilename.contains("\\"))) return "";
*/
		if (!pathWithFilename.matches("[.\\S]{1,100}\\.[a-zA-Z1-9]{3,5}")) return "";
		//If index of '/' or '\' return -1 the 'oldHrefWithFilename' consist of only the filename without href
		int lastIndexOFSlash = pathWithFilename.lastIndexOf("/") != -1 ?
				pathWithFilename.lastIndexOf("/") :
				pathWithFilename.lastIndexOf("\\");
		String filename = pathWithFilename.substring(lastIndexOFSlash + 1);
		log.trace("Filename as '{}' will be returned", filename);
		return filename.isBlank() ? "" : filename;
	}
	
}
