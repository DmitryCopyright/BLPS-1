package dmitryv.lab1.delegators;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dmitryv.lab1.models.Message;
import dmitryv.lab1.requests.MessageReqFilters;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
public class FilterMessagesDelegate implements JavaDelegate {

    @Autowired
    private MessageService messageService;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        Long userId = null;
        String userIdStr = (String) execution.getVariable("userId");
        if (userIdStr != null && !userIdStr.isEmpty()) {
            userId = Long.parseLong(userIdStr);
        }

        String textMessage = (String) execution.getVariable("textMessage");

        String startDateStr = (String) execution.getVariable("startDate");
        String startTimeStr = (String) execution.getVariable("startTime");
        String endDateStr = (String) execution.getVariable("endDate");
        String endTimeStr = (String) execution.getVariable("endTime");

        LocalDateTime startDate = null;
        LocalDateTime endDate = null;

        if (startDateStr != null && startTimeStr != null && endDateStr != null && endTimeStr != null) {
            startDate = LocalDateTime.parse(startDateStr + "T" + startTimeStr);
            endDate = LocalDateTime.parse(endDateStr + "T" + endTimeStr);
        }

        List<Message> filteredMessages = messageService.getFilteredMessages(
                new MessageReqFilters(userId, textMessage, startDate, endDate),
                Optional.ofNullable(userId)
        );

        String filteredMessagesJson = objectMapper.writeValueAsString(filteredMessages);
        execution.setVariable("filteredMessagesJson", filteredMessagesJson);

        saveToFile(filteredMessagesJson, "filter_message.json");
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