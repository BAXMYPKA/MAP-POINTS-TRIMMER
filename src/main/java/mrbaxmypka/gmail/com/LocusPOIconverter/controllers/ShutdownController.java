package mrbaxmypka.gmail.com.LocusPOIconverter.controllers;

import mrbaxmypka.gmail.com.LocusPOIconverter.services.KmlKmzService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.context.WebApplicationContext;

import java.nio.file.Files;
import java.util.Locale;

@Controller
public class ShutdownController {
	
	@Autowired
	private WebApplicationContext applicationContext;
	
	@Autowired
	private MessageSource messageSource;
	
	@Autowired
	private KmlKmzService kmlKmzService;
	
	/**
	 * Attribute for index.html to apply this css className to make the 'Shutdown' button grey.
	 */
	private final String SHUTDOWN_BTN_CLASS = "shutdownButtonOff";
	
	@GetMapping(path = "/shutdown")
	public String shutdownApp(Model model, Locale locale) throws Exception {
		try {
			Files.deleteIfExists(kmlKmzService.getTempKmlFile());
		} finally {
			Thread thread = new Thread(() -> {
				try {
					Thread.sleep(1500);
				} catch (InterruptedException e) {
					System.out.println(e.getMessage());
				}
				System.out.println("SHUTTING DOWN...");
				SpringApplication.exit(applicationContext, () -> 2);
			});
			thread.start();
			
			String shutdownMessage = messageSource.getMessage("userMessage.shutdownSuccess", null, locale);
			model.addAttribute("userMessage", shutdownMessage);
			model.addAttribute("shutdownBtnClass", SHUTDOWN_BTN_CLASS);
			return "redirect:/";
		}
	}
}
