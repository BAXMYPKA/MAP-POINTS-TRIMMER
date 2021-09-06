package mrbaxmypka.gmail.com.mapPointsTrimmer.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Locale;

@Controller
public class ErrorsController extends AbstractController {

    @GetMapping(path = "/error")
    public String getErrorPage(Model model, Locale locale) {
        return "error";
    }
}
