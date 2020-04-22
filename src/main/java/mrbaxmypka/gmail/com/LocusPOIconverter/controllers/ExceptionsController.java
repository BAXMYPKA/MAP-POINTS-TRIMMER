package mrbaxmypka.gmail.com.LocusPOIconverter.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

@ControllerAdvice
public class ExceptionsController {
	
	//TODO: to make logging
	
	/**
	 * @param npe {@link NullPointerException} from Service level
	 * @return HttpStatus 428
	 */
	@ExceptionHandler(NullPointerException.class)
	public ResponseEntity<String> nullPinterHandler(NullPointerException npe) {
		//code 428
		return ResponseEntity.status(HttpStatus.PRECONDITION_REQUIRED).body(npe.getMessage());
	}
	
	/**
	 * @param iae {@link IllegalArgumentException} from Service level
	 * @return {@link HttpStatus#NOT_ACCEPTABLE} 406
	 */
	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<String> illegalArgException(IllegalArgumentException iae) {
		//code 406
		return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(iae.getMessage());
	}
	
	/**
	 * @param sae {@link SAXException} from Service level or below
	 * @return {@link HttpStatus#UNPROCESSABLE_ENTITY} 422
	 */
	@ExceptionHandler(SAXException.class)
	public ResponseEntity<String> xmlParsingException(SAXException sae) {
		//code 422
		return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(sae.getMessage());
	}
	
	/**
	 * @param exception Any specific internal processing exceptions {@link Exception} from Service level
	 * @return HttpStatus 500
	 */
	@ExceptionHandler(value = Exception.class)
	public ResponseEntity<String> internalExceptions(Exception exception) {
		//code 500
		//TODO: to log a real message and substitute it for a localized one
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exception.getMessage());
	}
	
}
