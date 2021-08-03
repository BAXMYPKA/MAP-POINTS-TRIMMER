package mrbaxmypka.gmail.com.mapPointsTrimmer.controllers;

import lombok.extern.slf4j.Slf4j;
import mrbaxmypka.gmail.com.mapPointsTrimmer.MapPointsTrimmerApplication;
import mrbaxmypka.gmail.com.mapPointsTrimmer.services.FileService;
import mrbaxmypka.gmail.com.mapPointsTrimmer.services.MultipartFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.Mapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Locale;

@Slf4j
@Controller
public class ShutdownController extends AbstractController{
	
	/**
	 * Attribute for index.html to apply this css className to make the 'Shutdown' button grey.
	 */
	private final String SHUTDOWN_BTN_CLASS = "rightHeaderGroup__shutdownButtonOn_img_shutDown";
	@Autowired
	private WebApplicationContext applicationContext;
	@Autowired
	private MessageSource messageSource;
	@Autowired
	private MultipartFileService multipartFileService;
	@Autowired
	private FileService fileService;
	@Autowired
	private MapPointsTrimmerApplication mapPointsTrimmerApplication;

	/**
	 * {@link RequestMethod#GET} receives a request from the Shutdown button.
	 * {@link RequestMethod#POST} receives a Beacon POST request in 'beforeunload' from a browser.
	 * @param redirectAttributes
	 * @param locale
	 */
	@RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, path = "/shutdown")
//	@GetMapping(path = "/shutdown")
	public String shutdownApp(RedirectAttributes redirectAttributes, Locale locale) {
/*
		//To defer the shitting down a bit to be able to return the main page
		Thread thread = new Thread(() -> {
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				System.out.println(e.getMessage());
			} finally {
				multipartFileService.deleteTempFile();
				fileService.deleteLogFile();
				SpringApplication.exit(applicationContext, () -> 666);
			}
		});
		thread.start();
*/
		mapPointsTrimmerApplication.shutDownApp();

		String shutdownMessage = messageSource.getMessage("userMessage.shutdownSuccess", null, locale);
		redirectAttributes.addFlashAttribute("userMessage", shutdownMessage);
		redirectAttributes.addFlashAttribute("shutdownBtnClass", SHUTDOWN_BTN_CLASS);
		log.info("While the Application is shutting down it's redirecting request to the main page...");
		return "redirect:/";
	}
}
