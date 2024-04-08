package dmitryv.lab1.delegators;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dmitryv.lab1.services.NotificationService;
import org.camunda.bpm.engine.AuthenticationException;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;


@Component
public class GetNotificationsDelegate implements JavaDelegate {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private AuthenticationManager authenticationManager;

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void execute(DelegateExecution execution) throws JsonProcessingException {
        final String userEmail = "s@mail.ru";
        final String password = "string";

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(userEmail, password));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            List<String> notifications = notificationService.getNotificationsForUser(userEmail);

            String notificationsJson = mapper.writeValueAsString(notifications);
            execution.setVariable("notifications", notificationsJson);

            saveToFile(notificationsJson, "notifications.json");
        } catch (AuthenticationException e) {
            execution.setVariable("error", "Authentication failed for user: " + userEmail);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    private void saveToFile(String data, String filePath) {
        try {
            Path path = Paths.get(filePath);
            Files.write(path, data.getBytes());
            System.out.println("Данные сохранены в файл: " + path.toAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при записи данных в файл.", e);
        }
    }
}
