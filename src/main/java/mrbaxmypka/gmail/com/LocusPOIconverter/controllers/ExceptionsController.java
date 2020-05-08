package mrbaxmypka.gmail.com.LocusPOIconverter.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
import org.xml.sax.SAXException;

import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class ExceptionsController {
	
	@Autowired
	private MessageSource messageSource;
	
	//TODO: to make logging
	
	/**
	 * @param npe {@link NullPointerException} from Service level
	 * @return HttpStatus 428
	 */
	@ExceptionHandler(NullPointerException.class)
	public ModelAndView nullPinterHandler(NullPointerException npe) {
		//code 428
		return returnIndexPageWithError(HttpStatus.PRECONDITION_REQUIRED, npe.getMessage());
//		return ResponseEntity.status(HttpStatus.PRECONDITION_REQUIRED).body(npe.getMessage());
	}
	
	/**
	 * @param iae {@link IllegalArgumentException} from Service level
	 * @return {@link HttpStatus#NOT_ACCEPTABLE} 406
	 */
	@ExceptionHandler(IllegalArgumentException.class)
	public ModelAndView illegalArgException(IllegalArgumentException iae) {
		//code 406
		return returnIndexPageWithError(HttpStatus.NOT_ACCEPTABLE, iae.getMessage());
//		return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(iae.getMessage());
	}
	
	/**
	 * @param sae {@link SAXException} from Service level or below
	 * @return {@link HttpStatus#UNPROCESSABLE_ENTITY} 422
	 */
	@ExceptionHandler(SAXException.class)
	public ModelAndView xmlParsingException(SAXException sae) {
		//code 422
		return returnIndexPageWithError(HttpStatus.UNPROCESSABLE_ENTITY, sae.getMessage());
//		return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(sae.getMessage());
	}
	
	/**
	 * @param ie Occurs only in case of {@link ShutdownController#shutdownApp(Model, Locale)} cant shut the
	 *              application down.
	 * @return {@link HttpStatus#INTERNAL_SERVER_ERROR} with the information how a User can shut the application down
	 * manually.
	 */
	@ExceptionHandler(InterruptedException.class)
	public ModelAndView interruptedException(InterruptedException ie, Locale locale) {
		String shutdownFailureMessage = messageSource.getMessage("exception.shutdownFaulire", null, locale);
		return returnIndexPageWithError(HttpStatus.INTERNAL_SERVER_ERROR, shutdownFailureMessage);
//		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(shutdownFailureMessage);
	}
	
	/**
	 * @param ve Spring Boot validation exception from Controller arguments
	 * @param locale To determine the lang of a User
	 * @return {@link HttpStatus#BAD_REQUEST} 400
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ModelAndView validationException(MethodArgumentNotValidException ve, Locale locale) {
		Map<String, String> errors = ve.getBindingResult().getAllErrors().stream()
			.collect(Collectors.toMap(objectError ->
					objectError.getObjectName(),
					objectError -> objectError.getDefaultMessage()));
		String errorMessages = errors.entrySet().stream()
			.map(e ->
				messageSource.getMessage("exception.fieldError(2)", new Object[]{e.getKey(), e.getValue()}, locale))
			.collect(Collectors.joining());
		return returnIndexPageWithError(HttpStatus.BAD_REQUEST, errorMessages);
//		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessages);
	}
	
	/**
	 * @param exception Any specific internal processing exceptions {@link Exception} from Service level
	 * @return HttpStatus 500
	 */
	@ExceptionHandler(value = Exception.class)
	public ModelAndView internalExceptions(Exception exception) {
		//code 500
		//TODO: to log a real message and substitute it for a localized one
		return returnIndexPageWithError(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
//		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exception.getMessage());
	}
	
	ModelAndView returnIndexPageWithError(HttpStatus httpStatus, String localizedErrorMessage) {
		ModelAndView mav = new ModelAndView("forward:/index", httpStatus);
		mav.addObject("userMessage", localizedErrorMessage);
		return mav;
	
	}
}
