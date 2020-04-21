package mrbaxmypka.gmail.com.LocusPOIconverter.controllers.rest;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@RestController
public class KmlController {
	
	@PostMapping(path = "/kml", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<String> postKml(@RequestParam(name = "klm") MultipartFile kml) {
		
		if (kml.isEmpty()) {
			return ResponseEntity.unprocessableEntity().body("WRONG");
		}
		
		if (kml.getOriginalFilename().endsWith(".kml")) {
			System.out.println("ITS A KML");
		} else if (kml.getOriginalFilename().endsWith(".kmz")) {
			System.out.println("ITS A KMZ");
		}
		
		try {
			byte[] part = kml.getBytes();
			InputStream inputStream = kml.getInputStream();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
}
