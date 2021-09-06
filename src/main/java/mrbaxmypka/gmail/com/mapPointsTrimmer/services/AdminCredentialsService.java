package mrbaxmypka.gmail.com.mapPointsTrimmer.services;

import lombok.extern.slf4j.Slf4j;
import mrbaxmypka.gmail.com.mapPointsTrimmer.entitiesDto.AdminDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AdminCredentialsService {

    @Value("${trimmer.adminLogin}")
    private String adminLogin;

    @Value("${trimmer.adminPassword}")
    private String adminPassword;

    private final MessageSource messageSource;

    @Autowired
    public AdminCredentialsService(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public boolean verifyAdminCredentials(@NonNull AdminDto adminCredentials) {
        log.info("Admin credentials have been received as {}", adminCredentials);
        String login = adminCredentials.getLogin();
        String password = adminCredentials.getPassword();
        if ((login == null || password == null) || (login.isBlank() || password.isBlank())) {
            log.warn("Login or password is wrong!");
            return false;
        } else if (adminLogin.contentEquals(login) && adminPassword.contentEquals(password)) {
            log.info("Admin credentials have been successfully processed.");
            return true;
        } else {
            log.warn("Admin credentials are wrong.");
            return false;
        }
    }
}
