package dmitryv.lab1.delegators;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dmitryv.lab1.models.Message;
import dmitryv.lab1.models.Topic;
import dmitryv.lab1.repos.TopicRepo;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class GetTopicMessagesDelegate implements JavaDelegate {

    @Autowired
    private TopicRepo topicRepo;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Override
    public void execute(DelegateExecution execution) throws JsonProcessingException {
        String topicName = (String) execution.getVariable("topicName");
        Optional<Topic> topic = topicRepo.findByName(topicName);

        if (topic.isPresent()) {
            List<Message> messages = new ArrayList<>(topic.get().getMessages());
            String messagesJson = objectMapper.writeValueAsString(messages);
            execution.setVariable("messagesJson", messagesJson);
            saveToFile(messagesJson, "topicInfo.json");
        } else {
            execution.setVariable("messagesJson", "[]");
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