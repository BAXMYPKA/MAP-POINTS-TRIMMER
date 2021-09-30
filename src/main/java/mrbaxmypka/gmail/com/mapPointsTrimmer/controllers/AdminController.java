package mrbaxmypka.gmail.com.mapPointsTrimmer.controllers;

import lombok.extern.slf4j.Slf4j;
import mrbaxmypka.gmail.com.mapPointsTrimmer.entitiesDto.AdminDto;
import mrbaxmypka.gmail.com.mapPointsTrimmer.services.AdminCredentialsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Locale;

@Slf4j
@RestController
public class AdminController extends AbstractController {

    @Autowired
    private AdminCredentialsService credentialsService;

    @PostMapping(path = "/admin", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> verityAdminCredentials(@RequestBody AdminDto adminDto, Locale locale, HttpSession httpSession) {
        log.info("Admin credentials have been received as {}.", adminDto);
        if (credentialsService.verifyAdminCredentials(adminDto)) {
            boolean isSessionAlive = getWebSessionService().setAdminSession(httpSession.getId());
            if (isSessionAlive) {
                return ResponseEntity.ok(getMessageSource().getMessage("userMessage.adminLoginSuccess", null, locale));
            } else {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(getMessageSource().getMessage("userMessage.adminLoginSessionTimeout", null, locale));
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(getMessageSource().getMessage("userMessage.badAdminLogin", null, locale));
        }
    }
}
