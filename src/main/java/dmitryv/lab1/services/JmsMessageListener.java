package dmitryv.lab1.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

@Component
public class JmsMessageListener implements MessageListener {

    @Autowired
    private NotificationService notificationService;

    @Override
    @JmsListener(destination = "MessageQueue", containerFactory = "myFactory")
    public void onMessage(Message message) {
        if (message instanceof TextMessage) {
            TextMessage textMessage = (TextMessage) message;
            try {
                String messageText = textMessage.getText();
                String topicName = textMessage.getStringProperty("topicName"); // Извлечение свойства "topicName"

                if (topicName != null) {
                    notificationService.processReceivedMessage(messageText, topicName);
                }
            } catch (JMSException e) {
                throw new RuntimeException(e);
            }
        }
    }
}