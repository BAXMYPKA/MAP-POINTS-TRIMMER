package mrbaxmypka.gmail.com.LocusPOIconverter.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Locale;

@Controller
public class IndexController {
	
	@Autowired
	private MessageSource messageSource;
	
	@GetMapping(path = {"/", "/converter"})
	public String getIndex(Model model, Locale locale) {
		System.out.println("get index");
		String downloadMessage = messageSource.getMessage("userMessage.downloadMessageAwait", null, locale);
		model.addAttribute("downloadMessage", downloadMessage);
		return "index";
	}
	
}
