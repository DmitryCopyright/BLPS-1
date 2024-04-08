package dmitryv.lab1.delegators;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dmitryv.lab1.models.Message;
import dmitryv.lab1.services.MessageService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Component
public class GetAllMessagesDelegate implements JavaDelegate {

    @Autowired
    private MessageService messageService;
    @Autowired
    private SerializeMessagesDelegate serializeMessagesDelegate;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void execute(DelegateExecution execution) {
        List<Message> messages = messageService.getAll();
        try {
            String messagesJson = objectMapper.writeValueAsString(messages);
            execution.setVariable("messagesJson", messagesJson);


            String filePath = "messages.json";
            saveToFile(messagesJson, filePath);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Ошибка при сериализации списка сообщений.", e);
        }
    }

    private void saveToFile(String data, String filePath) {
        try {
            Path path = Paths.get(filePath);
            Files.write(path, data.getBytes(StandardCharsets.UTF_8));
            System.out.println("Данные сохранены в файл: " + path.toAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при записи данных в файл.", e);
        }
    }
}