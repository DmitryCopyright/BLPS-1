package dmitryv.lab1.delegators;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dmitryv.lab1.models.Topic;
import dmitryv.lab1.services.TopicService;
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
public class GetAllTopicsDelegate implements JavaDelegate {

    @Autowired
    private TopicService topicService;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Override
    public void execute(DelegateExecution execution) throws JsonProcessingException {
        List<Topic> topics = topicService.getAllTopics();
        String topicsJson = objectMapper.writeValueAsString(topics);
        execution.setVariable("topics", topicsJson);

        String filePath = "topics.json";
        saveToFile(topicsJson, filePath);
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