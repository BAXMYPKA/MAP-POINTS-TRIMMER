package mrbaxmypka.gmail.com.mapPointsTrimmer;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MapPointsTrimmerApplication {

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

}
