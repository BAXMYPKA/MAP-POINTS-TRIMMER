package mrbaxmypka.gmail.com.mapPointsTrimmer.controllers;

import lombok.extern.slf4j.Slf4j;
import mrbaxmypka.gmail.com.mapPointsTrimmer.entitiesDto.MultipartDto;
import mrbaxmypka.gmail.com.mapPointsTrimmer.services.AdminCredentialsService;
import mrbaxmypka.gmail.com.mapPointsTrimmer.services.MultipartFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.context.MessageSource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.xml.sax.SAXException;

import javax.validation.Valid;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Locale;

@Slf4j
@RestController
public class AdminController extends AbstractController {

	@Autowired
	private AdminCredentialsService credentialsService;

	@PostMapping(path = "/admin", consumes = MediaType.APPLICATION_JSON_VALUE)
	public HttpStatus verityAdminCredentials(@RequestBody JSONObject adminCredentials) {

		log.info( "Admin credentials have been received as {}.", adminCredentials);

		System.out.println("ADMIN CREDENTIALS ARE: ");

		if (credentialsService.verifyAdminCredentials(adminCredentials)) {
			return HttpStatus.OK;
		} else {
			return HttpStatus.UNAUTHORIZED;
		}
	}
}
