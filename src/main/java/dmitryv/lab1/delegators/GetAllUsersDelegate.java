package dmitryv.lab1.delegators;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dmitryv.lab1.models.User;
import dmitryv.lab1.services.UserService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Component
public class GetAllUsersDelegate implements JavaDelegate {

    private final UserService userService;

    @Autowired
    public GetAllUsersDelegate(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        List<User> users = userService.getAll();
        saveUsersToFile(users);
        String usersJson = serializeUsers(users);
        execution.setVariable("users", usersJson);
    }

    private String serializeUsers(List<User> users) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(users);
    }

    private void saveUsersToFile(List<User> users) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String usersJson = mapper.writeValueAsString(users);
        Path path = Paths.get("allUsers.json");
        Files.write(path, usersJson.getBytes());
    }
}