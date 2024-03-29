package mrbaxmypka.gmail.com.mapPointsTrimmer.controllers;

import lombok.extern.slf4j.Slf4j;
import mrbaxmypka.gmail.com.mapPointsTrimmer.MapPointsTrimmerApplication;
import mrbaxmypka.gmail.com.mapPointsTrimmer.services.FileService;
import mrbaxmypka.gmail.com.mapPointsTrimmer.services.MultipartMainFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.xml.sax.SAXException;

import javax.servlet.http.HttpSession;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
public class ExceptionsController extends AbstractController {

    @Autowired
    private MessageSource messageSource;
    @Autowired
    private MultipartMainFileService multipartMainFileService;
    @Autowired
    private FileService fileService;

    /**
     * @param npe {@link NullPointerException} from Service level
     * @return HttpStatus 428
     */
    @ExceptionHandler({NullPointerException.class})
    public ModelAndView nullPinterHandler(NullPointerException npe, Locale locale, HttpSession httpSession) {
        //code 428
        return returnPageWithError(HttpStatus.PRECONDITION_REQUIRED, npe.getMessage(), npe, locale, httpSession);
    }

    /**
     * @param iae {@link IllegalArgumentException} from Service level
     * @return {@link HttpStatus#NOT_ACCEPTABLE} 406
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ModelAndView illegalArgException(IllegalArgumentException iae, Locale locale, HttpSession httpSession) {
        //code 406
        return returnPageWithError(HttpStatus.NOT_ACCEPTABLE, iae.getMessage(), iae, locale, httpSession);
    }

    /**
     * @param xmlError {@link SAXException} from Service level or below
     * @return {@link HttpStatus#UNPROCESSABLE_ENTITY} 422
     */
    @ExceptionHandler(value = {SAXException.class, ParserConfigurationException.class, TransformerException.class})
    public ModelAndView xmlParsingException(Exception xmlError, Locale locale, HttpSession httpSession) {
        //code 422
        String xmlErrorPrefix = messageSource.getMessage("exception.xmlParseError", null, locale);
        return returnPageWithError(
                HttpStatus.UNPROCESSABLE_ENTITY, xmlErrorPrefix + xmlError.getMessage(), xmlError, locale, httpSession);
    }

    /**
     * @param ie Occurs only in case of {@link ShutdownController#shutdownApp(RedirectAttributes, Locale)} cant shut the
     *           application down.
     * @return {@link HttpStatus#INTERNAL_SERVER_ERROR} with the information how a User can shut the application down
     * manually.
     */
    @ExceptionHandler(InterruptedException.class)
    public ModelAndView interruptedException(InterruptedException ie, Locale locale, HttpSession httpSession) {
        String shutdownFailureMessage = messageSource.getMessage("exception.interruptedException", null, locale);
        return returnPageWithError(HttpStatus.INTERNAL_SERVER_ERROR, shutdownFailureMessage, ie, locale, httpSession);
    }

    @ExceptionHandler(IOException.class)
    public ModelAndView ioException(IOException io, Locale locale, HttpSession httpSession) {
        String fileSavingFailure = messageSource.getMessage(
                "exception.fileException(1)", new Object[]{io.getMessage()}, locale);
        return returnPageWithError(HttpStatus.INTERNAL_SERVER_ERROR, fileSavingFailure, io, locale, httpSession);
    }

    /**
     * @param ve     Spring Boot validation exception from Controller arguments
     * @param locale To determine the lang of a User
     * @return {@link HttpStatus#BAD_REQUEST} 400
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ModelAndView validationException(MethodArgumentNotValidException ve, Locale locale, HttpSession httpSession) {
        Map<String, String> errors = ve.getBindingResult().getAllErrors().stream()
                .collect(Collectors.toMap(objectError ->
                                objectError.getObjectName(),
                        objectError -> objectError.getDefaultMessage()));
        String errorMessages = errors.entrySet().stream()
                .map(e ->
                        messageSource.getMessage("exception.fieldError(2)", new Object[]{e.getKey(), e.getValue()}, locale))
                .collect(Collectors.joining());
        return returnPageWithError(HttpStatus.BAD_REQUEST, errorMessages, ve, locale, httpSession);
    }

    @ExceptionHandler(BindException.class)
    public ModelAndView validationBindingException(BindException be, Locale locale, HttpSession httpSession) {
        Map<String, String> errors = be.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        fieldError -> fieldError.getField(), fieldError -> fieldError.getDefaultMessage()));
        String errorMessages = errors.entrySet().stream()
                .map(e ->
                        messageSource.getMessage("exception.fieldError(2)", new Object[]{e.getKey(), e.getValue()}, locale))
                .collect(Collectors.joining());
        return returnPageWithError(HttpStatus.BAD_REQUEST, errorMessages, be, locale, httpSession);
    }
    
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ModelAndView maxFileSizeException(MaxUploadSizeExceededException ie, Locale locale, HttpSession httpSession) {
        String maxFileSizeMessage = messageSource.getMessage("exception.MaxFileSize(1)", new Object[]{getMaxFileSizeMb()}, locale);
        return returnPageWithError(HttpStatus.NOT_ACCEPTABLE, maxFileSizeMessage, ie, locale, httpSession);
    }
    
    /**
     * @param exception Any specific internal processing exceptions {@link Exception} from Service level
     * @return HttpStatus 500
     */
    @ExceptionHandler(value = Exception.class)
    public ModelAndView internalExceptions(Exception exception, Locale locale, HttpSession httpSession) {
        //code 500
        return returnPageWithError(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage(), exception, locale, httpSession);
    }

    /**
     * 1) Deletes the temporary file if it is exists in the temp directory
     * 1.1) If {@link MapPointsTrimmerApplication#debugModeIsOn()} = true
     * returns {@link ModelAndView} with forwarding to "/error" page with the localized message for a User
     * and the full stack trace as the {@link Model} attribute "debugMessage" and the given {@link HttpStatus}.
     * 1.2) If {@link MapPointsTrimmerApplication#debugModeIsOn()} = false
     * returns {@link ModelAndView} with redirecting to "/trimmer" page with just the
     * localized message for User and the given {@link HttpStatus}
     *
     * @param httpStatus            To be returned to a User page
     * @param localizedErrorMessage Prepared and localized message to be returned to a User as the "userMessage" attribute
     * @param throwable             To be logged here
     * @param locale                To send a possible localized message to the end User from this method.
     */
    ModelAndView returnPageWithError(
            HttpStatus httpStatus, String localizedErrorMessage, Throwable throwable, Locale locale, HttpSession httpSession) {
        log.error(localizedErrorMessage, throwable);

        if (isSingleUserMode()) {
            multipartMainFileService.deleteTempFiles();
        } else {
            multipartMainFileService.deleteTempFile(httpSession.getId());
        }
        ModelAndView mav = new ModelAndView();
        mav.setStatus(httpStatus);
        mav.addObject("userMessage", localizedErrorMessage);
        if (MapPointsTrimmerApplication.debugModeIsOn()) {
            mav.setViewName("forward:/error");
            mav.addObject("debugMessage", fileService.getStackTraceFromLogFile(locale));
        } else {
            mav.setViewName("redirect:/trimmer");
        }
        return mav;
    }
}
