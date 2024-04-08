package dmitryv.lab1.delegators;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import dmitryv.lab1.models.User;
import dmitryv.lab1.serializers.UserSerializer;
import dmitryv.lab1.services.UserService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;


@Component
public class GetUserDelegate implements JavaDelegate {

    @Autowired
    private UserService userService;

    @Override
    public void execute(DelegateExecution execution) {
        Long userId = (Long) execution.getVariable("userId");
        Optional<User> user = userService.get(userId);
        execution.setVariable("userExists", user.isPresent());
        if (user.isPresent()) {
            String userInfoJson = serializeUser(user.get());
            execution.setVariable("userInfo", serializeUser(user.get()));
            saveToFile(userInfoJson, "userInfo.json");

        }
    }

    private String serializeUser(User user) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            SimpleModule module = new SimpleModule();
            module.addSerializer(User.class, new UserSerializer());
            mapper.registerModule(module);

            return mapper.writeValueAsString(user);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Ошибка при сериализации пользователя.", e);
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
