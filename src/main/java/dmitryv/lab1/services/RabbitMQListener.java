package dmitryv.lab1.services;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class RabbitMQListener {
    private static final Logger logger = LoggerFactory.getLogger(RabbitMQListener.class);


    @Autowired
    private NotificationService notificationService;

    @RabbitListener(queues = "MessageQueue")
    public void receiveMessage(org.springframework.amqp.core.Message rabbitMessage) {
        String messageBody = new String(rabbitMessage.getBody());
        System.out.println("Received JMS message from RabbitMQ: " + messageBody);

        String topicName = rabbitMessage.getMessageProperties().getHeader("topicName");
        if (topicName != null) {
            notificationService.processReceivedMessage(messageBody, topicName);
        } else {
            logger.error("Received message without 'topicName' header. Message body: {}", messageBody);
        }
    }
}