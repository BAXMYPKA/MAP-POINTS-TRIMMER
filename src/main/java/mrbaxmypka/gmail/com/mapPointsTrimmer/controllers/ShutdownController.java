package mrbaxmypka.gmail.com.mapPointsTrimmer.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Locale;

@Slf4j
@Controller
public class ShutdownController extends AbstractController{
	
	/**
	 * Attribute for index.html to apply this css className to make the 'Shutdown' button grey.
	 */
	private final String SHUTDOWN_BTN_CLASS = "rightHeaderGroup__shutdownButtonOn_img_shutDown";

	/**
	 * {@link RequestMethod#GET} receives a request from the Shutdown button.
	 * {@link RequestMethod#POST} receives a Beacon POST request in 'navigator.sendBeacon()' browser's method.
	 * @param redirectAttributes To include a shutdown message for a User and the new gray style for the shutdown button.
	 * @param locale To localize a message for a User.
	 */
	@RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, path = "/shutdown")
	public String shutdownApp(RedirectAttributes redirectAttributes, Locale locale) {
		getWebSessionService().shutdownApplication();
		String shutdownMessage = getMessageSource().getMessage("userMessage.shutdownSuccess", null, locale);
		redirectAttributes.addFlashAttribute("userMessage", shutdownMessage);
		redirectAttributes.addFlashAttribute("shutdownBtnClass", SHUTDOWN_BTN_CLASS);
		redirectAttributes.addFlashAttribute("isShutDown", true);
		log.warn("While the Application is shutting down it's redirecting request to the main page...");
		return "redirect:/";
	}
}
