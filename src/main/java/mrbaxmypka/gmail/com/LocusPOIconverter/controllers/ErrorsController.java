package mrbaxmypka.gmail.com.LocusPOIconverter.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.Locale;

@Controller
public class ErrorsController {
	
	@GetMapping(path = "/error")
	public ModelAndView getErrorPage(Locale locale) {
		return new ModelAndView("error");
	}
}
