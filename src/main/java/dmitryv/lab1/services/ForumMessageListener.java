package dmitryv.lab1.services;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class ForumMessageListener {

    @RabbitListener(queues = "forumQueue")
    public void receiveMessage(String messageText) {

        System.out.println("Received message: " + messageText);
    }
}
