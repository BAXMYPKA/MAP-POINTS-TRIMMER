package mrbaxmypka.gmail.com.mapPointsTrimmer.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
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

    public boolean verifyAdminCredentials(@NonNull JSONObject adminCredentials) {
        log.info("Admin credentials have been received as {}", adminCredentials);

        try {
            String login = adminCredentials.getString("login");
            String password = adminCredentials.getString("password");

            if (!adminLogin.equals(login) || !adminPassword.equals(password)) {
                log.info("Admin credentials are wrong.");
                return false;
            } else {
                log.info("Admin credentials have been successfully processed.");
                return true;
            }
        } catch (JSONException e) {
            log.warn("One of the credentials is corrupted or undefined!", e);
            return false;
        }
    }
    public boolean verifyAdminCredentials(@NonNull String adminCredentials) {
        log.info("Admin credentials have been received as {}", adminCredentials);

        try {
            JSONObject jsonObject = new JSONObject(adminCredentials);
            String login = jsonObject.getString("login");
            String password = jsonObject.getString("password");

            if (!adminLogin.equals(login) || !adminPassword.equals(password)) {
                log.info("Admin credentials are wrong.");
                return false;
            } else {
                log.info("Admin credentials have been successfully processed.");
                return true;
            }
        } catch (JSONException e) {
            log.warn("One of the credentials is corrupted or undefined!", e);
            return false;
        }
    }
}
