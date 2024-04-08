package dmitryv.lab1.delegators;


import dmitryv.lab1.models.Message;
import dmitryv.lab1.models.Topic;
import dmitryv.lab1.models.User;
import dmitryv.lab1.services.MessageService;
import dmitryv.lab1.services.NotificationService;
import dmitryv.lab1.services.TopicService;
import dmitryv.lab1.services.UserService;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ManageTopicDelegate implements JavaDelegate {

    @Autowired
    private TopicService topicService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserService userService;


    @Override
    public void execute(DelegateExecution execution) {
        String topicName = (String) execution.getVariable("topicName");
        String messageText = (String) execution.getVariable("messageText");
        String username = (String) execution.getVariable("username");

        User user = userService.get(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Topic topic = topicService.getAllTopics().stream()
                .filter(t -> t.getName().equals(topicName))
                .findFirst()
                .orElseGet(() -> {
                    Topic newTopic = new Topic();
                    newTopic.setName(topicName);
                    topicService.createTopicUpdateRecord(newTopic);
                    return newTopic;
                });

        Message message = new Message(user, messageText, user.getName(), LocalDateTime.now());
        message.setTopic(topic);

        boolean messageAdded = messageService.add(message, user);

        if (messageAdded) {
            execution.setVariable("messageAdded", true);
            notificationService.generateInstantNotification(topic, user);
        } else {
            execution.setVariable("messageAdded", false);
            throw new BpmnError("MESSAGE_ADDITION_FAILED");
        }
    }
}