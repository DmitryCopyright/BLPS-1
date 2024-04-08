package dmitryv.lab1.delegators;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.List;

import dmitryv.lab1.models.Message;
import org.springframework.stereotype.Component;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.delegate.DelegateExecution;

@Component("serializeMessagesDelegate")
public class SerializeMessagesDelegate implements JavaDelegate {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void execute(DelegateExecution execution) {
        @SuppressWarnings("unchecked")
        List<Message> messages = (List<Message>) execution.getVariable("messagesList");
        try {
            String messagesJson = objectMapper.writeValueAsString(messages);
            execution.setVariable("messagesJson", messagesJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing messages", e);
        }
    }
}
