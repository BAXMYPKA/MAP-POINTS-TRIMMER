package mrbaxmypka.gmail.com.mapPointsTrimmer.controllers;

import mrbaxmypka.gmail.com.mapPointsTrimmer.services.MultipartFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.file.Files;
import java.util.Locale;

@Controller
public class ShutdownController {
	
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
	
	@GetMapping(path = "/shutdown")
	public String shutdownApp(RedirectAttributes redirectAttributes, Locale locale) {
		try {
			Files.deleteIfExists(multipartFileService.getTempFile());
		} finally {
			Thread thread = new Thread(() -> {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					System.out.println(e.getMessage());
				}
				SpringApplication.exit(applicationContext, () -> 666);
			});
			thread.start();
			
			String shutdownMessage = messageSource.getMessage("userMessage.shutdownSuccess", null, locale);
			redirectAttributes.addFlashAttribute("userMessage", shutdownMessage);
			redirectAttributes.addFlashAttribute("shutdownBtnClass", SHUTDOWN_BTN_CLASS);
			return "redirect:/";
		}
	}
}
