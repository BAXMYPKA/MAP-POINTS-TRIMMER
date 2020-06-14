package mrbaxmypka.gmail.com.mapPointsTrimmer;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
class MapPointsTrimmerApplicationTests {
	
	@Value("${logging.file.name}")
	private String logFile;
	
	@Order(1)
	@Test
	void contextLoads() {
	}
	
	@Order(2)
	@Test
	void set_Logging_Context_Should_Return_An_Appropriate_One() {
		//GIVEN
		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
		Logger logger = context.getLogger("mrbaxmypka.gmail.com.mapPointsTrimmer.MapPointsTrimmerApplication");
		
		//WHEN
		logger.setLevel(Level.valueOf("TRACE"));

		//THEN
		assertEquals("TRACE", logger.getEffectiveLevel().toString());
	}
	
	@Order(3)
	@Test
	void log_File_Should_Be_Readable() {
		//GIVEN
		Path logFilePath = Paths.get(logFile);
		
		//WHEN
		
		//THEN
		assertTrue(Files.exists(logFilePath));
		assertTrue(Files.isReadable(logFilePath));
		assertTrue(Files.isRegularFile(logFilePath));
		
		assertFalse(Files.isDirectory(logFilePath));
	}
}
