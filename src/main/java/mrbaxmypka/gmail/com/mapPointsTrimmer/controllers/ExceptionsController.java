package mrbaxmypka.gmail.com.mapPointsTrimmer.controllers;

import mrbaxmypka.gmail.com.mapPointsTrimmer.services.MultipartFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class ExceptionsController {
	
	@Autowired
	private MessageSource messageSource;
	@Autowired
	private MultipartFileService multipartFileService;
	
	//TODO: to make logging
	
	/**
	 * @param npe {@link NullPointerException} from Service level
	 * @return HttpStatus 428
	 */
	@ExceptionHandler(NullPointerException.class)
	public ModelAndView nullPinterHandler(NullPointerException npe) {
		//code 428
		return returnRedirectIndexPageWithError(HttpStatus.PRECONDITION_REQUIRED, npe.getMessage());
//		return ResponseEntity.status(HttpStatus.PRECONDITION_REQUIRED).body(npe.getMessage());
	}
	
	/**
	 * @param iae {@link IllegalArgumentException} from Service level
	 * @return {@link HttpStatus#NOT_ACCEPTABLE} 406
	 */
	@ExceptionHandler(IllegalArgumentException.class)
	public ModelAndView illegalArgException(IllegalArgumentException iae) {
		//code 406
		return returnRedirectIndexPageWithError(HttpStatus.NOT_ACCEPTABLE, iae.getMessage());
//		return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(iae.getMessage());
	}
	
	/**
	 * @param xmlError {@link SAXException} from Service level or below
	 * @return {@link HttpStatus#UNPROCESSABLE_ENTITY} 422
	 */
	@ExceptionHandler(value = {SAXException.class, ParserConfigurationException.class, TransformerException.class})
	public ModelAndView xmlParsingException(Exception xmlError, Locale locale) {
		//code 422
		String xmlErrorPrefix = messageSource.getMessage("exception.xmlParseError", null, locale);
		return returnRedirectIndexPageWithError(HttpStatus.UNPROCESSABLE_ENTITY, xmlErrorPrefix + xmlError.getMessage());
//		return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(sae.getMessage());
	}
	
	/**
	 * @param ie Occurs only in case of {@link ShutdownController#shutdownApp(RedirectAttributes, Locale)} cant shut the
	 *           application down.
	 * @return {@link HttpStatus#INTERNAL_SERVER_ERROR} with the information how a User can shut the application down
	 * manually.
	 */
	@ExceptionHandler(InterruptedException.class)
	public ModelAndView interruptedException(InterruptedException ie, Locale locale) {
		String shutdownFailureMessage = messageSource.getMessage("exception.shutdownFailure", null, locale);
		return returnRedirectIndexPageWithError(HttpStatus.INTERNAL_SERVER_ERROR, shutdownFailureMessage);
//		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(shutdownFailureMessage);
	}
	
	@ExceptionHandler(IOException.class)
	public ModelAndView ioException(IOException io, Locale locale) {
		String fileSavingFailure = messageSource.getMessage(
			"exception.fileException(1)", new Object[]{io.getMessage()}, locale);
		return returnRedirectIndexPageWithError(HttpStatus.INTERNAL_SERVER_ERROR, fileSavingFailure);
//		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(shutdownFailureMessage);
	}
	
	/**
	 * @param ve     Spring Boot validation exception from Controller arguments
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
		return returnRedirectIndexPageWithError(HttpStatus.BAD_REQUEST, errorMessages);
//		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessages);
	}
	
	/**
	 * @param exception Any specific internal processing exceptions {@link Exception} from Service level
	 * @return HttpStatus 500
	 */
	@ExceptionHandler(value = Exception.class)
	public ModelAndView internalExceptions(Exception exception, Model model) {
		//code 500
		return returnRedirectIndexPageWithError(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
//		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exception.getMessage());
	}
	
	/**
	 * 1) Deletes the temporary file if it is exists in the temp directory
	 * 2) Returns {@link ModelAndView} with redirecting to "/trimmer" page with the localized message for User
	 * and the given {@link HttpStatus}.
	 *
	 * @param httpStatus            To be returned to a User
	 * @param localizedErrorMessage To be returned to a User
	 */
	ModelAndView returnRedirectIndexPageWithError(HttpStatus httpStatus, String localizedErrorMessage) {
		multipartFileService.deleteTempFile();
		ModelAndView mav = new ModelAndView("redirect:/trimmer", httpStatus);
		mav.addObject("userMessage", localizedErrorMessage);
		return mav;
	}
	
	ModelAndView returnForwardErrorPageWithDebug(HttpStatus httpStatus) {
		multipartFileService.deleteTempFile();
		ModelAndView mav = new ModelAndView("forward:/error", httpStatus);
		return mav;
	}
}
