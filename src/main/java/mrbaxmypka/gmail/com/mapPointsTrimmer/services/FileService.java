package mrbaxmypka.gmail.com.mapPointsTrimmer.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

/**
 * @author BAXMYPKA
 */
@Slf4j
@Component
public class FileService {
	
	@Autowired
	private MessageSource messageSource;
	@Value("${logging.file.name}")
	private String logFile;
	
	//TODO: to rewrite the following
	
	/**
	 * 1) Waits for 1 second to all the log lines to be flushed on disk and try to read the temp log file
	 * 1.1)If failed (output stream may be not closed) waits more for 1.5s.
	 *
	 * @param locale To localize a message to the end User. Default is {@link Locale#ENGLISH}
	 * @return The full log messages from the temp log file or the failure message if that file couldn't be read.
	 */
	public String getStackTraceFromLogFile(@Nullable Locale locale) {
		locale = locale == null ? Locale.ENGLISH : locale;
		
		Path logFilePath = Paths.get(logFile);
		String stackTrace = "";
		
		try {
			Thread.sleep(1000); //To let all the lines to be written (flushed) to the log file
			stackTrace = Files.readString(logFilePath);
		} catch (InterruptedException interruptedException) {
			log.debug(interruptedException.getMessage(), interruptedException);
			stackTrace = interruptedException.getMessage();
		} catch (IOException e) {
			try {
				Thread.sleep(1500);
			} catch (InterruptedException interruptedException) {
				interruptedException.printStackTrace();
			}
			try {
				stackTrace = Files.readString(logFilePath);
			} catch (IOException ioException) {
				messageSource.getMessage(
					"exceptions.logFileReadFailure(1)",
					new Object[]{ioException.getMessage()}, locale);
			}
		} finally {
			return stackTrace;
		}
	}
}
