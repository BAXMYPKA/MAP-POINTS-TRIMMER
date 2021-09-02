package mrbaxmypka.gmail.com.mapPointsTrimmer;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import lombok.extern.slf4j.Slf4j;
import mrbaxmypka.gmail.com.mapPointsTrimmer.services.FileService;
import mrbaxmypka.gmail.com.mapPointsTrimmer.services.MultipartFileService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.context.WebApplicationContext;

@SpringBootApplication
@Slf4j
public class MapPointsTrimmerApplication {

	@Autowired
	private MultipartFileService multipartFileService;
	@Autowired
	private FileService fileService;
	@Autowired
	private WebApplicationContext applicationContext;

	public static void main(String[] args) {
		SpringApplication.run(MapPointsTrimmerApplication.class, args);
	}
	
	/**
	 * @return If {@link Logger#getEffectiveLevel()} for MapPointsTrimmerApplication is "INFO"
	 * it means that 'debugMode' is true. Any other levels will return false.
	 */
	public static boolean debugModeIsOn() {
		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
		Logger logger = context.getLogger("mrbaxmypka.gmail.com.mapPointsTrimmer.MapPointsTrimmerApplication");
		if (logger.getEffectiveLevel().equals(Level.INFO)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Waits for 3sec then shutting down the full application.
	 */
	public void shutDownApp() {
		//To defer the shitting down a bit to be able to return the main page
		Thread thread = new Thread(() -> {
			try {
				Thread.sleep(4000);
			} catch (InterruptedException e) {
				log.error(e.getMessage(), e);
			} finally {
				multipartFileService.deleteTempFiles();
				fileService.deleteLogFile();
				SpringApplication.exit(applicationContext, () -> 666);
			}
		});
		thread.start();
	}
}
